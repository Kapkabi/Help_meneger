"""Data-access layer. All SQL lives here; UI and core/ never write SQL directly."""

import sqlite3
import uuid
from typing import List, Optional

from core.exceptions import RepositoryError, ValidationError
from data.database import Database
from models.recipe import Category, Recipe, RecipeIngredient

SORT_COLUMNS = {
    "title": "r.title",
    "cook_time": "r.cook_time_minutes",
    "created_at": "r.created_at",
}


class CategoryRepository:
    def __init__(self, db: Database):
        self.db = db

    def list_all(self) -> List[Category]:
        try:
            cursor = self.db.connection.execute(
                "SELECT id, name, is_builtin FROM categories ORDER BY is_builtin DESC, name"
            )
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось загрузить категории: {exc}") from exc
        return [
            Category(id=row["id"], name=row["name"], is_builtin=bool(row["is_builtin"]))
            for row in cursor.fetchall()
        ]

    def get_or_create(self, name: str) -> Category:
        name = (name or "").strip()
        if not name:
            raise ValidationError("Название категории не может быть пустым")
        try:
            rows = self.db.connection.execute(
                "SELECT id, name, is_builtin FROM categories"
            ).fetchall()
            for row in rows:
                if row["name"].casefold() == name.casefold():
                    return Category(id=row["id"], name=row["name"], is_builtin=bool(row["is_builtin"]))
            with self.db.transaction() as conn:
                cursor = conn.execute(
                    "INSERT INTO categories (name, is_builtin) VALUES (?, 0)", (name,)
                )
                new_id = cursor.lastrowid
            return Category(id=new_id, name=name, is_builtin=False)
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось создать категорию «{name}»: {exc}") from exc


class IngredientRepository:
    def __init__(self, db: Database):
        self.db = db

    def list_all(self) -> List[str]:
        try:
            rows = self.db.connection.execute("SELECT name FROM ingredients ORDER BY name").fetchall()
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось загрузить список ингредиентов: {exc}") from exc
        return [row["name"] for row in rows]

    def get_or_create(self, name: str, conn: Optional[sqlite3.Connection] = None):
        name = (name or "").strip()
        if not name:
            raise ValidationError("Название ингредиента не может быть пустым")
        connection = conn or self.db.connection
        rows = connection.execute("SELECT id, name FROM ingredients").fetchall()
        for row in rows:
            if row["name"].casefold() == name.casefold():
                return row["id"]
        cursor = connection.execute("INSERT INTO ingredients (name) VALUES (?)", (name,))
        return cursor.lastrowid


class UnitRepository:
    """Dictionary of measurement units (гр/шт/л/...), grown via get_or_create."""

    def __init__(self, db: Database):
        self.db = db

    def list_all(self) -> List[str]:
        try:
            rows = self.db.connection.execute("SELECT name FROM units ORDER BY name").fetchall()
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось загрузить список единиц измерения: {exc}") from exc
        return [row["name"] for row in rows]

    def get_or_create(self, name: str, conn: Optional[sqlite3.Connection] = None):
        name = (name or "").strip()
        if not name:
            raise ValidationError("Единица измерения не может быть пустой")
        connection = conn or self.db.connection
        rows = connection.execute("SELECT id, name FROM units").fetchall()
        for row in rows:
            if row["name"].casefold() == name.casefold():
                return row["id"]
        cursor = connection.execute("INSERT INTO units (name) VALUES (?)", (name,))
        return cursor.lastrowid


class RecipeRepository:
    def __init__(self, db: Database):
        self.db = db
        self.ingredients = IngredientRepository(db)
        self.units = UnitRepository(db)

    def create(self, recipe: Recipe) -> str:
        recipe_id = recipe.id or str(uuid.uuid4())
        try:
            with self.db.transaction() as conn:
                conn.execute(
                    """INSERT INTO recipes (id, title, steps, cook_time_minutes, category_id, photo_path)
                       VALUES (?, ?, ?, ?, ?, ?)""",
                    (recipe_id, recipe.title, recipe.steps, recipe.cook_time_minutes,
                     recipe.category_id, recipe.photo_path),
                )
                self._save_ingredients(conn, recipe_id, recipe.ingredients)
            return recipe_id
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось сохранить рецепт: {exc}") from exc

    def update(self, recipe: Recipe) -> None:
        if recipe.id is None:
            raise ValidationError("Невозможно обновить рецепт без идентификатора")
        try:
            with self.db.transaction() as conn:
                cursor = conn.execute(
                    """UPDATE recipes SET title = ?, steps = ?, cook_time_minutes = ?,
                       category_id = ?, photo_path = ?, updated_at = datetime('now')
                       WHERE id = ?""",
                    (recipe.title, recipe.steps, recipe.cook_time_minutes, recipe.category_id,
                     recipe.photo_path, recipe.id),
                )
                if cursor.rowcount == 0:
                    raise RepositoryError(f"Рецепт с id={recipe.id} не найден")
                self._save_ingredients(conn, recipe.id, recipe.ingredients)
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось обновить рецепт: {exc}") from exc

    def delete(self, recipe_id: str) -> None:
        try:
            with self.db.transaction() as conn:
                cursor = conn.execute("DELETE FROM recipes WHERE id = ?", (recipe_id,))
                if cursor.rowcount == 0:
                    raise RepositoryError(f"Рецепт с id={recipe_id} не найден")
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось удалить рецепт: {exc}") from exc

    def get_by_id(self, recipe_id: str) -> Optional[Recipe]:
        try:
            row = self.db.connection.execute(
                """SELECT r.*, c.name AS category_name FROM recipes r
                   LEFT JOIN categories c ON c.id = r.category_id
                   WHERE r.id = ?""",
                (recipe_id,),
            ).fetchone()
        except sqlite3.Error as exc:
            raise RepositoryError(f"Не удалось загрузить рецепт: {exc}") from exc
        if row is None:
            return None
        return self._row_to_recipe(row, self._load_ingredients(recipe_id))

    def search(
        self,
        text: str = "",
        category_id: Optional[int] = None,
        ingredient_text: str = "",
        sort_by: str = "title",
        sort_order: str = "ASC",
    ) -> List[Recipe]:
        order_column = SORT_COLUMNS.get(sort_by, "r.title")
        order_direction = "DESC" if sort_order.upper() == "DESC" else "ASC"

        query = [
            """SELECT DISTINCT r.id, r.title, r.steps, r.cook_time_minutes, r.category_id,
                      r.photo_path, r.created_at, r.updated_at, c.name AS category_name
               FROM recipes r
               LEFT JOIN categories c ON c.id = r.category_id
               LEFT JOIN recipe_ingredients ri ON ri.recipe_id = r.id
               LEFT JOIN ingredients i ON i.id = ri.ingredient_id
               WHERE 1 = 1"""
        ]
        params: list = []
        if text:
            query.append("AND r.title LIKE ?")
            params.append(f"%{text}%")
        if category_id:
            query.append("AND r.category_id = ?")
            params.append(category_id)
        if ingredient_text:
            query.append("AND i.name LIKE ?")
            params.append(f"%{ingredient_text}%")
        query.append(f"ORDER BY {order_column} {order_direction}")

        try:
            rows = self.db.connection.execute(" ".join(query), params).fetchall()
        except sqlite3.Error as exc:
            raise RepositoryError(f"Ошибка поиска рецептов: {exc}") from exc
        return [self._row_to_recipe(row, self._load_ingredients(row["id"])) for row in rows]

    def _save_ingredients(self, conn: sqlite3.Connection, recipe_id: str, ingredients: List[RecipeIngredient]) -> None:
        conn.execute("DELETE FROM recipe_ingredients WHERE recipe_id = ?", (recipe_id,))
        for order, ingredient in enumerate(ingredients):
            ingredient_id = self.ingredients.get_or_create(ingredient.name, conn=conn)
            if ingredient.unit and ingredient.unit.strip():
                self.units.get_or_create(ingredient.unit, conn=conn)
            conn.execute(
                """INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, sort_order)
                   VALUES (?, ?, ?, ?, ?)""",
                (recipe_id, ingredient_id, ingredient.quantity, ingredient.unit, order),
            )

    def _load_ingredients(self, recipe_id: str) -> List[RecipeIngredient]:
        rows = self.db.connection.execute(
            """SELECT ri.id, ri.ingredient_id, i.name, ri.quantity, ri.unit
               FROM recipe_ingredients ri
               JOIN ingredients i ON i.id = ri.ingredient_id
               WHERE ri.recipe_id = ?
               ORDER BY ri.sort_order""",
            (recipe_id,),
        ).fetchall()
        return [
            RecipeIngredient(
                id=row["id"], ingredient_id=row["ingredient_id"], name=row["name"],
                quantity=row["quantity"], unit=row["unit"],
            )
            for row in rows
        ]

    @staticmethod
    def _row_to_recipe(row: sqlite3.Row, ingredients: List[RecipeIngredient]) -> Recipe:
        return Recipe(
            id=row["id"],
            title=row["title"],
            steps=row["steps"],
            cook_time_minutes=row["cook_time_minutes"],
            category_id=row["category_id"],
            category_name=row["category_name"] or "",
            photo_path=row["photo_path"],
            created_at=row["created_at"],
            updated_at=row["updated_at"],
            ingredients=ingredients,
        )

"""Business logic / validation layer sitting between the UI and the data layer.

UI widgets call only this service; they never touch repositories or SQL.
"""

from typing import List, Optional

from core.exceptions import ValidationError
from data.database import Database
from data.repositories import CategoryRepository, RecipeRepository
from models.recipe import Category, Recipe, RecipeIngredient

MAX_COOK_TIME_MINUTES = 24 * 60


class RecipeService:
    def __init__(self, db: Database):
        self.recipe_repo = RecipeRepository(db)
        self.category_repo = CategoryRepository(db)

    def list_categories(self) -> List[Category]:
        return self.category_repo.list_all()

    def list_ingredient_names(self) -> List[str]:
        return self.recipe_repo.ingredients.list_all()

    def list_units(self) -> List[str]:
        return self.recipe_repo.units.list_all()

    def get_recipe(self, recipe_id: str) -> Optional[Recipe]:
        return self.recipe_repo.get_by_id(recipe_id)

    def search_recipes(
        self,
        text: str = "",
        category_id: Optional[int] = None,
        ingredient_text: str = "",
        sort_by: str = "title",
        sort_order: str = "ASC",
    ) -> List[Recipe]:
        return self.recipe_repo.search(text, category_id, ingredient_text, sort_by, sort_order)

    def create_recipe(
        self,
        title: str,
        steps: str,
        cook_time_minutes,
        category_name: str,
        photo_path: Optional[str],
        ingredients: List[RecipeIngredient],
    ) -> str:
        cook_time = self._validate(title, steps, cook_time_minutes, ingredients)
        category = self.category_repo.get_or_create(category_name) if category_name else None
        recipe = Recipe(
            id=None,
            title=title.strip(),
            steps=steps.strip(),
            cook_time_minutes=cook_time,
            category_id=category.id if category else None,
            photo_path=photo_path or None,
            ingredients=ingredients,
        )
        return self.recipe_repo.create(recipe)

    def update_recipe(
        self,
        recipe_id: str,
        title: str,
        steps: str,
        cook_time_minutes,
        category_name: str,
        photo_path: Optional[str],
        ingredients: List[RecipeIngredient],
    ) -> None:
        cook_time = self._validate(title, steps, cook_time_minutes, ingredients)
        category = self.category_repo.get_or_create(category_name) if category_name else None
        recipe = Recipe(
            id=recipe_id,
            title=title.strip(),
            steps=steps.strip(),
            cook_time_minutes=cook_time,
            category_id=category.id if category else None,
            photo_path=photo_path or None,
            ingredients=ingredients,
        )
        self.recipe_repo.update(recipe)

    def delete_recipe(self, recipe_id: str) -> None:
        self.recipe_repo.delete(recipe_id)

    @staticmethod
    def _validate(title: str, steps: str, cook_time_minutes, ingredients: List[RecipeIngredient]) -> int:
        errors = []

        if not title or not title.strip():
            errors.append("Название рецепта не может быть пустым")

        if not steps or not steps.strip():
            errors.append("Шаги приготовления не могут быть пустыми")

        cook_time = 0
        try:
            cook_time = int(cook_time_minutes)
            if cook_time < 0 or cook_time > MAX_COOK_TIME_MINUTES:
                errors.append(f"Время приготовления должно быть от 0 до {MAX_COOK_TIME_MINUTES} минут")
        except (TypeError, ValueError):
            errors.append("Время приготовления должно быть числом")

        if not ingredients:
            errors.append("Добавьте хотя бы один ингредиент")
        else:
            for ingredient in ingredients:
                if not ingredient.name or not ingredient.name.strip():
                    errors.append("Название ингредиента не может быть пустым")
                    break
                if ingredient.quantity < 0:
                    errors.append(f"Количество для «{ingredient.name}» не может быть отрицательным")
                    break

        if errors:
            raise ValidationError("; ".join(errors))

        return cook_time

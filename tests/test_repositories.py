import uuid

import pytest

from core.exceptions import RepositoryError, ValidationError
from models.recipe import Recipe, RecipeIngredient


def make_recipe(title="Омлет", category_id=None):
    return Recipe(
        id=None,
        title=title,
        steps="Взбить яйца. Вылить на сковороду. Готовить 5 минут.",
        cook_time_minutes=10,
        category_id=category_id,
        ingredients=[
            RecipeIngredient(id=None, ingredient_id=None, name="Яйца", quantity=3, unit="шт"),
            RecipeIngredient(id=None, ingredient_id=None, name="Молоко", quantity=50, unit="мл"),
        ],
    )


def test_create_and_get_recipe(recipe_repo):
    recipe_id = recipe_repo.create(make_recipe())
    fetched = recipe_repo.get_by_id(recipe_id)

    assert fetched is not None
    assert fetched.title == "Омлет"
    assert len(fetched.ingredients) == 2
    assert {i.name for i in fetched.ingredients} == {"Яйца", "Молоко"}


def test_create_recipe_id_is_a_uuid_string(recipe_repo):
    recipe_id = recipe_repo.create(make_recipe())

    assert isinstance(recipe_id, str)
    assert uuid.UUID(recipe_id)  # raises ValueError if not a valid UUID


def test_create_recipe_ids_are_unique_across_offline_creates(recipe_repo):
    first_id = recipe_repo.create(make_recipe(title="Рецепт А"))
    second_id = recipe_repo.create(make_recipe(title="Рецепт Б"))

    assert first_id != second_id


def test_update_recipe_replaces_ingredients(recipe_repo):
    recipe_id = recipe_repo.create(make_recipe())
    recipe = recipe_repo.get_by_id(recipe_id)
    recipe.title = "Омлет с сыром"
    recipe.ingredients = [RecipeIngredient(id=None, ingredient_id=None, name="Сыр", quantity=30, unit="г")]

    recipe_repo.update(recipe)
    updated = recipe_repo.get_by_id(recipe_id)

    assert updated.title == "Омлет с сыром"
    assert len(updated.ingredients) == 1
    assert updated.ingredients[0].name == "Сыр"


def test_update_missing_recipe_raises(recipe_repo):
    ghost = make_recipe()
    ghost.id = "00000000-0000-0000-0000-000000000000"
    with pytest.raises(RepositoryError):
        recipe_repo.update(ghost)


def test_delete_recipe_removes_it_and_its_ingredients(db, recipe_repo):
    recipe_id = recipe_repo.create(make_recipe())
    recipe_repo.delete(recipe_id)

    assert recipe_repo.get_by_id(recipe_id) is None
    remaining = db.connection.execute(
        "SELECT COUNT(*) FROM recipe_ingredients WHERE recipe_id = ?", (recipe_id,)
    ).fetchone()[0]
    assert remaining == 0


def test_delete_missing_recipe_raises(recipe_repo):
    with pytest.raises(RepositoryError):
        recipe_repo.delete("00000000-0000-0000-0000-000000000000")


def test_search_by_title(recipe_repo):
    recipe_repo.create(make_recipe(title="Блинчики"))
    recipe_repo.create(make_recipe(title="Омлет"))

    results = recipe_repo.search(text="Блин")
    assert len(results) == 1
    assert results[0].title == "Блинчики"


def test_search_by_ingredient(recipe_repo):
    recipe_repo.create(make_recipe(title="Омлет"))
    recipe_repo.create(
        Recipe(
            id=None, title="Салат", steps="Смешать.", cook_time_minutes=5,
            ingredients=[RecipeIngredient(id=None, ingredient_id=None, name="Огурец", quantity=1, unit="шт")],
        )
    )

    results = recipe_repo.search(ingredient_text="Яйца")
    assert len(results) == 1
    assert results[0].title == "Омлет"


def test_search_sort_by_cook_time_desc(recipe_repo):
    recipe_repo.create(make_recipe(title="Быстрый"))
    slow = make_recipe(title="Долгий")
    slow.cook_time_minutes = 120
    recipe_repo.create(slow)

    results = recipe_repo.search(sort_by="cook_time", sort_order="DESC")
    assert [r.title for r in results] == ["Долгий", "Быстрый"]


def test_category_get_or_create_reuses_existing(category_repo):
    first = category_repo.get_or_create("Напитки")
    second = category_repo.get_or_create("напитки")
    assert first.id == second.id


def test_category_get_or_create_rejects_empty_name(category_repo):
    with pytest.raises(ValidationError):
        category_repo.get_or_create("   ")


def test_unit_dictionary_seeded_with_builtin_units(recipe_repo):
    assert {"гр", "шт", "л"}.issubset(set(recipe_repo.units.list_all()))


def test_unit_get_or_create_reuses_existing_case_insensitively(recipe_repo):
    first = recipe_repo.units.get_or_create("Гр")
    second = recipe_repo.units.get_or_create("гр")
    assert first == second


def test_unit_get_or_create_rejects_empty_name(recipe_repo):
    with pytest.raises(ValidationError):
        recipe_repo.units.get_or_create("   ")


def test_saving_recipe_grows_unit_dictionary(recipe_repo):
    recipe = make_recipe()
    recipe.ingredients.append(
        RecipeIngredient(id=None, ingredient_id=None, name="Соль", quantity=1, unit="щепотка")
    )
    recipe_repo.create(recipe)

    assert "щепотка" in recipe_repo.units.list_all()


def test_ingredient_list_all_returns_known_names(recipe_repo):
    recipe_repo.create(make_recipe())
    assert {"Яйца", "Молоко"}.issubset(set(recipe_repo.ingredients.list_all()))

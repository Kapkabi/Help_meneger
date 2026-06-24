import pytest

from core.exceptions import ValidationError
from models.recipe import RecipeIngredient


def make_ingredients():
    return [RecipeIngredient(id=None, ingredient_id=None, name="Мука", quantity=200, unit="г")]


def test_create_recipe_success(service):
    recipe_id = service.create_recipe(
        title="Блины", steps="Замесить тесто. Выпекать.", cook_time_minutes=20,
        category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
    )
    recipe = service.get_recipe(recipe_id)
    assert recipe.title == "Блины"
    assert recipe.category_name == "Завтрак"


def test_create_recipe_grows_dictionaries(service):
    service.create_recipe(
        title="Блины", steps="Замесить тесто. Выпекать.", cook_time_minutes=20,
        category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
    )
    assert "Мука" in service.list_ingredient_names()
    assert "г" in service.list_units()


def test_list_units_includes_builtin_units(service):
    assert {"гр", "шт", "л"}.issubset(set(service.list_units()))


def test_create_recipe_rejects_empty_title(service):
    with pytest.raises(ValidationError):
        service.create_recipe(
            title="  ", steps="Шаги", cook_time_minutes=10,
            category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
        )


def test_create_recipe_rejects_empty_steps(service):
    with pytest.raises(ValidationError):
        service.create_recipe(
            title="Блины", steps="   ", cook_time_minutes=10,
            category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
        )


def test_create_recipe_rejects_negative_cook_time(service):
    with pytest.raises(ValidationError):
        service.create_recipe(
            title="Блины", steps="Шаги", cook_time_minutes=-5,
            category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
        )


def test_create_recipe_rejects_non_numeric_cook_time(service):
    with pytest.raises(ValidationError):
        service.create_recipe(
            title="Блины", steps="Шаги", cook_time_minutes="много",
            category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
        )


def test_create_recipe_rejects_no_ingredients(service):
    with pytest.raises(ValidationError):
        service.create_recipe(
            title="Блины", steps="Шаги", cook_time_minutes=10,
            category_name="Завтрак", photo_path=None, ingredients=[],
        )


def test_create_recipe_rejects_ingredient_without_name(service):
    with pytest.raises(ValidationError):
        service.create_recipe(
            title="Блины", steps="Шаги", cook_time_minutes=10, category_name="Завтрак",
            photo_path=None,
            ingredients=[RecipeIngredient(id=None, ingredient_id=None, name="  ", quantity=1, unit="шт")],
        )


def test_update_recipe_success(service):
    recipe_id = service.create_recipe(
        title="Блины", steps="Шаги", cook_time_minutes=10,
        category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
    )
    service.update_recipe(
        recipe_id, title="Блины с медом", steps="Новые шаги", cook_time_minutes=15,
        category_name="Десерт", photo_path=None, ingredients=make_ingredients(),
    )
    updated = service.get_recipe(recipe_id)
    assert updated.title == "Блины с медом"
    assert updated.category_name == "Десерт"


def test_delete_recipe(service):
    recipe_id = service.create_recipe(
        title="Блины", steps="Шаги", cook_time_minutes=10,
        category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
    )
    service.delete_recipe(recipe_id)
    assert service.get_recipe(recipe_id) is None


def test_search_recipes_by_category(service):
    service.create_recipe(
        title="Блины", steps="Шаги", cook_time_minutes=10,
        category_name="Завтрак", photo_path=None, ingredients=make_ingredients(),
    )
    service.create_recipe(
        title="Торт", steps="Шаги", cook_time_minutes=60,
        category_name="Десерт", photo_path=None, ingredients=make_ingredients(),
    )
    categories = {c.name: c.id for c in service.list_categories()}

    results = service.search_recipes(category_id=categories["Десерт"])
    assert [r.title for r in results] == ["Торт"]

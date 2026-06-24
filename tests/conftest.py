import pytest

from core.recipe_service import RecipeService
from data.database import Database
from data.repositories import CategoryRepository, RecipeRepository


@pytest.fixture
def db(tmp_path):
    database = Database(db_path=tmp_path / "test_recipes.db")
    database.connect()
    yield database
    database.close()


@pytest.fixture
def recipe_repo(db):
    return RecipeRepository(db)


@pytest.fixture
def category_repo(db):
    return CategoryRepository(db)


@pytest.fixture
def service(db):
    return RecipeService(db)

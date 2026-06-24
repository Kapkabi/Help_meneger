from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class Category:
    id: Optional[int]
    name: str
    is_builtin: bool = False


@dataclass
class RecipeIngredient:
    """An ingredient line attached to a recipe (quantity + unit)."""

    id: Optional[int]
    ingredient_id: Optional[int]
    name: str
    quantity: float = 0.0
    unit: str = ""


@dataclass
class Recipe:
    id: Optional[str]
    title: str
    steps: str
    cook_time_minutes: int = 0
    category_id: Optional[int] = None
    category_name: str = ""
    photo_path: Optional[str] = None
    created_at: Optional[str] = None
    updated_at: Optional[str] = None
    ingredients: List[RecipeIngredient] = field(default_factory=list)

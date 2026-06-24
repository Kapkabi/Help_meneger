from pathlib import Path

from PySide6.QtCore import Qt
from PySide6.QtGui import QPixmap
from PySide6.QtWidgets import QLabel, QScrollArea, QVBoxLayout, QWidget

from models.recipe import Recipe


class RecipeDetailWidget(QWidget):
    """Read-only detail card for the currently selected recipe."""

    def __init__(self, parent=None):
        super().__init__(parent)
        layout = QVBoxLayout(self)

        self.photo_label = QLabel()
        self.photo_label.setFixedHeight(220)
        self.photo_label.setAlignment(Qt.AlignCenter)
        self.photo_label.setStyleSheet("background-color: #f0f0f0; border-radius: 6px;")
        layout.addWidget(self.photo_label)

        self.title_label = QLabel()
        self.title_label.setStyleSheet("font-size: 20px; font-weight: bold;")
        layout.addWidget(self.title_label)

        self.meta_label = QLabel()
        self.meta_label.setStyleSheet("color: gray;")
        layout.addWidget(self.meta_label)

        layout.addWidget(QLabel("<b>Ингредиенты</b>"))
        self.ingredients_label = QLabel()
        self.ingredients_label.setWordWrap(True)
        layout.addWidget(self.ingredients_label)

        layout.addWidget(QLabel("<b>Приготовление</b>"))
        self.steps_label = QLabel()
        self.steps_label.setWordWrap(True)
        self.steps_label.setAlignment(Qt.AlignTop | Qt.AlignLeft)
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll.setWidget(self.steps_label)
        layout.addWidget(scroll, 1)

        self.clear()

    def show_recipe(self, recipe: Recipe) -> None:
        self.title_label.setText(recipe.title)
        self.meta_label.setText(f"{recipe.category_name or 'Без категории'} • {recipe.cook_time_minutes} мин")

        pixmap = QPixmap(recipe.photo_path) if recipe.photo_path and Path(recipe.photo_path).exists() else None
        if pixmap and not pixmap.isNull():
            self.photo_label.setPixmap(
                pixmap.scaled(self.photo_label.width() or 400, 220, Qt.KeepAspectRatio, Qt.SmoothTransformation)
            )
        else:
            self.photo_label.clear()
            self.photo_label.setText("Нет фото")

        ingredients_text = "<br>".join(
            f"• {ingredient.name} — {ingredient.quantity:g} {ingredient.unit}".strip()
            for ingredient in recipe.ingredients
        ) or "Нет ингредиентов"
        self.ingredients_label.setText(ingredients_text)
        self.steps_label.setText(recipe.steps)

    def clear(self) -> None:
        self.title_label.setText("Выберите рецепт")
        self.meta_label.clear()
        self.photo_label.clear()
        self.ingredients_label.clear()
        self.steps_label.clear()

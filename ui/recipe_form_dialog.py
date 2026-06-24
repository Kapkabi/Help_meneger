from typing import Optional

from PySide6.QtCore import Qt
from PySide6.QtWidgets import (
    QComboBox,
    QDialog,
    QFileDialog,
    QFormLayout,
    QHBoxLayout,
    QHeaderView,
    QLabel,
    QLineEdit,
    QMessageBox,
    QPushButton,
    QSpinBox,
    QTableWidget,
    QTableWidgetItem,
    QTextEdit,
    QVBoxLayout,
)

from core.exceptions import ValidationError
from core.recipe_service import MAX_COOK_TIME_MINUTES, RecipeService
from models.recipe import Recipe, RecipeIngredient

INGREDIENT_COLUMNS = ["Ингредиент", "Количество", "Ед. изм."]


class RecipeFormDialog(QDialog):
    """Add/edit dialog. On success it has already persisted the recipe via the service."""

    def __init__(self, service: RecipeService, recipe: Optional[Recipe] = None, parent=None):
        super().__init__(parent)
        self.service = service
        self.recipe = recipe
        self.photo_path = recipe.photo_path if recipe else None
        self.ingredient_names = service.list_ingredient_names()
        self.unit_names = service.list_units()
        self.setWindowTitle("Редактировать рецепт" if recipe else "Новый рецепт")
        self.setMinimumWidth(520)
        self._build_ui()
        if recipe:
            self._load_recipe(recipe)
        else:
            self._add_ingredient_row()

    def _build_ui(self) -> None:
        layout = QVBoxLayout(self)
        form = QFormLayout()

        self.title_edit = QLineEdit()
        form.addRow("Название*", self.title_edit)

        self.category_combo = QComboBox()
        self.category_combo.setEditable(True)
        for category in self.service.list_categories():
            self.category_combo.addItem(category.name)
        form.addRow("Категория", self.category_combo)

        self.cook_time_spin = QSpinBox()
        self.cook_time_spin.setRange(0, MAX_COOK_TIME_MINUTES)
        self.cook_time_spin.setSuffix(" мин")
        form.addRow("Время готовки", self.cook_time_spin)

        photo_row = QHBoxLayout()
        self.photo_edit = QLineEdit()
        self.photo_edit.setReadOnly(True)
        browse_btn = QPushButton("Обзор…")
        browse_btn.clicked.connect(self._browse_photo)
        clear_btn = QPushButton("Убрать")
        clear_btn.clicked.connect(self._clear_photo)
        photo_row.addWidget(self.photo_edit)
        photo_row.addWidget(browse_btn)
        photo_row.addWidget(clear_btn)
        form.addRow("Фото", photo_row)

        layout.addLayout(form)

        layout.addWidget(QLabel("Ингредиенты*"))
        self.ingredients_table = QTableWidget(0, len(INGREDIENT_COLUMNS))
        self.ingredients_table.setHorizontalHeaderLabels(INGREDIENT_COLUMNS)
        self.ingredients_table.horizontalHeader().setSectionResizeMode(0, QHeaderView.Stretch)
        layout.addWidget(self.ingredients_table)

        ingredient_buttons = QHBoxLayout()
        add_btn = QPushButton("Добавить ингредиент")
        add_btn.clicked.connect(self._add_ingredient_row)
        remove_btn = QPushButton("Удалить выбранный")
        remove_btn.clicked.connect(self._remove_ingredient_row)
        ingredient_buttons.addWidget(add_btn)
        ingredient_buttons.addWidget(remove_btn)
        ingredient_buttons.addStretch()
        layout.addLayout(ingredient_buttons)

        layout.addWidget(QLabel("Шаги приготовления*"))
        self.steps_edit = QTextEdit()
        layout.addWidget(self.steps_edit)

        buttons = QHBoxLayout()
        save_btn = QPushButton("Сохранить")
        save_btn.clicked.connect(self._on_save)
        cancel_btn = QPushButton("Отмена")
        cancel_btn.clicked.connect(self.reject)
        buttons.addStretch()
        buttons.addWidget(save_btn)
        buttons.addWidget(cancel_btn)
        layout.addLayout(buttons)

    def _add_ingredient_row(self, name: str = "", quantity="", unit: str = "") -> None:
        row = self.ingredients_table.rowCount()
        self.ingredients_table.insertRow(row)
        self.ingredients_table.setCellWidget(row, 0, self._make_dictionary_combo(self.ingredient_names, name))
        self.ingredients_table.setItem(row, 1, QTableWidgetItem(str(quantity)))
        self.ingredients_table.setCellWidget(row, 2, self._make_dictionary_combo(self.unit_names, unit))

    def _make_dictionary_combo(self, items: list, current_text: str) -> QComboBox:
        """Editable dropdown over a growable dictionary (ingredients/units).

        Typing a value not yet in the list is allowed; it is persisted into
        the dictionary table (with a case-insensitive uniqueness check) the
        next time the recipe is saved (see RecipeRepository._save_ingredients).
        """
        combo = QComboBox()
        combo.setEditable(True)
        combo.addItems(items)
        combo.setInsertPolicy(QComboBox.NoInsert)
        completer = combo.completer()
        if completer is not None:
            completer.setCaseSensitivity(Qt.CaseInsensitive)
            completer.setFilterMode(Qt.MatchContains)
        if current_text:
            index = combo.findText(current_text, Qt.MatchFixedString)
            if index >= 0:
                combo.setCurrentIndex(index)
            else:
                combo.setCurrentText(current_text)
        return combo

    def _remove_ingredient_row(self) -> None:
        row = self.ingredients_table.currentRow()
        if row >= 0:
            self.ingredients_table.removeRow(row)

    def _browse_photo(self) -> None:
        path, _ = QFileDialog.getOpenFileName(
            self, "Выберите фото", "", "Изображения (*.png *.jpg *.jpeg *.bmp *.webp)"
        )
        if path:
            self.photo_path = path
            self.photo_edit.setText(path)

    def _clear_photo(self) -> None:
        self.photo_path = None
        self.photo_edit.clear()

    def _load_recipe(self, recipe: Recipe) -> None:
        self.title_edit.setText(recipe.title)
        if recipe.category_name:
            index = self.category_combo.findText(recipe.category_name)
            if index >= 0:
                self.category_combo.setCurrentIndex(index)
            else:
                self.category_combo.setCurrentText(recipe.category_name)
        self.cook_time_spin.setValue(recipe.cook_time_minutes)
        if recipe.photo_path:
            self.photo_edit.setText(recipe.photo_path)
        self.steps_edit.setPlainText(recipe.steps)

        self.ingredients_table.setRowCount(0)
        for ingredient in recipe.ingredients:
            self._add_ingredient_row(ingredient.name, ingredient.quantity, ingredient.unit)
        if self.ingredients_table.rowCount() == 0:
            self._add_ingredient_row()

    def _collect_ingredients(self) -> list:
        ingredients = []
        for row in range(self.ingredients_table.rowCount()):
            name_widget = self.ingredients_table.cellWidget(row, 0)
            name = name_widget.currentText().strip() if name_widget else ""
            if not name:
                continue
            qty_item = self.ingredients_table.item(row, 1)
            qty_text = (qty_item.text().strip() if qty_item else "") or "0"
            try:
                quantity = float(qty_text.replace(",", "."))
            except ValueError:
                raise ValidationError(f"Некорректное количество для ингредиента «{name}»")
            unit_widget = self.ingredients_table.cellWidget(row, 2)
            unit = unit_widget.currentText().strip() if unit_widget else ""
            ingredients.append(RecipeIngredient(id=None, ingredient_id=None, name=name, quantity=quantity, unit=unit))
        return ingredients

    def _on_save(self) -> None:
        try:
            ingredients = self._collect_ingredients()
            title = self.title_edit.text()
            steps = self.steps_edit.toPlainText()
            cook_time = self.cook_time_spin.value()
            category_name = self.category_combo.currentText().strip()
            if self.recipe:
                self.service.update_recipe(
                    self.recipe.id, title, steps, cook_time, category_name, self.photo_path, ingredients
                )
            else:
                self.service.create_recipe(title, steps, cook_time, category_name, self.photo_path, ingredients)
            self.accept()
        except ValidationError as exc:
            QMessageBox.warning(self, "Проверьте данные", str(exc))
        except Exception as exc:  # noqa: BLE001 - surfaced to the user, not swallowed
            QMessageBox.critical(self, "Ошибка", f"Не удалось сохранить рецепт: {exc}")

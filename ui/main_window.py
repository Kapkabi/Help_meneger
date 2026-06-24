from PySide6.QtCore import QSize, Qt
from PySide6.QtWidgets import (
    QComboBox,
    QHBoxLayout,
    QLineEdit,
    QListWidget,
    QListWidgetItem,
    QMainWindow,
    QMessageBox,
    QPushButton,
    QSplitter,
    QVBoxLayout,
    QWidget,
)

from core.exceptions import AppError
from core.recipe_service import RecipeService
from ui.recipe_detail_widget import RecipeDetailWidget
from ui.recipe_form_dialog import RecipeFormDialog
from ui.widgets.recipe_list_item import RecipeListItemWidget

SORT_OPTIONS = [
    ("Названию", "title"),
    ("Времени готовки", "cook_time"),
    ("Дате добавления", "created_at"),
]


class MainWindow(QMainWindow):
    def __init__(self, service: RecipeService):
        super().__init__()
        self.service = service
        self.setWindowTitle("Менеджер рецептов")
        self.resize(960, 600)
        self._build_ui()
        self._reload_categories()
        self.refresh_list()

    def _build_ui(self) -> None:
        central = QWidget()
        self.setCentralWidget(central)
        root_layout = QVBoxLayout(central)

        filters = QHBoxLayout()
        self.search_edit = QLineEdit()
        self.search_edit.setPlaceholderText("Поиск по названию…")
        self.search_edit.textChanged.connect(self.refresh_list)
        filters.addWidget(self.search_edit)

        self.ingredient_edit = QLineEdit()
        self.ingredient_edit.setPlaceholderText("Поиск по ингредиенту…")
        self.ingredient_edit.textChanged.connect(self.refresh_list)
        filters.addWidget(self.ingredient_edit)

        self.category_filter_combo = QComboBox()
        self.category_filter_combo.currentIndexChanged.connect(self.refresh_list)
        filters.addWidget(self.category_filter_combo)

        self.sort_combo = QComboBox()
        for label, _ in SORT_OPTIONS:
            self.sort_combo.addItem(label)
        self.sort_combo.currentIndexChanged.connect(self.refresh_list)
        filters.addWidget(self.sort_combo)

        self.sort_order_combo = QComboBox()
        self.sort_order_combo.addItems(["По возрастанию", "По убыванию"])
        self.sort_order_combo.currentIndexChanged.connect(self.refresh_list)
        filters.addWidget(self.sort_order_combo)

        root_layout.addLayout(filters)

        splitter = QSplitter()
        root_layout.addWidget(splitter, 1)

        left_panel = QWidget()
        left_layout = QVBoxLayout(left_panel)
        self.recipe_list = QListWidget()
        self.recipe_list.setIconSize(QSize(56, 56))
        self.recipe_list.currentItemChanged.connect(self._on_selection_changed)
        left_layout.addWidget(self.recipe_list)

        buttons_row = QHBoxLayout()
        add_btn = QPushButton("Добавить")
        add_btn.clicked.connect(self._on_add)
        edit_btn = QPushButton("Редактировать")
        edit_btn.clicked.connect(self._on_edit)
        delete_btn = QPushButton("Удалить")
        delete_btn.clicked.connect(self._on_delete)
        buttons_row.addWidget(add_btn)
        buttons_row.addWidget(edit_btn)
        buttons_row.addWidget(delete_btn)
        left_layout.addLayout(buttons_row)

        splitter.addWidget(left_panel)

        self.detail_widget = RecipeDetailWidget()
        splitter.addWidget(self.detail_widget)
        splitter.setStretchFactor(0, 1)
        splitter.setStretchFactor(1, 2)

    def _reload_categories(self) -> None:
        self.category_filter_combo.blockSignals(True)
        self.category_filter_combo.clear()
        self.category_filter_combo.addItem("Все категории", None)
        try:
            for category in self.service.list_categories():
                self.category_filter_combo.addItem(category.name, category.id)
        except AppError as exc:
            QMessageBox.critical(self, "Ошибка", str(exc))
        self.category_filter_combo.blockSignals(False)

    def refresh_list(self) -> None:
        sort_index = self.sort_combo.currentIndex()
        sort_by = SORT_OPTIONS[sort_index][1] if sort_index >= 0 else "title"
        sort_order = "DESC" if self.sort_order_combo.currentIndex() == 1 else "ASC"
        category_id = self.category_filter_combo.currentData()

        try:
            recipes = self.service.search_recipes(
                text=self.search_edit.text().strip(),
                category_id=category_id,
                ingredient_text=self.ingredient_edit.text().strip(),
                sort_by=sort_by,
                sort_order=sort_order,
            )
        except AppError as exc:
            QMessageBox.critical(self, "Ошибка", str(exc))
            return

        current_id = self._current_recipe_id()
        self.recipe_list.clear()
        for recipe in recipes:
            item = QListWidgetItem()
            item.setData(Qt.UserRole, recipe.id)
            item.setSizeHint(QSize(220, 64))
            self.recipe_list.addItem(item)
            widget = RecipeListItemWidget(recipe.title, recipe.category_name, recipe.photo_path, recipe.cook_time_minutes)
            self.recipe_list.setItemWidget(item, widget)
            if recipe.id == current_id:
                self.recipe_list.setCurrentItem(item)

        if self.recipe_list.currentItem() is None:
            self.detail_widget.clear()

    def _current_recipe_id(self):
        item = self.recipe_list.currentItem()
        return item.data(Qt.UserRole) if item else None

    def _on_selection_changed(self, current, _previous) -> None:
        if current is None:
            self.detail_widget.clear()
            return
        recipe_id = current.data(Qt.UserRole)
        try:
            recipe = self.service.get_recipe(recipe_id)
        except AppError as exc:
            QMessageBox.critical(self, "Ошибка", str(exc))
            return
        if recipe:
            self.detail_widget.show_recipe(recipe)

    def _on_add(self) -> None:
        dialog = RecipeFormDialog(self.service, parent=self)
        if dialog.exec():
            self._reload_categories()
            self.refresh_list()

    def _on_edit(self) -> None:
        recipe_id = self._current_recipe_id()
        if recipe_id is None:
            QMessageBox.information(self, "Нет выбора", "Выберите рецепт для редактирования")
            return
        try:
            recipe = self.service.get_recipe(recipe_id)
        except AppError as exc:
            QMessageBox.critical(self, "Ошибка", str(exc))
            return
        dialog = RecipeFormDialog(self.service, recipe=recipe, parent=self)
        if dialog.exec():
            self._reload_categories()
            self.refresh_list()

    def _on_delete(self) -> None:
        recipe_id = self._current_recipe_id()
        if recipe_id is None:
            QMessageBox.information(self, "Нет выбора", "Выберите рецепт для удаления")
            return
        confirm = QMessageBox.question(
            self,
            "Удалить рецепт",
            "Вы уверены, что хотите удалить этот рецепт?",
            QMessageBox.Yes | QMessageBox.No,
        )
        if confirm != QMessageBox.Yes:
            return
        try:
            self.service.delete_recipe(recipe_id)
        except AppError as exc:
            QMessageBox.critical(self, "Ошибка", str(exc))
            return
        self.refresh_list()

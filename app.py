"""Entry point for the Recipe Manager desktop application."""

import sys

from PySide6.QtWidgets import QApplication, QMessageBox

from core.exceptions import AppError
from core.recipe_service import RecipeService
from data.database import Database
from ui.main_window import MainWindow


def main() -> int:
    app = QApplication(sys.argv)
    app.setApplicationName("Recipe Manager")

    db = Database()
    try:
        db.connect()
    except AppError as exc:
        QMessageBox.critical(None, "Ошибка базы данных", str(exc))
        return 1

    service = RecipeService(db)
    window = MainWindow(service)
    window.show()

    exit_code = app.exec()
    db.close()
    return exit_code


if __name__ == "__main__":
    sys.exit(main())

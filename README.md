# Recipe Manager

Desktop-приложение для хранения и управления рецептами.
Python 3.11+, GUI на **PySide6**, хранение данных в **SQLite**.

## Структура проекта

```
My_helper/
├── app.py                     # точка входа, сборка слоёв и запуск Qt-приложения
├── models/
│   └── recipe.py              # dataclasses: Recipe, RecipeIngredient, Category
├── data/                       # слой доступа к данным — единственное место, где есть SQL
│   ├── database.py            # Database: подключение, PRAGMA, запуск миграций
│   ├── repositories.py        # CategoryRepository, IngredientRepository, RecipeRepository
│   └── migrations/            # версионированные миграции схемы
│       ├── __init__.py        # ALL_MIGRATIONS — реестр миграций по порядку
│       └── migration_0001_initial.py
├── core/                       # бизнес-логика и валидация
│   ├── exceptions.py          # AppError и подклассы (Validation/Repository/Database*)
│   └── recipe_service.py      # RecipeService — единственная точка входа для UI
├── ui/                         # виджеты PySide6 (ничего не знают о SQL)
│   ├── main_window.py         # список рецептов, фильтры, сортировка, карточка
│   ├── recipe_form_dialog.py  # диалог добавления/редактирования рецепта
│   ├── recipe_detail_widget.py
│   └── widgets/recipe_list_item.py
├── tests/                      # pytest: data-layer и business-logic
├── requirements.txt
└── recipes.db                  # создаётся автоматически при первом запуске
```

## Архитектура

Слои строго разделены и зависят только "вниз":

`ui` → `core` (бизнес-логика/валидация) → `data` (репозитории, SQL) → SQLite.

- **UI** (PySide6 widgets) вызывает только `RecipeService`. Виджеты не содержат SQL и не знают
  о структуре таблиц.
- **core/recipe_service.py** — фасад над репозиториями: валидирует ввод (пустые поля,
  отрицательное время/количество, нечисловые значения) и поднимает `ValidationError`
  до того, как данные попадут в БД.
- **data/repositories.py** — единственное место с SQL-запросами. Репозитории возвращают
  и принимают dataclass-модели из `models/`, оборачивают ошибки sqlite3 в `RepositoryError`.
- **data/database.py** — управляет соединением (`PRAGMA foreign_keys = ON`) и применяет
  миграции при каждом подключении.

Это упрощённый вариант MVC: `models/` — модели, `ui/` — view, `core/` + `data/` — контроллер/
сервисный слой. Полноценный MVVM с биндингами через `QAbstractItemModel` был бы избыточен для
текущего объёма функционала.

## Схема БД и расширяемость

Миграция `migration_0001_initial.py` создаёт сразу все таблицы, нужные для текущих функций,
и таблицы-заготовки под будущие фичи (создаются, но не используются UI сегодня):

| Таблица | Назначение |
|---|---|
| `categories`, `recipes`, `ingredients`, `recipe_ingredients` | текущий функционал |
| `users`, `recipes.owner_id` | задел под multi-user режим |
| `tags`, `recipe_tags` | задел под теги рецептов |
| `ratings` | задел под рейтинг рецептов |
| `recipe_history` | задел под историю изменений (audit log) |
| `recipes.is_deleted` | задел под мягкое удаление (для history/undo) |

Импорт/экспорт не требует отдельных таблиц — он реализуется поверх существующих репозиториев
(сериализация `Recipe`/`RecipeIngredient` в JSON) и может быть добавлен как новый модуль в `core/`
без изменения схемы.

## Миграции схемы

Простая система версионирования: таблица `schema_version` хранит максимальный применённый
номер версии. Каждый модуль в `data/migrations/` экспортирует `VERSION: int` и `up(conn)`.
При подключении `Database._migrate()` применяет по порядку все миграции с `VERSION` больше
текущего и фиксирует номер в `schema_version`.

Чтобы добавить изменение схемы в будущем:
1. создать `data/migrations/migration_00NN_description.py` с `VERSION = NN` и функцией `up(conn)`;
2. зарегистрировать модуль в `ALL_MIGRATIONS` в `data/migrations/__init__.py`.

Существующие миграции никогда не редактируются — только добавляются новые.

## Установка и запуск

```bash
python -m venv .venv
.venv\Scripts\activate        # Windows
pip install -r requirements.txt
python app.py
```

При первом запуске рядом с `app.py` создаётся файл `recipes.db` со схемой и встроенными
категориями (Завтрак/Обед/Ужин/Десерт/Другое).

## Тесты

```bash
pip install -r requirements.txt
pytest
```

Тесты покрывают:
- `tests/test_database.py` — применение миграций, идемпотентность, обработка повреждённой БД;
- `tests/test_repositories.py` — CRUD и поиск/сортировка в data-layer;
- `tests/test_service.py` — валидацию бизнес-логики (`RecipeService`).

Каждый тест работает с временной БД (`tmp_path`), отдельной от `recipes.db` приложения.

## Обработка ошибок

- Повреждённый файл БД или ошибка миграции → `DatabaseMigrationError`/`DatabaseConnectionError`,
  показывается диалогом `QMessageBox.critical` при запуске.
- Некорректный ввод в форме (пустое название, отрицательное время, нечисловое количество,
  отсутствие ингредиентов) → `ValidationError`, показывается `QMessageBox.warning` без потери
  введённых данных.
- Ошибки SQL на уровне репозиториев оборачиваются в `RepositoryError` и не приводят к падению
  приложения.

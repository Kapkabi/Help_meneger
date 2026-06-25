# Help Meneger — Android

Android-приложение для управления рецептами на Kotlin + Jetpack Compose. Вторая платформа для
той же модели данных, что и desktop-версия (`../`, PySide6 + SQLite) — схема БД совместима
по полям и типам, чтобы между платформами в будущем можно было синхронизировать рецепты.

## Структура проекта

```
android/
├── app/src/main/java/com/kapkabi/helpmeneger/
│   ├── data/
│   │   ├── local/
│   │   │   ├── entity/        # Room-сущности — зеркало таблиц desktop-схемы
│   │   │   ├── dao/           # Room DAO, Flow-запросы
│   │   │   ├── migration/     # по одному файлу на изменение схемы (см. ниже)
│   │   │   └── AppDatabase.kt
│   │   └── repository/        # Room*Repository — единственная реализация на сегодня
│   ├── domain/
│   │   ├── model/             # Recipe, RecipeIngredient, Category, MeasurementUnit
│   │   └── repository/        # интерфейсы: RecipeRepository, CategoryRepository, UnitRepository
│   ├── di/                    # Hilt-модули (DatabaseModule, RepositoryModule)
│   └── ui/
│       ├── navigation/        # Routes (sealed class) + NavHost
│       ├── recipelist/        # список, поиск, фильтр, сортировка
│       ├── recipedetail/      # карточка рецепта
│       ├── recipeform/        # добавление/редактирование
│       └── theme/
└── app/src/test/...           # unit-тесты DAO и Repository (Robolectric + in-memory Room)
```

## Архитектура

`UI (Compose)` → `ViewModel` → `Repository (интерфейс)` → `Room DAO` → `SQLite`.

- **UI** — экраны Compose, не содержат SQL и не обращаются к DAO напрямую.
- **ViewModel** (Hilt `@HiltViewModel`) держит `StateFlow` UI-состояния, обращается только
  к интерфейсам репозиториев из `domain/repository`. Это главная точка расширения под sync:
  когда появится `ApiRecipeRepository`, ViewModel не изменится — поменяется только Hilt-биндинг
  в `di/RepositoryModule.kt`.
- **Repository-интерфейсы** — отдельный интерфейс на каждую сущность (`RecipeRepository`,
  `CategoryRepository`, `UnitRepository`), а не один общий репозиторий. `RoomRecipeRepository` —
  единственная реализация сегодня.
- **Room DAO** — единственное место с SQL/Flow-запросами к SQLite.

## Схема БД и совместимость с desktop

Room-сущности в `data/local/entity/` названы и типизированы по миграциям desktop-проекта
(`../data/migrations/migration_0001_initial.py`, `_0002_units.py`, `_0003_recipe_is_synced.py`):

| Сущность | Назначение |
|---|---|
| `RecipeEntity` | `id` — TEXT UUID (не autoincrement!), генерируется на устройстве через `UUID.randomUUID()` — критично для офлайн-синка между устройствами |
| `RecipeIngredientEntity` | строка ингредиента рецепта (количество + единица); `id` — UUID по той же причине |
| `CategoryEntity`, `UnitEntity`, `IngredientEntity` | словари, integer-id (не нужно создавать офлайн на разных устройствах) |
| `TagEntity`, `RecipeTagEntity` | задел под теги — без UI |
| `RatingEntity` | задел под рейтинг — без UI |
| `RecipeHistoryEntity` | задел под историю изменений — без UI |
| `UserEntity`, `RecipeEntity.ownerId` | задел под multi-user — без UI |
| `RecipeEntity.isDeleted` | задел под мягкое удаление — сегодня удаление рецепта делает реальный `DELETE`, как и в desktop |

Поиск по названию/ингредиенту и сравнение "одно и то же название без учёта регистра"
(категории, единицы, ингредиенты) сделаны в Kotlin, а не через SQLite `COLLATE NOCASE`/`LOWER()` —
эти функции SQLite по умолчанию складывают регистр только для ASCII и не работают для кириллицы.

## Миграции схемы

Версия `1` уже включает весь объём desktop-миграций 0001–0003 (схема создаётся с нуля на
Android). Дальше — по одному файлу на изменение схемы в `data/local/migration/`, регистрация в
`AppDatabase.MIGRATIONS`, и `addMigrations(...)` в `di/DatabaseModule.kt` — без
`fallbackToDestructiveMigration`, чтобы обновления не теряли данные пользователя. Уже выпущенные
миграции не редактируются.

## Сборка и запуск

Требуется JDK 17 и Android SDK (cmdline-tools в `PATH`).

```bash
cd android
./gradlew assembleDebug      # сборка debug APK
./gradlew testDebugUnitTest  # unit-тесты data-слоя
./gradlew installDebug       # установка на подключённый эмулятор/устройство
```

Минимальная версия — API 26 (Android 8.0), целевая/компиляция — API 35.

## Тесты

`app/src/test/.../data/` — unit-тесты на Robolectric + Room в памяти (`Room.inMemoryDatabaseBuilder`):

- `RecipeDaoTest` — вставка, выборка по id, фильтр по категории, join с ингредиентами;
- `RoomRecipeRepositoryTest` — создание/обновление/удаление рецепта, пересохранение ингредиентов,
  сортировка по названию/времени, поиск по названию/ингредиенту (включая кириллицу в разном
  регистре), переиспользование записей словаря ингредиентов.

## Технологии

Kotlin, Jetpack Compose (Material 3), Room, Hilt, Kotlin Coroutines/Flow, Navigation Compose,
Coil (фото рецепта). DI — Hilt: новый репозиторий/use-case добавляется как новый `@Binds`/
`@Provides` без изменений в несвязанных модулях.

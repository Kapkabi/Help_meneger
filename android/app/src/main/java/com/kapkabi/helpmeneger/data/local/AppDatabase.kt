package com.kapkabi.helpmeneger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kapkabi.helpmeneger.data.local.dao.CategoryDao
import com.kapkabi.helpmeneger.data.local.dao.IngredientDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeIngredientDao
import com.kapkabi.helpmeneger.data.local.dao.UnitDao
import com.kapkabi.helpmeneger.data.local.entity.CategoryEntity
import com.kapkabi.helpmeneger.data.local.entity.IngredientEntity
import com.kapkabi.helpmeneger.data.local.entity.RatingEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeHistoryEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeIngredientEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeTagEntity
import com.kapkabi.helpmeneger.data.local.entity.TagEntity
import com.kapkabi.helpmeneger.data.local.entity.UnitEntity
import com.kapkabi.helpmeneger.data.local.entity.UserEntity

/**
 * Schema version 1 already covers desktop migrations 0001-0003 (initial schema, units
 * dictionary, recipes.is_synced) since this database starts fresh on Android. Any schema
 * change from this point on must ship as a new @Database version plus one new file under
 * data/local/migration/ (e.g. Migration1To2.kt) added to MIGRATIONS below — never edit an
 * already-released migration in place.
 */
@Database(
    entities = [
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        CategoryEntity::class,
        UnitEntity::class,
        IngredientEntity::class,
        TagEntity::class,
        RecipeTagEntity::class,
        RatingEntity::class,
        RecipeHistoryEntity::class,
        UserEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun categoryDao(): CategoryDao
    abstract fun unitDao(): UnitDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        const val DATABASE_NAME = "help_meneger.db"

        /** Empty for now; append Migration1To2, Migration2To3, ... here as the schema grows. */
        val MIGRATIONS: Array<androidx.room.migration.Migration> = emptyArray()

        fun seed(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT OR IGNORE INTO categories (name, is_builtin) VALUES " +
                    "('Завтрак', 1), ('Обед', 1), ('Ужин', 1), ('Десерт', 1), ('Другое', 1)"
            )
            db.execSQL(
                "INSERT OR IGNORE INTO units (name) VALUES ('гр'), ('шт'), ('л')"
            )
        }
    }
}

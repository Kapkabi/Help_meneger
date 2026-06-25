package com.kapkabi.helpmeneger.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kapkabi.helpmeneger.data.local.AppDatabase
import com.kapkabi.helpmeneger.data.local.dao.CategoryDao
import com.kapkabi.helpmeneger.data.local.dao.IngredientDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeIngredientDao
import com.kapkabi.helpmeneger.data.local.dao.UnitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(*AppDatabase.MIGRATIONS)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    AppDatabase.seed(db)
                }
            })
            .build()
    }

    @Provides
    fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()

    @Provides
    fun provideRecipeIngredientDao(db: AppDatabase): RecipeIngredientDao = db.recipeIngredientDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideUnitDao(db: AppDatabase): UnitDao = db.unitDao()

    @Provides
    fun provideIngredientDao(db: AppDatabase): IngredientDao = db.ingredientDao()
}

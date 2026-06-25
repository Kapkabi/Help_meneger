package com.kapkabi.helpmeneger.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kapkabi.helpmeneger.data.local.AppDatabase
import com.kapkabi.helpmeneger.data.local.entity.CategoryEntity
import com.kapkabi.helpmeneger.data.local.entity.IngredientEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class, sdk = [34])
class RecipeDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var recipeIngredientDao: RecipeIngredientDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recipeDao = db.recipeDao()
        categoryDao = db.categoryDao()
        ingredientDao = db.ingredientDao()
        recipeIngredientDao = db.recipeIngredientDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndObserveById_returnsInsertedRecipe() = runTest {
        recipeDao.insert(recipe(id = "r1", title = "Борщ"))

        val loaded = recipeDao.observeById("r1").first()

        assertEquals("Борщ", loaded?.title)
    }

    @Test
    fun observeById_returnsNull_whenRecipeDoesNotExist() = runTest {
        val loaded = recipeDao.observeById("missing").first()

        assertNull(loaded)
    }

    @Test
    fun observeRecipeList_filtersByCategory() = runTest {
        val categoryId = categoryDao.insert(CategoryEntity(name = "Десерт"))
        recipeDao.insert(recipe(id = "r1", title = "Торт", categoryId = categoryId))
        recipeDao.insert(recipe(id = "r2", title = "Суп", categoryId = null))

        val filtered = recipeDao.observeRecipeList(categoryId = categoryId).first()

        assertEquals(1, filtered.size)
        assertEquals("r1", filtered.first().id)
    }

    @Test
    fun observeRecipeList_includesIngredientNameForEachRow() = runTest {
        recipeDao.insert(recipe(id = "r1", title = "Омлет"))
        val ingredientId = ingredientDao.insert(IngredientEntity(name = "Яйцо"))
        recipeIngredientDao.insertAll(
            listOf(
                RecipeIngredientEntity(
                    id = "ri1",
                    recipeId = "r1",
                    ingredientId = ingredientId,
                    quantity = 2.0,
                    unit = "шт",
                )
            )
        )

        val rows = recipeDao.observeRecipeList(categoryId = null).first()

        assertEquals(1, rows.size)
        assertEquals("Яйцо", rows.first().ingredientName)
    }

    @Test
    fun deleteById_removesRecipe() = runTest {
        recipeDao.insert(recipe(id = "r1", title = "Каша"))

        recipeDao.deleteById("r1")

        assertNull(recipeDao.observeById("r1").first())
    }

    private fun recipe(id: String, title: String, categoryId: Long? = null) = RecipeEntity(
        id = id,
        title = title,
        steps = "",
        cookTimeMinutes = 10,
        categoryId = categoryId,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )
}

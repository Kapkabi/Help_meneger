package com.kapkabi.helpmeneger.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kapkabi.helpmeneger.data.local.AppDatabase
import com.kapkabi.helpmeneger.domain.model.Recipe
import com.kapkabi.helpmeneger.domain.model.RecipeIngredient
import com.kapkabi.helpmeneger.domain.model.RecipeSortField
import com.kapkabi.helpmeneger.domain.model.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class, sdk = [34])
class RoomRecipeRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomRecipeRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomRecipeRepository(
            recipeDao = db.recipeDao(),
            recipeIngredientDao = db.recipeIngredientDao(),
            ingredientDao = db.ingredientDao(),
            unitDao = db.unitDao(),
            categoryDao = db.categoryDao(),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createRecipe_persistsRecipeWithIngredients() = runTest {
        val id = repository.createRecipe(
            Recipe(
                id = "",
                title = "Блины",
                steps = "Смешать и обжарить",
                cookTimeMinutes = 20,
                ingredients = listOf(RecipeIngredient(id = "", name = "Молоко", quantity = 500.0, unit = "мл")),
            )
        )

        val loaded = repository.observeRecipe(id).first()

        assertEquals("Блины", loaded?.title)
        assertEquals(1, loaded?.ingredients?.size)
        assertEquals("Молоко", loaded?.ingredients?.first()?.name)
    }

    @Test
    fun observeRecipe_resolvesCategoryName() = runTest {
        val category = db.categoryDao().getOrCreate("Десерт")

        val id = repository.createRecipe(Recipe(id = "", title = "Торт", categoryId = category.id))

        val loaded = repository.observeRecipe(id).first()

        assertEquals("Десерт", loaded?.categoryName)
    }

    @Test
    fun updateRecipe_replacesIngredientList() = runTest {
        val id = repository.createRecipe(
            Recipe(id = "", title = "Салат", ingredients = listOf(RecipeIngredient(id = "", name = "Огурец")))
        )
        val existing = repository.observeRecipe(id).first()!!

        repository.updateRecipe(
            existing.copy(title = "Салат овощной", ingredients = listOf(RecipeIngredient(id = "", name = "Помидор")))
        )

        val updated = repository.observeRecipe(id).first()
        assertEquals("Салат овощной", updated?.title)
        assertEquals(listOf("Помидор"), updated?.ingredients?.map { it.name })
    }

    @Test
    fun deleteRecipe_removesIt() = runTest {
        val id = repository.createRecipe(Recipe(id = "", title = "Пирог"))

        repository.deleteRecipe(id)

        assertNull(repository.observeRecipe(id).first())
    }

    @Test
    fun observeRecipes_sortsByTitleAscendingByDefault() = runTest {
        repository.createRecipe(Recipe(id = "", title = "Вареники"))
        repository.createRecipe(Recipe(id = "", title = "Аладдин-плов"))

        val titles = repository.observeRecipes(sortField = RecipeSortField.TITLE, sortOrder = SortOrder.ASC)
            .first().map { it.title }

        assertEquals(listOf("Аладдин-плов", "Вареники"), titles)
    }

    @Test
    fun observeRecipes_sortsByCookTimeDescending() = runTest {
        repository.createRecipe(Recipe(id = "", title = "Быстрый", cookTimeMinutes = 5))
        repository.createRecipe(Recipe(id = "", title = "Долгий", cookTimeMinutes = 120))

        val titles = repository.observeRecipes(sortField = RecipeSortField.COOK_TIME, sortOrder = SortOrder.DESC)
            .first().map { it.title }

        assertEquals(listOf("Долгий", "Быстрый"), titles)
    }

    @Test
    fun observeRecipes_matchesByIngredientNameCaseInsensitively() = runTest {
        repository.createRecipe(
            Recipe(id = "", title = "Омлет", ingredients = listOf(RecipeIngredient(id = "", name = "Яйцо")))
        )
        repository.createRecipe(Recipe(id = "", title = "Суп"))

        val titles = repository.observeRecipes(query = "яйц").first().map { it.title }

        assertEquals(listOf("Омлет"), titles)
    }

    @Test
    fun createRecipe_reusesExistingIngredientDictionaryEntry() = runTest {
        repository.createRecipe(Recipe(id = "", title = "Рецепт 1", ingredients = listOf(RecipeIngredient(id = "", name = "Соль"))))
        repository.createRecipe(Recipe(id = "", title = "Рецепт 2", ingredients = listOf(RecipeIngredient(id = "", name = "соль"))))

        val ingredientCount = db.ingredientDao().observeAll().first().count { it.name.equals("Соль", ignoreCase = true) }

        assertTrue(ingredientCount == 1)
    }
}

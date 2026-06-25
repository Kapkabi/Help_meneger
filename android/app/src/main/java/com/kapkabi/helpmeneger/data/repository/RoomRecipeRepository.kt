@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kapkabi.helpmeneger.data.repository

import com.kapkabi.helpmeneger.data.local.dao.CategoryDao
import com.kapkabi.helpmeneger.data.local.dao.IngredientDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeIngredientDao
import com.kapkabi.helpmeneger.data.local.dao.RecipeListRow
import com.kapkabi.helpmeneger.data.local.dao.UnitDao
import com.kapkabi.helpmeneger.data.local.entity.RecipeEntity
import com.kapkabi.helpmeneger.data.local.entity.RecipeIngredientEntity
import com.kapkabi.helpmeneger.domain.model.Recipe
import com.kapkabi.helpmeneger.domain.model.RecipeIngredient
import com.kapkabi.helpmeneger.domain.model.RecipeSortField
import com.kapkabi.helpmeneger.domain.model.RecipeSummary
import com.kapkabi.helpmeneger.domain.model.SortOrder
import com.kapkabi.helpmeneger.domain.repository.RecipeRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Room-backed [RecipeRepository]. The only implementation today. When cloud sync ships, a
 * sibling `ApiRecipeRepository` can implement the same interface and be swapped in via the
 * Hilt binding in di/RepositoryModule.kt without touching ViewModels or UI.
 */
class RoomRecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val ingredientDao: IngredientDao,
    private val unitDao: UnitDao,
    private val categoryDao: CategoryDao,
) : RecipeRepository {

    override fun observeRecipes(
        query: String,
        categoryId: Long?,
        sortField: RecipeSortField,
        sortOrder: SortOrder,
    ): Flow<List<RecipeSummary>> {
        val needle = query.trim().lowercase()
        return recipeDao.observeRecipeList(categoryId).map { rows ->
            val summaries = rows
                .groupBy { it.id }
                .values
                .map { group -> group.first().toSummary() to group.mapNotNull { it.ingredientName } }
                .filter { (summary, ingredientNames) ->
                    needle.isEmpty() ||
                        summary.title.lowercase().contains(needle) ||
                        ingredientNames.any { it.lowercase().contains(needle) }
                }
                .map { (summary, _) -> summary }
            val comparator = comparatorFor(sortField)
            val sorted = summaries.sortedWith(comparator)
            if (sortOrder == SortOrder.DESC) sorted.reversed() else sorted
        }
    }

    private fun comparatorFor(field: RecipeSortField): Comparator<RecipeSummary> = when (field) {
        RecipeSortField.TITLE -> compareBy { it.title.lowercase() }
        RecipeSortField.COOK_TIME -> compareBy { it.cookTimeMinutes }
        RecipeSortField.DATE -> compareBy { it.createdAt }
    }

    override fun observeRecipe(id: String): Flow<Recipe?> {
        return recipeDao.observeById(id).flatMapLatest { entity ->
            if (entity == null) {
                flowOf(null)
            } else {
                val categoryFlow = entity.categoryId?.let { categoryDao.observeById(it) } ?: flowOf(null)
                combine(recipeIngredientDao.observeForRecipe(id), categoryFlow) { ingredientRows, category ->
                    entity.toDomain(
                        ingredients = ingredientRows.map { row ->
                            RecipeIngredient(
                                id = row.id,
                                name = row.name,
                                quantity = row.quantity,
                                unit = row.unit,
                            )
                        },
                        categoryName = category?.name.orEmpty(),
                    )
                }
            }
        }
    }

    override suspend fun createRecipe(recipe: Recipe): String {
        val id = recipe.id.ifBlank { UUID.randomUUID().toString() }
        val now = Instant.now().toString()
        recipeDao.insert(
            RecipeEntity(
                id = id,
                title = recipe.title,
                steps = recipe.steps,
                cookTimeMinutes = recipe.cookTimeMinutes,
                categoryId = recipe.categoryId,
                photoPath = recipe.photoPath,
                createdAt = now,
                updatedAt = now,
            )
        )
        saveIngredients(id, recipe.ingredients)
        return id
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val now = Instant.now().toString()
        recipeDao.update(
            RecipeEntity(
                id = recipe.id,
                title = recipe.title,
                steps = recipe.steps,
                cookTimeMinutes = recipe.cookTimeMinutes,
                categoryId = recipe.categoryId,
                photoPath = recipe.photoPath,
                createdAt = recipe.createdAt ?: now,
                updatedAt = now,
            )
        )
        saveIngredients(recipe.id, recipe.ingredients)
    }

    override suspend fun deleteRecipe(id: String) {
        recipeDao.deleteById(id)
    }

    private suspend fun saveIngredients(recipeId: String, ingredients: List<RecipeIngredient>) {
        recipeIngredientDao.deleteForRecipe(recipeId)
        val entities = ingredients.mapIndexed { index, ingredient ->
            val ingredientId = ingredientDao.getOrCreate(ingredient.name).id
            if (ingredient.unit.isNotBlank()) {
                unitDao.getOrCreate(ingredient.unit)
            }
            RecipeIngredientEntity(
                id = ingredient.id.ifBlank { UUID.randomUUID().toString() },
                recipeId = recipeId,
                ingredientId = ingredientId,
                quantity = ingredient.quantity,
                unit = ingredient.unit,
                sortOrder = index,
            )
        }
        if (entities.isNotEmpty()) {
            recipeIngredientDao.insertAll(entities)
        }
    }
}

private fun RecipeListRow.toSummary() = RecipeSummary(
    id = id,
    title = title,
    cookTimeMinutes = cookTimeMinutes,
    photoPath = photoPath,
    createdAt = createdAt,
    updatedAt = updatedAt,
    categoryId = categoryId,
    categoryName = categoryName,
)

private fun RecipeEntity.toDomain(ingredients: List<RecipeIngredient>, categoryName: String) = Recipe(
    id = id,
    title = title,
    steps = steps,
    cookTimeMinutes = cookTimeMinutes,
    categoryId = categoryId,
    categoryName = categoryName,
    photoPath = photoPath,
    createdAt = createdAt,
    updatedAt = updatedAt,
    ingredients = ingredients,
)

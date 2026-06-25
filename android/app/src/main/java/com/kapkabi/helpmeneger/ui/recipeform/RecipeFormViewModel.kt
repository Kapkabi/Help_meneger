package com.kapkabi.helpmeneger.ui.recipeform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapkabi.helpmeneger.domain.model.Category
import com.kapkabi.helpmeneger.domain.model.Recipe
import com.kapkabi.helpmeneger.domain.model.RecipeIngredient
import com.kapkabi.helpmeneger.domain.repository.CategoryRepository
import com.kapkabi.helpmeneger.domain.repository.IngredientRepository
import com.kapkabi.helpmeneger.domain.repository.RecipeRepository
import com.kapkabi.helpmeneger.domain.repository.UnitRepository
import com.kapkabi.helpmeneger.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IngredientFormRow(
    val key: String = UUID.randomUUID().toString(),
    val id: String = "",
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
)

data class RecipeFormUiState(
    val isEditing: Boolean = false,
    val title: String = "",
    val steps: String = "",
    val cookTimeMinutes: String = "",
    val categoryName: String = "",
    val photoPath: String? = null,
    val ingredients: List<IngredientFormRow> = listOf(IngredientFormRow()),
    val categories: List<Category> = emptyList(),
    val unitSuggestions: List<String> = emptyList(),
    val ingredientSuggestions: List<String> = emptyList(),
    val isSaved: Boolean = false,
)

@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val categoryRepository: CategoryRepository,
    private val unitRepository: UnitRepository,
    private val ingredientRepository: IngredientRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId: String? = savedStateHandle[Routes.RecipeFormEdit.ARG_RECIPE_ID]

    private val form = MutableStateFlow(RecipeFormUiState(isEditing = recipeId != null))

    val uiState: StateFlow<RecipeFormUiState> = combine(
        form,
        categoryRepository.observeCategories(),
        unitRepository.observeUnits(),
        ingredientRepository.observeIngredients(),
    ) { state, categories, units, ingredients ->
        state.copy(
            categories = categories,
            unitSuggestions = units.map { it.name },
            ingredientSuggestions = ingredients,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeFormUiState())

    init {
        recipeId?.let { id ->
            viewModelScope.launch {
                recipeRepository.observeRecipe(id).collect { recipe ->
                    if (recipe != null) {
                        form.value = form.value.copy(
                            isEditing = true,
                            title = recipe.title,
                            steps = recipe.steps,
                            cookTimeMinutes = recipe.cookTimeMinutes.toString(),
                            categoryName = recipe.categoryName,
                            photoPath = recipe.photoPath,
                            ingredients = recipe.ingredients.map {
                                IngredientFormRow(
                                    id = it.id,
                                    name = it.name,
                                    quantity = it.quantity.toString(),
                                    unit = it.unit,
                                )
                            }.ifEmpty { listOf(IngredientFormRow()) },
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) = update { it.copy(title = value) }
    fun onStepsChange(value: String) = update { it.copy(steps = value) }
    fun onCookTimeChange(value: String) = update { it.copy(cookTimeMinutes = value.filter(Char::isDigit)) }
    fun onCategoryNameChange(value: String) = update { it.copy(categoryName = value) }
    fun onPhotoPicked(path: String?) = update { it.copy(photoPath = path) }

    fun onIngredientChange(key: String, name: String, quantity: String, unit: String) = update { state ->
        state.copy(ingredients = state.ingredients.map { row ->
            if (row.key == key) row.copy(name = name, quantity = quantity, unit = unit) else row
        })
    }

    fun addIngredientRow() = update { it.copy(ingredients = it.ingredients + IngredientFormRow()) }

    fun removeIngredientRow(key: String) = update { state ->
        val remaining = state.ingredients.filterNot { it.key == key }
        state.copy(ingredients = remaining.ifEmpty { listOf(IngredientFormRow()) })
    }

    fun save() {
        val state = form.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            val category = state.categoryName.takeIf { it.isNotBlank() }?.let {
                categoryRepository.getOrCreate(it)
            }
            val ingredients = state.ingredients
                .filter { it.name.isNotBlank() }
                .map { row ->
                    RecipeIngredient(
                        id = row.id,
                        name = row.name.trim(),
                        quantity = row.quantity.toDoubleOrNull() ?: 0.0,
                        unit = row.unit.trim(),
                    )
                }

            val recipe = Recipe(
                id = recipeId ?: "",
                title = state.title.trim(),
                steps = state.steps.trim(),
                cookTimeMinutes = state.cookTimeMinutes.toIntOrNull() ?: 0,
                categoryId = category?.id,
                categoryName = category?.name.orEmpty(),
                photoPath = state.photoPath,
                ingredients = ingredients,
            )

            if (recipeId == null) {
                recipeRepository.createRecipe(recipe)
            } else {
                recipeRepository.updateRecipe(recipe)
            }
            form.value = form.value.copy(isSaved = true)
        }
    }

    private inline fun update(transform: (RecipeFormUiState) -> RecipeFormUiState) {
        form.value = transform(form.value)
    }
}

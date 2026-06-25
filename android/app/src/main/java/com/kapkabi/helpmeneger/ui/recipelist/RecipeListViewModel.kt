@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kapkabi.helpmeneger.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapkabi.helpmeneger.domain.model.Category
import com.kapkabi.helpmeneger.domain.model.RecipeSortField
import com.kapkabi.helpmeneger.domain.model.RecipeSummary
import com.kapkabi.helpmeneger.domain.model.SortOrder
import com.kapkabi.helpmeneger.domain.repository.CategoryRepository
import com.kapkabi.helpmeneger.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class RecipeListUiState(
    val query: String = "",
    val categoryId: Long? = null,
    val sortField: RecipeSortField = RecipeSortField.TITLE,
    val sortOrder: SortOrder = SortOrder.ASC,
    val categories: List<Category> = emptyList(),
    val recipes: List<RecipeSummary> = emptyList(),
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val categoryId = MutableStateFlow<Long?>(null)
    private val sortField = MutableStateFlow(RecipeSortField.TITLE)
    private val sortOrder = MutableStateFlow(SortOrder.ASC)

    private val filters = combine(query, categoryId, sortField, sortOrder) { q, cat, field, order ->
        Filters(q, cat, field, order)
    }

    private val recipesFlow = filters.flatMapLatest { f ->
        recipeRepository.observeRecipes(f.query, f.categoryId, f.sortField, f.sortOrder)
    }

    val uiState: StateFlow<RecipeListUiState> = combine(
        filters,
        categoryRepository.observeCategories(),
        recipesFlow,
    ) { f, categories, recipes ->
        RecipeListUiState(
            query = f.query,
            categoryId = f.categoryId,
            sortField = f.sortField,
            sortOrder = f.sortOrder,
            categories = categories,
            recipes = recipes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeListUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategorySelected(value: Long?) {
        categoryId.value = value
    }

    fun onSortFieldSelected(value: RecipeSortField) {
        sortField.value = value
    }

    fun onSortOrderToggled() {
        sortOrder.value = if (sortOrder.value == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
    }

    private data class Filters(
        val query: String,
        val categoryId: Long?,
        val sortField: RecipeSortField,
        val sortOrder: SortOrder,
    )
}

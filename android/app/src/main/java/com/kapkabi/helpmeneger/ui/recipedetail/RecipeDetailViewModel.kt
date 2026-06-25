package com.kapkabi.helpmeneger.ui.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapkabi.helpmeneger.domain.model.Recipe
import com.kapkabi.helpmeneger.domain.repository.RecipeRepository
import com.kapkabi.helpmeneger.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle[Routes.RecipeDetail.ARG_RECIPE_ID])

    val recipe: StateFlow<Recipe?> = recipeRepository.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun deleteRecipe(onDeleted: () -> Unit) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipeId)
            onDeleted()
        }
    }
}

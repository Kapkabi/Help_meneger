package com.kapkabi.helpmeneger.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val steps: String = "",
    val cookTimeMinutes: Int = 0,
    val categoryId: Long? = null,
    val categoryName: String = "",
    val photoPath: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
)

data class RecipeSummary(
    val id: String,
    val title: String,
    val cookTimeMinutes: Int,
    val photoPath: String?,
    val createdAt: String,
    val updatedAt: String,
    val categoryId: Long?,
    val categoryName: String?,
)

enum class RecipeSortField { TITLE, COOK_TIME, DATE }

enum class SortOrder { ASC, DESC }

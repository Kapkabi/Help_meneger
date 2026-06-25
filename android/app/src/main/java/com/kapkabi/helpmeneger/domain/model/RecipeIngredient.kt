package com.kapkabi.helpmeneger.domain.model

/** An ingredient line attached to a recipe: name + quantity + unit. */
data class RecipeIngredient(
    val id: String,
    val name: String,
    val quantity: Double = 0.0,
    val unit: String = "",
)

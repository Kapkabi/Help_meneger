package com.kapkabi.helpmeneger.domain.model

data class Category(
    val id: Long,
    val name: String,
    val isBuiltin: Boolean = false,
)

package com.kapkabi.helpmeneger.di

import com.kapkabi.helpmeneger.data.repository.RoomCategoryRepository
import com.kapkabi.helpmeneger.data.repository.RoomIngredientRepository
import com.kapkabi.helpmeneger.data.repository.RoomRecipeRepository
import com.kapkabi.helpmeneger.data.repository.RoomUnitRepository
import com.kapkabi.helpmeneger.domain.repository.CategoryRepository
import com.kapkabi.helpmeneger.domain.repository.IngredientRepository
import com.kapkabi.helpmeneger.domain.repository.RecipeRepository
import com.kapkabi.helpmeneger.domain.repository.UnitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each entity's repository interface to its Room implementation. A new repository
 * (e.g. TagRepository) is added here as its own @Binds pair — never folded into an existing
 * one — so unrelated features stay untouched. Swapping RoomRecipeRepository for a future
 * ApiRecipeRepository (sync) only requires changing the binding below.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RoomRecipeRepository): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: RoomCategoryRepository): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindUnitRepository(impl: RoomUnitRepository): UnitRepository

    @Binds
    @Singleton
    abstract fun bindIngredientRepository(impl: RoomIngredientRepository): IngredientRepository
}

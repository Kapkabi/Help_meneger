package com.kapkabi.helpmeneger.ui.recipedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kapkabi.helpmeneger.domain.model.RecipeIngredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val recipe by viewModel.recipe.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                    }
                },
            )
        },
    ) { padding ->
        val current = recipe
        if (current == null) {
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (current.photoPath != null) {
                item {
                    AsyncImage(
                        model = current.photoPath,
                        contentDescription = current.title,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )
                }
            }
            item {
                Text(text = current.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = listOfNotNull(
                        current.categoryName.takeIf { it.isNotBlank() },
                        "${current.cookTimeMinutes} мин",
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "Ингредиенты", style = MaterialTheme.typography.titleMedium)
            }
            items(current.ingredients, key = { it.id }) { ingredient ->
                IngredientRow(ingredient)
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "Приготовление", style = MaterialTheme.typography.titleMedium)
                Text(text = current.steps, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить рецепт?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteRecipe(onDeleted)
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "${ingredient.name} — ${ingredient.quantity} ${ingredient.unit}".trim())
    }
}

package com.kapkabi.helpmeneger.ui.recipelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.kapkabi.helpmeneger.domain.model.RecipeSortField
import com.kapkabi.helpmeneger.domain.model.RecipeSummary
import com.kapkabi.helpmeneger.domain.model.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Рецепты") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipeClick) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить рецепт")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Поиск по названию или ингредиенту") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryFilterDropdown(
                    categories = state.categories,
                    selectedCategoryId = state.categoryId,
                    onSelected = viewModel::onCategorySelected,
                    modifier = Modifier.weight(1f),
                )
                SortDropdown(
                    sortField = state.sortField,
                    onSortFieldSelected = viewModel::onSortFieldSelected,
                )
                IconButton(onClick = viewModel::onSortOrderToggled) {
                    Icon(
                        imageVector = if (state.sortOrder == SortOrder.ASC) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = "Направление сортировки",
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.recipes, key = { it.id }) { recipe ->
                    RecipeRow(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(recipe: RecipeSummary, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (recipe.photoPath != null) {
                AsyncImage(
                    model = recipe.photoPath,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = recipe.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listOfNotNull(
                        recipe.categoryName?.takeIf { it.isNotBlank() },
                        "${recipe.cookTimeMinutes} мин",
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterDropdown(
    categories: List<com.kapkabi.helpmeneger.domain.model.Category>,
    selectedCategoryId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Все категории"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Категория") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Все категории") },
                onClick = { onSelected(null); expanded = false },
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelected(category.id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDropdown(
    sortField: RecipeSortField,
    onSortFieldSelected: (RecipeSortField) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val labels = mapOf(
        RecipeSortField.TITLE to "Название",
        RecipeSortField.COOK_TIME to "Время",
        RecipeSortField.DATE to "Дата",
    )

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(labels[sortField] ?: "Название") },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            labels.forEach { (field, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { onSortFieldSelected(field); expanded = false },
                )
            }
        }
    }
}

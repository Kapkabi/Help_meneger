package com.kapkabi.helpmeneger.ui.recipeform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormScreen(
    recipeId: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: RecipeFormViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            val savedPath = RecipePhotoStorage.copyToAppStorage(context, uri)
            viewModel.onPhotoPicked(savedPath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Редактировать рецепт" else "Новый рецепт") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = state.categoryName,
                    onValueChange = viewModel::onCategoryNameChange,
                    label = { Text("Категория") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = state.cookTimeMinutes,
                    onValueChange = viewModel::onCookTimeChange,
                    label = { Text("Время приготовления (мин)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Column {
                    if (state.photoPath != null) {
                        AsyncImage(
                            model = state.photoPath,
                            contentDescription = "Фото рецепта",
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    OutlinedButton(onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Filled.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(if (state.photoPath != null) "Изменить фото" else "Добавить фото")
                    }
                }
            }
            item {
                Text(text = "Ингредиенты", style = MaterialTheme.typography.titleMedium)
            }
            items(state.ingredients, key = { it.key }) { row ->
                IngredientFormFields(
                    row = row,
                    onChange = { name, quantity, unit -> viewModel.onIngredientChange(row.key, name, quantity, unit) },
                    onRemove = { viewModel.removeIngredientRow(row.key) },
                )
            }
            item {
                OutlinedButton(onClick = viewModel::addIngredientRow, modifier = Modifier.fillMaxWidth()) {
                    Text("Добавить ингредиент")
                }
            }
            item {
                OutlinedTextField(
                    value = state.steps,
                    onValueChange = viewModel::onStepsChange,
                    label = { Text("Приготовление") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )
            }
            item {
                Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
private fun IngredientFormFields(
    row: IngredientFormRow,
    onChange: (name: String, quantity: String, unit: String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        OutlinedTextField(
            value = row.name,
            onValueChange = { onChange(it, row.quantity, row.unit) },
            label = { Text("Ингредиент") },
            modifier = Modifier.weight(2f),
        )
        Spacer(modifier = Modifier.size(8.dp))
        OutlinedTextField(
            value = row.quantity,
            onValueChange = { onChange(row.name, it, row.unit) },
            label = { Text("Кол-во") },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(8.dp))
        OutlinedTextField(
            value = row.unit,
            onValueChange = { onChange(row.name, row.quantity, it) },
            label = { Text("Ед.") },
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, contentDescription = "Удалить ингредиент")
        }
    }
}

package com.marble.shamsa.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.design.AccentPresets
import com.marble.shamsa.core.design.IconCatalog
import com.marble.shamsa.core.model.Category
import java.util.UUID

@Composable
fun CategoriesScreen(categories: List<Category>, onSave: (Category) -> Unit, persian: Boolean) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(if (persian) "دسته‌بندی‌ها" else "Categories", style = MaterialTheme.typography.headlineLarge)
                Text(
                    if (persian) "برای هر نوع یادآور رنگ و آیکن جدا داشته باش." else "Give each reminder group its own icon and accent.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            items(categories, key = { it.id }) { category ->
                Card(onClick = { editing = category }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(category.colorArgb).copy(alpha = .15f), modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(IconCatalog.icon(category.icon), null, tint = Color(category.colorArgb)) }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(category.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        FloatingActionButton(onClick = { creating = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Rounded.Add, null) }
    }

    if (creating || editing != null) {
        CategoryEditor(
            value = editing,
            persian = persian,
            onDismiss = { creating = false; editing = null },
            onSave = {
                onSave(it)
                creating = false
                editing = null
            }
        )
    }
}

@Composable
private fun CategoryEditor(value: Category?, persian: Boolean, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember(value) { mutableStateOf(value?.name.orEmpty()) }
    var icon by remember(value) { mutableStateOf(value?.icon ?: "folder") }
    var color by remember(value) { mutableLongStateOf(value?.colorArgb ?: 0xFF00A7C7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (value == null) if (persian) "دسته جدید" else "New category" else if (persian) "ویرایش دسته" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (persian) "نام" else "Name") })
                Text(if (persian) "آیکن" else "Icon", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconCatalog.categoryKeys.forEach { key ->
                        FilterChip(selected = icon == key, onClick = { icon = key }, label = { Icon(IconCatalog.icon(key), null, Modifier.size(20.dp)) })
                    }
                }
                Text(if (persian) "رنگ" else "Color", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccentPresets.values.forEach { argb ->
                        Box(
                            Modifier.size(if (color == argb) 42.dp else 36.dp)
                                .background(Brush.linearGradient(listOf(Color(argb), Color(argb).copy(alpha = .55f))), CircleShape)
                                .clickable { color = argb },
                            contentAlignment = Alignment.Center
                        ) { if (color == argb) Text("✓", color = Color.White) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(
                        Category(
                            id = value?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            icon = icon,
                            colorArgb = color,
                            createdAtMillis = value?.createdAtMillis ?: now,
                            updatedAtMillis = now
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text(if (persian) "ذخیره" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(if (persian) "انصراف" else "Cancel") } }
    )
}

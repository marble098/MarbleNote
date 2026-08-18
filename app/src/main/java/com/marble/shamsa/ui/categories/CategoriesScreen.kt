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
        LazyColumn(
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("🎨", style = MaterialTheme.typography.displaySmall)
                Text(if (persian) "دسته‌بندی‌ها" else "Categories", style = MaterialTheme.typography.headlineLarge)
                Text(
                    if (persian) "هر موضوع، شخصیت و رنگ خودش را داشته باشد." else "Give every group its own personality.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            if (categories.isEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)
                        )
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🗂️", style = MaterialTheme.typography.displaySmall)
                            Text(
                                if (persian) "اولین دسته را بساز" else "Create your first category",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }

            items(categories, key = { it.id }) { category ->
                val accent = Color(category.colorArgb)
                Card(
                    onClick = { editing = category },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().background(
                            Brush.horizontalGradient(listOf(accent.copy(alpha = .12f), Color.Transparent))
                        ).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = accent.copy(alpha = .16f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(IconCatalog.icon(category.icon), null, tint = accent)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(category.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creating = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp),
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text(if (persian) "دسته جدید" else "New category") }
        )
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
    var color by remember(value) { mutableLongStateOf(value?.colorArgb ?: 0xFF008DA7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(if (value == null) "✨" else "✏️", style = MaterialTheme.typography.headlineMedium) },
        title = {
            Text(
                if (value == null)
                    if (persian) "دسته جدید" else "New category"
                else
                    if (persian) "ویرایش دسته" else "Edit category"
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    name, { name = it }, Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(if (persian) "نام" else "Name") },
                    shape = MaterialTheme.shapes.medium
                )
                Text(if (persian) "✨ آیکن" else "✨ Icon", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconCatalog.categoryKeys.forEach { key ->
                        FilterChip(
                            selected = icon == key,
                            onClick = { icon = key },
                            label = { Icon(IconCatalog.icon(key), null, Modifier.size(20.dp)) }
                        )
                    }
                }
                Text(if (persian) "🎨 رنگ" else "🎨 Color", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccentPresets.values.forEach { argb ->
                        Box(
                            Modifier.size(if (color == argb) 42.dp else 36.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(argb), Color(argb).copy(alpha = .55f))),
                                    CircleShape
                                )
                                .clickable { color = argb },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == argb) Text("✓", color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
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
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (persian) "انصراف" else "Cancel") }
        }
    )
}

package com.marble.shamsa.ui.notes

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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.model.Note
import java.util.UUID

private val noteColors = listOf(
    0xFFFFE082,
    0xFFFFCCBC,
    0xFFC8E6C9,
    0xFFB3E5FC,
    0xFFD1C4E9,
    0xFFF8BBD0
)

@Composable
fun NotesScreen(
    notes: List<Note>,
    persian: Boolean,
    onSave: (Note) -> Unit,
    onDelete: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Note?>(null) }

    val shown = remember(notes, query) {
        val q = query.trim()
        if (q.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.body.contains(q, ignoreCase = true)
            }
        }
    }

    if (editorOpen) {
        NoteEditorDialog(
            note = editing,
            persian = persian,
            onDismiss = { editorOpen = false },
            onSave = {
                onSave(it)
                editorOpen = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 118.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                        .copy(alpha = .68f)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (persian) "یادداشت‌ها" else "Notes",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            if (persian)
                                "فکرها، ایده‌ها و چیزهایی که نباید گم شوند."
                            else
                                "Ideas, thoughts and things worth keeping.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                .copy(alpha = .75f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        if (persian)
                                            "${notes.size} یادداشت"
                                        else
                                            "${notes.size} notes"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.EditNote,
                                        null,
                                        Modifier.size(18.dp)
                                    )
                                }
                            )
                            FilledTonalButton(
                                onClick = {
                                    editing = null
                                    editorOpen = true
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    null,
                                    Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (persian)
                                        "یادداشت جدید"
                                    else
                                        "New note"
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Rounded.Search, null)
                },
                placeholder = {
                    Text(
                        if (persian)
                            "جست‌وجو در یادداشت‌ها"
                        else
                            "Search notes"
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )
        }

        if (shown.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant
                        .copy(alpha = .48f)
                ) {
                    Column(
                        Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 40.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📝",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            if (query.isBlank()) {
                                if (persian)
                                    "هنوز یادداشتی نداری"
                                else
                                    "No notes yet"
                            } else {
                                if (persian)
                                    "چیزی پیدا نشد"
                                else
                                    "Nothing found"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            if (persian)
                                "یک یادداشت کوتاه بساز؛ در Google Drive هم پشتیبان می‌شود."
                            else
                                "Create a quick note; it is backed up with Drive too.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(shown, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    persian = persian,
                    onOpen = {
                        editing = note
                        editorOpen = true
                    },
                    onDelete = { onDelete(note.id) }
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    persian: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(note.colorArgb).copy(alpha = .32f)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.pinned) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Rounded.PushPin,
                            null,
                            Modifier
                                .padding(7.dp)
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                }

                Text(
                    note.title.ifBlank {
                        if (persian) "بدون عنوان" else "Untitled"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        if (persian) "حذف" else "Delete"
                    )
                }
            }

            if (note.body.isNotBlank()) {
                Text(
                    note.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    note: Note?,
    persian: Boolean,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    val now = System.currentTimeMillis()
    var title by remember(note?.id) {
        mutableStateOf(note?.title.orEmpty())
    }
    var body by remember(note?.id) {
        mutableStateOf(note?.body.orEmpty())
    }
    var pinned by remember(note?.id) {
        mutableStateOf(note?.pinned ?: false)
    }
    var color by remember(note?.id) {
        mutableLongStateOf(note?.colorArgb ?: noteColors.first())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                if (note == null) {
                    if (persian) "یادداشت جدید" else "New note"
                } else {
                    if (persian) "ویرایش یادداشت" else "Edit note"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (persian) "عنوان" else "Title")
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (persian) "متن یادداشت" else "Note")
                    },
                    minLines = 5,
                    maxLines = 10,
                    shape = MaterialTheme.shapes.large
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    noteColors.forEach { candidate ->
                        val selected = candidate == color
                        Box(
                            Modifier
                                .size(if (selected) 42.dp else 36.dp)
                                .background(
                                    Color(candidate),
                                    CircleShape
                                )
                                .clickable { color = candidate },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Text(
                                    "✓",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.PushPin,
                        null,
                        Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (persian)
                            "سنجاق شود"
                        else
                            "Pin note",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = pinned,
                        onCheckedChange = { pinned = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() || body.isNotBlank(),
                onClick = {
                    onSave(
                        Note(
                            id = note?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            body = body.trim(),
                            colorArgb = color,
                            pinned = pinned,
                            createdAtMillis = note?.createdAtMillis ?: now,
                            updatedAtMillis = now
                        )
                    )
                }
            ) {
                Text(if (persian) "ذخیره" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (persian) "انصراف" else "Cancel")
            }
        }
    )
}

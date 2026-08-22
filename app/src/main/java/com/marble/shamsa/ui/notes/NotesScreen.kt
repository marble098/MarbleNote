package com.marble.shamsa.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
    onDelete: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Note?>(null) }
    var creating by remember { mutableStateOf(false) }

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

    if (creating || editing != null) {
        NoteEditorPage(
            note = editing,
            persian = persian,
            onBack = {
                creating = false
                editing = null
            },
            onSave = {
                onSave(it)
                creating = false
                editing = null
            }
        )
        return
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
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .68f)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            if (persian) "یادداشت‌ها" else "Notes",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            if (persian)
                                "ویرایش تمام‌صفحه، مارک‌داون، پیش‌نمایش و ردیف‌کردن یادداشت‌ها."
                            else
                                "Full-page editing, Markdown tools, preview and manual ordering.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        if (persian) "${notes.size} یادداشت" else "${notes.size} notes"
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.EditNote, null, Modifier.size(18.dp))
                                }
                            )
                            FilledTonalButton(
                                onClick = { creating = true }
                            ) {
                                Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (persian) "یادداشت جدید" else "New note")
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
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = {
                    Text(if (persian) "جست‌وجو در عنوان و متن" else "Search title and body")
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
                ) {
                    Column(
                        Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📝", style = MaterialTheme.typography.displaySmall)
                        Text(
                            if (query.isBlank()) {
                                if (persian) "هنوز یادداشتی نداری" else "No notes yet"
                            } else {
                                if (persian) "چیزی پیدا نشد" else "Nothing found"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            if (persian)
                                "اولین یادداشتت را بساز؛ هم در برنامه می‌ماند و هم با درایو پشتیبان می‌شود."
                            else
                                "Create your first note; it stays local and can be backed up with Drive.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(shown, key = { _, note -> note.id }) { index, note ->
                NoteCard(
                    note = note,
                    persian = persian,
                    onOpen = { editing = note },
                    onDelete = { onDelete(note.id) },
                    onMoveUp = if (index > 0) ({ onMoveUp(note.id) }) else null,
                    onMoveDown = if (index < shown.lastIndex) ({ onMoveDown(note.id) }) else null
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
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(note.colorArgb).copy(alpha = .24f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.pinned) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Rounded.PushPin,
                            null,
                            Modifier.padding(7.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                }

                Text(
                    note.title.ifBlank { if (persian) "بدون عنوان" else "Untitled" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.body.isNotBlank()) {
                Text(
                    note.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onMoveUp != null) {
                    IconButton(onClick = onMoveUp) {
                        Icon(Icons.Rounded.KeyboardArrowUp, if (persian) "بالاتر" else "Move up")
                    }
                }
                if (onMoveDown != null) {
                    IconButton(onClick = onMoveDown) {
                        Icon(Icons.Rounded.KeyboardArrowDown, if (persian) "پایین‌تر" else "Move down")
                    }
                }
                IconButton(onClick = onOpen) {
                    Icon(Icons.Rounded.Edit, if (persian) "ویرایش" else "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteOutline, if (persian) "حذف" else "Delete")
                }
            }
        }
    }
}

@Composable
private fun NoteEditorPage(
    note: Note?,
    persian: Boolean,
    onBack: () -> Unit,
    onSave: (Note) -> Unit
) {
    val now = System.currentTimeMillis()
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    var pinned by remember(note?.id) { mutableStateOf(note?.pinned ?: false) }
    var color by remember(note?.id) { mutableLongStateOf(note?.colorArgb ?: noteColors.first()) }
    var preview by remember(note?.id) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (note == null) {
                            if (persian) "یادداشت جدید" else "New note"
                        } else {
                            if (persian) "ویرایش یادداشت" else "Edit note"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, if (persian) "بازگشت" else "Back")
                    }
                },
                actions = {
                    TextButton(
                        enabled = title.isNotBlank() || body.isNotBlank(),
                        onClick = {
                            onSave(
                                Note(
                                    id = note?.id ?: UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    body = body.trim(),
                                    colorArgb = color,
                                    pinned = pinned,
                                    sortOrder = note?.sortOrder ?: 0L,
                                    createdAtMillis = note?.createdAtMillis ?: now,
                                    updatedAtMillis = now
                                )
                            )
                        }
                    ) {
                        Text(if (persian) "ذخیره" else "Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (persian) "عنوان" else "Title") },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = !preview,
                                onClick = { preview = false },
                                label = { Text(if (persian) "ویرایش" else "Edit") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                                }
                            )
                            FilterChip(
                                selected = preview,
                                onClick = { preview = true },
                                label = { Text(if (persian) "پیش‌نمایش" else "Preview") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Visibility, null, Modifier.size(18.dp))
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MarkdownChip(if (persian) "سرتیتر" else "Heading") {
                                body = body + (if (body.isBlank()) "# " else "\n# ")
                            }
                            MarkdownChip("Bold") {
                                body = body + (if (body.isBlank()) "**bold**" else "\n**bold**")
                            }
                            MarkdownChip("Italic") {
                                body = body + (if (body.isBlank()) "*italic*" else "\n*italic*")
                            }
                            MarkdownChip(if (persian) "فهرست" else "List") {
                                body = body + (if (body.isBlank()) "- " else "\n- ")
                            }
                            MarkdownChip(if (persian) "کارها" else "Task") {
                                body = body + (if (body.isBlank()) "- [ ] " else "\n- [ ] ")
                            }
                            MarkdownChip(if (persian) "نقل‌قول" else "Quote") {
                                body = body + (if (body.isBlank()) "> " else "\n> ")
                            }
                            MarkdownChip("Code") {
                                body = body + (if (body.isBlank()) "```\n\n```" else "\n```\n\n```")
                            }
                            MarkdownChip("Link") {
                                body = body + (if (body.isBlank()) "[title](https://)" else "\n[title](https://)")
                            }
                        }

                        if (!preview) {
                            OutlinedTextField(
                                value = body,
                                onValueChange = { body = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (persian) "متن مارک‌داون" else "Markdown body") },
                                minLines = 12,
                                maxLines = 20,
                                shape = MaterialTheme.shapes.large
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f),
                                shape = MaterialTheme.shapes.large
                            ) {
                                MarkdownPreview(
                                    text = body,
                                    persian = persian,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Text(
                            if (persian)
                                "پیش‌نمایش از سرتیترها، فهرست‌ها، چک‌باکس، نقل‌قول و بلوک کد پشتیبانی می‌کند."
                            else
                                "Preview supports headings, lists, checkboxes, quotes and fenced code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        .background(Color(candidate), CircleShape)
                                        .clickable { color = candidate },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Text("✓", fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.PushPin, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (persian) "سنجاق شود" else "Pin note",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = pinned,
                                onCheckedChange = { pinned = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownChip(
    text: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
private fun MarkdownPreview(
    text: String,
    persian: Boolean,
    modifier: Modifier = Modifier
) {
    val lines = text.lines()
    var inCode by remember(text) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lines.forEach { raw ->
                    val line = raw.trimEnd()

                    if (line.startsWith("```")) {
                        inCode = !inCode
                    } else if (inCode) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                line.ifBlank { " " },
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (line.startsWith("# ")) {
                        Text(
                            line.removePrefix("# ").ifBlank { " " },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (line.startsWith("## ")) {
                        Text(
                            line.removePrefix("## ").ifBlank { " " },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (line.startsWith("### ")) {
                        Text(
                            line.removePrefix("### ").ifBlank { " " },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (line.startsWith("- [ ] ")) {
                        Text(
                            (if (persian) "☐ " else "☐ ") + line.removePrefix("- [ ] "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else if (line.startsWith("- [x] ") || line.startsWith("- [X] ")) {
                        Text(
                            (if (persian) "☑ " else "☑ ") +
                                line.removePrefix("- [x] ").removePrefix("- [X] "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else if (line.startsWith("- ") || line.startsWith("* ")) {
                        Text(
                            "• " + line.drop(2),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else if (line.startsWith("> ")) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f)
                        ) {
                            Text(
                                line.removePrefix("> "),
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            line.ifBlank { " " },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

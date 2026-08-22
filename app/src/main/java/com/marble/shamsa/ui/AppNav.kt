package com.marble.shamsa.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.marble.shamsa.R
import com.marble.shamsa.core.data.AppSettings
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.ui.categories.CategoriesScreen
import com.marble.shamsa.ui.editor.ReminderEditorScreen
import com.marble.shamsa.ui.home.HomeScreen
import com.marble.shamsa.ui.notes.NotesScreen
import com.marble.shamsa.ui.settings.SettingsScreen
import com.marble.shamsa.ui.timeline.TimelineScreen
import com.marble.shamsa.ui.viewmodel.MainViewModel

private data class Tab(
    val route: String,
    val label: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun ShamsaAppNav(
    settings: AppSettings,
    viewModel: MainViewModel = hiltViewModel(),
    onDrive: () -> Unit,
    onExact: () -> Unit,
    onFullScreen: () -> Unit,
    initialReminderId: String? = null
) {
    val nav = rememberNavController()
    val reminders by viewModel.reminders.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val driveUi by viewModel.driveUi.collectAsState()

    val tabs = listOf(
        Tab("home", R.string.home, Icons.Rounded.Home),
        Tab("notes", R.string.notes_tab, Icons.Rounded.EditNote),
        Tab("timeline", R.string.timeline, Icons.Rounded.CalendarMonth),
        Tab("categories", R.string.categories, Icons.Rounded.Category),
        Tab("settings", R.string.settings, Icons.Rounded.Settings)
    )

    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route
    val persian = settings.language == "fa"

    LaunchedEffect(initialReminderId) {
        initialReminderId
            ?.takeIf { it.isNotBlank() }
            ?.let { nav.navigate("edit/$it") }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route in tabs.map { it.route }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(
                                        nav.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(tab.icon, null)
                            },
                            label = {
                                Text(stringResource(tab.label))
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (route == "home") {
                ExtendedFloatingActionButton(
                    onClick = { nav.navigate("edit/new") },
                    icon = { Icon(Icons.Rounded.Add, null) },
                    text = {
                        Text(
                            if (persian) "یادآور جدید" else "New reminder"
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn() + slideInHorizontally { it / 10 }
            },
            exitTransition = { fadeOut() },
            popEnterTransition = {
                fadeIn() + slideInHorizontally { -it / 10 }
            }
        ) {
            composable("home") {
                HomeScreen(
                    reminders = reminders,
                    query = query,
                    filter = filter,
                    sort = sort,
                    display = settings.displayMode,
                    countdownStyle = settings.countdownStyle,
                    persian = persian,
                    onQuery = viewModel::setQuery,
                    onFilter = viewModel::setFilter,
                    onSort = viewModel::setSort,
                    onOpen = { nav.navigate("edit/$it") },
                    onComplete = viewModel::complete,
                    onCancel = viewModel::cancel,
                    onDelete = viewModel::delete
                )
            }

            composable("notes") {
                NotesScreen(
                    notes = notes,
                    persian = persian,
                    onSave = viewModel::saveNote,
                    onDelete = viewModel::deleteNote,
                    onMoveUp = viewModel::moveNoteUp,
                    onMoveDown = viewModel::moveNoteDown
                )
            }

            composable("timeline") {
                TimelineScreen(reminders, persian) {
                    nav.navigate("edit/$it")
                }
            }

            composable("categories") {
                CategoriesScreen(
                    categories,
                    viewModel::saveCategory,
                    persian
                )
            }

            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    driveUi = driveUi,
                    onLanguage = viewModel::setLanguage,
                    onTheme = viewModel::setTheme,
                    onDisplay = viewModel::setDisplay,
                    onCountdownStyle = viewModel::setCountdownStyle,
                    onPopup = viewModel::setPopup,
                    onDrive = onDrive,
                    onSync = viewModel::syncNow,
                    onDisconnect = viewModel::disconnectDrive,
                    onExact = onExact,
                    onFullScreen = onFullScreen
                )
            }

            composable("edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")
                var existing by remember(id) {
                    mutableStateOf<Reminder?>(null)
                }

                LaunchedEffect(id) {
                    if (id != "new") {
                        existing = viewModel.getReminder(id.orEmpty())
                    }
                }

                ReminderEditorScreen(
                    existing,
                    categories,
                    persian,
                    settings.popupReminders,
                    onSave = {
                        viewModel.saveReminder(it)
                        nav.popBackStack()
                    },
                    onDelete = existing?.let { reminder ->
                        {
                            viewModel.delete(reminder.id)
                            nav.popBackStack()
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

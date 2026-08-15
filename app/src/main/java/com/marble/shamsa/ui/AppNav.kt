package com.marble.shamsa.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.marble.shamsa.R
import com.marble.shamsa.core.data.AppSettings
import com.marble.shamsa.core.model.*
import com.marble.shamsa.ui.categories.CategoriesScreen
import com.marble.shamsa.ui.editor.ReminderEditorScreen
import com.marble.shamsa.ui.home.HomeScreen
import com.marble.shamsa.ui.settings.SettingsScreen
import com.marble.shamsa.ui.timeline.TimelineScreen
import com.marble.shamsa.ui.viewmodel.MainViewModel

private data class Tab(val route:String, val label:Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

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
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val tabs = listOf(Tab("home",R.string.home,Icons.Rounded.Home),Tab("timeline",R.string.timeline,Icons.Rounded.CalendarMonth),Tab("categories",R.string.categories,Icons.Rounded.Category),Tab("settings",R.string.settings,Icons.Rounded.Settings))
    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route
    val persian = settings.language == "fa"

    LaunchedEffect(initialReminderId) {
        initialReminderId?.takeIf { it.isNotBlank() }?.let { nav.navigate("edit/$it") }
    }

    Scaffold(
        bottomBar = { if (route in tabs.map { it.route }) NavigationBar { tabs.forEach { tab -> NavigationBarItem(selected=route==tab.route,onClick={nav.navigate(tab.route){popUpTo(nav.graph.findStartDestination().id){saveState=true};launchSingleTop=true;restoreState=true}},icon={Icon(tab.icon,null)},label={Text(stringResource(tab.label))}) } } },
        floatingActionButton = { if(route=="home") FloatingActionButton(onClick={nav.navigate("edit/new")}) { Icon(Icons.Rounded.Add,null) } }
    ) { padding ->
        NavHost(nav, startDestination="home", modifier=Modifier.padding(padding), enterTransition={fadeIn()+slideInHorizontally{it/7}}, exitTransition={fadeOut()}, popEnterTransition={fadeIn()+slideInHorizontally{-it/7}}) {
            composable("home") { HomeScreen(reminders,query,filter,sort,settings.displayMode,persian,viewModel::setQuery,viewModel::setFilter,viewModel::setSort,{nav.navigate("edit/$it")},viewModel::complete) }
            composable("timeline") { TimelineScreen(reminders,persian){nav.navigate("edit/$it")} }
            composable("categories") { CategoriesScreen(categories,viewModel::saveCategory,persian) }
            composable("settings") { SettingsScreen(settings,viewModel::setLanguage,viewModel::setTheme,viewModel::setDisplay,viewModel::setPopup,onDrive,viewModel::syncNow,onExact,onFullScreen) }
            composable("edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")
                var existing by remember(id) { mutableStateOf<Reminder?>(null) }
                LaunchedEffect(id) { if(id!="new") existing=viewModel.getReminder(id.orEmpty()) }
                ReminderEditorScreen(existing,categories,persian,settings.popupReminders,onSave={viewModel.saveReminder(it);nav.popBackStack()},onDelete = existing?.let { reminder -> { viewModel.delete(reminder.id); nav.popBackStack() } },onBack={nav.popBackStack()})
            }
        }
    }
}

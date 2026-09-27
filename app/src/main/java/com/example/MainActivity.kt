package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.UserProfile
import com.example.model.NavDestination
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AiScheduleDialog
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppNavigationRail
import com.example.ui.components.AppTopBar
import com.example.ui.components.CalendarView
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.UserProfileDialog
import com.example.ui.screens.AiScheduleScreen
import com.example.ui.screens.CompletedScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ImportantScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MyTasksScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.Indigo600
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PriorityLow
import com.example.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val taskViewModel: TaskViewModel = viewModel()
            val uiState by taskViewModel.uiState.collectAsStateWithLifecycle()

            val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = isDark) {
                if (!uiState.userProfile.isLoggedIn) {
                    LoginScreen(
                        onGoogleLogin = { name, email ->
                            taskViewModel.loginWithGoogle(name, email)
                        },
                        onEmailLogin = { name, email ->
                            taskViewModel.loginWithEmail(name, email)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    )
                } else {
                    SmartDeadlineApp(
                        viewModel = taskViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun SmartDeadlineApp(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button: return to dashboard if on sub-screen
    BackHandler(enabled = uiState.currentDestination != NavDestination.DASHBOARD) {
        viewModel.setDestination(NavDestination.DASHBOARD)
    }

    BoxWithConstraints(modifier = modifier) {
        val isWideScreen = maxWidth >= 700.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            topBar = {
                AppTopBar(
                    userProfile = uiState.userProfile,
                    unreadNotificationsCount = uiState.notifications.size,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onNotificationsClick = { viewModel.toggleNotificationsDrawer(true) },
                    onProfileClick = { viewModel.toggleProfileDialog(true) }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    AppBottomNavigationBar(
                        currentDestination = uiState.currentDestination,
                        onDestinationSelected = { viewModel.setDestination(it) }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddTaskDialog() },
                    containerColor = Indigo600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // If wide screen (tablet / foldable / desktop), show side NavigationRail
                if (isWideScreen) {
                    AppNavigationRail(
                        currentDestination = uiState.currentDestination,
                        onDestinationSelected = { viewModel.setDestination(it) }
                    )
                }

                // Main screen destination container
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.currentDestination) {
                        NavDestination.DASHBOARD -> DashboardScreen(
                            userName = uiState.userProfile.name,
                            tasks = uiState.tasks,
                            currentEpochMs = uiState.currentEpochMs,
                            selectedPriority = uiState.selectedPriority,
                            onSelectPriority = { viewModel.setPriorityFilter(it) },
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openEditTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onChangePriority = { id, p -> viewModel.setTaskPriority(id, p) },
                            onAddTask = { viewModel.openAddTaskDialog() },
                            onOpenAiScheduler = { viewModel.openAiScheduleDialog() }
                        )

                        NavDestination.MY_TASKS -> MyTasksScreen(
                            tasks = uiState.filteredTasks,
                            currentEpochMs = uiState.currentEpochMs,
                            selectedPriority = uiState.selectedPriority,
                            selectedStatus = uiState.selectedStatus,
                            selectedCategory = uiState.selectedCategory,
                            onSelectPriority = { viewModel.setPriorityFilter(it) },
                            onSelectStatus = { viewModel.setStatusFilter(it) },
                            onSelectCategory = { viewModel.setCategoryFilter(it) },
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openEditTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onChangePriority = { id, p -> viewModel.setTaskPriority(id, p) },
                            onAddTask = { viewModel.openAddTaskDialog() }
                        )

                        NavDestination.AI_SCHEDULE -> AiScheduleScreen(
                            isGenerating = uiState.isGeneratingSchedule,
                            generatedSchedule = uiState.generatedSchedule,
                            errorMessage = uiState.aiScheduleError,
                            currentTasks = uiState.tasks,
                            onGenerate = { p, d, pace -> viewModel.generateScheduleWithGemini(p, d, pace) },
                            onToggleSelection = { id -> viewModel.toggleGeneratedItemSelection(id) },
                            onRemoveItem = { id -> viewModel.removeGeneratedItem(id) },
                            onApplySchedule = { viewModel.applyGeneratedSchedule() }
                        )

                        NavDestination.CALENDAR -> CalendarView(
                            tasks = uiState.tasks,
                            selectedDateMs = uiState.selectedCalendarDateMs,
                            currentEpochMs = uiState.currentEpochMs,
                            onDateSelected = { viewModel.selectCalendarDate(it) },
                            onAddTaskOnDate = { viewModel.openAddTaskDialog(it) },
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openEditTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onChangePriority = { id, p -> viewModel.setTaskPriority(id, p) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        NavDestination.PROGRESS -> ProgressScreen(
                            tasks = uiState.tasks
                        )

                        NavDestination.IMPORTANT -> ImportantScreen(
                            tasks = uiState.tasks,
                            currentEpochMs = uiState.currentEpochMs,
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openEditTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onChangePriority = { id, p -> viewModel.setTaskPriority(id, p) },
                            onAddTask = { viewModel.openAddTaskDialog() }
                        )

                        NavDestination.COMPLETED -> CompletedScreen(
                            tasks = uiState.tasks,
                            currentEpochMs = uiState.currentEpochMs,
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openEditTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onChangePriority = { id, p -> viewModel.setTaskPriority(id, p) }
                        )

                        NavDestination.SETTINGS -> SettingsScreen(
                            userProfile = uiState.userProfile,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }

        // Floating Success / Notification Toast Banner
        androidx.compose.animation.AnimatedVisibility(
            visible = uiState.toastSuccessMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = uiState.toastSuccessMessage ?: "",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }

    // Add / Edit Task Modal
    if (uiState.isAddTaskDialogOpen) {
        AddTaskDialog(
            initialTask = uiState.editingTask,
            initialDateMs = uiState.selectedCalendarDateMs,
            onDismiss = { viewModel.closeTaskDialog() },
            onOpenAiScheduler = { viewModel.openAiScheduleDialog() },
            onSave = { id, title, subject, description, notes, dueDateMs, priority, estimatedHours ->
                viewModel.saveTask(
                    id = id,
                    title = title,
                    subject = subject,
                    description = description,
                    notes = notes,
                    dueDateMs = dueDateMs,
                    priority = priority,
                    estimatedHours = estimatedHours
                )
            }
        )
    }

    // Gemini AI Schedule Modal
    if (uiState.isAiScheduleDialogOpen) {
        AiScheduleDialog(
            isGenerating = uiState.isGeneratingSchedule,
            generatedSchedule = uiState.generatedSchedule,
            errorMessage = uiState.aiScheduleError,
            onDismiss = { viewModel.closeAiScheduleDialog() },
            onGenerate = { p, d, pace -> viewModel.generateScheduleWithGemini(p, d, pace) },
            onToggleSelection = { id -> viewModel.toggleGeneratedItemSelection(id) },
            onRemoveItem = { id -> viewModel.removeGeneratedItem(id) },
            onApplySchedule = { viewModel.applyGeneratedSchedule() }
        )
    }

    // Notifications Dialog
    if (uiState.isNotificationsDrawerOpen) {
        NotificationsDialog(
            notifications = uiState.notifications,
            onDismissRequest = { viewModel.toggleNotificationsDrawer(false) },
            onDismissNotification = { viewModel.dismissNotification(it) },
            onClearAll = { viewModel.clearAllNotifications() }
        )
    }

    // User Profile Dialog
    if (uiState.isProfileDialogOpen) {
        UserProfileDialog(
            userProfile = uiState.userProfile,
            onDismissRequest = { viewModel.toggleProfileDialog(false) },
            onToggleTheme = { viewModel.toggleTheme() },
            onLogout = { viewModel.logout() }
        )
    }
}

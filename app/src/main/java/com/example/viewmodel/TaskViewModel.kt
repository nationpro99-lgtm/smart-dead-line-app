package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GeminiScheduleService
import com.example.data.GeneratedScheduleItem
import com.example.data.NotificationItem
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.data.UserProfile
import com.example.model.DeadlineUrgency
import com.example.model.NavDestination
import com.example.model.NotificationType
import com.example.model.Priority
import com.example.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TaskUiState(
    val userProfile: UserProfile = UserProfile(isLoggedIn = false),
    val currentDestination: NavDestination = NavDestination.DASHBOARD,
    val tasks: List<TaskEntity> = emptyList(),
    val filteredTasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val selectedCategory: String? = null,
    val selectedStatus: String = "All",
    val notifications: List<NotificationItem> = emptyList(),
    val currentEpochMs: Long = System.currentTimeMillis(),
    val selectedCalendarDateMs: Long = System.currentTimeMillis(),
    val isAddTaskDialogOpen: Boolean = false,
    val editingTask: TaskEntity? = null,
    val isNotificationsDrawerOpen: Boolean = false,
    val isProfileDialogOpen: Boolean = false,
    val toastSuccessMessage: String? = null,
    val isDarkTheme: Boolean? = null,
    // Gemini AI Schedule Generator fields
    val isAiScheduleDialogOpen: Boolean = false,
    val isGeneratingSchedule: Boolean = false,
    val generatedSchedule: List<GeneratedScheduleItem> = emptyList(),
    val aiScheduleError: String? = null
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val geminiService = GeminiScheduleService()

    private val _userProfile = MutableStateFlow(UserProfile(name = "Anshuman", email = "anshuman@student.edu", isLoggedIn = true))
    private val _currentDestination = MutableStateFlow(NavDestination.DASHBOARD)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedStatus = MutableStateFlow("All")
    private val _currentEpochMs = MutableStateFlow(System.currentTimeMillis())
    private val _selectedCalendarDateMs = MutableStateFlow(System.currentTimeMillis())
    private val _isAddTaskDialogOpen = MutableStateFlow(false)
    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    private val _isNotificationsDrawerOpen = MutableStateFlow(false)
    private val _isProfileDialogOpen = MutableStateFlow(false)
    private val _toastSuccessMessage = MutableStateFlow<String?>(null)
    private val _isDarkTheme = MutableStateFlow<Boolean?>(null)

    // AI Schedule States
    private val _isAiScheduleDialogOpen = MutableStateFlow(false)
    private val _isGeneratingSchedule = MutableStateFlow(false)
    private val _generatedSchedule = MutableStateFlow<List<GeneratedScheduleItem>>(emptyList())
    private val _aiScheduleError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TaskUiState>

    init {
        val database = AppDatabase.getInstance(application)
        repository = TaskRepository(database.taskDao(), database.notificationDao())

        viewModelScope.launch {
            repository.ensureInitialData()
        }

        // Live ticker for countdown updating every second
        viewModelScope.launch {
            while (isActive) {
                _currentEpochMs.value = System.currentTimeMillis()
                delay(1000)
            }
        }

        val baseFlow = combine(
            repository.allTasks,
            repository.allNotifications,
            _userProfile,
            _currentDestination,
            _searchQuery,
            _selectedPriority,
            _selectedCategory,
            _selectedStatus
        ) { args -> args }

        val interactionFlow = combine(
            _currentEpochMs,
            _selectedCalendarDateMs,
            _isAddTaskDialogOpen,
            _editingTask,
            _isNotificationsDrawerOpen,
            _isProfileDialogOpen,
            _toastSuccessMessage,
            _isDarkTheme
        ) { args -> args }

        val aiFlow = combine(
            _isAiScheduleDialogOpen,
            _isGeneratingSchedule,
            _generatedSchedule,
            _aiScheduleError
        ) { args -> args }

        uiState = combine(baseFlow, interactionFlow, aiFlow) { base, interact, ai ->
            val tasks = base[0] as List<TaskEntity>
            val notifications = base[1] as List<NotificationItem>
            val userProfile = base[2] as UserProfile
            val destination = base[3] as NavDestination
            val search = base[4] as String
            val priority = base[5] as Priority?
            val category = base[6] as String?
            val status = base[7] as String

            val nowMs = interact[0] as Long
            val calendarDateMs = interact[1] as Long
            val isAddOpen = interact[2] as Boolean
            val editing = interact[3] as TaskEntity?
            val isNotifOpen = interact[4] as Boolean
            val isProfileOpen = interact[5] as Boolean
            val toast = interact[6] as String?
            val dark = interact[7] as Boolean?

            val isAiOpen = ai[0] as Boolean
            val isGenerating = ai[1] as Boolean
            val generated = ai[2] as List<GeneratedScheduleItem>
            val aiError = ai[3] as String?

            val filtered = tasks.filter { task ->
                val matchesSearch = search.isBlank() ||
                        task.title.contains(search, ignoreCase = true) ||
                        task.subject.contains(search, ignoreCase = true) ||
                        task.description.contains(search, ignoreCase = true)

                val matchesPriority = priority == null || task.priority == priority
                val matchesCategory = category == null || task.subject.equals(category, ignoreCase = true)

                val urgency = DateTimeUtils.getUrgency(task.dueDateEpochMs, nowMs)
                val matchesStatus = when (status) {
                    "Due Today" -> !task.isCompleted && (urgency == DeadlineUrgency.TODAY || urgency == DeadlineUrgency.CRITICAL)
                    "Upcoming" -> !task.isCompleted && (urgency == DeadlineUrgency.TOMORROW || urgency == DeadlineUrgency.UPCOMING || urgency == DeadlineUrgency.LATER)
                    "Overdue" -> !task.isCompleted && urgency == DeadlineUrgency.OVERDUE
                    "Completed" -> task.isCompleted
                    "Active" -> !task.isCompleted
                    else -> true
                }

                val matchesScreen = when (destination) {
                    NavDestination.IMPORTANT -> task.priority == Priority.HIGH
                    NavDestination.COMPLETED -> task.isCompleted
                    NavDestination.CALENDAR -> DateTimeUtils.isSameDay(task.dueDateEpochMs, calendarDateMs)
                    else -> true
                }

                matchesSearch && matchesPriority && matchesCategory && matchesStatus && matchesScreen
            }

            TaskUiState(
                userProfile = userProfile,
                currentDestination = destination,
                tasks = tasks,
                filteredTasks = filtered,
                searchQuery = search,
                selectedPriority = priority,
                selectedCategory = category,
                selectedStatus = status,
                notifications = notifications,
                currentEpochMs = nowMs,
                selectedCalendarDateMs = calendarDateMs,
                isAddTaskDialogOpen = isAddOpen,
                editingTask = editing,
                isNotificationsDrawerOpen = isNotifOpen,
                isProfileDialogOpen = isProfileOpen,
                toastSuccessMessage = toast,
                isDarkTheme = dark,
                isAiScheduleDialogOpen = isAiOpen,
                isGeneratingSchedule = isGenerating,
                generatedSchedule = generated,
                aiScheduleError = aiError
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskUiState()
        )
    }

    fun loginWithGoogle(accountName: String = "Anshuman", email: String = "anshuman@student.edu") {
        _userProfile.value = UserProfile(
            name = accountName,
            email = email,
            avatarUrl = "",
            isLoggedIn = true
        )
        showToast("Welcome back, $accountName! 👋")
    }

    fun loginWithEmail(name: String, email: String) {
        val finalName = if (name.isNotBlank()) name else email.substringBefore("@").replaceFirstChar { it.uppercase() }
        _userProfile.value = UserProfile(
            name = finalName,
            email = email,
            avatarUrl = "",
            isLoggedIn = true
        )
        showToast("Signed in as $finalName")
    }

    fun logout() {
        _userProfile.value = _userProfile.value.copy(isLoggedIn = false)
        _isProfileDialogOpen.value = false
    }

    fun setDestination(dest: NavDestination) {
        _currentDestination.value = dest
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPriorityFilter(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    fun selectCalendarDate(dateMs: Long) {
        _selectedCalendarDateMs.value = dateMs
    }

    fun openAddTaskDialog(prefillDateMs: Long? = null) {
        _editingTask.value = null
        if (prefillDateMs != null) {
            _selectedCalendarDateMs.value = prefillDateMs
        }
        _isAddTaskDialogOpen.value = true
    }

    fun openEditTaskDialog(task: TaskEntity) {
        _editingTask.value = task
        _isAddTaskDialogOpen.value = true
    }

    fun closeTaskDialog() {
        _isAddTaskDialogOpen.value = false
        _editingTask.value = null
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        subject: String,
        description: String,
        notes: String,
        dueDateMs: Long,
        priority: Priority,
        estimatedHours: Float
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val newTask = TaskEntity(
                    title = title.trim(),
                    subject = subject.trim(),
                    description = description.trim(),
                    notes = notes.trim(),
                    dueDateEpochMs = dueDateMs,
                    priority = priority,
                    estimatedHours = estimatedHours,
                    isCompleted = false
                )
                repository.insertTask(newTask)
                repository.addNotification(
                    title = "Task Created",
                    message = "\"${title.trim()}\" added to deadlines.",
                    type = NotificationType.TASK_CREATED
                )
                showToast("✓ Task added successfully")
            } else {
                val existing = _editingTask.value
                val updated = TaskEntity(
                    id = id,
                    title = title.trim(),
                    subject = subject.trim(),
                    description = description.trim(),
                    notes = notes.trim(),
                    dueDateEpochMs = dueDateMs,
                    priority = priority,
                    estimatedHours = estimatedHours,
                    isCompleted = existing?.isCompleted ?: false,
                    completedAtEpochMs = existing?.completedAtEpochMs,
                    createdAtEpochMs = existing?.createdAtEpochMs ?: System.currentTimeMillis()
                )
                repository.updateTask(updated)
                showToast("✓ Task updated successfully")
            }
            closeTaskDialog()
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            repository.setCompleted(task.id, newCompleted)
            if (newCompleted) {
                repository.addNotification(
                    title = "Task Completed 🎉",
                    message = "Great work finishing \"${task.title}\"!",
                    type = NotificationType.COMPLETED
                )
                showToast("Task completed! 🎉")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showToast("Task deleted")
        }
    }

    fun setTaskPriority(taskId: Long, priority: Priority) {
        viewModelScope.launch {
            repository.updatePriority(taskId, priority)
            showToast("Priority updated to ${priority.label}")
        }
    }

    fun toggleNotificationsDrawer(open: Boolean) {
        _isNotificationsDrawerOpen.value = open
    }

    fun toggleProfileDialog(open: Boolean) {
        _isProfileDialogOpen.value = open
    }

    fun dismissNotification(id: Long) {
        viewModelScope.launch {
            repository.dismissNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
            showToast("All notifications cleared")
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = when (_isDarkTheme.value) {
            true -> false
            false -> null
            null -> true
        }
    }

    // --- Gemini AI Schedule Operations ---

    fun openAiScheduleDialog() {
        _isAiScheduleDialogOpen.value = true
        _aiScheduleError.value = null
    }

    fun closeAiScheduleDialog() {
        _isAiScheduleDialogOpen.value = false
    }

    fun generateScheduleWithGemini(
        prompt: String,
        timeframeDays: Int = 7,
        pace: String = "Moderate"
    ) {
        if (prompt.isBlank()) {
            _aiScheduleError.value = "Please enter a subject, exam, or project goal"
            return
        }

        viewModelScope.launch {
            _isGeneratingSchedule.value = true
            _aiScheduleError.value = null

            val currentList = uiState.value.tasks
            val result = geminiService.generateSchedule(
                userPrompt = prompt,
                timeframeDays = timeframeDays,
                studyPace = pace,
                currentTasks = currentList
            )

            result.onSuccess { items ->
                _generatedSchedule.value = items
                _isGeneratingSchedule.value = false
            }.onFailure { error ->
                _aiScheduleError.value = error.message ?: "Failed to generate schedule"
                _isGeneratingSchedule.value = false
            }
        }
    }

    fun toggleGeneratedItemSelection(itemId: String) {
        _generatedSchedule.value = _generatedSchedule.value.map {
            if (it.id == itemId) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun removeGeneratedItem(itemId: String) {
        _generatedSchedule.value = _generatedSchedule.value.filter { it.id != itemId }
    }

    fun applyGeneratedSchedule() {
        val selected = _generatedSchedule.value.filter { it.isSelected }
        if (selected.isEmpty()) {
            showToast("No tasks selected")
            return
        }

        viewModelScope.launch {
            val baseTime = System.currentTimeMillis()
            val tasksToInsert = selected.map { it.toTaskEntity(baseTime) }

            tasksToInsert.forEach { repository.insertTask(it) }

            repository.addNotification(
                title = "✨ AI Schedule Activated",
                message = "Gemini added ${selected.size} milestone deadlines to your schedule.",
                type = NotificationType.TASK_CREATED
            )

            _generatedSchedule.value = emptyList()
            _isAiScheduleDialogOpen.value = false
            showToast("✨ ${selected.size} tasks scheduled by Gemini!")
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            _toastSuccessMessage.value = message
            delay(2800)
            if (_toastSuccessMessage.value == message) {
                _toastSuccessMessage.value = null
            }
        }
    }

    fun clearToast() {
        _toastSuccessMessage.value = null
    }
}

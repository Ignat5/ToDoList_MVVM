package com.example.todolistmvvm.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.todolistmvvm.data.PreferencesManager
import com.example.todolistmvvm.data.SortOrder
import com.example.todolistmvvm.data.Task
import com.example.todolistmvvm.data.TaskDao
import com.example.todolistmvvm.ui.ADD_TASK_RESULT_OKAY
import com.example.todolistmvvm.ui.EDIT_TASK_RESULT_OKAY
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor
    (private val taskDao: TaskDao, private val preferencesManager: PreferencesManager,
@Assisted private val state: SavedStateHandle): ViewModel() {
    //MutableStateFlow is used to be aware about changes to it's 'value' (that represents some state) and do something
    //about it: like when we change a text in SearchView - state is changing
    val searchQuery = state.getLiveData("searchQuery","")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private  val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow) {
        query,filterPreferences -> Pair(query,filterPreferences) }
        .flatMapLatest { (query,filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }
    val tasks = tasksFlow.asLiveData()

    fun onAddNewTaskClick() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
        }
    }

    fun onSortOrderSelected(sortOrder: SortOrder) {
        viewModelScope.launch {
            preferencesManager.updateSortOrder(sortOrder)
        }
    }
     fun onHideCompletedClick(hideCompleted:Boolean) {
         viewModelScope.launch {
             preferencesManager.updateHideCompleted(hideCompleted)
         }
     }

    fun onTaskSelected(task: Task) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
        }
    }
    fun onTaskCheckedChanged(task:Task,isChecked:Boolean) {
        viewModelScope.launch {
           val newTask: Task = task.copy(completed = isChecked)
            taskDao.update(newTask)
        }
    }

    fun onTaskSwiped(task :Task) {
        viewModelScope.launch {
            taskDao.delete(task)
            tasksEventChannel.send(TasksEvent.ShowUndoDeleteMessage(task))
        }
    }

    fun onUndoDeleteClick(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }


    fun onAddEditResult(result:Int) {
        when(result) {
            ADD_TASK_RESULT_OKAY->showTaskSavedConfirmation("Task created")
            EDIT_TASK_RESULT_OKAY->showTaskSavedConfirmation("Task updated")
        }
    }
    private fun showTaskSavedConfirmation(msg:String) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(msg))
        }
    }

    fun onDeleteAllCompletedClick() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateToAllDeletedScreen)
        }
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen: TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task):TasksEvent()
        data class ShowUndoDeleteMessage(val task: Task):TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String):TasksEvent()
        object NavigateToAllDeletedScreen: TasksEvent()
    }

    }


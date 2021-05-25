package com.example.todolistmvvm.ui.addedittasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistmvvm.data.Task
import com.example.todolistmvvm.data.TaskDao
import com.example.todolistmvvm.ui.ADD_TASK_RESULT_OKAY
import com.example.todolistmvvm.ui.EDIT_TASK_RESULT_OKAY
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name?:""
    set(value) {
        field = value
        state.set("taskName",value)
    }
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important?:false
        set(value) {
            field = value
            state.set("taskImportance",value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if(taskName.isBlank()) {
            showInvalidInputMessage("Name can't be empty")
            return
        }
        if (task != null) {
            val updatedTask: Task = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask: Task = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun createTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(
                ADD_TASK_RESULT_OKAY))
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(
                EDIT_TASK_RESULT_OKAY))
        }
    }

    private fun showInvalidInputMessage(msg: String) {
        viewModelScope.launch {
            addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(msg))
        }
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String):AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int):AddEditTaskEvent()
    }


}
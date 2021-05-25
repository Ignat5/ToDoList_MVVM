package com.example.todolistmvvm.ui.dialog

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistmvvm.data.TaskDao
import com.example.todolistmvvm.di.ApplicationScope
import com.example.todolistmvvm.ui.tasks.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao:TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope)
    :ViewModel() {

    fun onDeleteCompletedClick() {
        applicationScope.launch {
            taskDao.deleteCompletedTasks()
        }
    }
}
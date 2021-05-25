package com.example.todolistmvvm.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistmvvm.data.Task
import com.example.todolistmvvm.databinding.ItemTaskBinding

class TaskAdapter(private val listener: OnItemClickListener): ListAdapter<Task,TaskAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
       val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root) {
    //init{} - runs after object init-on
    init {
           binding.apply {
               root.setOnClickListener{
                   val position = adapterPosition
                   if(position!=RecyclerView.NO_POSITION) {
                       val task:Task = getItem(position)
                       listener.onItemClick(task)
                   }
               }
               checkBoxCompleted.setOnClickListener{
                   val position = adapterPosition
                   if(position!=RecyclerView.NO_POSITION) {
                       val task: Task = getItem(position)
                       listener.onCheckBoxClick(task,checkBoxCompleted.isChecked)
                   }
               }
           }
    }

        fun bind (task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }
    }
    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task:Task,isChecked:Boolean)
    }

    //class for tracking differences between old list and new one
    class DiffCallback: DiffUtil.ItemCallback<Task>() {

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem // works thank to implementation of data class that compare objects of the class
        }
    }

}
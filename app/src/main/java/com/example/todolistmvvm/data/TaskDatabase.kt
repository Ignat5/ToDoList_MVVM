package com.example.todolistmvvm.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todolistmvvm.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class],version = 1)
abstract class TaskDatabase:RoomDatabase() {
    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
      @ApplicationScope  private val applicationScope: CoroutineScope
    ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            //db operations
            val dao = database.get().taskDao()
            applicationScope.launch {
                dao.insert(Task("Book a therapy session"))
                dao.insert(Task("Go to gym"))
                dao.insert(Task("Arrive to the airport"))
                dao.insert(Task(name="Prepare for exam",important = true))
                dao.insert(Task("Read a book",completed = true))
            }


        }
    }

}
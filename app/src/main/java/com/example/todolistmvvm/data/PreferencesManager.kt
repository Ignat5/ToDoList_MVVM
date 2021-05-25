package com.example.todolistmvvm.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

//12:30
enum class SortOrder {BY_NAME,BY_DATE}

private const val TAG = "PreferencesManager"

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

@Singleton
class PreferencesManager  @Inject constructor(@ApplicationContext context: Context){

    private val dataStore = context.createDataStore("user_preferences")
    //read preference data
    val preferencesFlow = dataStore.data
        .catch {exception ->
            if(exception is IOException) {
                Log.e(TAG, "Error reading references: ",exception )
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER]?:SortOrder.BY_DATE.name)
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED]?:false
            FilterPreferences(sortOrder,hideCompleted)
        }

        //function for updating preference that holds our sortOrder value when user change it
        //need to use suspend function because here we work with I/O operations and they can freeze main thread
    suspend fun updateSortOrder(sortOrder: SortOrder) {
            dataStore.edit { preference ->
                preference[PreferencesKeys.SORT_ORDER] = sortOrder.name
            }
        }
     suspend fun updateHideCompleted(hideCompleted: Boolean) {
         dataStore.edit { preference->
             preference[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
         }
     }

    }
    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }

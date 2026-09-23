package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.core.model.dincharya.DincharyaGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DincharyaProgressRepository private constructor(private val context: Context) {

    companion object {
        private val KEY_SELECTED_GOAL = stringPreferencesKey("dincharya_selected_goal")
        
        @Volatile
        private var INSTANCE: DincharyaProgressRepository? = null

        fun getInstance(context: Context): DincharyaProgressRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DincharyaProgressRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        private fun getDoneKeyForDate(date: LocalDate) = 
            stringSetPreferencesKey("dincharya_done_${date}")
    }

    val selectedGoalFlow: Flow<DincharyaGoal> = context.dataStore.data.map { preferences ->
        val goalName = preferences[KEY_SELECTED_GOAL] ?: DincharyaGoal.SPIRITUAL_GROWTH.name
        try {
            DincharyaGoal.valueOf(goalName)
        } catch (e: Exception) {
            DincharyaGoal.SPIRITUAL_GROWTH
        }
    }

    suspend fun setSelectedGoal(goal: DincharyaGoal) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_GOAL] = goal.name
        }
    }

    fun getCompletedActivitiesFlow(date: LocalDate): Flow<Set<String>> {
        val key = getDoneKeyForDate(date)
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: emptySet()
        }
    }

    suspend fun toggleActivityCompletion(date: LocalDate, activityId: String) {
        val key = getDoneKeyForDate(date)
        context.dataStore.edit { preferences ->
            val current = preferences[key]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(activityId)) {
                current.remove(activityId)
            } else {
                current.add(activityId)
            }
            preferences[key] = current
        }
    }

    suspend fun setActivityCompleted(date: LocalDate, activityId: String, completed: Boolean) {
        val key = getDoneKeyForDate(date)
        context.dataStore.edit { preferences ->
            val current = preferences[key]?.toMutableSet() ?: mutableSetOf()
            if (completed) {
                current.add(activityId)
            } else {
                current.remove(activityId)
            }
            preferences[key] = current
        }
    }

    suspend fun resetDayProgress(date: LocalDate) {
        val key = getDoneKeyForDate(date)
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}

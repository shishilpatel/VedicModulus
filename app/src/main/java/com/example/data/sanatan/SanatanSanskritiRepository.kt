package com.example.data.sanatan

import android.content.Context
import androidx.datastore.preferences.core.*
import com.example.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SanatanSanskritiRepository private constructor(private val context: Context) {

    companion object {
        private val KEY_COMPLETED_TOPICS = stringSetPreferencesKey("sanatan_completed_topics")
        private val KEY_BOOKMARKED_TERMS = stringSetPreferencesKey("sanatan_bookmarked_terms")

        @Volatile
        private var INSTANCE: SanatanSanskritiRepository? = null

        fun getInstance(context: Context): SanatanSanskritiRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SanatanSanskritiRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        private fun getQuizScoreKey(quizId: String) =
            intPreferencesKey("sanatan_quiz_score_${quizId}")
    }

    val completedTopicsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_COMPLETED_TOPICS] ?: emptySet()
    }

    suspend fun toggleTopicCompleted(topicId: String) {
        context.dataStore.edit { prefs ->
            val set = prefs[KEY_COMPLETED_TOPICS]?.toMutableSet() ?: mutableSetOf()
            if (set.contains(topicId)) {
                set.remove(topicId)
            } else {
                set.add(topicId)
            }
            prefs[KEY_COMPLETED_TOPICS] = set
        }
    }

    val bookmarkedTermsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_BOOKMARKED_TERMS] ?: emptySet()
    }

    suspend fun toggleBookmarkTerm(termId: String) {
        context.dataStore.edit { prefs ->
            val set = prefs[KEY_BOOKMARKED_TERMS]?.toMutableSet() ?: mutableSetOf()
            if (set.contains(termId)) {
                set.remove(termId)
            } else {
                set.add(termId)
            }
            prefs[KEY_BOOKMARKED_TERMS] = set
        }
    }

    fun getQuizScoreFlow(quizId: String): Flow<Int?> {
        val key = getQuizScoreKey(quizId)
        return context.dataStore.data.map { prefs ->
            prefs[key]
        }
    }

    suspend fun saveQuizScore(quizId: String, percentage: Int) {
        val key = getQuizScoreKey(quizId)
        context.dataStore.edit { prefs ->
            val existing = prefs[key] ?: 0
            if (percentage > existing) {
                prefs[key] = percentage
            }
        }
    }
}

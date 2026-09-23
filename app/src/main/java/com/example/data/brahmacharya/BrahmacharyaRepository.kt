package com.example.data.brahmacharya

import android.content.Context
import androidx.datastore.preferences.core.*
import com.example.core.model.brahmacharya.AudienceCategory
import com.example.core.model.brahmacharya.BrahmacharyaJournalEntry
import com.example.core.model.brahmacharya.SankalpaDuration
import com.example.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BrahmacharyaRepository private constructor(private val context: Context) {

    companion object {
        private val KEY_AUDIENCE = stringPreferencesKey("brahmacharya_audience")
        private val KEY_ACTIVE_SANKALPA = stringPreferencesKey("brahmacharya_active_sankalpa")
        private val KEY_SANKALPA_START_DATE = stringPreferencesKey("brahmacharya_sankalpa_start_date")
        private val KEY_CURRENT_STREAK = intPreferencesKey("brahmacharya_current_streak")
        private val KEY_LONGEST_STREAK = intPreferencesKey("brahmacharya_longest_streak")
        private val KEY_TOTAL_PURE_DAYS = intPreferencesKey("brahmacharya_total_pure_days")
        private val KEY_LAST_CHECKIN_DATE = stringPreferencesKey("brahmacharya_last_checkin_date")
        private val KEY_SOS_USED_COUNT = intPreferencesKey("brahmacharya_sos_used_count")

        @Volatile
        private var INSTANCE: BrahmacharyaRepository? = null

        fun getInstance(context: Context): BrahmacharyaRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BrahmacharyaRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        private fun getHabitKeyForDate(date: LocalDate) =
            stringSetPreferencesKey("brahmacharya_habits_${date}")

        private fun getJournalNoteKey(date: LocalDate) =
            stringPreferencesKey("brahmacharya_journal_note_${date}")

        private fun getJournalClarityKey(date: LocalDate) =
            intPreferencesKey("brahmacharya_journal_clarity_${date}")
    }

    val audienceCategoryFlow: Flow<AudienceCategory> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_AUDIENCE] ?: AudienceCategory.UNMARRIED.name
        try {
            AudienceCategory.valueOf(name)
        } catch (e: Exception) {
            AudienceCategory.UNMARRIED
        }
    }

    suspend fun setAudienceCategory(category: AudienceCategory) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUDIENCE] = category.name
        }
    }

    val activeSankalpaFlow: Flow<SankalpaDuration?> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_ACTIVE_SANKALPA] ?: return@map null
        try {
            SankalpaDuration.valueOf(name)
        } catch (e: Exception) {
            null
        }
    }

    val sankalpaStartDateFlow: Flow<LocalDate?> = context.dataStore.data.map { prefs ->
        val dateStr = prefs[KEY_SANKALPA_START_DATE] ?: return@map null
        try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun takeSankalpa(duration: SankalpaDuration, startDate: LocalDate = LocalDate.now()) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_SANKALPA] = duration.name
            prefs[KEY_SANKALPA_START_DATE] = startDate.toString()
        }
    }

    suspend fun cancelOrCompleteSankalpa() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACTIVE_SANKALPA)
            prefs.remove(KEY_SANKALPA_START_DATE)
        }
    }

    val currentStreakFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENT_STREAK] ?: 0
    }

    val longestStreakFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_LONGEST_STREAK] ?: 0
    }

    val totalPureDaysFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_PURE_DAYS] ?: 0
    }

    val sosCountFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOS_USED_COUNT] ?: 0
    }

    suspend fun incrementSosCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SOS_USED_COUNT] ?: 0
            prefs[KEY_SOS_USED_COUNT] = current + 1
        }
    }

    fun getCompletedHabitsFlow(date: LocalDate): Flow<Set<String>> {
        val key = getHabitKeyForDate(date)
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: emptySet()
        }
    }

    suspend fun toggleHabit(date: LocalDate, habitId: String) {
        val key = getHabitKeyForDate(date)
        context.dataStore.edit { prefs ->
            val set = prefs[key]?.toMutableSet() ?: mutableSetOf()
            if (set.contains(habitId)) {
                set.remove(habitId)
            } else {
                set.add(habitId)
            }
            prefs[key] = set

            // Check if user has accomplished minimum check-in for the day
            val lastDateStr = prefs[KEY_LAST_CHECKIN_DATE]
            val lastDate = lastDateStr?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }

            if (set.size >= 3) {
                if (lastDate == null || lastDate.isBefore(date)) {
                    val currentStreak = prefs[KEY_CURRENT_STREAK] ?: 0
                    val newStreak = if (lastDate != null && ChronoUnit.DAYS.between(lastDate, date) == 1L) {
                        currentStreak + 1
                    } else if (lastDate == null || lastDate != date) {
                        1
                    } else {
                        currentStreak
                    }

                    val longest = prefs[KEY_LONGEST_STREAK] ?: 0
                    val total = prefs[KEY_TOTAL_PURE_DAYS] ?: 0

                    prefs[KEY_CURRENT_STREAK] = newStreak
                    prefs[KEY_LONGEST_STREAK] = maxOf(longest, newStreak)
                    prefs[KEY_TOTAL_PURE_DAYS] = total + 1
                    prefs[KEY_LAST_CHECKIN_DATE] = date.toString()
                }
            }
        }
    }

    suspend fun logJournalEntry(date: LocalDate, clarityScore: Int, note: String) {
        context.dataStore.edit { prefs ->
            prefs[getJournalClarityKey(date)] = clarityScore
            prefs[getJournalNoteKey(date)] = note
        }
    }

    fun getJournalEntryFlow(date: LocalDate): Flow<Pair<Int, String>?> {
        val clarityKey = getJournalClarityKey(date)
        val noteKey = getJournalNoteKey(date)
        return context.dataStore.data.map { prefs ->
            val clarity = prefs[clarityKey] ?: return@map null
            val note = prefs[noteKey] ?: ""
            Pair(clarity, note)
        }
    }
}

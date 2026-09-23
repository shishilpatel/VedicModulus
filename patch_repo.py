import re

with open("app/src/main/java/com/example/data/UserPreferencesRepository.kt", "r") as f:
    content = f.read()

bad_func = """    suspend fun clearBirthProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.BIRTH_PROFILE_JSON)
        }
    }"""

good_func = """    suspend fun clearBirthProfile() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_FULL_NAME)
            prefs.remove(KEY_IS_ONBOARDED)
        }
    }"""

content = content.replace(bad_func, good_func)

with open("app/src/main/java/com/example/data/UserPreferencesRepository.kt", "w") as f:
    f.write(content)

package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.core.model.BirthProfile
import com.example.core.model.AppThemePalette
import com.example.core.model.WatchFaceStyle
import com.example.core.model.UserAccount
import com.example.core.model.UserRole
import com.example.data.UserPreferencesRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.AstrologerDashboardScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.Constraints
import android.util.Log
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import java.util.Locale
import android.os.Build
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.workers.DailyVedicNotificationManager
import com.example.workers.VedicSyncWorker
import com.example.workers.DailyPanchangSyncWorker

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.LifecycleOwner
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.savedstate.SavedStateRegistryOwner

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    private lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Daily Vedic Notification Channel (instant)
        try {
            DailyVedicNotificationManager.createNotificationChannel(applicationContext)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to create notification channel", e)
        }

        // Offload WorkManager database registration to background IO thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                DailyVedicNotificationManager.scheduleDailyNotification(applicationContext)
                com.example.workers.TransitionNotificationManager.scheduleDailyTransitions(applicationContext)

                // Battery-efficient mode: check if device is charging
                val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                
                // Reduce sync frequency when not charging to preserve battery life (120 mins vs 15 mins)
                val syncIntervalMinutes = if (isCharging) 15L else 120L
                Log.d("MainActivity", "Battery-Efficient Mode: Device is charging = $isCharging. Setting sync interval to ${syncIntervalMinutes}m.")

                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

                val syncWorkRequest = PeriodicWorkRequestBuilder<VedicSyncWorker>(
                    syncIntervalMinutes, TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    "VedicWearSyncWork",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncWorkRequest
                )

                val transitWorkRequest = PeriodicWorkRequestBuilder<com.example.workers.TransitNotificationWorker>(12, TimeUnit.HOURS).build()
                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    "TransitNotificationWork",
                    ExistingPeriodicWorkPolicy.KEEP,
                    transitWorkRequest
                )

                val panchangSyncRequest = PeriodicWorkRequestBuilder<DailyPanchangSyncWorker>(
                    24, TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    "DailyPanchangSyncWork",
                    ExistingPeriodicWorkPolicy.KEEP,
                    panchangSyncRequest
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Background work scheduling failed", e)
            }
        }
        
        val initialNavTab = intent?.getStringExtra("EXTRA_NAV_TAB")
        
        preferencesRepository = UserPreferencesRepository(applicationContext)
        setContent {
            // Request Notification Permission on Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* Permission handled gracefully */ }
                )
                LaunchedEffect(Unit) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Request Location Permission for accurate astrological calculations
            val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            com.example.data.UserLocationSyncManager.getInstance().syncUserLocationAndTimezone(applicationContext)
                        }
                    }
                }
            )
            LaunchedEffect(Unit) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            val storedLanguage by preferencesRepository.languageFlow.collectAsState(initial = "en")
            val appLanguage = storedLanguage
            val currentLocale = Locale.forLanguageTag(appLanguage)
            val context = LocalContext.current
            
            val configuration = remember(appLanguage) {
                Configuration(context.resources.configuration).apply {
                    setLocale(currentLocale)
                    setLayoutDirection(currentLocale)
                }
            }

            val localizedContext = remember(appLanguage) {
                com.example.core.language.LanguageManager.createLocalizedContext(context, appLanguage)
            }

            LaunchedEffect(appLanguage) {
                com.example.core.language.MLKitTranslationService.prewarmLanguageModel(appLanguage)
            }

            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalContext provides localizedContext
            ) {
                MainAppContent(
                    preferencesRepository = preferencesRepository,
                    initialNavTab = initialNavTab,
                    appLanguage = appLanguage,
                    onLanguageChange = { lang ->
                        lifecycleScope.launch {
                            preferencesRepository.saveLanguage(lang)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainAppContent(
    preferencesRepository: UserPreferencesRepository,
    initialNavTab: String?,
    appLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }
    val birthProfileState = preferencesRepository.birthProfileFlow.collectAsState(initial = null)
    val userAccountState = preferencesRepository.userAccountFlow.collectAsState(initial = null)
    val activeClientIdState = preferencesRepository.activeClientIdFlow.collectAsState(initial = null)
    var isGuestMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Firebase AuthStateListener to monitor real-time auth changes & route properly
    DisposableEffect(Unit) {
        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            android.util.Log.d("MainActivityAuth", "Firebase AuthState changed: userUid=${user?.uid}, email=${user?.email}, isAnon=${user?.isAnonymous}")
            if (user != null && !user.isAnonymous) {
                scope.launch {
                    val currentStored = preferencesRepository.userAccountFlow.first()
                    if (currentStored == null || !currentStored.isLoggedIn) {
                        val updatedAccount = UserAccount(
                            email = user.email ?: "seeker.vedic@gmail.com",
                            fullName = user.displayName ?: "Shishil Patel",
                            role = currentStored?.role ?: UserRole.REGULAR,
                            isLoggedIn = true
                        )
                        preferencesRepository.saveUserAccount(updatedAccount)
                        isGuestMode = false
                    }
                }
            }
        }
        auth?.addAuthStateListener(authListener)
        onDispose {
            auth?.removeAuthStateListener(authListener)
        }
    }

    MyApplicationTheme {
        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val formFactor = when {
                maxWidth < 600.dp -> com.example.ui.theme.WindowFormFactor.COMPACT
                maxWidth < 840.dp -> com.example.ui.theme.WindowFormFactor.MEDIUM
                else -> com.example.ui.theme.WindowFormFactor.EXPANDED
            }
            val appDimens = com.example.ui.theme.AppDimens.get(formFactor)
            androidx.compose.runtime.CompositionLocalProvider(com.example.ui.theme.LocalAppDimens provides appDimens) {
                Crossfade(targetState = showSplash, label = "SplashFade") { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val userAccount = userAccountState.value
                        val scope = rememberCoroutineScope()
                        val context = LocalContext.current
                        if (userAccount == null && !isGuestMode) {
                            AuthScreen(
                                onAuthSuccess = { account, initialBirthProfile ->
                                    isGuestMode = false
                                    scope.launch {
                                        preferencesRepository.saveUserAccount(account)
                                        if (initialBirthProfile != null) {
                                            preferencesRepository.saveBirthProfile(initialBirthProfile)
                                            try {
                                                val db = com.example.data.room.VedicDatabase.getDatabase(context)
                                                val existingPrimary = db.userProfileDao().getPrimaryProfile()
                                                val entity = com.example.data.room.UserProfileEntity.fromBirthProfile(
                                                    profile = initialBirthProfile,
                                                    relation = "Self",
                                                    isPrimary = true,
                                                    id = existingPrimary?.id ?: 0L
                                                )
                                                db.userProfileDao().insertProfile(entity)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                },
                                onSkipAsGuest = { isGuestMode = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (userAccount?.role == UserRole.ASTROLOGER) {
                            AstrologerDashboardScreen(
                                userAccount = userAccount,
                                activeClientId = activeClientIdState.value,
                                onUpdateActiveClientId = { clientId ->
                                    scope.launch { preferencesRepository.saveActiveClientId(clientId) }
                                },
                                onSignOut = {
                                    scope.launch {
                                        preferencesRepository.signOutUser()
                                        isGuestMode = false
                                    }
                                },
                                currentLanguage = appLanguage,
                                onLanguageChange = onLanguageChange,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Crossfade(targetState = (birthProfileState.value != null), label = "MainRoot") { hasProfile ->
                                val profile = birthProfileState.value
                                if (!hasProfile || profile == null) {
                                    val defaultName = userAccount?.fullName?.takeIf { it.isNotBlank() } ?: "Shishil Patel"
                                    val initialSeedProfile = BirthProfile(
                                        fullName = defaultName,
                                        birthDate = java.time.LocalDate.of(1990, 8, 15),
                                        birthTime = java.time.LocalTime.of(7, 42),
                                        birthLocation = com.example.data.CityRepository.getDefaultCity()
                                    )
                                    OnboardingScreen(
                                        initialProfile = initialSeedProfile,
                                        onComplete = { newProfile ->
                                            scope.launch { preferencesRepository.saveBirthProfile(newProfile) }
                                        }
                                    )
                                } else {
                                    DashboardScreen(
                                        profile = profile,
                                        userAccount = userAccount,
                                        onEditProfile = { },
                                        onProfileAdd = { newProfile ->
                                            scope.launch { preferencesRepository.saveBirthProfile(newProfile) }
                                        },
                                        onStyleChanged = { style ->
                                            scope.launch { preferencesRepository.updateWatchStyle(style) }
                                        },
                                        onThemeChanged = { theme ->
                                            scope.launch { preferencesRepository.updateAppTheme(theme) }
                                        },
                                        currentLanguage = appLanguage,
                                        onLanguageChange = onLanguageChange,
                                        onSignOut = {
                                            scope.launch {
                                                preferencesRepository.signOutUser()
                                                isGuestMode = false
                                            }
                                        },
                                        initialNavTab = initialNavTab,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

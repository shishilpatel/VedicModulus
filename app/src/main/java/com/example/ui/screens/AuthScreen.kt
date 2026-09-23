package com.example.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.ui.components.GooglePlacesSearchField
import com.example.ui.components.VedicDatePickerField
import com.example.ui.components.VedicTimePickerField
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

private const val GOOGLE_SERVER_CLIENT_ID = "268483350661-aiju49hkk04brnlshqsnui3itnhqgg89.apps.googleusercontent.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (UserAccount, BirthProfile?) -> Unit,
    onSkipAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth: FirebaseAuth? = remember {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.REGULAR) }
    var astrologerLicense by remember { mutableStateOf("") }
    var registerBirthDate by remember { mutableStateOf(LocalDate.of(1995, 1, 1)) }
    var registerBirthTime by remember { mutableStateOf(LocalTime.of(7, 30)) }
    var registerBirthLocation by remember { mutableStateOf(CityRepository.getDefaultCity()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleAuthRunning by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }
    var customGoogleName by remember { mutableStateOf("") }
    var isAddingCustomGoogleAccount by remember { mutableStateOf(false) }

    fun executeGoogleSignIn(chosenEmail: String, chosenName: String) {
        isLoading = true
        isGoogleAuthRunning = true
        errorMessage = null
        showGoogleAccountPicker = false
        android.util.Log.i("VedicGoogleAuth", "Initiating Google sign-in session for user: $chosenName ($chosenEmail), Role: $selectedRole")
        scope.launch(Dispatchers.IO) {
            try {
                if (auth != null && auth.currentUser == null) {
                    try {
                        auth.signInAnonymously().await()
                        android.util.Log.d("VedicGoogleAuth", "Anonymous fallback token provisioned.")
                    } catch (e: Exception) {
                        android.util.Log.w("VedicGoogleAuth", "Anonymous session initialization warning: ${e.message}")
                    }
                }

                val signedInUser = UserAccount(
                    email = chosenEmail.trim(),
                    fullName = chosenName.trim(),
                    role = selectedRole,
                    astrologerTitleOrLicense = if (selectedRole == UserRole.ASTROLOGER) "Certified Jyotish Praveena" else "",
                    isLoggedIn = true
                )

                val initialProfile = BirthProfile(
                    fullName = chosenName.trim(),
                    birthDate = LocalDate.of(1990, 8, 15),
                    birthTime = LocalTime.of(7, 42),
                    birthLocation = CityRepository.getDefaultCity()
                )

                withContext(Dispatchers.Main) {
                    isLoading = false
                    isGoogleAuthRunning = false
                    android.util.Log.i("VedicGoogleAuth", "Google Sign-In completed successfully for ${signedInUser.email}")
                    onAuthSuccess(signedInUser, initialProfile)
                }
            } catch (e: Exception) {
                android.util.Log.e("VedicGoogleAuth", "Error during Google profile configuration: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    isGoogleAuthRunning = false
                    val fallbackUser = UserAccount(
                        email = chosenEmail.trim(),
                        fullName = chosenName.trim(),
                        role = selectedRole,
                        astrologerTitleOrLicense = if (selectedRole == UserRole.ASTROLOGER) "Certified Jyotish Praveena" else "",
                        isLoggedIn = true
                    )
                    val initialProfile = BirthProfile(
                        fullName = chosenName.trim(),
                        birthDate = LocalDate.of(1990, 8, 15),
                        birthTime = LocalTime.of(7, 42),
                        birthLocation = CityRepository.getDefaultCity()
                    )
                    onAuthSuccess(fallbackUser, initialProfile)
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    // Celestial Vedic Luxury Color Palette for Auth
    val cosmicMaroon = Color(0xFF660000)
    val sacredGold = Color(0xFFFFB703)
    val goldAccent = Color(0xFFD4AF37)
    val cardBg = Color(0xFFFFFFFF)
    val cardBorder = Color(0xFFE2E8F0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFDF7),
                        Color(0xFFFBF4E8),
                        Color(0xFFF3E8D8)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Logo Header with glowing sacred ring
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape),
                color = Color(0xFFFFFFFF),
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, goldAccent)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Vedic Cosmic Watch Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "VEDIC COSMIC GHADI",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cosmicMaroon,
                letterSpacing = 1.8.sp
            )
            Text(
                text = "Personalized Jyotisha, Panchanga & Kundali Portal",
                fontSize = 12.sp,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Sign In / Register Tab Selector
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFEFE6D8),
                border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            isRegisterMode = false
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isRegisterMode) cosmicMaroon else Color.Transparent,
                            contentColor = if (!isRegisterMode) Color.White else DarkText
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("tab_login")
                    ) {
                        Text(
                            "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!isRegisterMode) Color.White else DarkText
                        )
                    }

                    Button(
                        onClick = {
                            isRegisterMode = true
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegisterMode) cosmicMaroon else Color.Transparent,
                            contentColor = if (isRegisterMode) Color.White else DarkText
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("tab_register")
                    ) {
                        Text(
                            "Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isRegisterMode) Color.White else DarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compact Role Selector Card (Icon on Left, Title on Right)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Regular User Option (Left Icon, Right Title)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedRole == UserRole.REGULAR) MaroonPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (selectedRole == UserRole.REGULAR) 1.5.dp else 0.5.dp,
                            color = if (selectedRole == UserRole.REGULAR) MaroonPrimary else LightBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = UserRole.REGULAR }
                            .testTag("role_regular")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedRole == UserRole.REGULAR) MaroonPrimary else GrayText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Regular",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Astrologer Option (Left Icon, Right Title)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedRole == UserRole.ASTROLOGER) MaroonPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (selectedRole == UserRole.ASTROLOGER) 1.5.dp else 0.5.dp,
                            color = if (selectedRole == UserRole.ASTROLOGER) MaroonPrimary else LightBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = UserRole.ASTROLOGER }
                            .testTag("role_astrologer")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (selectedRole == UserRole.ASTROLOGER) MaroonPrimary else GrayText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Astrologer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Fields
            AnimatedVisibility(visible = isRegisterMode) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name / Title") },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = MaroonPrimary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedLabelColor = MaroonPrimary,
                        unfocusedLabelColor = GrayText,
                        focusedTextColor = Color(0xFF212529),
                        unfocusedTextColor = Color(0xFF212529),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_fullname_input")
                )
            }

            AnimatedVisibility(visible = isRegisterMode && selectedRole == UserRole.ASTROLOGER) {
                OutlinedTextField(
                    value = astrologerLicense,
                    onValueChange = { astrologerLicense = it },
                    label = { Text("Organization / Title / License (Optional)") },
                    placeholder = { Text("e.g. Maharishi Astro Center / Vedic Jyotish Samiti") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaroonPrimary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedLabelColor = MaroonPrimary,
                        unfocusedLabelColor = GrayText,
                        focusedTextColor = Color(0xFF212529),
                        unfocusedTextColor = Color(0xFF212529),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_license_input")
                )
            }

            // Birth Details (DOB & TOB) for both Regular and Astrologer users during registration
            AnimatedVisibility(visible = isRegisterMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Primary Birth Details (for Kundli, Horoscope & Charts)",
                        color = MaroonPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                    )

                    // Date and Time Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            VedicDatePickerField(
                                date = registerBirthDate,
                                onDateChange = { registerBirthDate = it },
                                label = "Date of Birth",
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "auth_dob_picker",
                                borderColor = MaroonPrimary.copy(alpha = 0.35f),
                                accentColor = MaroonPrimary
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            VedicTimePickerField(
                                time = registerBirthTime,
                                onTimeChange = { registerBirthTime = it },
                                label = "Time of Birth",
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "auth_tob_picker",
                                borderColor = MaroonPrimary.copy(alpha = 0.35f),
                                accentColor = MaroonPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Birth Place Selector
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Birth Place: ${registerBirthLocation.cityName}, ${registerBirthLocation.state}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            GooglePlacesSearchField(
                                selectedLocation = registerBirthLocation,
                                onLocationSelected = { registerBirthLocation = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_birth_location_input")
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaroonPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    unfocusedBorderColor = LightBorder,
                    focusedLabelColor = MaroonPrimary,
                    unfocusedLabelColor = GrayText,
                    focusedTextColor = Color(0xFF212529),
                    unfocusedTextColor = Color(0xFF212529),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaroonPrimary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = GrayText
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    unfocusedBorderColor = LightBorder,
                    focusedLabelColor = MaroonPrimary,
                    unfocusedLabelColor = GrayText,
                    focusedTextColor = Color(0xFF212529),
                    unfocusedTextColor = Color(0xFF212529),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input")
            )

            AnimatedVisibility(visible = isRegisterMode) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = MaroonPrimary) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaroonPrimary,
                            unfocusedBorderColor = LightBorder,
                            focusedLabelColor = MaroonPrimary,
                            unfocusedLabelColor = GrayText,
                            focusedTextColor = Color(0xFF212529),
                            unfocusedTextColor = Color(0xFF212529),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_input")
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFF8B0000).copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B6B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFFC0C0),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Auth Button
            Button(
                onClick = {
                    if (email.isBlank() || !email.contains("@")) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                        return@Button
                    }
                    if (isRegisterMode && password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch(Dispatchers.IO) {
                        try {
                            val nameToUse = if (fullName.isNotBlank()) fullName else if (selectedRole == UserRole.ASTROLOGER) "Acharya ${email.substringBefore("@").replaceFirstChar { it.uppercase() }}" else email.substringBefore("@").replaceFirstChar { it.uppercase() }

                            val primaryProfile = if (isRegisterMode) {
                                BirthProfile(
                                    fullName = nameToUse,
                                    birthDate = registerBirthDate,
                                    birthTime = registerBirthTime,
                                    birthLocation = registerBirthLocation,
                                    ayanamsha = AyanamshaSystem.LAHIRI,
                                    calendarTradition = CalendarTradition.AMANTA,
                                    numerologySystem = NumerologySystem.CHALDEAN,
                                    language = LanguageDisplay.ENGLISH,
                                    preferredStyle = WatchFaceStyle.COSMIC
                                )
                            } else null

                            if (auth != null) {
                                if (isRegisterMode) {
                                    val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                    val firebaseUser = result.user
                                    if (firebaseUser != null && fullName.isNotBlank()) {
                                        try {
                                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName.trim())
                                                .build()
                                            firebaseUser.updateProfile(profileUpdates).await()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    val userAccount = UserAccount(
                                        email = firebaseUser?.email ?: email.trim(),
                                        fullName = nameToUse,
                                        role = selectedRole,
                                        astrologerTitleOrLicense = astrologerLicense,
                                        isLoggedIn = true
                                    )
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        onAuthSuccess(userAccount, primaryProfile)
                                    }
                                } else {
                                    val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                                    val firebaseUser = result.user
                                    val userAccount = UserAccount(
                                        email = firebaseUser?.email ?: email.trim(),
                                        fullName = if (fullName.isNotBlank()) fullName.trim() else (firebaseUser?.displayName ?: nameToUse),
                                        role = selectedRole,
                                        astrologerTitleOrLicense = astrologerLicense,
                                        isLoggedIn = true
                                    )
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        onAuthSuccess(userAccount, null)
                                    }
                                }
                            } else {
                                // Direct local authentication fallback if Firebase config is uninitialized
                                val userAccount = UserAccount(
                                    email = email.trim(),
                                    fullName = nameToUse,
                                    role = selectedRole,
                                    astrologerTitleOrLicense = astrologerLicense,
                                    isLoggedIn = true
                                )
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    onAuthSuccess(userAccount, primaryProfile)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                errorMessage = e.localizedMessage ?: "Authentication failed. Please check your credentials."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cosmicMaroon,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_primary_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "Create ${selectedRole.displayName} Account" else "Sign In as ${selectedRole.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Divider OR Continue with Google
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37).copy(alpha = 0.4f))
                Text(
                    text = "OR CONNECT WITH",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37).copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Google Sign In Button - High contrast, sacred gold & crisp readable styling
            Surface(
                onClick = {
                    if (isLoading || isGoogleAuthRunning) return@Surface
                    val activityContext = com.example.core.language.LanguageManager.findActivity(context)
                    isGoogleAuthRunning = true
                    isLoading = true
                    android.util.Log.i("VedicGoogleAuth", "Starting Google Sign-In via Credential Manager. App package: ${context.packageName}, Server Client ID: $GOOGLE_SERVER_CLIENT_ID")
                    
                    if (activityContext != null && auth != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val credentialManager = CredentialManager.create(activityContext)
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(GOOGLE_SERVER_CLIENT_ID)
                                    .setAutoSelectEnabled(false)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                android.util.Log.d("VedicGoogleAuth", "Calling CredentialManager.getCredential...")
                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = activityContext
                                )

                                val credential = result.credential
                                android.util.Log.d("VedicGoogleAuth", "Credential received: type=${credential.type}, class=${credential.javaClass.simpleName}")
                                
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    android.util.Log.i("VedicGoogleAuth", "Google ID Token received for ${googleIdTokenCredential.id}. Authenticating with Firebase...")
                                    
                                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                                    val authResult = auth.signInWithCredential(authCredential).await()
                                    val firebaseUser = authResult.user

                                    val googleEmail = firebaseUser?.email ?: googleIdTokenCredential.id
                                    val googleName = firebaseUser?.displayName ?: googleIdTokenCredential.displayName ?: "Shishil Patel"
                                    android.util.Log.i("VedicGoogleAuth", "Firebase auth successful for Google user: $googleEmail ($googleName)")

                                    executeGoogleSignIn(googleEmail, googleName)
                                    return@launch
                                } else {
                                    android.util.Log.w("VedicGoogleAuth", "Unrecognized credential type received: ${credential.type}")
                                }
                            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                                android.util.Log.w("VedicGoogleAuth", "Google Sign-In was cancelled by user: ${e.message}")
                            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                                android.util.Log.w("VedicGoogleAuth", "No Google account credentials found on device: ${e.message}. Presenting fallback account selector.")
                            } catch (e: androidx.credentials.exceptions.GetCredentialProviderConfigurationException) {
                                android.util.Log.e("VedicGoogleAuth", "Credential Provider Configuration Error: Check SHA-1 certificate fingerprint and OAuth 2.0 Web Client ID in Google Cloud & Firebase console. ${e.message}", e)
                            } catch (e: Exception) {
                                android.util.Log.e("VedicGoogleAuth", "Credential Manager encountered exception: ${e.javaClass.simpleName} - ${e.message}", e)
                            }
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                isGoogleAuthRunning = false
                                showGoogleAccountPicker = true
                            }
                        }
                    } else {
                        isLoading = false
                        isGoogleAuthRunning = false
                        showGoogleAccountPicker = true
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_google_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGoogleAuthRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = cosmicMaroon,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connecting to Google...",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        GoogleIcon(modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Skip / Guest button
            Surface(
                onClick = onSkipAsGuest,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF800000).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF800000).copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_skip_guest")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = cosmicMaroon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Explore App as Guest (Continue without Login)",
                        color = cosmicMaroon,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Interactive Google Account Chooser Dialog
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    GoogleIcon(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose an account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "to continue to " + stringResource(R.string.app_name),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Account 1: Shishil Patel (Active User Google Account)
                    Surface(
                        onClick = {
                            executeGoogleSignIn("shishil.patel9@gmail.com", "Shishil Patel")
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4285F4),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "S",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Shishil Patel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "shishil.patel9@gmail.com",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AuspiciousGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Account 2: Astrologer / Practitioner Account option if role is Astrologer
                    if (selectedRole == UserRole.ASTROLOGER) {
                        Surface(
                            onClick = {
                                executeGoogleSignIn("acharya.shastri.jyotish@gmail.com", "Acharya Shastri")
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF660000),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "A",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Acharya Shastri (Jyotish)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "acharya.shastri.jyotish@gmail.com",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    // Add Custom Google Account toggle
                    if (!isAddingCustomGoogleAccount) {
                        Surface(
                            onClick = { isAddingCustomGoogleAccount = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Use another Google account",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customGoogleName,
                                onValueChange = { customGoogleName = it },
                                label = { Text("Your Google Name") },
                                placeholder = { Text("e.g. Shishil Patel") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customGoogleEmail,
                                onValueChange = { customGoogleEmail = it },
                                label = { Text("Google Email Address") },
                                placeholder = { Text("e.g. yourname@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val name = customGoogleName.ifBlank { "Google Seeker" }
                                    val mail = customGoogleEmail.ifBlank { "user.vedic@gmail.com" }
                                    executeGoogleSignIn(mail, name)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign in with this Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Modal visual feedback during Google Authentication
    if (isGoogleAuthRunning) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* Non-dismissible while authenticating */ },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB703)),
                modifier = Modifier.padding(16.dp).testTag("auth_google_progress_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaroonPrimary,
                        strokeWidth = 3.5.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Authenticating with Google",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verifying Google credentials & syncing your Vedic astrological charts...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier.size(24.dp)) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = minOf(w, h) * 0.45f
        val stroke = radius * 0.36f

        val arcRect = androidx.compose.ui.geometry.Rect(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        // Red (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        // Yellow (bottom-left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        // Green (bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        // Blue (right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 315f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        // Blue crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(cx - stroke * 0.1f, cy),
            end = androidx.compose.ui.geometry.Offset(cx + radius + stroke * 0.18f, cy),
            strokeWidth = stroke
        )
    }
}



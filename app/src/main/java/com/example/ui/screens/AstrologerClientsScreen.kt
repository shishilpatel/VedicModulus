package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.data.room.ClientProfileEntity
import com.example.data.room.VedicDatabase
import com.example.ui.components.GooglePlacesSearchField
import com.example.ui.components.VedicDatePickerField
import com.example.ui.components.VedicTimePickerField
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstrologerClientsScreen(
    astrologerEmail: String,
    activeClientId: Long?,
    onSelectActiveClient: (ClientProfileEntity) -> Unit,
    onNavigateToCharts: (ClientProfileEntity) -> Unit,
    onNavigateToNotes: ((ClientProfileEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { VedicDatabase.getDatabase(context) }
    val clientDao = remember { database.clientProfileDao() }

    var searchQuery by remember { mutableStateOf("") }
    val clientListState = clientDao.getClientsForAstrologerFlow(astrologerEmail)
        .collectAsState(initial = emptyList())

    val filteredClients = remember(clientListState.value, searchQuery) {
        if (searchQuery.isBlank()) {
            clientListState.value
        } else {
            val q = searchQuery.trim().lowercase()
            clientListState.value.filter { client ->
                client.fullName.lowercase().contains(q) ||
                        client.cityName.lowercase().contains(q) ||
                        client.birthDate.contains(q) ||
                        client.notes.lowercase().contains(q) ||
                        client.phoneOrEmail.lowercase().contains(q)
            }
        }
    }

    var showAddClientDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientProfileEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ClientProfileEntity?>(null) }

    // Pre-populate mock clients if astrologer has no clients yet for easy demo
    LaunchedEffect(astrologerEmail) {
        withContext(Dispatchers.IO) {
            val count = clientDao.getClientById(1)
            if (clientListState.value.isEmpty()) {
                val mock1 = ClientProfileEntity(
                    astrologerEmail = astrologerEmail,
                    fullName = "Ramesh Sharma",
                    birthDate = "1988-05-14",
                    birthTime = "08:30",
                    cityName = "Mumbai",
                    state = "Maharashtra",
                    country = "India",
                    latitude = 19.0760,
                    longitude = 72.8777,
                    timezoneId = "Asia/Kolkata",
                    notes = "Asking about Sade Sati transition & Career promotion in Jupiter Dasha.",
                    phoneOrEmail = "+91 98765 43210",
                    lastConsultationDate = "2026-09-01"
                )
                val mock2 = ClientProfileEntity(
                    astrologerEmail = astrologerEmail,
                    fullName = "Priya Varma",
                    birthDate = "1994-11-22",
                    birthTime = "14:15",
                    cityName = "Bengaluru",
                    state = "Karnataka",
                    country = "India",
                    latitude = 12.9716,
                    longitude = 77.5946,
                    timezoneId = "Asia/Kolkata",
                    notes = "Kundli Matching evaluation for marriage compatibility.",
                    phoneOrEmail = "priya.v@gmail.com",
                    lastConsultationDate = "2026-08-20"
                )
                clientDao.insertClient(mock1)
                clientDao.insertClient(mock2)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clientToEdit = null
                    showAddClientDialog = true
                },
                containerColor = MaroonPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_client")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Client Profile")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search client name, birth date, city, or notes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = MaroonPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color(0xFF6C757D)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    unfocusedBorderColor = Color(0xFFCED4DA),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF212529),
                    unfocusedTextColor = Color(0xFF212529),
                    focusedPlaceholderColor = Color(0xFF6C757D),
                    unfocusedPlaceholderColor = Color(0xFF6C757D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("astrologer_client_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Summary Ribbon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CLIENT DIRECTORY (${filteredClients.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Manage birth profiles, consultations & Kundli charts",
                            fontSize = 11.sp,
                            color = Color(0xFF6C757D)
                        )
                    }

                    Button(
                        onClick = {
                            clientToEdit = null
                            showAddClientDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaroonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Client",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clients List
            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = Color(0xFFADB5BD),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No clients registered yet" else "No clients matching '$searchQuery'",
                            fontSize = 15.sp,
                            color = Color(0xFF212529),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ New Client' to add your first client profile.",
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("astrologer_clients_list")
                ) {
                    items(filteredClients, key = { it.id }) { client ->
                        val isActive = client.id == activeClientId

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0xFFFFFDF8) else Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) MaroonPrimary else Color(0xFFE2E8F0)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Profile Icon Avatar
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isActive) MaroonPrimary else MaroonPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = client.fullName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = if (isActive) Color.White else MaroonPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = client.fullName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF212529),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isActive) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaroonPrimary
                                                ) {
                                                    Text(
                                                        text = "ACTIVE CLIENT",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = MaroonPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${client.birthDate} at ${client.birthTime}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF495057)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = MaroonPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = client.cityName,
                                                fontSize = 12.sp,
                                                color = Color(0xFF495057)
                                            )
                                        }

                                        if (client.phoneOrEmail.isNotBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContactPhone,
                                                    contentDescription = null,
                                                    tint = Color(0xFF6C757D),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = client.phoneOrEmail,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6C757D)
                                                )
                                            }
                                        }
                                    }

                                    // Action overflow
                                    IconButton(
                                        onClick = { clientToEdit = client; showAddClientDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Client",
                                            tint = Color(0xFF495057),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (client.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8F9FA),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "📝 Note: ${client.notes}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF495057),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            onSelectActiveClient(client)
                                            onNavigateToCharts(client)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaroonPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("View Kundli & Reports", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    if (onNavigateToNotes != null) {
                                        OutlinedButton(
                                            onClick = {
                                                onSelectActiveClient(client)
                                                onNavigateToNotes(client)
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaroonPrimary
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Notes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showDeleteConfirmDialog = client },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFDC3545)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC3545).copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Client",
                                            modifier = Modifier.size(16.dp)
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

    // Add / Edit Client Dialog
    if (showAddClientDialog) {
        ClientEditorDialog(
            astrologerEmail = astrologerEmail,
            existingClient = clientToEdit,
            onDismiss = { showAddClientDialog = false },
            onSave = { newOrUpdated ->
                scope.launch(Dispatchers.IO) {
                    if (newOrUpdated.id == 0L) {
                        val newId = clientDao.insertClient(newOrUpdated)
                        val inserted = newOrUpdated.copy(id = newId)
                        withContext(Dispatchers.Main) {
                            onSelectActiveClient(inserted)
                        }
                    } else {
                        clientDao.updateClient(newOrUpdated)
                    }
                    withContext(Dispatchers.Main) {
                        showAddClientDialog = false
                    }
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog != null) {
        val clientToDelete = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Client Profile", color = Color(0xFF212529), fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete profile for ${clientToDelete.fullName}? This cannot be undone.", color = Color(0xFF495057)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            clientDao.deleteClient(clientToDelete.id)
                            withContext(Dispatchers.Main) {
                                showDeleteConfirmDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel", color = Color(0xFF6C757D))
                }
            },
            containerColor = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientEditorDialog(
    astrologerEmail: String,
    existingClient: ClientProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (ClientProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingClient?.fullName ?: "") }
    var birthDate by remember {
        mutableStateOf(
            try {
                LocalDate.parse(existingClient?.birthDate ?: "1992-07-10")
            } catch (_: Exception) {
                LocalDate.of(1992, 7, 10)
            }
        )
    }
    var birthTime by remember {
        mutableStateOf(
            try {
                LocalTime.parse(existingClient?.birthTime ?: "10:30")
            } catch (_: Exception) {
                LocalTime.of(10, 30)
            }
        )
    }
    var selectedLocation by remember {
        mutableStateOf(
            LocationData(
                country = existingClient?.country ?: "India",
                state = existingClient?.state ?: "Maharashtra",
                district = existingClient?.district ?: "Mumbai",
                talukaOrTown = existingClient?.talukaOrTown ?: "Mumbai",
                cityName = existingClient?.cityName ?: "Mumbai",
                latitude = existingClient?.latitude ?: 19.0760,
                longitude = existingClient?.longitude ?: 72.8777,
                timezoneId = existingClient?.timezoneId ?: "Asia/Kolkata"
            )
        )
    }
    var phoneOrEmail by remember { mutableStateOf(existingClient?.phoneOrEmail ?: "") }
    var notes by remember { mutableStateOf(existingClient?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingClient == null) "Add New Client Profile" else "Edit Client Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Client Full Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        focusedLabelColor = MaroonPrimary,
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        focusedTextColor = Color(0xFF212529),
                        unfocusedTextColor = Color(0xFF212529)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                VedicDatePickerField(
                    date = birthDate,
                    onDateChange = { birthDate = it },
                    label = "Client Date of Birth (Tap to select)",
                    borderColor = Color(0xFFCED4DA),
                    accentColor = MaroonPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "client_date_picker_field"
                )

                VedicTimePickerField(
                    time = birthTime,
                    onTimeChange = { birthTime = it },
                    label = "Client Time of Birth (Tap to select)",
                    borderColor = Color(0xFFCED4DA),
                    accentColor = MaroonPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "client_time_picker_field"
                )

                Text(
                    text = "Birth Location",
                    fontSize = 12.sp,
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold
                )

                GooglePlacesSearchField(
                    selectedLocation = selectedLocation,
                    onLocationSelected = { selectedLocation = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneOrEmail,
                    onValueChange = { phoneOrEmail = it },
                    label = { Text("Client Phone / Email") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        focusedLabelColor = MaroonPrimary,
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        focusedTextColor = Color(0xFF212529),
                        unfocusedTextColor = Color(0xFF212529)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Astrological Notes / Questions") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        focusedLabelColor = MaroonPrimary,
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        focusedTextColor = Color(0xFF212529),
                        unfocusedTextColor = Color(0xFF212529)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val clientEntity = ClientProfileEntity(
                            id = existingClient?.id ?: 0L,
                            astrologerEmail = astrologerEmail,
                            fullName = name.trim(),
                            birthDate = birthDate.toString(),
                            birthTime = String.format(java.util.Locale.US, "%02d:%02d", birthTime.hour, birthTime.minute),
                            cityName = selectedLocation.cityName,
                            state = selectedLocation.state,
                            country = selectedLocation.country,
                            latitude = selectedLocation.latitude,
                            longitude = selectedLocation.longitude,
                            timezoneId = selectedLocation.timezoneId,
                            notes = notes.trim(),
                            phoneOrEmail = phoneOrEmail.trim(),
                            lastConsultationDate = existingClient?.lastConsultationDate ?: LocalDate.now().toString()
                        )
                        onSave(clientEntity)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Client", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6C757D))
            }
        },
        containerColor = Color.White
    )
}

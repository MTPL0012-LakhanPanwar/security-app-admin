package com.security.cameralockfacility.ui

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.security.cameralockfacility.modal.ApiResult
import com.security.cameralockfacility.modal.FacilityData
import com.security.cameralockfacility.viewmodel.FacilityViewModel
import kotlinx.coroutines.launch

private val CuBgDark = Color(0xFF0B101F)
private val CuAccentBlue = Color(0xFF2196F3)
private val CuTextGray = Color(0xFF8A92A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUpdateScreen(
    facilityId: String?,
    navController: NavHostController,
    viewModel: FacilityViewModel
) {
    val isEditMode = facilityId != null
    val detailState by viewModel.detailState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isSaving = saveState is ApiResult.Loading

    // Form fields
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    val emails = remember { mutableStateListOf("") }
    var timezone by remember { mutableStateOf("UTC") }
    var status by remember { mutableStateOf("active") }
    var formLoaded by remember { mutableStateOf(!isEditMode) }

    // Load detail for edit mode
    LaunchedEffect(facilityId) {
        if (isEditMode) viewModel.loadDetail(facilityId!!)
    }

    // Pre-fill form from detail
    LaunchedEffect(detailState) {
        if (detailState is ApiResult.Success && isEditMode && !formLoaded) {
            val f = (detailState as ApiResult.Success<FacilityData>).data
            name = f.name
            description = f.description ?: ""
            address = f.location?.address ?: ""
            city = f.location?.city ?: ""
            state = f.location?.state ?: ""
            country = f.location?.country ?: ""
            if (f.notificationEmails.isNotEmpty()) {
                emails.clear()
                emails.addAll(f.notificationEmails)
            }
            timezone = f.timezone ?: "UTC"
            status = f.status
            formLoaded = true
        }
        if (detailState is ApiResult.Error && isEditMode) {
            scope.launch { snackbarHostState.showSnackbar((detailState as ApiResult.Error).message) }
        }
    }

    // Handle save result
    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is ApiResult.Success -> {
                scope.launch { snackbarHostState.showSnackbar(s.data) }
                viewModel.resetSave()
                viewModel.refreshFacilities()
                navController.popBackStack()
            }
            is ApiResult.Error -> {
                scope.launch { snackbarHostState.showSnackbar(s.message) }
                viewModel.resetSave()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = CuBgDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "EDIT FACILITY" else "CREATE FACILITY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetDetail()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CuBgDark)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = Color(0xFF2E7D32), contentColor = Color.White)
            }
        }
    ) { innerPadding ->
        // Show loading while fetching detail for edit
        if (isEditMode && detailState is ApiResult.Loading && !formLoaded) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CuAccentBlue)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomInputField(label = "Facility Name *", value = name, onValueChange = { name = it })
            CustomInputField(label = "Description", value = description, onValueChange = { description = it }, singleLine = false)

            // Location Section
            Text("Location", color = CuTextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            CustomInputField(label = "Address", value = address, onValueChange = { address = it })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    CustomInputField(label = "City", value = city, onValueChange = { city = it })
                }
                Column(modifier = Modifier.weight(1f)) {
                    CustomInputField(label = "State", value = state, onValueChange = { state = it })
                }
            }
            CustomInputField(label = "Country", value = country, onValueChange = { country = it })

            // Notification Emails
            EmailSection(
                emailList = emails,
                onAddEmail = { emails.add("") },
                onRemoveEmail = { index -> if (emails.size > 1) emails.removeAt(index) },
                onEmailChange = { index, value -> emails[index] = value }
            )

            // Timezone
            CustomDropdown(
                label = "Timezone",
                options = listOf("Asia/Kolkata", "UTC",),
                selectedOption = timezone,
                onOptionSelected = { timezone = it }
            )

            // Status
            CustomDropdown(
                label = "Status",
                options = listOf("active", "inactive"),
                selectedOption = status,
                onOptionSelected = { status = it }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Facility name is required") }
                        return@Button
                    }
                    val emailList = emails.filter { it.isNotBlank() }
                    if (isEditMode) {
                        viewModel.updateFacility(facilityId!!, name, description, address, city, state, country, emailList, timezone, status)
                    } else {
                        viewModel.createFacility(name, description, address, city, state, country, emailList, timezone, status)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.50f)
                    .align(Alignment.CenterHorizontally)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CuAccentBlue),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (isEditMode) "Update Facility" else "Create Facility",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmailSection(
    emailList: List<String>,
    onAddEmail: () -> Unit,
    onRemoveEmail: (Int) -> Unit,
    onEmailChange: (Int, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notification Emails", color = CuTextGray, fontSize = 14.sp)
            IconButton(onClick = onAddEmail) {
                Icon(Icons.Default.AddCircle, null, tint = CuAccentBlue)
            }
        }
        emailList.forEachIndexed { index, email ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { onEmailChange(index, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("email@example.com", color = Color.DarkGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CuAccentBlue,
                        unfocusedBorderColor = Color(0xFF2A3245)
                    )
                )
                if (emailList.size > 1) {
                    IconButton(onClick = { onRemoveEmail(index) }) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = CuTextGray, fontSize = 13.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0D1426),
                unfocusedContainerColor = Color(0xFF0D1426),
                focusedBorderColor = CuAccentBlue,
                unfocusedBorderColor = Color(0xFF2A3245),
                cursorColor = CuAccentBlue
            )
        )
    }
}

@Composable
fun CustomDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = CuTextGray, fontSize = 13.sp)
        Box {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            null,
                            tint = Color.White,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0D1426),
                    unfocusedContainerColor = Color(0xFF0D1426),
                    focusedBorderColor = CuAccentBlue,
                    unfocusedBorderColor = Color(0xFF2A3245)
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color(0xFF161C2C)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = { onOptionSelected(option); expanded = false }
                    )
                }
            }
        }
    }
}

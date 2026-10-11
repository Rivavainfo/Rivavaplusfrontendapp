package com.rivavafi.universal.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSessionPopup(
    initialName: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onRequestSession: (name: String, phone: String, dateStr: String, timeStr: String, message: String) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialName) }
    // Clean phone number to national 10 digits if prefills with +91
    val cleanedInitialPhone = remember(initialPhone) {
        val digits = initialPhone.filter { it.isDigit() }
        if (digits.length > 10 && digits.startsWith("91")) digits.takeLast(10) else digits.take(10)
    }
    var phoneDigits by remember { mutableStateOf(cleanedInitialPhone) }
    var additionalMessage by remember { mutableStateOf("") }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val formattedDate = remember(selectedDateMillis) {
        selectedDateMillis?.let {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(cal.time)
        } ?: ""
    }

    val formattedTime = remember(selectedHour, selectedMinute) {
        if (selectedHour != null && selectedMinute != null) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour!!)
                set(Calendar.MINUTE, selectedMinute!!)
            }
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
        } else ""
    }

    // Validation
    val isNameValid = name.trim().isNotBlank()
    val isPhoneValid = phoneDigits.length == 10 && phoneDigits.all { it.isDigit() }
    val isDateSelected = selectedDateMillis != null
    val isTimeSelected = selectedHour != null && selectedMinute != null

    val isFutureDateTime = remember(selectedDateMillis, selectedHour, selectedMinute) {
        if (selectedDateMillis != null && selectedHour != null && selectedMinute != null) {
            val chosenCal = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis!!
                set(Calendar.HOUR_OF_DAY, selectedHour!!)
                set(Calendar.MINUTE, selectedMinute!!)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            chosenCal.timeInMillis > System.currentTimeMillis()
        } else true
    }

    val isFormValid = isNameValid && isPhoneValid && isDateSelected && isTimeSelected && isFutureDateTime

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101524)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00C6FF).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BOOK A 1-ON-1 SESSION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Schedule a session with your Rivava Advisor",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Divider(color = Color(0xFF1E283D), thickness = 1.dp)

                // Name Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Full Name", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00C6FF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00C6FF),
                            unfocusedBorderColor = Color(0xFF26334D),
                            focusedContainerColor = Color(0xFF0B0E18),
                            unfocusedContainerColor = Color(0xFF0B0E18)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (!isNameValid && name.isNotEmpty()) {
                        Text("Name cannot be empty", color = Color(0xFFFF3366), fontSize = 10.sp)
                    }
                }

                // Phone Input with fixed +91 prefix
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Phone Number", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    OutlinedTextField(
                        value = phoneDigits,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phoneDigits = digitsOnly
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF00C6FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+91", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFF26334D)))
                            }
                        },
                        placeholder = { Text("Enter 10-digit number", color = Color(0xFF64748B), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00C6FF),
                            unfocusedBorderColor = Color(0xFF26334D),
                            focusedContainerColor = Color(0xFF0B0E18),
                            unfocusedContainerColor = Color(0xFF0B0E18)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (phoneDigits.isNotEmpty() && !isPhoneValid) {
                        Text("Please enter a valid 10-digit Indian mobile number", color = Color(0xFFFF3366), fontSize = 10.sp)
                    }
                }

                // Preferred Date Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Preferred Date", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B0E18))
                            .border(1.dp, if (isDateSelected) Color(0xFF00C6FF) else Color(0xFF26334D), RoundedCornerShape(12.dp))
                            .clickable { showDatePickerDialog = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF00C6FF), modifier = Modifier.size(20.dp))
                                Text(
                                    text = if (isDateSelected) formattedDate else "Select Date",
                                    color = if (isDateSelected) Color.White else Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Preferred Time Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Preferred Time", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B0E18))
                            .border(1.dp, if (isTimeSelected) Color(0xFF00C6FF) else Color(0xFF26334D), RoundedCornerShape(12.dp))
                            .clickable { showTimePickerDialog = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF00C6FF), modifier = Modifier.size(20.dp))
                                Text(
                                    text = if (isTimeSelected) formattedTime else "Select Time",
                                    color = if (isTimeSelected) Color.White else Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    if (!isFutureDateTime) {
                        Text("Selected session date & time cannot be in the past", color = Color(0xFFFF3366), fontSize = 10.sp)
                    }
                }

                // Additional Message (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Additional Message (Optional)", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    OutlinedTextField(
                        value = additionalMessage,
                        onValueChange = { additionalMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        placeholder = { Text("Enter details or topics you want to discuss", color = Color(0xFF64748B), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00C6FF),
                            unfocusedBorderColor = Color(0xFF26334D),
                            focusedContainerColor = Color(0xFF0B0E18),
                            unfocusedContainerColor = Color(0xFF0B0E18)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334B73))
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isFormValid && !isSubmitting) {
                                isSubmitting = true
                                onRequestSession(name, "+91$phoneDigits", formattedDate, formattedTime, additionalMessage)
                            }
                        },
                        enabled = isFormValid && !isSubmitting,
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C6FF),
                            disabledContainerColor = Color(0xFF1E283D)
                        )
                    ) {
                        Text(
                            text = if (isSubmitting) "Submitting..." else "Request Session",
                            color = if (isFormValid) Color(0xFF0A0F1D) else Color(0xFF64748B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val todayMillis = remember {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: todayMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayMillis - (24 * 60 * 60 * 1000)
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePickerDialog = false
                }) {
                    Text("OK", color = Color(0xFF00C6FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color(0xFF1A1F2C))
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1A1F2C),
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color(0xFF00C6FF),
                    subheadContentColor = Color.White,
                    selectedDayContainerColor = Color(0xFF00C6FF),
                    selectedDayContentColor = Color.Black,
                    todayDateBorderColor = Color(0xFF00C6FF),
                    todayContentColor = Color(0xFF00C6FF)
                )
            )
        }
    }

    // Material 3 Time Picker Dialog
    if (showTimePickerDialog) {
        val initialCal = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour ?: initialCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedMinute ?: initialCal.get(Calendar.MINUTE),
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePickerDialog = false
                }) {
                    Text("OK", color = Color(0xFF00C6FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFF0B0E18),
                            clockDialSelectedContentColor = Color.Black,
                            clockDialUnselectedContentColor = Color.White,
                            selectorColor = Color(0xFF00C6FF),
                            containerColor = Color(0xFF1A1F2C),
                            periodSelectorBorderColor = Color(0xFF00C6FF),
                            periodSelectorSelectedContainerColor = Color(0xFF00C6FF),
                            periodSelectorSelectedContentColor = Color.Black,
                            periodSelectorUnselectedContainerColor = Color(0xFF0B0E18),
                            periodSelectorUnselectedContentColor = Color.White,
                            timeSelectorSelectedContainerColor = Color(0xFF00C6FF),
                            timeSelectorSelectedContentColor = Color.Black,
                            timeSelectorUnselectedContainerColor = Color(0xFF0B0E18),
                            timeSelectorUnselectedContentColor = Color.White
                        )
                    )
                }
            },
            containerColor = Color(0xFF1A1F2C)
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EditProfileDialog(
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    initialClientCode: String,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, phone: String, clientCode: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(initialPhone) }
    var clientCode by remember { mutableStateOf(initialClientCode) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            border = BorderStroke(1.dp, NavyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Edit Profile",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Personal info & WhatsApp contact",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_profile_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Full Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null && it.isNotBlank()) nameError = null
                    },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Rakesh Nironi") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Name", tint = CyanAccent)
                    },
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let { Text(it, color = Color(0xFFFF5252)) }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Registered Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null && it.contains("@")) emailError = null
                    },
                    label = { Text("Registered Email Address") },
                    placeholder = { Text("e.g. rakeshnironi@gmail.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = CyanAccent)
                    },
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { Text(it, color = Color(0xFFFF5252)) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // WhatsApp Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError != null && it.filter { c -> c.isDigit() }.length >= 10) phoneError = null
                    },
                    label = { Text("WhatsApp Alert Number") },
                    placeholder = { Text("e.g. +91 9876543210") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone", tint = EmeraldGreen)
                    },
                    isError = phoneError != null,
                    supportingText = {
                        if (phoneError != null) {
                            Text(phoneError!!, color = Color(0xFFFF5252))
                        } else {
                            Text("Automated ML Buy/Sell trading alerts sent to this number", color = EmeraldGreen, fontSize = 11.sp)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = EmeraldGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Demat / Broker Client Code
                OutlinedTextField(
                    value = clientCode,
                    onValueChange = { clientCode = it },
                    label = { Text("Angel One / Broker Client Code") },
                    placeholder = { Text("e.g. ANGEL_RN99 or RN9942") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = "Client Code", tint = CyanAccent)
                    },
                    supportingText = {
                        Text("Unique trading account identifier", color = TextMuted, fontSize = 11.sp)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_client_code_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dialog Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_edit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NavyBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = "Please enter your name"
                                hasError = true
                            }
                            if (!email.contains("@") || !email.contains(".")) {
                                emailError = "Enter a valid email address"
                                hasError = true
                            }
                            if (phone.filter { it.isDigit() }.length < 10) {
                                phoneError = "Enter a valid 10-digit phone number"
                                hasError = true
                            }
                            if (!hasError) {
                                onSave(name.trim(), email.trim(), phone.trim(), clientCode.trim())
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_edit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = NavyDeep
                        )
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

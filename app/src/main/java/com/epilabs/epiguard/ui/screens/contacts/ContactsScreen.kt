package com.epilabs.epiguard.ui.screens.contacts

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.models.Contact
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.ConfirmDialog
import com.epilabs.epiguard.ui.screens.contacts.ContactViewDialog
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.viewmodels.ContactViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.Validators

// Hardcoded colors from design
private val DarkBackground = Color(0xFF11222E)
private val TextFieldBorder = Color(0xFF2F414F)
private val TextFieldBackground = Color(0xFF11222E)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
private val AccentGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFFF6B6B)

@Composable
fun ContactsScreen(
    navController: NavController,
    contactViewModel: ContactViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    // Set system bars to dark
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    LaunchedEffect(Unit) {
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.statusBarColor = DarkBackground.toArgb()
            it.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
        }
    }

    val contacts by contactViewModel.contacts.collectAsState()
    val isLoading by contactViewModel.isLoading.collectAsState()
    val error by contactViewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ButtonBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Emergency Contacts",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "These contacts will be notified when a seizure is detected",
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty() && !isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextFieldBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No contacts added yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Add your first emergency contact using the + button",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { contact ->
                        ContactItem(
                            contact = contact,
                            onView = {
                                selectedContact = contact
                                showViewDialog = true
                            },
                            onEdit = {
                                selectedContact = contact
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedContact = contact
                                showDeleteDialog = true
                            },
                            onLink = {
                                selectedContact = contact
                                showLinkDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add contact dialog
    if (showAddDialog) {
        ContactDialog(
            title = "Add Contact",
            onSave = { contact ->
                contactViewModel.addContact(contact)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit contact dialog
    if (showEditDialog && selectedContact != null) {
        ContactDialog(
            title = "Edit Contact",
            initialContact = selectedContact,
            onSave = { contact ->
                contactViewModel.updateContact(contact)
                showEditDialog = false
                selectedContact = null
            },
            onDismiss = {
                showEditDialog = false
                selectedContact = null
            }
        )
    }

    // NEW: View contact dialog
    if (showViewDialog && selectedContact != null) {
        ContactViewDialog(
            contact = selectedContact!!,
            onEdit = {
                showViewDialog = false
                showEditDialog = true
                // selectedContact stays the same for edit
            },
            onDismiss = {
                showViewDialog = false
                selectedContact = null
            }
        )
    }

    // Delete confirmation
    ConfirmDialog(
        isVisible = showDeleteDialog,
        title = "Delete Contact",
        message = "Are you sure you want to delete ${selectedContact?.firstName} ${selectedContact?.lastName}?",
        onConfirm = {
            selectedContact?.let { contact ->
                contactViewModel.deleteContact(contact.contactId)
            }
            showDeleteDialog = false
            selectedContact = null
        },
        onDismiss = {
            showDeleteDialog = false
            selectedContact = null
        },
        confirmText = "Delete"
    )

    // Link to user dialog
    if (showLinkDialog && selectedContact != null) {
        LinkUserDialog(
            contact = selectedContact!!,
            onLink = { email ->
                contactViewModel.linkContactToUser(selectedContact!!.contactId, email)
                showLinkDialog = false
                selectedContact = null
            },
            onDismiss = {
                showLinkDialog = false
                selectedContact = null
            }
        )
    }

    // Loading and error dialogs
    LoadingDialog(
        isVisible = isLoading,
        message = "Processing..."
    )

    ErrorDialog(
        isVisible = error != null,
        message = error ?: "",
        onDismiss = { contactViewModel.clearError() }
    )
}

@Composable
private fun ContactItem(
    contact: Contact,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLink: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() },
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextFieldBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${contact.firstName} ${contact.lastName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = contact.email,
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = contact.contactNumber,
                        fontSize = 15.sp,
                        color = TextSecondary
                    )

                    if (contact.isSystemUser) {
                        Text(
                            text = "✓ EpiGuard User",
                            fontSize = 13.sp,
                            color = AccentGreen
                        )
                    }
                }

                Row {
                    if (!contact.isSystemUser) {
                        IconButton(onClick = onLink) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = "Link to user",
                                tint = ButtonBlue
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactDialog(
    title: String,
    initialContact: Contact? = null,
    onSave: (Contact) -> Unit,
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf(initialContact?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialContact?.lastName ?: "") }
    var email by remember { mutableStateOf(initialContact?.email ?: "") }
    var contactNumber by remember { mutableStateOf(initialContact?.contactNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground,
        title = {
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("First Name", color = TextFieldPlaceholder, fontSize = 17.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextFieldPlaceholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextFieldBorder,
                        unfocusedBorderColor = TextFieldBorder,
                        focusedContainerColor = TextFieldBackground,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ButtonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Last Name", color = TextFieldPlaceholder, fontSize = 17.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextFieldPlaceholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextFieldBorder,
                        unfocusedBorderColor = TextFieldBorder,
                        focusedContainerColor = TextFieldBackground,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ButtonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = TextFieldPlaceholder, fontSize = 17.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextFieldPlaceholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextFieldBorder,
                        unfocusedBorderColor = TextFieldBorder,
                        focusedContainerColor = TextFieldBackground,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ButtonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    placeholder = { Text("Phone Number", color = TextFieldPlaceholder, fontSize = 17.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextFieldPlaceholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextFieldBorder,
                        unfocusedBorderColor = TextFieldBorder,
                        focusedContainerColor = TextFieldBackground,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ButtonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (Validators.isValidName(firstName) &&
                        Validators.isValidName(lastName) &&
                        Validators.isValidEmail(email) &&
                        Validators.isValidPhoneNumber(contactNumber)) {

                        val contact = Contact(
                            contactId = initialContact?.contactId ?: "",
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            contactNumber = contactNumber.trim()
                        )
                        onSave(contact)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text("Save", fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary, fontSize = 15.sp)
            }
        }
    )
}

@Composable
private fun LinkUserDialog(
    contact: Contact,
    onLink: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var userEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground,
        title = {
            Text(
                "Link to EpiGuard User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the email address of the EpiGuard user you want to link to ${contact.firstName} ${contact.lastName}:",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    placeholder = { Text("User Email", color = TextFieldPlaceholder, fontSize = 17.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextFieldPlaceholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextFieldBorder,
                        unfocusedBorderColor = TextFieldBorder,
                        focusedContainerColor = TextFieldBackground,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ButtonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (Validators.isValidEmail(userEmail)) {
                        onLink(userEmail.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text("Link", fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary, fontSize = 15.sp)
            }
        }
    )
}
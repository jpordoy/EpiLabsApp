package com.epilabs.epiguard.ui.screens.contacts

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
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

@Composable
fun ContactsScreen(
    navController: NavController,
    contactViewModel: ContactViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val contacts by contactViewModel.contacts.collectAsState()
    val isLoading by contactViewModel.isLoading.collectAsState()
    val error by contactViewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }  // NEW: View dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "These contacts will be notified when a seizure is detected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty() && !isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No contacts added yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Add your first emergency contact using the + button",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onView = {  // NEW: View action
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
    onView: () -> Unit,  // NEW: View action
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLink: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }  // NEW: Click to view
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = contact.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = contact.contactNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (contact.isSystemUser) {
                        Text(
                            text = "✓ EpiGuard User",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row {
                    if (!contact.isSystemUser) {
                        IconButton(onClick = onLink) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = "Link to user",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
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
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
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
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
        title = { Text("Link to EpiGuard User") },
        text = {
            Column {
                Text(
                    text = "Enter the email address of the EpiGuard user you want to link to ${contact.firstName} ${contact.lastName}:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("User Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (Validators.isValidEmail(userEmail)) {
                        onLink(userEmail.trim())
                    }
                }
            ) {
                Text("Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
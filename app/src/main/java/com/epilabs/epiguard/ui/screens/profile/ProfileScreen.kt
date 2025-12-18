package com.epilabs.epiguard.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.ConfirmDialog
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.AuthViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.Validators

@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user by userViewModel.currentUser.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()

    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var username by remember { mutableStateOf(user?.username ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var contactNumber by remember { mutableStateOf(user?.contactNumber ?: "") }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) } // modal toggle

    val context = LocalContext.current

    // Sync values with loaded user
    user?.let { u ->
        firstName = u.firstName
        lastName = u.lastName
        username = u.username
        email = u.email
        contactNumber = u.contactNumber
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                inputStream?.let { stream ->
                    userViewModel.uploadProfileImage(stream)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pen icon aligned top-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                }
            }

            // Profile picture
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.profileImageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user!!.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_default_icon),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Camera icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Static info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoRow("First Name", firstName)
                ProfileInfoRow("Last Name", lastName)
                ProfileInfoRow("Username", username)
                ProfileInfoRow("Email", email)
                ProfileInfoRow("Contact Number", contactNumber)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        singleLine = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (user != null &&
                        Validators.isValidName(firstName) &&
                        Validators.isValidName(lastName) &&
                        Validators.isValidUsername(username) &&
                        Validators.isValidPhoneNumber(contactNumber)) {

                        val updatedUser = user!!.copy(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            username = username.trim(),
                            contactNumber = contactNumber.trim()
                        )
                        userViewModel.updateUser(updatedUser)
                        showEditDialog = false
                    }
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Loading dialog
    LoadingDialog(
        isVisible = isLoading,
        message = "Updating profile..."
    )

    // Error dialog
    ErrorDialog(
        isVisible = error != null,
        message = error ?: "",
        onDismiss = { userViewModel.clearError() }
    )

    // Sign out confirmation
    ConfirmDialog(
        isVisible = showSignOutDialog,
        title = "Sign Out",
        message = "Are you sure you want to sign out?",
        onConfirm = {
            authViewModel.signOut(context)
            navController.navigate(Destinations.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        },
        onDismiss = { showSignOutDialog = false },
        confirmText = "Sign Out"
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

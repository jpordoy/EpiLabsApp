package com.epilabs.epiguard.ui.screens.auth

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.AuthViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.Validators
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Hardcoded colors from your design
private val DarkBackground = Color(0xFF11222E)
private val TextFieldBorder = Color(0xFF2F414F)
private val TextFieldBackground = Color(0xFF11222E)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondaryWhite = Color(0xFFF4E9F6)
private val ProgressTrackColor = Color(0xFF2F414F)

@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
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

    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3
    val progress by animateFloatAsState(
        targetValue = currentStep / totalSteps.toFloat(),
        animationSpec = tween(300),
        label = "progress"
    )

    // Step 1 - Email
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isCheckingEmail by remember { mutableStateOf(false) }

    // Step 2 - Passwords
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Step 3 - Personal Info
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var dateOfBirth by remember { mutableStateOf<Long?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var contactNumberError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate(Destinations.EmailLinkOtp.route) {
                    popUpTo(Destinations.Signup.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep-- }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = "Step $currentStep of $totalSteps",
                    fontSize = 15.sp,
                    color = TextFieldPlaceholder
                )

                TextButton(
                    onClick = { navController.navigate(Destinations.Login.route) }
                ) {
                    Text(
                        "Sign In",
                        fontSize = 15.sp,
                        color = ButtonBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = ButtonBlue,
                trackColor = ProgressTrackColor,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title and subtitle
            Text(
                text = when (currentStep) {
                    1 -> "What's your email?"
                    2 -> "Create a password"
                    else -> "Tell us about yourself"
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (currentStep) {
                    1 -> "We'll check if you already have an account"
                    2 -> "Make sure it's secure and easy to remember"
                    else -> "Just a few more details to get started"
                },
                fontSize = 17.sp,
                color = TextFieldPlaceholder,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Animated Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { if (targetState > initialState) it else -it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { if (targetState > initialState) -it else it }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = "step_content"
            ) { step ->
                when (step) {
                    1 -> EmailStep(
                        email = email,
                        onEmailChange = {
                            email = it
                            emailError = null
                        },
                        emailError = emailError,
                        isChecking = isCheckingEmail,
                        onNext = {
                            if (!Validators.isValidEmail(email)) {
                                emailError = "Please enter a valid email"
                                return@EmailStep
                            }

                            isCheckingEmail = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isCheckingEmail = false
                                currentStep = 2
                            }, 1000)
                        }
                    )

                    2 -> PasswordStep(
                        password = password,
                        confirmPassword = confirmPassword,
                        passwordVisible = passwordVisible,
                        confirmPasswordVisible = confirmPasswordVisible,
                        passwordError = passwordError,
                        confirmPasswordError = confirmPasswordError,
                        onPasswordChange = {
                            password = it
                            passwordError = null
                        },
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onConfirmPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        onNext = {
                            passwordError = when {
                                password.isEmpty() -> "Password is required"
                                password.length < 6 -> "Password must be at least 6 characters"
                                else -> null
                            }
                            confirmPasswordError = when {
                                confirmPassword.isEmpty() -> "Please confirm your password"
                                confirmPassword != password -> "Passwords do not match"
                                else -> null
                            }

                            if (passwordError == null && confirmPasswordError == null) {
                                currentStep = 3
                            }
                        }
                    )

                    3 -> PersonalInfoStep(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        contactNumber = contactNumber,
                        gender = gender,
                        dateOfBirth = dateOfBirth,
                        selectedImageUri = selectedImageUri,
                        firstNameError = firstNameError,
                        lastNameError = lastNameError,
                        usernameError = usernameError,
                        contactNumberError = contactNumberError,
                        genderError = genderError,
                        dobError = dobError,
                        onFirstNameChange = {
                            firstName = it
                            firstNameError = null
                        },
                        onLastNameChange = {
                            lastName = it
                            lastNameError = null
                        },
                        onUsernameChange = {
                            username = it
                            usernameError = null
                        },
                        onContactNumberChange = {
                            contactNumber = it
                            contactNumberError = null
                        },
                        onGenderSelect = {
                            gender = it
                            genderError = null
                        },
                        onDateOfBirthSelect = {
                            dateOfBirth = it
                            dobError = null
                        },
                        onImageSelect = { imagePickerLauncher.launch("image/*") },
                        context = context,
                        onCreateAccount = {
                            firstNameError = if (firstName.isEmpty()) "First name is required" else null
                            lastNameError = if (lastName.isEmpty()) "Last name is required" else null
                            usernameError = if (username.isEmpty()) "Username is required" else null
                            contactNumberError = if (!Validators.isValidPhoneNumber(contactNumber)) "Please enter a valid phone number" else null
                            genderError = if (gender.isEmpty()) "Please select your gender" else null
                            dobError = if (dateOfBirth == null) "Please select your date of birth" else null

                            if (listOf(firstNameError, lastNameError, usernameError, contactNumberError, genderError, dobError).all { it == null }) {
                                authViewModel.signUpExtended(
                                    email = email.trim(),
                                    password = password,
                                    firstName = firstName.trim(),
                                    lastName = lastName.trim(),
                                    username = username.trim(),
                                    contactNumber = contactNumber.trim(),
                                    gender = gender,
                                    dateOfBirth = dateOfBirth ?: 0L,
                                    profileImageUrl = ""
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    LoadingDialog(
        isVisible = authState is AuthViewModel.AuthState.Loading || isCheckingEmail,
        message = when {
            isCheckingEmail -> "Checking email..."
            else -> "Creating account..."
        }
    )

    ErrorDialog(
        isVisible = authState is AuthViewModel.AuthState.Error,
        message = (authState as? AuthViewModel.AuthState.Error)?.message ?: "",
        onDismiss = { authViewModel.clearState() }
    )
}

@Composable
private fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isChecking: Boolean,
    onNext: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Email address", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = email.isNotEmpty() && !isChecking,
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(40.dp)
        ) {
            Text(
                "Continue",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PasswordStep(
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    passwordError: String?,
    confirmPasswordError: String?,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Password", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = TextFieldPlaceholder
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = { Text("Confirm Password", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = TextFieldPlaceholder
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = password.isNotEmpty() && confirmPassword.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(40.dp)
        ) {
            Text(
                "Continue",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PersonalInfoStep(
    firstName: String,
    lastName: String,
    username: String,
    contactNumber: String,
    gender: String,
    dateOfBirth: Long?,
    selectedImageUri: Uri?,
    firstNameError: String?,
    lastNameError: String?,
    usernameError: String?,
    contactNumberError: String?,
    genderError: String?,
    dobError: String?,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onContactNumberChange: (String) -> Unit,
    onGenderSelect: (String) -> Unit,
    onDateOfBirthSelect: (Long) -> Unit,
    onImageSelect: () -> Unit,
    context: Context,
    onCreateAccount: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Column {
        // Profile Image
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { onImageSelect() }
                .clip(CircleShape)
                .background(TextFieldBackground)
                .border(2.dp, TextFieldBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Add Photo",
                    tint = TextFieldPlaceholder,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            placeholder = { Text("First Name", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            isError = firstNameError != null,
            supportingText = firstNameError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            placeholder = { Text("Last Name", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            isError = lastNameError != null,
            supportingText = lastNameError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("Username", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Number
        OutlinedTextField(
            value = contactNumber,
            onValueChange = onContactNumberChange,
            placeholder = { Text("Contact Number", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = contactNumberError != null,
            supportingText = contactNumberError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Selection
        Text(
            "Gender",
            fontSize = 15.sp,
            color = TextFieldPlaceholder,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGenderSelect("Male") }
                    .background(
                        color = if (gender == "Male") ButtonBlue else TextFieldBackground,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (gender == "Male") ButtonBlue else TextFieldBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Male",
                    fontSize = 17.sp,
                    color = if (gender == "Male") TextPrimary else TextFieldPlaceholder,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGenderSelect("Female") }
                    .background(
                        color = if (gender == "Female") ButtonBlue else TextFieldBackground,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (gender == "Female") ButtonBlue else TextFieldBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Female",
                    fontSize = 17.sp,
                    color = if (gender == "Female") TextPrimary else TextFieldPlaceholder,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (genderError != null) {
            Text(
                genderError,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date of Birth
        OutlinedTextField(
            value = dateOfBirth?.let { dateFormat.format(it) } ?: "",
            onValueChange = { },
            placeholder = { Text("Date of Birth", color = TextFieldPlaceholder, fontSize = 17.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextFieldPlaceholder
                )
            },
            readOnly = true,
            isError = dobError != null,
            supportingText = dobError?.let { { Text(it, color = Color.Red, fontSize = 14.sp) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR) - 18
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            val selectedCalendar = Calendar
                                .getInstance()
                                .apply {
                                    set(selectedYear, selectedMonth, selectedDay)
                                }
                            onDateOfBirthSelect(selectedCalendar.timeInMillis)
                        },
                        year,
                        month,
                        day
                    )
                        .apply {
                            datePicker.maxDate = Calendar
                                .getInstance()
                                .apply {
                                    add(Calendar.YEAR, -13)
                                }
                                .timeInMillis
                        }
                        .show()
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TextFieldBorder,
                unfocusedBorderColor = TextFieldBorder,
                focusedContainerColor = TextFieldBackground,
                unfocusedContainerColor = TextFieldBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = ButtonBlue,
                disabledBorderColor = TextFieldBorder,
                disabledContainerColor = TextFieldBackground,
                disabledTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(40.dp)
        ) {
            Text(
                "Create Account",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
package com.epilabs.epiguard.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.viewmodels.AuthViewModel
import com.epilabs.epiguard.utils.Validators

// Hardcoded colors from design
private val DarkBackground = Color(0xFF11222E)
private val TextFieldBorder = Color(0xFF2F414F)
private val TextFieldBackground = Color(0xFF11222E)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondaryWhite = Color(0xFFF4E9F6)
private val SuccessGreen = Color(0xFF4CAF50)

// -------------------- PURE UI --------------------
@Composable
fun ForgotPasswordScreenContent(
    email: String,
    emailError: String?,
    showSuccessMessage: Boolean,
    onEmailChange: (String) -> Unit,
    onSendResetClick: () -> Unit,
    onBackToSignInClick: () -> Unit,
    modifier: Modifier = Modifier
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Logo - now matches login screen exactly
            Image(
                painter = painterResource(id = R.drawable.newlogo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 30.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Reset Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your email address and we'll send you a password reset link",
                fontSize = 17.sp,
                color = TextFieldPlaceholder,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (showSuccessMessage) {
                Text(
                    text = "Password reset link sent! Check your email.",
                    fontSize = 17.sp,
                    color = SuccessGreen,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackToSignInClick,
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
                        "Back to Sign In",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = {
                        Text(
                            "Email address",
                            color = TextFieldPlaceholder,
                            fontSize = 17.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = TextFieldPlaceholder
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = emailError?.let {
                        { Text(it, color = Color.Red, fontSize = 14.sp) }
                    },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Send reset button
                Button(
                    onClick = onSendResetClick,
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
                        "Send Reset Link",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back to sign in
                TextButton(onClick = onBackToSignInClick) {
                    Text(
                        "Back to Sign In",
                        color = TextSecondaryWhite,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}

// -------------------- REAL SCREEN --------------------
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Idle -> {
                if (email.isNotEmpty()) {
                    showSuccessMessage = true
                }
            }
            else -> {}
        }
    }

    ForgotPasswordScreenContent(
        email = email,
        emailError = emailError,
        showSuccessMessage = showSuccessMessage,
        onEmailChange = {
            email = it
            emailError = null
        },
        onSendResetClick = {
            emailError = if (!Validators.isValidEmail(email)) "Please enter a valid email" else null
            if (emailError == null) {
                authViewModel.sendPasswordReset(email.trim())
            }
        },
        onBackToSignInClick = { navController.popBackStack() }
    )

    // Loading dialog
    LoadingDialog(
        isVisible = authState is AuthViewModel.AuthState.Loading,
        message = "Sending reset link..."
    )

    // Error dialog
    ErrorDialog(
        isVisible = authState is AuthViewModel.AuthState.Error,
        message = (authState as? AuthViewModel.AuthState.Error)?.message ?: "",
        onDismiss = { authViewModel.clearState() }
    )
}
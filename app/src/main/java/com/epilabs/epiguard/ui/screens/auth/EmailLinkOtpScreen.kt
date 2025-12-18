package com.epilabs.epiguard.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.AuthViewModel

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
fun EmailLinkOtpScreenContent(
    emailSent: Boolean,
    onSendVerificationClick: () -> Unit,
    onContinueClick: () -> Unit,
    onResendClick: () -> Unit,
    onSkipClick: () -> Unit,
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
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Verify Your Email",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (emailSent) {
                Text(
                    text = "Verification email sent! Please check your inbox and click the verification link.",
                    fontSize = 17.sp,
                    color = TextFieldPlaceholder,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "After clicking the link, return here and tap 'Continue'.",
                    fontSize = 17.sp,
                    color = TextFieldPlaceholder,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "We need to verify your email address to secure your account.",
                    fontSize = 17.sp,
                    color = TextFieldPlaceholder,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (emailSent) {
                // Continue button
                Button(
                    onClick = onContinueClick,
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
                        "Continue",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resend button
                TextButton(onClick = onResendClick) {
                    Text(
                        "Resend Verification Email",
                        color = TextSecondaryWhite,
                        fontSize = 17.sp
                    )
                }
            } else {
                // Send verification button
                Button(
                    onClick = onSendVerificationClick,
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
                        "Send Verification Email",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Skip button
            TextButton(onClick = onSkipClick) {
                Text(
                    "Skip for Now",
                    color = TextSecondaryWhite,
                    fontSize = 17.sp
                )
            }
        }
    }
}

// -------------------- REAL SCREEN --------------------
@Composable
fun EmailLinkOtpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var emailSent by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser?.isEmailVerified == true) {
            navController.navigate(Destinations.Dashboard.route) {
                popUpTo(Destinations.EmailLinkOtp.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Idle) {
            emailSent = true
        }
    }

    EmailLinkOtpScreenContent(
        emailSent = emailSent,
        onSendVerificationClick = { authViewModel.sendEmailVerification() },
        onContinueClick = { currentUser?.reload() },
        onResendClick = {
            emailSent = false
            authViewModel.sendEmailVerification()
        },
        onSkipClick = { navController.navigate(Destinations.Dashboard.route) }
    )

    // Loading dialog
    LoadingDialog(
        isVisible = authState is AuthViewModel.AuthState.Loading,
        message = "Sending verification email..."
    )

    // Error dialog
    ErrorDialog(
        isVisible = authState is AuthViewModel.AuthState.Error,
        message = (authState as? AuthViewModel.AuthState.Error)?.message ?: "",
        onDismiss = { authViewModel.clearState() }
    )
}
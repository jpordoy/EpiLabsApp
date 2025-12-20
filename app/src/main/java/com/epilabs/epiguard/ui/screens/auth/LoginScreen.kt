package com.epilabs.epiguard.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.theme.EpiGuardTheme
import com.epilabs.epiguard.ui.viewmodels.AuthViewModel
import com.epilabs.epiguard.utils.Validators

// Exact colors from the design
private val DarkBackground = Color(0xFF11222E)
private val TextFieldBorder = Color(0xFF2F414F)
private val TextFieldBackground = Color(0xFF11222E)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondaryWhite = Color(0xFFF4E9F6)
private val ButtonTextBlue = Color(0xFF0C5AC7)
private val ErrorRed = Color(0xFFFF6B6B)

// ------------------- UI only Composable -------------------

@Composable
fun LoginScreenContent(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
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

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logos),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(74.dp)
                    .padding(bottom = 30.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = {
                    Text(
                        "Mobile number or email address",
                        color = TextFieldPlaceholder,
                        fontSize = 17.sp
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = {
                    Text(
                        "Password",
                        color = TextFieldPlaceholder,
                        fontSize = 17.sp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextFieldPlaceholder
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

            // Forgotten Password link - aligned to right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        "Forgotten Password?",
                        color = TextSecondaryWhite,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign In button
            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = TextPrimary,
                    disabledContainerColor = ButtonBlue.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text(
                    "Log in",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Sign-In Button
            OutlinedButton(
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkBackground,
                    contentColor = TextPrimary,
                    disabledContainerColor = DarkBackground
                ),
                border = BorderStroke(2.dp, TextFieldBorder),
                shape = RoundedCornerShape(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Continue with Google",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    errorMessage,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(150.dp))

            // Create new account button
            OutlinedButton(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkBackground,
                    contentColor = ButtonTextBlue,
                    disabledContainerColor = DarkBackground
                ),
                border = BorderStroke(3.dp, ButtonBlue),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text(
                    "Create new account",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ------------------- ViewModel wrapper -------------------

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authViewModel.handleGoogleSignInResult(result.data)
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate(Destinations.Dashboard.route) {
                    popUpTo(Destinations.Login.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        onEmailChange = {
            email = it
            emailError = null
        },
        onPasswordChange = {
            password = it
            passwordError = null
        },
        passwordVisible = passwordVisible,
        onTogglePassword = { passwordVisible = !passwordVisible },
        onSignInClick = {
            // Validate inputs
            emailError = if (!Validators.isValidEmail(email)) "Please enter a valid email" else null
            passwordError = if (password.isEmpty()) "Password is required" else null

            if (emailError == null && passwordError == null) {
                authViewModel.signIn(email.trim(), password)
            }
        },
        onForgotPasswordClick = { navController.navigate(Destinations.ForgotPassword.route) },
        onSignUpClick = { navController.navigate(Destinations.Signup.route) },
        onGoogleClick = {
            val signInIntent = authViewModel.getGoogleSignInClient(context).signInIntent
            googleSignInLauncher.launch(signInIntent)
        },
        isLoading = authState is AuthViewModel.AuthState.Loading,
        errorMessage = (authState as? AuthViewModel.AuthState.Error)?.message
    )

    // Loading dialog
    LoadingDialog(
        isVisible = authState is AuthViewModel.AuthState.Loading,
        message = "Signing in..."
    )

    // Error dialog
    ErrorDialog(
        isVisible = authState is AuthViewModel.AuthState.Error,
        message = (authState as? AuthViewModel.AuthState.Error)?.message ?: "",
        onDismiss = { authViewModel.clearState() }
    )
}

// ------------------- Preview -------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    EpiGuardTheme {
        LoginScreenContent(
            email = "",
            password = "",
            onEmailChange = {},
            onPasswordChange = {},
            passwordVisible = false,
            onTogglePassword = {},
            onSignInClick = {},
            onForgotPasswordClick = {},
            onSignUpClick = {},
            onGoogleClick = {},
            isLoading = false,
            errorMessage = null
        )
    }
}
package com.epilabs.epiguard.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.epilabs.epiguard.ui.screens.auth.EmailLinkOtpScreen
import com.epilabs.epiguard.ui.screens.auth.ForgotPasswordScreen
import com.epilabs.epiguard.ui.screens.auth.LoginScreen
import com.epilabs.epiguard.ui.screens.auth.SignupScreen
import com.epilabs.epiguard.ui.screens.contacts.ContactsScreen
import com.epilabs.epiguard.ui.screens.dashboard.DashboardScreen
import com.epilabs.epiguard.ui.screens.notifications.NotificationsScreen
import com.epilabs.epiguard.ui.screens.onboarding.OnboardingScreen
import com.epilabs.epiguard.ui.screens.profile.ProfileScreen
import com.epilabs.epiguard.ui.screens.settings.AlertDelayScreen
import com.epilabs.epiguard.ui.screens.settings.ConfidenceThresholdScreen
import com.epilabs.epiguard.ui.screens.settings.HelpSupportScreen
import com.epilabs.epiguard.ui.screens.settings.PrivacyPolicyScreen
import com.epilabs.epiguard.ui.screens.settings.SettingsScreen
import com.epilabs.epiguard.ui.screens.settings.TermsOfServiceScreen
import com.epilabs.epiguard.ui.viewmodels.UserViewModel


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Destinations.Dashboard.route,
    modifier: Modifier = Modifier
) {
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding screen
        composable(Destinations.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // Auth screens
        composable(Destinations.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Destinations.Signup.route) {
            SignupScreen(navController = navController)
        }
        composable(Destinations.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Destinations.EmailLinkOtp.route) {
            EmailLinkOtpScreen(navController = navController)
        }

        // Main app screens
        composable(Destinations.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Destinations.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Destinations.Contacts.route) {
            ContactsScreen(navController = navController)
        }
        composable(Destinations.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(Destinations.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Settings screens
        composable(Destinations.ConfidenceThreshold.route) {
            ConfidenceThresholdScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(Destinations.AlertDelay.route) {
            AlertDelayScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }




        // Settings static pages
        composable(Destinations.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController = navController)
        }
        composable(Destinations.TermsOfService.route) {
            TermsOfServiceScreen(navController = navController)
        }
        composable(Destinations.HelpSupport.route) {
            HelpSupportScreen(navController = navController)
        }
    }
}
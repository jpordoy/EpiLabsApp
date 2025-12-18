package com.epilabs.epiguard.ui.nav

sealed class Destinations(val route: String) {
    // Auth screens
    object Login : Destinations("login")
    object Signup : Destinations("signup")
    object ForgotPassword : Destinations("forgot_password")
    object EmailLinkOtp : Destinations("email_link_otp")

    // Onboarding
    object Onboarding : Destinations("onboarding")

    // Main app screens
    object Dashboard : Destinations("dashboard")
    object Profile : Destinations("profile")
    object ProfileView : Destinations("profile_view")
    object Contacts : Destinations("contacts")
    object SeizureDetection : Destinations("seizure_detection")
    object DeviceSelection : Destinations("device_selection")
    object LocalSeizureDetection : Destinations("local_seizure_detection")
    object EnhancedSeizureDetection : Destinations("enhanced_seizure_detection") // NEW
    object WirelessCameraDetection : Destinations("wireless_camera_detection")

    object Notifications : Destinations("notifications")
    object Settings : Destinations("settings")

    object TestLab : Destinations("testlab") {
        fun createRoute(userId: String) = "testlab"
    }

    object VideoPlayer : Destinations("video_player/{videoId}") {
        fun createRoute(videoId: String) = "video_player/$videoId"
    }
    object TestResults : Destinations("test_results/{resultId}") {
        fun createRoute(resultId: String) = "test_results/$resultId"
    }

    // Settings screens
    object ConfidenceThreshold : Destinations("confidence_threshold")
    object AlertDelay : Destinations("alert_delay")

    // Settings static pages
    object PrivacyPolicy : Destinations("privacy_policy")
    object TermsOfService : Destinations("terms_of_service")
    object HelpSupport : Destinations("help_support")
    object DataPrivacy : Destinations("data_privacy")
}
package com.epilabs.epiguard.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.viewmodels.UserViewModel

@Composable
fun StaticContentScreen(
    navController: NavController,
    title: String,
    content: String,
    userViewModel: UserViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
            )
        }
    }
}

// Privacy Policy Screen
@Composable
fun PrivacyPolicyScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val privacyContent = """
        Privacy Policy
        
        Last updated: [Date]
        
        1. Information We Collect
        We collect information you provide directly to us, such as when you create an account, update your profile, or contact us for support.
        
        2. How We Use Your Information
        We use the information we collect to provide, maintain, and improve our services, including seizure detection and emergency notifications.
        
        3. Information Sharing
        We do not sell, trade, or rent your personal information to third parties. We may share information with emergency contacts when a seizure is detected.
        
        4. Data Security
        We implement appropriate security measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction.
        
        5. Your Rights
        You have the right to access, update, or delete your personal information. You can do this through your account settings or by contacting us.
        
        6. Contact Us
        If you have any questions about this Privacy Policy, please contact us at privacy@epiguard.com
        
        [Add your complete privacy policy content here]
    """.trimIndent()

    StaticContentScreen(
        navController = navController,
        title = "Privacy Policy",
        content = privacyContent,
        userViewModel = userViewModel
    )
}

// Terms of Service Screen
@Composable
fun TermsOfServiceScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val termsContent = """
        Terms of Service
        
        Last updated: [Date]
        
        1. Acceptance of Terms
        By using EpiGuard, you agree to be bound by these Terms of Service.
        
        2. Medical Disclaimer
        EpiGuard is not a medical device and should not be used as a substitute for professional medical advice, diagnosis, or treatment.
        
        3. Service Description
        EpiGuard provides seizure detection capabilities using device sensors and machine learning algorithms.
        
        4. User Responsibilities
        You are responsible for maintaining the accuracy of your profile information and emergency contacts.
        
        5. Privacy and Data Protection
        Your use of our service is also governed by our Privacy Policy.
        
        6. Limitation of Liability
        EpiGuard is provided "as is" without warranties. We are not liable for any damages resulting from the use of our service.
        
        7. Termination
        We may terminate or suspend your account at any time for violations of these terms.
        
        8. Changes to Terms
        We reserve the right to modify these terms at any time. Continued use constitutes acceptance of changes.
        
        [Add your complete terms of service content here]
    """.trimIndent()

    StaticContentScreen(
        navController = navController,
        title = "Terms of Service",
        content = termsContent,
        userViewModel = userViewModel
    )
}

// Help & Support Screen
@Composable
fun HelpSupportScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val helpContent = """
        Help & Support
        
        Welcome to EpiGuard Help Center
        
        Getting Started
        1. Create your profile with accurate information
        2. Add emergency contacts who will be notified during seizures
        3. Enable seizure detection from the Detection screen
        4. Keep your device charged and nearby
        
        Seizure Detection
        • EpiGuard uses your device's sensors to detect potential seizures
        • The AI model analyzes movement patterns in real-time
        • When a seizure is detected, emergency contacts are automatically notified
        
        Emergency Contacts
        • Add trusted contacts who can respond to emergencies
        • Contacts receive SMS and email alerts during detected seizures
        • Link contacts to other EpiGuard users for enhanced features
        
        Settings
        • Customize detection sensitivity and alert preferences
        • Manage notification settings
        • Export your detection history for medical appointments
        
        Troubleshooting
        • Ensure location permissions are enabled for emergency services
        • Keep the app running in the background
        • Check that Do Not Disturb settings allow EpiGuard notifications
        
        Contact Support
        Email: support@epiguard.com
        Phone: 1-800-EPI-GUARD
        
        Emergency: If you're experiencing a medical emergency, call 911 immediately.
        
        [Add more detailed help content here]
    """.trimIndent()

    StaticContentScreen(
        navController = navController,
        title = "Help & Support",
        content = helpContent,
        userViewModel = userViewModel
    )
}
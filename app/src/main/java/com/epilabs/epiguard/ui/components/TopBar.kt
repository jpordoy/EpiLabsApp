package com.epilabs.epiguard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.NotificationViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel

// hardcoded colors (same as elsewhere in your app)
private val DarkBackground = Color(0xFF11222E)
private val TextFieldPlaceholder = Color(0xFF606E77)

@Composable
fun TopBar(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val user by userViewModel.currentUser.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .statusBarsPadding()              // ✅ prevents overlap with phone status bar
            .padding(all = 20.dp),            // ✅ requested spacing
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🔹 LEFT — Company logo
        Image(
            painter = painterResource(id = R.drawable.newlogo), // your PNG logo
            contentDescription = "Company Logo",
            modifier = Modifier.height(50.dp)

        )

        Spacer(modifier = Modifier.weight(1f))

        // 🔹 RIGHT — Bell + Profile (inline)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Notification bell
            Box {
                Image(
                    painter = painterResource(id = R.drawable.topbar_notification_bell),
                    contentDescription = "Notifications",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.navigate(Destinations.Notifications.route)
                        },
                )

                if (unreadCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(Color(0xffd42a2a), CircleShape)
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 8.sp,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
            }

            // Profile image
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate(Destinations.Profile.route)
                    }
            ) {
                if (user?.profileImageUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user!!.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                BorderStroke(1.33.dp, TextFieldPlaceholder),
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                BorderStroke(1.33.dp, TextFieldPlaceholder),
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

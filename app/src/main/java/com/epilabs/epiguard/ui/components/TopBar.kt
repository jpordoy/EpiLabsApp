package com.epilabs.epiguard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

@Composable
fun TopBar(
    navController: NavController,
    showBackButton: Boolean = true,
    userViewModel: UserViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val user by userViewModel.currentUser.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // For dashboard (when showBackButton is false), use the new design
    if (!showBackButton) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.panel_logo),
                        contentDescription = "EPIGUARD Logo",
                        modifier = Modifier
                            .requiredWidth(width = 137.dp)
                            .requiredHeight(height = 47.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hi ${user?.firstName ?: "User"} 👋,",
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    if (unreadCount > 0) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                ) { append("You have ") }
                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xffd42a2a),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                ) { append("$unreadCount unread notification${if (unreadCount != 1) "s" else ""}") }
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                ) { append(" today") }
                            }
                        )
                    }
                }

                // Right side: Notification bell and Profile image
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Notification bell with badge
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.topbar_notification_bell),
                            contentDescription = "Notifications",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { navController.navigate(Destinations.Notifications.route) }
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
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Profile picture
                    Box(
                        modifier = Modifier
                            .requiredSize(size = 40.dp)
                            .clickable { navController.navigate(Destinations.Profile.route) }
                    ) {
                        if (user?.profileImageUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user!!.profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .clip(shape = CircleShape)
                                    .border(
                                        border = BorderStroke(1.33.dp, Color.White),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.profile_image),
                                contentDescription = "user-image",
                                modifier = Modifier
                                    .clip(shape = CircleShape)
                                    .border(
                                        border = BorderStroke(1.33.dp, Color.White),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    } else {
        // For other screens, use the simple back button design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Right side icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification bell with badge
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge {
                                Text(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { navController.navigate(Destinations.Notifications.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Profile picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate(Destinations.Profile.route) }
                ) {
                    if (user?.profileImageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user!!.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_default_icon),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
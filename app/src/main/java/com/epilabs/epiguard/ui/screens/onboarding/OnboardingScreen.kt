package com.epilabs.epiguard.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    var showLoading by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(pageCount = { 3 })

    // Show loading screen first
    LaunchedEffect(Unit) {
        delay(2000) // Show loading for 2 seconds
        showLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = showLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            LoadingPage()
        }

        AnimatedVisibility(
            visible = !showLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            OnboardingPager(
                pagerState = pagerState,
                onSkip = {
                    viewModel.completeOnboarding()
                    navController.navigate(Destinations.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onComplete = {
                    viewModel.completeOnboarding()
                    navController.navigate(Destinations.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo image
        Image(
            painter = painterResource(id = R.drawable.epiguard),
            contentDescription = "EpiGuard Logo",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Loading text",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun OnboardingPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar with skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(48.dp)) // Balance the skip button

            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(pageIndex = page)
        }

        // Bottom section with dots and buttons
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            // Go to previous page
                        },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(100.dp))
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            // Go to next page
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(
                        if (pagerState.currentPage < 2) "Next" else "Get Started"
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(pageIndex: Int) {
    val (imageText, titleText, descriptionText) = when (pageIndex) {
        0 -> Triple("IMAGE1", "text1", "Description for feature 1")
        1 -> Triple("IMAGE2", "text2", "Description for feature 2")
        2 -> Triple("IMAGE3", "text3", "Description for feature 3")
        else -> Triple("IMAGE", "Default", "Default description")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Onboarding image
        Image(
            painter = painterResource(id = when(pageIndex) {
                0 -> R.drawable.image1
                1 -> R.drawable.image2
                2 -> R.drawable.image3
                else -> R.drawable.image1
            }),
            contentDescription = "Feature illustration",
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = descriptionText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}


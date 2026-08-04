package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // Pulse animation for divine image border
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(2000L) // Wait for exactly 2 seconds
        onSplashComplete()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            CreamBackground,
            SurfaceCream,
            SoftGold
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Respectful Top Divine Title
            Text(
                text = "|| શ્રી અર્બુદા માતાજી પ્રસન્ન ||",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RoyalMaroon,
                textAlign = TextAlign.Center
            )

            Text(
                text = "|| Shree Arbuda Mataji Prasann ||",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Photo Card of Arbuda Maa
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(0.85f)
                    .scale(scale)
                    .testTag("arbuda_maa_full_photo_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = androidx.compose.foundation.BorderStroke(2.5.dp, RoyalGold)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = R.drawable.img_maa_arbuda_1785298366627,
                        contentDescription = "Shree Arbuda Mataji",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Community Subtitle
            Text(
                text = "ચૌધરી સમાજ વિવાહ મિલન પોર્ટલ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalMaroon,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Chaudhary Samaj Vivah Milan Portal",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            CircularProgressIndicator(
                color = RoyalMaroon,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

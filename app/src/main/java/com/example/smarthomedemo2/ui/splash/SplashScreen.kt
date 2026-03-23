package com.example.smarthomedemo2.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101417)), // Dark background
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(Unit) {
            delay(2000) // 2 seconds delay
            onTimeout()
        }
        
        Text(
            text = "Greetings",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                fontSize = 48.sp,
                color = Color.White,
                letterSpacing = (-1.5).sp
            )
        )
    }
}

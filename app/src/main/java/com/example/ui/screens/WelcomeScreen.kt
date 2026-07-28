package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.OnboardingDialog

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    var showOnboarding by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.migration_hero_image_1785102008558),
            contentDescription = "Migration Hero Image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "עוזר מעבר טלפון",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "סריקה חכמה לפני מעבר למכשיר חדש. אנו מזהים אוטומטית אפליקציות מותקנות, מדיה ואנשי קשר כדי ליצור רשימת מטלות למעבר חלק ומושלם.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { showOnboarding = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("התחל סריקה", style = MaterialTheme.typography.titleMedium)
        }
    }

    OnboardingDialog(
        showDialog = showOnboarding,
        onDismiss = { showOnboarding = false },
        onPermissionsGranted = {
            showOnboarding = false
            onContinue()
        }
    )
}


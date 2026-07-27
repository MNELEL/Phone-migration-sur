package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.components.CircularProgressBar
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(onComplete: () -> Unit, viewModel: ScanViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }
    
    LaunchedEffect(state.progress) {
        if (state.progress == 100) {
            delay(1200) // Allow smooth progress completion animation
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Analyzing Device",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = state.stage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(56.dp))
        
        CircularProgressBar(
            progress = state.progress / 100f,
            size = 200.dp,
            strokeWidth = 14.dp,
            modifier = Modifier.padding(16.dp)
        )
        
        if (state.error != null) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Error: ${state.error}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.startScan() },
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Retry Scan")
            }
        }
    }
}


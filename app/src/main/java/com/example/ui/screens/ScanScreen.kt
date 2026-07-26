package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ScanViewModel
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(onComplete: () -> Unit, viewModel: ScanViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }
    
    LaunchedEffect(state.progress) {
        if (state.progress == 100) {
            delay(500)
            onComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scanning...", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(state.stage)
        Spacer(Modifier.height(24.dp))
        if (state.running) {
            CircularProgressIndicator()
        }
        if (state.error != null) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.startScan() }) {
                Text("Retry")
            }
        }
    }
}

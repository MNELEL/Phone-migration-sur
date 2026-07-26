package com.example.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*

@Composable
fun MigrationNavigation(scanViewModel: ScanViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onContinue = { navController.navigate("permissions") })
        }
        composable("permissions") {
            PermissionScreen(onContinue = { navController.navigate("scan") })
        }
        composable("scan") {
            ScanScreen(onComplete = { navController.navigate("checklist") }, viewModel = scanViewModel)
        }
        composable("checklist") {
            ChecklistScreen(onBack = { navController.popBackStack() }, viewModel = scanViewModel)
        }
    }
}

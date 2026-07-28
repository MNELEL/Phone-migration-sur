package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.service.MigrationOrchestrator
import com.example.ui.screens.*

@Composable
fun MigrationNavigation(scanViewModel: ScanViewModel = viewModel()) {
    val navController = rememberNavController()
    val orchestrator = remember { MigrationOrchestrator() }

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onContinue = { navController.navigate("scan") })
        }
        composable("permissions") {
            PermissionScreen(onContinue = { navController.navigate("scan") })
        }
        composable("scan") {
            ScanScreen(onComplete = { navController.navigate("checklist") }, viewModel = scanViewModel)
        }
        composable("checklist") {
            ChecklistScreen(
                viewModel = scanViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToOrchestrator = { navController.navigate("orchestrator") },
                onNavigateToCloudDestination = { navController.navigate("cloud_destination") },
                onNavigateToQrWizard = { navController.navigate("qr_wizard") },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToDeviceAdmin = { navController.navigate("device_admin_cloning") },
                onNavigateToLivePreview = { navController.navigate("live_preview") }
            )
        }
        composable("dashboard") {
            StorageDashboardScreen(
                viewModel = scanViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToOrchestrator = { navController.navigate("orchestrator") }
            )
        }
        composable("cloud_destination") {
            CloudDestinationScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("orchestrator") }
            )
        }
        composable("qr_wizard") {
            QrWizardScreen(
                onBack = { navController.popBackStack() },
                onPairingComplete = { navController.navigate("orchestrator") }
            )
        }
        composable("report") {
            MigrationReportScreen(
                viewModel = scanViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("orchestrator") {
            OrchestratorScreen(
                orchestrator = orchestrator,
                onBack = { navController.popBackStack() },
                onNavigateToQrWizard = { navController.navigate("qr_wizard") },
                onNavigateToReport = { navController.navigate("report") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDeviceAdmin = { navController.navigate("device_admin_cloning") },
                onNavigateToLivePreview = { navController.navigate("live_preview") }
            )
        }
        composable("device_admin_cloning") {
            DeviceAdminCloningScreen(
                onBack = { navController.popBackStack() },
                onNavigateToReport = { navController.navigate("report") }
            )
        }
        composable("live_preview") {
            LivePreviewScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}


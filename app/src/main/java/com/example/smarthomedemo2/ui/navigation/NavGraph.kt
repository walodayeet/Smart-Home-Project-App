package com.example.smarthomedemo2.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarthomedemo2.data.FaceRecognitionRepository
import com.example.smarthomedemo2.data.LogRepository
import com.example.smarthomedemo2.data.UserPreferencesRepository
import com.example.smarthomedemo2.ui.camera.CameraScreen
import com.example.smarthomedemo2.ui.camera.CameraViewModel
import com.example.smarthomedemo2.ui.components.BottomNavigationBar
import com.example.smarthomedemo2.ui.dashboard.DashboardScreen
import com.example.smarthomedemo2.ui.dashboard.DashboardViewModel
import com.example.smarthomedemo2.ui.voice.VoiceScreen
import com.example.smarthomedemo2.ui.voice.VoiceViewModel
import com.example.smarthomedemo2.ui.settings.SettingsScreen
import com.example.smarthomedemo2.ui.settings.SettingsViewModel
import com.example.smarthomedemo2.ui.log.LogScreen
import com.example.smarthomedemo2.ui.log.LogViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun SmartHomeNavGraph(
    navController: NavHostController,
    repository: UserPreferencesRepository,
    logRepository: LogRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(
                        context.applicationContext as Application,
                        repository,
                        logRepository
                    )
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) }
                )
            }
            composable(Screen.Camera.route) {
                val cameraViewModel: CameraViewModel = viewModel(
                    factory = CameraViewModel.Factory(
                        repository = repository,
                        logRepository = logRepository,
                        faceRecognitionRepository = FaceRecognitionRepository(),
                    )
                )
                CameraScreen(
                    viewModel = cameraViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Voice.route) {
                val voiceViewModel: VoiceViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return VoiceViewModel(repository) as T
                        }
                    }
                )
                VoiceScreen(viewModel = voiceViewModel)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(repository)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToLog = { navController.navigate(Screen.ActivityLog.route) }
                )
            }
            composable(Screen.ActivityLog.route) {
                val logViewModel: LogViewModel = viewModel(
                    factory = LogViewModel.Factory(logRepository)
                )
                LogScreen(
                    viewModel = logViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}

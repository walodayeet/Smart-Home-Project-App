package com.example.smarthomedemo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smarthomedemo2.data.AppDatabase
import com.example.smarthomedemo2.data.LogRepository
import com.example.smarthomedemo2.data.UserPreferencesRepository
import com.example.smarthomedemo2.ui.navigation.SmartHomeNavGraph
import com.example.smarthomedemo2.ui.splash.SplashScreen
import com.example.smarthomedemo2.ui.theme.SmartHomeDemo2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = UserPreferencesRepository(this)
        val database = AppDatabase.getDatabase(this)
        val logRepository = LogRepository(database.logDao())
        
        enableEdgeToEdge()
        setContent {
            val userPreferences by repository.userPreferencesFlow.collectAsState(initial = null)
            val darkTheme = when (userPreferences?.isDarkTheme) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            SmartHomeDemo2Theme(darkTheme = darkTheme) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    val navController = rememberNavController()
                    SmartHomeNavGraph(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        repository = repository,
                        logRepository = logRepository
                    )
                }
            }
        }
    }
}

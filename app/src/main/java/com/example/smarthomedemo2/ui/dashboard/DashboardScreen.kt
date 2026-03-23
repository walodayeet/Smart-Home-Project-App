package com.example.smarthomedemo2.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarthomedemo2.ui.theme.SmartHomeDemo2Theme
import kotlinx.coroutines.launch
import com.example.smarthomedemo2.ui.navigation.Screen

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        DashboardContent(
            lightStatus = uiState.lightStatus,
            lockStatus = uiState.lockStatus,
            curtainStatus = uiState.curtainStatus,
            isAlarmArmed = uiState.isAlarmArmed,
            isAlarmTriggered = uiState.isAlarmTriggered,
            onToggleLights = { viewModel.toggleLights() },
            onToggleLock = { viewModel.toggleLock() },
            onToggleCurtains = { viewModel.toggleCurtains() },
            onToggleAlarm = { 
                viewModel.toggleAlarm()
                val message = if (!uiState.isAlarmArmed) "Alarm ARMED, House Secured" else "Alarm UNARMED"
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message)
                }
            },
            onTriggerManualAlarm = {
                viewModel.triggerManualAlarm()
                val message = if (!uiState.isAlarmTriggered) "MANUAL ALARM TRIGGERED" else "ALARM DEACTIVATED"
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message)
                }
            },
            onNavigateToCamera = onNavigateToCamera,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun DashboardContent(
    lightStatus: Boolean,
    lockStatus: Boolean,
    curtainStatus: Boolean,
    isAlarmArmed: Boolean,
    isAlarmTriggered: Boolean,
    onToggleLights: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleCurtains: () -> Unit,
    onToggleAlarm: () -> Unit,
    onTriggerManualAlarm: () -> Unit,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarm")
    
    // Persistent Alarm Pulse (Triggered)
    val alarmAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarmAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Flashing Red Background for Alarm (TRIGGERED)
        if (isAlarmTriggered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = alarmAlpha))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AULA HOMES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.BatteryChargingFull,
                    contentDescription = null,
                    tint = Color(0xFFB4EC51),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "98% ONLINE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB4EC51),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                val glowColor by animateColorAsState(
                    targetValue = when {
                        isAlarmTriggered -> Color.Red.copy(alpha = 0.4f)
                        isAlarmArmed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else -> Color.Transparent
                    },
                    label = "glowColor"
                )

                val iconColor by animateColorAsState(
                    targetValue = when {
                        isAlarmTriggered -> Color.Red
                        isAlarmArmed -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    },
                    label = "iconColor"
                )

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(glowColor, Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = iconColor
                )

                // Armed Status Badge
                if (isAlarmArmed && !isAlarmTriggered) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-20).dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(icon = Icons.Rounded.Lock, isActive = lockStatus, onClick = onToggleLock)
                QuickActionButton(icon = Icons.Rounded.Security, isActive = isAlarmArmed, onClick = onToggleAlarm)
                QuickActionButton(icon = Icons.Rounded.NotificationsActive, isActive = isAlarmTriggered, onClick = onTriggerManualAlarm)
                QuickActionButton(icon = Icons.Rounded.Lightbulb, isActive = lightStatus, onClick = onToggleLights)
                QuickActionButton(icon = Icons.Rounded.Curtains, isActive = curtainStatus, onClick = onToggleCurtains)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Device Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    DeviceControlCard(
                        title = "Main Entrance",
                        statusText = if (lockStatus) "Locked" else "Unlocked",
                        actionText = if (lockStatus) "Unlock" else "Lock",
                        icon = if (lockStatus) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        isActive = lockStatus,
                        onClick = onToggleLock
                    )
                }
                item {
                    DeviceControlCard(
                        title = "Living Room",
                        statusText = if (lightStatus) "Lights On" else "Lights Off",
                        actionText = if (lightStatus) "Turn Off" else "Turn On",
                        icon = Icons.Rounded.Lightbulb,
                        isActive = lightStatus,
                        onClick = onToggleLights
                    )
                }
                item {
                    DeviceControlCard(
                        title = "Windows",
                        statusText = if (curtainStatus) "Curtains Open" else "Curtains Closed",
                        actionText = if (curtainStatus) "Close" else "Open",
                        icon = if (curtainStatus) Icons.Rounded.Curtains else Icons.Rounded.CurtainsClosed,
                        isActive = curtainStatus,
                        onClick = onToggleCurtains
                    )
                }
                item {
                    DeviceControlCard(
                        title = "Security Cam",
                        statusText = "Live Feed",
                        actionText = "View",
                        icon = Icons.Rounded.Videocam,
                        isActive = true,
                        onClick = onNavigateToCamera
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = modifier.size(28.dp)
        )
    }
}

@Composable
fun DeviceControlCard(
    title: String,
    statusText: String,
    actionText: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun DashboardPreview() {
    SmartHomeDemo2Theme(darkTheme = true) {
        DashboardContent(
            lightStatus = true,
            lockStatus = true,
            curtainStatus = false,
            isAlarmArmed = false,
            isAlarmTriggered = false,
            onToggleLights = {},
            onToggleLock = {},
            onToggleCurtains = {},
            onToggleAlarm = {},
            onTriggerManualAlarm = {},
            onNavigateToCamera = {}
        )
    }
}

package com.example.smarthomedemo2.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        SettingsContent(
            doorLockTimer = uiState.doorLockTimer,
            automaticLocking = uiState.automaticLocking,
            isDarkTheme = uiState.isDarkTheme,
            isOwnerAuthenticated = uiState.isOwnerAuthenticated,
            onTimerChange = {
                if (!uiState.isOwnerAuthenticated) {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Verify identity before changing settings")
                    }
                } else {
                    viewModel.updateDoorLockTimer(it)
                }
            },
            onToggleAutoLock = {
                if (!uiState.isOwnerAuthenticated) {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Verify identity before changing settings")
                    }
                } else {
                    viewModel.toggleAutomaticLocking(it)
                }
            },
            onThemeChange = {
                if (!uiState.isOwnerAuthenticated) {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Verify identity before changing settings")
                    }
                } else {
                    viewModel.updateDarkTheme(it)
                }
            },
            onNavigateToLog = {
                if (!uiState.isOwnerAuthenticated) {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Verify identity before opening settings pages")
                    }
                } else {
                    onNavigateToLog()
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }

}

@Composable
fun SettingsContent(
    doorLockTimer: Int,
    automaticLocking: Boolean,
    isDarkTheme: Boolean?,
    isOwnerAuthenticated: Boolean,
    onTimerChange: (Int) -> Unit,
    onToggleAutoLock: (Boolean) -> Unit,
    onThemeChange: (Boolean?) -> Unit,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        AssistChip(
            onClick = { },
            enabled = false,
            label = {
                Text(if (isOwnerAuthenticated) "Owner Verified" else "Verify identity to change settings")
            },
            leadingIcon = {
                Icon(
                    imageVector = if (isOwnerAuthenticated) Icons.Rounded.VerifiedUser else Icons.Rounded.Lock,
                    contentDescription = null,
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "Appearance") {
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.titleMedium,
                color = if (isOwnerAuthenticated) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            
            val options = listOf("System", "Light", "Dark")
            val selectedOption = when (isDarkTheme) {
                null -> "System"
                false -> "Light"
                true -> "Dark"
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = {
                            onThemeChange(
                                when (label) {
                                    "Light" -> false
                                    "Dark" -> true
                                    else -> null
                                }
                            )
                        },
                        selected = label == selectedOption,
                        enabled = isOwnerAuthenticated
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "Security & Locking") {
            SettingsToggleItem(
                title = "Automatic Locking",
                description = "Automatically lock the main door after a delay.",
                icon = Icons.Rounded.Security,
                isSelected = automaticLocking,
                enabled = isOwnerAuthenticated,
                onCheckedChange = onToggleAutoLock
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            SettingsSliderItem(
                title = "Locking Timer",
                value = doorLockTimer.toFloat(),
                onValueChange = { onTimerChange(it.roundToInt()) },
                valueRange = 5f..120f,
                steps = 23,
                unit = "sec",
                icon = Icons.Rounded.LockClock,
                enabled = automaticLocking && isOwnerAuthenticated
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "General") {
            SettingsNavigationItem(
                title = "Activity Log",
                description = "View recent entry and exit events.",
                icon = Icons.Rounded.History,
                enabled = isOwnerAuthenticated,
                onClick = onNavigateToLog
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SettingsNavigationItem(
                title = "Device Management",
                description = "Add or remove smart devices.",
                icon = Icons.Rounded.Settings,
                enabled = isOwnerAuthenticated,
                onClick = {} // Future implementation
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
        Switch(
            checked = isSelected,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    unit: String,
    icon: ImageVector,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${value.roundToInt()} $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsNavigationItem(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

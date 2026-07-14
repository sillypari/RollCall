package com.simpleattendance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simpleattendance.BuildConfig
import com.simpleattendance.ui.components.InfoSettingRow
import com.simpleattendance.ui.components.RollCallSurface
import com.simpleattendance.ui.components.SegmentedSettingRow
import com.simpleattendance.ui.components.SwitchSettingRow
import com.simpleattendance.ui.theme.RollCallSpacing
import com.simpleattendance.ui.theme.SurfaceContainer
import com.simpleattendance.ui.settings.SettingsViewModel

/**
 * Compose Settings screen — Phase C.
 * Preserves all existing DataStore keys and setting semantics.
 * Settings are grouped into tonal section cards, not individual outlined cards.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = RollCallSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(RollCallSpacing.lg)
    ) {
        Spacer(Modifier.height(RollCallSpacing.sm))

        // ── Interaction ─────────────────────────────────────────────────────
        SettingsSectionCard(title = "Interaction") {
            SwitchSettingRow(
                title = "Haptic Feedback",
                description = "Vibration on mark actions and saves",
                checked = settings.hapticsEnabled,
                onCheckedChange = { viewModel.setHapticsEnabled(it) }
            )
            SettingsDivider()
            SegmentedSettingRow(
                title = "Attendance Input Mode",
                description = "How to mark students during attendance",
                options = listOf(
                    "Buttons" to "buttons",
                    "Swipe" to "swipe",
                    "Both" to "both"
                ),
                selectedValue = settings.attendanceMode,
                onValueSelected = { viewModel.setAttendanceMode(it) }
            )
        }

        // ── Reports ─────────────────────────────────────────────────────────
        SettingsSectionCard(title = "Reports") {
            SegmentedSettingRow(
                title = "Report Template",
                description = "Which students appear in generated reports",
                options = listOf(
                    "Both" to "both",
                    "Absent" to "absent_only",
                    "Present" to "present_only"
                ),
                selectedValue = settings.reportTemplate,
                onValueSelected = { viewModel.setReportTemplate(it) }
            )
            SettingsDivider()
            SegmentedSettingRow(
                title = "Numbering Mode",
                description = "Student numbering in reports",
                options = listOf(
                    "Relative" to "relative",
                    "Absolute" to "absolute"
                ),
                selectedValue = settings.numberingMode,
                onValueSelected = { viewModel.setNumberingMode(it) }
            )
        }

        // ── About ────────────────────────────────────────────────────────────
        SettingsSectionCard(title = "About") {
            InfoSettingRow(title = "Version", value = BuildConfig.VERSION_NAME)
            SettingsDivider()
            InfoSettingRow(title = "Developer", value = "Parikshit Singh Bais")
            SettingsDivider()
            InfoSettingRow(title = "Data", value = "Offline only · No ads · No tracking")
        }

        Spacer(Modifier.height(RollCallSpacing.epic))
    }
}

/** Tonal section card that groups related settings. */
@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                horizontal = RollCallSpacing.xs,
                vertical = RollCallSpacing.xs
            )
        )
        RollCallSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = SurfaceContainer
        ) {
            Column { content() }
        }
    }
}

/** Subtle horizontal divider between setting rows inside a card. */
@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = RollCallSpacing.lg),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

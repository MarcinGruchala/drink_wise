package com.mgruchala.drinkwise.presentation.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mgruchala.drinkwise.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val limitUpdatedSuccessfullyMessage =
        stringResource(id = R.string.settings_limit_updated_successfully)

    LaunchedEffect(state.showSuccessMessage) {
        if (state.showSuccessMessage) {
            snackbarHostState.showSnackbar(message = limitUpdatedSuccessfullyMessage)
            viewModel.dismissSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_set_alcohol_limits_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = stringResource(R.string.settings_limits_description),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LimitSettingCard(
                title = stringResource(R.string.settings_daily_limit_title),
                description = stringResource(R.string.settings_daily_limit_description),
                currentValue = state.dailyLimit,
                onApply = { viewModel.updateDailyLimit(it) },
                isSaving = LimitType.DAILY in state.savingLimits
            )

            LimitSettingCard(
                title = stringResource(R.string.settings_weekly_limit_title),
                description = stringResource(R.string.settings_weekly_limit_description),
                currentValue = state.weeklyLimit,
                onApply = { viewModel.updateWeeklyLimit(it) },
                isSaving = LimitType.WEEKLY in state.savingLimits
            )

            LimitSettingCard(
                title = stringResource(R.string.settings_monthly_limit_title),
                description = stringResource(R.string.settings_monthly_limit_description),
                currentValue = state.monthlyLimit,
                onApply = { viewModel.updateMonthlyLimit(it) },
                isSaving = LimitType.MONTHLY in state.savingLimits
            )
        }
    }
}

@Composable
fun LimitSettingCard(
    title: String,
    description: String,
    currentValue: Float,
    onApply: (Float) -> Unit,
    isSaving: Boolean
) {
    var value by remember(currentValue) { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        isError = it.toFloatOrNull() == null || it.toFloatOrNull()!! <= 0
                    },
                    label = {
                        Text(
                            stringResource(
                                R.string.settings_current_limit_label,
                                currentValue
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(stringResource(R.string.settings_error_invalid_positive_number))
                        }
                    }
                )

                Button(
                    onClick = {
                        value.toFloatOrNull()?.let {
                            if (it > 0) {
                                focusManager.clearFocus()
                                onApply(it)
                            }
                        }
                    },
                    enabled = !isSaving && !isError && value.toFloatOrNull() != null
                ) {
                    Text(stringResource(R.string.settings_apply_button))
                }
            }
        }
    }
}

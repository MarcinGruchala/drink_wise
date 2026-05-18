package com.mgruchala.drinkwise.presentation.daydetails.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorDraft
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

enum class DrinkEditorSheetMode {
    Add,
    Edit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkEditorSheet(
    mode: DrinkEditorSheetMode,
    selectedDate: LocalDate,
    draft: DrinkEditorDraft,
    isDeleteConfirming: Boolean,
    onDraftChange: (DrinkEditorDraft) -> Unit,
    onSave: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        DrinkEditorSheetContent(
            mode = mode,
            selectedDate = selectedDate,
            draft = draft,
            isDeleteConfirming = isDeleteConfirming,
            onDraftChange = onDraftChange,
            onSave = onSave,
            onDeleteClick = onDeleteClick,
            onCancelDelete = onCancelDelete,
            onConfirmDelete = onConfirmDelete,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun DrinkEditorSheetContent(
    mode: DrinkEditorSheetMode,
    selectedDate: LocalDate,
    draft: DrinkEditorDraft,
    isDeleteConfirming: Boolean,
    onDraftChange: (DrinkEditorDraft) -> Unit,
    onSave: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val canSave = when (mode) {
        DrinkEditorSheetMode.Add -> draft.isValidForAdd
        DrinkEditorSheetMode.Edit -> draft.isValidForEdit
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(
                id = when (mode) {
                    DrinkEditorSheetMode.Add -> R.string.day_details_add_drink
                    DrinkEditorSheetMode.Edit -> R.string.day_details_edit_drink
                }
            ),
            style = MaterialTheme.typography.titleLarge
        )

        TimePickerField(
            time = draft.time,
            onTimeChange = { onDraftChange(draft.copy(time = it)) }
        )

        Text(
            text = stringResource(
                id = R.string.day_details_selected_date,
                selectedDate.format(dateFormatter)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DrinkEditorTextFields(
            draft = draft,
            onDraftChange = onDraftChange
        )

        if (mode == DrinkEditorSheetMode.Add) {
            NumberOfDrinksStepper(
                draft = draft,
                onDraftChange = onDraftChange
            )
        }

        if (mode == DrinkEditorSheetMode.Edit && isDeleteConfirming) {
            DeleteConfirmation(
                onCancelDelete = onCancelDelete,
                onConfirmDelete = onConfirmDelete
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mode == DrinkEditorSheetMode.Edit && !isDeleteConfirming) {
                TextButton(onClick = onDeleteClick) {
                    Text(stringResource(R.string.day_details_delete_drink))
                }
            } else {
                Spacer(modifier = Modifier)
            }

            Button(
                onClick = onSave,
                enabled = canSave
            ) {
                Text(stringResource(R.string.day_details_save_drink))
            }
        }
    }
}

@Composable
private fun DrinkEditorTextFields(
    draft: DrinkEditorDraft,
    onDraftChange: (DrinkEditorDraft) -> Unit
) {
    val quantityError = draft.quantityMlText.isNotBlank() && draft.quantityMl == null
    val abvError = draft.abvText.isNotBlank() && draft.abv == null

    OutlinedTextField(
        value = draft.quantityMlText,
        onValueChange = { onDraftChange(draft.copy(quantityMlText = it)) },
        label = { Text(stringResource(R.string.day_details_quantity_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = quantityError,
        supportingText = {
            if (quantityError) {
                Text(stringResource(R.string.day_details_invalid_quantity))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = draft.abvText,
        onValueChange = { onDraftChange(draft.copy(abvText = it.replace(',', '.'))) },
        label = { Text(stringResource(R.string.day_details_abv_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = abvError,
        supportingText = {
            if (abvError) {
                Text(stringResource(R.string.day_details_invalid_abv))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberOfDrinksStepper(
    draft: DrinkEditorDraft,
    onDraftChange: (DrinkEditorDraft) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                id = R.string.day_details_number_of_drinks,
                draft.numberOfDrinks ?: 1
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onDraftChange(draft.decrementCount()) }) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = stringResource(R.string.day_details_decrease_number_content_description)
            )
        }
        IconButton(onClick = { onDraftChange(draft.incrementCount()) }) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.day_details_increase_number_content_description)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    var showPicker by remember { mutableStateOf(false) }
    val formattedTime = time.format(formatter)
    val changeTimeDescription = stringResource(R.string.day_details_change_time)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.day_details_time_label),
            style = MaterialTheme.typography.labelLarge
        )
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$changeTimeDescription, $formattedTime" }
        ) {
            Text(formattedTime)
        }
    }

    if (showPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                        showPicker = false
                    }
                ) {
                    Text(stringResource(R.string.day_details_time_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.day_details_time_picker_cancel))
                }
            },
            text = {
                TimePicker(state = pickerState)
            }
        )
    }
}

@Composable
private fun DeleteConfirmation(
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.day_details_delete_confirmation_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.day_details_delete_confirmation_message),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancelDelete) {
                    Text(stringResource(R.string.day_details_cancel_delete))
                }
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.day_details_confirm_delete))
                }
            }
        }
    }
}

@Preview(
    name = "Add",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkEditorSheetAddPreview() {
    DrinkWiseTheme {
        DrinkEditorSheetContent(
            mode = DrinkEditorSheetMode.Add,
            selectedDate = LocalDate.of(2026, 5, 17),
            draft = DrinkEditorDraft(
                quantityMlText = "500",
                abvText = "5.2",
                numberOfDrinksText = "2",
                time = LocalTime.of(18, 30)
            ),
            isDeleteConfirming = false,
            onDraftChange = {},
            onSave = {},
            onDeleteClick = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(
    name = "Edit",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkEditorSheetEditPreview() {
    DrinkWiseTheme {
        DrinkEditorSheetContent(
            mode = DrinkEditorSheetMode.Edit,
            selectedDate = LocalDate.of(2026, 5, 17),
            draft = DrinkEditorDraft(
                quantityMlText = "175",
                abvText = "13.5",
                numberOfDrinksText = "1",
                time = LocalTime.of(20, 15)
            ),
            isDeleteConfirming = false,
            onDraftChange = {},
            onSave = {},
            onDeleteClick = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(
    name = "Edit delete confirmation",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkEditorSheetEditDeletePreview() {
    DrinkWiseTheme {
        DrinkEditorSheetContent(
            mode = DrinkEditorSheetMode.Edit,
            selectedDate = LocalDate.of(2026, 5, 17),
            draft = DrinkEditorDraft(
                quantityMlText = "175",
                abvText = "13.5",
                numberOfDrinksText = "1",
                time = LocalTime.of(20, 15)
            ),
            isDeleteConfirming = true,
            onDraftChange = {},
            onSave = {},
            onDeleteClick = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(
    name = "Validation errors",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkEditorSheetValidationErrorPreview() {
    DrinkWiseTheme {
        DrinkEditorSheetContent(
            mode = DrinkEditorSheetMode.Add,
            selectedDate = LocalDate.of(2026, 5, 17),
            draft = DrinkEditorDraft(
                quantityMlText = "0",
                abvText = "101",
                numberOfDrinksText = "1",
                time = LocalTime.of(18, 30)
            ),
            isDeleteConfirming = false,
            onDraftChange = {},
            onSave = {},
            onDeleteClick = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            modifier = Modifier.padding(24.dp)
        )
    }
}

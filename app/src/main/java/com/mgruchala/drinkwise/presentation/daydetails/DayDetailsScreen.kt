package com.mgruchala.drinkwise.presentation.daydetails

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.daydetails.components.DayConsumptionIndicator
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkEditorSheet
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkEditorSheetMode
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkListItem
import com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorDraft
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayDetailsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DayDetailsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onAddDrinks = viewModel::addDrinks,
        onUpdateDrink = viewModel::updateDrink,
        onDeleteDrink = viewModel::deleteDrink,
        onUndoDelete = viewModel::undoLastDeletedDrink,
        createAddDraft = viewModel::createAddDraft,
        modifier = modifier
    )
}

private sealed interface DrinkEditorUiState {
    data class Add(val draft: DrinkEditorDraft) : DrinkEditorUiState

    data class Edit(
        val drink: DrinkEntity,
        val draft: DrinkEditorDraft,
        val isDeleteConfirming: Boolean = false
    ) : DrinkEditorUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailsContent(
    state: DayDetailsState,
    onNavigateBack: () -> Unit,
    onAddDrinks: (quantityMl: Int, abv: Float, numberOfDrinks: Int, time: LocalTime) -> Unit,
    onUpdateDrink: (original: DrinkEntity, quantityMl: Int, abv: Float, time: LocalTime) -> Unit,
    onDeleteDrink: suspend (DrinkEntity) -> Boolean,
    onUndoDelete: () -> Unit,
    createAddDraft: () -> DrinkEditorDraft,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var editorState by remember { mutableStateOf<DrinkEditorUiState?>(null) }
    val drinkDeletedMessage = stringResource(R.string.day_details_drink_deleted)
    val undoDeleteLabel = stringResource(R.string.day_details_undo_delete)
    val deleteWithUndo: (DrinkEntity) -> Unit = { drink ->
        snackbarHostState.currentSnackbarData?.dismiss()
        coroutineScope.launch {
            val didDelete = onDeleteDrink(drink)
            if (!didDelete) {
                return@launch
            }
            val result = snackbarHostState.showSnackbar(
                message = drinkDeletedMessage,
                actionLabel = undoDeleteLabel
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editorState = DrinkEditorUiState.Add(
                        draft = createAddDraft()
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.day_details_add_drink)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = state.selectedDate.format(formatter))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.day_details_navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DayConsumptionIndicator(alcoholUnitLevel = state.alcoholUnitLevel)
                }
            }
            item {
                Text(
                    text = stringResource(R.string.day_details_drinks_section_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (state.drinks.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.day_details_no_drinks),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(state.drinks, key = { it.uid }) { drink ->
                    SwipeToDeleteDrinkItem(
                        drink = drink,
                        onClick = {
                            editorState = DrinkEditorUiState.Edit(
                                drink = drink,
                                draft = DrinkEditorDraft.forEdit(drink)
                            )
                        },
                        onDelete = { deleteWithUndo(drink) }
                    )
                }
            }
        }
    }

    when (val currentEditorState = editorState) {
        is DrinkEditorUiState.Add -> {
            DrinkEditorSheet(
                mode = DrinkEditorSheetMode.Add,
                selectedDate = state.selectedDate,
                draft = currentEditorState.draft,
                isDeleteConfirming = false,
                onDraftChange = { draft -> editorState = currentEditorState.copy(draft = draft) },
                onSave = {
                    val draft = currentEditorState.draft
                    if (draft.isValidForAdd) {
                        onAddDrinks(
                            requireNotNull(draft.quantityMl),
                            requireNotNull(draft.abv),
                            requireNotNull(draft.numberOfDrinks),
                            draft.time
                        )
                        editorState = null
                    }
                },
                onDeleteClick = {},
                onCancelDelete = {},
                onConfirmDelete = {},
                onDismiss = { editorState = null }
            )
        }

        is DrinkEditorUiState.Edit -> {
            DrinkEditorSheet(
                mode = DrinkEditorSheetMode.Edit,
                selectedDate = state.selectedDate,
                draft = currentEditorState.draft,
                isDeleteConfirming = currentEditorState.isDeleteConfirming,
                onDraftChange = { draft ->
                    editorState = currentEditorState.copy(
                        draft = draft,
                        isDeleteConfirming = false
                    )
                },
                onSave = {
                    val draft = currentEditorState.draft
                    if (draft.isValidForEdit) {
                        onUpdateDrink(
                            currentEditorState.drink,
                            requireNotNull(draft.quantityMl),
                            requireNotNull(draft.abv),
                            draft.time
                        )
                        editorState = null
                    }
                },
                onDeleteClick = {
                    editorState = currentEditorState.copy(isDeleteConfirming = true)
                },
                onCancelDelete = {
                    editorState = currentEditorState.copy(isDeleteConfirming = false)
                },
                onConfirmDelete = {
                    editorState = null
                    deleteWithUndo(currentEditorState.drink)
                },
                onDismiss = { editorState = null }
            )
        }

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteDrinkItem(
    drink: DrinkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deleteBackgroundDescription = stringResource(R.string.day_details_swipe_delete_background)
    val deleteAction = CustomAccessibilityAction(
        label = deleteBackgroundDescription,
        action = {
            onDelete()
            true
        }
    )
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp)
                    .semantics {
                        contentDescription = deleteBackgroundDescription
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        DrinkListItem(
            drink = drink,
            onClick = onClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    customActions = listOf(deleteAction)
                }
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun DayDetailsContentPreviewLightTheme() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = dayDetailsPreviewState,
            onNavigateBack = {},
            onAddDrinks = { _, _, _, _ -> },
            onUpdateDrink = { _, _, _, _ -> },
            onDeleteDrink = { true },
            onUndoDelete = {},
            createAddDraft = ::previewAddDraft
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun DayDetailsContentPreviewDarkTheme() {
    DrinkWiseTheme(darkTheme = true) {
        DayDetailsContent(
            state = dayDetailsPreviewState,
            onNavigateBack = {},
            onAddDrinks = { _, _, _, _ -> },
            onUpdateDrink = { _, _, _, _ -> },
            onDeleteDrink = { true },
            onUndoDelete = {},
            createAddDraft = ::previewAddDraft
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun DayDetailsContentEmptyPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = dayDetailsPreviewState.copy(
                drinks = emptyList(),
                totalUnits = 0f,
                alcoholUnitLevel = AlcoholUnitLevel.Low(0f, 4f)
            ),
            onNavigateBack = {},
            onAddDrinks = { _, _, _, _ -> },
            onUpdateDrink = { _, _, _, _ -> },
            onDeleteDrink = { true },
            onUndoDelete = {},
            createAddDraft = ::previewAddDraft
        )
    }
}

private fun previewAddDraft() = DrinkEditorDraft(
    quantityMlText = "",
    abvText = "",
    numberOfDrinksText = "1",
    time = LocalTime.of(12, 0)
)

private val dayDetailsPreviewState = DayDetailsState(
    selectedDate = LocalDate.of(2026, 5, 17),
    drinks = listOf(
        previewDrink(
            uid = 1,
            quantity = 500,
            alcoholContent = 5f,
            hour = 18,
            minute = 30
        ),
        previewDrink(
            uid = 2,
            quantity = 175,
            alcoholContent = 13.5f,
            hour = 20,
            minute = 15
        ),
        previewDrink(
            uid = 3,
            quantity = 40,
            alcoholContent = 40f,
            hour = 22,
            minute = 5
        )
    ),
    totalUnits = 7.4f,
    dailyLimit = 4f,
    alcoholUnitLevel = AlcoholUnitLevel.High(7.4f, 4f),
    isLoading = false
)

private fun previewDrink(
    uid: Int,
    quantity: Int,
    alcoholContent: Float,
    hour: Int,
    minute: Int
) = DrinkEntity(
    uid = uid,
    quantity = quantity,
    alcoholContent = alcoholContent,
    timestamp = LocalDateTime.of(2026, 5, 17, hour, minute)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
)

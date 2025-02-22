package com.mgruchala.drinkwise.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgruchala.drinkwise.calculator.AlcoholCalculatorContent
import com.mgruchala.drinkwise.calculator.AlcoholUnitsCalculatorViewModel

@Composable
fun AddDrinkDialog(
    alcoholUnitsCalculatorViewModel: AlcoholUnitsCalculatorViewModel = viewModel(),
    onAddClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val state by alcoholUnitsCalculatorViewModel.state.collectAsState()
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AlcoholCalculatorContent(
                    state = state,
                    modifier = Modifier
                        .wrapContentSize()
                        .weight(1f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onAddClick
                    ) {
                        Text(
                            "Add",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun AddDrinkDialogPreview() {
    AddDrinkDialog(
        onAddClick = {},
        onDismiss = {}
    )
}


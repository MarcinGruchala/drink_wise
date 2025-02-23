package com.mgruchala.drinkwise.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgruchala.drinkwise.calculator.AlcoholCalculatorContent
import com.mgruchala.drinkwise.calculator.AlcoholUnitsCalculatorViewModel
import com.mgruchala.drinkwise.theme.DrinkWiseTheme

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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AlcoholCalculatorContent(
                    state = state,
                    modifier = Modifier
                        .wrapContentSize(),
                    onPercentageChanged = alcoholUnitsCalculatorViewModel::onPercentageChanged,
                    onAmountDecrement = alcoholUnitsCalculatorViewModel::onDecrement,
                    onAmountIncrement = alcoholUnitsCalculatorViewModel::onIncrement,
                    onQuantityChanged = alcoholUnitsCalculatorViewModel::onQuantityChanged
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
                        Text(
                            "Cancel",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = onAddClick
                    ) {
                        Text(
                            "Add",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun AddDrinkDialogPreviewDarkTheme() {
    DrinkWiseTheme {

        Scaffold {
            AddDrinkDialog(
                onAddClick = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun AddDrinkDialogPreviewLightTheme() {
    DrinkWiseTheme {
        Scaffold {
            AddDrinkDialog(
                onAddClick = {},
                onDismiss = {}
            )
        }
    }
}



package com.mgruchala.drinkwise.calculator

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgruchala.drinkwise.ui.theme.DrinkWiseTheme

@Composable
fun AlcoholCalculatorView(
    viewModel: AlcoholUnitsCalculatorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    AlcoholCalculatorContent(
        state = state,
        onQuantityChanged = viewModel::onQuantityChanged,
        onPercentageChanged = viewModel::onPercentageChanged,
        onAmountDecrement = viewModel::onDecrement,
        onAmountIncrement = viewModel::onIncrement
    )
}

@Composable
fun AlcoholCalculatorContent(
    state: AlcoholCalculatorState,
    onQuantityChanged: (Int) -> Unit = {},
    onPercentageChanged: (Float) -> Unit = {},
    onAmountDecrement: () -> Unit = {},
    onAmountIncrement: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        DrinkTypeSection(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
        )
        HorizontalDivider()
        DrinkParametersSection(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
            quantityValue = state.drinkQuantityMl,
            alcoholContentValue = state.alcoholPercentage,
            onQuantityChanged = onQuantityChanged,
            onPercentageChanged = onPercentageChanged
        )
        HorizontalDivider()
        DrinksAmountSection(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
            amount = state.amountOfDrinks,
            onDecrement = onAmountDecrement,
            onIncrement = onAmountIncrement
        )
        HorizontalDivider()
        AlcoholUnitSection(
            modifier = Modifier.weight(1f),
            alcoholUnits = state.calculatedUnits
        )
    }
}

@Composable
fun DrinkTypeSection(
    modifier: Modifier
) {
    // TODO: Implement drink type selection
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlcoholCalculatorSectionText(text = "Drink Type")
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.WineBar,
            contentDescription = "Drink Icon"
        )
    }
}

@Composable
fun DrinkParametersSection(
    modifier: Modifier,
    quantityValue: Int? = null,
    alcoholContentValue: Float? = null,
    onQuantityChanged: (Int) -> Unit = {},
    onPercentageChanged: (Float) -> Unit = {}
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AlcoholCalculatorSectionText("Quantity (ml)")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = quantityValue?.toString() ?: "",
                onValueChange = {
                    val newValue = it.toIntOrNull()
                    if (newValue != null) {
                        onQuantityChanged(newValue)
                    }
                },
                modifier = Modifier.size(width = 120.dp, height = 50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AlcoholCalculatorSectionText("Alcohol content (%)")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = alcoholContentValue?.toString() ?: "",
                onValueChange = {
                    Log.d("AlcoholCalculator", "New value: $it")
                    val newValue = it.toFloatOrNull()
                    if (newValue != null) {
                        onPercentageChanged(newValue)
                    }
                },
                modifier = Modifier.size(width = 120.dp, height = 50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
fun DrinksAmountSection(
    modifier: Modifier,
    amount: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlcoholCalculatorSectionText("Amount of drinks: $amount")
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onDecrement() }) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease amount"
            )
        }

        IconButton(onClick = { onIncrement() }) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase amount"
            )
        }
    }
}

@Composable
fun AlcoholUnitSection(
    modifier: Modifier,
    alcoholUnits: Float?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (alcoholUnits != null) {
            Text(
                text = "${"%.2f".format(alcoholUnits)} Alcohol Units", // placeholder text, no real calculation here
                style = MaterialTheme.typography.headlineLarge
            )
        } else {
            Text(
                text = "Fill in the details above to calculate alcohol units",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AlcoholCalculatorSectionText(
    text: String
) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge
    )
}


val alcoholCalculatorInitialState = AlcoholCalculatorState()
val alcoholUnitsFilledState = AlcoholCalculatorState(
    drinkQuantityMl = 250,
    alcoholPercentage = 40f,
    amountOfDrinks = 2,
    calculatedUnits = 1.0f
)

@Preview(name = "Light Mode Preview (Initial)", showBackground = true)
@Composable
fun AlcoholCalculatorScreenPreviewLight() {
    DrinkWiseTheme {
        AlcoholCalculatorContent(state = alcoholCalculatorInitialState)
    }
}

@Preview(
    name = "Dark Mode Preview (Initial)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AlcoholCalculatorScreenPreviewDark() {
    DrinkWiseTheme {
        AlcoholCalculatorContent(state = alcoholCalculatorInitialState)
    }
}

@Preview(name = "Light Mode Preview (Filled)", showBackground = true)
@Composable
fun AlcoholCalculatorScreenPreviewLightFilled() {
    DrinkWiseTheme {
        AlcoholCalculatorContent(state = alcoholUnitsFilledState)
    }
}

@Preview(
    name = "Dark Mode Preview (Filled)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AlcoholCalculatorScreenPreviewDarkFilled() {
    DrinkWiseTheme {
        AlcoholCalculatorContent(state = alcoholUnitsFilledState)
    }
}

package com.mgruchala.drinkwise.presentation.calculator

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgruchala.drinkwise.R
import androidx.compose.ui.res.stringResource
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme

@Composable
fun AlcoholCalculatorView(
    viewModel: AlcoholUnitsCalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    AlcoholCalculatorContent(
        modifier = modifier,
        state = state,
        onQuantityChanged = viewModel::onQuantityChanged,
        onPercentageChanged = viewModel::onPercentageChanged,
        onNumberDecrement = viewModel::onDecrement,
        onNumberIncrement = viewModel::onIncrement,
        isInDialog = false
    )
}

@Composable
fun AlcoholCalculatorContent(
    modifier: Modifier = Modifier,
    state: AlcoholCalculatorState,
    onQuantityChanged: (Int) -> Unit = {},
    onPercentageChanged: (Float) -> Unit = {},
    onNumberDecrement: () -> Unit = {},
    onNumberIncrement: () -> Unit = {},
    isInDialog: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
//        DrinkTypeSection(
//            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
//        )
//        HorizontalDivider()
        DrinkParametersSection(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            quantityValue = state.drinkQuantityMl,
            alcoholContentValue = state.alcoholPercentage,
            onQuantityChanged = onQuantityChanged,
            onPercentageChanged = onPercentageChanged
        )
        HorizontalDivider()
        DrinksNumberSection(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            number = state.numberOfDrinks,
            onDecrement = onNumberDecrement,
            onIncrement = onNumberIncrement
        )
        HorizontalDivider()
        AlcoholUnitSection(
            alcoholUnits = state.calculatedUnits,
            modifier = if (!isInDialog) Modifier.weight(1f) else Modifier
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
        AlcoholCalculatorSectionText(text = stringResource(id = R.string.calculator_drink_type))
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.WineBar,
            contentDescription = stringResource(id = R.string.calculator_drink_icon_content_description)
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
            AlcoholCalculatorSectionText(stringResource(id = R.string.calculator_quantity_ml))
            Spacer(modifier = Modifier.height(8.dp))
            AlcoholCalculatorTextField(
                value = quantityValue?.toString() ?: "",
                onValueChange = { newValue ->
                    val newQuantityValue = newValue.toIntOrNull()
                    if (newQuantityValue != null) {
                        onQuantityChanged(newQuantityValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AlcoholCalculatorSectionText(stringResource(id = R.string.calculator_alcohol_content_percentage))
            AlcoholCalculatorTextField(
                value = alcoholContentValue?.toString() ?: "",
                onValueChange = { newValue ->
                    val newAlcoholContentValue = newValue.toFloatOrNull()
                    if (newAlcoholContentValue != null) {
                        onPercentageChanged(newAlcoholContentValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
fun AlcoholCalculatorTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        modifier = modifier
            .size(width = 106.dp, height = 50.dp)
            .padding(0.dp),
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        textStyle = TextStyle(
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = keyboardOptions,
        singleLine = true,
    )
}

@Composable
fun DrinksNumberSection(
    modifier: Modifier,
    number: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlcoholCalculatorSectionText(
            stringResource(
                id = R.string.calculator_number_of_drinks,
                number
            )
        )
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onDecrement() }) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = stringResource(id = R.string.calculator_decrease_number_content_description)
            )
        }

        IconButton(onClick = { onIncrement() }) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(id = R.string.calculator_increase_number_content_description)
            )
        }
    }
}

@Composable
fun AlcoholUnitSection(
    modifier: Modifier = Modifier,
    alcoholUnits: Float?
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (alcoholUnits != null) {
            Text(
                text = "${"%.2f".format(alcoholUnits)} ${stringResource(id = R.string.calculator_alcohol_units_suffix)}",
                style = MaterialTheme.typography.headlineLarge
            )
        } else {
            Text(
                text = stringResource(id = R.string.calculator_fill_details_prompt),
                style = MaterialTheme.typography.headlineSmall,
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
        style = MaterialTheme.typography.titleMedium
    )
}


val alcoholCalculatorInitialState = AlcoholCalculatorState()
val alcoholUnitsFilledState = AlcoholCalculatorState(
    drinkQuantityMl = 250,
    alcoholPercentage = 40f,
    numberOfDrinks = 2,
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

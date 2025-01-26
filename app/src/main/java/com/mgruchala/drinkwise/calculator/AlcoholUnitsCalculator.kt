package com.mgruchala.drinkwise.calculator

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.ui.theme.DrinkWiseTheme

@Composable
fun AlcoholCalculatorView() {
    AlcoholCalculatorContent()
}

@Composable
fun AlcoholCalculatorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        DrinkTypeSection(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
        )
        HorizontalDivider()
        DrinkParametersSection(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
        )
        HorizontalDivider()
        DrinksAmountSection(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
        )
        HorizontalDivider()
        AlcoholUnitSection(
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DrinkTypeSection(
    modifier: Modifier
) {
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
    modifier: Modifier
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
                value = "",
                onValueChange = { },
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
                value = "",
                onValueChange = { },
                modifier = Modifier.size(width = 120.dp, height = 50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun DrinksAmountSection(
    modifier: Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlcoholCalculatorSectionText("Amount of drinks: 1")
        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { }
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease amount"
            )
        }

        IconButton(
            onClick = { }
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase amount"
            )
        }
    }
}

@Composable
fun AlcoholUnitSection(
    modifier: Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "0.0 Alcohol Units", // placeholder text, no real calculation here
            style = MaterialTheme.typography.headlineLarge
        )
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

@Preview(
    name = "Light Mode Preview",
    showBackground = true,
)
@Composable
fun AlcoholCalculatorScreenPreviewLight() {
    DrinkWiseTheme {
        AlcoholCalculatorContent()
    }
}


@Preview(
    name = "Dark Mode Preview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AlcoholCalculatorScreenPreviewDark() {
    DrinkWiseTheme {
        AlcoholCalculatorContent()
    }
}

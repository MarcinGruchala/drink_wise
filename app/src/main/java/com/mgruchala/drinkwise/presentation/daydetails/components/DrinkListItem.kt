package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DrinkListItem(
    drink: DrinkEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val units = calculateAlcoholUnits(drink.quantity, drink.alcoholContent)
    val formattedUnits = "%.2f".format(units)
    val formattedVolume = formatVolume(drink.quantity)
    val formattedAbv = "%.1f".format(drink.alcoholContent)
    val formattedTime = formatTimestamp(drink.timestamp)

    // Accessibility description for TalkBack
    val accessibilityDescription = context.getString(
        R.string.day_details_a11y_drink_item,
        formattedVolume,
        formattedTime,
        formattedAbv,
        formattedUnits
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = accessibilityDescription
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalBar,
                contentDescription = null, // Decorative, covered by card semantics
                modifier = Modifier
                    .size(40.dp)
                    .clearAndSetSemantics { },
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$formattedVolume \u2022 $formattedAbv% \u2022 $formattedUnits units",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

@Preview(showBackground = true, name = "Drink Item - Small volume")
@Composable
private fun DrinkListItemSmallPreview() {
    DrinkWiseTheme {
        DrinkListItem(
            drink = DrinkEntity(
                uid = 1,
                quantity = 330,
                alcoholContent = 5.0f,
                timestamp = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Drink Item - Large volume")
@Composable
private fun DrinkListItemLargePreview() {
    DrinkWiseTheme {
        DrinkListItem(
            drink = DrinkEntity(
                uid = 2,
                quantity = 1500,
                alcoholContent = 12.5f,
                timestamp = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

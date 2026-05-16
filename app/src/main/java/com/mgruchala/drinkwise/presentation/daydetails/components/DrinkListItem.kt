package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits

@Composable
fun DrinkListItem(
    drink: DrinkEntity,
    modifier: Modifier = Modifier
) {
    val volume = formatDayDetailsVolume(drink.quantity)
    val abv = formatDayDetailsAbv(drink.alcoholContent)
    val units = formatDayDetailsUnits(calculateAlcoholUnits(drink.quantity, drink.alcoholContent).toFloat())
    val time = formatDayDetailsTime(drink.timestamp)
    val description = stringResource(
        id = R.string.day_details_drink_item_description,
        volume,
        abv,
        units,
        time
    )
    val details = stringResource(
        id = R.string.day_details_drink_item_details,
        volume,
        abv,
        units,
        time
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalBar,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.day_details_drink_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider()
    }
}

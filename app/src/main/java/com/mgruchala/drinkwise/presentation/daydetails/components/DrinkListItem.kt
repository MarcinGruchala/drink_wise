package com.mgruchala.drinkwise.presentation.daydetails.components

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import java.time.LocalDateTime
import java.time.ZoneId

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

@Preview(
    name = "Light",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkListItemPreviewLightTheme() {
    DrinkListItemPreview(darkTheme = false)
}

@Preview(
    name = "Dark",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DrinkListItemPreviewDarkTheme() {
    DrinkListItemPreview(darkTheme = true)
}

@Composable
private fun DrinkListItemPreview(darkTheme: Boolean) {
    DrinkWiseTheme(darkTheme = darkTheme) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            DrinkListItem(
                drink = previewDrink,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private val previewDrink = DrinkEntity(
    uid = 1,
    quantity = 500,
    alcoholContent = 5f,
    timestamp = LocalDateTime.of(2026, 5, 17, 18, 30)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
)

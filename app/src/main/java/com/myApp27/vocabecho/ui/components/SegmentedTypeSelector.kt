package com.myApp27.vocabecho.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myApp27.vocabecho.domain.model.CardType

/**
 * Reusable segmented button row for selecting card types.
 * Uses Material3 SingleChoiceSegmentedButtonRow with equal-width buttons.
 *
 * @param selectedType currently selected CardType
 * @param onTypeSelected callback when user selects a new type
 * @param modifier optional modifier for the row
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedTypeSelector(
    selectedType: CardType,
    onTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = CardType.selectableTypes()
    // Map BASIC_REVERSED to BASIC for display
    val displayType = if (selectedType == CardType.BASIC_REVERSED) CardType.BASIC else selectedType

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
    ) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = displayType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = types.size,
                    baseShape = RoundedCornerShape(10.dp)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = type.shortLabelRu(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

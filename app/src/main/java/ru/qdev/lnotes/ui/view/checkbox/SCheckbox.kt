package ru.qdev.lnotes.ui.view.checkbox

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun defCheckboxColors(): CheckboxColors {
    val disabledSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    return CheckboxColors(
            checkedCheckmarkColor = MaterialTheme.colorScheme.tertiary,
            uncheckedCheckmarkColor = Color.Transparent,
            checkedBoxColor = MaterialTheme.colorScheme.secondaryContainer,
            uncheckedBoxColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledCheckedBoxColor = disabledSecondaryContainer,
            disabledUncheckedBoxColor = Color.Transparent,
            disabledIndeterminateBoxColor = disabledSecondaryContainer,
            checkedBorderColor = MaterialTheme.colorScheme.tertiary,
            uncheckedBorderColor = MaterialTheme.colorScheme.tertiary,
            disabledBorderColor = disabledSecondaryContainer,
            disabledUncheckedBorderColor = disabledSecondaryContainer,
            disabledIndeterminateBorderColor = disabledSecondaryContainer
        )
}

@Composable
fun SCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = defCheckboxColors(),
    interactionSource: MutableInteractionSource? = null
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}
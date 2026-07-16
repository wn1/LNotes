package ru.qdev.lnotes.ui.view.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ru.qdev.lnotes.ui.theme.dp1
import ru.qdev.lnotes.ui.theme.dp10
import ru.qdev.lnotes.ui.view.text.SText

@Composable fun MainButtonColors() = ButtonDefaults.buttonColors().copy(
    contentColor = MaterialTheme.colorScheme.tertiary,
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    disabledContentColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
)

@Composable fun SecondaryButtonColors() = ButtonDefaults.buttonColors().copy(
    contentColor = MaterialTheme.colorScheme.secondary,
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
)

@Composable
fun MainButtonBorderStroke(contentColor: Color) =
    BorderStroke(dp1, contentColor)

@Composable
fun MainButtonContent(
    text: String
): @Composable RowScope.(Color) -> Unit {
    return {
        SText(
            text = text,
            color = it
        )
    }
}


@Composable
fun SButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(dp10),
    colors: ButtonColors = MainButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: @Composable (Color) -> BorderStroke? = {
        MainButtonBorderStroke(it)
    },
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.(Color) -> Unit
) {
    val b = border(if (enabled) colors.contentColor else colors.disabledContentColor)

    Button (
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = b,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = {
            content.invoke(
                this,
                if (enabled) colors.contentColor else colors.disabledContentColor
            )
        }
    )
}
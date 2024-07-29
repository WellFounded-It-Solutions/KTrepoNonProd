package se.infomaker.profile.view.items.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.NoOpUpdate
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.profile.data.SectionPosition
import se.infomaker.utilities.toComposeColor

@ExperimentalMaterialApi
@Composable
fun ProfileCard(
    backgroundColor: Color = Color.Transparent,
    action: (() -> Unit)? = null,
    position: SectionPosition = SectionPosition.MIDDLE,
    content: @Composable () -> Unit,
) {
    val theme by LocalContext.current.theme()
    val rippleColor = remember { theme.brandColor.toComposeColor }
    Column {
        rememberRipple(bounded = true, color = rippleColor)
        Card(
            onClick = action ?: {},
            modifier = remember { Modifier.fillMaxWidth() },
            enabled = action != null,
            shape = remember { RoundedCornerShape(0.dp) },
            backgroundColor = backgroundColor,
            border = remember { BorderStroke(0.dp, Color.Transparent) },
            elevation = 0.dp,
            interactionSource = remember { MutableInteractionSource() }) {
            content()
        }
        AddDivider(position = position)
    }
}

fun Modifier.rowPadding(start: Dp): Modifier {
    return this.padding(top = 6.dp, bottom = 6.dp, start = start, end = 0.dp)
}

fun Modifier.sectionMargin(position: SectionPosition, bottom: Dp): Modifier {
    return when (position) {
        SectionPosition.END -> this.padding(bottom = bottom)
        SectionPosition.BOTH -> this.padding(bottom = bottom)
        else -> this.padding(top = 0.dp, bottom = 0.dp)
    }
}

fun Modifier.cardElevation(thickness: Dp = 5.dp, color: Color): Modifier = this
    .fillMaxWidth()
    .height(thickness)
    .drawWithCache {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color,
                Color.Transparent
            )
        )
        onDrawBehind {
            drawRect(gradient)
        }
    }

@NonRestartableComposable
@Composable
fun Divider(sectionPosition: SectionPosition) {
    Column {
        val cardDecorator = LocalCardDecorator.current

        Spacer(modifier = remember {
            Modifier
                .sectionMargin(position = sectionPosition, bottom = cardDecorator.marginBottom)
                .cardElevation(
                    thickness = cardDecorator.elevationThickness,
                    color = cardDecorator.elevationColor)
        })
        Spacer(modifier = remember {
                Modifier.height(cardDecorator.marginBottom)
        })
    }
}

@NonRestartableComposable
@Composable
fun AddDivider(position: SectionPosition) {
    when (position) {
        SectionPosition.END -> Divider(position)
        SectionPosition.BOTH -> Divider(position)
        else -> NoOpUpdate
    }
}
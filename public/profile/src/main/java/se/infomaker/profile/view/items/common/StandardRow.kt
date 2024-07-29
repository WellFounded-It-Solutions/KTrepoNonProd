package se.infomaker.profile.view.items.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import se.infomaker.theme.ThemeColorListDelegate

@Composable
fun BaseItem(
    modifier: Modifier? = Modifier,
    padding: Modifier? = null,
    action: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val rippleColor by ThemeColorListDelegate(
        context = context,
        themeKeys = listOf("brandColor")
    )

    val defaultModifier = Modifier.background(color = Color.Transparent)
    val baseModifier = (modifier ?: defaultModifier)

    val paddingModifier = padding ?: Modifier.padding(vertical = 12.dp, horizontal = 12.dp)

    val enhancedModifier: Modifier = action?.let {
        val clickableModifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = true, color = rippleColor),
            onClick = it
        )
        baseModifier.then(clickableModifier).then(paddingModifier)
    } ?: baseModifier.then(paddingModifier)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = enhancedModifier.fillMaxWidth()
    ) {
        content()
    }
}

package se.infomaker.profile.view.items.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun AnimatedItem(visibility: Boolean, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    AnimatedVisibility(visible = visibility,
        enter = slideInVertically(
            initialOffsetY = { with(density) { +80.dp.roundToPx() } }
        ) + expandVertically(
            expandFrom = Alignment.Bottom
        ) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()) {
        content()
    }
}
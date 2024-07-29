package se.infomaker.profile.view.items.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import timber.log.Timber

@Composable
fun ProfileIcon(
    modifier: Modifier = Modifier,
    identifier: Int,
    color: Color? = null
) {
    when (identifier) {
        0 -> Timber.d("Unable to load specified image resource with name=$identifier.")
        else -> {
            Image(
                painter = painterResource(id = identifier),
                contentDescription = null,
                modifier = modifier,
                colorFilter = color?.let { ColorFilter.tint(it) }
            )
        }
    }
}

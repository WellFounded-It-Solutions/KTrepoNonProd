package se.infomaker.profile.view.items.consent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.navigaglobal.mobile.consent.ConsentManagerProvider
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.profile.data.ConsentItem
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.ProfileIcon


@ExperimentalMaterialApi
@Composable
fun ConsentItemView(consentItem: ConsentItem) {

    val context = LocalContext.current
    val consentManager =
        (context.findActivity())?.let { activity -> ConsentManagerProvider.provide(activity) }
            ?: return

    ProfileCard(backgroundColor = consentItem.backgroundColor,
        position = consentItem.position.value,
        action = {
            (context.findActivity())?.let { activity ->
                consentManager.apply {
                    resetConsent(activity)
                    presentConsentForm(activity)
                }
            }
        }) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(
                text = consentItem.text,
                style = consentItem.textStyle,
                modifier = Modifier.weight(2f),
            )
            ProfileIcon(identifier = consentItem.trailingDrawable, color = consentItem.trailingDrawableTint)
        }
    }
}
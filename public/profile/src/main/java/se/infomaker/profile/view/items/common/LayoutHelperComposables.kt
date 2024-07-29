package se.infomaker.profile.view.items.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.infomaker.profile.data.ProfileItem

@Composable
fun ImageColumn(profileItem: ProfileItem) {
    Column(
        modifier = Modifier.width(if (profileItem.image != -1) 72.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (profileItem.image != -1) {
            ProfileIcon(identifier = profileItem.image)
        }
    }
}

@Composable
fun RowContainer(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 6.dp, bottom = 6.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }
}

@Composable
fun WrappableText(
    profileItem: ProfileItem,
    indent: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rowPadding(if (profileItem.image != -1) 0.dp else indent)
    ) {
        Text(
            text = profileItem.text.orEmpty(),
            style = profileItem.textStyle,
        )
    }
}

@Composable
fun WrappableTextWithChevron(
    profileItem: ProfileItem,
    indent: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rowPadding(if (profileItem.image != -1) 0.dp else indent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Text(
                modifier = Modifier
                    .weight(0.9f)
                    .wrapContentWidth(Alignment.Start),
                text = profileItem.text.orEmpty(),
                style = profileItem.textStyle,
            )
            ProfileIcon(
                modifier = Modifier
                    .weight(0.1f)
                    .wrapContentWidth(Alignment.End),
                identifier = profileItem.trailingDrawable,
                color = profileItem.trailingDrawableTint
            )
        }
    }
}


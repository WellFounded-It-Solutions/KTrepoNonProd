package se.infomaker.dependencies.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.infomaker.dependencies.data.DependencyListEvents
import se.infomaker.dependencies.data.DependencyListState
import se.infomaker.profile.data.SectionPosition
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.theme.StyleData

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun DependencyList(
    state: DependencyListState,
    onTriggerEvent: (DependencyListEvents) -> Unit,
    styleData: StyleData
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {

        item {
            state.headerText?.let { headerText ->
                Box(modifier = Modifier
                    .background(styleData.itemBackgroundColor)
                    .padding(horizontal = 12.dp, vertical = 12.dp)) {
                    ProfileCard(backgroundColor = styleData.itemBackgroundColor) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = headerText, style = styleData.licenseHeaderTextStyle)
                        }
                    }
                }
            }
        }

        item {
            Divider(
                modifier = Modifier.padding(0.dp),
                color = styleData.dividerColor,
                thickness = 1.dp
            )
        }

        items(items = state.dependencies,
            key = { "${it.groupName}:${it.artifactName}${it.getVersion()}" }) { dependency ->

            dependency.moduleName?.let {
                val modifier = remember { Modifier.fillMaxWidth() }
                val action = if (state.showLicenses) {
                    dependency.license?.let {
                        { onTriggerEvent(DependencyListEvents.OpenLicense(it.url)) }
                    }
                } else null

                ProfileCard(backgroundColor = styleData.itemBackgroundColor,
                    position = SectionPosition.END,
                    action = action) {
                    Column(modifier = modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                        Row(modifier = modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(dependency.artifactName, style = styleData.artifactTextStyle, fontWeight = FontWeight.Bold)
                            Text(dependency.getVersion(), style = styleData.artifactTextStyle, fontWeight = FontWeight.SemiBold)
                        }

                        Text(dependency.groupName, style = styleData.groupTextStyle)

                        if (state.showLicenses) {
                            dependency.license?.let {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        it.name,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = styleData.groupTextStyle
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
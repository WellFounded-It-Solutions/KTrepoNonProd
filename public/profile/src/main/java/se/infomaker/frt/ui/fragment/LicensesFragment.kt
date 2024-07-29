package se.infomaker.frt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.lightColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import se.infomaker.dependencies.data.DependencyViewModel
import se.infomaker.dependencies.view.DependencyList
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.iap.theme.ktx.backgroundColor
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.iap.theme.ktx.brandVariantColor
import se.infomaker.iap.theme.ktx.onBrandColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.profile.view.items.common.CardDecorator
import se.infomaker.profile.view.items.common.ListDecorator
import se.infomaker.profile.view.items.common.LocalCardDecorator
import se.infomaker.profile.view.items.common.LocalListDecorator
import se.infomaker.profile.view.items.common.rememberViewInteropNestedScrollConnection
import se.infomaker.theme.StyleData
import se.infomaker.utilities.ProfileViewModelFactory
import se.infomaker.utilities.toComposeColor
import se.infomaker.utilities.toComposeTextStyle
import se.infomaker.utilities.toDp

class LicensesFragment : BaseModule() {

    val theme by theme()

    @FlowPreview
    private val dependencyViewModel: DependencyViewModel by viewModels {
        ProfileViewModelFactory(
            requireActivity().application,
            moduleIdentifier
        )
    }

    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val cardDecorator = CardDecorator(
                    elevationColor = theme.getColor("profileSectionSeparator", null)?.toComposeColor ?: CardDecorator.DEFAULT_COLOR,
                    elevationThickness = 1.dp,
                    marginBottom = 0.dp,
                )

                val listDecorator =
                    ListDecorator(top = theme.getSize("profileListMarginTop", null)?.toDp ?: ListDecorator.DEFAULT_MARGIN,
                        bottom = theme.getSize("profileListMarginBottom", null)?.toDp ?: ListDecorator.DEFAULT_MARGIN
                    )

                val styleData = StyleData(
                    itemBackgroundColor = theme.getColor(listOf("licensesItemBackground", "profileSectionItemBackground"), null)?.toComposeColor ?: Color.White,
                    dividerColor = theme.getColor(listOf("profileSectionItemSeparator"), null)?.toComposeColor ?: Color(0x33000000),
                    artifactTextStyle =  theme.getText(listOf("artifactLicensesSectionItemText"), null)?.toComposeTextStyle(theme) ?: TextStyle(color = Color(0xCC000000), fontSize = 14.sp),
                    groupTextStyle = theme.getText(listOf("licensesSectionItemText", "profileSectionItemText"), null)?.toComposeTextStyle(theme) ?: TextStyle(color = Color(0xCC000000), fontSize = 14.sp),
                    licenseHeaderTextStyle = theme.getText(listOf("licensesSectionHeader"), null)?.toComposeTextStyle(theme) ?: TextStyle(color = Color(0xCC000000), fontSize = 14.sp)
                )

                val iapColors = remember {
                    lightColors(
                        primary = theme.brandColor.toComposeColor,
                        primaryVariant = theme.brandVariantColor.toComposeColor,
                        background = theme.backgroundColor.toComposeColor,
                        onPrimary = theme.onBrandColor.toComposeColor,
                    )
                }

                CompositionLocalProvider(
                    LocalCardDecorator provides cardDecorator,
                    LocalListDecorator provides listDecorator
                ) {
                    MaterialTheme(colors = iapColors) {
                        Surface(color = MaterialTheme.colors.background,
                            modifier = Modifier.nestedScroll(
                                rememberViewInteropNestedScrollConnection())) {
                            DependencyList(
                                state = dependencyViewModel.state.value,
                                onTriggerEvent = dependencyViewModel::onTriggerEvent,
                                styleData = styleData
                            )
                        }
                    }
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                dependencyViewModel.getThirdPartyDependencies()
            }
        }
    }

    override fun shouldDisplayToolbar(): Boolean = true

    override fun onBackPressed(): Boolean = false

    override fun onAppBarPressed() {}
}

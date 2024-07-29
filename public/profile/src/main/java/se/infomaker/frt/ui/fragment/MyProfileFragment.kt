package se.infomaker.frt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.lightColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.consent.ConsentManager
import com.navigaglobal.mobile.consent.ConsentManagerProvider
import com.navigaglobal.mobile.profile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.NavigationChromeOwner
import se.infomaker.iap.theme.ktx.backgroundColor
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.iap.theme.ktx.brandVariantColor
import se.infomaker.iap.theme.ktx.onBrandColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.profile.data.ConfigLoaderUtil
import se.infomaker.profile.data.ConsentItemConfig
import se.infomaker.profile.data.MyProfileConfig
import se.infomaker.profile.data.ProfileItemConfig
import se.infomaker.profile.data.ProfileListEvent
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.Section
import se.infomaker.profile.data.SectionFooterItemConfig
import se.infomaker.profile.data.SectionHeaderItemConfig
import se.infomaker.profile.view.ProfileList
import se.infomaker.profile.view.items.authentication.data.AuthenticationItemViewModel
import se.infomaker.profile.view.items.common.CardDecorator
import se.infomaker.profile.view.items.common.ListDecorator
import se.infomaker.profile.view.items.common.LocalCardDecorator
import se.infomaker.profile.view.items.common.LocalListDecorator
import se.infomaker.profile.view.items.common.LocalModuleInfo
import se.infomaker.profile.view.items.common.LocalSectionDecorator
import se.infomaker.profile.view.items.common.ModuleInfo
import se.infomaker.profile.view.items.common.SectionDecorator
import se.infomaker.profile.view.items.common.rememberViewInteropNestedScrollConnection
import se.infomaker.utilities.ProfileViewModelFactory
import se.infomaker.utilities.toComposeColor
import se.infomaker.utilities.toDp

@FlowPreview
@ExperimentalMaterialApi
class MyProfileFragment : BaseModule() {

    val theme by theme()
    var view: ComposeView? = null

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(
            requireActivity().application,
            moduleIdentifier
        )
    }

    private val authenticationViewModel: AuthenticationItemViewModel by viewModels {
        ProfileViewModelFactory(
            requireActivity().application,
            moduleIdentifier
        )
    }

    private val configToList: Flow<MyProfileConfig?> = flow {
        emit(MyProfileConfig(emptyList()))
        val config = getModuleConfig(MyProfileConfig::class.java, ConfigLoaderUtil.customGson)
            .insertSectionHeaders()
            .stripConsentItemsIfUnavailable(ConsentManagerProvider.provide(requireContext()))
        emit(config)
    }.flowOn(Dispatchers.IO)

    @FlowPreview
    private fun loadProfileConfig() {
        CoroutineScope(Dispatchers.Main).launch {
            configToList.collect {
                profileViewModel.configUpdated(ProfileListEvent.UpdateConfig(config = it
                    ?: MyProfileConfig(emptyList())))
            }
        }
    }

    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        loadProfileConfig()
        activity?.collapsingToolbarLayoutParams()?.scrollFlags = DEFAULT_SCROLL_FLAGS
        StatisticsManager.getInstance().logEvent(
            StatisticsEvent.Builder()
                .viewShow()
                .moduleId(moduleIdentifier)
                .moduleName(moduleName)
                .moduleTitle(moduleTitle)
                .viewName("profile")
                .build()
        )

        return inflater.inflate(R.layout.compose_profile_layout, container, false)?.apply {
            findViewById<ComposeView>(R.id.profile_view)?.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    val moduleInfo = ModuleInfo(id = moduleIdentifier)

                    val sectionDecorator = SectionDecorator (
                        paddingTop = theme.getSize("profileSectionPaddingTop", null)?.toDp ?: SectionDecorator.DEFAULT_PADDING,
                        paddingBottom = theme.getSize("profileSectionPaddingBottom", null)?.toDp ?: SectionDecorator.DEFAULT_PADDING,
                    )

                    val cardDecorator = CardDecorator(
                        elevationColor = theme.getColor("profileSectionSeparator", null)?.toComposeColor ?: CardDecorator.DEFAULT_COLOR,
                        elevationThickness = theme.getSize("profileSectionSeparatorThickness", null)?.toDp ?: CardDecorator.DEFAULT_THICKNESS,
                        marginBottom = theme.getSize("profileSectionMarginBottom", null)?.toDp ?: CardDecorator.DEFAULT_MARGIN,
                    )

                    val listDecorator =
                        ListDecorator(top = theme.getSize("profileListMarginTop", null)?.toDp
                            ?: ListDecorator.DEFAULT_MARGIN,
                            bottom = theme.getSize("profileListMarginBottom", null)?.toDp
                                ?: ListDecorator.DEFAULT_MARGIN
                        )

                    val backgroundColor = if (moduleIdentifier != null) theme.getColor("${moduleIdentifier}Background", theme.backgroundColor) else theme.backgroundColor
                    val iapColors = remember {
                        lightColors(
                            primary = theme.brandColor.toComposeColor,
                            primaryVariant = theme.brandVariantColor.toComposeColor,
                            background = backgroundColor.toComposeColor,
                            onPrimary = theme.onBrandColor.toComposeColor,
                        )
                    }

                    CompositionLocalProvider(
                        LocalModuleInfo provides moduleInfo,
                        LocalCardDecorator provides cardDecorator,
                        LocalListDecorator provides listDecorator,
                        LocalSectionDecorator provides sectionDecorator,

                        /**
                         * Compose Material 1.1 broke our initial profile designs with their introduction of
                         * minimum touch targets (48dpx48dp), for some items.
                         *
                         * That Compose change is in and of itself not a bad things, it just didn't work for us
                         * at the moment.
                         *
                         * So this is a quick fix provided by the Compose team:
                         * https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#LocalMinimumTouchTargetEnforcement()
                         *
                         * Source:
                         * https://twitter.com/Lojanda/status/1492081683511877654
                         */
                        LocalMinimumTouchTargetEnforcement provides false
                    ) {
                        MaterialTheme(colors = iapColors) {
                            Surface(color = iapColors.background,
                                modifier = Modifier.nestedScroll(
                                    rememberViewInteropNestedScrollConnection())) {
                                ProfileList(viewModel = profileViewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun MyProfileConfig?.insertSectionHeaders(): MyProfileConfig? {
        this?.sections?.filterNot { it -> it.profileItemConfigs?.isEmpty() ?: true }?.forEach { section ->
            section.profileItemConfigs?.add(0, SectionHeaderItemConfig(title = section.title))

            val insertIndex = section.profileItemConfigs?.let { configs ->
                configs.size.takeIf { it > 1 }
            } ?: 1

            section.profileItemConfigs?.apply {
                add(insertIndex, SectionFooterItemConfig())
            }
        }
        return this
    }

    private fun MyProfileConfig?.stripConsentItemsIfUnavailable(consentManager: ConsentManager?): MyProfileConfig? {
        val sections = mutableListOf<Section>()
        this?.sections?.forEach { section ->
            val profileItems = mutableListOf<ProfileItemConfig>()
            section.profileItemConfigs?.filterNotTo(profileItems) { it -> it is ConsentItemConfig && consentManager == null }
            sections.add(section.copy(profileItemConfigs = profileItems))
        }
        return this?.copy(sections = sections)
    }
    
    override fun shouldDisplayToolbar(): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean = false

    override fun onAppBarPressed() {}

    private fun FragmentActivity.collapsingToolbarLayoutParams(): AppBarLayout.LayoutParams? {
        return ((this as? NavigationChromeOwner)?.collapsingToolbarLayout?.layoutParams as? AppBarLayout.LayoutParams)
    }

    companion object {
        private const val DEFAULT_SCROLL_FLAGS =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    }
}
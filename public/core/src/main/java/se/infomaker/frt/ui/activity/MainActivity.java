package se.infomaker.frt.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.navigaglobal.mobile.R;
import com.navigaglobal.mobile.consent.ConsentCompleteListener;
import com.navigaglobal.mobile.consent.ConsentManager;
import com.navigaglobal.mobile.consent.ConsentManagerProvider;
import com.navigaglobal.mobile.databinding.ActivityMainBinding;
import com.netcore.android.Smartech;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.hansel.hanselsdk.Hansel;
import io.reactivex.Observable;
import kotlin.Unit;
import se.infomaker.frt.MenuHandlerActionHandler;
import se.infomaker.frt.moduleinterface.AppBarElevationHandler;
import se.infomaker.frt.moduleinterface.BaseModule;
import se.infomaker.frt.moduleinterface.HostInterface;
import se.infomaker.frt.moduleinterface.ModuleInterface;
import se.infomaker.frt.moduleinterface.action.GlobalActionHandler;
import se.infomaker.frt.remotenotification.notification.NotificationIntentFactory;
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler;
import se.infomaker.frt.ui.MenuHandler;
import se.infomaker.frt.ui.fragment.PaywallWrapperFragment;
import se.infomaker.frt.ui.view.RightCropImageView;
import se.infomaker.frt.ui.view.extensions.BottomNavigationViewExtensionsKt;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.GlobalValueManager;
import se.infomaker.frtutilities.MainMenuConfig;
import se.infomaker.frtutilities.MainMenuItem;
import se.infomaker.frtutilities.NavigationChromeOwner;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.mainmenutoolbarsettings.ToolbarConfig;
import se.infomaker.frtutilities.mainmenutoolbarsettings.ToolbarConfig.Position;
import se.infomaker.iap.action.ActionManager;
import se.infomaker.iap.action.Operation;
import se.infomaker.iap.theme.OnThemeUpdateListener;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.theme.view.ThemeableImageView;
import se.infomaker.iap.ui.fragment.FragmentPresenter;
import se.infomaker.iap.ui.promotion.PromotionManager;

@AndroidEntryPoint
public class MainActivity extends BaseActivity implements HostInterface, FragmentPresenter, OnThemeUpdateListener, NavigationChromeOwner, ConsentCompleteListener {

    public static final int DEFAULT_SCROLL_FLAGS = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
    public static final int TRANSPARENT_TOOLBAR_SCROLL_FLAGS = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
    private static final int POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 897345;

    private static final String INSTANCE_STATE_CURRENT_MENU_ITEM_ID = "currentMenuItemId";
    private static final String PROMOTION = "promotion";

    private final Map<String, Integer> drawableIdentifiers = new HashMap<>();

    private ActivityMainBinding binding;

    private MainMenuConfig mModulesConfig;
    private MainMenuItem mSelectedMenuItem;

    private ResourceManager mResourceManager;

    private MenuHandler mMenuHandler;
    private MenuHandlerActionHandler mMenuHandlerActionHandler;

    @Inject Map<String, OnNotificationInteractionHandler> notificationHandlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initMenuConfig();
        initToolbar();
        initBranding();



        MenuHandler menuHandler = new MenuHandler(this, binding.bottomNavigation, binding.navigationView, binding.drawerLayout, binding.toolbar, mModulesConfig.getMainMenuItems());
        mMenuHandler = menuHandler;
        mMenuHandlerActionHandler = new MenuHandlerActionHandler(menuHandler);

        MainMenuItem menuItem = menuHandler.getCurrentMenuItem();
        Bundle extras = null;

        if (savedInstanceState != null) {
            String moduleId = savedInstanceState.getString(INSTANCE_STATE_CURRENT_MENU_ITEM_ID);
            menuItem = menuHandler.getMenuItemFromModuleId(moduleId);
        } else {
            String moduleId = getIntent().getStringExtra(NotificationIntentFactory.MODULE_ID);

            Map<String, String> notificationBundle = (HashMap<String, String>) getIntent().getSerializableExtra(NotificationIntentFactory.NOTIFICATION_DATA);
            if (notificationBundle != null) {
                if (moduleId != null) {
                    menuItem = menuHandler.getMenuItemFromModuleId(moduleId);
                }
                extras = getIntent().getExtras();

                OnNotificationInteractionHandler handler = notificationHandlers.get(moduleId);

                if (handler != null) {
                    handler.handleOpenNotification(this, notificationBundle);
                }
            }
        }
        if (menuItem != null) {
            menuHandler.setSelectedItem(menuItem);
            selectMenuItem(menuItem, true, extras);
        }
        menuHandler.addSelectMenuItemListener(this::selectMenuItem);
        ThemeManager.getInstance(this).getAppTheme().apply(binding.bottomStickyAdWrapper);
    }

    private void handleConsent() {
        ConsentManager consentManager = ConsentManagerProvider.INSTANCE.provide(this);
        if (consentManager != null) {
            consentManager.presentConsentForm(this);
        }
        else {
            handlePostConsentActions();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActionManager.INSTANCE.register("core-select-module", mMenuHandlerActionHandler);
        ThemeManager.getInstance(this).addOnUpdateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActionManager.INSTANCE.unregister("core-select-module", mMenuHandlerActionHandler);
        ThemeManager.getInstance(this).removeOnUpdateListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(INSTANCE_STATE_CURRENT_MENU_ITEM_ID, mSelectedMenuItem.getId());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        ActionBarDrawerToggle drawerToggle = mMenuHandler.getDrawerToggle();
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        ActionBarDrawerToggle drawerToggle = mMenuHandler.getDrawerToggle();
        if (drawerToggle != null) {
            mMenuHandler.getDrawerToggle().onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMenuConfig() {
        mModulesConfig = ConfigManager.getInstance(this).getMainMenuConfig();
        mResourceManager = new ResourceManager(this, "shared");
    }

    private void setTopBarAndToolbar(MainMenuItem menuItem, boolean shouldDisplayToolbar) {
        String module = menuItem.getId();
        Theme theme = ThemeManager.getInstance(this).getModuleTheme(module);

        setToolbarSettings(menuItem, theme, shouldDisplayToolbar);
        ThemeUtils.apply(theme, getWindow());
    }

    private void setToolbarSettings(MainMenuItem menuItem, Theme theme, boolean shouldDisplayToolbar) {
        setSupportActionBar(binding.toolbar);

        if (!shouldDisplayToolbar || menuItem.getToolbarConfig().visibility() == ToolbarConfig.ToolbarVisibility.HIDDEN) {
            ((AppBarLayout.LayoutParams) binding.collapsingToolbarLayout.getLayoutParams()).setScrollFlags(0);
            binding.collapsingToolbarLayout.setVisibility(CollapsingToolbarLayout.GONE);
            return;
        }

        paintToolbar(theme);

        if (menuItem.getToolbarConfig().visibility() == ToolbarConfig.ToolbarVisibility.TRANSPARENT) {
            CoordinatorLayout.LayoutParams cParams = (CoordinatorLayout.LayoutParams) binding.contentFrame.getLayoutParams();
            cParams.setBehavior(null);
            binding.contentFrame.requestLayout();

            OneShotPreDrawListener.add(binding.appBarLayout, () -> ViewCompat.setElevation(binding.appBarLayout, 0f));
            binding.appBarLayout.setBackgroundColor(Color.TRANSPARENT);

            getSupportActionBar().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.transparent_toolbar));
            ((AppBarLayout.LayoutParams) binding.collapsingToolbarLayout.getLayoutParams()).setScrollFlags(TRANSPARENT_TOOLBAR_SCROLL_FLAGS);

            binding.collapsingToolbarLayout.setContentScrim(new ColorDrawable(Color.TRANSPARENT));
            binding.collapsingToolbarLayout.setContentScrimColor(Color.TRANSPARENT);
        } else {
            CoordinatorLayout.LayoutParams cParams = (CoordinatorLayout.LayoutParams) binding.contentFrame.getLayoutParams();
            cParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            binding.contentFrame.requestLayout();

            handleNonTransparentAppBarElevation(menuItem);

            ((AppBarLayout.LayoutParams) binding.collapsingToolbarLayout.getLayoutParams()).setScrollFlags(DEFAULT_SCROLL_FLAGS);
        }

        layoutToolbarTitle(menuItem);
        updateToolbarLogo(menuItem);
        layoutToolbarLogo(menuItem);
        paintToolbarButtons(menuItem, theme);
        binding.collapsingToolbarLayout.setVisibility(CollapsingToolbarLayout.VISIBLE);
        binding.toolbar.setOnClickListener(v -> onAppBarPressed());
    }

    private void handleNonTransparentAppBarElevation(MainMenuItem menuItem) {
        if (mCurrentFragment instanceof AppBarElevationHandler) {
            ((AppBarElevationHandler) mCurrentFragment).setOnAppBarElevationChanged(elevation -> {
                if (mCurrentFragment instanceof AppBarElevationHandler) {
                    OneShotPreDrawListener.add(binding.appBarLayout, () -> ViewCompat.setElevation(binding.appBarLayout, elevation));
                }
                return Unit.INSTANCE;
            });
        }
        else {
            float elevation;
            if (menuItem.getToolbarConfig().visibility() == ToolbarConfig.ToolbarVisibility.FLAT) {
                elevation = 0f;
            }
            else {
                elevation = dpToPx(4f);
            }
            OneShotPreDrawListener.add(binding.appBarLayout, () -> ViewCompat.setElevation(binding.appBarLayout, elevation));
        }
    }

    private void paintToolbar(Theme theme) {
        theme.apply(binding.toolbar);
        binding.toolbarTitle.apply(theme);
        binding.toolbarTitleLogo.apply(theme);
        ThemeColor toolbarColor = theme.getColor("toolbarColor", null);
        if (toolbarColor != null) {
            binding.collapsingToolbarLayout.setContentScrim(new ColorDrawable(toolbarColor.get()));
            binding.collapsingToolbarLayout.setContentScrimColor(toolbarColor.get());
        }
    }

    private void layoutToolbarLogo(MainMenuItem menuItem) {
        Position logoPosition = menuItem.getToolbarConfig().getLogoPosition();
        if (logoPosition == ToolbarConfig.Position.NONE) {
            binding.toolbarLogo.setVisibility(View.GONE);
        } else {
            binding.toolbarLogo.setVisibility(View.VISIBLE);
            ((Toolbar.LayoutParams) binding.toolbarLogo.getLayoutParams()).gravity = convertToGravity(logoPosition);
        }
    }

    private void layoutToolbarTitle(MainMenuItem menuItem) {
        Position titlePosition = menuItem.getToolbarConfig().getTitlePosition();
        if (titlePosition == ToolbarConfig.Position.NONE) {
            binding.toolbarTitle.setVisibility(View.GONE);
            binding.toolbarTitleLogo.setVisibility(View.GONE);
        } else {
            setToolbarLogoOrTitle(menuItem);
            int gravity = convertToGravity(titlePosition);
            ((Toolbar.LayoutParams) binding.toolbarTitle.getLayoutParams()).gravity = gravity;
            ((Toolbar.LayoutParams) binding.toolbarTitleLogo.getLayoutParams()).gravity = gravity;
        }
    }

    private int convertToGravity(ToolbarConfig.Position position) {
        switch (position) {
            case RIGHT:
                return Gravity.END;
            case CENTER:
                return Gravity.CENTER;
            case NONE:
            case LEFT:
            default:
                return Gravity.START;
        }
    }

    List<View> toolbarButtonViews = new ArrayList<>();

    private void paintToolbarButtons(MainMenuItem menuItem, Theme theme) {
        //TODO: reuse icons if we already have some to use
        for (View view : toolbarButtonViews) {
            binding.toolbar.removeView(view);
        }
        toolbarButtonViews.clear();

        // Reverse the order of buttons to get a natural order from config.
        List<ToolbarConfig.ButtonConfig> buttons = new ArrayList<>(menuItem.getToolbarConfig().getButtons());
        Collections.reverse(buttons);

        Observable.fromIterable(buttons)
                .filter(buttonConfig -> buttonConfig.getIcon() != null)
                .forEach(buttonConfig -> {
                    ThemeableImageView icon = new ThemeableImageView(MainActivity.this);
                    icon.setImageDrawable(getDrawable(buttonConfig.getIcon()));
                    icon.setThemeTintColor("toolbarAction");
                    toolbarButtonViews.add(icon);

                    // Get selectable background
                    TypedValue typedValue = new TypedValue();
                    getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true);
                    icon.setBackgroundResource(typedValue.resourceId);

                    if (buttonConfig.getClick() != null) {
                        icon.setOnClickListener(v -> {
                                    Operation operation = Operation.create(buttonConfig.getClick(), null, GlobalValueManager.INSTANCE.getGlobalValueManager(this));
                                    GlobalActionHandler.getInstance().perform(MainActivity.this, operation);
                                }
                        );
                    }

                    int padding = (int) dpToPx(12);
                    icon.setPadding(padding, padding, padding, padding);
                    int size = (int) dpToPx(48);
                    Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(size, size);
                    layoutParams.gravity = convertToGravity(buttonConfig.getPosition());
                    icon.setLayoutParams(layoutParams);

                    icon.apply(theme);
                    binding.toolbar.addView(icon);
                });
    }

    private void onAppBarPressed() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            if (currentFragment instanceof ModuleInterface) {
                ((ModuleInterface) currentFragment).onAppBarPressed();
            }
        }
    }

    public float dpToPx(float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = size * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        setTitle(null);
        binding.collapsingToolbarLayout.setTitleEnabled(false);
    }

    @Nullable
    private Drawable getDrawable(String identifierKey) {
        int identifier;
        if (!drawableIdentifiers.containsKey(identifierKey)) {
            identifier = mResourceManager.getDrawableIdentifier(identifierKey);
            drawableIdentifiers.put(identifierKey, identifier);
        } else {
            identifier = drawableIdentifiers.get(identifierKey);
        }
        if (identifier != 0) {
            return AppCompatResources.getDrawable(this, identifier);
        }
        return null;
    }

    private void setToolbarLogoOrTitle(MainMenuItem mainMenuItem) {
        Drawable drawable = null;
        if (mainMenuItem.getId() != null) {
            drawable = getDrawable(mainMenuItem.getId() + "_logo");
        }

        // If logo is specified, use that. Otherwise use title.
        if (drawable != null) {
            binding.toolbarTitle.setText(null);
            binding.toolbarTitle.setVisibility(View.GONE);
            binding.toolbarTitleLogo.setImageDrawable(drawable);
            binding.toolbarTitleLogo.setVisibility(View.VISIBLE);
        } else {
            binding.toolbarTitle.setText(mainMenuItem.getToolbarTitle());
            binding.toolbarTitle.setVisibility(View.VISIBLE);
            binding.toolbarTitleLogo.setImageDrawable(null);
            binding.toolbarTitleLogo.setVisibility(View.GONE);
        }
    }

    private void initBranding() {
        Theme appTheme = ThemeManager.getInstance(this).getAppTheme();
        ThemeColor drawerBackgroundColor = appTheme.getColor("drawerBackgroundColor", ThemeColor.WHITE);
        binding.navigationView.setBackgroundColor(drawerBackgroundColor.get());

        FrameLayout navHeader = (FrameLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.nav_header, binding.navigationView, false);
        TextView navHeaderTextView = navHeader.findViewById(R.id.nav_header_text_view);
        ImageView navHeaderLogoImageView = navHeader.findViewById(R.id.nav_header_logo_image_view);
        int navHeaderLogoId = mResourceManager.getDrawableIdentifier("navdrawer_logo");
        if (navHeaderLogoId != 0) {
            navHeaderTextView.setVisibility(View.GONE);
            navHeaderLogoImageView.setImageDrawable(AppCompatResources.getDrawable(this, navHeaderLogoId));
            navHeaderLogoImageView.setVisibility(View.VISIBLE);
        } else {
            navHeaderLogoImageView.setVisibility(View.GONE);
            navHeaderTextView.setText(mResourceManager.getString("core_app_name", ""));
            navHeaderTextView.setVisibility(View.VISIBLE);
        }
        RightCropImageView navHeaderImageView = navHeader.findViewById(R.id.nav_header_image_view);
        int headerId = mResourceManager.getDrawableIdentifier("navdrawer_header");
        if (headerId != 0) {
            navHeaderImageView.setImageDrawable(AppCompatResources.getDrawable(this, headerId));
        }
        binding.navigationView.addHeaderView(navHeader);

        ColorStateList colorTintStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_enabled},
                {android.R.attr.state_checked},
                new int[]{}
        }, new int[]{
                ThemeUtils.getTextColor(appTheme).get(),
                ThemeUtils.getBrandColor(appTheme).get(),
                ThemeUtils.getTextColor(appTheme).get()
        });
        binding.navigationView.setItemTextColor(colorTintStateList);
        binding.navigationView.setItemIconTintList(colorTintStateList);
    }

    private void updateToolbarLogo(MainMenuItem menuItem) {
        ResourceManager resourceManager = new ResourceManager(this, menuItem.getId());
        int logoId = resourceManager.getDrawableIdentifier(menuItem.getToolbarConfig().getLogoResource());
        if (logoId != 0) {
            binding.toolbarLogo.setBackground(AppCompatResources.getDrawable(this, logoId));
            binding.toolbarLogo.setVisibility(View.VISIBLE);
        } else {
            binding.toolbarLogo.setVisibility(View.GONE);
        }
    }

    /**
     * Call this method when a {@link MainMenuItem} has been selected to open it.
     *
     * @param menuItem the menu item that was selected
     * @param topLevel     is the menuItem a top level item, if it is not, we open the fragment in a new
     *                     activity
     * @param extras bundle values passed as arguments
     */
    public void selectMenuItem(MainMenuItem menuItem, boolean topLevel, Bundle extras) {
        if (mModulesConfig != null) {
            if (menuItem == null) {
                return;
            }

            if (!topLevel) {
                binding.drawerLayout.closeDrawers();
                Intent intent = SingleMenuItemActivity.createIntent(this, menuItem);
                startActivity(intent);
                return;
            }

            Fragment fragment = null;
            if (menuItem.getRequiresPermission() != null) {
                fragment = PaywallWrapperFragment.create(menuItem, extras);
            }
            else {
                Fragment current = getSupportFragmentManager().findFragmentByTag(CURRENT_FRAGMENT);
                if (current instanceof BaseModule) {
                    String currentModuleIdentifier = ((BaseModule) current).getModuleIdentifier();
                    String currentModuleName = ((BaseModule) current).getModuleName();
                    if (currentModuleIdentifier != null && currentModuleIdentifier.equals(menuItem.getId()) &&
                            currentModuleName != null && currentModuleName.equals(menuItem.getModuleName())) {
                        fragment = current;
                    }
                }

                if (fragment == null) {
                    fragment = FragmentHelper.createModuleFragment(this, menuItem, extras);
                }
            }

            if (fragment != null) {
                if (menuItem.getPromotion() != null && getSupportFragmentManager().findFragmentById(R.id.top_level_frame) == null) {
                    boolean presented = PromotionManager.getInstance(this).promote(this, menuItem.getId(), "shared/configuration/" + menuItem.getPromotion());
                    if (!presented) {
                        handleConsent();
                    }
                }
                else {
                    handleConsent();
                }

                binding.appBarLayout.setExpanded(true);
                boolean shouldDisplayToolbar = !(fragment instanceof ModuleInterface) || ((ModuleInterface) fragment).shouldDisplayToolbar();
                if (shouldDisplayToolbar) {
                    binding.toolbar.setVisibility(View.VISIBLE);
                }
                else {
                    binding.toolbar.setVisibility(View.GONE);
                }

                //NOTE: Would be nice to load this in onFragmentDismissed if promote is shown
                // Insert the fragment by replacing any existing fragment
                if (mSelectedMenuItem != menuItem) {
                    binding.tabLayout.setVisibility(View.GONE);
                    binding.tabLayout.setupWithViewPager(null);
                    switchFragment(fragment);
                    setTopBarAndToolbar(menuItem, shouldDisplayToolbar);
                }
                mSelectedMenuItem = menuItem;
            }
            binding.drawerLayout.closeDrawers();
        }
    }

    /**
     * Call this method when a {@link MainMenuItem} has been selected to open it.
     *
     * @param mainMenuItem the menu item that was selected
     * @param topLevel     is the menuItem a top level item, if it is not, we open the fragment in a new
     *                     activity
     */
    public void selectMenuItem(MainMenuItem mainMenuItem, boolean topLevel) {
        selectMenuItem(mainMenuItem, topLevel, null);
    }

    public MainMenuConfig getMainMenuConfig() {
        return mModulesConfig;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Needed to call onActivityResult on child fragments
        getCurrentFragment().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            if (currentFragment instanceof ModuleInterface) {
                if (((ModuleInterface) currentFragment).onBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    public void openNavigationDrawer() {
        runOnUiThread(() -> binding.drawerLayout.openDrawer(GravityCompat.START));
    }

    @Override
    public void onFragmentDismissed(Fragment fragment) {
        handleConsent();

        String moduleId = null;
        if (mSelectedMenuItem != null) {
            moduleId = mSelectedMenuItem.getId();
        }
        Theme theme = ThemeManager.getInstance(this).getModuleTheme(moduleId);
        ThemeUtils.apply(theme, getWindow());
    }

    @Override
    public void presentFullScreen(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().addToBackStack(PROMOTION).replace(R.id.top_level_frame, fragment, PROMOTION).commitAllowingStateLoss();
    }

    @Override
    public void onThemeUpdated() {
        if (mSelectedMenuItem != null) {
            setTopBarAndToolbar(mSelectedMenuItem, shouldDisplayToolbar());
            mMenuHandler.reload();
        }
    }

    private boolean shouldDisplayToolbar() {
        return !(getCurrentFragment() instanceof ModuleInterface) || ((ModuleInterface) getCurrentFragment()).shouldDisplayToolbar();
    }

    @NonNull
    @Override
    public AppBarLayout getAppBarLayout() {
        return binding.appBarLayout;
    }

    @NonNull
    @Override
    public CollapsingToolbarLayout getCollapsingToolbarLayout() {
        return binding.collapsingToolbarLayout;
    }

    @Override
    public void expandNavigationChrome() {
        binding.appBarLayout.setExpanded(true);
        BottomNavigationViewExtensionsKt.slideUp(binding.bottomNavigation);
    }

    @Override
    public void onConsentFormPresentationComplete() {
        handlePostConsentActions();
    }

    private void handlePostConsentActions() {
        handlePostNotificationsPermission();
    }

    private void handlePostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE) {
            /*
             * Well, we don't really care at this point.. If anything the classes responsible to show notifications
             * should worry about this.
             *
             * I'm leaving this callback here, should this change in the future.
             */
        }
    }
}

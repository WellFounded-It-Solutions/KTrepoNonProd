package se.infomaker.livecontentui.livecontentrecyclerview.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.NavigationChromeOwner;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import com.navigaglobal.mobile.livecontent.R;
import com.navigaglobal.mobile.livecontent.databinding.TranslucentAppBarBinding;

import se.infomaker.livecontentui.MenuActivity;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.LiveContentRecyclerViewFragment;
import se.infomaker.livecontentui.livecontentrecyclerview.view.ContentInsetProvider;
import se.infomaker.livecontentui.livecontentrecyclerview.view.OnContentInsetsChangedListener;
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator;
import se.infomaker.livecontentui.offline.OfflineBannerOwner;
import se.infomaker.livecontentui.offline.TransparentOfflineBannerLayout;
import se.infomaker.livecontentui.view.appbar.TranslucentAppBarCoordinator;

@AndroidEntryPoint
public class LiveContentRecyclerviewActivity extends MenuActivity implements OfflineBannerOwner, NavigationChromeOwner, ContentInsetProvider {
    public static final String CURRENT_FRAGMENT = "currentFragment";
    public static final String STATS_EXTRAS_KEY = "statsExtras";

    private Fragment mCurrentFragment;
    private String moduleId;
    private LiveContentUIConfig config;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private OfflineBannerCoordinator offlineBannerCoordinator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        moduleId = getIntent().getStringExtra("moduleId");
        ResourceManager resourceManager = new ResourceManager(this, moduleId);
        String moduleName = getIntent().getStringExtra("moduleName");

        config = ConfigManager.getInstance(getApplicationContext()).getConfig(moduleName, moduleId, LiveContentUIConfig.class);
        if (config.getTranslucentToolbar()) {
            setContentView(R.layout.activity_contentlist_translucent);

            TransparentOfflineBannerLayout offlineBannerLayout = findViewById(R.id.offline_banner);
            offlineBannerCoordinator = new OfflineBannerCoordinator(offlineBannerLayout, resourceManager);
            getLifecycle().addObserver(offlineBannerCoordinator);

            CoordinatorLayout root = findViewById(R.id.root_coordinator);
            TranslucentAppBarBinding appBarBinding = TranslucentAppBarBinding.bind(root);
            TranslucentAppBarCoordinator translucentAppBarCoordinator = new TranslucentAppBarCoordinator(root, appBarBinding);
            getLifecycle().addObserver(translucentAppBarCoordinator);
        } else {
            setContentView(R.layout.activity_contentlist);
        }

        appBarLayout = findViewById(R.id.app_bar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        Theme theme = ThemeManager.getInstance(this).getModuleTheme(moduleId);
        initToolbar(resourceManager, theme, toolbar, toolbarTitle);
        theme.apply(findViewById(android.R.id.content));

        mCurrentFragment = getSupportFragmentManager().findFragmentByTag(CURRENT_FRAGMENT);
        if (mCurrentFragment == null) {
            mCurrentFragment = createFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, mCurrentFragment, CURRENT_FRAGMENT).commit();
        }
    }

    protected Fragment createFragment() {
        LiveContentRecyclerViewFragment fragment = new LiveContentRecyclerViewFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment != null)
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }

    private void initToolbar(ResourceManager resourceManager, Theme theme, Toolbar toolbar, @Nullable TextView toolbarTitle) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (toolbarTitle != null) {
            String title = getTitleFromIntent();
            toolbarTitle.setText(TextUtils.isEmpty(title) ? ModuleInformationManager.getInstance().getModuleTitle(moduleId) : title);
        }
        final Drawable up = getResources().getDrawable(resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow));
        if (config.getTranslucentToolbar()) {
            up.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        } else {
            up.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
        }
        getSupportActionBar().setHomeAsUpIndicator(up);

        setBlackStatusbar();
    }

    private int resourceWithFallback(ResourceManager resourceManager, String resourceName, int fallback){
        int identifier = resourceManager.getDrawableIdentifier(resourceName);
        return identifier != 0 ? identifier : fallback;
    }

    private void setBlackStatusbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    private String getTitleFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            if (title != null) {
                return title;
            }
        }
        return "";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public OfflineBannerCoordinator getOfflineBannerCoordinator() {
        return offlineBannerCoordinator;
    }

    @NonNull
    @Override
    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    @NonNull
    @Override
    public CollapsingToolbarLayout getCollapsingToolbarLayout() {
        return collapsingToolbarLayout;
    }

    @Override
    public void expandNavigationChrome() {
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true);
        }
    }

    @Override
    public void addOnContentInsetChangedListener(OnContentInsetsChangedListener contentInsetsChangedListener) {
        if (offlineBannerCoordinator != null) {
            offlineBannerCoordinator.addOnContentInsetsChangedListener(contentInsetsChangedListener);
        }
    }

    @Override
    public void removeOnContentInsetChangedListener(OnContentInsetsChangedListener contentInsetsChangedListener) {
        if (offlineBannerCoordinator != null) {
            offlineBannerCoordinator.removeOnContentInsetsChangedListener(contentInsetsChangedListener);
        }
    }
}

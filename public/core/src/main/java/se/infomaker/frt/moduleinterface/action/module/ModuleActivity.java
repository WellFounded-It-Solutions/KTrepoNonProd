package se.infomaker.frt.moduleinterface.action.module;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frt.moduleinterface.ModuleInterface;
import com.navigaglobal.mobile.R;
import se.infomaker.frt.moduleinterface.behaviour.DesiredCollapsingToolbarLayoutBehaviour;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.theme.util.UI;
import timber.log.Timber;

@AndroidEntryPoint
public class ModuleActivity extends AppCompatActivity {
    public static final String FRAGMENT_TAG = "modularFragment";
    public static final String MODULE_NAME = "moduleName";

    private Fragment mCurrentFragment;
    private Toolbar mToolbar;
    private String mModuleId;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolbarLayout;
    private TextView mToolbarTitle;
    private Theme mTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moduleinterface_module_activity);
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        mToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        mModuleId = getIntent().getStringExtra(Module.MODULE_ID);
        mTheme = ThemeManager.getInstance(this).getModuleTheme(mModuleId);

        mCurrentFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (mCurrentFragment == null) {
            try {
                mCurrentFragment = createModuleFragment(getIntent());
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.content_frame, mCurrentFragment, FRAGMENT_TAG).commit();
            } catch (InvalidModuleException e) {
                finish();
                return;
            }
        }
        initToolbar();
        ThemeUtils.apply(mTheme, this);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        String title = getTitleFromIntent();
        mToolbarTitle.setText(title != null ? title : ModuleInformationManager.getInstance().getModuleTitle(mModuleId));

        Drawable up = AppCompatResources.getDrawable(this, getDrawableIdentifier(new ResourceManager(this, mModuleId), "action_up", R.drawable.up_arrow));
        DrawableCompat.setTint(up, mTheme.getColor("toolbarAction", ThemeUtils.getToolbarActionColor(mTheme)).get());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(up);

        if (shouldPresentFlatToolbar()) {
            OneShotPreDrawListener.add(mAppBarLayout, () -> ViewCompat.setElevation(mAppBarLayout, UI.dp2px(0f)));
        }
        else {
            OneShotPreDrawListener.add(mAppBarLayout, () -> ViewCompat.setElevation(mAppBarLayout, UI.dp2px(4f)));
        }

        mToolbar.setOnClickListener(v -> onAppBarPressed());
    }

    private int getDrawableIdentifier(ResourceManager resourceManager, String resourceName, int fallbackIdentifier) {
        int identifier = resourceManager.getDrawableIdentifier(resourceName);
        return identifier != 0 ? identifier : fallbackIdentifier;
    }

    private void onAppBarPressed() {
        if (mCurrentFragment instanceof ModuleInterface) {
            ((ModuleInterface) mCurrentFragment).onAppBarPressed();
        }
    }

    public Fragment createModuleFragment(Intent intent) throws InvalidModuleException {
        Fragment fragment;
        String moduleName = intent.getStringExtra(MODULE_NAME);
        String className = Module.fullModuleFragmentClassName(moduleName);
        try {
            Class.forName(className);
            fragment = Fragment.instantiate(getApplicationContext(), className);
        } catch (ClassNotFoundException e) {
            throw new InvalidModuleException(e);
        }

        Bundle bundle = new Bundle(intent.getExtras());

        if (!bundle.containsKey(Module.MODULE_ID)) {
            throw new InvalidModuleException("No module id");
        }
        fragment.setArguments(bundle);

        if (!(fragment instanceof ModuleInterface)) {
            Timber.w("Module does not implement ModuleInterface.");
        }

        return fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment != null)
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment instanceof ModuleInterface) {
            if (!((ModuleInterface)mCurrentFragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentFragment instanceof DesiredCollapsingToolbarLayoutBehaviour) {
            ((DesiredCollapsingToolbarLayoutBehaviour) mCurrentFragment).updateBehaviour(mToolbarLayout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private boolean shouldPresentFlatToolbar() {
        Intent intent = getIntent();
        if (intent != null) {
            String toolbar = intent.getStringExtra("toolbar");
            return "flat".equals(toolbar);
        }
        return false;
    }
}

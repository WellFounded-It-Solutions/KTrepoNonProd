package se.infomaker.frt.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.navigaglobal.mobile.R;

import se.infomaker.frtutilities.MainMenuItem;
import se.infomaker.frtutilities.ModuleInformation;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.ktx.ThemeUtils;

public class SingleMenuItemActivity extends AppCompatActivity {
    private static final String ARG_MODULE_ID = "moduleId";
    private static final String ARG_MODULE_TITLE = "moduleTitle";
    private static final String ARG_MODULE_NAME = "moduleTitle";
    private static final String ARG_MODULE_PROMOTION = "moduleTitle";
    private Fragment currentFragment;
    private Toolbar toolbar;

    public static Intent createIntent(Context context, MainMenuItem mainMenuItem) {
        Intent intent = new Intent(context, SingleMenuItemActivity.class);
        intent.putExtra(ARG_MODULE_ID, mainMenuItem.getId());
        intent.putExtra(ARG_MODULE_TITLE, mainMenuItem.getTitle());
        intent.putExtra(ARG_MODULE_NAME, mainMenuItem.getModuleName());
        intent.putExtra(ARG_MODULE_PROMOTION, mainMenuItem.getPromotion());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_menu_item);
        ResourceManager resourceManager = new ResourceManager(this, "shared");
        toolbar = findViewById(R.id.toolbar);

        //Set the toolbar logo
        ImageView toolbarLogo = findViewById(R.id.toolbar_logo);
        int logoId = resourceManager.getDrawableIdentifier("toolbar_logo");
        if (logoId != 0) {
            toolbarLogo.setBackground(ContextCompat.getDrawable(getApplicationContext(), logoId));
            toolbarLogo.setVisibility(View.VISIBLE);
        } else {
            toolbarLogo.setVisibility(View.GONE);
        }

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);

        Theme appTheme = ThemeManager.getInstance(this).getAppTheme();
        supportActionBar.setBackgroundDrawable(appTheme.getColor("toolbarColor", ThemeUtils.getChromeColor(appTheme)).asDrawable());

        ModuleInformation moduleInformation = getModuleInfoFromIntent(getIntent());
        setToolbarLogoOrTitle(moduleInformation, resourceManager);

        currentFragment = getSupportFragmentManager().findFragmentByTag("currentFragment");
        if (currentFragment == null) {
            currentFragment = FragmentHelper.createModuleFragment(this, moduleInformation, null);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, currentFragment, "currentFragment").commit();
        }
    }

    private ModuleInformation getModuleInfoFromIntent(Intent intent) {
        String moduleId = intent.getStringExtra(ARG_MODULE_ID);
        String moduleTitle = intent.getStringExtra(ARG_MODULE_TITLE);
        String moduleName = intent.getStringExtra(ARG_MODULE_NAME);
        String modulePromotion = intent.getStringExtra(ARG_MODULE_PROMOTION);
        return new ModuleInformation(moduleId, moduleTitle, moduleName, modulePromotion);
    }

    private void setToolbarLogoOrTitle(ModuleInformation moduleInformation, ResourceManager resourceManager) {
        // If logo is specified, use that. Otherwise use title.
        int identifier = 0;
        if (moduleInformation.getIdentifier() != null) {
            identifier = resourceManager.getDrawableIdentifier(moduleInformation.getIdentifier() + "_logo");
        }

        if (identifier > 0) {
            setTitle(null);
            toolbar.setLogo(identifier);
        } else {
            setTitle(moduleInformation.getTitle());
            toolbar.setLogo(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

package se.infomaker.frt.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;

import org.jetbrains.annotations.Nullable;

import se.infomaker.frt.moduleinterface.ModuleInterface;
import se.infomaker.frtutilities.MainMenuItem;
import se.infomaker.frtutilities.ModuleInformation;
import se.infomaker.frtutilities.ktx.ContextUtils;
import timber.log.Timber;

public class FragmentHelper {
    private static final String MODULE_FRAGMENT_PACKAGE_NAME = "se.infomaker.frt.ui.fragment";
    private static final String MODULE_FRAGMENT_SUFFIX = "Fragment";

    @Nullable
    public static Fragment createModuleFragment(Context context, MainMenuItem mainMenuItem) {
        return createModuleFragment(context, mainMenuItem, null);
    }

    public static Fragment createModuleFragment(Context context, MainMenuItem mainMenuItem, Bundle extras) {
        Timber.e("FragmentHelper CreateModuleFragment MainMenuItem: %s, ID: %s, Title: %s, ModuleName: %s, Promotion: %s", mainMenuItem, mainMenuItem.getId(), mainMenuItem.getTitle(), mainMenuItem.getModuleName(), mainMenuItem.getPromotion());
        ModuleInformation moduleInformation = new ModuleInformation(mainMenuItem.getId(), mainMenuItem.getTitle(), mainMenuItem.getModuleName(), mainMenuItem.getPromotion());
        return createModuleFragment(context, moduleInformation, extras);
    }

    public static Fragment createModuleFragment(Context context, ModuleInformation moduleInformation, Bundle extras) {
        Fragment fragment = null;
        String className = MODULE_FRAGMENT_PACKAGE_NAME + "." + moduleInformation.getName() + MODULE_FRAGMENT_SUFFIX;
        try {
            Activity activity = ContextUtils.requireActivity(context);
            if (activity instanceof FragmentActivity) {
                FragmentFactory factory = ((FragmentActivity) activity).getSupportFragmentManager().getFragmentFactory();
                fragment = factory.instantiate(ClassLoader.getSystemClassLoader(), className);
            }
        } catch (Fragment.InstantiationException e) {
            //My class isn't there!
            Timber.e(e, "Fragment with that name does not exist.");
        }

        if (fragment != null) {
            Bundle bundle = new Bundle();
            if(extras != null) {
                bundle.putAll(extras);
            }

            bundle.putString("moduleId", moduleInformation.getIdentifier());
            bundle.putString("promotion", moduleInformation.getPromotion());

            fragment.setArguments(bundle);

            if (!(fragment instanceof ModuleInterface)) {
                Timber.w("Module does not implement ModuleInterface.");
            }
        }
        return fragment;
    }
}

package se.infomaker.frt.moduleinterface.action.module;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import timber.log.Timber;

public class Module {

    public static final String MODULE_ID = "moduleId";
    private static final String MODULE_FRAGMENT_PACKAGE_NAME = "se.infomaker.frt.ui.fragment";
    private static final String MODULE_FRAGMENT_SUFFIX = "Fragment";

    public static void open(Context context, String moduleName, Bundle arguments) throws InvalidModuleException {
        if (!isValid(moduleName)) {
            throw new InvalidModuleException(moduleName + " does not exist");
        }

        Timber.e("Module.java ModuleName: %s, Arguments: %s", moduleName, arguments);

        Intent intent = new Intent(context, ModuleActivity.class);
        intent.putExtra(ModuleActivity.MODULE_NAME, moduleName);
        intent.putExtras(arguments);
        context.startActivity(intent);
    }

    public static boolean isValid(String moduleName) {
        String className = fullModuleFragmentClassName(moduleName);
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @NonNull
    public static String fullModuleFragmentClassName(String moduleName) {
        return MODULE_FRAGMENT_PACKAGE_NAME + "." + moduleName + MODULE_FRAGMENT_SUFFIX;
    }
}

package se.infomaker.frt.moduleinterface.action.module;

import android.content.Context;
import android.os.Bundle;

import java.util.Iterator;

import se.infomaker.frt.moduleinterface.action.ActionHandler;
import se.infomaker.iap.action.Operation;
import timber.log.Timber;

import static se.infomaker.frt.moduleinterface.action.module.Module.MODULE_ID;

public class ModuleActionHandler implements ActionHandler {
    public static final String OPEN_MODULE = "openModule";

    @Override
    public String perform(Context context, Operation operation) {
        Timber.d("ModuleActionHandler OpenModule Operation: %s", operation);
        switch (operation.getAction()) {
            case OPEN_MODULE: {
                Bundle bundle = new Bundle();

                Iterator<String> iterator = operation.getParameters().keys();
                while(iterator.hasNext()) {
                    String key = iterator.next();
                    bundle.putString(key, operation.getParameter(key));
                }
                // Module is defined by id externally but moduleId internally -> remapping
                if (!bundle.containsKey(MODULE_ID)) {
                    bundle.putString(MODULE_ID, bundle.getString("id"));
                }
                try {
                    Module.open(context, operation.getParameter(ModuleActivity.MODULE_NAME), bundle);
                } catch (InvalidModuleException e) {
                    Timber.e(e, "Trying to open invalid module");
                    return null;
                }
                return "";
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public boolean canPerform(Context context, Operation operation) {
        return OPEN_MODULE.equals(operation.getAction()) && Module.isValid(operation.getParameter(ModuleActivity.MODULE_NAME));
    }
}

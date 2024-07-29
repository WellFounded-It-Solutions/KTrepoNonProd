package se.infomaker.frt.moduleinterface.action;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import se.infomaker.frt.moduleinterface.action.module.ModuleActionHandler;
import se.infomaker.iap.action.Operation;
import se.infomaker.iap.action.Result;

public class GlobalActionHandler implements ActionHandler, ActionHandlerCollection {

    private Map<String, ActionHandler> handlers = new HashMap<>();
    private static GlobalActionHandler INSTANCE = new GlobalActionHandler();

    public static GlobalActionHandler getInstance() {
        return INSTANCE;
    }

    private GlobalActionHandler() {
        register(ModuleActionHandler.OPEN_MODULE, new ModuleActionHandler());
    }

    public void register(String action, ActionHandler actionHandler) {
        handlers.put(action, actionHandler);
    }

    @Override
    public String perform(Context context, Operation operation) {
        ActionHandler handler = handlers.get(operation.getAction());
        if (handler != null) {
            return handler.perform(context, operation);
        } else {
            operation.perform(context, result -> null);
            return null;
        }
    }

    @Override
    public boolean canPerform(Context context, Operation operation) {
        ActionHandler handler = handlers.get(operation.getAction());
        return handler != null && handler.canPerform(context, operation);
    }
}

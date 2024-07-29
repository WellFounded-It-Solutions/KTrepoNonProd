package se.infomaker.frt.moduleinterface.action;

import android.content.Context;

public interface ActionHandlerRegister {
    /**
     * Callback where a ActionHandlerRegister register action handlers for operations
     * @param actionHandler
     */
    void registerActions(Context context, ActionHandlerCollection actionHandler);
}

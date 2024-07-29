package se.infomaker.frt.moduleinterface.action;

import android.content.Context;

import se.infomaker.iap.action.Operation;

public interface ActionHandler {
    /**
     * Performs an operation, if the operation has a return value it is sent back as a String
     */
    String perform(Context context, Operation operation);

    /**
     * Returns true if the interface can perform the operation
     */
    boolean canPerform(Context context, Operation operation);
}

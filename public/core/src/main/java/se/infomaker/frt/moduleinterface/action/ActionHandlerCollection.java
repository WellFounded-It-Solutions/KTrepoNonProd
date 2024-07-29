package se.infomaker.frt.moduleinterface.action;

public interface ActionHandlerCollection {
    /**
     * Register an action handler for an operation name
     * @param operationName to map to the action handler
     * @param actionHandler that handles the operation
     */
    void register(String operationName, ActionHandler actionHandler);
}

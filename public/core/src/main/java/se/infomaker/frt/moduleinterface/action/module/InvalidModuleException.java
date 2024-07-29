package se.infomaker.frt.moduleinterface.action.module;

class InvalidModuleException extends Exception {
    public InvalidModuleException(Exception e) {
        super(e);
    }

    public InvalidModuleException(String s) {
        super(s);
    }
}

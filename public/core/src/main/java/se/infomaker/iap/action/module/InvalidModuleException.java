package se.infomaker.iap.action.module;

public class InvalidModuleException extends Exception {
    public InvalidModuleException(Exception e) {
        super(e);
    }

    public InvalidModuleException(String s) {
        super(s);
    }
}

package se.infomaker.livecontentui.livecontentrecyclerview.view;

/**
 *  Allow modification of bindings 
 */
public interface OverridableBinding {

    /**
     * Replace the current keyPath with another
     * @param keyPath
     */
    void overrideBinding(String keyPath);
}

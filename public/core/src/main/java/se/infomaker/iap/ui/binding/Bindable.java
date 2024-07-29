package se.infomaker.iap.ui.binding;

/**
 * An object that can be bound content in the form of a string value
 */
public interface Bindable {
    /**
     * Binds the provided value
     * @param value to bind
     */
    void bind(String value);

    /**
     * Clears any bound value
     */
    void clear();
}

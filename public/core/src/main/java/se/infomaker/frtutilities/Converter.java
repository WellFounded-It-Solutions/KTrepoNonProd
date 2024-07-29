package se.infomaker.frtutilities;

/**
 * Convert between <R,T>
 */
public interface Converter<R,T> {
    /**
     * Convert an object to another type
     * @param object to convert
     * @return Converted object
     */
    T convert(R object);
}

package se.infomaker.iap.theme.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SetUtil {
    /**
     * Convert iterator to set
     * @param iterator to convert
     * @param <T> of type
     * @return A set of type
     */
    public static  <T> Set<T> setFrom(Iterator<T> iterator) {
        HashSet<T> set = new HashSet<>();
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }
}

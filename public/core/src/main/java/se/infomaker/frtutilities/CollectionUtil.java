package se.infomaker.frtutilities;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionUtil {

    /**
     * A list of parameter values
     *
     * @param values
     * @return a list of parameter values,
     */
    @NonNull
    public static <T> List<T> asList(@NonNull T... values) {
        if (values.length == 0) {
            return Collections.emptyList();
        }
        ArrayList<T> out = new ArrayList<>();
        for (T value : out) {
            out.add(value);
        }
        return out;
    }

    public static List<String> convertToStrings(@NonNull List list) {
        return convert(list, Object::toString);
    }

    /**
     * Converts a list of objects using a converter
     * @param list to convert
     * @return converted list
     */
    public static <T> List<T> convert(@NonNull List list, @NonNull Converter<Object,T> converter) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<T> out = new ArrayList<>();
        for (Object o : list) {
            out.add(converter.convert(o));
        }
        return out;
    }
}

package se.infomaker.iap.theme.debug;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

public class DebugUtil {
    public static void enableDrawOutside(View view) {
        ViewParent parent = view.getParent();
        if (parent != null) {
            ViewGroup viewGroup = (ViewGroup) parent;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
            ViewParent outerViewGroup = viewGroup.getParent();
            if (outerViewGroup != null) {
                ((ViewGroup)outerViewGroup).setClipChildren(false);
                ((ViewGroup)outerViewGroup).setClipToPadding(false);
            }
        }
    }

    public static void writeGroups(StringBuilder builder,String prefix, List<String> values, boolean ignoreEmpty) {
        if (values == null || values.size() == 0) {
            if (ignoreEmpty) {
                return;
            }
            builder.append(prefix).append("<undefined>");
        }
        else {
            builder.append(prefix);
            for (String themeKey : values) {
                builder.append(themeKey);
                if (!themeKey.equals(values.get(values.size() - 1))) {
                    builder.append(", ");
                }
            }
        }
    }

    public static void write(StringBuilder builder, String prefix, String value, boolean ignoreEmpty) {
        if (TextUtils.isEmpty(value)) {
            if (ignoreEmpty) {
                return;
            }
            builder.append(prefix).append("<undefined>");
        }
        else {
            builder.append(prefix).append(value);
        }
    }
}

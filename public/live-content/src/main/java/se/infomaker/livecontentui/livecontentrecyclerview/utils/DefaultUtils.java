package se.infomaker.livecontentui.livecontentrecyclerview.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.infomaker.livecontentui.config.BindingOverride;
import se.infomaker.livecontentui.config.TemplateConfig;
import se.infomaker.livecontentui.livecontentrecyclerview.view.OverridableBinding;
import timber.log.Timber;

public class DefaultUtils {

    public static int dp2px(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int px2dp(Context context, int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static String getStringFromAssetsFile(Context context, String filename) {
        StringBuilder buf = new StringBuilder();
        try {
            String str = "";
            InputStream json = context.getAssets().open(filename);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();

        } catch (IOException e) {
            Timber.e(e, null);
        }
        return buf.toString();
    }

    public static List<View> getAllChildren(View v) {
        HashSet<View> objects = new HashSet<>();
        addAllChildrenUnique(v, objects);
        return new ArrayList<>(objects);
    }

    public static Map<String,View> getNamedChildren(View v) {
        HashSet<View> objects = new HashSet<>();
        addAllChildrenUnique(v, objects);
        HashMap<String, View> map = new HashMap<>();
        for (View child : objects) {
            if (child.getId() != 0 && child.getId() != 0xffffffff) {
                try {
                    String name = child.getResources().getResourceEntryName(child.getId());
                    map.put(name, child);
                }
                catch (Resources.NotFoundException exception) {
                    // Well not all resources could be found
                }

            }
        }
        return map;
    }

    public static void applyOverrides(TemplateConfig templateConfig, ViewGroup viewGroup) {
        if (templateConfig == null) {
            return;
        }
        applyOverrides(templateConfig.getBindingOverrides(), viewGroup);
    }

    public static void applyOverrides(List<BindingOverride> overrides, View view) {
        if (view instanceof ViewGroup) {
            applyOverrides(overrides, (ViewGroup) view);
        }
    }

    public static void applyOverrides(List<BindingOverride> overrides, ViewGroup viewGroup) {
        if (overrides == null || overrides.size() == 0) {
            return;
        }
        Map<String, View> namedChildren = DefaultUtils.getNamedChildren(viewGroup);

        for (BindingOverride override : overrides) {
            View view = namedChildren.get(override.getView());
            if (view instanceof OverridableBinding) {
                ((OverridableBinding) view).overrideBinding(override.getKeyPath());
            }
        }
    }

    private static void addAllChildrenUnique(View v, Set<View> set) {
        if (!(v instanceof ViewGroup)) {
            set.add(v);
            return;
        }
        ViewGroup viewGroup = (ViewGroup) v;
        set.add(viewGroup);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            set.add(v);
            addAllChildrenUnique(child, set);
        }
    }

    public static String uncapFirstChar(String s) {
        if (s != null && s.length() > 0) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        } else {
            return s;
        }
    }

    public static String capitFirstChar(String s) {
        if (s != null && s.length() > 0) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        } else {
            return s;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.setDensity((int) Resources.getSystem().getDisplayMetrics().density);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static boolean isOnMainThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }

    /**
     * @deprecated Don't do this. The {@link CollapsingToolbarLayout} passed in here used to be something
     *             else before migrating to AndroidX.
     */
    @Deprecated
    public static void setRefreshToolbarEnable(CollapsingToolbarLayout collapsingToolbarLayout, boolean refreshToolbarEnable) {
        try {
            Field field = CollapsingToolbarLayout.class.getDeclaredField("mRefreshToolbar");
            field.setAccessible(true);
            field.setBoolean(collapsingToolbarLayout, refreshToolbarEnable);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Timber.e(e);
        }
    }
}

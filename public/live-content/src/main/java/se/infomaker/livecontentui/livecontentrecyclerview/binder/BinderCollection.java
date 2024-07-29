package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;

public class BinderCollection implements ViewBinder {

    private Map<Class, ViewBinder> binders = new HashMap<>();

    private BinderCollection() {
    }

    public static BinderCollection with(ViewBinder... viewBinders) {
        BinderCollection collection = new BinderCollection();
        for (ViewBinder binder : viewBinders) {
            collection.add(binder);
        }
        return collection;
    }

    private void add(ViewBinder binder) {
        for (Class clazz : binder.supportedViews()) {
            binders.put(clazz, binder);
        }
    }

    @Override
    public LiveBinding bind(View view, String value, PropertyObject properties) {
        ViewBinder binder = binders.get(view.getClass());
        if (binder != null) {
            return binder.bind(view, value, properties);
        }
        return null;
    }

    @Override
    public Set<Class> supportedViews() {
        return binders.keySet();
    }

    @Override
    public String getKey(View view) {
        ViewBinder binder = binders.get(view.getClass());
        if (binder != null) {
            return binder.getKey(view);
        }
        return null;
    }
}

package se.infomaker.iap.ui.binding;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class BindableProvider {
    private final Map<Class, BindableFactory> factories = new HashMap<>();

    public BindableProvider(BindableFactory... factories) {
        for (BindableFactory factory : factories) {
            register(factory);
        }
    }

    public Bindable makeBindable(View view){
        return factories.get(findSupportedParent(view)).create(view);
    }

    public void register(BindableFactory factory) {
        for (Class aClass : factory.supported()) {
            factories.put(aClass, factory);
        }
    }

    public boolean supported(View view){
        return findSupportedParent(view) != null;
    }

    public Class findSupportedParent(View view) {
        Class clazz = view.getClass();
        do {
            if (factories.containsKey(clazz)) {
                return clazz;
            }
        } while ((clazz = clazz.getSuperclass()) != View.class);
        return null;
    }
}

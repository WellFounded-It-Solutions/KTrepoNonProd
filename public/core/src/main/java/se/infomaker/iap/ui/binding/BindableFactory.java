package se.infomaker.iap.ui.binding;

import android.view.View;

import java.util.Set;

@SuppressWarnings("WeakerAccess")
public interface BindableFactory {
    Bindable create(View view);

    Set<Class> supported();
}

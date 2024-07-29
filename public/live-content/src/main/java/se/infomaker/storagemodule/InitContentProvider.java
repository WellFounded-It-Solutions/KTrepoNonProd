package se.infomaker.storagemodule;

import android.content.Context;

import androidx.annotation.NonNull;

import se.infomaker.frtutilities.AbstractInitContentProvider;

public class InitContentProvider extends AbstractInitContentProvider {

    @Override
    public void init(@NonNull Context context) {
        Storage.initialize(context);
    }
}

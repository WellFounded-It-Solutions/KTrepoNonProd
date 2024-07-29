package se.infomaker.iap.theme;

import android.view.View;
import android.view.ViewGroup;

import se.infomaker.iap.theme.view.Themeable;

abstract class BaseTheme implements Theme {

    @Override
    public void apply(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                this.apply(((ViewGroup) view).getChildAt(i));
            }
        }
        if (view instanceof Themeable) {
            ((Themeable) view).apply(this);
        }
    }
}

package se.infomaker.livecontentui.livecontentrecyclerview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigaglobal.mobile.livecontent.R;

import se.infomaker.iap.theme.view.ThemeableFrameLayout;


public class IMFrameLayout extends ThemeableFrameLayout implements OverridableBinding {
    private boolean showOnFalse;
    private String propertyKey;
    private String goneOnMissing;

    public IMFrameLayout(@NonNull Context context) {
        super(context);
    }

    public IMFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public IMFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.IMFrameLayout);
        propertyKey = typedArray.getString(R.styleable.IMFrameLayout_propertyKey);
        showOnFalse = typedArray.getBoolean(R.styleable.IMFrameLayout_showOnFalse, false);
        goneOnMissing = typedArray.getString(R.styleable.IMFrameLayout_goneOnMissing);
        typedArray.recycle();
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getGoneOnMissing() {
        return goneOnMissing;
    }

    public boolean isShowOnFalse() {
        return showOnFalse;
    }

    @Override
    public void overrideBinding(String bindKeyPath) {
        propertyKey = bindKeyPath;
    }
}

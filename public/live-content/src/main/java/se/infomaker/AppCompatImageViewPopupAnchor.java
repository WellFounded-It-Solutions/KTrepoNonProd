package se.infomaker;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class AppCompatImageViewPopupAnchor extends AppCompatImageView {

    public AppCompatImageViewPopupAnchor(Context context) {
        super(context);
    }

    public AppCompatImageViewPopupAnchor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppCompatImageViewPopupAnchor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        return false;
    }
}

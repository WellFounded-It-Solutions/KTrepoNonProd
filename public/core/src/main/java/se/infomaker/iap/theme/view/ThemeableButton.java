package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.navigaglobal.mobile.R;
import se.infomaker.iap.theme.util.UI;

public class ThemeableButton extends ThemeableMaterialButton{

    private boolean roundedCorners;

    public ThemeableButton(Context context) {
        super(context);
        init(null);
    }

    public ThemeableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray buttonTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableButton);
            roundedCorners = buttonTypedArray.getBoolean(R.styleable.ThemeableButton_roundedCorners, false);
            buttonTypedArray.recycle();
        }

        int paddingExtra = (int) UI.dp2px(10);
        setPadding(getPaddingLeft(), getPaddingTop() + paddingExtra, getPaddingRight(), getPaddingBottom() + paddingExtra);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (roundedCorners) {
            updateCornerRadius(h);
        }
    }

    private void updateCornerRadius(int h) {
        setCornerRadius((int) (h/2.0f));
    }

    public boolean isRoundedCorners() {
        return roundedCorners;
    }

    public void setRoundedCorners(boolean roundedCorners) {
        if (roundedCorners) {
            updateCornerRadius(getHeight());
        }
        this.roundedCorners = roundedCorners;
    }
}

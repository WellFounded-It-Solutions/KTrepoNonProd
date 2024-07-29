package se.infomaker.iap.ui.promotion.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.navigaglobal.mobile.R;
import com.rd.PageIndicatorView;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.view.Themeable;
import se.infomaker.iap.ui.promotion.ThemeKeys;

/**
 * Page indicator view that supports themeable
 */
public class ThemeablePageIndicatorView extends PageIndicatorView implements Themeable{

    private static final ThemeColor defaultIndicatorColor = new ThemeColor(Color.GRAY);

    private String selectedColor = ThemeKeys.INDICATOR_SELECTED;
    private String unselectedColor = ThemeKeys.INDICATOR_UNSELECTED;

    public ThemeablePageIndicatorView(Context context) {
        super(context);
    }

    public ThemeablePageIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeablePageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ThemeablePageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeablePageIndicatorView);
        selectedColor = typedArray.getString(R.styleable.ThemeablePageIndicatorView_themeSelected);
        unselectedColor = typedArray.getString(R.styleable.ThemeablePageIndicatorView_themeUnselected);
        typedArray.recycle();
        if (selectedColor == null) {
            selectedColor = ThemeKeys.INDICATOR_SELECTED;
        }
        if (unselectedColor == null) {
            unselectedColor = ThemeKeys.INDICATOR_UNSELECTED;
        }
    }

    @Override
    public void apply(Theme theme) {
        ThemeColor selected = theme.getColor(selectedColor, defaultIndicatorColor);
        setSelectedColor(selected.get());
        setUnselectedColor(theme.getColor(unselectedColor, selected).get());
    }

    public String getSelectedThemeColor() {
        return selectedColor;
    }

    public void setSelectedThemeColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    public String getUnselectedThemeColor() {
        return unselectedColor;
    }

    public void setUnselectedThemeColor(String unselectedColor) {
        this.unselectedColor = unselectedColor;
    }
}

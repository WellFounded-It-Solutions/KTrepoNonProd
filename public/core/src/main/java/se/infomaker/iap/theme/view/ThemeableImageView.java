package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.imageview.ShapeableImageView;
import com.navigaglobal.mobile.R;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.image.ThemeImage;
import se.infomaker.iap.theme.util.UI;

public class ThemeableImageView extends ShapeableImageView implements Themeable{
    private final List<String> themeKeys = new ArrayList<>();
    private final List<String> themeTouchColors = new ArrayList<>();
    private final List<String> themeTintColors = new ArrayList<>();
    private int fallbackDrawable;

    private final DebugPainter debugPainter = new DebugPainter(DebugPainter.GREEN, DebugPainter.Position.TOP_RIGHT);
    private boolean showDebug = false;

    public ThemeableImageView(Context context) {
        super(context);
    }

    public ThemeableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableImageView);
        setThemeKey(typedArray.getString(R.styleable.ThemeableImageView_themeKey));
        setThemeTouchColor(typedArray.getString(R.styleable.ThemeableImageView_themeTouchColor));
        setThemeTintColor(typedArray.getString(R.styleable.ThemeableImageView_themeTint));
        fallbackDrawable = typedArray.getResourceId(R.styleable.ThemeableImageView_fallbackImage, 0);
        typedArray.recycle();
    }

    public int getFallbackDrawable() {
        return fallbackDrawable;
    }

    public void setFallbackDrawable(int fallbackDrawable) {
        this.fallbackDrawable = fallbackDrawable;
    }

    @Override
    public void apply(Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            setupDebugMessage();
            DebugUtil.enableDrawOutside(this);
        }
        ThemeImage image = theme.getImage(themeKeys, null);
        if (image != null) {
            Drawable drawable = image.getImage(this.getContext());
            setImageDrawable(drawable);
        }
        else if(fallbackDrawable != 0) {
            setImageResource(fallbackDrawable);
        }

        if (!themeTouchColors.isEmpty()) {
            UI.setTouchFeedback(this, getThemeColor(theme, themeTouchColors, ThemeColor.WHITE).get());
        }

        if (!themeTintColors.isEmpty()) {
            ThemeColor themeColor = getThemeColor(theme, themeTintColors, null);
            if (themeColor != null) {
                setColorFilter(themeColor.get());
            }
            else {
                clearColorFilter();
            }
        }
    }

    private void setupDebugMessage() {
        if (showDebug) {
            StringBuilder builder = new StringBuilder();
            DebugUtil.writeGroups(builder, "I: ", themeKeys, true);
            DebugUtil.writeGroups(builder, " TC: ", themeTouchColors, true);
            DebugUtil.writeGroups(builder, " TiC: ", themeTintColors, true);
            debugPainter.setDebugMessage(builder.toString());
        }
    }

    private ThemeColor getThemeColor(Theme theme, List<String> colors, ThemeColor fallback) {
        for (String color : colors) {
            ThemeColor result = ThemeableUtil.getThemeColor(theme, color, null);
            if (result != null) {
                return result;
            }
        }
        return fallback;
    }

    public String getThemeKey() {
        return themeKeys.size() > 0 ? themeKeys.get(0) : null;
    }

    public String getThemeTouchColor() {
        return themeTouchColors.size() > 0 ? themeTouchColors.get(0) : null;
    }

    public String getThemeTintColors() {
        return themeTintColors.size() > 0 ? themeTintColors.get(0) : null;
    }

    public void setThemeKey(String themeKey) {
        themeKeys.clear();
        if (themeKey != null) {
            themeKeys.add(themeKey);
        }
    }

    public void setThemeKey(List<String> keys) {
        themeKeys.clear();
        if (keys != null) {
            themeKeys.addAll(keys);
        }
    }

    public void setThemeTouchColor(String themeTouchColor) {
        themeTouchColors.clear();
        if (themeTouchColor != null) {
            themeTouchColors.add(themeTouchColor);
        }
    }
    public void setThemeTouchColor(List<String> keys) {
        themeTouchColors.clear();
        if (keys != null) {
            themeTouchColors.addAll(keys);
        }
    }

    public void setThemeTintColor(String themeTintColor) {
        themeTintColors.clear();
        if (themeTintColor != null) {
            themeTintColors.add(themeTintColor);
        }
    }

    public void setThemeTintColor(List<String> keys) {
        themeTintColors.clear();
        if (keys != null) {
            themeTintColors.addAll(keys);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }
}

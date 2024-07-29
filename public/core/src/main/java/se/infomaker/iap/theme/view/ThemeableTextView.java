package se.infomaker.iap.theme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;

import com.navigaglobal.mobile.R;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.color.ThemeColorParser;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.util.UI;

public class ThemeableTextView extends AppCompatTextView implements Themeable {

    private List<String> themeKeys;
    private String themeTouchColor;
    private String themeBackgroundColor;
    private String themeLinkColor;
    private final DebugPainter debugPainter = new DebugPainter(DebugPainter.RED, DebugPainter.Position.ON_CONTENT);
    private boolean showDebug = false;

    public ThemeableTextView(Context context) {
        super(context);
    }

    public ThemeableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ThemeableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableTextView);

        String themeKey = typedArray.getString(R.styleable.ThemeableTextView_themeKey);
        if (themeKey != null) {
            themeKeys = new ArrayList<>();
            themeKeys.add(themeKey);
        }
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableTextView_themeBackgroundColor);
        themeTouchColor = typedArray.getString(R.styleable.ThemeableTextView_themeTouchColor);
        themeLinkColor = typedArray.getString(R.styleable.ThemeableTextView_themeLinkColor);

        int drawableLeftIdentifier = typedArray.getResourceId(R.styleable.ThemeableTextView_iapDrawableLeft, 0);
        int drawableRightIdentifier = typedArray.getResourceId(R.styleable.ThemeableTextView_iapDrawableRight, 0);

        Drawable left = null;
        Drawable right = null;
        if (drawableLeftIdentifier != 0) {
            left = AppCompatResources.getDrawable(context, drawableLeftIdentifier);
        }
        if (drawableRightIdentifier != 0) {
            right =
                    AppCompatResources.getDrawable(context, drawableRightIdentifier);
        }
        setDrawables(left, right);
        typedArray.recycle();
        updateDebugMessage();
    }

    private void updateDebugMessage() {
        StringBuilder builder = new StringBuilder();
        DebugUtil.writeGroups(builder, "T: ", themeKeys, false);
        debugPainter.setDebugMessage(builder.toString());
    }

    public void setDrawables(Drawable left, Drawable right) {
        setCompoundDrawablesWithIntrinsicBounds(left, null, right, null);
    }

    @Override
    public void apply(Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            DebugUtil.enableDrawOutside(this);
        }
        if (themeKeys != null) {
            ThemeTextStyle text = theme.getText(themeKeys, null);
            if (text != null) {
                text.apply(theme, this);
            }
        }
        if (!TextUtils.isEmpty(themeTouchColor)) {
            ThemeColor touchColor;
            try {
                touchColor = new ThemeColorParser().parseObject(themeTouchColor);
            } catch (AttributeParseException e) {
                touchColor = theme.getColor(themeTouchColor, ThemeColor.WHITE);
            }
            if (!TextUtils.isEmpty(themeBackgroundColor)) {
                UI.setTouchFeedback(this, ThemeableUtil.getThemeColor(theme, themeBackgroundColor, ThemeColor.TRANSPARENT).get() , touchColor.get());
            }
            else {
                UI.setTouchFeedback(this, touchColor.get());
            }
        }
        else if (!TextUtils.isEmpty(themeBackgroundColor)) {
            setBackgroundColor(ThemeableUtil.getThemeColor(theme, themeBackgroundColor, ThemeColor.TRANSPARENT).get());
        }
        if (!TextUtils.isEmpty(themeLinkColor)) {
            ThemeColor linkColor = theme.getColor(themeLinkColor, null);
            if (linkColor != null) {
                setLinkTextColor(linkColor.get());
            }
        }
        //TODO: Add support for autosize max font size
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDebug) {
            debugPainter.paint(canvas, this);
        }
    }

    public void setThemeKey(String themeKey) {
        themeKeys = new ArrayList<>();
        themeKeys.add(themeKey);
        updateDebugMessage();
    }

    public void setThemeKeys(List<String> themeKeys) {
        this.themeKeys = themeKeys;
        updateDebugMessage();
    }

    public void setThemeTouchColor(String themeTouchColor) {
        this.themeTouchColor = themeTouchColor;
    }

    public List<String> getThemeKeys() {
        return themeKeys;
    }

    public String getThemeTouchColor() {
        return themeTouchColor;
    }

    public void setThemeBackgroundColor(String themeBackgroundColor) {
        this.themeBackgroundColor = themeBackgroundColor;
    }

    public String getThemeBackgroundColor() {
        return themeBackgroundColor;
    }
}

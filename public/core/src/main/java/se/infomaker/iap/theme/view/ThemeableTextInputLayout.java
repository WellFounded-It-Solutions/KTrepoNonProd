package se.infomaker.iap.theme.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.ViewCompat;

import com.google.android.material.internal.CollapsingTextHelper;
import com.google.android.material.textfield.TextInputLayout;
import com.navigaglobal.mobile.R;

import java.lang.reflect.Field;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.debug.DebugPainter;
import se.infomaker.iap.theme.debug.DebugUtil;
import se.infomaker.iap.theme.extensions.ViewUtil;
import se.infomaker.iap.theme.font.ThemeFont;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import timber.log.Timber;

public class ThemeableTextInputLayout extends TextInputLayout implements Themeable {
    private String themeColor;
    private String themeErrorColor;
    private String themeKey;
    private String themeBackgroundColor;

    private DebugPainter debugPainter = new DebugPainter(DebugPainter.GREEN, DebugPainter.Position.TOP_RIGHT);
    private boolean showDebug = false;

    public ThemeableTextInputLayout(Context context) {
        super(context);
    }

    public ThemeableTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThemeableTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThemeableTextInputLayout);
        themeColor = typedArray.getString(R.styleable.ThemeableTextInputLayout_themeColor);
        themeErrorColor = typedArray.getString(R.styleable.ThemeableTextInputLayout_themeErrorColor);
        themeBackgroundColor = typedArray.getString(R.styleable.ThemeableTextInputLayout_themeBackgroundColor);
        themeKey = typedArray.getString(R.styleable.ThemeableTextInputLayout_themeKey);
        typedArray.recycle();
    }

    private void setupDebugMessage() {
        if (showDebug) {
            StringBuilder builder = new StringBuilder();
            DebugUtil.write(builder, "T: ", themeKey, true);
            DebugUtil.write(builder, " C: ", themeColor, true);
            DebugUtil.write(builder, " EC: ", themeErrorColor, true);
            DebugUtil.write(builder, " BC: ", themeBackgroundColor, true);
            debugPainter.setDebugMessage(builder.toString());
        }
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public void setThemeErrorColor(String themeErrorColor) {
        this.themeErrorColor = themeErrorColor;
    }

    public void setThemeKey(String themeKey) {
        this.themeKey = themeKey;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void apply(@NonNull Theme theme) {
        showDebug = ThemeManager.getInstance(getContext()).showDebug();
        if (showDebug) {
            setupDebugMessage();
            DebugUtil.enableDrawOutside(this);
        }

        EditText editText = getEditText();
        if (editText == null) {
            return;
        }

        if (themeKey != null) {
            ThemeTextStyle textStyle = theme.getText(themeKey, null);
            if (textStyle != null) {
                ThemeColor textColor = textStyle.getColor(theme);
                ThemeFont font = textStyle.getFont(theme);
                ThemeSize size = textStyle.getSize(theme);

                // Handle the hint
                setTypeface(font.getTypeface());

                // Handle hint text color
                int textColorInt = textColor.get();
                setDefaultHintTextColor(ColorStateList.valueOf(textColorInt));

                CollapsingTextHelper textHelper = getCollapsingTextHelper();
                if (textHelper != null) {
                    // Only mess with expanded text size
                    textHelper.setExpandedTextSize(size.getSizePx());
                }

                // Handle input text
                editText.setTextSize(size.getSize());
                editText.setTypeface(font.getTypeface());
                editText.setTextColor(textColorInt);

                // Best effort cursor coloring...
                ViewUtil.setCursorDrawableColor(editText, textColorInt);

                // Handle end icon tint color
                setEndIconTintList(ColorStateList.valueOf(textColorInt));

                // To remain backwards compatible
                setBoxColor(textColorInt);
            }
        }
        if (themeColor != null) {
            ThemeColor color = theme.getColor(themeColor, null);
            if (color != null) {
                int colorInt = color.get();
                setBoxColor(colorInt);
            }
        }
        if (themeErrorColor != null) {
            ThemeColor color = theme.getColor(themeErrorColor, null);
            if (color != null) {
                ColorStateList errorColorList = ColorStateList.valueOf(color.get());
                setErrorTextColor(errorColorList);
                setBoxStrokeErrorColor(errorColorList);
            }
        }
        if (themeBackgroundColor != null) {
            ThemeColor themeColor = theme.getColor(themeBackgroundColor, ThemeColor.TRANSPARENT);
            int backgroundColor = themeColor.get();
            setBoxBackgroundColor(backgroundColor);
        }
        else {
            setBoxBackgroundColor(Color.TRANSPARENT);
        }
    }

    @SuppressLint("RestrictedApi")
    private void setBoxColor(int color) {
        if (getBoxBackgroundMode() == TextInputLayout.BOX_BACKGROUND_NONE) {
            EditText editText = getEditText();
            if (editText != null) {
                ColorStateList colorStateList = ColorStateList.valueOf(color);
                ViewCompat.setBackgroundTintList(editText, colorStateList);
                // Urgh  || Fix to ensure color is applied to active state too.
                //       VV
                ((AppCompatEditText) editText).setSupportBackgroundTintList(colorStateList);
            }
        }
        else {
            /*
             * This is a deviation from the default material TextInputLayout behaviour.
             * Where the default only sets the color of the box when focused, we (currently)
             * want this color for the box in both focused and default state.
             */
            reallySetBoxStrokeColor(color);
        }
    }

    private CollapsingTextHelper getCollapsingTextHelper() {
        Field collapsingTextHelper;
        try {
            collapsingTextHelper = TextInputLayout.class.getDeclaredField("collapsingTextHelper");
            collapsingTextHelper.setAccessible(true);

            try {
                return (CollapsingTextHelper) collapsingTextHelper.get(this);
            }
            catch (IllegalAccessException e) {
                Timber.e(e, "Not allowed to access CollapsingTextHelper, themeing of ThemeableTextInputLayouts is probably broken...");
                return null;
            }
        }
        catch (NoSuchFieldException e) {
            Timber.e(e, "There is no CollapsingTextHelper, themeing of ThemeableTextInputLayouts is probably broken...");
            return null;
        }
    }

    private void reallySetBoxStrokeColor(int color) {

        // Set the color of the "focusedStrokeColor", no reflection.
        setBoxStrokeColor(color);

        // Set the color of the "defaultStrokeColor", reflection... :/
        try {
            Field field = TextInputLayout.class.getDeclaredField("defaultStrokeColor");
            field.setAccessible(true);
            field.set(this, color);
        }
        catch (Exception e) {
            Timber.e(e, "Could not set color with reflection");
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

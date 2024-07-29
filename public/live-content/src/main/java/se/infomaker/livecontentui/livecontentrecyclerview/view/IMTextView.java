package se.infomaker.livecontentui.livecontentrecyclerview.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.navigaglobal.mobile.livecontent.R;
import com.navigaglobal.mobile.livecontent.R.styleable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.view.ThemeableTextView;
import timber.log.Timber;


public class IMTextView extends ThemeableTextView implements OverridableBinding, OnContentInsetsChangedListener {

    private static final Map<String, Typeface> FONT_CACHE = new HashMap<>();

    private String prefixSuffix;
    private String prefixPrefix;
    private String prefixBindKeyPath;
    private String prefixFallbackBindKeyPath;
    private String prefixFallback;
    private String suffixPrefix;
    private String suffixBindKeyPath;
    private String suffixDelimiter;
    private String prefixDelimiter;
    private String bindKeyPath;
    private String prefixThemeKey;
    private String suffixThemeKey;
    private List<ThemeableSpan> spannables = new ArrayList<>();
    private SpannableStringBuilder builder = new SpannableStringBuilder();
    private Theme lastTheme;
    // Sets "plainText" to default.
    String outFormat;
    String textFormat = TextFormat.PLAIN_TEXT;
    private String prefixOutFormat;
    private String prefixTextFormat = TextFormat.PLAIN_TEXT;
    String textSuffix = "";
    String textPrefix = "";
    String fontType = "";
    String fallback;
    String fallbackBindKeyPath;
    List<String> mRequiredFields;
    private LiveBinding updater;
    private String resourceKey;
    private boolean contentInsetAware;
    private LeadingMarginHelper leadingMarginHelper;
    private String invisiblePrefix;

    public IMTextView(Context context) {
        super(context);
    }

    public IMTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.IMTextView);
        textFormat = typedArray.getString(R.styleable.IMTextView_textFormat);
        prefixTextFormat = typedArray.getString(R.styleable.IMTextView_prefixTextFormat);
        prefixOutFormat = typedArray.getString(R.styleable.IMTextView_prefixOutFormat);
        outFormat = typedArray.getString(R.styleable.IMTextView_outFormat);
        bindKeyPath = typedArray.getString(R.styleable.IMTextView_bindKeyPath);
        prefixBindKeyPath = typedArray.getString(R.styleable.IMTextView_prefixBindKeyPath);
        prefixFallbackBindKeyPath = typedArray.getString(R.styleable.IMTextView_prefixFallbackBindKeyPath);
        prefixFallback = typedArray.getString(R.styleable.IMTextView_prefixFallback);
        prefixSuffix = typedArray.getString(R.styleable.IMTextView_prefixSuffix);
        prefixPrefix = typedArray.getString(R.styleable.IMTextView_prefixPrefix);
        prefixThemeKey = typedArray.getString(R.styleable.IMTextView_prefixThemeKey);
        textPrefix = typedArray.getString(R.styleable.IMTextView_textPrefix);
        suffixBindKeyPath = typedArray.getString(R.styleable.IMTextView_suffixBindKeyPath);
        suffixPrefix = typedArray.getString(R.styleable.IMTextView_suffixPrefix);
        prefixDelimiter = typedArray.getString(R.styleable.IMTextView_textPrefixDelimiter);
        suffixDelimiter = typedArray.getString(R.styleable.IMTextView_textSuffixDelimiter);
        suffixThemeKey = typedArray.getString(R.styleable.IMTextView_suffixThemeKey);
        textSuffix = typedArray.getString(styleable.IMTextView_textSuffix);
        fontType = typedArray.getString(R.styleable.IMTextView_fontType);
        fallback = typedArray.getString(R.styleable.IMTextView_fallback);
        fallbackBindKeyPath = typedArray.getString(R.styleable.IMTextView_fallbackBindKeyPath);
        resourceKey = typedArray.getString(R.styleable.IMTextView_resourceKey);
        contentInsetAware = typedArray.getBoolean(R.styleable.IMTextView_contentInsetsAware, false);
        String leadingMarginProperty = typedArray.getString(R.styleable.IMTextView_leadingMarginProperty);
        int leadingMarginTargetId = typedArray.getResourceId(R.styleable.IMTextView_leadingMarginTarget, 0);
        int leadingMarginPadding = typedArray.getDimensionPixelSize(styleable.IMTextView_leadingMarginPadding, 0);
        leadingMarginHelper = new LeadingMarginHelper(leadingMarginProperty, leadingMarginTargetId, leadingMarginPadding, this::triggerInvisiblePrefixRemeasure);

        if (fontType != null) {
            Typeface typeface = loadTypeface(fontType);
            if (typeface != null) {
                setTypeface(typeface);
            }
        }

        if (textPrefix != null) {
            setTextPrefix(textPrefix);
        }

        if (textSuffix != null) {
            setTextSuffix(textSuffix);
        }
        typedArray.recycle();
    }

    @Nullable
    private Typeface loadTypeface(@NonNull String fontType) {
        synchronized (FONT_CACHE) {
            if (FONT_CACHE.containsKey(fontType)) {
                return FONT_CACHE.get(fontType);
            }
            try {
                final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "shared/fonts/" + fontType);
                FONT_CACHE.put(fontType, typeface);
                return typeface;
            }
            catch (Exception e) {
                Timber.w(e, "Failed to load font: %s", fontType);
                FONT_CACHE.put(fontType, null);
            }
        }
        return null;
    }

    public IMTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Activity activity = ContextUtils.findActivity(getContext());
        if (contentInsetAware && activity instanceof ContentInsetProvider) {
            ((ContentInsetProvider) activity).addOnContentInsetChangedListener(this);
        }
    }

    @Override
    public void onContentInsetsChanged(ContentInsets contentInsets) {

        if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            int originalTopMargin;
            Object savedMargin = getTag(R.id.IMTextView_original_top_margin);
            if (savedMargin instanceof Integer) {
                originalTopMargin = (int) savedMargin;
            }
            else {
                originalTopMargin = marginLayoutParams.topMargin;
                setTag(R.id.IMTextView_original_top_margin, originalTopMargin);
            }

            int targetMargin = originalTopMargin + contentInsets.getTop();

            if (ViewCompat.isLaidOut(this)) {
                int marginDifference = Math.abs(marginLayoutParams.topMargin - targetMargin);
                ValueAnimator valueAnimator = ValueAnimator.ofInt(marginLayoutParams.topMargin, targetMargin);
                valueAnimator.addUpdateListener(animator -> {
                    marginLayoutParams.topMargin = (int) animator.getAnimatedValue();
                    setLayoutParams(marginLayoutParams);
                });
                valueAnimator.setDuration(Math.max(marginDifference, 300));
                valueAnimator.start();
            }
            else {
                marginLayoutParams.topMargin = targetMargin;
                setLayoutParams(marginLayoutParams);
            }
        }
    }

    /**
     *
     * @return true if the view does not need to modify the content
     */
    private boolean isSimple() {
        return TextUtils.isEmpty(prefixDelimiter) && TextUtils.isEmpty(suffixDelimiter)
                && TextUtils.isEmpty(textPrefix) && TextUtils.isEmpty(textSuffix)
                && TextUtils.isEmpty(invisiblePrefix);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (type == BufferType.NORMAL && isSimple()) {
            super.setText(text, type);
            return;
        }

        /*
         * prefix/suffix can be determined using a delimiter eg. :
         */
        if (!TextUtils.isEmpty(prefixDelimiter) && text != null) {
            String[] parts = text.toString().split(prefixDelimiter);
            if (parts.length > 1) {
                textPrefix = parts[0] + prefixDelimiter;
                text = text.subSequence(parts[0].length() + prefixDelimiter.length(), text.length());
            } else {
                textPrefix = null;
            }
        }
        if (!TextUtils.isEmpty(suffixDelimiter) && text != null) {
            String[] parts = text.toString().split(suffixDelimiter);
            if (parts.length > 1) {
                textSuffix = parts[parts.length - 1];
                text = text.subSequence(text.length() - textSuffix.length(), text.length());
            } else {
                textSuffix = null;
            }
        }

        if (builder == null) {
            builder = new SpannableStringBuilder();
        }
        else {
            builder.clear();
        }

        if (invisiblePrefix != null && !isPrefixed(text, invisiblePrefix)) {
            builder.append(invisiblePrefix);
        }
        /*
         * The !isPrefixed-check is a workaround for issues when the OS calls setText() on us
         * with the same text as is already present in the view. This text might already contain
         * the prefix, if it does, don't add it again.
         *
         * This problem was discovered when applying a theme TextStyle with uppercase transform,
         * which internally causes the text to be modified and applied again using
         * #setText(CharSequence) with the uppercased string.
         *
         * This will cause trouble if we want to prefix a text that start with the exact same
         * string as the prefix... But we probably shouldn't do that anyways.
         */
        int prefixStart = 0;
        if (!TextUtils.isEmpty(invisiblePrefix) && text != null && text.toString().startsWith(invisiblePrefix)) {
            prefixStart += invisiblePrefix.length();
        }
        if (textPrefix != null && !isPrefixed(text, textPrefix, prefixStart)) {
            builder.append(textPrefix);
        }
        if (text != null) {
            builder.append(text);
        }
        if (textSuffix != null && !isSuffixed(text, textSuffix)) {
            builder.append(textSuffix);
        }

        if (needsIndividualTheme()) {
            if (!TextUtils.isEmpty(prefixThemeKey) && textPrefix != null) {
                int prefixBuilderStart = 0;
                if (!TextUtils.isEmpty(invisiblePrefix)) {
                    prefixBuilderStart += invisiblePrefix.length();
                }
                addThemeSpan(prefixThemeKey, prefixBuilderStart, prefixBuilderStart + textPrefix.length());
            }
            if (!TextUtils.isEmpty(suffixThemeKey) && textSuffix != null) {
                int start = builder.length() == 0 ? 0 : builder.length() - textSuffix.length();
                addThemeSpan(suffixThemeKey, start, builder.length());
            }
        }
        super.setText(builder, BufferType.SPANNABLE);
    }

    private static boolean isPrefixed(@Nullable CharSequence text, @NonNull String prefix) {
        return isPrefixed(text, prefix, 0);
    }

    private static boolean isPrefixed(@Nullable CharSequence text, @NonNull String prefix, int start) {
        try {
            if (text != null) {
                CharSequence possiblePrefix = text.subSequence(start, start + Math.min(text.length(), prefix.length()));
                return possiblePrefix.toString().equals(prefix);
            }
        }
        catch (StringIndexOutOfBoundsException sioobe) {
            Timber.w(sioobe, "Failed to check if prefix is already applied.");
        }
        return false;
    }

    private static boolean isSuffixed(@Nullable CharSequence text, @NonNull String suffix) {
        if (text != null) {
            CharSequence possibleSuffix = text.subSequence(Math.max(text.length() - suffix.length(), 0), text.length());
            return possibleSuffix.toString().equals(suffix);
        }
        return false;
    }

    private void addThemeSpan(String themeKey, int start, int end) {
        ArrayList<String> list = new ArrayList<>();
        list.add(themeKey);
        ThemeableSpan span = new ThemeableSpan(lastTheme, list);
        spannables.add(span);
        builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (leadingMarginHelper.shouldConsiderLeadingMargin()) {
            String newPrefix = leadingMarginHelper.buildPrefix(this);
            updatePrefix(newPrefix);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updatePrefix(String prefix) {
        if (!prefix.equals(invisiblePrefix)) {
            String currentText = getText().toString();
            if (invisiblePrefix != null && currentText.startsWith(invisiblePrefix)) {
                currentText = currentText.substring(invisiblePrefix.length());
            }
            invisiblePrefix = prefix;
            setText(currentText);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        lastTheme = null;
        Activity activity = ContextUtils.findActivity(getContext());
        if (contentInsetAware && activity instanceof ContentInsetProvider) {
            ((ContentInsetProvider) activity).removeOnContentInsetChangedListener(this);
        }
    }

    private boolean needsIndividualTheme() {
        return (!TextUtils.isEmpty(prefixThemeKey) &&
                !TextUtils.isEmpty(textPrefix)) ||
                (!TextUtils.isEmpty(suffixThemeKey) &&
                        !TextUtils.isEmpty(textSuffix));
    }

    public String getPrefixOutFormat() {
        return (prefixOutFormat != null) ? prefixOutFormat : "";
    }

    public String getPrefixTextFormat() {
        return (prefixTextFormat != null) ? prefixTextFormat : TextFormat.PLAIN_TEXT;
    }

    public String getTextFormat() {
        return textFormat;
    }

    public void setTextFormat(String textFormat) {
        this.textFormat = textFormat;
    }

    public String getTextPrefix() {
        return textPrefix;
    }

    public void setTextPrefix(String textPrefix) {
        this.textPrefix = textPrefix;
    }

    public String getPrefixBindKeyPath() {
        return prefixBindKeyPath;
    }

    public String getPrefixFallbackBindKeyPath() {
        return prefixFallbackBindKeyPath;
    }

    public String getSuffixPrefix() {
        return suffixPrefix != null ? suffixPrefix : "";
    }

    public String getSuffixBindKeyPath() {
        return suffixBindKeyPath;
    }

    public String getTextSuffix() {
        return textSuffix;
    }

    public void setTextSuffix(String textSuffix) {
        this.textSuffix = textSuffix;
    }

    public List<String> getRequiredFields() {
        return mRequiredFields;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getFallbackBindKeyPath() {
        return fallbackBindKeyPath;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.mRequiredFields = requiredFields;
    }

    public String getOutFormat() {
        return outFormat;
    }

    @Override
    public void apply(Theme theme) {
        super.apply(theme);

        if (needsIndividualTheme()) {
            lastTheme = theme;
            for (ThemeableSpan spannable : spannables) {
                spannable.setTheme(theme);
            }
        }
    }

    public String getBindKeyPath() {
        return bindKeyPath;
    }

    public void overrideBinding(String bindKeyPath) {
        this.bindKeyPath = bindKeyPath;
    }

    public LiveBinding getUpdater() {
        return updater;
    }

    public void setUpdater(LiveBinding updater) {
        this.updater = updater;
    }

    public boolean isResourceManaged() {
        return !TextUtils.isEmpty(resourceKey);
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getPrefixFallback() {
        return prefixFallback;
    }

    public String getPrefixPrefix() {
            return prefixPrefix != null ? prefixPrefix: "";

    }
    
    public String getPrefixSuffix() {
        return prefixSuffix != null ? prefixSuffix: "";
    }

    public String getLeadingMarginProperty() {
        return leadingMarginHelper.getProperty();
    }

    public void setLeadingMarginPropertyValue(String leadingMarginPropertyValue) {
        leadingMarginHelper.setLeadingMarginPropertyValue(leadingMarginPropertyValue);
    }

    private Unit triggerInvisiblePrefixRemeasure() {
        invisiblePrefix = null;
        invalidate();
        requestLayout();
        return Unit.INSTANCE;
    }
}

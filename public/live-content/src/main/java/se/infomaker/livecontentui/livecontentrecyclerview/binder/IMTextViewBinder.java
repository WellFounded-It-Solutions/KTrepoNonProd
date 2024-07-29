package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import se.infomaker.frtutilities.DateUtil;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentrecyclerview.view.ACTextView;
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMTextView;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.livecontentrecyclerview.view.TextFormat;

public class IMTextViewBinder implements ViewBinder {

    private final ResourceManager resourceManager;
    private final HashSet<Class> mSupportedTypes;

    public IMTextViewBinder() {
        this(null);
    }

    public IMTextViewBinder(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        mSupportedTypes = new HashSet<>();
        mSupportedTypes.add(IMTextView.class);
        mSupportedTypes.add(ACTextView.class);
    }

    @Override
    public LiveBinding bind(View view, String value, PropertyObject properties) {
        if (!(view instanceof IMTextView)) {
            throw new RuntimeException("Unexpected view + " + view);
        }
        IMTextView textView = (IMTextView) view;
        if (textView.getUpdater() != null) {
            textView.getUpdater().recycle();
            textView.setUpdater(null);
        }

        if (!TextUtils.isEmpty(textView.getLeadingMarginProperty())) {
            textView.setLeadingMarginPropertyValue(properties.optString(textView.getLeadingMarginProperty()));
        }

        if (resourceManager != null && textView.isResourceManaged()) {
            bindResource(resourceManager, textView);
        }
        else {
            bindNow(textView, value, properties);
            if (needsUpdate(textView.getTextFormat(), value)) {
                textView.setUpdater(LiveBinding.add(textView, () -> bindNow(textView, value, properties), textView.getTextFormat().equals(TextFormat.COUNT_DOWN) ? 1 : 30));
            }
        }
        return textView.getUpdater();
    }

    private void bindResource(@NonNull ResourceManager resourceManager, @NonNull IMTextView textView) {
        String text = resourceManager.getString(textView.getResourceKey(), null);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        }
        else {
            textView.setVisibility(View.GONE);
        }
    }

    private void bindNow(IMTextView textView, String value, PropertyObject propertyObject) {
        if (!TextUtils.isEmpty(textView.getPrefixBindKeyPath())) {
            String prefixValue = propertyObject.optString(textView.getPrefixBindKeyPath());
            String prefixFallbackValue = null;
            if (!TextUtils.isEmpty(textView.getPrefixFallbackBindKeyPath())) {
                prefixFallbackValue = propertyObject.optString(textView.getPrefixFallbackBindKeyPath());
            }
            if (TextUtils.isEmpty(prefixFallbackValue)) {
                prefixFallbackValue = textView.getPrefixFallback();
            }
            String prefix = getFormattedValue(textView.getContext(), prefixValue, textView.getPrefixTextFormat(), textView.getPrefixOutFormat(), prefixFallbackValue);
            if (!TextUtils.isEmpty(prefix)) {
                textView.setTextPrefix(textView.getPrefixPrefix() + prefix + textView.getPrefixSuffix());
            }
        }
        if (!TextUtils.isEmpty(textView.getSuffixBindKeyPath())) {
            String suffixValue = propertyObject.optString(textView.getSuffixBindKeyPath());
            if (!TextUtils.isEmpty(suffixValue)) {
                String suffix = getFormattedValue(textView.getContext(), suffixValue, TextFormat.PLAIN_TEXT, null, null);
                if (!TextUtils.isEmpty(suffix)) {
                    textView.setTextSuffix(textView.getSuffixPrefix() + suffix);
                }
            }
        }
        String fallbackValue = textView.getFallback();
        if (!TextUtils.isEmpty(textView.getFallbackBindKeyPath())) {
            String fallbackPropertyValue = propertyObject.optString(textView.getFallbackBindKeyPath());
            if (fallbackPropertyValue != null) {
                fallbackValue = fallbackPropertyValue;
            }
        }
        String finalText = getFormattedValue(textView.getContext(), value, textView.getTextFormat(), textView.getOutFormat(), fallbackValue);
        if (!TextUtils.isEmpty(finalText)) {
            textView.setText(finalText);
            if (FieldValidator.validateRequiredFields(textView.getRequiredFields(), propertyObject)) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public Set<Class> supportedViews() {
        return mSupportedTypes;
    }

    @Override
    public String getKey(View view) {
        if (view instanceof IMTextView) {
            String keyPath = ((IMTextView) view).getBindKeyPath();
            if (keyPath != null) {
                return keyPath;
            }
        }
        if (view.getId() != 0) {
            return view.getResources().getResourceEntryName(view.getId());
        }
        return null;
    }

    private String getFormattedValue(Context context, @NonNull String value, @NonNull String key, @Nullable String outFormat, @Nullable String defaultValue) {
        String formattedValue = formatTextFormat(context, key, value, outFormat);
        return (formattedValue == null || formattedValue.isEmpty()) && defaultValue != null ? defaultValue : formattedValue;
    }

    public static boolean needsUpdate(String format, String value) {
        return format != null && !TextUtils.isEmpty(value) && (TextFormat.ISO_DATE.equals(format.toLowerCase()) || TextFormat.COUNT_DOWN.equals(format.toLowerCase())||TextFormat.ISO_DATE_WITHOUT_TIME_AGO.equals(format.toLowerCase()));
    }

    public static String formatTextFormat(String key, String value, String outFormat) {
        return formatTextFormat(null, key, value, outFormat);
    }

    public static String formatTextFormat(Context context, String key, String value, String outFormat) {
        if (key != null && value != null) {
            switch (key.toLowerCase()) {
                case TextFormat.PLAIN_TEXT:
                    // Do nothing for now
                    break;
                case TextFormat.ISO_DATE:
                    return DateUtil.formatDateString(context, value, outFormat);
                case TextFormat.COUNT_DOWN:
                    Date date = DateUtil.getDateFromString(value);
                    long seconds = (date.getTime() - System.currentTimeMillis()) / 1000;
                    if (seconds <= 0) {
                        return "00:00:00";
                    }
                    return DateUtils.formatElapsedTime(seconds);
                case TextFormat.ISO_DATE_WITHOUT_TIME_AGO:
                    return  DateUtil.formatDateStringWithoutTimeAgo(context,value, outFormat);

            }
        }
        return value;
    }
}

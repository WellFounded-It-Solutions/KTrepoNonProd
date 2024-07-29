package se.infomaker.livecontentui.livecontentrecyclerview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.navigaglobal.mobile.livecontent.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.image.ThemeImage;
import se.infomaker.iap.theme.view.ThemeableImageView;
import se.infomaker.livecontentui.PresentationContextMatchable;


public class IMImageView extends ThemeableImageView implements OverridableBinding, PresentationContextMatchable {
    private static final int DEFAULT_FALLBACK_DRAWABLE = R.drawable.light_grey_solid;
    private static final int DEFAULT_PLACEHOLDER_DRAWABLE = R.drawable.transparent;

    private static final ThemeImage DEFAULT_IMAGE_PLACEHOLDER = new ThemeImage(R.drawable.default_placeholder_image);
    private static final String PLACEHOLDER_IMAGE = "placeholderImage";

    private boolean mDisplayFallbackIfEmpty;
    private Drawable mPlaceholderDrawable;
    private Drawable mFallbackDrawable;
    private Drawable mErrorDrawable;
    String mLayoutName;
    String mFunctionType;
    int[] mAspectRatio;
    String mProportionalWidth;
    // Sets "plainText" to default.
    String mTextFormat = TextFormat.PLAIN_TEXT;
    String imageFormat;
    int mPreviewWidth;
    int mPreviewHeight;
    List<String> mRequiredFields;
    private String bindKeyPath;
    private int blur;

    private String cropKeyPath;
    private String heightKeyPath;
    private String widthKeyPath;
    private String preferredCrop;
    private String cropsKeyPath;

    private Map<String, List<String>> contextMatchMap;

    public IMImageView(Context context) {
        super(context);
    }

    public IMImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public IMImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.IMImageView);
        mFunctionType = typedArray.getString(R.styleable.IMImageView_type);
        imageFormat = typedArray.getString(R.styleable.IMImageView_imageFormat);
        bindKeyPath = typedArray.getString(R.styleable.IMImageView_bindKeyPath);

        cropKeyPath = typedArray.getString(R.styleable.IMImageView_cropKeyPath);
        heightKeyPath = typedArray.getString(R.styleable.IMImageView_heightKeyPath);
        widthKeyPath = typedArray.getString(R.styleable.IMImageView_widthKeyPath);
        preferredCrop = typedArray.getString(R.styleable.IMImageView_preferredCrop);
        cropsKeyPath = typedArray.getString(R.styleable.IMImageView_cropsKeyPath);
        if (cropsKeyPath == null) {
            cropsKeyPath = "teaserImageCrops";
        }

        blur = typedArray.getInt(R.styleable.IMImageView_blur, 0);
        if (mFunctionType == null) {
            mFunctionType = "hardCrop";
        }
        mTextFormat = typedArray.getString(R.styleable.IMImageView_textFormat);
        String aspectRatio = typedArray.getString(R.styleable.IMImageView_aspectRatio);
        if (aspectRatio != null) {
            mAspectRatio = createAspectRatioFromString(aspectRatio);
        }

        mFallbackDrawable = typedArray.getDrawable(R.styleable.IMImageView_fallbackDrawable);
        if (mFallbackDrawable == null && getFallbackDrawable() > 0) {
            mFallbackDrawable = ContextCompat.getDrawable(getContext(), getFallbackDrawable());
        }

        if (mFallbackDrawable != null) {
            mDisplayFallbackIfEmpty = true;
        }
        else {
            mFallbackDrawable = ContextCompat.getDrawable(getContext(), DEFAULT_FALLBACK_DRAWABLE);
        }

        mPlaceholderDrawable = typedArray.getDrawable(R.styleable.IMImageView_placeholderDrawable);
        if (mPlaceholderDrawable == null) {
            mPlaceholderDrawable = ContextCompat.getDrawable(getContext(), DEFAULT_PLACEHOLDER_DRAWABLE);
        }
        String contextKey = typedArray.getString(R.styleable.IMImageView_contextKey);
        String contextValues = typedArray.getString(R.styleable.IMImageView_contextValues);
        contextMatchMap = PresentationContextMatchable.makePresentationContextMatchMap(contextKey, contextValues);
        typedArray.recycle();
    }

    private int[] createAspectRatioFromString(String aspectRatio) {
        String[] ratioParts = aspectRatio.split(":");
        int[] ratio = new int[2];
        ratio[0] = Integer.parseInt(ratioParts[0]);
        ratio[1] = Integer.parseInt(ratioParts[1]);
        return ratio;
    }

    /**
     * Override of onMeasure to enforce mAspectRatio.
     *
     * NOTE: The enforcing of mAspectRatio assumes width is known and only
     *       re-calculates the height.
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio != null && mAspectRatio.length == 2) {
            int xAspectRatio = mAspectRatio[0];
            int yAspectRatio = mAspectRatio[1];

            int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
            int calculatedHeight = (originalWidth - getPaddingLeft() - getPaddingRight()) * yAspectRatio / xAspectRatio;

            widthMeasureSpec = MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void apply(Theme theme) {
        super.apply(theme);

        if (mDisplayFallbackIfEmpty) {
            mErrorDrawable = mFallbackDrawable;
            return;
        }
        /*
         * Yes, the naming is confusing but it is intended to be used as an image placeholder when offline.
         */
        mErrorDrawable = theme.getImage(PLACEHOLDER_IMAGE, DEFAULT_IMAGE_PLACEHOLDER).getImage(getContext());
    }

    public String getFunctionType() {
        return mFunctionType;
    }

    public void setAspectRatio(String aspectRatio) {
        if (TextUtils.isEmpty(aspectRatio)) {
            mAspectRatio = null;
        }
        else {
            mAspectRatio = createAspectRatioFromString(aspectRatio);
        }
    }

    public String getAspectRatio() {
        if (mAspectRatio == null) {
            return null;
        }
        return mAspectRatio[0] + ":" + mAspectRatio[1];
    }

    public void setProportionalWidth(String proportionalWidth) {
        this.mProportionalWidth = proportionalWidth;
    }

    public String getProportionalWidth() {
        return mProportionalWidth;
    }

    public String getTextFormat() {
        return mTextFormat;
    }

    public void setTextFormat(String textFormat) {
        this.mTextFormat = textFormat;
    }

    public void setLayoutName(String layoutName) {
        this.mLayoutName = layoutName;
    }

    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.mPreviewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.mPreviewHeight = previewHeight;
    }

    public List<String> getRequiredFields() {
        return mRequiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.mRequiredFields = requiredFields;
    }

    public boolean displayFallbackIfEmpty() {
        return mDisplayFallbackIfEmpty;
    }

    public Drawable getmPlaceholderDrawable() {
        return mPlaceholderDrawable;
    }

    public Drawable getErrorDrawable() {
        return mErrorDrawable;
    }

    public Point fitAspectRatio() {

        if (mAspectRatio != null && mAspectRatio.length == 2) {
            int xAspectRatio = mAspectRatio[0];
            int yAspectRatio = mAspectRatio[1];
            getLayoutParams().height = (int) ((getMeasuredWidth() / (double) xAspectRatio) * yAspectRatio);
            return new Point(getMeasuredWidth(), getLayoutParams().height);
        }
        return new Point(getMeasuredWidth(), getMeasuredHeight());
    }

    public String getBindKeyPath() {
        return bindKeyPath;
    }

    public int getBlur() {
        return blur;
    }

    public String getCropKeyPath() {
        return cropKeyPath;
    }

    public String getPreferredCrop() {
        return preferredCrop;
    }

    public String getCropsKeyPath() {
        return cropsKeyPath;
    }

    public String getHeightKeyPath() {
        return heightKeyPath;
    }

    public String getWidthKeyPath() {
        return widthKeyPath;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    @Override
    public void overrideBinding(String bindKeyPath) {
        this.bindKeyPath = bindKeyPath;
    }

    @Override
    public Map<String, List<String>> getPresentationContextMatchMap() {
        return contextMatchMap;
    }
}

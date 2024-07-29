package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.OneShotPreDrawListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.navigaglobal.mobile.livecontent.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.wasabeef.glide.transformations.BlurTransformation;
import se.infomaker.iap.articleview.item.image.CropData;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilder;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.view.ACImageView;
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMImageView;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;

public class IMImageViewBinder implements ViewBinder {

    private final List<Double> mImageSizes;
    private final HashSet<Class> mSupportedTypes;
    private final ImageUrlBuilderFactory imageUrlFactory;

    public IMImageViewBinder(ImageUrlBuilderFactory imageUrlFactory, List<Double> imageSizes) {
        this.imageUrlFactory = imageUrlFactory;
        mSupportedTypes = new HashSet<>();
        mSupportedTypes.add(IMImageView.class);
        mSupportedTypes.add(ACImageView.class);
        mImageSizes = imageSizes;
    }

    @Override
    public LiveBinding bind(final View view, final String value, PropertyObject properties) {
        if (!(view instanceof IMImageView)) {
            throw new RuntimeException("Unexpected view type" + view);
        }
        final IMImageView imageView = (IMImageView) view;
        final String functionType = imageView.getFunctionType().toLowerCase();

        if (value == null || value.trim().isEmpty()) {
            if (imageView.displayFallbackIfEmpty()) {
                imageView.setImageResource(imageView.getFallbackDrawable());
            } else {
                imageView.setVisibility(View.GONE);
            }
        } else {
            if (!FieldValidator.validateRequiredFields(imageView.getRequiredFields(), properties)) {
                imageView.setVisibility(View.GONE);
                return null;
            } else {
                imageView.setVisibility(View.VISIBLE);
                OneShotPreDrawListener.add(imageView, () -> {
                    int finalWidth = imageView.getMeasuredWidth();
                    int finalHeight = imageView.getMeasuredHeight();

                    CropDataHelper cropDataHelper = getCropDataHelper(imageView, properties);
                    String url = buildFromCrop(value, imageView, cropDataHelper);
                    boolean usingCrop = !TextUtils.isEmpty(url);
                    if (url == null) {
                        float imageRatio = (float) finalHeight / finalWidth;
                        int imageWidth = finalWidth;
                        int imageHeight = finalHeight;
                        if (mImageSizes != null && mImageSizes.size() > 0) {
                            imageWidth = nearnum(imageWidth, mImageSizes).intValue();
                            imageHeight = Math.round(imageWidth * imageRatio);
                        }

                        url = imageUrlFactory.create()
                                .setImageId(value)
                                .setWidth(imageWidth)
                                .setHeight(imageHeight)
                                .setFunction(functionType)
                                .setFormat(imageView.getImageFormat())
                                .build();
                    }

                    Context appContext = imageView.getContext().getApplicationContext();
                    Drawable errorResource;
                    if (imageView.getFallbackDrawable() != 0) {
                        errorResource = imageView.getResources().getDrawable(imageView.getFallbackDrawable());
                    } else if (imageView.getErrorDrawable() != null) {
                        errorResource = imageView.getErrorDrawable();
                    } else {
                        errorResource = view.getResources().getDrawable(R.drawable.default_placeholder_image);
                    }

                    Drawable placeholderResource = imageView.getmPlaceholderDrawable();

                    if (TextUtils.isEmpty(url)) {
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        layoutParams.height = 0;
                        imageView.setLayoutParams(layoutParams);
                    } else {
                        String aspectRatio = imageView.getAspectRatio();
                        if (aspectRatio != null && aspectRatio.length() == 3) {
                            int xAspectRatio = Integer.valueOf(Character.toString(aspectRatio.charAt(0)));
                            int yAspectRatio = Integer.valueOf(Character.toString(aspectRatio.charAt(2)));
                            finalHeight = (imageView.getMeasuredWidth() / xAspectRatio) * yAspectRatio;

                            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                            layoutParams.height = finalHeight;
                            imageView.setLayoutParams(layoutParams);
                        }
                        // Scale up to closest size value in value list
                        if (finalWidth > 300 && finalHeight > 300) {
                            finalWidth = finalWidth / 2;
                            finalHeight = finalHeight / 2;
                        }

                        imageView.setPreviewWidth(finalWidth);
                        imageView.setPreviewHeight(finalHeight);
                    }

                    RequestOptions options = new RequestOptions()
                            .error(errorResource)
                            .fallback(errorResource)
                            .placeholder(placeholderResource);
                    if (!usingCrop) {
                        if (functionType.equals("cover")) {
                            options.circleCrop();

                        } else if (functionType.equals("hardcrop")) {
                            options.fitCenter();
                        }
                    }
                    if (imageView.getBlur() != 0) {
                        options.transform(new BlurTransformation(imageView.getBlur()));
                    }

                    RequestBuilder<Bitmap> requestBuilder = Glide.with(appContext).asBitmap()
                            .load(url)
                            .transition(BitmapTransitionOptions.withCrossFade())
                            .apply(options);

                    requestBuilder.into(imageView);
                });
            }
        }
        return null;
    }

    private CropDataHelper getCropDataHelper(IMImageView imageView, PropertyObject properties) {
        CropDataHelper cropDataHelper = new CropDataHelper();
        cropDataHelper.parseCropData(imageView, properties);
        return cropDataHelper;
    }

    private String buildFromCrop(String imageId, IMImageView imageView, CropDataHelper cropDataHelper) {
        CropData crop = cropDataHelper.findCrop();
        if (crop == null) {
            return null;
        }

        Double croppedImageHeight = cropDataHelper.getSrcImageHeight() * crop.getHeight();
        Double croppedImageWidth = cropDataHelper.getSrcImageWidth() * crop.getWidth();

        int scaledImageHeight = (int) (croppedImageHeight * (imageView.getWidth() / croppedImageWidth));

        String partialUri = imageUrlFactory.create()
                .setFunction("cropresize")
                .setImageId(imageId)
                .setWidth(imageView.getWidth())
                .setHeight(scaledImageHeight)
                .setFormat(imageView.getImageFormat())
                .build();

        return Uri.parse(partialUri).buildUpon()
                .appendQueryParameter("crop_w", String.valueOf(crop.getWidth()))
                .appendQueryParameter("crop_h", String.valueOf(crop.getHeight()))
                .appendQueryParameter("x", String.valueOf(crop.getX()))
                .appendQueryParameter("y", String.valueOf(crop.getY())).build().toString();
    }

    @Override
    public Set<Class> supportedViews() {
        return mSupportedTypes;
    }

    @Override
    public String getKey(View view) {
        if (view instanceof IMImageView) {
            String bindKeyPath = ((IMImageView) view).getBindKeyPath();
            if (bindKeyPath != null) {
                return bindKeyPath;
            }
        }
        if (view.getId() > 0) {
            return view.getResources().getResourceEntryName(view.getId());
        }
        return null;
    }

    private static Double nearnum(double myNumber, List<Double> numbers) {
        double distance = Math.abs(numbers.get(0) - myNumber);
        int idx = 0;
        for (int c = 1; c < numbers.size(); c++) {
            double cdistance = Math.abs(numbers.get(c) - myNumber);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return numbers.get(idx);
    }
}

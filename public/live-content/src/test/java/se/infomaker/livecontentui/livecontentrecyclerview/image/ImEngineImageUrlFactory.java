package se.infomaker.livecontentui.livecontentrecyclerview.image;

import android.net.Uri;

public class ImEngineImageUrlFactory implements ImageUrlBuilderFactory {
    private static class ImEngineImageUrlBuilder implements ImageUrlBuilder {
        private static final String DEFAULT_FUNCTION = "hardcrop";
        private final Uri.Builder builder;
        private boolean qualitySet;
        private boolean functionSet;

        ImEngineImageUrlBuilder(String baseUrl) {
            builder = Uri.parse(baseUrl).buildUpon();
            builder.appendPath("imengine").appendPath("image.php");
        }

        @Override
        public ImageUrlBuilder setWidth(int width) {
            builder.appendQueryParameter("width", "" + width);
            return this;
        }

        @Override
        public ImageUrlBuilder setHeight(int height) {
            builder.appendQueryParameter("height", "" + height);
            return this;
        }

        @Override
        public ImageUrlBuilder setQuality(int quality) {
            qualitySet = true;
            builder.appendQueryParameter("q", "" + quality);
            return this;
        }

        @Override
        public ImageUrlBuilder setFunction(String function) {
            builder.appendQueryParameter("function", function != null ? function : "hardcrop");
            functionSet = true;
            return this;
        }

        @Override
        public ImageUrlBuilder setImgSrc(String imgSrc) {
            builder.appendQueryParameter("imgsrc", imgSrc);
            return this;
        }

        @Override
        public ImageUrlBuilder setImageId(String imageId) {
            builder.appendQueryParameter("uuid", imageId);
            return this;
        }

        @Override
        public String build() {
            builder.appendQueryParameter("type", "preview")
                    .appendQueryParameter("source", "false");
            if (!qualitySet) {
                setQuality(80);
            }
            if (!functionSet) {
                setFunction(DEFAULT_FUNCTION);
            }
            return builder.build().toString();
        }
    }

    public static final String PROVIDER_NAME = "imengine";
    private final String mImageBaseUrl;

    public ImEngineImageUrlFactory(String baseUrl) {
        mImageBaseUrl = baseUrl;
    }

    @Override
    public ImageUrlBuilder create() {
        return new ImEngineImageUrlBuilder(mImageBaseUrl);
    }
}

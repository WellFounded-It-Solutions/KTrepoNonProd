package se.infomaker.livecontentui.livecontentrecyclerview.image;

import android.text.TextUtils;

import timber.log.Timber;

public class ImageUrlFactoryProvider {
    public ImageUrlBuilderFactory provide(String providerName, String baseUrl) {
        String provider = TextUtils.isEmpty(providerName) ? "" : providerName;
        switch (provider) {
            default: {
                Timber.d("Using default");
            }
            case ImEngineImageUrlFactory.PROVIDER_NAME: {
                return new ImEngineImageUrlFactory(baseUrl);
            }
        }
    }
}

package se.infomaker.iap.articleview;

import org.jetbrains.annotations.Nullable;

import se.infomaker.frtutilities.ResourceProvider;

public class TestResourceProvider implements ResourceProvider {

    @Nullable
    @Override
    public <T> T getAsset(String asset, Class<T> classOfT) {
        return null;
    }
}

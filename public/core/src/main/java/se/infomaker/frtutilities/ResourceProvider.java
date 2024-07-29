package se.infomaker.frtutilities;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface ResourceProvider {
    @Nullable
    <T> T getAsset(String asset, Class<T> classOfT) throws IOException;
}

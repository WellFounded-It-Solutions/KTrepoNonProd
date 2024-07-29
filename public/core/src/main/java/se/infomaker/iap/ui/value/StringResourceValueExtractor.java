package se.infomaker.iap.ui.value;

import android.content.Context;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.ui.content.Content;

public class StringResourceValueExtractor extends KeyPathValueExtractor {
    private final Context context;
    private final ResourceManager resourceManager;

    @SuppressWarnings("SameParameterValue")
    public StringResourceValueExtractor(Context context, ResourceManager resourceManager, String keyPath) {
        super(keyPath);
        this.context = context;
        this.resourceManager = resourceManager;
    }

    @Override
    public String getValue(Content content) {
        String value = super.getValue(content);
        int identifier = resourceManager.getStringIdentifier(value);
        if (identifier != 0) {
            context.getString(identifier);
        }
        return value;
    }
}
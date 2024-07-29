package se.infomaker.iap.ui.binding;

import android.text.TextUtils;

import se.infomaker.iap.ui.content.Content;
import se.infomaker.iap.ui.value.ValueExtractor;

public class ContentBinding {
    private final Bindable bindable;
    private final ValueExtractor extractor;

    public ContentBinding(Bindable bindable, ValueExtractor extractor) {
        this.bindable = bindable;
        this.extractor = extractor;
    }

    public void bind(Content content) {
        String value = extractor.getValue(content);
        if (TextUtils.isEmpty(value)) {
            bindable.clear();
        }
        else {
            bindable.bind(value);
        }
    }
}

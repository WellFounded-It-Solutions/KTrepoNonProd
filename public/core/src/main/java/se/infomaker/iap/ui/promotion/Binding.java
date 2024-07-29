package se.infomaker.iap.ui.promotion;

import se.infomaker.iap.ui.value.ValueExtractor;

@SuppressWarnings("WeakerAccess")
public class Binding {
    private final String target;
    private final ValueExtractor valueExtractor;

    public Binding(String target, ValueExtractor valueExtractor) {
        this.target = target;
        this.valueExtractor = valueExtractor;
    }

    public String getTarget() {
        return target;
    }

    public ValueExtractor getValueExtractor() {
        return valueExtractor;
    }
}

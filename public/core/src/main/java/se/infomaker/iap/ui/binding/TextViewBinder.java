package se.infomaker.iap.ui.binding;

import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import se.infomaker.frtutilities.ResourceManager;

public class TextViewBinder implements Bindable{
    public static class Creator implements BindableFactory {
        private final ResourceManager resourceManager;

        public Creator(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        @Override
        public Bindable create(View view) {
            return new TextViewBinder(resourceManager, (TextView) view);
        }

        @Override
        public Set<Class> supported() {
            HashSet<Class> classes = new HashSet<>();
            classes.add(TextView.class);
            return classes;
        }
    }


    private final TextView textView;
    private final ResourceManager resourceManager;

    @SuppressWarnings("WeakerAccess")
    public TextViewBinder(ResourceManager resourceManager, TextView textView) {
        this.resourceManager = resourceManager;
        this.textView = textView;
    }

    @Override
    public void bind(String value) {
        int identifier = resourceManager.getStringIdentifier(value);
        if (identifier != 0) {
            textView.setText(identifier);
        }
        else {
            textView.setText(value);
        }
    }

    @Override
    public void clear() {
        textView.setText("");
    }
}

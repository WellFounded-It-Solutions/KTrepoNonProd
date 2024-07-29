package se.infomaker.iap.ui.binding;

import android.view.View;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Set;

import se.infomaker.frtutilities.ResourceManager;

@SuppressWarnings("WeakerAccess")
public class ImageViewBinder implements Bindable {
    public static class Creator implements BindableFactory {
        private final ResourceManager resourceManager;

        public Creator(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        @Override
        public Bindable create(View view) {
            return new ImageViewBinder(resourceManager, (ImageView) view);
        }

        @Override
        public Set<Class> supported() {
            HashSet<Class> classes = new HashSet<>();
            classes.add(ImageView.class);
            return classes;
        }
    }

    private final ImageView imageView;
    private final ResourceManager resourceManager;

    public ImageViewBinder(ResourceManager resourceManager, ImageView imageView) {
        this.resourceManager = resourceManager;
        this.imageView = imageView;
    }

    @Override
    public void bind(String value) {
        int identifier = resourceManager.getDrawableIdentifier(value);
        if (identifier != 0) {
            imageView.setImageResource(identifier);
        }
        else {
            clear();
        }
    }

    @Override
    public void clear() {
        imageView.setImageDrawable(null);
    }
}

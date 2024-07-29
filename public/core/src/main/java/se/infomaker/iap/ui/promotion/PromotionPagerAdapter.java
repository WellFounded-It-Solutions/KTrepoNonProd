package se.infomaker.iap.ui.promotion;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.ui.binding.Bindable;
import se.infomaker.iap.ui.binding.ContentBinding;
import se.infomaker.iap.ui.view.ViewFactory;
import se.infomaker.iap.ui.view.ViewHolder;
import se.infomaker.iap.ui.binding.BindableProvider;
import se.infomaker.iap.ui.binding.ImageViewBinder;
import se.infomaker.iap.ui.binding.TextViewBinder;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.ui.util.UI;

class PromotionPagerAdapter extends PagerAdapter {
    private final PromotionConfiguration configuration;
    private final ViewFactory viewFactory;
    private final Theme theme;
    private final BindableProvider bindableProvider;
    private final ResourceManager resourceManager;

    public PromotionPagerAdapter(ResourceManager resourceManager, Theme theme, ViewFactory viewFactory, PromotionConfiguration configuration) {
        this.theme = theme;
        this.resourceManager = resourceManager;
        this.viewFactory = viewFactory;
        this.configuration = configuration;
        bindableProvider = new BindableProvider(new ImageViewBinder.Creator(resourceManager), new TextViewBinder.Creator(resourceManager));
    }

    public Object instantiateItem(ViewGroup container, int position) {
        Page page = configuration.getPages().get(position);
        View view = viewFactory.create(page.getViewName(), container, false);
        container.addView(view);
        ViewHolder viewHolder = createViewHolder(page, view);
        viewHolder.bind(page);

        Theme extendedFrom = ThemeManager.getInstance(container.getContext()).extendedFrom(this.theme, resourceManager, page.getTheme());
        ThemeColor background = extendedFrom.getColor(ThemeKeys.BACKGROUND, null);
        if (background != null) {
            view.setBackgroundColor(background.get());
        }
        extendedFrom.apply(viewHolder.getView());
        return view;
    }

    private ViewHolder createViewHolder(Page page, View view) {
        return new ViewHolder(view, createContentBindings(view, page.createBindings()));
    }

    @NonNull
    private ArrayList<ContentBinding> createContentBindings(View view, List<Binding> bindings) {
        Map<String, View> viewMap = UI.extractViews(view);
        ArrayList<ContentBinding> contentBindings = new ArrayList<>();
        for (Binding binding : bindings) {
            View target = viewMap.get(binding.getTarget());
            if (target instanceof Bindable) {
                contentBindings.add(new ContentBinding((Bindable) target, binding.getValueExtractor()));
            }
            else if (bindableProvider.supported(target)) {
                contentBindings.add(new ContentBinding(bindableProvider.makeBindable(target), binding.getValueExtractor()) );
            }
        }
        return contentBindings;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return configuration.getPages().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}

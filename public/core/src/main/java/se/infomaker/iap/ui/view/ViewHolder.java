package se.infomaker.iap.ui.view;

import android.view.View;

import java.util.List;

import se.infomaker.iap.ui.content.Content;
import se.infomaker.iap.ui.binding.ContentBinding;

public class ViewHolder {
    private final View view;
    private final List<ContentBinding> bindings;

    public ViewHolder(View view, List<ContentBinding> bindings) {
        this.view = view;
        this.bindings = bindings;
    }

    public View getView() {
        return view;
    }

    public void bind(Content content) {
        for (ContentBinding binding : bindings) {
            binding.bind(content);
        }
    }
}

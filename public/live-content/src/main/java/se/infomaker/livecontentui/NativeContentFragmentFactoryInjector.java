package se.infomaker.livecontentui;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import se.infomaker.frtutilities.AbstractInitContentProvider;
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.NativeContentFragmentFactory;

public class NativeContentFragmentFactoryInjector extends AbstractInitContentProvider {
    @Override
    public void init(@NotNull Context context) {
        DetailViewRegistry.INSTANCE.put("nativeArticle", NativeContentFragmentFactory.SHARED_INSTANCE);
    }
}

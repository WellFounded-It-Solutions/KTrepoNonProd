package se.infomaker.livecontentui.livecontentdetailview.pageadapters;

import android.content.Context;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.List;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.extensions.OrgJSONKt;

public class NativeContentFragmentFactory implements DetailFragmentFactory {
    public static final NativeContentFragmentFactory SHARED_INSTANCE = new NativeContentFragmentFactory();

    @Override
    public Fragment createFragment(Context context, String moduleId, PropertyObject content, List<String> overlayThemes) {
        return UpdatableContentFragment.newInstance(moduleId, "ContentList", content.getProperties(), overlayThemes, createPresentationContext(moduleId), content.getId());
    }

    private JSONObject createPresentationContext(String moduleId) {
        // TODO "read"
        JSONObject out = new JSONObject();
        OrgJSONKt.safePut(out, "moduleId", moduleId);
        return out;
    }
}

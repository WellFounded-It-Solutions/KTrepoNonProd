package se.infomaker.livecontentui.section.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.ui.theme.OverlayThemeProvider;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.OnPropertyObjectUpdated;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.BinderCollection;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMFrameLayoutBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMImageViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMTextViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlFactoryProvider;
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import timber.log.Timber;

public class DetailTemplateFragment extends Fragment implements OnPropertyObjectUpdated {

    private static final String MODULE_ID = "moduleId";
    private static final String MODULE_NAME = "moduleName";
    private static final String TEMPLATE = "template";
    private static final String THEME_OVERLAYS = "themeOverlays";
    private static final String PROPERTIES = "properties";
    private static final String PRESENTATION_CONTEXT = "presentationContext";
    private String moduleId;
    private String moduleName;
    private ArrayList<String> themeOverlays;
    private String template;
    private PropertyBinder binder;
    private LiveContentUIConfig config;
    private List<View> viewsArrayList;
    private PropertyObject propertyObject;
    private JSONObject presentationContext;
    private Set<LiveBinding> liveBindings;
    private ResourceManager resourceManager;

    public static DetailTemplateFragment createInstance(String moduleId, String moduleName, String template, PropertyObject object, List<String> themeOverlays, JSONObject context) {
        DetailTemplateFragment fragment = new DetailTemplateFragment();
        Bundle arguments = new Bundle();
        arguments.putString(MODULE_ID, moduleId);
        arguments.putString(MODULE_NAME, moduleName);
        arguments.putString(TEMPLATE, template);
        if (themeOverlays != null) {
            arguments.putStringArrayList(THEME_OVERLAYS, new ArrayList(themeOverlays));
        }
        arguments.putSerializable(PROPERTIES, object);
        if (context != null) {
            arguments.putString(PRESENTATION_CONTEXT, context.toString());
        }
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        this.moduleId = arguments.getString(MODULE_ID);
        this.moduleName = arguments.getString(MODULE_NAME);
        resourceManager = new ResourceManager(getActivity(), moduleId);
        this.template = arguments.getString(TEMPLATE);
        this.themeOverlays = arguments.getStringArrayList(THEME_OVERLAYS);
        this.propertyObject = (PropertyObject) arguments.getSerializable(PROPERTIES);
        if (arguments.getString(PRESENTATION_CONTEXT) != null) {
            try {
                this.presentationContext = new JSONObject(arguments.getString(PRESENTATION_CONTEXT));
            }
            catch (JSONException e) {
                Timber.w(e, "Failed to re create context.");
            }
        }
        config = ConfigManager.getInstance(getActivity()).getConfig(moduleName, moduleId, LiveContentUIConfig.class);
        ImageUrlBuilderFactory imageUrlBuilderFactory = new ImageUrlFactoryProvider().provide(config.getImageProvider(), config.getImageBaseUrl());

        List<Double> imageSizes = null;
        try {
            imageSizes = config.getMedia().getImage().getSizes();
        } catch (Exception e) {
            Timber.e(e, "Failed to get image sizes");
        }

        binder = new PropertyBinder(BinderCollection.with(new IMImageViewBinder(imageUrlBuilderFactory, imageSizes), new IMTextViewBinder(resourceManager), new IMFrameLayoutBinder()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutIdentifier = resourceManager.getLayoutIdentifier(template);
        View view;
        if (layoutIdentifier > 0) {
            view = inflater.inflate(layoutIdentifier, container, false);
        } else {
            view = new FrameLayout(getActivity());
        }
        OverlayThemeProvider.forModule(getActivity(), moduleId).getTheme(themeOverlays).apply(view);
        viewsArrayList = DefaultUtils.getAllChildren(view);
        update();

        return view;
    }

    private void update() {
        if (liveBindings != null) {
            Observable.fromIterable(liveBindings).doOnNext(LiveBinding::stop).subscribe();
        }
        liveBindings = binder.bind(propertyObject, viewsArrayList, presentationContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (liveBindings != null) {
            Observable.fromIterable(liveBindings).doOnNext(LiveBinding::start).subscribe();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (liveBindings != null) {
            Observable.fromIterable(liveBindings).doOnNext(LiveBinding::stop).subscribe();
        }
    }

    @Override
    public void onObjectUpdated(PropertyObject object) {
        this.propertyObject = object;
        update();
        if (liveBindings != null) {
            Observable.fromIterable(liveBindings).doOnNext(LiveBinding::start).subscribe();
        }
    }
}
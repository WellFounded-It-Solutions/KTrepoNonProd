package se.infomaker.iap.ui.promotion;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.GlobalValueManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.TextUtils;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.iap.action.Operation;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import com.navigaglobal.mobile.R;

import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.ui.fragment.DummyFragmentPresenter;
import se.infomaker.iap.ui.fragment.FragmentPresenter;
import se.infomaker.iap.ui.fragment.ModuleAwareFragment;
import timber.log.Timber;

/**
 * Displays a promotion flow
 */
public class PromotionFragment extends ModuleAwareFragment {

    private static final String CONFIGURATION = "configuration";

    private FragmentPresenter presenter = DummyFragmentPresenter.INSTANCE;
    private ViewPager pager;
    private TextView nextButton;
    private TextView doneButton;

    public static PromotionFragment createInstance(String moduleId, PromotionConfiguration configuration) {
        Bundle arguments = ModuleAwareFragment.createModuleArguments(moduleId);
        arguments.putParcelable(CONFIGURATION, configuration);
        PromotionFragment fragment = new PromotionFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = ContextUtils.findActivity(context);
        if (!(activity instanceof FragmentPresenter)) {
            throw new RuntimeException("Hosting activity must implement " + FragmentPresenter.class.getCanonicalName());
        }
        presenter = (FragmentPresenter) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter = DummyFragmentPresenter.INSTANCE;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout = getResourceManager().getLayoutIdentifier("promotion_container");
        if (layout < 1) {
            layout = R.layout.fragment_promotion;
        }
        View view = inflater.inflate(layout, container, false);

        pager = view.findViewById(R.id.pager);
        View cancelButton = view.findViewById(R.id.cancelButton);

        nextButton = view.findViewById(R.id.nextButton);
        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(view1 -> {

            JSONObject doneAction = getConfiguration().getDoneAction();
            if (doneAction != null) {
                try {
                    Operation operation = new Operation(doneAction.getString("action"), getModuleId(), doneAction.getJSONObject("parameters"), GlobalValueManager.INSTANCE.getGlobalValueManager(view1.getContext()));
                    operation.perform(view1.getContext(), result -> {
                        dismiss();
                        return null;
                    });
                } catch (JSONException e) {
                    Timber.e(e, "Failed to create operation");
                }
            } else {
                dismiss();
            }
        });
        nextButton.setOnClickListener(view1 -> pager.setCurrentItem(pager.getCurrentItem() + 1, true));

        doneButton.setText(getStringForKey(getConfiguration().getDoneButtonTitle()));
        nextButton.setText(getStringForKey(getConfiguration().getNextButtonTitle()));
        if (cancelButton instanceof TextView) {
            ((TextView) cancelButton).setText(getStringForKey(getConfiguration().getCancelButtonTitle()));
        }

        cancelButton.setOnClickListener(view1 -> dismiss());
        Theme theme = ThemeManager.getInstance(getActivity()).extendedFrom(getModuleTheme(), getResourceManager(), getConfiguration().getTheme());
        if (savedInstanceState == null) {
            registerPageView(0);
        }
        pager.setAdapter(new PromotionPagerAdapter(getResourceManager(), theme, getViewFactory(), getConfiguration()));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateButtons();
                registerPageView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (pager.getAdapter().getCount() <= 1) {
            view.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);
        }
        updateButtons();
        
        theme.apply(view);
        ThemeUtils.apply(theme, requireActivity().getWindow());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isRemoving()) {
            presenter.onFragmentDismissed(this);
        }
    }

    private String getStringForKey(String key) {
        if (key == null) {
            return null;
        }
        int identifier = getResourceManager().getStringIdentifier(key);
        if (identifier == 0) {
            return key;
        } else {
            return getString(identifier);
        }
    }

    private void registerPageView(int position) {
        StatisticsManager.getInstance().logEvent(new StatisticsEvent.Builder()
                .viewShow()
                .moduleId(getModuleId())
                .moduleName(ModuleInformationManager.getInstance().getModuleName(getModuleId()))
                .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(getModuleId()))
                .viewName("promotion")
                .attribute("promotionId", getConfiguration() != null ? getConfiguration().getId() : -1)
                .attribute("pageIndex", position).build());
    }

    private void updateButtons() {
        if (isOnLastPage()) {
            nextButton.setVisibility(View.INVISIBLE);
            doneButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(TextUtils.isEmpty(nextButton.getText()) ? View.INVISIBLE : View.VISIBLE);
            doneButton.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isOnLastPage() {
        return pager.getAdapter().getCount() - 1 == pager.getCurrentItem();
    }

    private PromotionConfiguration getConfiguration() {
        if (getArguments() != null) {
            return getArguments().getParcelable(CONFIGURATION);
        }
        return null;
    }

    private void dismiss() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            PromotionManager.getInstance(activity).setPresented(getConfiguration().getId());
            FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
            supportFragmentManager.beginTransaction().remove(this).commitNow();
            if (supportFragmentManager.getBackStackEntryCount() > 0) {
                String name = supportFragmentManager.getBackStackEntryAt(0).getName();
                supportFragmentManager.popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }
}
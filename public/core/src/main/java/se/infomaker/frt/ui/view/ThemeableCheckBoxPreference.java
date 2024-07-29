package se.infomaker.frt.ui.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.core.graphics.ColorUtils;
import androidx.core.widget.CompoundButtonCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;

public class ThemeableCheckBoxPreference extends CheckBoxPreference {

    private static final int ALPHA_FULL = 255;
    private static final int ALPHA_MEDIUM = 138;
    private static final int ALPHA_DISABLED = 97;

    private static final int[][] CHECKED_STATES =
            new int[][] {
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_checked},
                    new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
                    new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked},
                    new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked}
            };

    public ThemeableCheckBoxPreference(Context context) {
        super(context);
    }

    public ThemeableCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeableCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemeableCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        Context context = holder.itemView.getContext();
        ThemeColor settingsAccent = ThemeManager.getInstance(context).getAppTheme().getColor("settingsAccent", null);

        if (settingsAccent != null) {
            int color = settingsAccent.get();

            View view = holder.findViewById(android.R.id.checkbox);
            if (view instanceof CompoundButton) {
                CompoundButtonCompat.setButtonTintList((CompoundButton) view, getColorStateList(color, Color.BLACK));
            }
        }
    }

    private ColorStateList getColorStateList(int checkedColor, int uncheckedColor) {
        int[] checkBoxColorsList = new int[CHECKED_STATES.length];
        checkBoxColorsList[0] = ColorUtils.setAlphaComponent(checkedColor, ALPHA_FULL);
        checkBoxColorsList[1] = ColorUtils.setAlphaComponent(uncheckedColor, ALPHA_MEDIUM);
        checkBoxColorsList[2] = ColorUtils.setAlphaComponent(checkedColor, ALPHA_DISABLED);
        checkBoxColorsList[3] = ColorUtils.setAlphaComponent(uncheckedColor, ALPHA_DISABLED);
        return new ColorStateList(CHECKED_STATES, checkBoxColorsList);
    }
}

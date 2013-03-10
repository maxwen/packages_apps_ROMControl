package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarBattery extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_BATT_ICON = "battery_icon_list";
    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

    private static final String STATUS_BAR_CIRCLE_BATTERY_COLOR = "status_bar_circle_battery_color";
    private static final String STATUS_BAR_CIRCLE_BATTERY_TEXT_COLOR = "status_bar_circle_battery_text_color";
    private static final String STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED = "status_bar_circle_battery_animationspeed";

    private static final String PREF_CIRCLE_COLOR_RESET = "circle_battery_reset";
    private static final String PREF_STATUS_BAR_CIRCLE_BATTERY_COLOR = "circle_battery_color";
    private static final String PREF_STATUS_BAR_CIRCLE_BATTERY_TEXT_COLOR = "circle_battery_text_color";
    private static final String PREF_STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED = "circle_battery_animation_speed";
    private static final String KEY_LOW_BATTERY_WARNING_POLICY = "pref_low_battery_warning_policy";

    ListPreference mBatteryIcon;
    ColorPickerPreference mCircleColor;
    ColorPickerPreference mCircleTextColor;
    ListPreference mCircleAnimSpeed;
    Preference mCircleColorReset;
    ListPreference mLowBatteryWarning;

    ListPreference mBatteryBar;
    ListPreference mBatteryBarStyle;
    ListPreference mBatteryBarThickness;
    CheckBoxPreference mBatteryBarChargingAnimation;
    ColorPickerPreference mBatteryBarColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_battery);

        int defaultColor;
        int intColor;
        String hexColor;

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_battery);

        mBatteryIcon = (ListPreference) findPreference(PREF_BATT_ICON);
        mBatteryIcon.setOnPreferenceChangeListener(this);
        mBatteryIcon.setValue((Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_BATTERY_ICON,
                0))
                + "");

        mCircleColor = (ColorPickerPreference) findPreference(PREF_STATUS_BAR_CIRCLE_BATTERY_COLOR);
        mCircleColor.setOnPreferenceChangeListener(this);
        defaultColor = getResources().getColor(
                com.android.internal.R.color.holo_blue_dark);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_COLOR, defaultColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mCircleColor.setSummary(hexColor);

        mCircleTextColor = (ColorPickerPreference) findPreference(PREF_STATUS_BAR_CIRCLE_BATTERY_TEXT_COLOR);
        mCircleTextColor.setOnPreferenceChangeListener(this);
        defaultColor = getResources().getColor(
                com.android.internal.R.color.holo_blue_dark);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_TEXT_COLOR, defaultColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mCircleTextColor.setSummary(hexColor);

        mCircleAnimSpeed = (ListPreference) findPreference(PREF_STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED);
        mCircleAnimSpeed.setOnPreferenceChangeListener(this);
        mCircleAnimSpeed.setValue((Settings.System
                .getInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED, 3))
                + "");
        mCircleAnimSpeed.setSummary(mCircleAnimSpeed.getEntry());

        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        mBatteryBar.setValue((Settings.System
                .getInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_BATTERY_BAR, 0))
                + "");

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(getActivity()
                .getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0))
                + "");

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);

        mBatteryBarChargingAnimation = (CheckBoxPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(
                getActivity().getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);
        mBatteryBarThickness.setValue((Settings.System.getInt(getActivity()
                .getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1))
                + "");

        mCircleColorReset = (Preference) findPreference(PREF_CIRCLE_COLOR_RESET);
        if (Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_RESET, 0) == 1) {
            circleColorReset();
        }

        mLowBatteryWarning = (ListPreference) findPreference(KEY_LOW_BATTERY_WARNING_POLICY);
        int lowBatteryWarning = Settings.System.getInt(getActivity().getContentResolver(),
                                    Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY, 0);
        mLowBatteryWarning.setValue(String.valueOf(lowBatteryWarning));
        mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntry());
        mLowBatteryWarning.setOnPreferenceChangeListener(this);

        updateBatteryIconOptions();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mBatteryBarChargingAnimation) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mCircleColorReset) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_RESET, 1);
            circleColorReset();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryIcon) {

            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_ICON, val);
        } else if (preference == mCircleColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_COLOR, intHex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_RESET, 0);
            return true;
        } else if (preference == mCircleTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_TEXT_COLOR, intHex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_RESET, 0);
        } else if (preference == mLowBatteryWarning) {
            int lowBatteryWarning = Integer.valueOf((String) newValue);
            int index = mLowBatteryWarning.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY,
                    lowBatteryWarning);
            mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mCircleAnimSpeed) {
            int val = Integer.parseInt((String) newValue);
            int index = mCircleAnimSpeed.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED, val);
            mCircleAnimSpeed.setSummary(mCircleAnimSpeed.getEntries()[index]);
            return true;

        } else if (preference == mBatteryBar) {

            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR, val);

        } else if (preference == mBatteryBarStyle) {

            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);

        } else if (preference == mBatteryBarThickness) {

            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);

        }
        return false;
    }

    private void circleColorReset() {
        int defaultColor = getResources().getColor(
                com.android.internal.R.color.holo_blue_dark);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CIRCLE_BATTERY_COLOR, defaultColor);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CIRCLE_BATTERY_TEXT_COLOR, defaultColor);
        String hexColor = String.format("#%08x", (0xffffffff & defaultColor));
        mCircleColor.setSummary(hexColor);
        mCircleTextColor.setSummary(hexColor);
    }

    private void updateBatteryIconOptions() {
        int batteryIconStat = Settings.System.getInt(getActivity().getContentResolver(),
               Settings.System.STATUSBAR_BATTERY_ICON, 0);

        if (batteryIconStat == 0 || batteryIconStat == 1 || batteryIconStat == 5) {
            mCircleColor.setEnabled(false);
            mCircleTextColor.setEnabled(false);
            mCircleAnimSpeed.setEnabled(false);
            mCircleColorReset.setEnabled(false);
        } else if (batteryIconStat == 2) {
            mCircleColor.setEnabled(true);
            mCircleTextColor.setEnabled(false);
            mCircleAnimSpeed.setEnabled(true);
            mCircleColorReset.setEnabled(true);
        } else {
            mCircleColor.setEnabled(true);
            mCircleTextColor.setEnabled(true);
            mCircleAnimSpeed.setEnabled(true);
            mCircleColorReset.setEnabled(true);
        }
    }
}

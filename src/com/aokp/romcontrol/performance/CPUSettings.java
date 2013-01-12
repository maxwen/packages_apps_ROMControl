package com.aokp.romcontrol.performance;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import com.aokp.romcontrol.R;

import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class CPUSettings extends Fragment implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "CPUSettings";

    public static final String TEGRA_MAX_FREQ = "/sys/module/cpu_tegra/parameters/cpu_user_cap";
    public static final String STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String SCREEN_OFF_FREQ = "/sys/htc/suspend_freq";
    
    public static final String FREQ_MAX = "freq_max";
    //public static final String FREQ_SUSPEND = "freq_suspend";
    public static final String SOB = "cpu_boot";

    private SeekBar mFreqMaxSlider;
    //private SeekBar mFreqSuspendSlider;
    private Switch mSetOnBoot;
    private TextView mFreqMaxText;
    //private TextView mFreqSuspendText;
    private String[] availableFrequencies;
    private String[] availableFrequenciesWithZero;
    private Activity mActivity;

    private String mFreqMaxSetting;
    //private String mFreqSuspendSetting;

    private static SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root,
            Bundle savedInstanceState) {
        mActivity = getActivity();
        View view = inflater.inflate(R.layout.cpu_settings, root, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        availableFrequencies = new String[0];
        String availableFrequenciesLine = Helpers.readOneLine(STEPS);
        if (availableFrequenciesLine != null) {
            availableFrequencies = availableFrequenciesLine.split(" ");
            
	    Arrays.sort(availableFrequencies, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
                }
            });
        }
        int frequenciesNum = availableFrequencies.length - 1;

		List<String> l=Arrays.asList(availableFrequencies);
		List<String> j1=new ArrayList<String>();
		j1.add("0");
		j1.addAll(l);
		availableFrequenciesWithZero= j1.toArray(new String[]{});
		
		int frequenciesNumWithZero = availableFrequenciesWithZero.length - 1;
		
        String curTegraMaxSpeed = Helpers.readOneLine(TEGRA_MAX_FREQ);
        int curMaxSpeed = 0;
        try {
            curMaxSpeed = Integer.parseInt(curTegraMaxSpeed);
        } catch (NumberFormatException ex) {
            curMaxSpeed = 0;
        }

        String curSuspendSpeed = Helpers.readOneLine(SCREEN_OFF_FREQ);
        int curSuspendSpeedMax = 0;
        try {
            curSuspendSpeedMax = Integer.parseInt(curSuspendSpeed);
        } catch (NumberFormatException ex) {
            curSuspendSpeedMax = 0;
        }

        mFreqMaxSlider = (SeekBar) view.findViewById(R.id.freq_max_slider);
        mFreqMaxSlider.setMax(frequenciesNumWithZero);
        mFreqMaxText = (TextView) view.findViewById(R.id.freq_max_speed_text);
        mFreqMaxText.setText(toMHz(curTegraMaxSpeed));
        mFreqMaxSlider.setProgress(Arrays.asList(availableFrequenciesWithZero).indexOf(curTegraMaxSpeed));
        mFreqMaxSlider.setOnSeekBarChangeListener(this);

        //mFreqSuspendSlider = (SeekBar) view.findViewById(R.id.freq_suspend_slider);
        //mFreqSuspendSlider.setMax(frequenciesNum);
        //mFreqSuspendText = (TextView) view.findViewById(R.id.freq_suspend_speed_text);
        //mFreqSuspendText.setText(toMHz(curSuspendSpeed));
        //mFreqSuspendSlider.setProgress(Arrays.asList(availableFrequencies).indexOf(curSuspendSpeed));
        //mFreqSuspendSlider.setOnSeekBarChangeListener(this);

        mSetOnBoot = (Switch) view.findViewById(R.id.set_on_boot);
        mSetOnBoot.setChecked(preferences.getBoolean(SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(SOB, checked);
                editor.commit();
            }
        });

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
        if(fromUser) {
            switch(seekBar.getId()) {
                case R.id.freq_max_slider:
                    setFreqMaxSpeed(seekBar, progress);
                    break;
                //case R.id.freq_suspend_slider:
                //    setSuspendMaxSpeed(seekBar, progress);
                //    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // we have a break now, write the values..
        CMDProcessor cmd = new CMDProcessor();
        
        cmd.su.runWaitFor("busybox echo " + mFreqMaxSetting + " > " + TEGRA_MAX_FREQ);
        //cmd.su.runWaitFor("busybox echo " + mFreqSuspendSetting + " > " + SCREEN_OFF_FREQ);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setFreqMaxSpeed(SeekBar seekBar, int progress) {
        String current = "";
        current = availableFrequenciesWithZero[progress];
        CMDProcessor cmd = new CMDProcessor();
        int sliderProgress = mFreqMaxSlider.getProgress();
        if (progress <= sliderProgress) {
            mFreqMaxSlider.setProgress(progress);
            mFreqMaxText.setText(toMHz(current));
            mFreqMaxSetting = current;
        }
        mFreqMaxText.setText(toMHz(current));
        mFreqMaxSetting = current;
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FREQ_MAX, current);
        editor.commit();
    }

    /*public void setSuspendMaxSpeed(SeekBar seekBar, int progress) {
        String current = "";
        current = availableFrequencies[progress];
        CMDProcessor cmd = new CMDProcessor();
        int sliderProgress = mFreqSuspendSlider.getProgress();
        if (progress >= sliderProgress) {
            mFreqSuspendSlider.setProgress(progress);
            mFreqSuspendText.setText(toMHz(current));
            mFreqSuspendSetting = current;
        }
        mFreqSuspendText.setText(toMHz(current));
        mFreqSuspendSetting = current;
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FREQ_SUSPEND, current);
        editor.commit();
    }*/

    private String toMHz(String mhzString) {
    	if(mhzString == null || mhzString.equals("0")){
    		return "disabled";
    	}
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz").toString();
    }
}

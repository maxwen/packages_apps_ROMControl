package com.aokp.romcontrol.performance;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.EditText;

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
import com.aokp.romcontrol.AOKPPreferenceFragment;

public class CPUSettings extends AOKPPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "CPUSettings";

    public static final String TEGRA_MAX_FREQ = "/sys/module/cpu_tegra/parameters/cpu_user_cap";
    public static final String STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String SCREEN_OFF_FREQ = "/sys/htc/suspend_freq";
    public static final String TEGRA_MAX_CPU = "/sys/kernel/tegra_mpdecision/conf/max_cpus";
    public static final String TEGRA_ENABLE_OC = "/sys/module/cpu_tegra/parameters/enable_oc";
            
    public static final String FREQ_MAX = "freq_max";
    public static final String CPU_MAX = "cpu_max";
    public static final String ENABLE_OC = "enable_oc";
    public static final String SOB = "cpu_boot";

    private SeekBar mFreqMaxSlider;
    private SeekBar mMaxCPUSlider;
    private Switch mSetOnBoot;
    private Switch mEnableOC;
    private TextView mFreqMaxText;
    private TextView mCPUMaxText;
    private String[] availableFrequencies;
    private String[] availableFrequenciesWithZero;
    private String[] maxCPUList = {"1", "2", "3", "4"};
    private Activity mActivity;

    private String mFreqMaxSetting;
    private String mCPUMaxSetting;
        
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
            curTegraMaxSpeed = "0";
        }

        mMaxCPUSlider = (SeekBar) view.findViewById(R.id.max_cpu_slider);
        	
		File f=new File(TEGRA_MAX_CPU);
		if(f.exists() && f.canRead()){
        	String curCPU = Helpers.readOneLine(TEGRA_MAX_CPU);
        	int curCPUMax = 0;
        	try {
            	curCPUMax = Integer.parseInt(curCPU);
        	} catch (NumberFormatException ex) {
            	curCPUMax = 4;
            	curCPU = "4";
        	}
        	mCPUMaxSetting = Integer.valueOf(curCPUMax).toString();
       
        	mMaxCPUSlider.setMax(3);
        	mCPUMaxText = (TextView) view.findViewById(R.id.max_cpu_text);
        	mCPUMaxText.setText(mCPUMaxSetting);
        	mMaxCPUSlider.setProgress(Arrays.asList(maxCPUList).indexOf(curCPU));
        	mMaxCPUSlider.setOnSeekBarChangeListener(this);
		} else {
			mMaxCPUSlider.setVisibility(View.GONE);
			view.findViewById(R.id.max_cpu_info).setVisibility(View.GONE);
		}
		
        mFreqMaxSlider = (SeekBar) view.findViewById(R.id.freq_max_slider);
        mFreqMaxSlider.setMax(frequenciesNumWithZero);
       	mFreqMaxText = (TextView) view.findViewById(R.id.freq_max_speed_text);
        mFreqMaxText.setText(toMHz(curTegraMaxSpeed));
        mFreqMaxSlider.setProgress(Arrays.asList(availableFrequenciesWithZero).indexOf(curTegraMaxSpeed));
        mFreqMaxSlider.setOnSeekBarChangeListener(this);

        mEnableOC = (Switch) view.findViewById(R.id.enable_oc);
        mEnableOC.setChecked(preferences.getBoolean(ENABLE_OC, false));
        mEnableOC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(ENABLE_OC, checked);
                editor.commit();
                
        		CMDProcessor.runSuCommand("busybox echo " + (checked?"1":"0") + " > " + TEGRA_ENABLE_OC);
            }
        });
		
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
                case R.id.max_cpu_slider:
                    setMaxCPU(seekBar, progress);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // we have a break now, write the values..        
        CMDProcessor.runSuCommand("busybox echo " + mFreqMaxSetting + " > " + TEGRA_MAX_FREQ);
        CMDProcessor.runSuCommand("busybox echo " + mCPUMaxSetting + " > " + TEGRA_MAX_CPU);
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

    public void setMaxCPU(SeekBar seekBar, int progress) {
    	if(mMaxCPUSlider==null){
    		return;
    	}
        String current = "";
        current = maxCPUList[progress];
        int sliderProgress = mMaxCPUSlider.getProgress();
        if (progress <= sliderProgress) {
            mMaxCPUSlider.setProgress(progress);
            mCPUMaxText.setText(current);
            mCPUMaxSetting = current;
        }
        mCPUMaxText.setText(current);
        mCPUMaxSetting = current;
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CPU_MAX, current);
        editor.commit();
    }
	
    private String toMHz(String mhzString) {
    	if(mhzString == null || mhzString.equals("0")){
    		return "disabled";
    	}
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz").toString();
    }
    

}

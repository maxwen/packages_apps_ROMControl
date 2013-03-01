package com.aokp.romcontrol.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.aokp.romcontrol.R;

import com.aokp.romcontrol.performance.CPUSettings;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class BootService extends Service {

    public static boolean servicesStarted = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
        }
        new BootWorker(this).execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BootWorker extends AsyncTask<Void, Void, Void> {

        Context c;

        public BootWorker(Context c) {
            this.c = c;
        }

        @Override
        protected Void doInBackground(Void... args) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            final CMDProcessor cmd = new CMDProcessor();

            if (HeadphoneService.getUserHeadphoneAudioMode(c) != -1
                    || HeadphoneService.getUserBTAudioMode(c) != -1) {
                c.startService(new Intent(c, HeadphoneService.class));
            }

            if (FlipService.getUserFlipAudioMode(c) != -1
                    || FlipService.getUserCallSilent(c) != 0)
                c.startService(new Intent(c, FlipService.class));

            if (preferences.getBoolean(CPUSettings.SOB, false)) {
                final String freqMax = preferences.getString(
                        CPUSettings.FREQ_MAX, null);
                final String maxCPU = preferences.getString(
                        CPUSettings.CPU_MAX, null);
                final String enableOC = preferences.getBoolean(
                		CPUSettings.ENABLE_OC, false)?"1":"0";
                
               	if (freqMax != null && maxCPU != null) {
                    cmd.su.runWaitFor("busybox echo " + freqMax +
                            " > " + CPUSettings.TEGRA_MAX_FREQ);

 					File f=new File(CPUSettings.TEGRA_MAX_CPU);
					if(f.exists()){
                   		cmd.su.runWaitFor("busybox echo " + maxCPU +
                            " > " + CPUSettings.TEGRA_MAX_CPU);
                    }
                          
                    cmd.su.runWaitFor("busybox echo " + enableOC + 
                     		" > " + CPUSettings.TEGRA_ENABLE_OC);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            servicesStarted = true;
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

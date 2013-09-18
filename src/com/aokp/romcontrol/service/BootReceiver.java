package com.aokp.romcontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "RC_BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootReceiver onreceive");
        try {
            if (HeadphoneService.getUserHeadphoneAudioMode(context) != -1
                    || HeadphoneService.getUserBTAudioMode(context) != -1) {
                context.startService(new Intent(context, HeadphoneService.class));
            }
            if (FlipService.getUserFlipAudioMode(context) != -1
                    || FlipService.getUserCallSilent(context) != 0) {
                context.startService(new Intent(context, FlipService.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't start boot receiver", e);
        }
    }
}

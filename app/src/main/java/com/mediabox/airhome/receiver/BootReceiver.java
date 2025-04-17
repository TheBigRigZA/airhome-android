package com.mediabox.airhome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.mediabox.airhome.service.AirPlayService;
import com.mediabox.airhome.util.ConfigManager;

/**
 * BroadcastReceiver that starts the AirPlay service when the device boots up,
 * if the auto-start setting is enabled.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, checking if service should be started");
            
            // Initialize configuration manager
            ConfigManager.getInstance().init(context);
            
            // Check if auto-start is enabled
            if (ConfigManager.getInstance().isAutoStartEnabled()) {
                Log.d(TAG, "Auto-start is enabled, starting AirPlay service");
                
                // Start the service
                Intent serviceIntent = new Intent(context, AirPlayService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
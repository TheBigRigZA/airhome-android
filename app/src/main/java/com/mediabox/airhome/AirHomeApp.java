package com.mediabox.airhome;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.mediabox.airhome.service.AirPlayService;
import com.mediabox.airhome.util.ConfigManager;

/**
 * Main application class for AirHome.
 * Handles application lifecycle and initialization.
 */
public class AirHomeApp extends Application {
    private static final String TAG = "AirHomeApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AirHome Application starting");
        
        // Initialize configuration
        ConfigManager.getInstance().init(this);
        
        // Start the main service if enabled in settings
        if (ConfigManager.getInstance().isAutoStartEnabled()) {
            startAirPlayService();
        }
    }
    
    private void startAirPlayService() {
        Intent serviceIntent = new Intent(this, AirPlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}

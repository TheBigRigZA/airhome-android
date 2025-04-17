package com.mediabox.airhome.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuration manager for storing and retrieving application settings.
 * Uses the Singleton pattern for global access.
 */
public class ConfigManager {
    private static final String PREFS_NAME = "airhome_prefs";
    private static final String KEY_AUTO_START = "auto_start_enabled";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_TRANSCODING_ENABLED = "transcoding_enabled";
    
    private static ConfigManager instance;
    private SharedPreferences prefs;
    
    private ConfigManager() {
        // Private constructor for singleton
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    public void init(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public boolean isAutoStartEnabled() {
        return prefs.getBoolean(KEY_AUTO_START, true);
    }
    
    public void setAutoStartEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply();
    }
    
    public String getDeviceName() {
        return prefs.getString(KEY_DEVICE_NAME, "AirHome Bridge");
    }
    
    public void setDeviceName(String name) {
        prefs.edit().putString(KEY_DEVICE_NAME, name).apply();
    }
    
    public boolean isTranscodingEnabled() {
        return prefs.getBoolean(KEY_TRANSCODING_ENABLED, true);
    }
    
    public void setTranscodingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TRANSCODING_ENABLED, enabled).apply();
    }
}

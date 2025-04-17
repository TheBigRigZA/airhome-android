package com.mediabox.airhome.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mediabox.airhome.R;
import com.mediabox.airhome.service.AirPlayService;
import com.mediabox.airhome.util.ConfigManager;
import com.mediabox.airhome.util.ServiceUtils;

/**
 * Main Activity for the Android TV AirHome app.
 * Provides a simple configuration interface for the AirPlay service.
 */
public class MainActivity extends Activity {
    
    private TextView statusText;
    private EditText deviceNameInput;
    private Switch serviceSwitch;
    private Switch autoStartSwitch;
    private Switch transcodingSwitch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        statusText = findViewById(R.id.status_text);
        deviceNameInput = findViewById(R.id.device_name_input);
        serviceSwitch = findViewById(R.id.service_switch);
        autoStartSwitch = findViewById(R.id.auto_start_switch);
        transcodingSwitch = findViewById(R.id.transcoding_switch);
        
        // Load saved settings
        ConfigManager configManager = ConfigManager.getInstance();
        
        // Set device name from saved settings
        deviceNameInput.setText(configManager.getDeviceName());
        
        // Set auto-start switch
        autoStartSwitch.setChecked(configManager.isAutoStartEnabled());
        autoStartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configManager.setAutoStartEnabled(isChecked);
        });
        
        // Set transcoding switch
        transcodingSwitch.setChecked(configManager.isTranscodingEnabled());
        transcodingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configManager.setTranscodingEnabled(isChecked);
        });
        
        // Set service switch
        serviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startAirPlayService();
            } else {
                stopAirPlayService();
            }
            updateStatus();
        });
        
        // Save button click handler
        findViewById(R.id.save_button).setOnClickListener(v -> saveSettings());
        
        // Help button click handler
        findViewById(R.id.help_button).setOnClickListener(v -> showHelp());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
    
    /**
     * Start the AirPlay service
     */
    private void startAirPlayService() {
        Intent serviceIntent = new Intent(this, AirPlayService.class);
        startService(serviceIntent);
        Toast.makeText(this, "AirPlay service started", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Stop the AirPlay service
     */
    private void stopAirPlayService() {
        Intent serviceIntent = new Intent(this, AirPlayService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "AirPlay service stopped", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Update service status display
     */
    private void updateStatus() {
        // Check if service is running
        boolean isServiceRunning = ServiceUtils.isServiceRunning(this, AirPlayService.class);
        serviceSwitch.setChecked(isServiceRunning);
        
        if (isServiceRunning) {
            statusText.setText(getString(R.string.status_running));
            statusText.setTextColor(getResources().getColor(R.color.status_running));
        } else {
            statusText.setText(getString(R.string.status_stopped));
            statusText.setTextColor(getResources().getColor(R.color.status_stopped));
        }
    }
    
    /**
     * Save user settings
     */
    private void saveSettings() {
        String deviceName = deviceNameInput.getText().toString().trim();
        
        if (deviceName.isEmpty()) {
            Toast.makeText(this, "Device name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save the device name
        ConfigManager.getInstance().setDeviceName(deviceName);
        
        // If service is running, restart it to apply changes
        boolean wasRunning = serviceSwitch.isChecked();
        if (wasRunning) {
            stopAirPlayService();
            startAirPlayService();
        }
        
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show help information
     */
    private void showHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }
    
    /**
     * Handle remote control keys
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // Handle selection with D-pad
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    focusedView.performClick();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
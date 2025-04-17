package com.mediabox.airhome.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mediabox.airhome.R;
import com.mediabox.airhome.audio.AudioHandler;
import com.mediabox.airhome.ui.MainActivity;
import com.mediabox.airhome.util.ConfigManager;

/**
 * Foreground service that handles the AirPlay functionality.
 * Manages the mDNS advertiser and AirPlay server.
 */
public class AirPlayService extends Service {
    private static final String TAG = "AirPlayService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "airhome_channel";
    
    private PowerManager.WakeLock wakeLock;
    private MDNSManager mdnsManager;
    private AirPlayServer airPlayServer;
    private AudioHandler audioHandler;
    private boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AirPlay service creating");
        
        // Create notification channel for Android O+
        createNotificationChannel();
        
        // Initialize handlers and managers
        audioHandler = new AudioHandler(this);
        mdnsManager = new MDNSManager(this);
        airPlayServer = new AirPlayServer(this, audioHandler);
        
        // Acquire wake lock to keep CPU running
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AirHome:AirPlayServiceWakeLock");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AirPlay service starting");
        
        // Start as a foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification());
        
        if (!isRunning) {
            // Acquire wake lock
            wakeLock.acquire();
            
            // Initialize audio handler
            audioHandler.initialize();
            
            // Start the AirPlay server
            String deviceName = ConfigManager.getInstance().getDeviceName();
            mdnsManager.startService(deviceName);
            airPlayServer.start();
            
            isRunning = true;
            Log.i(TAG, "AirPlay service started successfully");
        }
        
        // Restart if killed
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "AirPlay service destroying");
        
        // Stop the AirPlay server
        if (airPlayServer != null) {
            airPlayServer.stop();
        }
        
        // Stop mDNS service
        if (mdnsManager != null) {
            mdnsManager.stopService();
        }
        
        // Cleanup audio handler
        if (audioHandler != null) {
            audioHandler.cleanup();
        }
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        isRunning = false;
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            
            channel.setDescription(getString(R.string.notification_channel_description));
            channel.setShowBadge(false);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_airplay)
                .setContentIntent(pendingIntent)
                .build();
    }
}
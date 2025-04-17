package com.mediabox.airhome.service;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Manages mDNS (Bonjour) service registration and discovery.
 * Used to advertise our service as an AirPlay receiver on the network.
 */
public class MDNSManager {
    private static final String TAG = "MDNSManager";
    
    private static final String SERVICE_TYPE = "_raop._tcp.local.";
    private static final int AIRPLAY_PORT = 5000;
    
    private final Context context;
    private JmDNS jmDNS;
    private WifiManager.MulticastLock multicastLock;
    private ServiceInfo serviceInfo;
    
    public MDNSManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Start advertising the AirPlay service on the network
     * 
     * @param deviceName The name to advertise on the network
     */
    public void startService(String deviceName) {
        Log.d(TAG, "Starting mDNS service with name: " + deviceName);
        
        // Acquire multicast lock to receive multicast packets
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("AirHomeMulticastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
        
        try {
            // Create JmDNS instance
            jmDNS = JmDNS.create();
            
            // Create service properties
            Map<String, String> props = createServiceProperties(deviceName);
            
            // Create a unique service name based on device name and MAC address
            String serviceName = deviceName + "@" + getMacAddress();
            
            // Register service
            serviceInfo = ServiceInfo.create(
                    SERVICE_TYPE,    // Service type
                    serviceName,     // Service name
                    AIRPLAY_PORT,    // Port
                    0,               // Weight
                    0,               // Priority
                    props            // Properties
            );
            
            jmDNS.registerService(serviceInfo);
            Log.d(TAG, "mDNS service registered successfully as: " + serviceName);
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to start mDNS service", e);
        }
    }
    
    /**
     * Stop advertising the service and cleanup resources
     */
    public void stopService() {
        Log.d(TAG, "Stopping mDNS service");
        
        if (jmDNS != null) {
            if (serviceInfo != null) {
                jmDNS.unregisterService(serviceInfo);
                serviceInfo = null;
            }
            
            try {
                jmDNS.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing JmDNS", e);
            }
            jmDNS = null;
        }
        
        if (multicastLock != null && multicastLock.isHeld()) {
            multicastLock.release();
            multicastLock = null;
        }
    }
    
    /**
     * Create service properties for AirPlay advertisement
     * 
     * @param deviceName The device name to advertise
     * @return Map of properties for AirPlay service
     */
    private Map<String, String> createServiceProperties(String deviceName) {
        Map<String, String> props = new HashMap<>();
        
        // AirPlay service properties
        props.put("deviceid", getMacAddress());
        props.put("features", "0x5A7FFFF7,0x1E");
        props.put("model", "AndroidTV");
        props.put("srcvers", "220.68");
        props.put("pw", "false");            // Password protected: false
        props.put("tp", "UDP");              // Transport: UDP
        props.put("vn", "65537");            // Version
        props.put("vs", "220.68");           // Server version
        props.put("sv", "false");            // Is supervised: false
        props.put("et", "0,1,3,5");          // Encryption types supported
        props.put("ek", "1");                // Encryption key
        props.put("cn", "0,1,2,3");          // Audio codecs
        props.put("ch", "2");                // Channels: stereo
        props.put("ss", "16");               // Sample size: 16 bits
        props.put("sr", "44100");            // Sample rate: 44.1 kHz
        props.put("txtvers", "1");           // TXT record version
        props.put("sf", "0x4");              // Supported formats
        props.put("md", "0,1,2");            // Metadata types
        props.put("am", deviceName);         // Device name (AirPlay name)
        
        return props;
    }
    
    /**
     * Get or generate a MAC address for device identification
     * 
     * @return String MAC address or a simulated one
     */
    private String getMacAddress() {
        // In a real app, you would get the device's MAC address
        // For privacy reasons, Android restricts this now, so you might
        // need to generate a stable identifier for your device
        
        // This is a placeholder - in a production app, you'd use a stable 
        // device identifier that persists across app launches
        return "11:22:33:AA:BB:CC";
    }
}
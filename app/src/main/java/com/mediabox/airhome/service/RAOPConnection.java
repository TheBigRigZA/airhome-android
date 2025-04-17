package com.mediabox.airhome.service;

import android.util.Log;

import com.mediabox.airhome.audio.AudioHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles a single RAOP (Remote Audio Output Protocol) connection.
 * This is a simplified implementation that covers the basics of
 * the AirPlay audio protocol.
 */
public class RAOPConnection {
    private static final String TAG = "RAOPConnection";
    
    // RTSP response codes
    private static final String OK = "200 OK";
    private static final String UNAUTHORIZED = "401 Unauthorized";
    private static final String NOT_IMPLEMENTED = "501 Not Implemented";
    
    private final Socket clientSocket;
    private final AudioHandler audioHandler;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private String sessionId;
    
    /**
     * Create a new RAOP connection handler
     * 
     * @param clientSocket The client socket
     * @param audioHandler The audio handler to process audio data
     */
    public RAOPConnection(Socket clientSocket, AudioHandler audioHandler) {
        this.clientSocket = clientSocket;
        this.audioHandler = audioHandler;
    }
    
    /**
     * Process the RAOP connection
     */
    public void process() {
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            
            while (isConnected()) {
                // Read the request line
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                
                // Parse the request
                String[] requestParts = line.split(" ", 3);
                if (requestParts.length < 3) {
                    Log.e(TAG, "Invalid RTSP request: " + line);
                    continue;
                }
                
                String method = requestParts[0];
                String uri = requestParts[1];
                
                Log.d(TAG, "Received request: " + method + " " + uri);
                
                // Read headers
                requestHeaders.clear();
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    int colonPos = line.indexOf(':');
                    if (colonPos > 0) {
                        String key = line.substring(0, colonPos).trim();
                        String value = line.substring(colonPos + 1).trim();
                        requestHeaders.put(key, value);
                    }
                }
                
                // Handle the request based on the method
                switch (method) {
                    case "OPTIONS":
                        handleOptions(writer);
                        break;
                    case "ANNOUNCE":
                        handleAnnounce(reader, writer);
                        break;
                    case "SETUP":
                        handleSetup(writer, uri);
                        break;
                    case "RECORD":
                        handleRecord(writer);
                        break;
                    case "SET_PARAMETER":
                        handleSetParameter(reader, writer);
                        break;
                    case "FLUSH":
                        handleFlush(writer);
                        break;
                    case "TEARDOWN":
                        handleTeardown(writer);
                        break;
                    case "GET_PARAMETER":
                        handleGetParameter(writer);
                        break;
                    default:
                        sendResponse(writer, NOT_IMPLEMENTED, null);
                }
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error processing RAOP connection", e);
        }
    }
    
    /**
     * Handle OPTIONS request
     */
    private void handleOptions(PrintWriter writer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");
        headers.put("Apple-Jack-Status", "connected; type=analog");
        
        sendResponse(writer, OK, headers);
    }
    
    /**
     * Handle ANNOUNCE request (session description)
     */
    private void handleAnnounce(BufferedReader reader, PrintWriter writer) throws IOException {
        // Read content length
        int contentLength = getContentLength();
        if (contentLength <= 0) {
            sendResponse(writer, "400 Bad Request", null);
            return;
        }
        
        // Read the SDP data
        char[] sdpBuffer = new char[contentLength];
        int read = reader.read(sdpBuffer, 0, contentLength);
        if (read != contentLength) {
            Log.w(TAG, "Incomplete SDP read: " + read + " of " + contentLength);
        }
        
        String sdp = new String(sdpBuffer, 0, read);
        
        // Parse SDP for audio format information
        // In a real implementation, you would extract codec, sample rate, etc.
        parseSDPData(sdp);
        
        // Send success response
        sendResponse(writer, OK, null);
    }
    
    /**
     * Handle SETUP request (transport setup)
     */
    private void handleSetup(PrintWriter writer, String uri) {
        // Parse transport header
        String transport = requestHeaders.get("Transport");
        if (transport == null) {
            sendResponse(writer, "400 Bad Request", null);
            return;
        }
        
        // Generate a unique session ID if we don't already have one
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        
        // Setup response headers
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Transport", transport + ";server_port=1234");
        responseHeaders.put("Session", sessionId);
        responseHeaders.put("Audio-Jack-Status", "connected; type=analog");
        
        sendResponse(writer, OK, responseHeaders);
    }
    
    /**
     * Handle RECORD request (start streaming)
     */
    private void handleRecord(PrintWriter writer) {
        if (sessionId == null) {
            sendResponse(writer, "400 Bad Request", null);
            return;
        }
        
        // Start audio streaming
        audioHandler.startAudioSession(sessionId);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Audio-Latency", "0");
        
        sendResponse(writer, OK, headers);
    }
    
    /**
     * Handle SET_PARAMETER request (metadata, volume, etc.)
     */
    private void handleSetParameter(BufferedReader reader, PrintWriter writer) throws IOException {
        String contentType = requestHeaders.get("Content-Type");
        int contentLength = getContentLength();
        
        if (contentType == null || contentLength <= 0) {
            sendResponse(writer, OK, null);
            return;
        }
        
        // Read the parameter content
        char[] buffer = new char[contentLength];
        int read = reader.read(buffer, 0, contentLength);
        if (read != contentLength) {
            Log.w(TAG, "Incomplete parameter data read: " + read + " of " + contentLength);
        }
        
        String content = new String(buffer, 0, read);
        
        // Handle different parameter types
        if (contentType.equalsIgnoreCase("text/parameters")) {
            handleTextParameters(content);
        } else if (contentType.equalsIgnoreCase("image/jpeg")) {
            // Handle cover art
            handleCoverArt(content.getBytes());
        } else if (contentType.equalsIgnoreCase("application/x-dmap-tagged")) {
            // Handle metadata
            handleMetadata(content.getBytes());
        }
        
        sendResponse(writer, OK, null);
    }
    
    /**
     * Handle FLUSH request (clear buffers)
     */
    private void handleFlush(PrintWriter writer) {
        if (sessionId == null) {
            sendResponse(writer, "400 Bad Request", null);
            return;
        }
        
        // Flush audio buffer
        audioHandler.flushAudioBuffer(sessionId);
        
        sendResponse(writer, OK, null);
    }
    
    /**
     * Handle TEARDOWN request (end session)
     */
    private void handleTeardown(PrintWriter writer) {
        if (sessionId == null) {
            sendResponse(writer, "400 Bad Request", null);
            return;
        }
        
        // Stop audio streaming
        audioHandler.stopAudioSession(sessionId);
        sessionId = null;
        
        sendResponse(writer, OK, null);
    }
    
    /**
     * Handle GET_PARAMETER request
     */
    private void handleGetParameter(PrintWriter writer) {
        // Typically this is used as a keep-alive ping
        sendResponse(writer, OK, null);
    }
    
    /**
     * Send an RTSP response
     */
    private void sendResponse(PrintWriter writer, String status, Map<String, String> headers) {
        writer.println("RTSP/1.0 " + status);
        writer.println("CSeq: " + requestHeaders.getOrDefault("CSeq", "1"));
        writer.println("Server: AirHome/1.0");
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        
        writer.println();
        writer.flush();
    }
    
    /**
     * Get the content length from headers
     */
    private int getContentLength() {
        String contentLengthStr = requestHeaders.get("Content-Length");
        if (contentLengthStr != null) {
            try {
                return Integer.parseInt(contentLengthStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid Content-Length header: " + contentLengthStr);
            }
        }
        return 0;
    }
    
    /**
     * Parse SDP data to extract audio format information
     */
    private void parseSDPData(String sdp) {
        // This is a simplified version - real implementation would be more complex
        
        // Example: Extract audio format
        Pattern formatPattern = Pattern.compile("a=rtpmap:(\\d+) ([\\w-]+)/(\\d+)");
        Matcher matcher = formatPattern.matcher(sdp);
        
        if (matcher.find()) {
            String formatId = matcher.group(1);
            String codec = matcher.group(2);
            String sampleRate = matcher.group(3);
            
            Log.d(TAG, "Audio format: " + codec + " at " + sampleRate + "Hz");
            audioHandler.setAudioFormat(codec, Integer.parseInt(sampleRate));
        }
    }
    
    /**
     * Handle text parameters (like volume)
     */
    private void handleTextParameters(String content) {
        // Process parameters like volume
        if (content.startsWith("volume:")) {
            try {
                float volume = Float.parseFloat(content.substring(7));
                audioHandler.setVolume(volume);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid volume parameter: " + content);
            }
        }
    }
    
    /**
     * Handle cover art from the client
     */
    private void handleCoverArt(byte[] imageData) {
        // Process album artwork
        audioHandler.updateCoverArt(imageData);
    }
    
    /**
     * Handle metadata from the client
     */
    private void handleMetadata(byte[] metadataBytes) {
        // Process metadata - in a real app, you'd parse the DMAP format
        // This is complex and would require a DMAP parser
        Log.d(TAG, "Received metadata (" + metadataBytes.length + " bytes)");
    }
    
    /**
     * Check if the socket is still connected
     */
    private boolean isConnected() {
        return clientSocket != null && !clientSocket.isClosed() && clientSocket.isConnected();
    }
}
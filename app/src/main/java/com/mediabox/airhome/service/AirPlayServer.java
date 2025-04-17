package com.mediabox.airhome.service;

import android.content.Context;
import android.util.Log;

import com.mediabox.airhome.audio.AudioHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main AirPlay server implementation that listens for incoming connections
 * and handles them using the RAOP protocol.
 */
public class AirPlayServer {
    private static final String TAG = "AirPlayServer";
    private static final int PORT = 5000;
    
    private final Context context;
    private final ExecutorService threadPool;
    private final AudioHandler audioHandler;
    
    private ServerSocket serverSocket;
    private boolean isRunning;
    private Thread serverThread;
    
    /**
     * Create a new AirPlay server
     * 
     * @param context Context used for system resources
     * @param audioHandler Handler for audio processing
     */
    public AirPlayServer(Context context, AudioHandler audioHandler) {
        this.context = context.getApplicationContext();
        this.threadPool = Executors.newCachedThreadPool();
        this.audioHandler = audioHandler;
    }
    
    /**
     * Start the AirPlay server and begin listening for connections
     */
    public void start() {
        Log.d(TAG, "Starting AirPlay server");
        
        if (isRunning) {
            Log.w(TAG, "Server already running");
            return;
        }
        
        serverThread = new Thread(this::runServer, "AirPlayServerThread");
        serverThread.start();
    }
    
    /**
     * Stop the AirPlay server and clean up resources
     */
    public void stop() {
        Log.d(TAG, "Stopping AirPlay server");
        
        isRunning = false;
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket", e);
            }
        }
        
        threadPool.shutdown();
    }
    
    /**
     * Main server loop that accepts connections and dispatches them
     * to handler threads
     */
    private void runServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            
            Log.i(TAG, "AirPlay server started on port " + PORT);
            
            while (isRunning) {
                try {
                    // Wait for incoming connections
                    Socket clientSocket = serverSocket.accept();
                    
                    Log.d(TAG, "New client connection from: " + 
                           clientSocket.getInetAddress().getHostAddress());
                    
                    // Handle client connection in a separate thread
                    threadPool.execute(() -> handleClient(clientSocket));
                    
                } catch (IOException e) {
                    if (isRunning) {
                        Log.e(TAG, "Error accepting client connection", e);
                    }
                }
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error starting server", e);
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing server socket", e);
                }
            }
            Log.d(TAG, "AirPlay server stopped");
        }
    }
    
    /**
     * Handle an individual client connection
     * 
     * @param clientSocket The socket connected to the client
     */
    private void handleClient(Socket clientSocket) {
        try {
            // Create a new RAOP connection handler for this client
            RAOPConnection connection = new RAOPConnection(clientSocket, audioHandler);
            
            // Process the connection (this will block until the connection ends)
            connection.process();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling client", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket", e);
            }
        }
    }
}
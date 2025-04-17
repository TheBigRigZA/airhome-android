package com.mediabox.airhome.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;

import com.mediabox.airhome.R;

/**
 * Activity that displays help information for the user.
 */
public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        // Set up the back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        // Make sure the back button is focused on start
        backButton.requestFocus();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
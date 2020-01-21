package com.madhopssoft.pro1kbbacklighttoggle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Pro1KBBacklightToggle";
    static Activity thisActivity = null;
    private static final String KBBACKLIGHT_FILE = "/sys/class/leds/keyboard-backlight/brightness";
    private static TextView serviceStatusText;
    private static ToggleButton toggleServiceButton;
    private static Switch startupSwitch;
    public static final String PREFS_NAME = "P1KBPrefsFile";
    public static Boolean startOnBoot = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;
        serviceStatusText = findViewById(R.id.serviceStatusText);
        toggleServiceButton = findViewById(R.id.toggleServiceButton);
        startupSwitch = findViewById(R.id.startOnBootSwitch);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        startOnBoot = settings.getBoolean("startOnBoot", true);
        startupSwitch.setChecked(startOnBoot);
        startupSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startOnBoot = startupSwitch.isChecked();
                SaveSettings();
            }
        });

        toggleServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleServiceButton.toggle();

                if ( toggleServiceButton.getText().toString().equalsIgnoreCase("START SERVICE")) {

                    //toggleServiceButton.setTextOff("STOP SERVICE");
                    toggleServiceButton.setChecked(true);
                    startService();
                } else if ( toggleServiceButton.getText().toString().equalsIgnoreCase("STOP SERVICE")) {
                    //toggleServiceButton.setTextOn("START SERVICE");
                    toggleServiceButton.setChecked(false);
                    stopService();
                }
            }
        });
    }

    private void SaveSettings() {
        //Save startup preference
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("startOnBoot", startOnBoot);

        // Commit the edits
        editor.apply();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, P1KBBacklightService.class);
        serviceIntent.putExtra("inputExtra", "Turns on the KB Backlight when screen is in landscape");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, P1KBBacklightService.class);
        stopService(serviceIntent);
    }

    public static boolean getLightState()  {
        try {
            FileInputStream fis = new FileInputStream(new File(KBBACKLIGHT_FILE));

            StringBuilder fileContent = new StringBuilder();
            int n;
            byte[] buffer = new byte[128];

            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            if (fileContent.toString().trim().equalsIgnoreCase("1")) {
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.e (TAG, "Error reading light state. " + e.getMessage());
            return false;
        }
    }

    public static void toggleBacklightBit(boolean on) {
        try {
            FileOutputStream fos = new FileOutputStream(KBBACKLIGHT_FILE);
            byte[] bytes = new byte[2];
            bytes[0] = (byte) (on ? '1' : '0');
            bytes[1] = '\n';
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG,"Failed to write bit. " + e.getMessage());
        }

    }
    public static void updateTileStatus(Context context){
        try {
            TileService.requestListeningState(context, new ComponentName(context, P1KBTileService.class));
            Log.d(TAG, "Sending Quicksetting toggle update intent.");
            Intent i = new Intent("TOGGLE_P1KBBL");
            thisActivity.sendBroadcast(i);

        } catch (Exception e) {
            Log.e(TAG, "Failed to send intent. " + e.getMessage());
        }
    }
    public static void updateServiceStatus (String status){
        serviceStatusText.setText("Service Status: " + status);
    }
}

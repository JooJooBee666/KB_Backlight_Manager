package com.madhopssoft.pro1kbbacklighttoggle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
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
    private static Switch keepBacklightOffSwitch;
    public static Boolean startOnBoot = true;
    public static Boolean keepBacklightOff = false;
    boolean serviceBound = false;

    P1KBBacklightService backlightService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;
        serviceStatusText = findViewById(R.id.serviceStatusText);
        toggleServiceButton = findViewById(R.id.toggleServiceButton);
        startupSwitch = findViewById(R.id.startOnBootSwitch);
        keepBacklightOffSwitch = findViewById(R.id.backlightDisabledSwitch);

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        startOnBoot = settings.getBoolean("startOnBoot", true);
        keepBacklightOff = settings.getBoolean("keepBacklightOff", false);
        startupSwitch.setChecked(startOnBoot);
        keepBacklightOffSwitch.setChecked(keepBacklightOff);

        startupSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startOnBoot = startupSwitch.isChecked();
                SaveSettings();
            }
        });

        keepBacklightOffSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                keepBacklightOff = keepBacklightOffSwitch.isChecked();
                SaveSettings();
            }
        });

        try {
            if (!serviceBound) {
                //Try to bind to background service if running
                bindService(new Intent(this,
                        P1KBBacklightService.class), mConnection, Context.BIND_AUTO_CREATE);
                serviceBound = true;
            }
        } catch (Exception e) {
           Log.d(TAG,"Service not running.");
        }

        // update the UI based off of the service status (if found)
        if (serviceBound) {
            if (backlightService.serviceRunning) {
                updateServiceStatus("Running");
                toggleServiceButton.setChecked(true);
            } else {
                updateServiceStatus("Stopped");
                toggleServiceButton.setChecked(false);
            }
        }
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
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("startOnBoot", startOnBoot);
        editor.putBoolean("keepBacklightOff", keepBacklightOff);

        // Commit the edits
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound && thisActivity != null) {
            thisActivity.unbindService(mConnection);
            thisActivity = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, P1KBBacklightService.class);
        serviceIntent.putExtra("inputExtra", "Turns on the KB Backlight when screen is in landscape");
        serviceIntent.setAction(Constants.ACTION.START_FOREGROUND_ACTION);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(new Intent(this,
                P1KBBacklightService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, P1KBBacklightService.class);
        serviceIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
        unbindService(mConnection);
        this.stopService(serviceIntent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                P1KBBacklightService.P1KBLocalBinder binder = (P1KBBacklightService.P1KBLocalBinder) service;
                backlightService = binder.getService();
                serviceBound = true;

            }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            backlightService = null;
            serviceBound = false;
        }
    };

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
            Intent i = new Intent(Constants.ACTION.TOGGLE_P1KEYBOARD_BACKLIGHT);
            thisActivity.sendBroadcast(i);

        } catch (Exception e) {
            Log.e(TAG, "Failed to send intent. " + e.getMessage());
        }
    }
    public static void updateServiceStatus (String status){
        serviceStatusText.setText("Service Status: " + status);
    }
}

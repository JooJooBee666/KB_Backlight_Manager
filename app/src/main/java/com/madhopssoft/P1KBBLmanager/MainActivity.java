package com.madhopssoft.P1KBBLmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import static com.madhopssoft.P1KBBLmanager.Constants.*;
import static com.madhopssoft.P1KBBLmanager.Constants.ACTION.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "P1KBBacklightToggle";

    public static Boolean startOnBoot = true;
    public static Boolean keepBacklightOff = false;
    public static Boolean quickSettingEnabled = true;
    boolean serviceBound = false;

    KBBacklightService backlightService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton toggleServiceButton = findViewById(R.id.toggleServiceButton);
        Switch startupSwitch = findViewById(R.id.startOnBootSwitch);
        Switch keepBacklightOffSwitch = findViewById(R.id.backlightDisabledSwitch);
        Switch quicksettingEnabledSwitch = findViewById(R.id.quickSettingEnableSwitch);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        startOnBoot = settings.getBoolean("startOnBoot", true);
        keepBacklightOff = settings.getBoolean("keepBacklightOff", false);
        quickSettingEnabled = settings.getBoolean("quicksettingEnabled", true);
        startupSwitch.setChecked(startOnBoot);
        keepBacklightOffSwitch.setChecked(keepBacklightOff);
        quicksettingEnabledSwitch.setChecked(quickSettingEnabled);

        startupSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Switch startupSwitch= findViewById(R.id.startOnBootSwitch);
                startOnBoot = startupSwitch.isChecked();
                SaveSettings();
            }
        });

        keepBacklightOffSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Switch keepBacklightOffSwitch = findViewById(R.id.backlightDisabledSwitch);
                keepBacklightOff = keepBacklightOffSwitch.isChecked();
                changeTileService();
                SaveSettings();
            }
        });

        quicksettingEnabledSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Switch quicksettingEnabledSwitch = findViewById(R.id.quickSettingEnableSwitch);
                quickSettingEnabled = quicksettingEnabledSwitch.isChecked();
                changeTileService();
                SaveSettings();
            }
        });

        try {
            if (!serviceBound) {
                //Try to bind to background service if running
                bindService(new Intent(this,
                        KBBacklightService.class), mConnection, Context.BIND_AUTO_CREATE);
                serviceBound = true;
            }
        } catch (Exception e) {
           Log.d(TAG,"Service not running.");
        }

        // update the UI based off of the service status (if found)
        if (serviceBound) {
            if (KBBacklightService.serviceRunning) {
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
                ToggleButton toggleServiceButton = findViewById(R.id.toggleServiceButton);
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

        IntentFilter filter = new IntentFilter(getPackageName());
        filter.addAction(UPDATE_SERVICE_STATUS);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void SaveSettings() {
        //Save startup preference
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("startOnBoot", startOnBoot);
        editor.putBoolean("keepBacklightOff", keepBacklightOff);
        editor.putBoolean("quickSettingEnabled", quickSettingEnabled);
        // Commit the edits
        editor.apply();
    }

    private void changeTileService() {
        PackageManager pm = getPackageManager();
        ComponentName component = new ComponentName(this.getPackageName(), KBTileService.class.getName());
        if (quickSettingEnabled) {
            pm.setComponentEnabledSetting(component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            Log.w(TAG, "Failed to unregister receiver. " + e.getMessage());
        }
        if (serviceBound) {
            try {
                getApplicationContext().unbindService(mConnection);
                serviceBound = false;
            } catch (Exception e) {
                Log.w(TAG,"Failed to unregister service bind. " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            this.unregisterReceiver(mBroadcastReceiver);
            serviceBound = false;
        } catch (Exception e) {
            Log.w(TAG, "Failed to unregister receiver. " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(getPackageName());
        filter.addAction(UPDATE_SERVICE_STATUS);
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, KBBacklightService.class);
        serviceIntent.putExtra("inputExtra", getString(R.string.foreground_service_info));
        serviceIntent.setAction(START_FOREGROUND_ACTION);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(new Intent(this,
                KBBacklightService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, KBBacklightService.class);
        serviceIntent.setAction(STOP_FOREGROUND_ACTION);
        unbindService(mConnection);
        this.stopService(serviceIntent);
        startService(serviceIntent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                KBBacklightService.KBLocalBinder binder = (KBBacklightService.KBLocalBinder) service;
                backlightService = binder.getService();
                serviceBound = true;
            }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            backlightService = null;
            serviceBound = false;
        }
    };

    private void updateServiceStatus (String status){
        TextView serviceText = findViewById(R.id.serviceStatusText);
        serviceText.setText(String.format("%s %s", getString(R.string.service_status), status));
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
            if (UPDATE_SERVICE_STATUS.equals(action)) {
                String result = intent.getStringExtra("status");
                updateServiceStatus(result);
            }

        }
    };
}

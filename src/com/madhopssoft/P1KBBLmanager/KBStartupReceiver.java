package com.madhopssoft.P1KBBLmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Objects;

import static com.madhopssoft.P1KBBLmanager.Constants.*;

public class KBStartupReceiver extends BroadcastReceiver {

    private static final String TAG = "P1KBStartup";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED) ||
                Objects.equals(intent.getAction(), Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
                Objects.equals(intent.getAction(), Intent.ACTION_REBOOT)) {

            try {
                switch (intent.getAction()) {
                    case Intent.ACTION_BOOT_COMPLETED:{
                        Log.d(TAG, "Starting service from ACTION_BOOT_COMPLETED");
                        //Verify the option to start on boot is enabled
                        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
                        boolean startOnBoot = settings.getBoolean("startOnBoot", true);
                        if (!startOnBoot) {
                            //Stop the service we started earlier
                            Intent serviceIntent = new Intent(context, KBBacklightService.class);
                            serviceIntent.putExtra("inputExtra", context.getString(R.string.foreground_service_info));
                            serviceIntent.setAction(ACTION.STOP_FOREGROUND_ACTION);
                            context.startForegroundService(serviceIntent);
                            return;
                        }
                        break;
                    }
                    case Intent.ACTION_LOCKED_BOOT_COMPLETED:{
                        Log.d(TAG, "Starting service from ACTION_LOCKED_BOOT_COMPLETED");
                        break;
                    }
                    case Intent.ACTION_REBOOT:{
                        Log.d(TAG, "Starting service from ACTION_REBOOT");
                        break;
                    }
                    default: {
                        break;
                    }
                }

                Intent serviceIntent = new Intent(context, KBBacklightService.class);
                serviceIntent.putExtra("inputExtra", context.getString(R.string.foreground_service_info));
                serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                serviceIntent.setAction(ACTION.START_FOREGROUND_ACTION);
                context.startForegroundService(serviceIntent);
            } catch (Exception e) {
                Log.e(TAG,"Service failed to start on boot.");
                e.printStackTrace();
            }
        }
    }
}
package com.madhopssoft.pro1kbbacklighttoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Objects;

public class P1KBStartupReceiver extends BroadcastReceiver {

    private static final String TAG = "P1KBBL Startup";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED) ||
                Objects.equals(intent.getAction(), Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
                Objects.equals(intent.getAction(), Intent.ACTION_REBOOT)) {

            //Verify the option to start on boot is enabled
            SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
            boolean startOnBoot = settings.getBoolean("startOnBoot", true);
            if (!startOnBoot) {
                return;
            }

            try {
                switch (intent.getAction()) {
                    case Intent.ACTION_BOOT_COMPLETED:{
                        Log.d(TAG, "Starting service from ACTION_BOOT_COMPLETED");
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

                Intent i = new Intent(context, P1KBBacklightService.class);
                i.putExtra("inputExtra", "Turns on the KB Backlight when screen is in landscape");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setAction(Constants.ACTION.START_FOREGROUND_ACTION);
                context.startForegroundService(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
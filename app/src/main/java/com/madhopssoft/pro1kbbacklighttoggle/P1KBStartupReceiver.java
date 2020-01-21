package com.madhopssoft.pro1kbbacklighttoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class P1KBStartupReceiver extends BroadcastReceiver {

    private static final String TAG = "P1KBBL Startup";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            try {
                Log.d(TAG, "Starting service from boot");
                Intent i = new Intent(context, P1KBBacklightService.class);
                i.putExtra("inputExtra", "Turns on the KB Backlight when screen is in landscape");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startForegroundService(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
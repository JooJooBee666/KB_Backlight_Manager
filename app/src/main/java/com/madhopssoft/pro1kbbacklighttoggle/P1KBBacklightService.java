package com.madhopssoft.pro1kbbacklighttoggle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Objects;


public class P1KBBacklightService extends Service {

    private final String TAG = "P1KBBacklightService";
    private final int LID_CLOSED = 0;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static boolean serviceRunning = false;
    private static boolean kbOpened = false;
    // Binder given to clients
    private final IBinder serviceBinder = new P1KBLocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(lineageos.content.Intent.ACTION_LID_STATE_CHANGED);
        this.registerReceiver(mBroadcastReceiver, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunning = false;
        this.unregisterReceiver(mBroadcastReceiver);
        try {
            MainActivity.updateServiceStatus("Stopped");
        } catch (Exception e) {
            Log.w(TAG,"Failed to update service status on MainActivity.");
            e.printStackTrace();
        }
        Log.d(TAG,"Service DESTRUCTION!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Objects.requireNonNull(intent.getAction()).equals(Constants.ACTION.START_FOREGROUND_ACTION)) {
            Log.d(TAG, "Received Start Foreground Intent");
            try {
                String input = intent.getStringExtra("inputExtra");
                createNotificationChannel();
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Pro1 KB Backlight Service")
                        .setContentText(input)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_notif2)
                        .setContentIntent(pendingIntent)
                        .build();
                startForeground(1, notification);
                serviceRunning = true;
                try {
                    MainActivity.updateServiceStatus("Running");
                    MainActivity.updateTileStatus(this);
                } catch (Exception e) {
                    Log.w(TAG,"Failed to update service status on MainActivity.");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e(TAG,"Failed to start service.");
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            Log.d(TAG, "Received Stop Foreground Intent");
            //your end servce code
            try {
                MainActivity.updateServiceStatus("Stopped");
            } catch (Exception e) {
                Log.w(TAG,"Failed to update service status on MainActivity.");
                e.printStackTrace();
            }
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class P1KBLocalBinder extends Binder {
        P1KBBacklightService getService() {
            // Return this instance of LocalService so clients can call public methods
            return P1KBBacklightService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }


    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {
            //if the user want's the backlight to stay disabled then ensure it is disabled
            SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
            boolean keepBacklightOff = settings.getBoolean("keepBacklightOff", false);

            // Toggle Backlight when the lid state changes
            if (lineageos.content.Intent.ACTION_LID_STATE_CHANGED.equals(myIntent.getAction())) {


                int lidState = myIntent.getIntExtra(lineageos.content.Intent.EXTRA_LID_STATE, -1);
                if (lidState == LID_CLOSED) {
                    Log.d(TAG,"LID_CLOSED detected.");
                    MainActivity.toggleBacklightBit(false);
                    kbOpened = false;

                } else {
                    Log.d(TAG,"LID_OPEN detected.");
                    if (keepBacklightOff) {
                        MainActivity.toggleBacklightBit(false);
                    } else {
                        MainActivity.toggleBacklightBit(true);
                    }
                    kbOpened = true;
                }
                MainActivity.updateTileStatus(context);
            }

            if (myIntent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                //if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (kbOpened) {
                    Log.d(TAG, "Resume - backlight enabled");
                    Log.d(TAG,"LID_OPEN detected.");
                    if (keepBacklightOff) {
                        MainActivity.toggleBacklightBit(false);
                    } else {
                        MainActivity.toggleBacklightBit(true);
                    }
                    MainActivity.updateTileStatus(context);
                }
            }
        }
    };
}

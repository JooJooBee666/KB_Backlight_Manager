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
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;


public class P1KBBacklightService extends Service {

    private final String TAG = "P1KBBacklightService";
    private final int LID_CLOSED = 0;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static boolean serviceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        IntentFilter filter = new IntentFilter();
        //filter.addAction(Intent.ACTION_SCREEN_ON);
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
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MainActivity.updateServiceStatus("Running");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {

            if (lineageos.content.Intent.ACTION_LID_STATE_CHANGED.equals(myIntent.getAction())) {
                int lidState = myIntent.getIntExtra(lineageos.content.Intent.EXTRA_LID_STATE, -1);
                if (lidState == LID_CLOSED) {
                    Log.d(TAG,"LID_CLOSED detected.");
                    MainActivity.toggleBacklightBit(false);

                } else {
                    Log.d(TAG,"LID_OPEN detected.");
                    MainActivity.toggleBacklightBit(true);
                }
                MainActivity.updateTileStatus(context);
            }

            //if (myIntent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            //    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //        Log.d(TAG, "Resume - backlight enabled");
            //        MainActivity.toggleBacklightBit(true);
            //        MainActivity.updateTileStatus(context);
            //    }
            //}
        }
    };
}

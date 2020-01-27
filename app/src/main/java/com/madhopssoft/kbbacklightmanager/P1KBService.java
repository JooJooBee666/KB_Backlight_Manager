package com.madhopssoft.kbbacklightmanager;

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

import com.madhopssoft.kbbacklightmanager.Constants.ACTION;

import java.util.Objects;

import static android.app.NotificationManager.IMPORTANCE_LOW;
import static com.madhopssoft.kbbacklightmanager.MainActivity.*;


public class P1KBService extends Service {

    private final String TAG = "P1KBService";
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
            updateServiceStatus("Stopped");
        } catch (Exception e) {
            Log.w(TAG,"Failed to update service status on MainActivity.");
            e.printStackTrace();
        }
        Log.d(TAG,"Service DESTRUCTION!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            switch (action) {
                case (ACTION.START_FOREGROUND_ACTION): {
                    startThisService(intent, flags, startId);
                    break;
                }
                case (ACTION.STOP_FOREGROUND_ACTION):{
                    Log.d(TAG, "Received Stop Foreground Intent");
                    //your end servce code
                    try {
                        updateServiceStatus("Stopped");
                    } catch (Exception e) {
                        Log.w(TAG,"Failed to update service status on MainActivity.");
                        e.printStackTrace();
                    }
                    stopForeground(true);
                    stopSelf();
                    break;
                }
                default: {
                    Log.w(TAG,"Unknown intent - " + action + ". No action taken.");
                    break;
                }
            }
        } else {
            Log.w(TAG,"No intent sent to service. Starting service.");
            startThisService(intent, flags, startId);
        }
        return START_NOT_STICKY;
    }

    private void startThisService(Intent intent, int flags, int startId){
        Log.d(TAG, "Received Start Foreground Intent");
        try {
            String input = intent.getStringExtra("inputExtra");
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Pro1 KB Backlight Manager")
                    .setContentText(input)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_notif2)
                    .setPriority(IMPORTANCE_LOW)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            serviceRunning = true;
            try {
                updateServiceStatus("Running");
                updateTileStatus(this);
            } catch (Exception e) {
                Log.w(TAG,"Failed to update service status on MainActivity.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG,"Failed to start service.");
            e.printStackTrace();
        }

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
    class P1KBLocalBinder extends Binder {
        P1KBService getService() {
            // Return this instance of LocalService so clients can call public methods
            return P1KBService.this;
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
                    toggleBacklightBit(false);
                    kbOpened = false;

                } else {
                    Log.d(TAG,"LID_OPEN detected.");
                    if (keepBacklightOff) {
                        toggleBacklightBit(false);
                    } else {
                        toggleBacklightBit(true);
                    }
                    kbOpened = true;
                }
                updateTileStatus(context);
            }

            if (Objects.requireNonNull(myIntent.getAction()).equals(Intent.ACTION_SCREEN_ON)){
                //if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (kbOpened) {
                    Log.d(TAG, "Resume - backlight enabled");
                    Log.d(TAG,"LID_OPEN detected.");
                    if (keepBacklightOff) {
                        toggleBacklightBit(false);
                    } else {
                        toggleBacklightBit(true);
                    }
                    updateTileStatus(context);
                }
            }
        }
    };
}

package com.madhopssoft.pro1kbbacklighttoggle;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.appcompat.widget.AppCompatDrawableManager;



public class P1KBTileService extends TileService {

    private static final String TAG = "P1KBTileService";

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.i(TAG, "onTileAdded");
        Tile tile = getQsTile();
        if (tile != null) {
            if (MainActivity.getLightState()) {
                tile.setState(Tile.STATE_ACTIVE);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }
            tile.updateTile();
        }
        super.onStartListening();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.i(TAG, "onTileRemoved");
    }

    @Override
    public void onStartListening() {

        Log.i(TAG, "onStartListening");
        Tile tile = getQsTile();
        if (tile != null) {
            if (MainActivity.getLightState()) {
                tile.setState(Tile.STATE_ACTIVE);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }
            tile.updateTile();
        }
        try {
            new IntentFilter();
            IntentFilter filter;
            filter = new IntentFilter("TOGGLE_P1KBBL");
            this.registerReceiver(toggleReceiver, filter);

        } catch (Exception e) {
            Log.e(TAG,"Failed to register receiver. " + e.getMessage());
        }
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.i(TAG, "onStopListening");
        this.unregisterReceiver(toggleReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        Log.i(TAG, "onClick");

        //Start main activity and close Quick Settings Panel
        Intent updateIntent = new Intent( this, MainActivity.class);
        updateIntent.putExtra("methodName","toggleBacklight");
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        //update tile
        try {
            updateTileForOnOrOff();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        update tile by Animation
        updateTileByAnimation();

        show dialog
        generateDialogAndShow();

        checking lock and if user enter password, run runnableForUnlock
        unlockAndRun(runnableForUnlock);
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    public void updateTileForOnOrOff() {

        Tile tile = this.getQsTile();

        boolean lightEnabled = MainActivity.getLightState();

        if (lightEnabled) {
            MainActivity.toggleBacklightBit(false);
        } else
        {
            MainActivity.toggleBacklightBit(true);
        }

        //Icon icon;
        //String label;
        int state;

        // Change the tile to match the service status.
        if (tile.getState() == Tile.STATE_INACTIVE) {

        //    label = "On ";
            MainActivity.toggleBacklightBit(true);
        //    icon = Icon.createWithResource(getApplicationContext(), R.drawable.ic_world);

            state = Tile.STATE_ACTIVE;

        } else {

         //   label = "Off";
            MainActivity.toggleBacklightBit(false);
        //    icon = Icon.createWithResource(getApplicationContext(), android.R.drawable.ic_dialog_alert);

            state = Tile.STATE_INACTIVE;
        }

        // Update the UI of the tile.
        // tile.setLabel(label);
        // tile.setIcon(icon);
        tile.setState(state);

        // call updateTile to show changes.
        tile.updateTile();
    }

    private void updateTileByAnimation() {

        ValueAnimator rotationIcon = ValueAnimator.ofInt(0, 360);
        rotationIcon.setDuration(360 * 16);
        rotationIcon.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {

                Integer angle = (Integer) animation.getAnimatedValue();

                Bitmap iconBitmap = getBitmapFromVectorDrawable(getApplicationContext(),
                        R.drawable.ic_world);

                Matrix matrix = new Matrix();
                matrix.postRotate(angle);
                iconBitmap = Bitmap.createBitmap(iconBitmap, 0, 0, iconBitmap.getWidth(), iconBitmap.getHeight(), matrix, true);

                //get tile and change icon
                Tile tile = getQsTile();
                Icon icon = Icon.createWithBitmap(iconBitmap);
                tile.setIcon(icon);
                tile.setState(Tile.STATE_ACTIVE);
                tile.setContentDescription("ffffff");

                // Need to call updateTile for the tile to pick up changes.
                tile.updateTile();
            }
        });
        rotationIcon.start();
    }

    @SuppressLint("RestrictedApi")
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public BroadcastReceiver toggleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(Constants.ACTION.TOGGLE_P1KEYBOARD_BACKLIGHT)) {
                Log.d(TAG, "Received Toggle Intent");
                updateTileForOnOrOff();
            }
        }

    };

    public void generateDialogAndShow() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Dialog");
        builder.setMessage("Hello...");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        showDialog(builder.create());
    }

    public Runnable runnableForUnlock = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "runnableForUnlock");
        }
    };


}
package com.madhopssoft.pro1kbbacklighttoggle;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Pro1KBBacklightToggle";
    static Activity thisActivity = null;
    private static final String KBBACKLIGHT_FILE = "/sys/class/leds/keyboard-backlight/brightness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;
    }



    public static boolean getLightState() throws IOException {

        FileInputStream fis = new FileInputStream (new File(KBBACKLIGHT_FILE));

        StringBuffer fileContent = new StringBuffer("");
        int n;
        byte[] buffer = new byte[128];

        while ((n = fis.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, n));
        }
        if (fileContent.toString().trim().equalsIgnoreCase("1")) {
            return true;
        }
        return false;

    }

    public static void toggleBacklight(String state) throws IOException {
        Boolean status = getLightState();

        if (status) {
            toggleBacklightBit(true);
            Toast.makeText(thisActivity, "Backlight disabled", Toast.LENGTH_SHORT).show();

        } else {
            toggleBacklightBit(false);
            Toast.makeText(thisActivity, "Backlight enabled", Toast.LENGTH_SHORT).show();
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
}

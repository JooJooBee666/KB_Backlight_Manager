package com.madhopssoft.P1KBBLmanager;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class KBLightsMethods {
    private static final String TAG = "P1KBLightsMethods";

    static boolean getLightState()  {
        try {
            FileInputStream fis = new FileInputStream(new File(Constants.KBBACKLIGHT_FILE));

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
            Log.e (TAG, "Error reading light state.");
            e.printStackTrace();
            return false;
        }
    }

    static void toggleBacklightBit(boolean on) {
        try {
            FileOutputStream fos = new FileOutputStream(Constants.KBBACKLIGHT_FILE);
            byte[] bytes = new byte[2];
            bytes[0] = (byte) (on ? '1' : '0');
            bytes[1] = '\n';
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG,"Failed to write backlight bytes.");
            e.printStackTrace();
        }

    }
}

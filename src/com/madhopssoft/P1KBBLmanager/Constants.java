package com.madhopssoft.P1KBBLmanager;

class Constants {

    public static class ACTION {
        static final String START_FOREGROUND_ACTION = "START_FOREGROUND_ACTION";
        static final String STOP_FOREGROUND_ACTION = "STOP_FOREGROUND_ACTION";
        static final String TOGGLE_P1KEYBOARD_BACKLIGHT = "TOGGLE_P1KEYBOARD_BACKLIGHT";
        static final String UPDATE_SERVICE_STATUS = "UPDATE_SERVICE_STATUS";
        public static final String SEND_UPDATE_TO_TILE = "SEND_UPDATE_TO_TILE";
    }
    static final String PREFS_NAME = "P1KBPrefsFile";
    static final String KBBACKLIGHT_FILE = "/sys/class/leds/keyboard-backlight/brightness";
}

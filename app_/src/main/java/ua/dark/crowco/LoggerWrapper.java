package ua.dark.crowco;

import android.util.Log;

public class LoggerWrapper {

    private static String APP_TAG = "UA.DARK.CROWCO";

    private LoggerWrapper() {
        Log.d(APP_TAG,"You can't instantiate utility class");
    }

    public static void inf(String msg) {
        Log.i(APP_TAG, msg);
    }

    public static void d(String msg) {
        Log.d(APP_TAG, msg);
    }

    public static void err(String msg) {
        Log.e(APP_TAG, msg);
    }
}

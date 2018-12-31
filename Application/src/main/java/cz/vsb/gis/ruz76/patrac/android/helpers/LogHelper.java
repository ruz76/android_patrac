package cz.vsb.gis.ruz76.patrac.android.helpers;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jencek on 26.12.18.
 */

public class LogHelper {
    private static String log;

    public static String getLog() {
        return log;
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
        logit("I: " + tag + ": " + message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
        logit("E: " + tag + ": " + message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        logit("W: " + tag + ": " + message);
    }

    private static void logit(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(log);
        stringBuilder.append("\n");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
        stringBuilder.append(simpleDateFormat.format(new Date()));
        //stringBuilder.append(new Date("YYYY-MM-DD H:i:s"));
        stringBuilder.append(" ");
        stringBuilder.append(message);
        log = stringBuilder.toString();
    }
}

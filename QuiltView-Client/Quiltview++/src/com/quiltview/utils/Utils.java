
package com.quiltview.utils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.quiltview.TimelineService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

public class Utils {

    private final static String TAG = "Utils";

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public static String getDeviceId(Context context) {

        String prop = "ro.serialno.glass";
        String result = null;
        try {
            Class SystemProperties = context.getClassLoader().loadClass(
                    "android.os.SystemProperties");

            Class[] paramtypes = new Class[1];
            paramtypes[0] = String.class;
            Object[] paramvalues = new Object[1];
            paramvalues[0] = new String(prop);
            Method get = SystemProperties.getMethod("get", paramtypes);

            result = (String) get.invoke(SystemProperties, paramvalues);
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * Convert timestamp to milliseconds
     * 
     * @param dateTime
     * @return
     */
    public static long convertToMilliseconds(String dateTime) {
        // Sat, 15 Nov 2014 00:29:53 GMT
        try {
            if (!TextUtils.isEmpty(dateTime)) {
                String format = "EEE, d MMM yyyy HH:mm:ss Z";
                SimpleDateFormat pattern = new SimpleDateFormat(format, Locale.getDefault());
                // System.out.println("TBefore: " + dateTime + "Time:  " +
                // pattern.parse(dateTime).getTime());
                return pattern.parse(dateTime).getTime();
            }

        } catch (Exception e) {
            LogUtils.LOGE(TAG, "" + e.getMessage());
        }

        return 0;
    }

    public static boolean isExpired(long timestamp) {
        boolean isExpired = false;

        long currentTimeStamp = System.currentTimeMillis();

        if ((currentTimeStamp - timestamp) >= Constants.EXPIRY_TIMESTAMP)
            isExpired = true;
        else
            isExpired = false;

        // System.out.println("TisExpired: " + isExpired);
        return isExpired;
    }

    /**
     * 
     * @param context
     */
    public static void cancelPollingEvent(Context context) {

        Intent myIntent = new Intent(context, TimelineService.class);
        myIntent.putExtra(Constants.TAG_POLLING_INTENT, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    /**
     * 
     * @param context
     */
    public static void setPollingAlarmManager(Context context) {
        Intent myIntent = new Intent(context, TimelineService.class);
        myIntent.putExtra(Constants.TAG_POLLING_INTENT, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + Constants.INITIAL_DELAY_MILIIS, Constants.DELAY_MILIIS, pendingIntent);
    }
}

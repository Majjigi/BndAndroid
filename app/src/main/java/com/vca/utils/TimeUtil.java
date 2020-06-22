package com.vca.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    public static String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("ddMMYHHmmss");
        String time = df.format(Calendar.getInstance().getTime());
        Log.d("prabhu", "getCurrentTime: df " + time);
        return time;
    }

    public static String getCurrentDate() {
        DateFormat df = new SimpleDateFormat("dd-MM-YYYY");
        String time = df.format(Calendar.getInstance().getTime());
        Log.d("prabhu", "getCurrentTime: df " + time);
        return time;
    }

}

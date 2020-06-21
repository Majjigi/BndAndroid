package com.vca.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    public static String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("ddMMYHHmmss");
        DateFormat df1 = new SimpleDateFormat("ddMMYYHHmmss");
        DateFormat df2 = new SimpleDateFormat("ddMMyHHmmss");
        DateFormat df3 = new SimpleDateFormat("ddMMYYYYHHmmss");
        String time = df.format(Calendar.getInstance().getTime());
        Log.d("prabhu", "getCurrentTime: df " + time);
        return time;
    }

}

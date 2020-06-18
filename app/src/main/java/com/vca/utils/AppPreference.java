package com.vca.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This Class is SharedPreference class used to store small data in key value form.
 * Created by Prabhu on 21/7/2019.
 */

public class AppPreference {
    private final String TAG = AppPreference.class.getSimpleName();
    private static String KEY_App_Status = "Vca_APP_STATUS";
    private SharedPreferences mSharedPreferences;

    public AppPreference(Context context) {
        this.mSharedPreferences = context.getSharedPreferences("VCA", Context.MODE_PRIVATE);
    }

    public void putData(String key, int data) {
        mSharedPreferences.edit().putInt(key, data).apply();
    }

    public int getData(String key) {
        return mSharedPreferences.getInt(key, 0);
    }

    public void isFirstTime(boolean status) {
        mSharedPreferences.edit().putBoolean(KEY_App_Status, status).apply();
    }

    public boolean isFirstTime() {
        return mSharedPreferences.getBoolean(KEY_App_Status, true);
    }
}
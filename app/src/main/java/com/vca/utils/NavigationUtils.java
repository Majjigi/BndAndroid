package com.vca.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.vca.activity.homeScreen.MainActivity;
import com.vca.activity.DropBoxSetupActivity;

public class NavigationUtils {
    private static final String TAG = "NavigationUtils";
    private static Fragment mFragment;
    private static FragmentTransaction fragmentTransaction;
    private static FragmentManager fragmentManager;
    private Context mContext;

    public NavigationUtils(Context mContext) {
        this.mContext = mContext;
    }


    public static Fragment getCurrentFragment() {
        return mFragment;
    }

    public void setCurrentFragment(Fragment fragment) {
        mFragment = fragment;
    }

    @TargetApi(21)
    public void navigateToHomeActivity() {
        Log.d(TAG, "gotoHomeScreen");
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
    }

    @TargetApi(21)
    public void navigateToDropBoxSetUpActivity() {
        Log.d(TAG, "gotoDropBoxSetupActivity");
        Intent intent = new Intent(mContext, DropBoxSetupActivity.class);
        mContext.startActivity(intent);
    }
}

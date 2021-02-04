package com.vca.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import com.vca.R;
import com.vca.utils.AppPreference;
import com.vca.utils.NavigationUtils;

public class SplashScreenActivity extends DropboxActivity {
    private final static String TAG = SplashScreenActivity.class.getSimpleName();
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private final int SPLASH_SCREEN_TIME_OUT = 3000;
    private int PERMISSION_ALL = 1;
    public NavigationUtils navigationUtils;
    AppPreference appPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        navigationUtils = new NavigationUtils(this);
        appPreference = new AppPreference(this);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestPermission();
    }

    public void goToNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (appPreference.isFirstTime() || !hasToken()) {
                    navigationUtils.navigateToHomeActivity();
                } else {
                    navigationUtils.navigateToHomeActivity();
                }
                finish();
            }
        }, SPLASH_SCREEN_TIME_OUT);
    }

    private void checkAndRequestPermission() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            goToNextActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            goToNextActivity();
        } else {
            /* Here we have to handle Denay permitions */
        }
    }


    @Override
    protected void loadData() {

    }
}

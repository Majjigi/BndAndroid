package com.vca.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.users.FullAccount;
import com.vca.R;
import com.vca.activity.homeScreen.MainActivity;
import com.vca.utils.AppPreference;
import com.vca.utils.NavigationUtils;
import com.vca.utils.UIUtil;
import com.vca.utils.dropbox.CreateFileTask;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.GetCurrentAccountTask;

public class DropBoxSetupActivity extends DropboxActivity {
    private final static String TAG = DropBoxSetupActivity.class.getSimpleName();
    Button btnGetStarted;
    Animation btnAnim;
    private boolean isDropBoxSignInRequested = false;
    NavigationUtils navigationUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_drop_box_setup);
        btnGetStarted = findViewById(R.id.btn_get_started);
        isDropBoxSignInRequested = false;
        navigationUtils = new NavigationUtils(this);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        UIUtil.setWhiteNavigationBar(findViewById(android.R.id.content), this);
        getSupportActionBar().hide();

        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);
        btnGetStarted.setVisibility(View.VISIBLE);
        btnGetStarted.setAnimation(btnAnim);
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDropBoxSignInRequested = true;
                DropboxActivity.startOAuth2Authentication(DropBoxSetupActivity.this, getString(R.string.app_key), null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void loadData() {
        Log.d(TAG, "loadData: ");
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                Log.d(TAG, " loadData onComplete: " + result.toString());
                if (isDropBoxSignInRequested) {
                    createDefaultFolders();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadData Failed to get account details.", e);
            }
        }).execute();
    }

    private void createDefaultFolders() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait...");
        dialog.show();

        new CreateFileTask(this, DropboxClientFactory.getClient(), new CreateFileTask.Callback() {
            @Override
            public void onFilesCreated(CreateFolderBatchLaunch result) {
                Log.d(TAG, "onUploadComplete: " + result.toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        navigationUtils.navigateToHomeActivity();
                        new AppPreference(DropBoxSetupActivity.this).isFirstTime(false);
                        finish();
                    }
                }, 3000);
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Log.e(TAG, "Failed to Create file.", e);
            }
        }).execute("");
    }

}

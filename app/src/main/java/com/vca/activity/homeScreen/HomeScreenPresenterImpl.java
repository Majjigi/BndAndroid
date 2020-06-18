package com.vca.activity.homeScreen;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.core.android.Auth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;
import com.vca.R;
import com.vca.utils.dropbox.DbxRequestConfigFactory;
import com.vca.utils.dropbox.DropboxClientFactory;
import com.vca.utils.dropbox.PicassoClient;

public class HomeScreenPresenterImpl implements HomeScreenPresenter {
    private final static boolean USE_SLT = false; //If USE_SLT is set to true, our Android example
    private Context mContext;
    private HomeScreenView homeScreenView;
    private boolean isLoginRequested = false;

    public HomeScreenPresenterImpl(Context mContext, HomeScreenView homeScreenView) {
        this.mContext = mContext;
        this.homeScreenView = homeScreenView;
    }

    @Override
    public boolean isAccountActivated() {
        SharedPreferences prefs = mContext.getSharedPreferences("dropbox-sample", mContext.MODE_PRIVATE);
        if (USE_SLT) {
            return prefs.getString("credential", null) != null;
        } else {
            String accessToken = prefs.getString("access-token", null);
            return accessToken != null;
        }
    }

    @Override
    public void login() {
        if (USE_SLT) {
//                Auth.startOAuth2PKCE(mContext, R.string.app_key, DbxRequestConfigFactory.getRequestConfig(), scope);
        } else {
            isLoginRequested = true;
            homeScreenView.onLoginStarted();
            Auth.startOAuth2Authentication(mContext, mContext.getString(R.string.app_key));
        }
    }

    @Override
    public void logout() {

    }

    @Override
    public void onResume() {
        SharedPreferences prefs = mContext.getSharedPreferences("dropbox-sample", mContext.MODE_PRIVATE);

        if (USE_SLT) {
            String serailizedCredental = prefs.getString("credential", null);
            if (serailizedCredental == null) {
                DbxCredential credential = Auth.getDbxCredential();

                if (credential != null) {
                    prefs.edit().putString("credential", credential.toString()).apply();
                    initAndLoadData(credential);
                }
            } else {
                try {
                    DbxCredential credential = DbxCredential.Reader.readFully(serailizedCredental);
                    initAndLoadData(credential);
                } catch (JsonReadException e) {
                    throw new IllegalStateException("Credential data corrupted: " + e.getMessage());
                }
            }

        } else {
            String accessToken = prefs.getString("access-token", null);
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token();
                if (accessToken != null) {
                    prefs.edit().putString("access-token", accessToken).apply();
                    initAndLoadData(accessToken);
                }
            } else {
                initAndLoadData(accessToken);
            }
        }

        String uid = Auth.getUid();
        String storedUid = prefs.getString("user-id", null);
        if (uid != null && !uid.equals(storedUid)) {
            prefs.edit().putString("user-id", uid).apply();
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(mContext, DropboxClientFactory.getClient());
        if (isLoginRequested) {
            homeScreenView.onLoginSuccess();
        }else {
            homeScreenView.onRefreshFiles();
        }
    }

    private void initAndLoadData(DbxCredential dbxCredential) {
        DropboxClientFactory.init(dbxCredential);
        PicassoClient.init(mContext, DropboxClientFactory.getClient());
        if (isLoginRequested) {
            homeScreenView.onLoginSuccess();
        }else {
            homeScreenView.onRefreshFiles();
        }
    }
}

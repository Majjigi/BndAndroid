package com.vca.activity.homeScreen;

public interface HomeScreenView {
    public void onLoginStarted();

    public void onLoginError(String error);

    public void onLoginSuccess();

    public void onSignOutStarted();

    public void onSignOut();

    public void onRefreshFiles();
}

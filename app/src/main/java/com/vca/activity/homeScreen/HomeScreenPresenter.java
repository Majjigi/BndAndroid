package com.vca.activity.homeScreen;

public interface HomeScreenPresenter {
    public boolean isAccountActivated();

    public void login();

    public void logout();

    public void onResume();
}

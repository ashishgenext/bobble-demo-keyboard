package com.demo.bobble.keyboard.application;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

public class DemoApplication extends Application {

    private static DemoApplication mApplication;
    private static FirebaseAnalytics mFirebaseAnalytics;



    public static DemoApplication getInstance(){
        return  mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public FirebaseAnalytics getFirebaseAnalytics(){
        return mFirebaseAnalytics;
    }
}


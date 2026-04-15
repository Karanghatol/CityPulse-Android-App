package com.citypulse;

import android.app.Application;
import com.citypulse.local.AppDatabase;
import com.citypulse.utils.SessionManager;

public class CityPulseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
        AppDatabase.get(this);  // warm up DB on start
    }
}

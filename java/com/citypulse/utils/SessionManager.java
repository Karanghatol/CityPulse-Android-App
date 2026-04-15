package com.citypulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — stores logged-in user ID in SharedPreferences.
 * All real data lives in Room. This just remembers who is logged in.
 */
public class SessionManager {
    private static final String PREF    = "cp_session";
    private static final String KEY_ID  = "user_id";
    private static final String KEY_NAME= "user_name";
    private static final String KEY_CITY= "user_city";
    private static final String KEY_PIC = "user_pic";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void init(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx);
    }

    public static SessionManager get() { return instance; }

    public void login(int userId, String name, String city, String pic) {
        prefs.edit()
             .putInt(KEY_ID, userId)
             .putString(KEY_NAME, name)
             .putString(KEY_CITY, city)
             .putString(KEY_PIC,  pic)
             .apply();
    }

    public void logout() { prefs.edit().clear().apply(); }

    public boolean isLoggedIn()   { return prefs.getInt(KEY_ID, -1) != -1; }
    public int     getUserId()    { return prefs.getInt(KEY_ID, -1); }
    public String  getUserName()  { return prefs.getString(KEY_NAME, ""); }
    public String  getUserCity()  { return prefs.getString(KEY_CITY, "Mumbai"); }
    public String  getProfilePic(){ return prefs.getString(KEY_PIC,  ""); }
}

package com.nalsasupport.nalsaacademy.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    private int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "goat_diary";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_STANDARD_ID = "standard_id";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setAsAdmin(boolean isAdmin) {
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.commit();
    }

    public boolean isAdmin() {
        return pref.getBoolean(KEY_IS_ADMIN, false);
    }

    public void setStandardId(String id) {
        editor.putString(KEY_STANDARD_ID, id);
        editor.commit();
    }

    public String getStandardId() {
        return pref.getString(KEY_STANDARD_ID, null);
    }
}

package com.example.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Central access to user settings stored in SharedPreferences. */
public final class AppPrefs {

    private static final String FILE_NAME = "smart_pantry_prefs";

    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";

    private AppPrefs() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public static boolean isShowAlmostThere(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public static void setShowAlmostThere(Context context, boolean show) {
        prefs(context).edit().putBoolean(KEY_SHOW_ALMOST_THERE, show).apply();
    }
}

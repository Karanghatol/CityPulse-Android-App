package com.citypulse.utils;

// Kept as stub — no network needed for inbuilt DB.
// Can be used later if you add sync features.
public class NetworkUtils {
    public static boolean isConnected(android.content.Context ctx) {
        android.net.ConnectivityManager cm =
            (android.net.ConnectivityManager) ctx.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network n = cm.getActiveNetwork();
        if (n == null) return false;
        android.net.NetworkCapabilities c = cm.getNetworkCapabilities(n);
        return c != null && (c.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                          || c.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}

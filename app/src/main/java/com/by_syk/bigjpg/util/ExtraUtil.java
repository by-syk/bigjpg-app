package com.by_syk.bigjpg.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by By_syk on 2017-08-05.
 */

public class ExtraUtil {
    public static boolean isNetworkConnected(Context context, boolean isWifiOnly) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }

        boolean isConnected = networkInfo.isAvailable();
        if (isWifiOnly) {
            isConnected &= networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }

        return isConnected;
    }

    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected(context, false);
    }
}

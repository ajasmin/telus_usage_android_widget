package com.github.ajasmin.telususageandroidwidget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.github.ajasmin.telususageandroidwidget.TelusWidgetPreferences.PreferencesData;
import com.github.ajasmin.telususageandroidwidget.TelusWidgetPreferences.Status;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean failover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            Log.i("CONNECT", "CONNECTIVITY_ACTION   failOver: "+failover+"  noConnectivity:"+noConnectivity);

            if (!noConnectivity) {
                // When connectivity becomes available try to update widget instances
                // for which we couldn't connect before.

                int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, TelusWidgetProvider.class));
                for (int appWidgetId : appWidgetIds) {
                    PreferencesData prefData = TelusWidgetPreferences.getPreferences(appWidgetId);
                    if (prefData.status != Status.OKAY && prefData.status != Status.INVALID_CREDENTIALS) {
                        TelusWidgetUpdateService.updateWidget(context, appWidgetId);
                    }
                }
            }
        }
    }

}

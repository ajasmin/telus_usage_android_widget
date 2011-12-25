/*
 * Copyright (C) 2011 by Alexandre Jasmin <alexandre.jasmin@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/*
 * Part of this was inspired by WakefulIntentService:
 * https://github.com/commonsguy/cwac-wakeful/blob/v0.4.2/src/com/commonsware/cwac/wakeful/WakefulIntentService.java
 */

package com.github.ajasmin.telususageandroidwidget;

import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.ajasmin.telususageandroidwidget.TelusWidgetPreferences.PreferencesData;

public class TelusWidgetUpdateService<E> extends Service {
    private static final String ACTION_UPDATE_WIDGET
            = MyApp.getContext().getPackageName()+".UPDATE_WIDGET";

    private final Map<Integer, Thread> widgetThreads = new HashMap<Integer, Thread>();
    private int lastTaskId;
    private int taskCount = 0;


    private static final String LOCK_NAME
            = MyApp.getContext().getPackageName()+".WakefulIntentService";

    private static final PowerManager.WakeLock wakeLock = initLock();


    public TelusWidgetUpdateService() {
        super();
    }

    protected void updateWidget(int appWidgetId) {
        Context context = MyApp.getContext();

        AppWidgetManager manager = AppWidgetManager.getInstance(this);

        // Don't proceed unless the widget was configured
        PreferencesData prefData = TelusWidgetPreferences.getPreferences(appWidgetId);
        if (prefData.email == null) {
            RemoteViews updateViews = unconfiguredRemoteViews(prefData);
            manager.updateAppWidget(appWidgetId, updateViews);
            return;
        }

        showLoadingMessage(context, appWidgetId, manager);
        RemoteViews updateViews = buildUpdate(prefData);
        manager.updateAppWidget(appWidgetId, updateViews);
    }

    private void showLoadingMessage(Context context, int appWidgetId, AppWidgetManager manager) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_loading_message);
        manager.updateAppWidget(appWidgetId, updateViews);
    }

    /**
     * Build a widget update to show the current usage Will block until the
     * online API returns.
     * @param appWidgetId
     * @param password
     * @param email
     */
    public RemoteViews buildUpdate(PreferencesData prefData) {
        Map<String, Map<String, String>> data = null;
        try {
            // Try fetching data from https://mobile.telus.com
            data = TelusWebScraper.retriveUsageSummaryData(prefData.appWidgetId);
        } catch (TelusWebScraper.InvalidCredentialsException e) {
            Log.e("TelusWebScraper", "Invalid credentials for " + prefData.email, e);

            // Don't cache the response in this case
            getFileStreamPath(""+prefData.appWidgetId).delete();

            return invalidCredentialsRemoteViews(prefData);
        } catch (TelusWebScraper.NetworkErrorException e) {
            Log.e("TelusWebScraper", "Network error scraping mobile.telus.com for " + prefData.email, e);
            return networkErrorRemoteViews(prefData);
        } catch (TelusWebScraper.ParsingDataException e) {
            Log.e("TelusWebScraper", "Error parsing data for " + prefData.email, e);
            return unrecognizedDataRemoteViews(prefData);
        }

        DataPresenter dataPresenter = DataPresenter.getPresenterFor(data);
        if (dataPresenter == null) {
            Log.e("TelusWebScraper", "No presenter found for " + prefData.email);
            return unrecognizedDataRemoteViews(prefData);
        }

        return normalRemoteView(prefData, data, dataPresenter);
    }

    private RemoteViews normalRemoteView(PreferencesData prefData, Map<String, Map<String, String>> data, DataPresenter dataPresenter) {
        RemoteViews updateViews = dataPresenter.buildUpdate(this, data);

        // When user clicks on widget, visit mobile.telus.com
        String loginUriTemplate = "https://mobile.telus.com/login.htm?username=%s&password=%s&_rememberMe=on&forwardAction=/index.htm";
        String loginUri = String.format(loginUriTemplate, Uri.encode(prefData.email), Uri.encode(prefData .password));

        String uri;
        if (prefData.subscriber != null) {
            // Trick to login and redirect to the proper subscriber page
            String indexUri = "https://mobile.telus.com/index.htm?subscriber=" + Uri.encode(prefData.subscriber);
            String htmlTemplate = "<iframe src=\"%s\" onload=\"location.href=%s\" width=\"0\" height=\"0\" style=\"border: 0\"></iframe>"
                    + "Please wait&#133;";
            try {
                String html = String.format(htmlTemplate, TextUtils.htmlEncode(loginUri), TextUtils.htmlEncode(JS.string(indexUri)));
                uri = "data:text/html;charset=UTF-8;base64," + Base64.encodeBytes(html.getBytes("UTF-8"));

            } catch (Exception e) {
                throw new Error(e);
            }
        } else {
            uri = loginUri;
        }

        Log.i("TELUS_URL", uri);


        Intent defineIntent = new Intent();
        defineIntent.setData(Uri.parse(uri));
        defineIntent.setAction(Intent.ACTION_VIEW);
        defineIntent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, prefData.appWidgetId, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
        return updateViews;
    }

    private RemoteViews networkErrorRemoteViews(PreferencesData prefData) {
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_network_error);

        Intent defineIntent = new Intent(this, TelusWidgetProvider.class);
        defineIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { prefData.appWidgetId });
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, prefData.appWidgetId, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return updateViews;
    }

    private RemoteViews invalidCredentialsRemoteViews(PreferencesData prefData) {
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_invalid_credentials_error);

        Intent defineIntent = new Intent(this, ConfigureActivity.class);
        defineIntent.setAction(ConfigureActivity.ACTION_EDIT_CONFIG);
        defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefData.appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, prefData.appWidgetId, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return updateViews;
    }

    private RemoteViews unconfiguredRemoteViews(PreferencesData prefData) {
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_not_configured);

        Intent defineIntent = new Intent(this, ConfigureActivity.class);
        defineIntent.setAction(ConfigureActivity.ACTION_EDIT_CONFIG);
        defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefData.appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, prefData.appWidgetId, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return updateViews;
    }


    private RemoteViews unrecognizedDataRemoteViews(PreferencesData prefData) {
        RemoteViews updateViews;
        updateViews = new RemoteViews(getPackageName(), R.layout.widget_unrecognized_data_error);

        // Submit error report on touch
        Intent defineIntent = new Intent(this, ReportAccountErrorActivity.class);
        defineIntent.setAction(getPackageName()+".UNRECOGNIZED");
        defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefData.appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, prefData.appWidgetId, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return updateViews;
    }

    static private PowerManager.WakeLock initLock() {
        PowerManager mgr=(PowerManager)MyApp.getContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME);
        wakeLock.setReferenceCounted(true);
        return wakeLock;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, final int startId) {
        if (!intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
            stopSelf(startId);
            return;
        }

        // Get intent extras
        Bundle extra = intent.getExtras();
        final int appWidgetId = extra.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

        synchronized (this) {
            lastTaskId = startId;
            // There's no need to spawn more than one thread per widgetId
            if (!widgetThreads.containsKey(appWidgetId)) {
                Thread thread = createWidgetThread(appWidgetId);
                widgetThreads.put(appWidgetId, thread);
                taskCount++;
                thread.start();
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        // fail-safe for crash restart
        if ((flags & START_FLAG_REDELIVERY) != 0) {
            wakeLock.acquire();
        }

        onStart(intent, startId);

        // Redeliver the intent in case of failure
        return START_REDELIVER_INTENT;
    }

    private Thread createWidgetThread(final int appWidgetId) {
        Thread thread = new Thread(TelusWidgetUpdateService.class.toString() + " " + appWidgetId) {
            public void run() {
                try {
                    updateWidget(appWidgetId);

                    // We have to pass the last tasks to stopSelf()
                    // when were're done or we risk the service
                    // being killed early.
                    synchronized (TelusWidgetUpdateService.this) {
                        taskCount--;
                        if (taskCount == 0)
                            stopSelf(lastTaskId);
                    }
                } finally {
                    synchronized (TelusWidgetUpdateService.this) {
                        widgetThreads.remove(appWidgetId);
                    }
                    wakeLock.release();
                }
            }
        };
        return thread;
    }

    public static void updateWidget(Context context, int appWidgetId) {
        Intent intent = new Intent(context, TelusWidgetUpdateService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        wakeLock.acquire();

        context.startService(intent);
    }
}
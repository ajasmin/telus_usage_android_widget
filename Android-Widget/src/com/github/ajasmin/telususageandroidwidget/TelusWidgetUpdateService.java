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

package com.github.ajasmin.telususageandroidwidget;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.ajasmin.telususageandroidwidget.TelusWidgetPreferences.PreferencesData;

public class TelusWidgetUpdateService extends Service {
	public static final String ACTION_UPDATE_WIDGET
			= MyApp.getContext().getPackageName()+".UPDATE_WIDGET";
	
	public static final ConcurrentMap<Integer, Thread> widgetThreads 
			= new ConcurrentHashMap<Integer, Thread>();
	
	
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
        	data = TelusWebScraper.retriveUsageSummaryData(prefData);
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
        String uriTemplate = "https://mobile.telus.com/login.htm?username=%s&password=%s&_rememberMe=on&forwardAction=/index.htm";
        String uri = String.format(uriTemplate, Uri.encode(prefData.email), Uri.encode(prefData .password));
        Log.i("TELUS_URL", uri);
        Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
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
	public int onStartCommand(Intent intent, int flags, final int startId) {
		if (!intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
			stopSelf(startId);
			return 0;
		}
		
		// fail-safe for crash restart
		if ((flags & START_FLAG_REDELIVERY) != 0) {
			wakeLock.acquire();
		}
		
		// Get intent extras
		Bundle extra = intent.getExtras();
		final int appWidgetId = extra.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

		// Start widget thread if it doesn't exists
		Thread thread = new Thread(TelusWidgetUpdateService.class.toString() + appWidgetId) {
			public void run() {
				try {
					updateWidget(appWidgetId);
					stopSelf(startId);
				} finally {
					widgetThreads.remove(appWidgetId);
					wakeLock.release();
				}
			}
		};
		
		Thread previousThread = widgetThreads.putIfAbsent(appWidgetId, thread);
		if (previousThread == null) {
			thread.start();
		}

		// Redeliver the intent in case of failure
		return START_REDELIVER_INTENT;
	}

	public static void updateWidget(Context context, int appWidgetId) {
		Intent intent = new Intent(context, TelusWidgetUpdateService.class);
		intent.setAction(ACTION_UPDATE_WIDGET);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		
		wakeLock.acquire();
		
		context.startService(intent);
	}
}
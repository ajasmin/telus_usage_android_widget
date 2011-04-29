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

package com.github.ajasmin.telususagewidget;

import java.io.IOException;
import java.util.Map;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.ajasmin.telususagewidget.TelusWidgetPreferences.PreferencesData;

public class TelusWidgetUpdateService extends IntentService {
	public static final String ACTION_UPDATE_WIDGET = "UPDATE_WIDGET";

	public TelusWidgetUpdateService() {
		super(TelusWidgetUpdateService.class.getName());
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
			Context context = MyApp.getContext();
			
			// Get intent extras
			Bundle extra = intent.getExtras();
			int appWidgetId = extra.getInt(context.getPackageName() + ".appWidgetId");
			PreferencesData prefData = TelusWidgetPreferences.getPreferences(appWidgetId);

			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			showLoadingMessage(context, appWidgetId, manager);
			RemoteViews updateViews = buildUpdate(prefData);
			manager.updateAppWidget(appWidgetId, updateViews);
		}
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
        	
        	return configRemoteViews(prefData);
        } catch (IOException e) {
        	Log.e("TelusWebScraper", "IOException scraping mobile.telus.com for " + prefData.email, e);
        	return new RemoteViews(getPackageName(), R.layout.widget_network_error);
        } catch (Exception e) {
            Log.e("TelusWebScraper", "Error parsing data for " + prefData.email, e);
            return unrecognizedDataRemoteViews(prefData);
        }
        
        DataPresenter dataPresenter = DataPresenter.getPresenterFor(data);
        if (dataPresenter == null) {
            Log.e("TelusWebScraper", "No presenter found for " + prefData.email);
            return unrecognizedDataRemoteViews(prefData);
        }
		RemoteViews updateViews = dataPresenter.buildUpdate(this, data);

        // When user clicks on widget, visit mobile.telus.com
        String uriTemplate = "https://mobile.telus.com/login.htm?username=%s&password=%s&_rememberMe=on&forwardAction=/index.htm";
        String uri = String.format(uriTemplate, Uri.encode(prefData.email), Uri.encode(prefData .password));
        Log.i("TELUS_URL", uri);
        Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* no requestCode */, defineIntent, 0 /* no flags */);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return updateViews;
    }

    private RemoteViews configRemoteViews(PreferencesData prefData) {
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_invalid_credentials_error);
        
        Intent defineIntent = new Intent(this, ConfigureActivity.class);
        defineIntent.setAction(getPackageName()+".CONFIG_"+prefData.appWidgetId);
        defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefData.appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return updateViews;
    }

	private RemoteViews unrecognizedDataRemoteViews(PreferencesData prefData) {
		RemoteViews updateViews;
		updateViews = new RemoteViews(getPackageName(), R.layout.widget_unrecognized_data_error);
		
		// Submit error report on touch
		Intent defineIntent = new Intent(this, ReportAccountErrorActivity.class);
		defineIntent.setAction(getPackageName()+".REPORT_"+prefData.appWidgetId);
		defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefData.appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, defineIntent, 0);
		updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

		return updateViews;
	}

	public static void updateWidget(Context context, int appWidgetId) {
		// To prevent any ANR timeouts, we perform the update in a service
		Intent intent = new Intent(context, TelusWidgetUpdateService.class);
		intent.setAction(ACTION_UPDATE_WIDGET);
		intent.putExtra(context.getPackageName() + ".appWidgetId", appWidgetId);
		context.startService(intent);
	}
}
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

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.ajasmin.telususagewidget.TelusWidgetPreferences.PreferencesData;

public class TelusWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			PreferencesData prefData = TelusWidgetPreferences.getPreferences(appWidgetId);
			updateWidget(context, appWidgetId, prefData.email, prefData.password);
		}
	}

	public static void updateWidget(Context context, int appWidgetId, String email, String password) {
		if (email == null || password == null)
			return;
		
		Log.i("TelusWidget", "UPDATING " + email);
		// To prevent any ANR timeouts, we perform the update in a service
		Intent intent = new Intent(context, UpdateService.class);
		intent.setAction(UpdateService.ACTION_UPDATE_WIDGET);
		intent.putExtra(context.getPackageName() + ".email", email);
		intent.putExtra(context.getPackageName() + ".password", password);
		intent.putExtra(context.getPackageName() + ".appWidgetId", appWidgetId);
		context.startService(intent);
	}

	public static class UpdateService extends IntentService {
		public static final String ACTION_UPDATE_WIDGET = "UPDATE_WIDGET";

		public UpdateService() {
			super(UpdateService.class.getName());
		}
		
		@Override
		protected void onHandleIntent(Intent intent) {
			if (intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
				Context context = MyApp.getContext();
				
				// Get intent extras
				Bundle extra = intent.getExtras();
				String email = extra.getString(context.getPackageName() + ".email");
				String password = extra.getString(context.getPackageName() + ".password");
				int appWidgetId = extra.getInt(context.getPackageName() + ".appWidgetId");
				
				// Build the widget update for this account
				RemoteViews updateViews = buildUpdate(this, appWidgetId, email, password);
	
				// Push update for this widget to the home screen
				AppWidgetManager manager = AppWidgetManager.getInstance(this);
				manager.updateAppWidget(appWidgetId, updateViews);
			}
		}
		
		/**
		 * Build a widget update to show the current usage Will block until the
		 * online API returns.
		 * @param appWidgetId 
		 * @param password 
		 * @param email 
		 */
		public RemoteViews buildUpdate(Context context, int appWidgetId, String email, String password) {
        	UsageData data = null;
            try {
                // Try fetching data from https://mobile.telus.com
            	data = TelusWebScraper.retriveUsageSummaryData(email, password );
            } catch (TelusWebScraper.InvalidCredentialsException e) {
            	Log.e("TelusWebScraper", "Invalid credentials for " + email, e);
            	
            	RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_invalid_credentials_error);
            	
            	Intent defineIntent = new Intent(context, ConfigureActivity.class);
            	defineIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            	defineIntent.putExtra(context.getPackageName() + ".email", email);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* no requestCode */, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

                return updateViews;
            	
            } catch (Exception e) {
                Log.e("TelusWebScraper", "Couldn't scrap mobile.telus.com for " + email, e);
                return new RemoteViews(context.getPackageName(), R.layout.widget_error);
            }
            
            // Build an update that holds the updated widget contents
            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            
            {
	            String airtimeRemainingMinutes = getString(R.string.airtime_remaining);
	            airtimeRemainingMinutes = String.format(airtimeRemainingMinutes, data.airtimeRemainingMinutes, data.airtimeIncludedMinutes);
	            updateViews.setTextViewText(R.id.airtime_remaining, airtimeRemainingMinutes);
            }

            {
	            String airtimeChargeableMinutes=getString(R.string.airtime_chargeable);
	            airtimeChargeableMinutes = String.format(airtimeChargeableMinutes, data.airtimeChargeableMinutes);
	            updateViews.setTextViewText(R.id.airtime_chargeable, airtimeChargeableMinutes);
            }
            
            {
	            updateViews.setTextViewText(R.id.data, data.dataUsage);
            }
            
            {
	            updateViews.setTextViewText(R.id.data_amount, data.dataAmount);
            }
            
            {
	            updateViews.setTextViewText(R.id.text, data.textUsage);
            }

            {
	            updateViews.setTextViewText(R.id.text_amount, data.textAmount);
            }

            // When user clicks on widget, visit mobile.telus.com
            String uriTemplate = "https://mobile.telus.com/login.htm?username=%s&password=%s&_rememberMe=on&forwardAction=/index.htm";
            String uri = String.format(uriTemplate, Uri.encode(email), Uri.encode(password));
            Log.i("TELUS_URL", uri);
            Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* no requestCode */, defineIntent, 0 /* no flags */);
            updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            return updateViews;
        }
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
	        TelusWidgetPreferences.deletePreferences(appWidgetId);
		}
	}
}

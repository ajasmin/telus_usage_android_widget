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

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AccountErrorDetailsActivity extends Activity {
	public static final String ERROR_MESSAGE
			= MyApp.getContext().getPackageName() + ".ERROR_MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.account_error_details);
		
		Bundle extras = getIntent().getExtras();
		String strMessage = extras.getString(ERROR_MESSAGE);
		final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		
		TextView errorMessage = (TextView) findViewById(R.id.error_message);
		errorMessage.setText(strMessage);
		
		Button openSite = (Button) findViewById(R.id.open_site);
		openSite.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
	        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mobile.telus.com"));
	        startActivity(intent);
		}});
		
		Button refreshWidget = (Button) findViewById(R.id.refresh_widget);
		refreshWidget.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
			getFileStreamPath(""+appWidgetId).delete();
			TelusWidgetUpdateService.updateWidget(AccountErrorDetailsActivity.this, appWidgetId);
			finish();
		}});
		
		Button emailDeveloper = (Button) findViewById(R.id.email_developer);
		emailDeveloper.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
			/* Create the Intent */
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

			/* Fill it with Data */
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"alexandre.jasmin@gmail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Telus-Widget] Issue");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

			/* Send it off to the Activity-Chooser */
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}});		
		
		Button leave = (Button) findViewById(R.id.leave);
		leave.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
			finish();
		}});
	}
}

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

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.ajasmin.telususageandroidwidget.TelusWidgetPreferences.PreferencesData;

public class ConfigureActivity extends Activity {
    static private enum ScrapeResult {
        IN_PROGRESS,
        COMPLETE,
        ERROR,
        PARSING_ERROR
    };

    static private class ScraperThread extends Thread {
        public int appWidgetId;

        public volatile Handler scraperCompletedHandler;
        public volatile ScrapeResult result = ScrapeResult.IN_PROGRESS;
        public volatile String[] subscribers;
        public volatile int errorMessageId;
        @Override
        public void run() {
            ScrapeResult r = ScrapeResult.COMPLETE;
            try {
                TelusReportFetcher.retriveUsageSummaryData(appWidgetId);
                subscribers = ReportParser.subscribers(appWidgetId);
            } catch (TelusReportFetcher.InvalidCredentialsException e) {
                r = ScrapeResult.ERROR;
                errorMessageId = R.string.invalid_credentials;
            } catch (TelusReportFetcher.NetworkErrorException e) {
                Log.e("Telus", "Network Error", e);
                r = ScrapeResult.ERROR;
                errorMessageId = R.string.network_error;
            } catch (ReportParser.ServiceUnavailableException e) {
                r = ScrapeResult.ERROR;
                errorMessageId = R.string.widget_service_unavailable;
            } catch (ReportParser.ParsingError e) {
                r = ScrapeResult.PARSING_ERROR;
            }

            result = r;
            Handler handler = scraperCompletedHandler;
            if (handler != null)
                handler.sendEmptyMessage(0);
        }
    }

    private static Map<Integer, ScraperThread> scraperThreadsMap
        = new HashMap<Integer, ScraperThread>();

    public static final String ACTION_EDIT_CONFIG
        = MyApp.getContext().getPackageName()+".ACTION_EDIT_CONFIG";

    int appWidgetId;
    private ScraperThread scraperThread;

    private ProgressDialog progressDialog;
    private AlertDialog errorDialog;
    private AlertDialog pickSubscriberDialog;

    EditText emailView;
    EditText passwordView;
    Button addButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the back button is pressed.
        setResult(RESULT_CANCELED);

        retriveAppWidgetId();

        setContentView(R.layout.configure);
        findViews();

        looksBetterOnIceCream();

        prefillEmail();
        configureEventHandlers();
        setupValidation();
        prepareSmallPrintTextWithLink();
    }

    private void looksBetterOnIceCream() {
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 11) {
            findViewById(R.id.bottom_pane).setBackgroundColor(Color.BLACK);
        }
    }

    private void retriveAppWidgetId() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Finish the activity if we don't receive an appWidgetId
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish();
    }

    private void findViews() {
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        addButton = (Button) findViewById(R.id.add_button);
    }

    private void prefillEmail() {
        // I had a case where after closing the emulator an
        // old widget ID was reused when adding a new widget.
        // This caused the email address to be prefilled
        // even though I was adding a new widget instance.
        //
        // To avoid that only prefil the email address
        // when "editing" the config.
        if (!ACTION_EDIT_CONFIG.equals(getIntent().getAction()))
            return;

        PreferencesData prefs = TelusWidgetPreferences.getPreferences(appWidgetId);
        String email = prefs.email;
        if (email != null) {
            emailView.setText(email);
        }
    }

    private void configureEventHandlers() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButtonClicked();
            }
        });
    }

    private void setupValidation() {
        TextWatcher validationTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateFields();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        };

        emailView.addTextChangedListener(validationTextWatcher);
        passwordView.addTextChangedListener(validationTextWatcher);
        validateFields();
    }

    private void validateFields() {
        addButton.setEnabled(emailView.getText().length() != 0 && passwordView.getText().length() != 0);
    }

    private void prepareSmallPrintTextWithLink() {
        TextView smallPrint = (TextView) findViewById(R.id.small_print_text);
        smallPrint.setText(Html.fromHtml(getString(R.string.small_print)));
        smallPrint.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void addButtonClicked() {
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        TelusWidgetPreferences.createPreferences(appWidgetId, email, password);

        initiateScraper();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scraperThread = scraperThreadsMap.get(appWidgetId);
        if (scraperThread != null) {
            registerScraperCompletedHandler();
            showScraperState();
        }
    }

    @Override
    protected void onPause() {
        dismissDialogs();
        unregisterPostCompletedHandler();
        super.onPause();
    }

    private void initiateScraper() throws Error {
        scraperThread = new ScraperThread();
        scraperThreadsMap.put(appWidgetId, scraperThread);
        scraperThread.appWidgetId = appWidgetId;
        registerScraperCompletedHandler();
        scraperThread.start();
        showScraperState();
    }

    private void registerScraperCompletedHandler() {
        Handler handler = new Handler() { public void handleMessage(Message msg) {
            // See http://dimitar.me/android-displaying-dialogs-from-background-threads/
            if (!isFinishing()) {
                showScraperState();
            }
        }};
        scraperThread.scraperCompletedHandler = handler;
    }

    private void unregisterPostCompletedHandler() {
        if (scraperThread != null)
            scraperThread.scraperCompletedHandler = null;
    }

    private void showScraperState() {
        switch (scraperThread.result) {
            case IN_PROGRESS:
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(this, null, getString(R.string.configure_loading));
                }
                break;
            case COMPLETE:
                dismissDialogs();
                if (scraperThread.subscribers != null) {
                    if (pickSubscriberDialog == null) {
                        final String[] phoneNumbers = scraperThread.subscribers;
                        pickSubscriberDialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.pick_phone_number)
                            .setCancelable(false)
                            .setItems(phoneNumbers,
                                    new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int item) {
                                        PreferencesData preferences = TelusWidgetPreferences.getPreferences(appWidgetId);
                                        preferences.subscriber = phoneNumbers[item];
                                        preferences.lastUpdateTime = -1;
                                        preferences.save();

                                        scraperThreadsMap.remove(appWidgetId);
                                        finishOk();
                             }})
                            .show();
                    }
                } else {
                    finishOk();
                }
                break;
            case ERROR:
                dismissDialogs();
                if (errorDialog == null) {
                    errorDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.unable_to_add_widget)
                        .setMessage(scraperThread.errorMessageId)
                        .setCancelable(false)
                        .setPositiveButton(R.string.okay, new AlertDialog.OnClickListener() { public void onClick(DialogInterface dialog, int which) {
                            TelusWidgetPreferences.deletePreferences(appWidgetId);

                            scraperThreadsMap.remove(appWidgetId);
                            dismissDialogs();
                        }})
                        .show();
                }
                break;
            case PARSING_ERROR:
                Intent intent = new Intent(ConfigureActivity.this, ReportAccountErrorActivity.class);
                intent.setAction(getPackageName()+".UNRECOGNIZED");
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                startActivity(intent);
                break;
        }
    }

    private void dismissDialogs() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (pickSubscriberDialog != null) {
            pickSubscriberDialog.dismiss();
            pickSubscriberDialog = null;
        }
        if (errorDialog != null) {
            errorDialog.dismiss();
            errorDialog = null;
        }
    }

    private void finishOk() {
        // Update the widget
        TelusWidgetUpdateService.updateWidget(ConfigureActivity.this, appWidgetId);

        // Make sure we pass back the original appWidgetId for RESULT_OK
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);

        finish();
    }
}

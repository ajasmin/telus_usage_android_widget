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

import com.github.ajasmin.telususagewidget.TelusWidgetPreferences.PreferencesData;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigureActivity extends Activity {
	int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
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

        
        setContentView(R.layout.configure);
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        addButton = (Button) findViewById(R.id.add_button);
        
        obtainIntentExtras();
          
        addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ConfigureActivity.this.addButtonClicked();
			}
        });
        		
        setupValidation();
        prepareSmallPrintTextWithLink();
    }


	private void addButtonClicked() {
    	String email = emailView.getText().toString();
    	String password = passwordView.getText().toString();

        TelusWidgetPreferences.savePreferences(appWidgetId, email, password);
        TelusWidgetUpdateService.updateWidget(ConfigureActivity.this, appWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();		
	}

	private void setupValidation() {
		TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        };

        emailView.addTextChangedListener(validationTextWatcher);
        passwordView.addTextChangedListener(validationTextWatcher);
        validateFields();
	}

	private void obtainIntentExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        PreferencesData prefs = TelusWidgetPreferences.getPreferences(appWidgetId);
		String email = prefs.email;
        if (email != null) {
        	emailView.setText(email);
        }
	}

	private void prepareSmallPrintTextWithLink() {
		TextView smallPrint = (TextView) findViewById(R.id.small_print_text);
        smallPrint.setText(Html.fromHtml(getString(R.string.small_print)));
        smallPrint.setMovementMethod(LinkMovementMethod.getInstance());
	}
    
    private void validateFields() {
    	addButton.setEnabled(emailView.getText().length() != 0 && passwordView.getText().length() != 0);
    }
}

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

import android.content.Context;
import android.content.SharedPreferences;

public class TelusWidgetPreferences {
	public static class PreferencesData {
		public String email;
		public String password;
	}
	
	public static PreferencesData getPreferences(int appWidgetId) {
		PreferencesData prefData = new PreferencesData();
		
		Context context = MyApp.getContext();
        SharedPreferences prefs = context.getSharedPreferences("widget", 0);
        prefData.email = prefs.getString(appWidgetId + "_email", null);
        prefData.password = prefs.getString(appWidgetId + "_password", null);
		return prefData;
	}
	
    public static void savePreferences(int appWidgetId, String email, String password) {
    	Context context = MyApp.getContext();
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
        prefs.putString(appWidgetId + "_email", email);
        prefs.putString(appWidgetId + "_password", password);
        prefs.commit();
    }
	
	public static void deletePreferences(int appWidgetId) {
		Context context = MyApp.getContext();
		SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
		prefs.remove(appWidgetId + "_email");
		prefs.remove(appWidgetId + "_password");
		prefs.commit();
	}
}

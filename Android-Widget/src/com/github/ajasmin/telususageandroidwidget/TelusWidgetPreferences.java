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

import android.content.Context;
import android.content.SharedPreferences;

public class TelusWidgetPreferences {
    public static class PreferencesData {
        public int appWidgetId;
        public String email;
        public String password;
        public long lastUpdateTime;

        public void markAsUpdatedNow() {
            lastUpdateTime = System.currentTimeMillis();
            Context context = MyApp.getContext();
            SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
            prefs.putLong(appWidgetId + "_lastUpdateTime", lastUpdateTime);
            prefs.commit();
        }
    }

    public static PreferencesData getPreferences(int appWidgetId) {
        PreferencesData prefData = new PreferencesData();

        Context context = MyApp.getContext();
        SharedPreferences prefs = context.getSharedPreferences("widget", 0);
        prefData.appWidgetId = appWidgetId;
        prefData.email = prefs.getString(appWidgetId + "_email", null);
        String obfuscatedPassword = prefs.getString(appWidgetId + "_password", null);
        if (obfuscatedPassword != null)
            prefData.password = PasswordObfuscator.unobfuscate(obfuscatedPassword);
        prefData.lastUpdateTime = prefs.getLong(appWidgetId + "_lastUpdateTime", 0);
        return prefData;
    }

    public static void savePreferences(int appWidgetId, String email, String password) {
        Context context = MyApp.getContext();
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
        prefs.putString(appWidgetId + "_email", email);
        String obfuscatedPassword = PasswordObfuscator.obfuscate(password);
        prefs.putString(appWidgetId + "_password", obfuscatedPassword);
        prefs.commit();
    }

    public static void deletePreferences(int appWidgetId) {
        Context context = MyApp.getContext();
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
        prefs.remove(appWidgetId + "_email");
        prefs.remove(appWidgetId + "_password");
        prefs.remove(appWidgetId + "_lastUpdateTime");
        prefs.commit();
    }
}

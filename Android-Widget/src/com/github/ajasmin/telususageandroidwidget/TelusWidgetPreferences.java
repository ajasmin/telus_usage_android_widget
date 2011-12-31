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

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.SharedPreferences;

public class TelusWidgetPreferences {
    private final static int VERSION = 1;

    public static class PreferencesData {
        public int appWidgetId;
        public String email;
        public String password;
        public String subscriber;
        public long lastUpdateTime;

        public void markAsUpdatedNow() {
            lastUpdateTime = System.currentTimeMillis();
            save();
        }

        public void save() {
            Context context = MyApp.getContext();
            SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
            prefs.putString(appWidgetId + "_email", email);
            String obfuscatedPassword;
            try {
                obfuscatedPassword = Base64.encodeBytes(password.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
            prefs.putString(appWidgetId + "_password", obfuscatedPassword);
            prefs.putString(appWidgetId + "_subscriber", subscriber);
            prefs.putLong(appWidgetId + "_lastUpdateTime", lastUpdateTime);
            prefs.putInt(appWidgetId + "_prefVer", VERSION);
            prefs.commit();
        }
    }

    public static PreferencesData getPreferences(int appWidgetId) {
        PreferencesData prefData = new PreferencesData();

        Context context = MyApp.getContext();
        SharedPreferences prefs = context.getSharedPreferences("widget", 0);
        prefData.appWidgetId = appWidgetId;
        prefData.email = prefs.getString(appWidgetId + "_email", null);
        prefData.subscriber = prefs.getString(appWidgetId + "_subscriber", null);
        prefData.lastUpdateTime = prefs.getLong(appWidgetId + "_lastUpdateTime", 0);

        // Old obfuscation scheme caused random crashes
        // and was too complex for a mild obfuscation
        if (prefs.getInt(appWidgetId + "_prefVer", 0) == 0) {
            String obfuscatedPassword = prefs.getString(appWidgetId + "_password", null);
            if (obfuscatedPassword != null)
                prefData.password = PasswordObfuscator.unobfuscate(obfuscatedPassword);
            prefData.save();
        } else {
            String obfuscatedPassword = prefs.getString(appWidgetId + "_password", null);
            if (obfuscatedPassword != null) {
                try {
                    prefData.password = new String(Base64.decode(obfuscatedPassword), "UTF-8");
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        return prefData;
    }

    public static void createPreferences(int appWidgetId, String email, String password) {
        PreferencesData prefData = new PreferencesData();

        prefData.appWidgetId = appWidgetId;
        prefData.email = email;
        prefData.password = password;
        prefData.save();
    }

    public static void deletePreferences(int appWidgetId) {
        Context context = MyApp.getContext();
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget", 0).edit();
        prefs.remove(appWidgetId + "_email");
        prefs.remove(appWidgetId + "_password");
        prefs.remove(appWidgetId + "_subscriber");
        prefs.remove(appWidgetId + "_lastUpdateTime");
        prefs.remove(appWidgetId + "_prefVer");
        prefs.commit();
    }
}

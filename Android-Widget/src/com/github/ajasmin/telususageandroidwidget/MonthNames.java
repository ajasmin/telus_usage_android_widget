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

public class MonthNames {
    private static final Map<String, String> fr;
    private static final Map<String, String> en;

    static {
        fr = new HashMap<String, String>();
        fr.put("Jan", "Janvier");
        fr.put("Feb", "Février");
        fr.put("Mar", "Mars");
        fr.put("Apr", "Avril");
        fr.put("May", "Mai");
        fr.put("Jun", "Juin");
        fr.put("Jul", "Juillet");
        fr.put("Aug", "Août");
        fr.put("Sep", "Septembre");
        fr.put("Oct", "Octobre");
        fr.put("Nov", "Novembre");
        fr.put("Dec", "Décembre");

        en = new HashMap<String, String>();
        en.put("Jan", "January");
        en.put("Feb", "February");
        en.put("Mar", "March");
        en.put("Apr", "April");
        en.put("May", "May");
        en.put("Jun", "June");
        en.put("Jul", "July");
        en.put("Aug", "August");
        en.put("Sep", "September");
        en.put("Oct", "October");
        en.put("Nov", "November");
        en.put("Dec", "December");
    }

    public static String getFr(String month) {
        return fr.get(month);
    }

    public static String get(String month) {
        return en.get(month);
    }
}


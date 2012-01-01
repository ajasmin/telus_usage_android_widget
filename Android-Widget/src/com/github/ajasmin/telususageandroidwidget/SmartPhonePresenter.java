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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.widget.RemoteViews;

public class SmartPhonePresenter extends DataPresenter {

    private static final Collection<String> requiredData
        = Arrays.asList(new String[]{"Airtime Usage", "Data Usage", "Text Usage"});

    @Override
    protected Collection<String> getRequiredData() {
        return requiredData;
    }

    @Override
    public RemoteViews buildUpdate(Context context, Map<String, Map<String, String>> data) {
        // Build an update that holds the updated widget contents
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_smart_phone_layout);

        {
            Map<String, String> airtimeUsage = data.get("Airtime Usage");
            String includedMinutes = airtimeUsage.get("Included Minutes");
            if (includedMinutes == null) { includedMinutes = "--"; }
            String remainingMinutes = airtimeUsage.get("Remaining Minutes");
            if (remainingMinutes == null) { remainingMinutes = "--"; }
            String chargeableMinutes = airtimeUsage.get("Chargeable Minutes");
            if (chargeableMinutes == null) { chargeableMinutes = "--"; }

            String airtimeRemainingMinutes = context.getString(R.string.airtime_remaining);
            airtimeRemainingMinutes = String.format(airtimeRemainingMinutes, remainingMinutes, includedMinutes);
            updateViews.setTextViewText(R.id.airtime_remaining, airtimeRemainingMinutes);

            String airtimeChargeableMinutes= context.getString(R.string.airtime_chargeable);
            airtimeChargeableMinutes = String.format(airtimeChargeableMinutes, chargeableMinutes);
            updateViews.setTextViewText(R.id.airtime_chargeable, airtimeChargeableMinutes);
        }

        {
            Map<String, String> dataUsage = data.get("Data Usage");
            String usage = dataUsage.get("Usage");
            if (usage != null) {
                usage = formatMbUsageForCompactness(usage);
            } else {
                usage = "--";
            }
            String amount = dataUsage.get("Amount");
            if (amount == null) { amount = "--"; }

            updateViews.setTextViewText(R.id.data, usage);
            updateViews.setTextViewText(R.id.data_amount, amount);
        }

        {
            Map<String, String> textUsage = data.get("Text Usage");
            String usage = textUsage.get("Usage");
            if (usage != null) {
                usage = usage.replace("Messages", "Msg");
            } else {
                usage = "--";
            }
            String amount = textUsage.get("Amount");
            if (amount == null) { amount = "--"; }

            updateViews.setTextViewText(R.id.text, usage);
            updateViews.setTextViewText(R.id.text_amount, amount);
        }

        return updateViews;
    }

    /**
     * Make the data usage string smaller by replacing " MB" by "M"
     * and rounding off some of the decimals
     */
    private String formatMbUsageForCompactness(String usage) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        if (usage.matches("\\d\\d\\d(\\.\\d+)? MB")) {
            // Round to 1 decimal place
            String s = usage.replace(" MB", "");
            double d = Double.parseDouble(s);
            usage = new DecimalFormat("#0.#", symbols).format(d) + "M";
        } else if (usage.matches("\\d+,\\d\\d\\d(\\.\\d+)? MB")) {
            // Round of 0 decimal places
            String s = usage.replace(",", "").replace(" MB", "");
            double d = Double.parseDouble(s);
            usage = new DecimalFormat("#,###", symbols).format(d) + "M";
        }
        return usage;
    }
}

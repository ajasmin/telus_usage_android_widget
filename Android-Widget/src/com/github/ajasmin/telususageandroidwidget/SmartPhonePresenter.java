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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.RemoteViews;

public class SmartPhonePresenter extends DataPresenter {

    private static final HashMap<String, List<String>> requiredData;
    static {
        requiredData = new HashMap<String, List<String>>();
        requiredData.put("Airtime Usage", Arrays.asList(new String[] {"Included Minutes", "Remaining Minutes", "Chargeable Minutes"}));
        requiredData.put("Data Usage", Arrays.asList(new String[] {"Usage", "Amount"}));
        requiredData.put("Text Usage", Arrays.asList(new String[] {"Usage", "Amount"}));
    }

    @Override
    protected Map<String, List<String>> getRequiredData() {
        return requiredData;
    }

    @Override
    public RemoteViews buildUpdate(Context context, Map<String, Map<String, String>> data) {
        // Build an update that holds the updated widget contents
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_smart_phone_layout);

        {
            Map<String, String> airtimeUsage = data.get("Airtime Usage");
            String includedMinutes = airtimeUsage.get("Included Minutes");
            String remainingMinutes = airtimeUsage.get("Remaining Minutes");
            String chargeableMinutes = airtimeUsage.get("Chargeable Minutes");

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
            usage = usage.replace("Kilobytes", "K");
            String amount = dataUsage.get("Amount");

            updateViews.setTextViewText(R.id.data, usage);
            updateViews.setTextViewText(R.id.data_amount, amount);
        }

        {
            Map<String, String> textUsage = data.get("Text Usage");
            String usage = textUsage.get("Usage");
            usage = usage.replace("Messages", "Msg");
            String amount = textUsage.get("Amount");

            updateViews.setTextViewText(R.id.text, usage);
            updateViews.setTextViewText(R.id.text_amount, amount);
        }

        return updateViews;
    }
}

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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.widget.RemoteViews;

public abstract class DataPresenter {
	private static final DataPresenter[] presenters = {new SmartPhonePresenter()};
	
	public static DataPresenter getPresenterFor(Map<String, Map<String, String>> data) {
		for (DataPresenter p : presenters) {
			if (p.appliesTo(data))
				return p;
		}
		// TODO: What to do if none applies
		throw new Error("No presenter found");
	}
	
	private boolean appliesTo(Map<String, Map<String, String>> data) {
		for (Entry<String, List<String>> e : getRequiredData().entrySet()) {
			Map<String, String> section = data.get(e.getKey());
			if (section == null)
				return false;
			if (!section.keySet().containsAll(e.getValue()))
				return false;
		}
		return true;
	}
	
	protected abstract Map<String, List<String>> getRequiredData();
	public abstract RemoteViews buildUpdate(Context context, Map<String, Map<String, String>> data);
}
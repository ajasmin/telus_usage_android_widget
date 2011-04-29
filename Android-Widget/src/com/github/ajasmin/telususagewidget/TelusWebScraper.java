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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import com.github.ajasmin.telususagewidget.TelusWidgetPreferences.PreferencesData;

public class TelusWebScraper {
	@SuppressWarnings("serial")
	public static class ScrapException extends Exception { }
	
	@SuppressWarnings("serial")
	public static class InvalidCredentialsException extends Exception { }

	public static Map<String, Map<String, String>> retriveUsageSummaryData(final PreferencesData prefs) throws IOException, ParserConfigurationException, SAXException, InvalidCredentialsException {
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		enableAuto302Redirects(httpclient);
		InputStream summaryHtmlStream = fetchUsageSummaryPage(httpclient, prefs);
		
		// Just strip ampersands from input. We don't care about the
		// parts of the document containing character entities anyways
		InputStream stripAmpersandsInputStream = new StripAmpersandInputStream(summaryHtmlStream);
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = spf.newSAXParser();
		TelusSaxHandler handler = new TelusSaxHandler();
		InputSource inputSource = new InputSource(stripAmpersandsInputStream);
		inputSource.setEncoding("UTF-8");
		parser.parse(inputSource, handler);
		
		if (handler.isLoginError()) {
			throw new InvalidCredentialsException();
		}
		
		// Log out to avoid session limit
		// on background thread to avoid extra delay
		new Thread(new Runnable() {	public void run() {
			try {
				fetchLogOutPage(httpclient);
				Log.i("TelusWebScraper", "Logged out " + prefs.email);
			} catch (IOException e) {
				Log.e("TelusWebScraper", "Couldn't fetch logout page for " + prefs.email, e);
			}
		}}).run();

		return handler.getData();
	}

	private static void enableAuto302Redirects(DefaultHttpClient httpclient) {
		httpclient.setRedirectHandler(new RedirectHandler() {
			@Override
			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
				if (response.getStatusLine().getStatusCode() == 302)
					return true;
				return false;
			}
			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
				try {
					return new URI(response.getFirstHeader("Location").getValue());
				} catch (URISyntaxException e) {
					throw new Error(e);
				}
			}
		});
	}

	private static InputStream fetchUsageSummaryPage(DefaultHttpClient httpclient, PreferencesData prefs) throws IOException {
		final String url = "https://mobile.telus.com/login.htm";
		HttpPost httpPost = new HttpPost(url);

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("username", prefs.email));
		formparams.add(new BasicNameValuePair("password", prefs.password));
		formparams.add(new BasicNameValuePair("_rememberMe", "on"));
		formparams.add(new BasicNameValuePair("forwardAction", "/index.htm?lang=en"));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		httpPost.setEntity(entity);

		HttpResponse response = httpclient.execute(httpPost);
		HttpEntity responseEntity = response.getEntity();

		// Save the page so that we can report errors later on
		String fileName = Integer.toString(prefs.appWidgetId);
		FileOutputStream fileOutput = MyApp.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
		responseEntity.writeTo(fileOutput);
		fileOutput.close();
		
		InputStream fileInput = MyApp.getContext().openFileInput(fileName);
		return fileInput;
	}
	
	private static void fetchLogOutPage(DefaultHttpClient httpclient) throws IOException {
		final String url = "https://mobile.telus.com/logout.htm";
		HttpGet httpGet = new HttpGet(url);

		HttpResponse response = httpclient.execute(httpGet);
		HttpEntity responseEntity = response.getEntity();
		// fetch the contents
		responseEntity.getContent().close();
	}
}
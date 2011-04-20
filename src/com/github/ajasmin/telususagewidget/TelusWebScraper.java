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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class TelusWebScraper {
	@SuppressWarnings("serial")
	public static class ScrapException extends Exception {
	}

	public static UsageData retriveUsageSummaryData(String email, String password) throws IOException, ParserConfigurationException, SAXException, ScrapException {
		Document doc = retriveUsageSummaryDocument(email, password);
		UsageData usageSummaryData = new UsageData();
		
		{
			Element table = findTableWithHeading(doc, "Data Usage");
			usageSummaryData.dataUsage = findTableValue(table, "Usage");
			usageSummaryData.dataUsage = usageSummaryData.dataUsage.replace("Kilobytes", "K");
			usageSummaryData.dataAmount = findTableValue(table, "Amount");
		}
		
		{
			Element table = findTableWithHeading(doc, "Text Usage");
			usageSummaryData.textUsage = findTableValue(table, "Usage");
			usageSummaryData.textUsage = usageSummaryData.textUsage.replace("Messages", "Msg");
			usageSummaryData.textAmount = findTableValue(table, "Amount");
		}

		{
			Element table = findTableWithHeading(doc, "Airtime Usage");
			usageSummaryData.airtimeIncludedMinutes = findTableValue(table, "Included Minutes");
			usageSummaryData.airtimeRemainingMinutes = findTableValue(table, "Remaining Minutes");
			usageSummaryData.airtimeChargeableMinutes = findTableValue(table, "Chargeable Minutes");
		}
		
		return usageSummaryData;
	}

	private static Document retriveUsageSummaryDocument(final String email, final String password) throws IOException, ParserConfigurationException, SAXException {
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		enableAuto302Redirects(httpclient);
		String summaryHtml = fetchUsageSummaryPage(httpclient, email, password);
		
		// Log out to avoid session limit
		// Using background thread to avoid extra delay
		new Thread(new Runnable() {	public void run() {
			try {
				fetchLogOutPage(httpclient);
				Log.i("TelusWebScraper", "Logged out " + email);
			} catch (IOException e) {
				Log.e("TelusWebScraper", "Couldn't fetch logout page for " + email, e);
			}
		}}).run();

		// Remove unterminated XML entity
		summaryHtml = summaryHtml.replace("href=\"style.css?pageType=&\"", "");

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(summaryHtml)));
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

	private static String fetchUsageSummaryPage(DefaultHttpClient httpclient, String username, String password) throws IOException {
		final String url = "https://mobile.telus.com/login.htm";
		HttpPost httpPost = new HttpPost(url);

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("username", username));
		formparams.add(new BasicNameValuePair("password", password));
		formparams.add(new BasicNameValuePair("_rememberMe", "on"));
		formparams.add(new BasicNameValuePair("forwardAction", "/index.htm?lang=en"));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		httpPost.setEntity(entity);

		HttpResponse response = httpclient.execute(httpPost);
		HttpEntity responseEntity = response.getEntity();

		return EntityUtils.toString(responseEntity, "UTF-8");
	}
	
	private static void fetchLogOutPage(DefaultHttpClient httpclient) throws IOException {
		final String url = "https://mobile.telus.com/logout.htm";
		HttpGet httpGet = new HttpGet(url);

		HttpResponse response = httpclient.execute(httpGet);
		HttpEntity responseEntity = response.getEntity();
		// fetch the contents
		responseEntity.getContent().close();
	}

	// find the HTML table with the given heading
	private static Element findTableWithHeading(Document doc, String headingName) throws ScrapException {
		NodeList divList = doc.getElementsByTagName("div");
		for (int i = 0; i < divList.getLength(); i++) {
			{
				Element div = (Element) divList.item(i);
				if (!"headingBB".equals(div.getAttribute("class")))
					continue;
				Element b = (Element) div.getElementsByTagName("b").item(0);
				if (!headingName.equals(((Text) b.getFirstChild()).getNodeValue().trim()))
					continue;
			}

			{
				Element div = (Element) divList.item(++i);
				if (!div.getAttribute("class").equals("labelValueBG"))
					throw new ScrapException();
				Element table = (Element) div.getElementsByTagName("table").item(0);
				return table;
			}
		}
		throw new ScrapException();
	}

	// Retrieve a value inside the HTML table
	private static String findTableValue(Element table, String name) throws ScrapException {
		NodeList tdList = table.getElementsByTagName("td");
		for (int i = 0; i < tdList.getLength(); i++) {
			{
				Element td = (Element) tdList.item(i);
				if (!td.getAttribute("class").trim().equals("labelValueLabel"))
					continue;
				if (!((Text) td.getFirstChild()).getNodeValue().trim().equals(name))
					continue;
			}

			{
				Element td = (Element) tdList.item(++i);
				if (!td.getAttribute("class").trim().equals("labelValueValue"))
					throw new ScrapException();
				return ((Text) td.getFirstChild()).getNodeValue().trim();
			}

		}
		throw new ScrapException();
	}
}

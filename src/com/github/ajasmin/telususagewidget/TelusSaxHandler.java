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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import com.github.ajasmin.telususagewidget.repackaged.opencsv.CSVWriter;

public class TelusSaxHandler extends DefaultHandler2 {
	private Map<String, Map<String, String>> data;
	private List<String> columns;
	private String currentHeading;
	private LinkedList<String> tableClasses;
	
	private boolean isLoginError;
	
    private StringBuilder builder;
    
    private StringWriter writer;
    private CSVWriter csv;

	@Override
	public void startDocument() throws SAXException {
		data = new HashMap<String, Map<String,String>>();
		columns = new ArrayList<String>();
		currentHeading = "";
		tableClasses = new LinkedList<String>();
		
		isLoginError = false;
		
        builder = new StringBuilder();

        writer = new StringWriter();
        csv = new CSVWriter(writer);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		builder.setLength(0);
		if (localName.equals("table")) {
			tableClasses.addLast(attributes.getValue("class"));
		}
	};
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String trimedText = builder.toString().trim();
		if (localName.equals("b")) {
			currentHeading = trimedText;
			
			csv.writeNext(new String[] {currentHeading});
		} else if (localName.equals("td")) {
			columns.add(trimedText);
		} else if (localName.equals("tr")) {
			if (!"quickNavList".equals(tableClasses.getLast())) {
				Map<String, String> underHeading = data.get(currentHeading);
				if (underHeading == null) {
					underHeading = new HashMap<String, String>();
					data.put(currentHeading, underHeading);
				}
				underHeading.put(columns.get(0), columns.size() > 0 ? columns.get(1) : "");
			
				String[] csvRow = new String[columns.size()+1];
				csvRow[0] = "";
				System.arraycopy(columns.toArray(), 0, csvRow, 1, columns.size());
				csv.writeNext(csvRow);
			}
			columns.clear();
		} else if (localName.equals("table")) {
			tableClasses.removeLast();
			currentHeading = "";
		} else if (localName.equals("div")) {
			if (trimedText.equals("The email or password you entered is invalid.  Please try again.")) {
				isLoginError = true;
			}
		}
	}

	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        builder.append(ch, start, length);
    }
    
    public Map<String, Map<String, String>> getData() {
    	return data;
    }
    
    public String getCsv() {
    	return writer.toString();
    }
    
    public boolean isLoginError() {
    	return isLoginError;
    }
}

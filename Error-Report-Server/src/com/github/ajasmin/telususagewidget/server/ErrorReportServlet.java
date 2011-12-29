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

package com.github.ajasmin.telususagewidget.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class ErrorReportServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Blob postData = new Blob(readStream(req.getInputStream()));

        Entity report = new Entity("Report");
        report.setProperty("html", postData);
        report.setProperty("timeStamp", new Date());
        report.setProperty("address", req.getRemoteAddr());
        
        String versionCodeStr = req.getParameter("versionCode");
        int versionCode = 0;
        if (versionCodeStr != null) {
            versionCode = Integer.parseInt(versionCodeStr);
        }
        report.setProperty("versionCode", versionCode);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(report);

        resp.setContentType("text/plain");
        resp.getWriter().println("OK");
    }

    private static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int count;
        while ((count = in.read(buf)) > 0)
                byteArrayOutputStream.write(buf, 0, count);
        in.close();
        return byteArrayOutputStream.toByteArray();
    }
}

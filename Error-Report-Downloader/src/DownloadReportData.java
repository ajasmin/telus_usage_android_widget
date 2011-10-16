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


/*
 * Download a bunch of reports stored in the appengine datastore
 * using the remote API
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

public class DownloadReportData {
    private static void downloadReports() throws FileNotFoundException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Report");
        Iterable<Entity> entities = datastore.prepare(query).asIterable();

        for (Entity e : entities) {
            long id = e.getKey().getId();
            Blob html = (Blob) e.getProperty("html");
            Date timeStamp = (Date) e.getProperty("timeStamp");

            String timeStampStr
                = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(timeStamp);

            FileOutputStream fileOut = new FileOutputStream(timeStampStr + "_" + id + ".html");
            fileOut.write(html.getBytes());
            fileOut.close();
        }
    }

    public static void main(String[] args) throws IOException, EntityNotFoundException {
        String username = System.console().readLine("username: ");
        String password = new String(System.console().readPassword("password: "));
        RemoteApiOptions options = new RemoteApiOptions()
            .server("telus-widget-error-reports.appspot.com", 443)
            .credentials(username, password);

        RemoteApiInstaller installer = new RemoteApiInstaller();
        installer.install(options);

        try {
            downloadReports();
        } finally {
            installer.uninstall();
        }
    }
}

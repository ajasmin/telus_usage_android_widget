Telus account usage Widget for Android BETA
===========================================

This Widget displays your [Telus Mobility][1] account usage data on the home screen of an [Android device][2].

The data is retrieved from https://mobile.telus.com and displayed in a compact format on your device home screen.
Touching the widget opens your account in the browser.

Note that *Alexandre Jasmin* the author of this program **is not affiliated with Telus in any way.**


Please DO NOT PURCHASE THIS APP right now
-----------------------------------------

**As of 3 Aug 2011 the site https://mobile.telus.com is down and the widget CAN'T LOG IN ON THE SITE. I can't do anything about it. Call Telus if you want them to fix the problem.** 

Known issues
------------

 - As of **May 19 2011** https://mobile.telus.com seems to be having issues. Users of this widget and myself have been
**unable to access the site** for a few days. This means that **the widget may not currently work**. The exact error
message seems to vary but often states that *"usage information is currently unavailable"*:

![site error](https://github.com/ajasmin/telus_usage_android_widget/raw/master/media/currently_unavailable.png)

**Please make sure that you can access the site before installing this widget and feel free to email me if you have information
about this issue.**

 - Beta-3 has issues with more than one widget instance and breaks compatibility with Android < 2.0
fixes are committed but I'm waiting for the service to come back online before uploading a new build.

 - Accounts with more than one phone are not currently supported [Issue 1][3]

 - With plans that include bonus minutes only the normal (non-bonus) minutes are shown. Normal minutes are only used
after the bonus minutes are depleted. As far as I can tell usage of these initial bonus minutes is not reported
anywhere on the Telus website.

 - Since the author of this program doesn't have access to all types of Telus Mobility accounts the widget may not
work in some cases. If the data from the website is not recognized you will be given the option to review and
securely send a copy of your account data to the author for review. You can also contact the author
alexandre.jasmin@gmail.com or [report an issue][4] on github.


Screenshots
-----------
![Screenshot](https://github.com/ajasmin/telus_usage_android_widget/raw/master/media/screenshot-1-small.png)
![Screenshot](https://github.com/ajasmin/telus_usage_android_widget/raw/master/media/screenshot-2-small.png)
![Screenshot](https://github.com/ajasmin/telus_usage_android_widget/raw/master/media/screenshot-3-small.png)


Source code
-----------

 - Android-Widget/

      Contains the source code for the widget  


 - Error-Report-Server/

      Server that receive error reports. Runs on App Engine for Java


 - Error-Report-Downloader/

      Client app for retrieving the reports stored on App Engine


All of these include an Eclipse project.


Installing
----------

You can obtain a copy from the [download section on github][5] or buy it for a dollar on the [Android Market][6].

Scan this QR Code to download the current version directly:
*Note: Your phone must be configured to accept downloads from unknown sources*:

![QR Code APK](https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=https://github.com/downloads/ajasmin/telus_usage_android_widget/Telus-Usage-BETA-3.apk&chld=H|0)

Scan this QR Code to buy the Widget on the Android Market (and get notified of future updates):

![QR Code Market](https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=market%3A//details%3Fid%3Dcom.github.ajasmin.telususageandroidwidget&chld=H|0)



  [1]: http://www.telusmobility.com/ "Telus Website"
  [2]: http://www.android.com/ "Android Website"
  [3]: https://github.com/ajasmin/telus_usage_android_widget/issues/1
  [4]: https://github.com/ajasmin/telus_usage_android_widget/issues "Issues"
  [5]: https://github.com/ajasmin/telus_usage_android_widget/downloads "github downloads"
  [6]: https://market.android.com/details?id=com.github.ajasmin.telususageandroidwidget "Market download"

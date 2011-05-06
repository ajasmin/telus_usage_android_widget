Telus account usage Widget for Android BETA
===========================================

This Widget displays your [Telus Mobility][1] account usage data on the home screen of an [Android device][2].

The data is retrieved from https://mobile.telus.com and displayed in a compact format on your device home screen.
Touching the widget opens your account in the browser.

Note that *Alexandre Jasmin* the author of this program **is not affiliated with Telus in any way.**


Known issues
------------

With plans that include bonus minutes only the normal (non-bonus) minutes are shown. Normal minutes are only used after the bonus minutes are depleted. As far as I can tell usage of these initial bonus minutes is not reported anywhere on the Telus website.

Since the author of this program doesn't have access to all types of Telus Mobility accounts the widget may not work in some cases. If the data from the website is not recognized you will be given the option to review and securely send a copy of your account data to the author for review. You can also contact the author alexandre.jasmin@gmail.com or [report an issue][3] on github.


Source code
-----------

 - Android-Widget/

      Contains the source code for the widget  


 - Error-Report-Server/

      Server that receive error reports. Runs on App Engine for Java

Both of these include an Eclipse project.


Installing
----------

You can obtain a copy from the [download section on github][4] or buy it for a dollar on the [Android Market][5].

Scan this QR Code to buy the Widget on the Android Market (and get notified of future updates):

![QR Code Market](https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=market%3A//details%3Fid%3Dcom.github.ajasmin.telususageandroidwidget&chld=H|0)


Scan this QR Code to download the current version directly:
*Note: Your phone must be configured to accept downloads from unknown sources*:

![QR Code APK](https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=https://github.com/downloads/ajasmin/telus_usage_android_widget/Telus-Usage-BETA-1.apk&chld=H|0)


  [1]: http://www.telusmobility.com/ "Telus Website"
  [2]: http://www.android.com/ "Android Website"
  [3]: https://github.com/ajasmin/telus_usage_android_widget/issues "Issues"
  [4]: https://github.com/ajasmin/telus_usage_android_widget/downloads "github downloads"
  [5]: https://market.android.com/details?id=com.github.ajasmin.telususageandroidwidget "Market download"
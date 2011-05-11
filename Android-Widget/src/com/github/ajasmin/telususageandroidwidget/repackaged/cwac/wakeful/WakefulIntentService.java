/***
	Copyright (c) 2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

/***** NOTICE *****
 * This file was modified
 * 
 * The original version is available at:
 * https://github.com/commonsguy/cwac-wakeful/blob/v0.4.2/src/com/commonsware/cwac/wakeful/WakefulIntentService.java
 * 
 *  Changes:
 *  	- Attempt to maintain compatibility with Android < 2.0
 *  	  calling setIntentRedelivery() seems unnecessary as
 *        onStartCommand() is overridden.
 *  	- Fix possible issue with if (!getLock(this).isHeld())
 *        when restarting more than one intent after a service kill
 *        it seems the wakeLock was acquired twice but released once
 *        causing a crash. Check for START_FLAG_REDELIVERY instead.
 *      - Initialise wakeLock in a static block.
 */

package com.github.ajasmin.telususageandroidwidget.repackaged.cwac.wakeful;

import com.github.ajasmin.telususageandroidwidget.MyApp;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

abstract public class WakefulIntentService extends IntentService {

	abstract protected void doWakefulWork(Intent intent);
	
	private static final String LOCK_NAME_STATIC="com.github.ajasmin.telususageandroidwidget.repackaged.cwac.wakeful.WakefulIntentService";
	private static final PowerManager.WakeLock lockStatic;
	
	static {
		PowerManager mgr=(PowerManager)MyApp.getContext().getSystemService(Context.POWER_SERVICE);
		lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
		lockStatic.setReferenceCounted(true);
	}
	
	public WakefulIntentService(String name) {
		super(name);
	}
	
	public static void sendWakefulWork(Context ctxt, Intent i) {
		lockStatic.acquire();
		ctxt.startService(i);
	}
	
	public static void sendWakefulWork(Context ctxt, Class clsService) {
		sendWakefulWork(ctxt, new Intent(ctxt, clsService));
	}
	
	@Override
  public int onStartCommand(Intent intent, int flags, int startId) {
		if ((flags | START_FLAG_REDELIVERY) != 0) {	// fail-safe for crash restart
			lockStatic.acquire();
		}

		onStart(intent, startId);
		
		return START_REDELIVER_INTENT;
	}
	
	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doWakefulWork(intent);
		}
		finally {
			lockStatic.release();
		}
	}
}

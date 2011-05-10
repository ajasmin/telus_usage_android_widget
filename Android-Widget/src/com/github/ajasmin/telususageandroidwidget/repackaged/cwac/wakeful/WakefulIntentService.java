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
 * This file was modified to work on Android versions < 2.0
 * 
 * The original version is available at:
 * https://github.com/commonsguy/cwac-wakeful/blob/v0.4.2/src/com/commonsware/cwac/wakeful/WakefulIntentService.java 
 */

package com.github.ajasmin.telususageandroidwidget.repackaged.cwac.wakeful;

import java.lang.reflect.Method;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

abstract public class WakefulIntentService extends IntentService {
	abstract protected void doWakefulWork(Intent intent);
	
	private static final String LOCK_NAME_STATIC="com.commonsware.cwac.wakeful.WakefulIntentService";
	private static volatile PowerManager.WakeLock lockStatic=null;
	private static Method setIntentRedeliveryReflect;
	
	static {
		initCompatibility();
	}
	
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic==null) {
			PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			
			lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
														LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		
		return(lockStatic);
	}
	
	public static void sendWakefulWork(Context ctxt, Intent i) {
		getLock(ctxt).acquire();
		ctxt.startService(i);
	}
	
	public static void sendWakefulWork(Context ctxt, Class clsService) {
		sendWakefulWork(ctxt, new Intent(ctxt, clsService));
	}
	
	public WakefulIntentService(String name) {
		super(name);
		
		setIntentRedeliveryCompat(true);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (!getLock(this).isHeld()) {	// on 2.0+ onStart will have done this alredy
			getLock(this).acquire();
		}
		
		super.onStart(intent, startId);
	}
	
	@Override
  public int onStartCommand(Intent intent, int flags, int startId) {
		if (!getLock(this).isHeld()) {	// fail-safe for crash restart
			getLock(this).acquire();
		}

		onStart(intent, startId);
		
		return(START_REDELIVER_INTENT);
	}
	
	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doWakefulWork(intent);
		}
		finally {
			getLock(this).release();
		}
	}
	

   private static void initCompatibility() {
       try {
    	   setIntentRedeliveryReflect = WakefulIntentService.class.getMethod(
                   "setIntentRedelivery", new Class[] { boolean.class } );
           /* success, this is a newer device */
       } catch (NoSuchMethodException nsme) {
           /* failure, must be older device */
       }
   }
   
   private void setIntentRedeliveryCompat(boolean b) throws Error {
	   if (setIntentRedeliveryReflect != null) {
		   try {
			   setIntentRedeliveryReflect.invoke(this, b);
		   } catch (Exception e) {
			   throw new Error(e);
		   }
	   }
   }
}

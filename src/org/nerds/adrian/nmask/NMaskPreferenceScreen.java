/*
 * Copyright (c) by Adrian Griffis.  All rights reserved.
 */
package org.nerds.adrian.nmask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class NMaskPreferenceScreen extends PreferenceActivity {
	
	public static final String OPT_DELAY = "delay";
	public static final String OPT_DELAY_DEFAULT = "1000";
	public static final String OPT_AD_STOP_UNTIL = "adStopUntil";
	public static final String OPT_IP_ADDR = "ipAddr";
	public static final String OPT_NET_MASK = "netMask";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.nmask_settings);
	}
	
	public static long getPrefDelay(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String delayString = sharedPreferences.getString(OPT_DELAY, OPT_DELAY_DEFAULT);
		long delayInt = -1;
		try {
			delayInt = Long.valueOf(delayString);
		}
		catch (Exception e) {
			// TODO:
		}
		return(delayInt);
	}

	public static long getPrefAdStopUntil(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String adStopUntilString = sharedPreferences.getString(OPT_AD_STOP_UNTIL, "0");
		long adStopUntilInt = -1;
		try {
			adStopUntilInt = Long.valueOf(adStopUntilString);
		}
		catch (Exception e) {
			// TODO:
		}
		return(adStopUntilInt);
	}
	
	public static void setPrefAdStopUntil(Context context, long until) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor spEditor = sharedPreferences.edit();
		spEditor.putString(OPT_AD_STOP_UNTIL, Long.toString(until));
		spEditor.commit();
	}

	public static String getPrefIPAddr(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String ipAddrString = sharedPreferences.getString(OPT_IP_ADDR, "192.168.0.1");
		if (null == ipAddrString) { ipAddrString = ""; }
		return(ipAddrString);
	}
	
	public static void setPrefIPAddr(Context context, String ipAddr) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor spEditor = sharedPreferences.edit();
		if ((null == ipAddr) || (ipAddr.equals(""))) {
			spEditor.remove(OPT_IP_ADDR);
		}
		else {
			spEditor.putString(OPT_IP_ADDR, ipAddr);
		}
		spEditor.commit();
	}

	public static String getPrefNetMask(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String netMaskString = sharedPreferences.getString(OPT_NET_MASK, "");
		if (null == netMaskString) { netMaskString = ""; }
		return(netMaskString);
	}
	
	public static void setPrefNetMask(Context context, String netMask) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor spEditor = sharedPreferences.edit();
		if ((null == netMask) || (netMask.equals(""))) {
			spEditor.remove(OPT_NET_MASK);
		}
		else {
			spEditor.putString(OPT_NET_MASK, netMask);
		}
		spEditor.commit();
	}
}

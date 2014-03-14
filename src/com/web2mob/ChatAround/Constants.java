/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.security.MessageDigest;

import android.provider.Settings;

public final class Constants {
	// You can get at the IMEI number via android.telephony.TelephonyManager's
	// getDeviceId() method, but that's tied to the SIM, and I don't
	// know what will occur if/when Android is ported to non-GSM devices. This
	// one also requires a security permission, IIRC.

	// You can get ANDROID_ID from android.provider.Settings.System, which is
	// "a unique 64-bit value as a hex string"...except on the emulator 
	public static String phoneUniqueId = null;
	public static final String prefsFileName = phoneUniqueId + "_prefs";
	public static final String prefsMapsettingGender_suffix = "_MapsettingGender";
	public static final String appName = "chatAround";
	public static final String appVersion = "v2";
	public static final String hostName = "http://web2mob.com/chatAround/v2/";
	
	public static final int ACTIVITY_CREATEORUPDATEUSER = 0;
	public static final int ACTIVITY_VIEWUSERMAP = 1;
	public static final int ACTIVITY_VIEWMESSAGEDUSERS = 2;
	public static final int ACTIVITY_VIEWMESSAGESFROMUSER = 3;
    public static final int ACTIVITY_CREATEMESSAGE = 4;
    public static final int ACTIVITY_CREATEFEEDBACK = 5;
    public static final int ACTIVITY_TUTORIAL = 6;
    public static final int ACTIVITY_CREDITS = 7;
    
    public static double nycLatitude = 40.755883;//1.352596457;//
    public static double nycLongitude = -73.987248;//103.78991787;//
    public static double nycCoordinates[] = {nycLatitude, nycLongitude};
    
    public static int maxUsersToShowOnMap = 40;
    
    public static final String[] interests = {"books", "football", "soccer"};
    
    public static final long SPLASHSCREEN_DELAYMS = 2000;
    
    public static String getPhoneHashKey()
	{
		return hash256(phoneUniqueId + "bce2a3e43209");//only use hex digits in the salt
	}
    
    public static String hash256(String input)
    {
    	try
    	{
    		MessageDigest md = MessageDigest.getInstance("SHA-256");

    		md.update(input.getBytes(), 0, input.length());

	        byte[] mdbytes = md.digest();
	  
			//convert the byte to hex format method 1
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
	  
	        //convert the byte to hex format method 2
			StringBuffer hexString = new StringBuffer();
	     	for (int i = 0; i < mdbytes.length ; i++) {
	     		hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
	     	}
	  
	     	return hexString.toString();
    	}
    	catch(Exception e)
    	{}
    	return null;
    }
    
    public enum Interests
    {
    	A(0x00000001),
    	B(0x00000002),
    	C(0x00000004);
    	
    	private int code;
    	private Interests(int c)
    	{
    		code = c;
    	}
    	public int getInterest()
    	{
    		return code;
    	}
    }
    
	public enum ErrorCode
	{
		noError(0),
		internalError(1),
		userNotFound(2),
		userNameExists(3);
		
		private int code;
		ErrorCode(int c)
		{
			this.code = c;
		}
	}
}

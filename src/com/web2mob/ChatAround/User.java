/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import android.content.Context;

import android.content.SharedPreferences;

public class User {
	public static User thisUser;
	
	public static final char MAPSETTINGGENDER_MALE = 'M';
	public static final char MAPSETTINGGENDER_FEMALE = 'F';
	public static final char MAPSETTINGGENDER_BOTH = 'B';
	
	public char userMapsettingGender;
	public UserProfile userProfile;
	public UserLocation userLocation;
	private HashMap<String, UserCorrespondence> userCorrespondences; // messages, etc in relation to this user
	
	public static boolean isThisUser_MapsettingGender_Both() {
		if (User.thisUser.userMapsettingGender == 'B') {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isThisUser_MapsettingGender_Male() {
		if (User.thisUser.userMapsettingGender == 'M') {
			return true;
		} else {
			return false;
		}
	}
	
	public static void setThisUser_MapsettingGender_Male() {
		User.thisUser.userMapsettingGender = 'M';
	}
	
	public static void setThisUser_MapsettingGender_Female() {
		User.thisUser.userMapsettingGender = 'F';
	}
	
	public static void setThisUser_MapsettingGender_Both() {
		User.thisUser.userMapsettingGender = 'B';
	}
	
	public HashMap<String, UserCorrespondence> GetUserCorrespondences()
	{
		if (userCorrespondences == null)
			userCorrespondences = new HashMap<String, UserCorrespondence>();
		return userCorrespondences;
	}
	
	public ArrayList<UserCorrespondence> GetUserCorrespondencesArrayList()
	{
		ArrayList<UserCorrespondence> ucArrayList = new ArrayList<UserCorrespondence>();
        Collection<UserCorrespondence> ucCollection = GetUserCorrespondences().values();
        if (!userCorrespondences.isEmpty()) {
        	Iterator<UserCorrespondence> it = ucCollection.iterator();
        	while (it.hasNext()) {
	        	UserCorrespondence uc = (UserCorrespondence) it.next();
	        	ucArrayList.add(uc);
        	}
        }
        Collections.sort(ucArrayList, new ByLastOnlineTime());
        return ucArrayList;
	}
	
	public ArrayList<UserCorrespondence> GetMessagedUsersArrayList() // array list of only users who have messages
	{
		ArrayList<UserCorrespondence> ucArrayList = new ArrayList<UserCorrespondence>();
        Collection<UserCorrespondence> ucCollection = GetUserCorrespondences().values();
        if (!userCorrespondences.isEmpty()) {
        	Iterator<UserCorrespondence> it = ucCollection.iterator();
        	while (it.hasNext()) {
	        	UserCorrespondence uc = (UserCorrespondence) it.next();
	        	if (uc.correspondences == null || uc.correspondences.size() == 0)
	        		continue;
	        	ucArrayList.add(uc);
        	}
        }
        Collections.sort(ucArrayList, new ByLastOnlineTime());
        return ucArrayList;
	}
	
	public ArrayList<UserCorrespondence> GetUnreadMessagedUsersArrayList(Context context) // array list of only users who have messages
	{
		ArrayList<UserCorrespondence> ucArrayList = new ArrayList<UserCorrespondence>();
        Collection<UserCorrespondence> ucCollection = GetUserCorrespondences().values();
        if (!userCorrespondences.isEmpty()) {
        	Iterator<UserCorrespondence> it = ucCollection.iterator();
        	while (it.hasNext()) {
	        	UserCorrespondence uc = (UserCorrespondence) it.next();
	        	if (uc.correspondences == null || uc.correspondences.size() == 0)
	        		continue;
	        	if (uc.hasUnviewedCorrespondences(context))
	        		ucArrayList.add(uc);
        	}
        }
        Collections.sort(ucArrayList, new ByLastOnlineTime());
        return ucArrayList;
	}
	
	public void AddUserCorrespondence(UserCorrespondence userCorrespondence)
	{
		if (userCorrespondences == null)
			userCorrespondences = new HashMap<String, UserCorrespondence>();

		// we already have this, combine and update
		if (userCorrespondences.containsKey(userCorrespondence.userProfile.userName))
		{
			userCorrespondences.get(userCorrespondence.userProfile.userName).combine(userCorrespondence);
			return;
		}
		// otherwise add it
		userCorrespondences.put(userCorrespondence.userProfile.userName, userCorrespondence);
		return;
	}
	
	public boolean hasUnviewedCorrespondences(Context context){
		ArrayList<UserCorrespondence> ucs = GetUserCorrespondencesArrayList();
		for (int i = 0; i < ucs.size(); ++i){
			if (ucs.get(i).hasUnviewedCorrespondences(context))
				return true;
		}
		return false;
	}
	
	public UserCorrespondence GetUserCorrespondenceByUserName(String userName)
	{
		if (userCorrespondences.containsKey(userName))
			return userCorrespondences.get(userName);
		return null;
	}
	
	class UserProfile
	{
		public String userName;
		public char gender;
		public String getGender()
		{ 
			if (gender == 'M')
				return "Male";
			else 
				return "Female";
		}
		public boolean isMale()
		{
			if (gender == 'M')
				return true;
			else 
				return false;
		}
		public int dobYear;
		public String[] interests;
		public String status;
		public String imageName;
		
		public String[] decodeInterests(int interests)
		{
			String[] ints = {"Dance", "Running", "Singing"};
			return ints;
		}
		
		public boolean isComplete()// is this profile complete or not
		{
			if (userName == null || userName.length() == 0 || dobYear < 1930 || status == null || status.length() == 0)
				return false;
			return true;
		}
	}
	class UserLocation
	{
		public double latitude;
		public double longitude;
	}

	class UserCorrespondence
	{
		UserProfile userProfile;
		UserLocation userLocation;
		ArrayList<Correspondence> correspondences;
		Timestamp lastOnlineTime;// this is when the user last contacted the server
		Timestamp lastViewedTime;
		public boolean combine(UserCorrespondence otherUserCorrespondence)
		{
			userProfile = otherUserCorrespondence.userProfile;
			userLocation = otherUserCorrespondence.userLocation;
			lastOnlineTime = otherUserCorrespondence.lastOnlineTime;
			
			if (correspondences == null)
			{
				correspondences = otherUserCorrespondence.correspondences;
				return true;
			}
			
			if (otherUserCorrespondence.correspondences == null)
				return true;
				
			for (int i = 0; i < otherUserCorrespondence.correspondences.size(); i++)
			{
				correspondences.add(otherUserCorrespondence.correspondences.get(i));
			}
			return true;
		}
		
		public boolean hasUnviewedCorrespondences(Context context){
			if (correspondences == null || correspondences.size() == 0)
				return false;
			
			if (lastViewedTime == null){
				SharedPreferences preferences = context.getSharedPreferences(Constants.prefsFileName, Context.MODE_PRIVATE);
	        	// try to get it from the android storage
		        String lastViewedTimeStr = preferences.getString(userProfile.userName + "_lastViewedTime", null);
		        if (lastViewedTimeStr != null){
		        	try{
	        			lastViewedTime = Timestamp.valueOf(lastViewedTimeStr);
	        		}
	        		catch(Exception e){}
	        	}
			}
			
			for (int i = 0; i < correspondences.size(); ++i) {
				Correspondence correspondence = correspondences.get(i);
				if (correspondence.received && (lastViewedTime == null || correspondence.created.after(lastViewedTime)))
					return true;
			}
			return false;
		}
		
		public ArrayList<Correspondence> getUnviewedCorrespondences(Context context){
			ArrayList<Correspondence> unviewedCorrespondences = new ArrayList<Correspondence>();
			
			if (correspondences == null || correspondences.size() == 0)
				return unviewedCorrespondences;
			
			if (lastViewedTime == null){
				SharedPreferences preferences = context.getSharedPreferences(Constants.prefsFileName, Context.MODE_PRIVATE);
	        	// try to get it from the android storage
		        String lastViewedTimeStr = preferences.getString(userProfile.userName + "_lastViewedTime", null);
		        if (lastViewedTimeStr != null){
		        	try{
	        			lastViewedTime = Timestamp.valueOf(lastViewedTimeStr);
	        		}
	        		catch(Exception e){}
	        	}
			}
			
			for (int i = 0; i < correspondences.size(); ++i) {
				Correspondence correspondence = correspondences.get(i);
				if (correspondence.received && (lastViewedTime == null || correspondence.created.after(lastViewedTime)))
					unviewedCorrespondences.add(correspondence);
			}
			return unviewedCorrespondences;
		}
		
		class Correspondence
		{
			public Correspondence() {};
			public Correspondence(boolean received, String text, Timestamp created)
			{
				this.received = received;
				this.text = text;
				this.created = created;
			}
			boolean received; // received from main user
			String text;
			Timestamp created;
		}
	}
	
	// replaces location from other User, adds messages from other User
	/*public boolean combineUser(User otherUser)
	{
		// only combine if username is the same
		if (userProfile.userName != otherUser.userProfile.userName)
			return false;
		// update location
		userLocation = otherUser.userLocation;
		
		for (int i = 0; i < otherUser.userCorrespondences.size(); ++i)
		{
			for (int j = 0; j < userCorrespondences.size(); ++j)
			{
				if (otherUser.userCorrespondences.get(i).userProfile.userName == userCorrespondences.get(j).userProfile.userName)
				{
					userCorrespondences.get(j).combine(otherUser.userCorrespondences.get(i));
					break;
				}
			}
		}
		return true;
	}*/
	
	public class ByLastOnlineTime implements java.util.Comparator<User.UserCorrespondence> {
		 public int compare(User.UserCorrespondence uc1, User.UserCorrespondence uc2) {
		  int sdif = uc1.lastOnlineTime.compareTo(uc2.lastOnlineTime);
		  return sdif * -1;// to make it descending
		 }
		} 
}

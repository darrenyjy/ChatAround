/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MapView.LayoutParams;
import com.web2mob.ChatAround.User.UserCorrespondence;
import com.web2mob.ChatAround.User.UserCorrespondence.Correspondence;
import com.web2mob.ChatAround.view.TransparentPanel;

public class UsersMapView extends MapActivity 
{
	private static boolean mapInited = false;
	private static Timestamp lastUpdated;
	private static ArrayList<User.UserCorrespondence> nearbyUsers = null;
	private static ArrayList<User.UserCorrespondence> messagedUsers = null;
	private int countUpdateLocation = 0;// how many times have we tried to update location
	private Animation userInfoAnimShow, userInfoAnimHide;
	private Animation newMessagesAnimShow, newMessagesAnimHide;

	MapView mapView;
	MapController mc;
	GeoPoint p;
	private Timer myTimer;
	
	private static int screenWidth, screenHeight;

	private Runnable getImportantUsers;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview);
		// init the popups for when we will need it
		initUserInfoPopup();
		initNewMessagesPopup();

		Display display = getWindowManager().getDefaultDisplay(); 
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		
		getImportantUsers = new Runnable(){
			@Override
			public void run() {
				runGetImportantUsers();
			}
		};

		// setup a timer to update everything
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}
		}, 0, 10 * 1000); // every six hunderd seconds
	}

	private void TimerMethod()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.
		
		//We call the method that will work with the UI
		//through the runOnUiThread method.
		this.runOnUiThread(Timer_Tick);
	}

	private Runnable Timer_Tick = new Runnable() {
		public void run() {
			//This method runs in the same thread as the UI. 
			new FetchUsersAndMessagesTask().execute("");
			//onReceivedUsers();

			/*Thread thread =  new Thread(null, getImportantUsers, "MagentoBackground");
			thread.start();
			progressDialog = ProgressDialog.show(UsersMapView.this,    
					"Please wait...", "Retrieving data ...", true);*/
		}
	};
	
	private void runGetImportantUsers()
	{
		if (countUpdateLocation++ % 2 == 0) {// call update location on every alternate call
			// get the nearby users
			// the nearby users are replaced every time
			nearbyUsers = getNearbyUsers();
	
			// add these
			if (nearbyUsers != null)
			{
				for (int i = 0; i < nearbyUsers.size(); ++i)
				{
					// do not add yourself
					if (nearbyUsers.get(i).userProfile.userName.compareTo(User.thisUser.userProfile.userName) == 0)
						continue;
					User.thisUser.AddUserCorrespondence(nearbyUsers.get(i));
				}
			}
			else {
				Toast.makeText(this, "Getting users failed! network unavailable", Toast.LENGTH_SHORT).show();
			}
		}

		// messages are added to every time
		GetNewMessagesResponse response = getNewMessages();
		if (response != null)
			messagedUsers = response.userCorrespondences;

		// add these
		if (messagedUsers != null)
		{
			for (int i = 0; i < messagedUsers.size(); ++i)
			{
				// do not add yourself
				if (messagedUsers.get(i).userProfile.userName.compareTo(User.thisUser.userProfile.userName) == 0)
					continue;
				User.thisUser.AddUserCorrespondence(messagedUsers.get(i));
			}
		}
		
		if (response != null)
			lastUpdated = response.updateTime;
	}
	
	private void onReceivedUsers() {
		//Do something to the UI thread here
		if (!mapInited || mapView == null){
			mapInited = true;
			initMap();
		}
		
		// if there are unread messages, inform the user
		if (User.thisUser.hasUnviewedCorrespondences(this)) {
			//Toast.makeText(this, "You have new messages", Toast.LENGTH_SHORT).show();
			showNewMessagesPopup();
		}
		showMap();
	}
	
	public ArrayList<User.UserCorrespondence> getNearbyUsers()
	{
		try 
		{
			boolean gotLocation = false; 
			
			// get the current location
			User.thisUser.userLocation = User.thisUser.new UserLocation();
			
			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			String bestProvider = locationManager.getBestProvider(new Criteria(), true);
			Location location = null;
			if (bestProvider != null)
				location = locationManager.getLastKnownLocation(bestProvider);
			if (location == null) {
				List<String> providers = locationManager.getAllProviders();
				Iterator<String> it = providers.iterator();
				while (location == null && it.hasNext()){
					String provider = (String) it.next();
					location = locationManager.getLastKnownLocation(provider);
				}
			}
			if (location != null) {
				User.thisUser.userLocation.latitude = location.getLatitude();// Constants.userLatitude;
				User.thisUser.userLocation.longitude = location.getLongitude();//Constants.userLongitude;
				gotLocation = true;
			}
			else {
				// check if the user's location is stored
				SharedPreferences preferences = getSharedPreferences(Constants.prefsFileName, Context.MODE_PRIVATE);
	        	// try to get it from the android storage
				String userLat = preferences.getString("thisUser_lastLatitude", null);
				String userLong = preferences.getString("thisUser_lastLongitude", null);
		        if (userLat != null && userLong != null){
		        	try{
		        		User.thisUser.userLocation.latitude = Double.parseDouble(userLat);
		        		User.thisUser.userLocation.longitude = Double.parseDouble(userLong);
		        		gotLocation = true;
	        		}
	        		catch(Exception e){
	        			userLat = null;
	        			userLong = null;
	        		}
	        	}
			}
		        
	        if (!gotLocation){
	        	try {
		        	// now use the location on the map
					mapView = (MapView) findViewById(R.id.mapView);
					GeoPoint mapCenter = mapView.getMapCenter();
					User.thisUser.userLocation.latitude = mapCenter.getLatitudeE6() / 1E6;
					User.thisUser.userLocation.longitude = mapCenter.getLongitudeE6() / 1E6;
	        	}
	        	catch (Exception e) {
					// put the user in NYC
					User.thisUser.userLocation.latitude = Constants.nycLatitude;
					User.thisUser.userLocation.longitude = Constants.nycLongitude;
	        	}
	        }
			
			// Construct data 
			String data = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(Double.toString(User.thisUser.userLocation.latitude), "UTF-8");
			data += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(Double.toString(User.thisUser.userLocation.longitude), "UTF-8");
			
			// we have to pass two locations, where the user is and what the user wants, otherwise the user won't see
			// users where he is looking
        	// now use the location on the map
			mapView = (MapView) findViewById(R.id.mapView);
			GeoPoint mapCenter = mapView.getMapCenter();
			double wantedLatitude = mapCenter.getLatitudeE6() / 1E6;
			double wantedLongitude = mapCenter.getLongitudeE6() / 1E6;
			
			data += "&" + URLEncoder.encode("wantedLatitude", "UTF-8") + "=" + URLEncoder.encode(Double.toString(wantedLatitude), "UTF-8");
			data += "&" + URLEncoder.encode("wantedLongitude", "UTF-8") + "=" + URLEncoder.encode(Double.toString(wantedLongitude), "UTF-8");

			// Send data
			WebServiceResponse response = WebServiceUtil.sendRequest("updateLocation.php", data);
			
			// parse the result
			ArrayList<User.UserCorrespondence> users = parseUserLocations(response.doc);
			return users;
		} catch (Exception e) {}
		return null;
	}

	public ArrayList<User.UserCorrespondence> parseUserLocations(Document doc)
	{
		try
		{
			NodeList nodeLst = doc.getElementsByTagName("user");

			ArrayList<User.UserCorrespondence> nearbyUsers = new ArrayList<UserCorrespondence>();
			for (int s = 0; s < nodeLst.getLength(); s++) 
			{
				User.UserCorrespondence userCorrespondence = User.thisUser.new UserCorrespondence();
				Node userNode = nodeLst.item(s);
				if (userNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element fstElmnt = (Element) userNode;

					userCorrespondence.userProfile = User.thisUser.new UserProfile();
					userCorrespondence.userProfile.userName = WebServiceUtil.getNodeValue(fstElmnt, "userName");
					userCorrespondence.userProfile.gender = WebServiceUtil.getNodeValue(fstElmnt, "gender").charAt(0);
					userCorrespondence.userProfile.dobYear = Integer.parseInt(WebServiceUtil.getNodeValue(fstElmnt, "dobYear"));
					userCorrespondence.userProfile.status = WebServiceUtil.getNodeValue(fstElmnt, "status");
					userCorrespondence.userProfile.imageName = WebServiceUtil.getNodeValue(fstElmnt, "imageName");

					userCorrespondence.userLocation = User.thisUser.new UserLocation();
					userCorrespondence.userLocation.latitude = Double.parseDouble(WebServiceUtil.getNodeValue(fstElmnt, "latitude"));
					userCorrespondence.userLocation.longitude = Double.parseDouble(WebServiceUtil.getNodeValue(fstElmnt, "longitude"));
					userCorrespondence.lastOnlineTime = Timestamp.valueOf(WebServiceUtil.getNodeValue(fstElmnt, "lastOnlineTime"));

					if (userCorrespondence.userProfile.userName == null)
						continue;
					nearbyUsers.add(userCorrespondence);
				}
			}
			return nearbyUsers;
		} catch (Exception e) 
		{ 
			String eMsg = e.getMessage();
		}
		return null;
	}

	public class GetNewMessagesResponse
	{
		ArrayList<User.UserCorrespondence> userCorrespondences;
		Timestamp updateTime;
	}
	
	public GetNewMessagesResponse getNewMessages()
	{
		try 
		{ 
			// Construct data
			String data = null;

			if (lastUpdated != null)
				data = URLEncoder.encode("timestamp", "UTF-8") + "=" + URLEncoder.encode(lastUpdated.toString(), "UTF-8");

			// Send data
			WebServiceResponse response = WebServiceUtil.sendRequest("getAllCorrespondence.php", data);

			// parse the result
			GetNewMessagesResponse newMessagesResponse = parseUserMessages(response.doc);

			return newMessagesResponse;
		} catch (Exception e) {}
		return null;
	}

	public GetNewMessagesResponse parseUserMessages(Document doc)
	{
		try
		{
			NodeList nodeLst = doc.getElementsByTagName("user");

			ArrayList<User.UserCorrespondence> messagedUsers = new ArrayList<UserCorrespondence>();
			for (int i = 0; i < nodeLst.getLength(); i++) 
			{
				User.UserCorrespondence userCorrespondence = User.thisUser.new UserCorrespondence();
				Node userNode = nodeLst.item(i);
				if (userNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element fstElmnt = (Element) userNode;

					userCorrespondence.userProfile = User.thisUser.new UserProfile();
					userCorrespondence.userProfile.userName = WebServiceUtil.getNodeValue(fstElmnt, "userName");
					if (userCorrespondence.userProfile.userName == null)
						userCorrespondence.userProfile.userName = "unknown";
					userCorrespondence.userProfile.gender = WebServiceUtil.getNodeValue(fstElmnt, "gender").charAt(0);
					userCorrespondence.userProfile.dobYear = Integer.parseInt(WebServiceUtil.getNodeValue(fstElmnt, "dobYear"));
					userCorrespondence.userProfile.status = WebServiceUtil.getNodeValue(fstElmnt, "status");
					userCorrespondence.userProfile.imageName = WebServiceUtil.getNodeValue(fstElmnt, "imageName");

					userCorrespondence.userLocation = User.thisUser.new UserLocation();
					userCorrespondence.userLocation.latitude = Double.parseDouble(WebServiceUtil.getNodeValue(fstElmnt, "latitude"));
					userCorrespondence.userLocation.longitude = Double.parseDouble(WebServiceUtil.getNodeValue(fstElmnt, "longitude"));
					userCorrespondence.lastOnlineTime = Timestamp.valueOf(WebServiceUtil.getNodeValue(fstElmnt, "lastOnlineTime"));

					NodeList messagesLst = fstElmnt.getElementsByTagName("message");
					userCorrespondence.correspondences = new ArrayList<User.UserCorrespondence.Correspondence>();

					for (int j = 0; j < messagesLst.getLength(); j++)
					{
						User.UserCorrespondence.Correspondence message = userCorrespondence.new Correspondence();
						Node messageNode = messagesLst.item(j);
						if (messageNode.getNodeType() == Node.ELEMENT_NODE) 
						{
							Element messagesElmnt = (Element) messageNode;

							message.text = WebServiceUtil.getNodeValue(messagesElmnt, "text");
							message.created = Timestamp.valueOf(WebServiceUtil.getNodeValue(messagesElmnt, "created"));
							message.received = Boolean.valueOf(WebServiceUtil.getNodeValue(messagesElmnt, "received"));
							userCorrespondence.correspondences.add(message);
						}
					}
				}
				messagedUsers.add(userCorrespondence);
			}
			
			nodeLst = doc.getElementsByTagName("currentTime");

			Timestamp currentTime = null;
			for (int i = 0; i < nodeLst.getLength(); i++) 
			{
				Node currentTimeNode = nodeLst.item(i);
				if (currentTimeNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element fstElmnt = (Element) currentTimeNode;
					currentTime = Timestamp.valueOf(WebServiceUtil.getNodeValue(fstElmnt, "currentTime"));
				}
			}

			GetNewMessagesResponse response = new GetNewMessagesResponse();
			response.userCorrespondences = messagedUsers;
			response.updateTime = currentTime;
			return response;
		} catch (Exception e) { }
		return null;
	}


	public void initMap()
	{
		mapView = (MapView) findViewById(R.id.mapView);
		LinearLayout zoomLayout = (LinearLayout)findViewById(R.id.zoom);  
		View zoomView = mapView.getZoomControls(); 

		zoomLayout.addView(zoomView, 
				new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, 
						LayoutParams.WRAP_CONTENT)); 
		mapView.displayZoomControls(true);

		mc = mapView.getController();

		p = new GeoPoint(
				(int) (User.thisUser.userLocation.latitude * 1E6), 
				(int) (User.thisUser.userLocation.longitude * 1E6));

		mc.animateTo(p);
		mc.setZoom(6);
	}
	
	public void showMap()
	{
		List<Overlay> mapOverlays = mapView.getOverlays();

		// clear the old list if any
		mapOverlays.clear();

		// first add yourself
		/*MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapOverlays.add(myLocationOverlay);
        myLocationOverlay.enableMyLocation();*/

		Drawable drawable = this.getResources().getDrawable(R.drawable.b01);
		UserItemizedOverlay itemizedoverlay = new UserItemizedOverlay(drawable, this);

		int usersShown = 0;
		Point screenCoords = new Point();
		ArrayList<UserCorrespondence> userCorrespondences = User.thisUser.GetUserCorrespondencesArrayList();
		for (int i = userCorrespondences.size() - 1; i > 0; i--) {
			UserCorrespondence userCorrespondence = userCorrespondences.get(i);
			
			// only add users who are of preferred gender, unless setting is for Both genders
			if ( !User.isThisUser_MapsettingGender_Both() ) {
				if ( userCorrespondence.userProfile.gender != User.thisUser.userMapsettingGender) {
					continue;
				}
			}
			
			User.UserLocation userLocation = userCorrespondence.userLocation;
			GeoPoint geoPoint = new GeoPoint(
					(int) (userLocation.latitude * 1E6), 
					(int) (userLocation.longitude * 1E6));

			// only add users who are in or just near the view
			mapView.getProjection().toPixels(geoPoint, screenCoords);
			if (screenCoords.x < -10 || screenCoords.x > screenWidth + 10 || 
					screenCoords.y < -10 || screenCoords.y > screenHeight + 10) {
				continue;
			}
			
			usersShown++;
			
			OverlayItem overlayitem = new OverlayItem(geoPoint, userCorrespondence.userProfile.userName, "I'm in Mexico City!");
			int imagePosn = Images.getImagePosition(userCorrespondence.userProfile.imageName, userCorrespondence.userProfile.gender, true);
			
			Integer imageId = Images.getImageId(imagePosn, userCorrespondence.userProfile.gender);
			Drawable customDrawable = this.getResources().getDrawable(imageId);
			
			// the ones who are online in the last 15 mins are shown opaque, other ones are not
			if (lastUpdated.getTime() - userCorrespondence.lastOnlineTime.getTime() > 15 * 1000 * 60)
				customDrawable.setAlpha(120);
			else 
				customDrawable.setAlpha(255);
			
			customDrawable.setBounds(0, 0, customDrawable.getIntrinsicWidth(), customDrawable.getIntrinsicHeight());
			
			overlayitem.setMarker(customDrawable);
			itemizedoverlay.addOverlay(overlayitem);
		}
		if (itemizedoverlay.size() > 0)
			mapOverlays.add(itemizedoverlay);
		mapView.invalidate();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    
	    // set gender mapsetting submenu to be 'checked'
	    if ( User.isThisUser_MapsettingGender_Male() ) {
	    	menu.findItem(R.id.mapsetting_guys).setChecked(true);
	    } else if ( User.isThisUser_MapsettingGender_Both() ) {
	    	menu.findItem(R.id.mapsetting_both).setChecked(true);
	    } else {
	    	menu.findItem(R.id.mapsetting_gals).setChecked(true);
	    }
	    
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_messages:
			showMessages();
			return true;
		case R.id.profile:
			// show the create user screen
			showUpdateUser();
			return true;
		//mapsetting submenus
		case R.id.mapsetting_gals:
		case R.id.mapsetting_guys:
		case R.id.mapsetting_both:
			if ( !item.isChecked() ) {
				item.setChecked(true);
				// set option for map
				doMapSetting(item);
			}
			return true;
		case R.id.feedback:
			// show the create user screen
			showCreateFeedback();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showMessages() {
		Intent i = new Intent(this, MessagedUsersView.class);
		startActivityForResult(i, Constants.ACTIVITY_VIEWMESSAGEDUSERS);
	}
	
	public void showMessagesForUser(String userName) {
		Intent i = new Intent(this, CorrespondencesView.class);
    	i.putExtra("userName", userName);
		startActivityForResult(i, Constants.ACTIVITY_VIEWMESSAGESFROMUSER);
	}
	
	public void doMapSetting(MenuItem item) {
		Toast.makeText(this, "Setting is "+item.getTitle(), Toast.LENGTH_SHORT).show();
		
		// persist preference in Shared Preferences
		String genderKey = Constants.phoneUniqueId +	//setting is keyed to phone, not user name
		   				   Constants.prefsMapsettingGender_suffix;
		SharedPreferences shPref =
			this.getSharedPreferences(Constants.prefsFileName, Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = shPref.edit();
		char cMapsettingGender;
		switch ( item.getItemId() ) {
			case R.id.mapsetting_guys:
				cMapsettingGender = User.MAPSETTINGGENDER_MALE;
				break;
			case R.id.mapsetting_gals:
				cMapsettingGender = User.MAPSETTINGGENDER_FEMALE;
				break;
			default:
				cMapsettingGender = User.MAPSETTINGGENDER_BOTH;
		}
		ed.putString( genderKey, Character.toString(cMapsettingGender) );
		ed.commit();
		
		// set MapsettingGender preference for thisUser
    	User.thisUser.userMapsettingGender = cMapsettingGender;
    	//showMap() will be next called using updated User.thisUser.userMapsettingGender 
 	}
	
	public void showCreateFeedback() {
		Intent i = new Intent(this, FeedbackCreate.class);
		startActivityForResult(i, Constants.ACTIVITY_CREATEFEEDBACK);
	}
	
	public void showUpdateUser() {
		Intent i = new Intent(this, CreateOrUpdateUserView.class);
		i.putExtra(CreateOrUpdateUserView.isCreate, false);
		startActivityForResult(i, Constants.ACTIVITY_CREATEORUPDATEUSER);
	}

	public void showPanelForUser(String userName) {
		showUserInfoPopup(userName);
	}

	private void initUserInfoPopup() {

		final TransparentPanel popup = (TransparentPanel) findViewById(R.id.mapview_userinfo_popup);

		popup.setBackgroundResource(R.drawable.dew_ladybug_thumb);
		//  Start out with the popup initially hidden.
		popup.setVisibility(View.GONE);

		userInfoAnimShow = AnimationUtils.loadAnimation( this, R.anim.userinfo_popup_show);
		userInfoAnimHide = AnimationUtils.loadAnimation( this, R.anim.userinfo_popup_hide);

		final Button   hideButton = (Button) findViewById(R.id.mapview_userinfo_popup_hide);

		hideButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				hideUserInfoPopup();
			}});
	}

	private void showUserInfoPopup(final String userName) {
		final TransparentPanel popup = (TransparentPanel) findViewById(R.id.mapview_userinfo_popup);
		popup.setVisibility(View.VISIBLE);
		popup.startAnimation( userInfoAnimShow );

		final Button   hideButton = (Button) findViewById(R.id.mapview_userinfo_popup_hide);
		hideButton.setEnabled(true);
		
		final Button sendPokeButton = (Button) findViewById(R.id.mapview_userinfo_send_poke);
		sendPokeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				//first check if the user has completed the profile
				if (!User.thisUser.userProfile.isComplete()){
					showCompleteProfileAlert(view);
		    		return;
				}
				popup.startAnimation( userInfoAnimHide );
				//sendMessageButton.setEnabled(false);
				popup.setVisibility(View.GONE);
				sendPoke(userName);
			}});

		final Button sendMessageButton = (Button) findViewById(R.id.mapview_userinfo_send_message);
		sendMessageButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				//first check if the user has completed the profile
				if (!User.thisUser.userProfile.isComplete()) {
					showCompleteProfileAlert(view);
		    		return;
				}
				popup.startAnimation( userInfoAnimHide );
				//sendMessageButton.setEnabled(false);
				popup.setVisibility(View.GONE);
				Intent intent = new Intent(view.getContext(), MessageCreate.class);
				intent.putExtra("UserName", userName);
				startActivityForResult(intent, Constants.ACTIVITY_CREATEMESSAGE);
			}});
		
		UserCorrespondence uc = User.thisUser.GetUserCorrespondenceByUserName(userName);
		final Button viewMessagesButton = (Button) findViewById(R.id.mapview_userinfo_view_messages);
		if (uc.correspondences != null && uc.correspondences.size() > 0) {
			viewMessagesButton.setVisibility(View.VISIBLE);
			viewMessagesButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					popup.startAnimation( userInfoAnimHide );
					//sendMessageButton.setEnabled(false);
					popup.setVisibility(View.GONE);
					Intent intent = new Intent(view.getContext(), CorrespondencesView.class);
					intent.putExtra("userName", userName);
					startActivityForResult(intent, Constants.ACTIVITY_VIEWMESSAGESFROMUSER);
				}});
		}
		else {
			//hide the button
			viewMessagesButton.setVisibility(View.GONE);
		}
		
		final TextView popupNameText = (TextView) findViewById(R.id.mapview_userinfo_name);
		final TextView popupDescText = (TextView) findViewById(R.id.mapview_userinfo_description);

		int thisYear = new Date().getYear() + 1900;
		String popupName = userName + " is a " + (thisYear - uc.userProfile.dobYear) + " year old ";
		
		if (uc.userProfile.isMale())
			popupName += "guy";
		else
			popupName += "gal";
		popupNameText.setText(popupName);
		
		String desc = "";
		if (uc.userProfile.status != null && uc.userProfile.status.length() > 0)
			desc = uc.userProfile.status;
		popupDescText.setText(desc);
	}
	
	private void hideUserInfoPopup() {
		final TransparentPanel popup = (TransparentPanel) findViewById(R.id.mapview_userinfo_popup);
		popup.startAnimation( userInfoAnimHide );
		final Button   hideButton = (Button) findViewById(R.id.mapview_userinfo_popup_hide);
		hideButton.setEnabled(false);
		popup.setVisibility(View.GONE);
		return;
	}
	
	private void initNewMessagesPopup() {
		final TransparentPanel popup = (TransparentPanel) findViewById(R.id.mapview_newmessages_popup);

		//  Start out with the popup initially hidden.
		popup.setVisibility(View.GONE);

		//newMessagesAnimShow = AnimationUtils.loadAnimation( this, R.anim.newmessages_popup_show);
		//newMessagesAnimHide = AnimationUtils.loadAnimation( this, R.anim.newmessages_popup_hide);

		final Button   hideButton = (Button) findViewById(R.id.mapview_newmessages_popup_hide);

		hideButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				hideNewMessagesPopup();
			}});
	}

	private void showNewMessagesPopup() {
		final TransparentPanel popup = (TransparentPanel) findViewById(R.id.mapview_newmessages_popup);
		popup.setVisibility(View.VISIBLE);
		//popup.startAnimation( newMessagesAnimShow );

		final Button   hideButton = (Button) findViewById(R.id.mapview_newmessages_popup_hide);
		hideButton.setEnabled(true);
		
		ArrayList<UserCorrespondence> unreadMessagedUsers = User.thisUser.GetUnreadMessagedUsersArrayList(getApplicationContext());

		final Button viewMessagesButton = (Button) findViewById(R.id.mapview_newmessages_view_messages);
		final TextView popupNameText = (TextView) findViewById(R.id.mapview_newmessages_title);
		final TextView popupDescText = (TextView) findViewById(R.id.mapview_newmessages_description);

		if (unreadMessagedUsers.size() == 1){
			final UserCorrespondence uc = unreadMessagedUsers.get(0);
			ArrayList<Correspondence> unreadCorrespondences = uc.getUnviewedCorrespondences(getApplicationContext());
			if (unreadCorrespondences.size() == 1){
				Correspondence unreadCorrespondence = unreadCorrespondences.get(0);
				// simply show the message
				popupNameText.setText(uc.userProfile.userName + " said");
				String desc = unreadCorrespondence.text;
				if (desc.length() > 80)
					desc = desc.substring(0, 80) + "...";
				popupDescText.setText(desc);
			}
			else{
				popupNameText.setText("You have unread messages from:");
				popupDescText.setText(uc.userProfile.userName);
			}
			viewMessagesButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					hideNewMessagesPopup();
					showMessagesForUser(uc.userProfile.userName);
				}});
		}
		else {
			popupNameText.setText("You have unread messages from:");
			String desc = "";
			for (int i = 0; i < unreadMessagedUsers.size(); ++i){
				if (i > 0)
					desc += ", ";
				desc += unreadMessagedUsers.get(i).userProfile.userName;	
			}
			if (desc.length() > 80)
				desc = desc.substring(0, 80) + "...";
			popupDescText.setText(desc);
			viewMessagesButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					hideNewMessagesPopup();
					showMessages();
				}});
		}
	}
	
	private void hideNewMessagesPopup() {
		final TransparentPanel popup = (TransparentPanel) findViewById(R.id.mapview_newmessages_popup);
		//popup.startAnimation( newMessagesAnimHide );
		final Button   hideButton = (Button) findViewById(R.id.mapview_newmessages_popup_hide);
		hideButton.setEnabled(false);
		popup.setVisibility(View.GONE);
		return;
	}
	
	public void showCompleteProfileAlert(View view)
	{
		// alert user that the profile needs to be completed
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
		dialog.setTitle("Just one more step!");
		dialog.setMessage("Before you message another user, you need to complete your profile. Press OK to complete your profile.");
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				showUpdateUser();
				}
			});
		dialog.show();
		return;	
	}
	
	public boolean sendPoke(String userName)
	{
		try
		{
			// Construct data
			String message = User.thisUser.userProfile.userName + " says hi!";
	    	String data = URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
	    	data += "&" + URLEncoder.encode("toUserName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8");
	    	
	    	// Send data
	    	WebServiceResponse response = WebServiceUtil.sendRequest("sendMessage.php", data);
	    	if (response.errCode != Constants.ErrorCode.noError)
	    	{
	            // some other error
	            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
	    		dialog.setTitle("Oops, we goofed up!");
	    		dialog.setMessage("We're sorry, there has been an internal error. Please try again.");
	    		dialog.show();
	    		return false;
	    	}
	    	
	    	Timestamp created = ServerCalls.parseMessageCreateResponse(response.doc);
			// update the user's correspondence
			UserCorrespondence uc = User.thisUser.GetUserCorrespondenceByUserName(userName);
			if (uc.correspondences == null)
				uc.correspondences = new ArrayList<Correspondence>();
			
			Correspondence correspondence = uc.new Correspondence(false, message, created);
			uc.correspondences.add(correspondence);
	
			Toast.makeText(this, "Your poke to " + userName + " has been sent!", Toast.LENGTH_SHORT).show();
	    	return true;
		}
		catch(Exception e){}
		return false;
	}

    @Override
    protected void onStop(){
    	super.onStop();
    	
        // now use the location on the map
		mapView = (MapView) findViewById(R.id.mapView);
		GeoPoint mapCenter = mapView.getMapCenter();
		User.thisUser.userLocation.latitude = mapCenter.getLatitudeE6() / 1E6;
		User.thisUser.userLocation.longitude = mapCenter.getLongitudeE6() / 1E6;
		
		// store the location
		// We need an Editor object to make preference changes.
		SharedPreferences settings = getSharedPreferences(Constants.prefsFileName, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("thisUser_lastLatitude", String.valueOf(User.thisUser.userLocation.latitude));
		editor.putString("thisUser_lastLongitude", String.valueOf(User.thisUser.userLocation.longitude));
		// Commit the edits!
		editor.commit();
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	if (myTimer != null) {
      	   myTimer.cancel();
      	   myTimer = null;
         }
    }
    
    private class FetchUsersAndMessagesTask extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... junk) {
    		runGetImportantUsers();
            return (long) 0;
        }

        protected void onProgressUpdate(Integer... progress) {
            return;
        }

        protected void onPostExecute(Long result) {
            onReceivedUsers();
        }
    }   
}
/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.web2mob.ChatAround.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ChatAround extends Activity implements OnClickListener
{ 
    private boolean isDoneGetUserInfo = false;		// has getUserInfo.php returned
    private boolean isReadyProcessUserInfo = false;	// is splashscreen delay over
    private WebServiceResponse response;
    private ProgressDialog progressDialog;
    private Handler splashscreenHandler;			// handler to post delayed runnable
    private Runnable doAfterSplashscreen;			// runnable to run at end of splashscreen delay
    
    Button startBtn, tutorialBtn, creditsBtn, exitBtn;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
		Constants.phoneUniqueId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		if (Constants.phoneUniqueId == null)
			Constants.phoneUniqueId = "bart";
		
        // display splash view
        setContentView(R.layout.splashscreen);
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/segoeprb.ttf");
        TextView titleTextView = (TextView) findViewById(R.id.splashscreen_title);
        titleTextView.setTypeface(tf);
        startBtn = (Button) findViewById(R.id.startapp_button);
        startBtn.setTypeface(tf);
        startBtn.setVisibility(View.GONE);
        tutorialBtn = (Button) findViewById(R.id.tutorial_button);
        tutorialBtn.setTypeface(tf);
        tutorialBtn.setVisibility(View.GONE);
        creditsBtn = (Button) findViewById(R.id.splashscreen_credits_button);
        creditsBtn.setTypeface(tf);
        creditsBtn.setVisibility(View.GONE);
        exitBtn = (Button) findViewById(R.id.exitapp_button);
        exitBtn.setTypeface(tf);
        exitBtn.setVisibility(View.GONE);
        
        progressDialog = ProgressDialog.show(this, "", "Loading...");
        
        // create handler to post runnable delayed to run at end of splashscreen delay
        splashscreenHandler = new Handler();
        doAfterSplashscreen = new Runnable() {
        	public void run() {
        		// decide whether can call processUserInfo() now
            	//  flag isDoneGetUserInfo is set after getUserInfo.php has returned
        		isReadyProcessUserInfo = true;
            	if (isDoneGetUserInfo) {
            		processUserInfo();
            	}
        	}
        };
        splashscreenHandler.postDelayed(doAfterSplashscreen, Constants.SPLASHSCREEN_DELAYMS);
        
        getUserInfo();
    }
    
    public void getUserInfo() {
        // first check if we have this user's info or not
		response = WebServiceUtil.sendRequest("getUserInfo.php", null);
        
        // set flag for processUserInfo() to be called by splash screen delayed runnable
        isDoneGetUserInfo = true;
        // decide whether can call processUserInfo() now
        //  flag isReadyProcessUserInfo is set by splash screen delayed runnable
        if (isReadyProcessUserInfo) {
        	processUserInfo();
        }
    }
    
    public void processUserInfo() {
        // if we have the information, go ahead directly
    	if (response.errCode == Constants.ErrorCode.noError) //we found the user
        {
        	User.thisUser = parseUserInfo(response.doc);
        	
        	// get user's MapsettingGender preference from stored Shared Preferences
        	String genderKey = Constants.phoneUniqueId +	//setting is keyed to phone, not user name
        					   Constants.prefsMapsettingGender_suffix;
        	SharedPreferences shPref =
        		this.getSharedPreferences(Constants.prefsFileName, Context.MODE_PRIVATE);
        	String mapsettingGender = shPref.getString(genderKey, null);
        	// any preference set?
        	if (mapsettingGender == null) {
        		// user not set preference, use opposite gender as default Mapsetting Gender
        		if ( User.thisUser.userProfile.isMale() ) {
        			User.setThisUser_MapsettingGender_Female();
        			mapsettingGender = Character.toString(User.MAPSETTINGGENDER_FEMALE);
        		} else {
        			User.setThisUser_MapsettingGender_Male();
        			mapsettingGender = Character.toString(User.MAPSETTINGGENDER_MALE);
        		}
        		// persist preference in Shared Preferences
        		SharedPreferences.Editor ed = shPref.edit();
        		ed.putString( genderKey, mapsettingGender );
        		ed.commit();
        	} else {
        		// set MapsettingGender preference for thisUser
        		if (mapsettingGender.charAt(0) == User.MAPSETTINGGENDER_FEMALE) {
        			User.setThisUser_MapsettingGender_Female();
        		} else if (mapsettingGender.charAt(0) == User.MAPSETTINGGENDER_MALE) {
        			User.setThisUser_MapsettingGender_Male();
        		} else {
        			User.setThisUser_MapsettingGender_Both();
        		}
        	}
        	        	
        	
        	// dismiss dialog
        	progressDialog.dismiss();
            // register event handler for buttons on splash screen
            findViewById(R.id.startapp_button).setOnClickListener(this);
            findViewById(R.id.tutorial_button).setOnClickListener(this);
            findViewById(R.id.splashscreen_credits_button).setOnClickListener(this);
            findViewById(R.id.exitapp_button ).setOnClickListener(this);

        	showMap(); // go ahead
        	return;
        }
        else if (response.errCode == Constants.ErrorCode.userNotFound) // user not found, ask to create new
        {
        	// dismiss dialog
        	progressDialog.dismiss();
            // register event handler for buttons on splash screen
            findViewById(R.id.startapp_button).setOnClickListener(this);
            findViewById(R.id.tutorial_button).setOnClickListener(this);
            findViewById(R.id.splashscreen_credits_button).setOnClickListener(this);
            findViewById(R.id.exitapp_button ).setOnClickListener(this);

        	// ask the user if he wants to see the tutorial
        	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        	dialog.setMessage("Do you want to learn how to Chat Around?");
        	dialog.setCancelable(false);
        	dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int id) {
		        	showTutorial();
	        		dialog.cancel();
		        }});
        	dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        		//  Action for 'NO' Button
        		dialog.cancel();
        		showCreateUser();
        		}});
        	dialog.show();
        	return;
        }        
        // some other error
		// alert user that the profile needs to be completed
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Network error!");
		dialog.setMessage("The server could not be reached. Please wait for a sufficient signal and press OK.");
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getUserInfo();
				}
			});
		dialog.show();
        return;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == Constants.ACTIVITY_TUTORIAL) {
    		if (resultCode == RESULT_OK) {
    			showCreateUser();
    		}
    	}
    	else if (requestCode == Constants.ACTIVITY_CREATEORUPDATEUSER) {
            if (resultCode == RESULT_OK) {
            	showMap();
            }
        }
    }
    
    public User parseUserInfo(Document doc)
	{
		try
		{
        	NodeList nodeLst = doc.getElementsByTagName("user");

        	User user = new User();
        	Node userNode = nodeLst.item(0);
        	if (userNode.getNodeType() == Node.ELEMENT_NODE) 
        	{
        		Element fstElmnt = (Element) userNode;
        		
        		user.userProfile = user.new UserProfile();
        		user.userProfile.userName = WebServiceUtil.getNodeValue(fstElmnt, "userName");
        		user.userProfile.gender = WebServiceUtil.getNodeValue(fstElmnt, "gender").charAt(0);
        		user.userProfile.dobYear = Integer.parseInt(WebServiceUtil.getNodeValue(fstElmnt, "dobYear"));
        		user.userProfile.status = WebServiceUtil.getNodeValue(fstElmnt, "status");
        		user.userProfile.imageName = WebServiceUtil.getNodeValue(fstElmnt, "imageName");
    	    	return user;
        	}
		} catch (Exception e) 
		{ 
			String eMsg = e.getMessage();
		}
		return null;
	}
    
    public void showCreateUser() {
        // show the tutorial
    	// show the create user screen
    	Intent i = new Intent(this, CreateOrUpdateUserView.class);
      	i.putExtra(CreateOrUpdateUserView.isCreate, true);
        startActivityForResult(i, Constants.ACTIVITY_CREATEORUPDATEUSER);
        return;
    }
    
    public void showTutorial() {
        // show the tutorial
    	Intent i = new Intent(this, TutorialView.class);
        startActivityForResult(i, Constants.ACTIVITY_TUTORIAL);
        return;
    }
    
    public void showCredits() {
        // show the tutorial
    	Intent i = new Intent(this, CreditsView.class);
        startActivityForResult(i, Constants.ACTIVITY_CREDITS);
        return;
    }
    
    public void showMap() {
    	// show the view user map screen
    	Intent i = new Intent(this, UsersMapView.class);
        startActivityForResult(i, Constants.ACTIVITY_VIEWUSERMAP);
        return;
    }
    
    // Implement the OnClickListener callback
    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
			case R.id.startapp_button:
				if (User.thisUser == null)
					showCreateUser();
				else
					showMap();
				break;
			case R.id.tutorial_button:
		        showTutorial();
				break;
			case R.id.splashscreen_credits_button:
		        showCredits();
				break;
			case R.id.exitapp_button:
				finish();
				break;
		}
    }
    
    @Override
    public void onRestart(){
    	super.onRestart();
    	startBtn.setVisibility(View.VISIBLE);
    	tutorialBtn.setVisibility(View.VISIBLE);
    	creditsBtn.setVisibility(View.VISIBLE);
    	exitBtn.setVisibility(View.VISIBLE);
    }
}
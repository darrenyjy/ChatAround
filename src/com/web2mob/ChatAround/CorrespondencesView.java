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
import java.util.Timer;
import java.util.TimerTask;

import com.web2mob.ChatAround.R;
import com.web2mob.ChatAround.User.UserCorrespondence.Correspondence;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CorrespondencesView extends ListActivity 
{	
	private User.UserCorrespondence uc;
	private Timer myTimer; 
	
	private CorrespondenceAdapter correspondenceAdapter;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.correspondencesview);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        String userName = bundle.getString("userName");
        
        uc = User.thisUser.GetUserCorrespondenceByUserName(userName);
        if (uc == null) {
        	// big error
        	return;
        }
        
        TextView titleView = (TextView) findViewById(R.id.correspondencesview_title);
        titleView.setText("Messages between you and " + userName);
        ImageView userIcon = (ImageView) findViewById(R.id.correspondencesview_icon);
        int imagePosn = Images.getImagePosition(uc.userProfile.imageName, uc.userProfile.gender, true);
    	Integer imageId = Images.getImageId(imagePosn, uc.userProfile.gender);
    	Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), imageId);
    	userIcon.setImageBitmap(bitmap);
    	
    	TextView userNameTextView = (TextView) findViewById(R.id.correspondencesview_username);
    	int thisYear = new Date().getYear() + 1900;
		String userNameText = uc.userProfile.userName + " is a " + (thisYear - uc.userProfile.dobYear) + " year old ";
		if (uc.userProfile.isMale())
			userNameText += "guy";
		else
			userNameText += "gal";
    	userNameTextView.setText(userNameText);
    	TextView statusTextView = (TextView) findViewById(R.id.correspondencesview_profile);
    	statusTextView.setText(uc.userProfile.status);
        
        Button newMessageButton = (Button) findViewById(R.id.correspondencesview_sendmessage);
        newMessageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	sendMessage();
            }
        });
        
        Timestamp oldLastViewedTime = uc.lastViewedTime;
        
        // we can't pass a null here
		if (uc.correspondences == null)
			uc.correspondences = new ArrayList<Correspondence>();

        correspondenceAdapter = new CorrespondenceAdapter(this, R.layout.correspondencesview_row, uc, oldLastViewedTime);
        setListAdapter(correspondenceAdapter);
        correspondenceAdapter.notifyDataSetChanged();
        
		// setup a timer to update everything
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}
		}, 0, 10 * 1000); // every six hunderd seconds

        return;
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (uc.correspondences == null)
    		return;
        // set the last viewed timestamp to the last message create time
    	for (int i = 0; i < uc.correspondences.size(); ++i){
    		Correspondence correspondence = uc.correspondences.get(i);
    		if (!correspondence.received)
    			continue;
    		if (uc.lastViewedTime == null || uc.lastViewedTime.before(correspondence.created))
    			uc.lastViewedTime = uc.correspondences.get(i).created;
    	}
    }
    
    private boolean sendMessage() {
    	try
    	{
        	EditText newMessageEditText = (EditText) findViewById(R.id.correspondencesview_newmessagetext);
        	String newMessageText = newMessageEditText.getText().toString();
        	if (newMessageText == null || newMessageText.length() == 0) {
        		Toast.makeText(getApplicationContext(), "Write a message first", Toast.LENGTH_SHORT).show();
        		return false;
        	}

	    	Button sendButton = (Button) findViewById(R.id.correspondencesview_sendmessage);
	    	
	    	// Construct data 
	    	String data = URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(newMessageText, "UTF-8");
	    	data += "&" + URLEncoder.encode("toUserName", "UTF-8") + "=" + URLEncoder.encode(uc.userProfile.userName, "UTF-8");
	    	
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
	    	// DO NOT update the user's correspondence, otherwise after we get messages on the server 
	    	// we end up with two copies 
    		// update the user's correspondence
    		/*UserCorrespondence uc = User.thisUser.GetUserCorrespondenceByUserName(userName);
    		if (uc.correspondences == null)
    			uc.correspondences = new ArrayList<Correspondence>();
    		
    		Correspondence correspondence = uc.new Correspondence(false, message.getText().toString(), created);
    		uc.correspondences.add(correspondence);*/
	    	
            int duration = Toast.LENGTH_SHORT;

    		Toast toast = Toast.makeText(this, "Message has been sent", duration);
    		toast.show();
    		newMessageEditText.setText("");
	    	return true;
    	}
    	catch(Exception e){}
    	return false;
    }

    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	return;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.ACTIVITY_CREATEMESSAGE) {
            if (resultCode == RESULT_OK) {
            	correspondenceAdapter.notifyDataSetChanged();
            }
        }
    }
    
	private void TimerMethod()
	{
		//We call the method that will work with the UI
		//through the runOnUiThread method.
		this.runOnUiThread(Timer_Tick);
	}

	private Runnable Timer_Tick = new Runnable() {
		public void run() {
			//This method runs in the same thread as the UI. 

			new UpdateChatTask().execute("");

			/*Thread thread =  new Thread(null, getImportantUsers, "MagentoBackground");
			thread.start();
			progressDialog = ProgressDialog.show(UsersMapView.this,    
					"Please wait...", "Retrieving data ...", true);*/
		}
	};
	
	private void updateAdapter() {
		correspondenceAdapter.notifyDataSetChanged();
	}
	
    @Override
    protected void onStop(){
       super.onStop();

       if (myTimer != null) {
     	   myTimer.cancel();
     	   myTimer = null;
        }

       if (uc.lastViewedTime == null)
    	   return;
       // We need an Editor object to make preference changes.
       SharedPreferences settings = getSharedPreferences(Constants.prefsFileName, MODE_PRIVATE);
       SharedPreferences.Editor editor = settings.edit();
       editor.putString(uc.userProfile.userName + "_lastViewedTime", uc.lastViewedTime.toString());

       // Commit the edits!
       editor.commit();
    }
    
    private class UpdateChatTask extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... junk) {
            return (long) 0;
        }

        protected void onProgressUpdate(Integer... progress) {
            return;
        }

        protected void onPostExecute(Long result) {
            updateAdapter();
        }
    }
}

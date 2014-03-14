/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.web2mob.ChatAround.R;
import com.web2mob.ChatAround.User.UserCorrespondence;
import com.web2mob.ChatAround.User.UserLocation;
import com.web2mob.ChatAround.User.UserProfile;
import com.web2mob.ChatAround.User.UserCorrespondence.Correspondence;

public class MessageCreate extends Activity 
{
	private ProgressDialog progressDialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messagecreate);

        String uname = null;
        if (savedInstanceState != null && savedInstanceState.getSerializable("UserName") != null) 
        	uname = savedInstanceState.getSerializable("UserName").toString();
		if (uname == null) {
			Bundle extras = getIntent().getExtras();
			uname = extras != null ? extras.getString("UserName") : null;
		}
        
		final String userName = uname;
		
        TextView title = (TextView) findViewById(R.id.messagecreate_title);
        
        title.setText("Write your message to " + userName);
        
        Button sendButton = (Button) findViewById(R.id.send_message);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!sendMessage(userName))
                	return;
                setResult(RESULT_OK, null);
	    		finish();
            }
        });
    }

    private boolean sendMessage(String userName) {
    	try
    	{
	    	EditText message = (EditText) findViewById(R.id.messagecreate_content);
	    	
	    	Button sendButton = (Button) findViewById(R.id.send_message);
	    	
	    	if (message.getText().toString().length() == 0)
	    	{
	    		int duration = Toast.LENGTH_SHORT;

	    		Toast toast = Toast.makeText(this, "Message too short", duration);
	    		toast.show();
	    		return false;
	    	}
	    	progressDialog = ProgressDialog.show(this, "", "Sending...");
	    	// Construct data 
	    	String data = URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message.getText().toString(), "UTF-8");
	    	data += "&" + URLEncoder.encode("toUserName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8");
	    	
	    	// Send data
	    	WebServiceResponse response = WebServiceUtil.sendRequest("sendMessage.php", data);
	    	progressDialog.dismiss();
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
    		
	    	return true;
    	}
    	catch(Exception e){}
    	return false;
    }
}

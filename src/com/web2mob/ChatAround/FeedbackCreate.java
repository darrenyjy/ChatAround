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

public class FeedbackCreate extends Activity 
{
	private ProgressDialog progressDialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.feedbackcreate);
		
        TextView title = (TextView) findViewById(R.id.feedbackcreate_title);
        
        title.setText("Write your message to the developers");
        
        Button sendButton = (Button) findViewById(R.id.feedbackcreate_send_message);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!sendMessage())
                	return;
                setResult(RESULT_OK, null);
	    		finish();
            }
        });
    }

    private boolean sendMessage() {
    	try
    	{
	    	EditText message = (EditText) findViewById(R.id.feedbackcreate_content);
	    	
	    	if (message.getText().toString().length() < 10)
	    	{
	    		Toast.makeText(this, "Message too short", Toast.LENGTH_SHORT).show();
	    		return false;
	    	}
	    	
	    	progressDialog = ProgressDialog.show(this, "", "Sending...");
	    	// Construct data 
	    	String data = URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message.getText().toString(), "UTF-8");
	    	
	    	// Send data
	    	WebServiceResponse response = WebServiceUtil.sendRequest("sendFeedback.php", data);
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
	    	
    		Toast.makeText(this, "Thank you for the feedback", Toast.LENGTH_LONG).show();
	    	return true;
    	}
    	catch(Exception e){}
    	return false;
    }
}

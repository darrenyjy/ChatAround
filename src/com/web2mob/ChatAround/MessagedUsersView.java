/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.util.ArrayList;

import com.web2mob.ChatAround.R;
import com.web2mob.ChatAround.User.UserCorrespondence;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class MessagedUsersView extends ListActivity 
{	
	private MessagedUsersAdapter messagedUsersAdapter;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messagedusersview);
        
        this.messagedUsersAdapter = new MessagedUsersAdapter(this, R.layout.messagedusersview_row, User.thisUser.GetMessagedUsersArrayList());
        setListAdapter(messagedUsersAdapter);
        messagedUsersAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	// show this users messages
    	Intent i = new Intent(this, CorrespondencesView.class);
    	TextView userNameTextView = (TextView) v.findViewById(R.id.messagedusers_username);
    	userNameTextView.setTextColor(Color.WHITE); //change color back to normal
    	i.putExtra("userName", userNameTextView.getText().toString());
        startActivityForResult(i, Constants.ACTIVITY_VIEWMESSAGESFROMUSER);
        
    	return;
    }
    
    @Override 
    protected void onStop() {
    	super.onStop();
    	return;
    }
}

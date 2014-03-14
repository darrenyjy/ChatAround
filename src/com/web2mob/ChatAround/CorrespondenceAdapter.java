/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.sql.Timestamp;
import com.web2mob.ChatAround.R;
import com.web2mob.ChatAround.User.UserCorrespondence.Correspondence;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CorrespondenceAdapter extends ArrayAdapter<Correspondence> {
	private User.UserCorrespondence userCorrespondence;
	private Timestamp oldLastViewedTime;
	
	public CorrespondenceAdapter(Context context, int textViewResourceId, User.UserCorrespondence item, 
			Timestamp lastViewedTime) {
        super(context, textViewResourceId, item.correspondences);
        this.userCorrespondence = item;
        oldLastViewedTime = lastViewedTime;
	}
	
	@Override
	public int getCount() {
		return userCorrespondence.correspondences.size();
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.correspondencesview_row, null);
        }
            
        Correspondence correspondence = userCorrespondence.correspondences.get(position);
        if (correspondence != null) {
        	TextView sentByTextView = (TextView) v.findViewById(R.id.sentontext);
            TextView messageTextView = (TextView) v.findViewById(R.id.messagetext);
            String title;
            if (correspondence.received)// you received this message
            {
            	 title = "Message received on ";
            	 if (oldLastViewedTime == null || correspondence.created.after(oldLastViewedTime))
            		 sentByTextView.setTextColor(Color.GREEN);
            }
            else
            	title = "You said on ";
            title += correspondence.created.toLocaleString();
            sentByTextView.setText(title);

            if(messageTextView != null){
            	messageTextView.setText(correspondence.text);
            }
        }
        return v;
	}
}

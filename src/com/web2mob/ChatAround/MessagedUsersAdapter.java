/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.util.ArrayList;

import com.web2mob.ChatAround.R;
import com.web2mob.ChatAround.User.UserCorrespondence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessagedUsersAdapter extends ArrayAdapter<UserCorrespondence> {
	private ArrayList<UserCorrespondence> userCorrespondences;
	Context mContext;
	
	public MessagedUsersAdapter(Context context, int textViewResourceId, ArrayList<UserCorrespondence> items) {
	        super(context, textViewResourceId, items);
	        mContext = context;
	        userCorrespondences = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.messagedusersview_row, null);
        }
        UserCorrespondence uc = userCorrespondences.get(position);
        if (uc != null) {
        	ImageView userIcon = (ImageView) v.findViewById(R.id.messagedusers_icon);
        	int imagePosn = Images.getImagePosition(uc.userProfile.imageName, uc.userProfile.gender, true);
        	Integer imageId = Images.getImageId(imagePosn, uc.userProfile.gender);
        	Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), imageId);
        	userIcon.setImageBitmap(bitmap);
        	TextView userNameTextView = (TextView) v.findViewById(R.id.messagedusers_username);
            TextView profileTextView = (TextView) v.findViewById(R.id.messagedusers_profile);
            if (userNameTextView != null) {
            	userNameTextView.setText(uc.userProfile.userName);
            }
            if (uc.hasUnviewedCorrespondences(mContext)) // set the color to green if there are unread messages
            	userNameTextView.setTextColor(Color.GREEN);
            if(profileTextView != null && uc.userProfile.status != null){
            	profileTextView.setText("Status: " + uc.userProfile.status);
            }
        }
        return v;
	}
}

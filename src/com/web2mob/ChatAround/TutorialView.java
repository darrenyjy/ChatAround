/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import com.web2mob.ChatAround.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TutorialView extends Activity 
{
	int currentBackground = R.drawable.tutorial0;
	static final String info0 = "Enter some info about yourself";
	static final String info1 = "Check the map for people around you. Move around to find more people";
	static final String info2 = "Tap on some faces around you to know more about them";
	static final String info3 = "Send them a poke or a write a message";
	static final String info4 = "New messages are directly displayed on the map screen";
	static final String info5 = "The view messages screen shows your conversation";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tutorial);
	    Toast.makeText(this, info0, Toast.LENGTH_LONG).show();
	    
	    final Button tutorialPreviousBtn = (Button) findViewById(R.id.tutorial_previous_button);
	    final Button tutorialNextBtn = (Button) findViewById(R.id.tutorial_next_button);
	    final Button tutorialExitBtn = (Button) findViewById(R.id.tutorial_exit_button);
	    if (currentBackground == R.drawable.tutorial0)// hide the back button
	    	tutorialPreviousBtn.setVisibility(View.GONE);
	    tutorialPreviousBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				LinearLayout tutorialLayout = (LinearLayout) findViewById(R.id.tutorial_layout);
				if (currentBackground == R.drawable.tutorial1){
				    currentBackground = R.drawable.tutorial0;
				    Toast.makeText(view.getContext(), info0, Toast.LENGTH_LONG).show();
				    // hide the back button
				    tutorialPreviousBtn.setVisibility(View.GONE);
				}
				else if (currentBackground == R.drawable.tutorial2){
					Toast.makeText(view.getContext(), info1, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial1;
				}
				else if (currentBackground == R.drawable.tutorial3){
					Toast.makeText(view.getContext(), info2, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial2;
				}
				else if (currentBackground == R.drawable.tutorial4){
					Toast.makeText(view.getContext(), info3, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial3;
				}
				else if (currentBackground == R.drawable.tutorial5){
					Toast.makeText(view.getContext(), info4, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial4;
					tutorialNextBtn.setVisibility(View.VISIBLE);
				}
				tutorialLayout.setBackgroundResource(currentBackground);
				return;
			}});
	    
	    tutorialNextBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				LinearLayout tutorialLayout = (LinearLayout) findViewById(R.id.tutorial_layout);
				if (currentBackground == R.drawable.tutorial0){
					tutorialPreviousBtn.setVisibility(View.VISIBLE);
					Toast.makeText(view.getContext(), info1, Toast.LENGTH_LONG).show();
				    currentBackground = R.drawable.tutorial1;
				}
				else if (currentBackground == R.drawable.tutorial1){
					Toast.makeText(view.getContext(), info2, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial2;
				}
				else if (currentBackground == R.drawable.tutorial2){
					Toast.makeText(view.getContext(), info3, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial3;
				}
				else if (currentBackground == R.drawable.tutorial3){
					Toast.makeText(view.getContext(), info4, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial4;
				}
				else if (currentBackground == R.drawable.tutorial4){
					Toast.makeText(view.getContext(), info5, Toast.LENGTH_LONG).show();
					currentBackground = R.drawable.tutorial5;
					tutorialNextBtn.setVisibility(View.GONE);
				}
				tutorialLayout.setBackgroundResource(currentBackground);
				return;
			}});
	    
	    tutorialExitBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}});
	}
}

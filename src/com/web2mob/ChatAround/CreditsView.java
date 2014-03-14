/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;

import com.web2mob.ChatAround.R;

public class CreditsView extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creditsview);
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/segoepr.ttf");
        TextView name1 = (TextView) findViewById(R.id.credits_name1);
        name1.setTypeface(tf);
        
        TextView name2 = (TextView) findViewById(R.id.credits_name2);
        name2.setTypeface(tf);
        
        TextView name3 = (TextView) findViewById(R.id.credits_name3);
        name3.setTypeface(tf);
    }    

}

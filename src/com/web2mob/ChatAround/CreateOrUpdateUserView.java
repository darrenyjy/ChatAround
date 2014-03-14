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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

import java.net.URLEncoder;

import com.web2mob.ChatAround.R;

public class CreateOrUpdateUserView extends Activity 
{
	public static String isCreate = "IsCreate";
	private ProgressDialog progressDialog;
	private int selectedImagePosn = -1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createorupdateuser);
        
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        boolean showOnlyCreate = bundle.getBoolean(isCreate); // tells us whether to show the create or update screen

        EditText userNameText = (EditText) findViewById(R.id.createorupdateuser_userName);
        RadioButton rdbSexMale = (RadioButton) findViewById(R.id.createorupdateuser_rdbSexMale);
        rdbSexMale.setChecked(true);
        rdbSexMale.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Gallery gallery = (Gallery) findViewById(R.id.createorupdateuser_icongallery);
            	if (gallery == null)
            		return;
            	gallery.setAdapter(new ImageAdapter(v.getContext(), Images.getImageArray('M')));
            	selectedImagePosn = -1;
            }
        });
        
        RadioButton rdbSexFemale = (RadioButton) findViewById(R.id.createorupdateuser_rdbSexFemale);
        rdbSexFemale.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Gallery gallery = (Gallery) findViewById(R.id.createorupdateuser_icongallery);
            	if (gallery == null)
            		return;
            	gallery.setAdapter(new ImageAdapter(v.getContext(), Images.getImageArray('F')));
            	selectedImagePosn = -1;
            }
        });
        
        Spinner dobYearSpinner = (Spinner) findViewById(R.id.createorupdateuser_dobYear);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.year, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dobYearSpinner.setAdapter(adapter);
        
        EditText status = (EditText) findViewById(R.id.createorupdateuser_status);
        
        char gender = 'M';
        String imageName = null;
        if (User.thisUser != null)
        {
        	userNameText.setText(User.thisUser.userProfile.userName);
        	gender = User.thisUser.userProfile.gender;
        	
        	if (User.thisUser.userProfile.gender != 'M')
        		rdbSexFemale.setChecked(true);
        	dobYearSpinner.setSelection(User.thisUser.userProfile.dobYear - 1930);// couldn't think of a better way
        	status.setText(User.thisUser.userProfile.status);
        	imageName = User.thisUser.userProfile.imageName;
        }
        Gallery gallery = (Gallery) findViewById(R.id.createorupdateuser_icongallery);
        gallery.setSpacing(2);
        gallery.setAdapter(new ImageAdapter(this, Images.getImageArray(gender)));        
        gallery.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView parent, View v, int position, long id) 
            {
            	RadioButton rdbSexMale = (RadioButton) findViewById(R.id.createorupdateuser_rdbSexMale);
            	char gender = 'F';
                if (rdbSexMale.isChecked())
                	gender = 'M';
            	String imageName = Images.getImageName(position, gender);
                Toast.makeText(getBaseContext(), 
                	imageName + " selected", 
                	Toast.LENGTH_SHORT).show();
                selectedImagePosn = position;
            }
        });
        
        int imagePosn = -1;
        if (imageName != null)
        	imagePosn = Images.getImagePosition(imageName, gender, false);
        if (imagePosn != -1) {
        	selectedImagePosn = imagePosn;
        	gallery.setSelection(imagePosn);
        }

        final Button button = (Button) findViewById(R.id.createOrUpdateUser_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	EditText userNameText = (EditText) findViewById(R.id.createorupdateuser_userName);
            	String userName = userNameText.getText().toString();
            	if (userName.length() == 0) {
            		Toast.makeText(getApplicationContext(), "Enter your user name", Toast.LENGTH_LONG).show();
            		return;
            	}
            	
                RadioButton genderRadioButton = (RadioButton) findViewById(R.id.createorupdateuser_rdbSexMale);
                char gender = genderRadioButton.isChecked() ? 'M' : 'F';
            	
                Spinner dobYearSpinner = (Spinner) findViewById(R.id.createorupdateuser_dobYear);
                int dobYear = 0;
            	try
            	{
            		dobYear = Integer.parseInt(dobYearSpinner.getSelectedItem().toString());
            	}
            	catch (Exception e)
            	{
            		Toast.makeText(getApplicationContext(), "Enter your birth year", Toast.LENGTH_LONG).show();
            		return;
            	}
            	
            	//LinearLayout updateRow = (LinearLayout) findViewById(R.id.createorupdateuser_updaterow);
            	String status = null;
            	String imageName = null;
            	if (true){//(updateRow.getVisibility() == View.VISIBLE){
                    EditText statusText = (EditText) findViewById(R.id.createorupdateuser_status);
            		status = statusText.getText().toString();
            		if (status.length() == 0) {
            			Toast.makeText(getBaseContext(), "Enter your status", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if (selectedImagePosn == -1){
	            		Toast.makeText(getApplicationContext(), "Select an image", Toast.LENGTH_LONG).show();
	            		return;
	            	}
            	}

            	if (selectedImagePosn != -1)
            		imageName = Images.getImageName(selectedImagePosn, gender);
            	
            	if (!createOrUpdateUser(userName, gender, dobYear, status, imageName))
            		return;
            	
            	if (User.thisUser == null)
            		User.thisUser = new User();
            	
            	if (User.thisUser.userProfile == null)
            		User.thisUser.userProfile = User.thisUser.new UserProfile();
            	
            	// update our user
            	User.thisUser.userProfile.userName = userName;
            	User.thisUser.userProfile.gender = gender;
            	User.thisUser.userProfile.dobYear = dobYear;
            	User.thisUser.userProfile.status = status;
            	User.thisUser.userProfile.imageName = imageName;

            	setResult(RESULT_OK, null);
                finish();
            }
        });
    }

    public boolean createOrUpdateUser(String userName, char gender, int dobYear, String status, String imageName)
    {
    	try 
        { 
    		progressDialog = ProgressDialog.show(this, "", "Updating...");
    		
        	// Construct data 
        	String data = URLEncoder.encode("userName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8");
        	data += "&" + URLEncoder.encode("gender", "UTF-8") + "=" + gender;
        	data += "&" + URLEncoder.encode("dobYear", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(dobYear), "UTF-8");
        	if (status != null)
             	data += "&" + URLEncoder.encode("status", "UTF-8") + "=" + URLEncoder.encode(status, "UTF-8");
        	if (imageName != null)
        		data += "&" + URLEncoder.encode("imageName", "UTF-8") + "=" + URLEncoder.encode(imageName, "UTF-8");
        	
        	WebServiceResponse response = WebServiceUtil.sendRequest("createOrUpdateUser.php", data);
        	
        	progressDialog.dismiss();
        	if (response.errCode == Constants.ErrorCode.userNameExists)
        	{
        		Toast.makeText(getApplicationContext(), "User name is taken, choose a different one", Toast.LENGTH_LONG).show();
        		return false;
        	}
        	else if (response.errCode == Constants.ErrorCode.internalError)
        	{
                // some other error
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        		dialog.setTitle("Oops, we goofed up!");
        		dialog.setMessage("We're sorry, there has been an internal error. Please try again.");
        		dialog.show();
        		return false;
        	}
        } catch (Exception e) {
        	return false;
        }
        return true;
    }
    
    
    public class ImageAdapter extends BaseAdapter 
    {
        private Context context;
        private Integer[] imageArray;
 
        public ImageAdapter(Context c, Integer[] iArray) 
        {
            context = c;
            imageArray = iArray;
        }
 
        //---returns the number of images---
        public int getCount() {
            return imageArray.length;
        }
 
        //---returns the ID of an item--- 
        public Object getItem(int position) {
            return position;
        }            
 
        public long getItemId(int position) {
            return position;
        }
 
        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(imageArray[position]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new Gallery.LayoutParams(80, 80));
            //imageView.setBackgroundResource(itemBackground);
            return imageView;
        }
    }    

}

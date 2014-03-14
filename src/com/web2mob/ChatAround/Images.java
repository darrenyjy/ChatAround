package com.web2mob.ChatAround;

import com.web2mob.ChatAround.R;

public class Images {
    //---the images to display---
    private static Integer[] imageIdsMale = {
            R.drawable.a01,
            R.drawable.a02,
            R.drawable.a03,
            R.drawable.a04,
            R.drawable.a05,
            R.drawable.b01,
            R.drawable.b02,
            R.drawable.b03,
            R.drawable.b04,
            R.drawable.b05,
            R.drawable.c01,
            R.drawable.c02,
            R.drawable.c03,
            R.drawable.c04,
            R.drawable.c05,
            R.drawable.d01,
            R.drawable.d02,
            R.drawable.d03,
            R.drawable.d04,
            R.drawable.d05,
            R.drawable.e01,
            R.drawable.e02,
            R.drawable.e03,
            R.drawable.e04,
            R.drawable.e05,
            R.drawable.f01,
            R.drawable.f02,
            R.drawable.f03,
            R.drawable.f04,
            R.drawable.f05,
            R.drawable.g01,
            R.drawable.g02,
            R.drawable.g03,
            R.drawable.g04,
            R.drawable.g05,
            R.drawable.h01,
            R.drawable.h02,
            R.drawable.h03,
            R.drawable.h04,
            R.drawable.h05
    };
    
    private static String[] imageNamesMale = {
        "a01",
        "a02",
        "a03",
        "a04",
        "a05",
        "b01",
        "b02",
        "b03",
        "b04",
        "b05",
        "c01",
        "c02",
        "c03",
        "c04",
        "c05",
        "d01",
        "d02",
        "d03",
        "d04",
        "d05",
        "e01",
        "e02",
        "e03",
        "e04",
        "e05",
        "f01",
        "f02",
        "f03",
        "f04",
        "f05",
        "g01",
        "g02",
        "g03",
        "g04",
        "g05",
        "h01",
        "h02",
        "h03",
        "h04",
        "h05"
    };
    
    private static Integer[] imageIdsFemale = {
        R.drawable.fa01,
        R.drawable.fa02,
        R.drawable.fa03,
        R.drawable.fa04,
        R.drawable.fa05,
        R.drawable.fb01,
        R.drawable.fb02,
        R.drawable.fb03,
        R.drawable.fb04,
        R.drawable.fb05,
        R.drawable.fc01,
        R.drawable.fc02,
        R.drawable.fc03,
        R.drawable.fc04,
        R.drawable.fc05,
        R.drawable.fd01,
        R.drawable.fd02,
        R.drawable.fd03,
        R.drawable.fd04,
        R.drawable.fd05,
        R.drawable.fe01,
        R.drawable.fe02,
        R.drawable.fe03,
        R.drawable.fe04,
        R.drawable.fe05,
        R.drawable.fg01,
        R.drawable.fg02,
        R.drawable.fg03,
        R.drawable.fg04,
        R.drawable.fg05,
        R.drawable.fh01,
        R.drawable.fh02,
        R.drawable.fh03,
        R.drawable.fh04,
        R.drawable.fh05,
        R.drawable.fi01,
        R.drawable.fi02,
        R.drawable.fi03,
        R.drawable.fi04,
        R.drawable.fi05
    };

    private static String[] imageNamesFemale = {
	    "fa01",
	    "fa02",
	    "fa03",
	    "fa04",
	    "fa05",
	    "fb01",
	    "fb02",
	    "fb03",
	    "fb04",
	    "fb05",
	    "fc01",
	    "fc02",
	    "fc03",
	    "fc04",
	    "fc05",
	    "fd01",
	    "fd02",
	    "fd03",
	    "fd04",
	    "fd05",
	    "fe01",
	    "fe02",
	    "fe03",
	    "fe04",
	    "fe05",
	    "fg01",
	    "fg02",
	    "fg03",
	    "fg04",
	    "fg05",
	    "fh01",
	    "fh02",
	    "fh03",
	    "fh04",
	    "fh05",
	    "fi01",
	    "fi02",
	    "fi03",
	    "fi04",
	    "fi05",
	};
    
    public static String getImageName(int position, char gender)
    {
    	if (gender == 'M')
    		return imageNamesMale[position];
    	return imageNamesFemale[position];
    }
    
    public static int getImagePosition(String imageName, char gender, boolean defaultIfNotFound)
    {
    	String[] imageNames = null;
    	if (gender == 'M')
    		imageNames = imageNamesMale;
    	else
    		imageNames = imageNamesFemale;
    	
    	if (imageName != null) {
	    	for (int i = 0; i < imageNames.length; ++i) {
	    		if (imageNames[i].compareTo(imageName) == 0)
	    			return i;
	    	}
    	}
    	
    	if (defaultIfNotFound)
    		return 3;
    	return -1;
    }
    
    public static int getImageId(int position, char gender)
    {
    	if (gender == 'M')
    		return imageIdsMale[position];
    	return imageIdsFemale[position];
    }
    
    public static Integer[] getImageArray(char gender)
    {
    	if (gender == 'M')
    		return imageIdsMale;
    	return imageIdsFemale;
    }
}

/*
Copyright © 2010, Ninad Divadkar and ChongLim Kim
All rights reserved.
ninad.divadkar@gmail.com, clkim@ieee.org
*/
package com.web2mob.ChatAround;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WebServiceUtil {	
	public static WebServiceResponse sendRequest(String webServiceName, String data)
    {
    	String result = null;
    	try
    	{
    		if (data != null && data.length() > 0)
    			data += "&" + URLEncoder.encode("phoneUniqueId", "UTF-8") + "=" + URLEncoder.encode(Constants.phoneUniqueId, "UTF-8");	
    		else
    			data = URLEncoder.encode("phoneUniqueId", "UTF-8") + "=" + URLEncoder.encode(Constants.phoneUniqueId, "UTF-8");
    		data += "&" + URLEncoder.encode("phoneHashKey", "UTF-8") + "=" + URLEncoder.encode(Constants.getPhoneHashKey(), "UTF-8");
    		data += "&" + URLEncoder.encode("appName", "UTF-8") + "=" + URLEncoder.encode(Constants.appName, "UTF-8");
    		data += "&" + URLEncoder.encode("appVersion", "UTF-8") + "=" + URLEncoder.encode(Constants.appVersion, "UTF-8");
    		
	    	// Send data
	    	URL url = new URL(Constants.hostName + webServiceName); 
	    	URLConnection conn = url.openConnection();
	    	conn.setDoOutput(true); 
	    	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
	    	wr.write(data); wr.flush(); 
	    	
	    	// Get the response 
	    	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
	    	String line;
	    	result = new String();
	    	while ((line = rd.readLine()) != null) 
	    	{
	    		// Process line...
	    		result += line;
	    	} 
	    	wr.close();
	    	rd.close();
    	}
    	catch (Exception e) 
    	{
    		e.getMessage();
    	}
    	return new WebServiceResponse(result);
    }
	
	public static String getNodeValue(Element fstElmnt, String tagName)
	{
    	NodeList elmntLst = fstElmnt.getElementsByTagName(tagName);
    	Element elmnt = (Element) elmntLst.item(0);
    	NodeList nodeLst = elmnt.getChildNodes();
    	if (nodeLst == null)
    		return null;
    	if (nodeLst.item(0) == null)
    		return null;
    	return ((Node) nodeLst.item(0)).getNodeValue();
	}
}

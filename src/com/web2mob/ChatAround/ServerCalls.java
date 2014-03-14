package com.web2mob.ChatAround;

import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ServerCalls {
    public static Timestamp parseMessageCreateResponse(Document doc)
    {
    	try
    	{
	    	NodeList nodeLst = doc.getElementsByTagName("success");
			
			if (nodeLst.getLength() > 0)
			{
				for (int i = 0; i < nodeLst.getLength(); i++) 
				{
					Node successNode = nodeLst.item(i);
					if (successNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element fstElmnt = (Element) successNode;
	
						Timestamp created = Timestamp.valueOf(WebServiceUtil.getNodeValue(fstElmnt, "created"));
						return created;
					}
				}
			}
		} catch (Exception e) { 
		String eMsg = e.getMessage();
		}
		return null;
    }
}

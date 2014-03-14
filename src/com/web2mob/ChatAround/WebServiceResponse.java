package com.web2mob.ChatAround;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WebServiceResponse
{
	Document doc;
	Constants.ErrorCode errCode;
	String errMsg;
	private String result;
	
	public WebServiceResponse(String webServiceResult)
	{
		result = webServiceResult;
		errCode = Constants.ErrorCode.noError;
		errMsg = null;
		doc = null;
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(result));

			doc = db.parse(is);

			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("failure");
			
			if (nodeLst.getLength() > 0)
			{
				for (int i = 0; i < nodeLst.getLength(); i++) 
				{
					Node failureNode = nodeLst.item(i);
					if (failureNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element fstElmnt = (Element) failureNode;

						String errorCode = WebServiceUtil.getNodeValue(fstElmnt, "errorCode");
						errCode = Constants.ErrorCode.valueOf(errorCode);
						errMsg = WebServiceUtil.getNodeValue(fstElmnt, "errorMessage");
						return;
					}
				}
			}
			errCode = Constants.ErrorCode.noError;
			errMsg = null;
		}
		catch (Exception e)
		{
			errCode = Constants.ErrorCode.internalError;
			errMsg = e.getMessage();
		}
	}
}
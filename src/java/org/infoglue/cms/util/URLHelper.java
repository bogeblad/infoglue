package org.infoglue.cms.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class URLHelper {
	private final static String CONTENT_ID   = "contentId";
	private final static String SITE_NODE_ID = "siteNodeId";
	private final static String LANGUAGE_ID  = "languageId";
	
	private String baseURL;
	private Map parameters = new HashMap();
	
	/*
	 * 
	 */
	public URLHelper(String baseURL, Integer contentID, Integer siteNodeID, Integer languageID) {
		this.baseURL = baseURL;
		addParameter(CONTENT_ID,   contentID.toString());
		addParameter(SITE_NODE_ID, siteNodeID.toString());
		addParameter(LANGUAGE_ID,  languageID.toString());
	}
	
	
	
	/**
	 * 
	 */
	public String getURL() {
		if(!parameters.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for(Iterator i = parameters.keySet().iterator(); i.hasNext(); ) {
				String name      = (String) i.next();
				String value     = (String) parameters.get(name);
				String parameter = name + "=" + value;
				sb.append(parameter + (i.hasNext() ? "&" : ""));
			}
			return baseURL + "?" + sb.toString();
		}
		return baseURL;
	}
	
	/**
	 * 
	 */
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}
}

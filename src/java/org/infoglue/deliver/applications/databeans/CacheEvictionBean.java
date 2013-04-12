/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.deliver.applications.databeans;

import java.util.HashMap;
import java.util.Map;

import org.infoglue.deliver.util.HttpHelper;

/**
 * This class contains information about a cache update.
 * 
 * @author mattias
 */

public class CacheEvictionBean
{
	public final static int TRANS_CREATE      = 0;
	public final static int TRANS_UPDATE      = 1;
	public final static int TRANS_DELETE      = 2;
	public final static int PUBLISHING        = 10;
	public final static int DENIED_PUBLISHING = 20;
	public final static int UNPUBLISHING      = 30;
	
	public final static String TRANS_CREATE_TEXT 		= "create";
	public final static String TRANS_UPDATE_TEXT 		= "update";
	public final static String TRANS_DELETE_TEXT		= "delete";
	public final static String PUBLISHING_TEXT   		= "publishing";
	public final static String DENIED_PUBLISHING_TEXT 	= "publishing denied";
	public final static String UNPUBLISHING_TEXT   		= "unpublishing";

	private long receivedTimestamp = System.currentTimeMillis();
	private long processedTimestamp = -1;

	private Integer publicationId = -1;
	private String userName = "Unknown";
	private long timestamp = System.currentTimeMillis() - 2000;
	private String className = null;
	private String objectId = null;
	private String objectName = null;
	private String typeId = null;
	private Map<String,String> extraInfo = new HashMap<String,String>();

	public CacheEvictionBean(Integer publicationId, String userName, String timestamp, String className, String typeId, String objectId, String objectName, Map<String,String> extraInfo)
	{
		this.publicationId = publicationId;
		if(userName != null)
			this.userName = userName;
		if(timestamp != null)
			this.timestamp = Long.parseLong(timestamp);
		this.className = className;
		this.typeId = typeId;
		this.objectId = objectId;
		this.objectName = objectName;	
		this.extraInfo = extraInfo;
	}

	public CacheEvictionBean(Integer publicationId, String userName, String timestamp, String className, String typeId, String objectId, String objectName, String receivedTimestamp, String processedTimestamp)
	{
		this.publicationId = publicationId;
		if(userName != null)
			this.userName = userName;
		if(timestamp != null)
			this.timestamp = Long.parseLong(timestamp);
		this.className = className;
		this.typeId = typeId;
		this.objectId = objectId;
		this.objectName = objectName;	
		this.receivedTimestamp = Long.parseLong(receivedTimestamp);
		this.processedTimestamp = Long.parseLong(processedTimestamp);
	}
	
	public Integer getPublicationId()
    {
        return publicationId;
    }
	
	public String getUserName()
    {
        return userName;
    }
	
	public long getTimestamp()
    {
        return timestamp;
    }

	public long getReceivedTimestamp()
    {
        return receivedTimestamp;
    }

    public String getClassName()
    {
        return className;
    }

    public String getObjectId()
    {
        return objectId;
    }
    
    public String getObjectName()
    {
        return objectName;
    }
    
    public String getTypeId()
    {
        return typeId;
    }

	public void setProcessed()
	{
		this.processedTimestamp = System.currentTimeMillis();
	}

	public long getProcessedTimestamp()
    {
        return processedTimestamp;
    }

	public Map<String,String> getExtraInformation()
    {
        return this.extraInfo;
    }

	public static String getTransactionTypeName(Integer transactionType)
	{
		switch (transactionType.intValue())
		{
			case (int) (TRANS_CREATE):
				return TRANS_CREATE_TEXT;
			case (TRANS_DELETE):
				return TRANS_DELETE_TEXT;
			case (TRANS_UPDATE):
				return TRANS_UPDATE_TEXT;
			case (PUBLISHING):
				return PUBLISHING_TEXT;
			case (DENIED_PUBLISHING):
				return DENIED_PUBLISHING_TEXT;
		}
		return "unknown - map " + transactionType + " to correct text";
	}

	public static CacheEvictionBean getCacheEvictionBean(Map<String,String> map)
	{
		if(map.get("publicationId") == null)
			return null;
		
		CacheEvictionBean newBean = new CacheEvictionBean(new Integer(map.get("publicationId")),map.get("userName"), map.get("timestamp"),map.get("className"), map.get("typeId"), map.get("objectId"), map.get("objectName"), map.get("receivedTimestamp"), map.get("processedTimestamp"));
		
		return newBean;
	}

	public String toQueryString() throws Exception
	{
		Map<String,String> map = new HashMap<String,String>();
		map.put("publicationId", "" + this.getPublicationId());
		map.put("userName", this.getUserName());
		map.put("timestamp", "" + this.getTimestamp());
		map.put("className", "" + this.getClassName());
		map.put("objectId", "" + this.getObjectId());
		map.put("objectName", "" + this.getObjectName());
		map.put("extraInfo", "" + this.getExtraInformation());
		map.put("typeId", "" + this.getTypeId());
		map.put("receivedTimestamp", "" + this.getReceivedTimestamp());
		map.put("processedTimestamp", "" + this.getProcessedTimestamp());
		String status = "Received";
		if(getProcessedTimestamp() > 0)
			status = "Processed";
		
		map.put("status", "" + status);
		
		HttpHelper helper = new HttpHelper();
		return helper.toEncodedString(map, "utf-8");
	}
}
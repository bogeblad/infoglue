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

/**
 * This class is meant to store information about a cache update.
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

	private String className = null;
	private String objectId = null;
	private String objectName = null;
	private String typeId = null;

	public CacheEvictionBean(String className, String typeId, String objectId, String objectName)
	{
		this.className = className;
		this.typeId = typeId;
		this.objectId = objectId;
		this.objectName = objectName;	
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
}
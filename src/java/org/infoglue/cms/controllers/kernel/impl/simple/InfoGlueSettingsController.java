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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.InfoGlueProperty;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.common.settings.controllers.CastorSettingsController;
import org.infoglue.common.settings.controllers.CastorSettingsPersister;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.CacheController;

import webwork.action.ActionContext;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;


public class InfoGlueSettingsController extends BaseController implements CastorSettingsPersister
{
	private static Log log = LogFactory.getLog(InfoGlueSettingsController.class);

	private DOMBuilder domBuilder = new DOMBuilder();
	private CastorSettingsController settingsController = null;
	//private InfoGluePropertyHelper propertyHelper = new FileInfoGluePropertyHelper();
	
	/**
	 * A simple factory method
	 */
	
	public static InfoGlueSettingsController getInfoGlueSettingsController()
	{
		return new InfoGlueSettingsController();
	}
	
	private InfoGlueSettingsController()
	{
		this.settingsController = CastorSettingsController.getController(this);
	}

	public InfoGlueProperty getPropertyWithId(Integer id, Database db) throws SystemException, Bug
    {
    	return (InfoGlueProperty) getObjectWithId(InfoGlueProperty.class, id, db);
    } 

	/**
	 * This method returns a list (of strings) of all label-keys the system uses.
	 */
	
	public List getSystemSettings(String bundleName)
	{
		return settingsController.getSystemSettings(bundleName);
	}

	/**
	 * This method returns a list (of locales) of all defined label-languages.
	 */
	
	public List getSettingsVariations(String nameSpace, String name, Database database) throws Exception
	{
		return settingsController.getSettingsVariations(nameSpace, name, database);
	}

	private Document getPropertyDocument(String nameSpace, String name, Database database) throws Exception
	{
		return settingsController.getPropertyDocument(nameSpace, name, database);
	}

	public void addVariation(String nameSpace, String variationId, Database database) throws Exception
	{
		settingsController.addVariation(nameSpace, "systemSettings", variationId, database);
	}

	public void addVariation(String nameSpace, String name, String variationId, Database database) throws Exception
	{
		settingsController.addVariation(nameSpace, name, variationId, database);
	}

	public void removeVariation(String nameSpace, String variationId, Database database) throws Exception
	{
		settingsController.removeVariation(nameSpace, "systemSettings", variationId, database);
	}

	public void removeVariation(String nameSpace, String name, String variationId, Database database) throws Exception
	{
		settingsController.removeVariation(nameSpace, name, variationId, database);
	}

	public void removeProperty(String nameSpace, String variationId, String key, Database database) throws Exception
	{
		settingsController.removeProperty(nameSpace, "systemSettings", variationId, key, database);
	}

	public void removeProperty(String nameSpace, String name, String variationId, String key, Database database) throws Exception
	{
		settingsController.removeProperty(nameSpace, name, variationId, key, database);
	}

	public void updateSettings(String nameSpace, String variationId, Map properties, Database database) throws Exception
	{
		settingsController.updateSettings(nameSpace, "systemSettings", variationId, properties, database);
	}

	public void updateSettings(String nameSpace, String name, String variationId, Map properties, Database database) throws Exception
	{
		settingsController.updateSettings(nameSpace, name, variationId, properties, database);
	}

	public void updateSetting(String nameSpace, String name, String variationId, String key, String value, Database database) throws Exception
	{
		Map propertiesMap = new HashMap();
		propertiesMap.put(key, value);
		
		settingsController.updateSettings(nameSpace, name, variationId, propertiesMap, database);
	}

	public String getSetting(String nameSpace, String name, String derivedValue, String variationId, Database database) throws Exception
	{
		return settingsController.getSetting(nameSpace, name, derivedValue, variationId, database);
	}

    /**
     * This method returns a InfoGlueProperty based on it's primary key inside a transaction
     * @return InfoGlueProperty
     * @throws Exception
     */

	public InfoGlueProperty getProperty(Integer id, Database database) throws Exception
    {
		InfoGlueProperty property = (InfoGlueProperty)database.load(InfoGlueProperty.class, id);
		
		return property;
    }
    
    
    /**
     * Gets a list of all events available for a particular day.
     * @return List of Event
     * @throws Exception
     */
    
    public InfoGlueProperty getProperty(String nameSpace, String name, Database database) throws Exception 
    {
        InfoGlueProperty property = null;

        boolean localTransaction = false;
    	if(database == null)
    	{
    		localTransaction = true;
	    	database = CastorDatabaseService.getDatabase();
	    	database.begin();
	    }
    	
		OQLQuery oql = database.getOQLQuery("SELECT p FROM org.infoglue.cms.entities.management.InfoGlueProperty p WHERE p.nameSpace = $1 AND p.name = $2 ORDER BY p.id");
		oql.bind(nameSpace);
		oql.bind(name);
		
		QueryResults results = oql.execute();

		if (results.hasMore()) 
        {
			property = (InfoGlueProperty)results.next();
        }
        
		results.close();
		oql.close();
        
    	if(localTransaction)
    	{
        	database.commit();
        	database.close();
    	}

        return property;
    }

    
    /**
     * This method is used to create a new InfoGlueProperty object in the database inside a transaction.
     */
    
    public InfoGlueProperty createProperty(String nameSpace, String name, String value, Database database) throws Exception 
    {
        boolean localTransaction = false;
    	if(database == null)
    	{
    		localTransaction = true;
	    	database = CastorDatabaseService.getDatabase();
	    	database.begin();
	    }
    	
    	InfoGlueProperty property = new InfoGlueProperty();
        property.setNameSpace(nameSpace);
        property.setName(name);
        property.setValue(value);

        database.create(property);
        
    	if(localTransaction)
    	{
        	database.commit();
        	database.close();
    	}
    	
    	log.debug("Creating property with:" + nameSpace + ":" + name);        

        return property;
    }
    
    
    /**
     * Updates an property.
     * 
     * @throws Exception
     */
    
    public void updateProperty(String nameSpace, String name, String value, Database database) throws Exception 
    {
        boolean localTransaction = false;
    	if(database == null)
    	{
    		localTransaction = true;
	    	database = CastorDatabaseService.getDatabase();
	    	database.begin();
	    }

    	InfoGlueProperty property = getProperty(nameSpace, name, database);
		if(property == null)
			property = createProperty(nameSpace, name, value, database);

		updateProperty(property, value, database);

    	if(localTransaction)
    	{
        	database.commit();
        	database.close();
    	}
    }
    
    /**
     * Updates an property inside an transaction.
     * 
     * @throws Exception
     */
    
    public void updateProperty(InfoGlueProperty property, String value, Database database) throws Exception 
    {
    	property.setValue(value);
    }


    public String getProperty(String key, String variationId, boolean skipInfoGlueProperty, boolean fallbackToDefault, boolean fallbackToKey, boolean useDerivedValue, Database database)
    {
    	return getProperty(key, "systemSettings", variationId, skipInfoGlueProperty, fallbackToDefault, fallbackToKey, useDerivedValue, database);
    }

    public String getProperty(String key, String name, String variationId, boolean skipInfoGlueProperty, boolean fallbackToDefault, boolean fallbackToKey, boolean useDerivedValue, Database database)
    {
    	return getPropertyFromPropertySet(key, name, variationId, skipInfoGlueProperty, fallbackToDefault, fallbackToKey, useDerivedValue, database);
    	//return getPropertyFromInfoGlue(key, name, variationId, skipInfoGlueProperty, fallbackToDefault, fallbackToKey, useDerivedValue, database);
    }
    
    public String getPropertyFromPropertySet(String key, String name, String variationId, boolean skipInfoGlueProperty, boolean fallbackToDefault, boolean fallbackToKey, boolean useDerivedValue, Database database)
    {
	    Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    return ps.getString(key);
    }
    
    public String getPropertyFromInfoGlue(String key, String name, String variationId, boolean skipInfoGlueProperty, boolean fallbackToDefault, boolean fallbackToKey, boolean useDerivedValue, Database database)
    {
	    String value = null;
	    String newValue = null;
		
        String cacheKey = "" + key + "_" + name + "_" + variationId + "_" + skipInfoGlueProperty + "_" + fallbackToDefault + "_" + fallbackToKey + "_" + useDerivedValue;
        Object cacheObject = CacheController.getCachedObject(CacheController.SETTINGSPROPERTIESCACHENAME, cacheKey);
	    if(cacheObject instanceof NullObject)
		{
	    	return null;
		}
	    else if(cacheObject != null)
		{
	    	return cacheObject.toString().trim();
		}

	    String property = "";
    	if(fallbackToKey)
    		property = key;
	    
	    try
	    {
	        String derivedValue = null;
	        if(useDerivedValue)
	        {
	        	Object derivedObject = findOnValueStack(key);
		        if(derivedObject != null)
		        	derivedValue = derivedObject.toString();
	        }
	        
	        if(!skipInfoGlueProperty)
	        {
		        if(derivedValue != null)
		        	property = InfoGlueSettingsController.getInfoGlueSettingsController().getSetting("infoglueCMS", name, derivedValue, variationId, database);
		        else
		        	property = InfoGlueSettingsController.getInfoGlueSettingsController().getSetting("infoglueCMS", name, key, variationId, database);
	        }
	        
	        if(skipInfoGlueProperty || ((property == null || property.equals("")) && fallbackToDefault))
	        {
		    	if(derivedValue != null)
		    		property = CmsPropertyHandler.getProperty(derivedValue);
		        else
		        	property = CmsPropertyHandler.getProperty(key);
	        }
	        
	        if((property == null || property.equals("")) && fallbackToKey)
	        	property = key;
	    
		    if(property == null)
		    {
		    	CacheController.cacheObject(CacheController.SETTINGSPROPERTIESCACHENAME, cacheKey, new NullObject());
		    }
		    else
		    {
		    	CacheController.cacheObject(CacheController.SETTINGSPROPERTIESCACHENAME, cacheKey, property);
		    }
	    }
	    catch(Exception e)
	    {
	        log.warn("An property was not found for key: " + key + ": " + e.getMessage(), e);
	    }
	    
	    return property;
    }

    public static Object findOnValueStack(String expr) 
    {
		ActionContext a = ActionContext.getContext();
		Object value = a.getValueStack().findValue(expr);
		return value;
	}
/*
	public void setInfoGluePropertyHelper(InfoGluePropertyHelper propertyHelper) 
	{
		this.propertyHelper = propertyHelper;
	}
*/

	public void setInfoGlueProperty(String string, String string2, String string3, String allowedContentTypeNames)
	{
		// TODO Auto-generated method stub
		
	}

	public BaseEntityVO getNewVO()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public InfoGlueProperty getProperty(Long id, Database database) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
}

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.FormEntry;
import org.infoglue.cms.entities.management.FormEntryAsset;
import org.infoglue.cms.entities.management.FormEntryAssetVO;
import org.infoglue.cms.entities.management.FormEntryVO;
import org.infoglue.cms.entities.management.FormEntryValue;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.impl.simple.FormEntryAssetImpl;
import org.infoglue.cms.entities.management.impl.simple.FormEntryImpl;
import org.infoglue.cms.entities.management.impl.simple.FormEntryValueImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.Timer;


/**
 * @author Mattias Bogeblad
 */

public class LabelController extends BaseController implements StringManager
{
    private final static Logger logger = Logger.getLogger(LabelController.class.getName());

    private Locale locale = Locale.getDefault();
    
    private static Map<String,LabelController> controllers = new HashMap<String,LabelController>();
    
    private LabelController()
    {
    }

    private LabelController(Locale locale)
    {
    	this.locale = locale;
    }

	/**
	 * Factory method
	 */

	public static LabelController getController(Locale locale)
	{
		LabelController controller = controllers.get("" + locale.getLanguage());
		if(controller == null)
		{
			controller = new LabelController(locale);
			controllers.put("" + locale.getLanguage(), controller);
		}
		
		return controller;
	}

   	/**
   	 * This method deletes a digital asset in the database.
   	 */

   	public static void delete(Integer digitalAssetId) throws ConstraintException, SystemException
   	{
		deleteEntity(DigitalAssetImpl.class, digitalAssetId);
   	}

   	
	public List<Locale> getAvailableTranslations()
	{
		List<Locale> translations = new ArrayList<Locale>();
		
		try
		{
			File file = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations");
			File[] translationFiles = file.listFiles();
			for(int i=0; i<translationFiles.length; i++)
			{
				File translation = translationFiles[i];
				if(!translation.isDirectory())
				{
					String name = translation.getName();
					if(name.startsWith("PresentationStrings_"))
					{
						String localeName = name.substring("PresentationStrings_".length(), name.indexOf("."));
						//System.out.println("localeName:" + localeName);
						try
						{
							Locale locale = new Locale(localeName);
							translations.add(locale);
						}
						catch (Exception e) 
						{
							logger.error("Error getting locale for " + localeName + ":" + e.getMessage(), e);
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("Could not get themes: " + e.getMessage(), e);
		}
		
		return translations;
	}
    
	private static Map<String,Object> cachedBundles = new HashMap<String,Object>();
	private static Map<String,String> cachedLabels = new HashMap<String,String>();
	
	public String getLocalizedString(Locale locale, String key)
	{
		Timer t = new Timer();
		
		String value = null;

		String valueCacheKey = "" + locale + "_" + key;
		String cachedLabel = cachedLabels.get(valueCacheKey);
		//System.out.println("valueCacheKey:" + valueCacheKey + " gave " + cachedLabel);
		if(cachedLabel != null)
		{
			//t.printElapsedTime("Getting getLocalizedString...");
			return cachedLabel;
		}
		
		String cacheKey = "" + locale;
		Object cachedBundle = cachedBundles.get(cacheKey);
		//System.out.println("cacheKey:" + cacheKey + " gave " + cachedBundle);
		
		ResourceBundle resourceBundle = null;

		//t.printElapsedTime("1");

		if(!(cachedBundle instanceof NullObject))
		{
			//System.out.println("Not a NullObject.." + cachedBundle);
			if(cachedBundle != null)
			{
				//System.out.println("Not null:" + cachedBundle.getClass().getName());
				resourceBundle = (ResourceBundle)cachedBundle;
			}
			else
			{
				//System.out.println("A null:" + cachedBundle);
				try
				{
					//t.printElapsedTime("1.1");
					resourceBundle = getResourceBundle(locale);
					//t.printElapsedTime("1.2");
					if(resourceBundle == null)
					{
						resourceBundle = checkForResourceBundleAsset(locale);
						//t.printElapsedTime("1.3");
					}
					if(resourceBundle == null)
						cachedBundles.put(cacheKey, new NullObject());
					else
						cachedBundles.put(cacheKey, resourceBundle);
				}
				catch (Exception e) 
				{
					logger.error("Could not get value from bundle:" + e.getMessage(), e);
				}
			}
		}			
		//t.printElapsedTime("2");
		
		if(resourceBundle != null)
		{
			value = resourceBundle.getString(key);
		}
		
		//t.printElapsedTime("3");

		if(value == null || value.equals(""))
			value = getLocalizedSystemString(locale, key);
			
		if(value != null)
			cachedLabels.put(valueCacheKey, value);
		
		//t.printElapsedTime("Getting getLocalizedString...");
		
		return value;
	}

	public String getLocalizedString(Locale locale, String key, Object[] args)
	{
		Timer t = new Timer();
		
		String value = null;

		String cacheKey = "" + locale;
		Object cachedBundle = cachedBundles.get(cacheKey);

		ResourceBundle resourceBundle = null;

		//t.printElapsedTime("1");

		if(!(cachedBundle instanceof NullObject))
		{
			//System.out.println("Not a NullObject.." + cachedBundle);
			if(cachedBundle != null)
			{
				//System.out.println("Not null:" + cachedBundle.getClass().getName());
				resourceBundle = (ResourceBundle)cachedBundle;
			}
			else
			{
				//System.out.println("A null:" + cachedBundle);
				try
				{
					//t.printElapsedTime("1.1");
					resourceBundle = getResourceBundle(locale);
					//t.printElapsedTime("1.2");
					if(resourceBundle == null)
					{
						resourceBundle = checkForResourceBundleAsset(locale);
						//t.printElapsedTime("1.3");
					}
					if(resourceBundle == null)
						cachedBundles.put(cacheKey, new NullObject());
					else
						cachedBundles.put(cacheKey, resourceBundle);
				}
				catch (Exception e) 
				{
					logger.error("Could not get value from bundle:" + e.getMessage(), e);
				}
			}
		}			
		//t.printElapsedTime("2");
		//System.out.println("\n\n:args:" + args[0]);
		if(resourceBundle != null)
		{
			value = MessageFormat.format(resourceBundle.getString(key), args);
		}
		
		//t.printElapsedTime("3");

		if(value == null || value.equals(""))
			value = getLocalizedSystemString(locale, key, args);
			
		//t.printElapsedTime("Getting getLocalizedString...");
		
		return value;
	}

	private String getLocalizedSystemString(Locale locale, String key) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);
    	return stringManager.getString(key);    		
    	/*
    	try
    	{
        	return stringManager.getString(key);    		
    	}
    	catch (Throwable t) 
    	{
    		System.out.println("There was no string:" + key + " in locale:" + locale);
    		stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", Locale.ENGLISH);
    		return stringManager.getString(key); 
		}
		*/
  	}

	private String getLocalizedSystemString(Locale locale, String key, Object arg1) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key, arg1);
    	/*
    	try
    	{
        	return stringManager.getString(key, arg1);    		
    	}
    	catch (Throwable t) 
    	{
    		System.out.println("There was no string:" + key + " in locale:" + locale);
    		stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", Locale.ENGLISH);
    		return stringManager.getString(key, arg1); 
		}
		*/
  	}

	private String getLocalizedSystemString(Locale locale, String key, Object arg1, Object arg2) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key, arg1, arg2);
    	/*
    	try
    	{
        	return stringManager.getString(key, arg1, arg2);    		
    	}
    	catch (Throwable t) 
    	{
    		System.out.println("There was no string:" + key + " in locale:" + locale);
    		stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", Locale.ENGLISH);
    		return stringManager.getString(key, arg1, arg2); 
		}
		*/
  	}
	
	private String getLocalizedSystemString(Locale locale, String key, Object[] args) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key, args);
    	/*
    	try
    	{
        	return stringManager.getString(key, arg1, arg2);	
    	}
    	catch (Throwable t) 
    	{
    		System.out.println("There was no string:" + key + " in locale:" + locale);
    		stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", Locale.ENGLISH);
    		return stringManager.getString(key, arg1, arg2);
		}
		*/
  	}
	
	public ResourceBundle getResourceBundle(Locale locale)
	{
		ResourceBundle resourceBundle = null;
		
		//String javaVersion = System.getProperty("java.version", "1.5");
		
		File file = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations" + File.separator + "PresentationStrings_" + locale.getLanguage() + ".properties");
		
		if(file.exists())
		{
			try
			{
				//if(javaVersion.equalsIgnoreCase("1.3") || javaVersion.equalsIgnoreCase("1.4") || javaVersion.equalsIgnoreCase("1.5"))
					resourceBundle = new PropertyResourceBundle(new FileInputStream(file));
				//else
					//resourceBundle = new PropertyResourceBundle(new FileReader(file));
			}
			catch (Exception e) 
			{
				logger.error("Could not load custom resource bundle for locale " + locale.getLanguage() + ":" + e.getMessage(), e);
			}
		}
		
		return resourceBundle;
	}
	
	
    public static DigitalAsset create(DigitalAssetVO digitalAssetVO, InputStream is) throws SystemException 
    {
        Database db = CastorDatabaseService.getDatabase();

        DigitalAsset digitalAsset = null;

        beginTransaction(db);
        
        try 
        {
            digitalAsset = new DigitalAssetImpl();
            digitalAsset.setValueObject(digitalAssetVO);
            digitalAsset.setAssetBlob(is);

            db.create(digitalAsset);

            commitTransaction(db);
        } 
        catch (Exception e) 
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return digitalAsset;
    }

    public static List getDigitalAssetByName(String name) throws SystemException 
    {
        Database db = CastorDatabaseService.getDatabase();
        
        List contents = new ArrayList();

        beginTransaction(db);
        try 
        {
        	contents = getDigitalAssetByName(name, db);
            
            commitTransaction(db);
        } 
        catch (Exception e) 
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return contents;
    }

    public static List getDigitalAssetByName(String name, Database db) throws SystemException, Exception
    {
        List contents = new ArrayList();

        OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl c WHERE c.assetContentType = $1 AND c.assetFileName = $2");
        oql.bind("text/infoglue-translation");
        oql.bind(name);

        QueryResults results = oql.execute(Database.ReadOnly);

        while (results.hasMore()) 
        {
            contents.add(results.next());
        }

		results.close();
		oql.close();

		return contents;
    }
    
	public ResourceBundle checkForResourceBundleAsset(Locale locale) throws SystemException
	{
		ResourceBundle resourceBundle = null;
		
		Database db = CastorDatabaseService.getDatabase();

        try
		{
			db.begin();

			File file = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations" + File.separator + "PresentationStrings_" + locale.getLanguage() + ".properties");

			List assets = getDigitalAssetByName(file.getName(), db);
			
			//System.out.println("assets:" + assets);
			
			Iterator assetsIterator = assets.iterator();
			if(assetsIterator.hasNext())
			{
				File translationsDir = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations");
				
				//System.out.println("translationsDir:" + translationsDir);
				
				DigitalAsset da = (DigitalAsset)assetsIterator.next();
				String themeName = da.getAssetFileName();

				//System.out.println("themeName:" + themeName);

				File presentationStringFile = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "translations" + File.separator + da.getAssetFileName());
				logger.info("Caching " + presentationStringFile + " at " + translationsDir);
				InputStream is = da.getAssetBlob();

				FileOutputStream os = new FileOutputStream(presentationStringFile);
	            BufferedOutputStream bos = new BufferedOutputStream(os);
	            int num = copyStream(is, bos);
	            bos.close();
	            os.close();
	            is.close();
	            
	            resourceBundle = getResourceBundle(locale);

	            //System.out.println("Checking resourceBundle: " + resourceBundle);
			}
		}
        catch(Exception e)
        {
        	logger.error("An error occurred when caching theme:" + e.getMessage(), e);
        }
        finally
        {
	        try
			{
				db.commit();
				db.close();
			} 
	        catch (Exception e)
			{
	        	logger.error("Error closing db: " + e.getMessage());
			} 
        }

        return resourceBundle;
	}

	private static int copyStream(InputStream is, OutputStream os) throws IOException 
	{
        int total = 0;
        byte[] buffer = new byte[1024];
        int length = 0;

        while ((length = is.read(buffer)) >= 0) 
        {
            os.write(buffer, 0, length);
            total += length;
        }
        
        os.flush();
        
        return total;
    }

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return null /*new ContentTypeDefinitionVO()*/;
	}

	
	public String getString(String key)
	{
		return getLocalizedString(this.locale, key);
	}

	public String getString(String key, Object[] args)
	{
		return getLocalizedString(this.locale, key, args);
	}

	public String getString(String key, Object arg)
	{
		return getLocalizedString(this.locale, key, arg);
	}

	public String getString(String key, Object arg1, Object arg2)
	{
		return getLocalizedString(this.locale, key, arg1, arg2);
	}

	public String getString(String key, Object arg1, Object arg2, Object arg3)
	{
		return getLocalizedString(this.locale, key, arg1, arg2, arg3);
	}

}

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
import org.infoglue.deliver.portal.deploy.Deploy;
import org.infoglue.deliver.portal.dispatcher.PortalServletDispatcher;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;


/**
 * @author Mattias Bogeblad
 */

public class ThemeController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ThemeController.class.getName());

	/**
	 * Factory method
	 */

	public static ThemeController getController()
	{
		return new ThemeController();
	}

   	/**
   	 * This method deletes a digital asset in the database.
   	 */

   	public static void delete(Integer digitalAssetId) throws ConstraintException, SystemException
   	{
		deleteEntity(DigitalAssetImpl.class, digitalAssetId);
   	}

	public List getAvailableThemes()
	{
		List themes = new ArrayList();
		
		try
		{
			File file = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "css" + File.separator + "skins");
			File[] skins = file.listFiles();
			for(int i=0; i<skins.length; i++)
			{
				File skin = skins[i];
				if(skin.isDirectory())
					themes.add(skin.getName());
			}
		}
		catch (Exception e) 
		{
			logger.error("Could not get themes: " + e.getMessage(), e);
		}
		
		return themes;
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
        oql.bind("zip/infoglue-theme");
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

	public static String verifyThemeExistenceOtherwiseFallback(String theme) throws SystemException
	{
		List themes = ThemeController.getController().getAvailableThemes();
		if(themes.contains(theme))
		{
			Database db = CastorDatabaseService.getDatabase();

	        try
			{
				db.begin();

				List assets = getDigitalAssetByName(theme, db);
				
				Iterator assetsIterator = assets.iterator();
				if(assetsIterator.hasNext())
				{
					File skinsDir = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "css" + File.separator + "skins");
						
					DigitalAsset da = (DigitalAsset)assetsIterator.next();
					String themeName = da.getAssetFileName();
					
					File zip = new File(CmsPropertyHandler.getContextRootPath() + File.separator + "css" + File.separator + "skins" + File.separator + da.getAssetFileName());
					logger.info("Caching " + themeName + " at " + skinsDir);
					InputStream is = da.getAssetBlob();

					FileOutputStream os = new FileOutputStream(zip);
		            BufferedOutputStream bos = new BufferedOutputStream(os);
		            int num = copyStream(is, bos);
		            bos.close();
		            os.close();
		            is.close();
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
			return theme;
		}
		else
		{
			return "outlook2007";
		}
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


}

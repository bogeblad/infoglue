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
/*
 * Created on 2005-apr-05
 *
 */
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.portal.services.PortletEntityRegistryServiceDBImpl;


/**
 * Controller that handles portlets. Simply an extension of DigitalAssetController.
 * 
 * @author jand
 */
public class PortletAssetController extends DigitalAssetController 
{
    private final static Logger logger = Logger.getLogger(PortletAssetController.class.getName());

	/**
	 * Factory method
	 */

	public static PortletAssetController getPortletAssetController()
	{
		return new PortletAssetController();
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

        OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl c WHERE c.assetFileName = $1");
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

	public DigitalAsset getPortletRegistryAsset() throws Exception 
	{
        List das = PortletAssetController.getDigitalAssetByName(PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME);
        if (das != null && das.size() > 0) 
        {
            DigitalAsset da = (DigitalAsset) das.get(0);
            logger.debug("Registry located as id=" + da.getId());
            return da;
        } 
        else 
        {
        	logger.info("Portlet Registry not found");
        }
        
        return null;
    }

	public DigitalAsset getPortletRegistryAsset(Database db) throws Exception 
	{
        List das = PortletAssetController.getDigitalAssetByName(PortletEntityRegistryServiceDBImpl.PORTLET_REGISTRY_CONTENT_NAME, db);
        if (das != null && das.size() > 0) 
        {
            DigitalAsset da = (DigitalAsset) das.get(0);
            logger.debug("Registry located as id=" + da.getId());
            return da;
        } 
        else 
        {
        	logger.info("Portlet Registry not found");
        }
        
        return null;
    }

}
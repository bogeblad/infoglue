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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
* This class handles all interaction against OS_propertyset.
* 
* @author Mattias Bogeblad
*/

public class PropertySetController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(PropertySetController.class.getName());

    private static PropertySetController singelton = null; 

    private static List<String> entriesToRemove = new ArrayList<String>();
    
	public synchronized void run()
	{
		logger.info("Starting Optimized Export....");
		try
		{
			while(true)
			{
				removeQueue();
				Thread.sleep(60000);
			}
		}
		catch (Exception e) 
		{
			logger.error("Error in export thread:" + e.getMessage(), e);
		}
	}
	
	private PropertySetController()
	{
	}
	
	public static PropertySetController getController()
	{
		if(singelton == null)
		{
			singelton = new PropertySetController();
			Thread thread = new Thread(singelton);
			thread.start();
		}
		return singelton;
	}

	public void removeQueue() throws Exception
	{
		List<String> localEntriesToRemove = new ArrayList<String>();
		synchronized (entriesToRemove) 
		{
			localEntriesToRemove.addAll(entriesToRemove);
			entriesToRemove.clear();
		}
		
		Map args = new HashMap();
        args.put("globalKey", "infoglue");
        PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		for(String entryToRemove : localEntriesToRemove)
		{
	        ps.remove( entryToRemove );
	        logger.info("Removing:" + entryToRemove);
		}
	}

	public void addEntryToRemove(String entry)
	{
		synchronized (entriesToRemove) 
		{
			entriesToRemove.add(entry);
		}
	}
   	
    /* (non-Javadoc)
     * @see org.infoglue.cms.controllers.kernel.impl.simple.BaseController#getNewVO()
     */
    public BaseEntityVO getNewVO()
    {
        // TODO Auto-generated method stub
        return null;
    }


}

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

package org.infoglue.cms.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.security.interceptors.CacheEvictionBeanListener;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;

/**
 * This service allows custom logic to tap into the cache notification flow. Can for example be used 
 * for reacting when contents are changed or when a new language comes online etc. 
 */

public class CacheEvictionBeanListenerService
{
	private static final long serialVersionUID = 1L;
    private final static Logger logger = Logger.getLogger(CacheEvictionBeanListenerService.class.getName());
	
	private Map<String,List<CacheEvictionBeanListener>> listeners = new HashMap<String,List<CacheEvictionBeanListener>>();
	
	private static CacheEvictionBeanListenerService service = null;
	
	public CacheEvictionBeanListenerService()
	{
	}
	
	public static CacheEvictionBeanListenerService getService()
	{
		if(service == null)
			service = new CacheEvictionBeanListenerService();
		
		return service;
	}
	
	public void notifyListeners(CacheEvictionBean cacheEvictionBean) 
	{
		try
		{
			logger.info("CLASS:" + cacheEvictionBean.getClassName());
			List<CacheEvictionBeanListener> listeners = getListeners(cacheEvictionBean.getClassName());
			if(listeners != null)
			{
				for(CacheEvictionBeanListener listener : listeners)
				{
					logger.info("listener:" + listener);
					listener.notify(cacheEvictionBean);					
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("Error notifying NotificationMessageListener: " + e.getMessage(), e);
		}
	}
	
	public void registerListener(String className, CacheEvictionBeanListener listener)
	{
		logger.info("Registering:" + className + ":" + listener.getListenerID());
		List<CacheEvictionBeanListener> listeners = getListeners(className); 
		if(this.listeners.containsKey(className))
		{
			unregisterListener(className, listener.getListenerID());
		}
		
		listeners.add(listener);
	}

	public void unregisterListener(String className, String listenerID)
	{
		List<CacheEvictionBeanListener> listeners = getListeners(className);
		if(listeners != null)
		{
			logger.info("listeners:" + listeners.size());
			
			Iterator<CacheEvictionBeanListener> i = listeners.iterator();
			while(i.hasNext())
			{
				CacheEvictionBeanListener cbl = i.next();
				if(cbl.getListenerID().equals(listenerID))
					i.remove();
			}
		}
	}

	public List<CacheEvictionBeanListener> getListeners(String className)
	{
		List<CacheEvictionBeanListener> listeners = this.listeners.get(className);
		if(listeners == null)
		{
			listeners = new ArrayList<CacheEvictionBeanListener>();
			this.listeners.put(className, listeners);
		}
		return listeners;
	}

	public boolean containsListener(String className, String listenerID)
	{
		List<CacheEvictionBeanListener> listeners = getListeners(className);
		if(listeners != null)
		{
			Iterator<CacheEvictionBeanListener> i = listeners.iterator();
			while(i.hasNext())
			{
				CacheEvictionBeanListener cbl = i.next();
				logger.info("cbl:" + cbl.getListenerID());
				if(cbl.getListenerID().equals(listenerID))
					return true;
			}
		}
		
		return false;
	}


}
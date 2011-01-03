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

package org.infoglue.cms.applications.tasktool.actions;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;

import webwork.action.ActionContext;

/**
 * This is the script controller interface. This is the entry point for scripts running tasks.
 */

public class BasicScriptController implements ScriptController
{
    private final static Logger logger = Logger.getLogger(BasicScriptController.class.getName());

	private Database db = null;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;
	private Map outputParameters = new HashMap();
	private final InfoGluePrincipal infoGluePrincipal;
	

	/**
	 * This is the public constructor which automatically locates the database object. 
	 * All scripts are run within it's own transaction for now.
	 */

	public BasicScriptController(InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
	{
		this.db = CastorDatabaseService.getDatabase();
		this.infoGluePrincipal = infoGluePrincipal;
	}
	
	/**
	 * A method to set the request for this script
	 */
	
	public HttpServletRequest getRequest()
	{
		return this.request;
	}

	/**
	 * A method to get the request for this script
	 */
	
	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	/**
	 * A method to set the request for this script
	 */
	
	public HttpServletResponse getResponse()
	{
		return this.response;
	}

	/**
	 * A method to get the request for this script
	 */
	
	public void setResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	
	/**
	 * This is a method to get hold of the SiteNodeController.
	 */
	
	public SiteNodeController getSiteNodeController()
	{
		return new SiteNodeController();
	}
	
	/**
	 * This is a method to get hold of the SiteNodeController.
	 */
	
	public RepositoryController getRepositoryController()
	{
		return new RepositoryController();
	}
	
	/**
	 * This is a method to get hold of the ServiceBindingController.
	 */
	
	public ServiceBindingController getServiceBindingController()
	{
		return new ServiceBindingController();
	}

	/**
	 * This is a method to get hold of the SiteNodeVersionController.
	 */
	
	public SiteNodeVersionController getSiteNodeVersionController()
	{
		return new SiteNodeVersionController();
	}


	/**
	 * This is a method to get hold of the SiteNodeVersionController.
	 */
	
	public ContentController getContentController()
	{
		return new ContentController();
	}

	/**
	 * This is a method to get hold of the SiteNodeVersionController.
	 */
	
	public ContentVersionController getContentVersionController()
	{
		return new ContentVersionController();
	}

	/**
	 * This is a method to get hold of the SiteNodeVersionController.
	 */
	
	public LanguageController getLanguageController()
	{
		return new LanguageController();
	}
	
	/**
	 * Returns the InfoGluePrincipal running the task
	 */

	public InfoGluePrincipal getInfoGluePrincipal()
	{
		return infoGluePrincipal;
	}

	/**
	 * This method returns an Integer from a String.
	 */
	
	public Integer getInteger(String value)
	{
		return new Integer(value);
	}
	
	/**
	 * This method returns true if the object is null - false otherwise.
	 */
	
	public boolean isNull(Object object)
	{
		if(object == null)
			return true;
		else
			return false;
	}	
	
	/**
	 * This method returns the ActionContext for the action
	 */
	
	public ActionContext getActionContext()
	{
		return ActionContext.getContext();
	}
	
	/**
	 * This method instansiate a new object of the given class
	 */
	
	public Object getObjectWithName(String className)
	{
		try 
		{
			Class theClass = null;
			try 
			{
				theClass = Thread.currentThread().getContextClassLoader().loadClass( className );
			}
			catch (ClassNotFoundException e) 
			{
				theClass = getClass().getClassLoader().loadClass( className );
			}
			return theClass.newInstance();
			
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * This method gets the database for the script
	 */
	
	public Database getDatabase()
	{
		return this.db;
	}
	
	/**
	 * Gets a parameter for output
	 */

	public Object getOutputParameter(String parameter)
	{
		return this.outputParameters.get(parameter);
	}

	/**
	 * Sets a parameter for output later
	 */

	public void setOutputParameter(String parameter, Object value)
	{
		this.outputParameters.put(parameter, value);
	}


	/**
	 * Begins a transaction on the named database
	 */
         
	public void beginTransaction() throws SystemException
	{
		try
		{
			this.db.begin();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
		}
	}
       
	/**
	 * Ends a transaction on the named database
	 */
     
	public void commitTransaction() throws SystemException
	{
		try
		{
			this.db.commit();
			this.db.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
		}
	}
 
 
	/**
	 * Rollbacks a transaction on the named database
	 */
     
	public void rollbackTransaction() throws SystemException
	{
		try
		{
			if (this.db.isActive())
			{
				this.db.rollback();
				this.db.close();
			}
		}
		catch(Exception e)
		{
			logger.info("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage());
			//throw new SystemException("An error occurred when we tried to rollback an transaction. Reason:" + e.getMessage(), e);    
		}
	}


	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logInfo(String info)
	{
	    logger.info(info);
	}
	
	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logInfo(String header, String info)
	{
	    logger.info(header + ": " + info);
	}
	
	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logInfo(String header, boolean info)
	{
	    logger.info(header + ": " + info);
	}

	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logInfo(String header, int info)
	{
	    logger.info(header + ": " + info);
	}
	
	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logInfo(String header, Object info)
	{
	    logger.info(header + ": " + info);
	}


	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logWarning(String info)
	{
	    logger.warn(info);
	}
	
	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logWarning(String header, String info)
	{
	    logger.warn(header + ": " + info);
	}
	
	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logWarning(String header, boolean info)
	{
	    logger.warn(header + ": " + info);
	}

	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logWarning(String header, int info)
	{
	    logger.info(header + ": " + info);
	}
	
	/**
	 * This is a logmethod used to debug scripts.
	 */
	
	public void logWarning(String header, Object info)
	{
	    logger.warn(header + ": " + info);
	}

}

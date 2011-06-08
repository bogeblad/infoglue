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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ServerNode;
import org.infoglue.cms.entities.management.ServerNodeVO;
import org.infoglue.cms.entities.management.impl.simple.ServerNodeImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

public class ServerNodeController extends BaseController
{ 
    private final static Logger logger = Logger.getLogger(ServerNodeController.class.getName());

    private String useUpdateSecurity = CmsPropertyHandler.getUseUpdateSecurity();
    
	/**
	 * Factory method
	 */

	public static ServerNodeController getController()
	{
		return new ServerNodeController();
	}
	
	public void initialize()
	{
	}
	
	/**
	 * This method creates a serverNode
	 * 
	 * @param vo
	 * @return
	 * @throws ConstraintException
	 * @throws SystemException
	 */
    public ServerNodeVO create(ServerNodeVO vo) throws ConstraintException, SystemException
    {
        ServerNode ent = new ServerNodeImpl();
        ent.setValueObject(vo);
        ent = (ServerNode) createEntity(ent);
        return ent.getValueObject();
    }     
    
    public ServerNodeVO update(ServerNodeVO vo) throws ConstraintException, SystemException
    {
    	return (ServerNodeVO) updateEntity(ServerNodeImpl.class, (BaseEntityVO) vo);
    }        
        
	// Singe object
    public ServerNode getServerNodeWithId(Integer id, Database db) throws SystemException, Bug
    {
		return (ServerNode) getObjectWithId(ServerNodeImpl.class, id, db);
    }

    public ServerNodeVO getServerNodeVOWithId(Integer serverNodeId) throws ConstraintException, SystemException, Bug
    {
		return  (ServerNodeVO) getVOWithId(ServerNodeImpl.class, serverNodeId);        
    }
	
    
	/**
	 * Returns the ServerNodeVO with the given name.
	 * 
	 * @param name
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */
	
	public ServerNodeVO getServerNodeVOWithName(String name) throws SystemException, Bug
	{
		ServerNodeVO serverNodeVO = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			ServerNode serverNode = getServerNodeWithName(name, db);
			if(serverNode != null)
				serverNodeVO = serverNode.getValueObject();
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return serverNodeVO;	
	}
	
	/**
	 * Returns the ServerNode with the given name fetched within a given transaction.
	 * 
	 * @param name
	 * @param db
	 * @return
	 * @throws SystemException
	 * @throws Bug
	 */

	public ServerNode getServerNodeWithName(String name, Database db) throws SystemException, Bug
	{
		ServerNode serverNode = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.ServerNodeImpl f WHERE f.name = $1");
			oql.bind(name);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode" + name);

			if (results.hasMore()) 
			{
				serverNode = (ServerNode)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch a named serverNode. Reason:" + e.getMessage(), e);    
		}
		
		return serverNode;		
	}

	/**
	 * This method can be used by actions and use-case-controllers that only need to have simple access to the
	 * functionality. They don't get the transaction-safety but probably just wants to show the info.
	 */	
    
    public List getServerNodeVOList() throws SystemException, Bug
    {   
		/*
        String key = "serverNodeVOList";
		logger.info("key:" + key);
		List cachedServerNodeVOList = (List)CacheController.getCachedObject("serverNodeCache", key);
		if(cachedServerNodeVOList != null)
		{
			logger.info("There was an cached authorization:" + cachedServerNodeVOList.size());
			return cachedServerNodeVOList;
		}
		*/
        
		List serverNodeVOList = getAllVOObjects(ServerNodeImpl.class, "serverNodeId");

		//CacheController.cacheObject("serverNodeCache", key, serverNodeVOList);
			
		return serverNodeVOList;
    }
	

    public void delete(ServerNodeVO serverNodeVO, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    {
    	Integer serverNodeId = serverNodeVO.getId();
    	
    	deleteEntity(ServerNodeImpl.class, serverNodeVO.getId());

    	Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    Collection keys = ps.getKeys();
	    Iterator keysIterator = keys.iterator();
	    while(keysIterator.hasNext())
	    {
	    	String key = (String)keysIterator.next();
	    	if(key.indexOf("serverNode_" + serverNodeId + "_") > -1)
	    		ps.remove(key);
	    }
	    
		try 
		{
			CacheController.clearServerNodeProperty(true);
			InfoGlueAuthenticationFilter.initializeCMSProperties();
		} 
		catch (SystemException e) 
		{
			e.printStackTrace();
		}

		NotificationMessage notificationMessage = new NotificationMessage("ViewServerNodePropertiesAction.doSave():", "ServerNodeProperties", infoGluePrincipal.getName(), NotificationMessage.SYSTEM, "0", "ServerNodeProperties");
		RemoteCacheUpdater.getSystemNotificationMessages().add(notificationMessage);
    }
	
	
	public List getAllowedAdminIPList()
	{
	    Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    String allowedAdminIP = ps.getString("allowedAdminIP");
	    if(allowedAdminIP != null)
	        return Arrays.asList(allowedAdminIP.split(","));
	    else
	        return new ArrayList();
	}
	
	public boolean getIsIPAllowed(HttpServletRequest request, String ip)
	{
		boolean allowXForwardedIPCheck = CmsPropertyHandler.getAllowXForwardedIPCheck();
		if(!allowXForwardedIPCheck)
			return false;
		
		boolean isIPAllowed = false;              
		String ipRemote = null;
	    String ipRequest = null;
	    
	    if (request != null) 
	    {
	    	ipRequest = request.getRemoteAddr();
	        ipRemote = request.getHeader("X-Forwarded-For");
	        logger.info("Request: "+ipRequest+", Remote: "+ipRemote+", Ip:"+ip);
	        if (ip.equals(ipRequest))
	        {
	        	isIPAllowed=true;
	        }
	        else if (ipRemote.indexOf(",") > 0)
	        {
	        	String[] ips = ipRemote.split(",");
	            //The first IP is the origin, other IPs added by forward                              
	        	if ((ips.length>0) && (ip.equals(ips[0].trim())))
	        	{
	        		isIPAllowed=true;                       
	            }                                            
	        }
	        else
	        {
	        	if (ip.equals(ipRemote)) 
	        		isIPAllowed=true;
	        }
	    }
	    return isIPAllowed;
	}

	/**
	 * This method return if the caller has access to the semi admin services.
	 * @param request
	 * @return
	 */

	public boolean getIsIPAllowed(HttpServletRequest request)
	{
	    boolean isIPAllowed = false;

	    if(useUpdateSecurity != null && useUpdateSecurity.equals("true"))
	    {
		    String remoteIP = request.getRemoteAddr();
		    if(remoteIP.equals("127.0.0.1"))
		    {
		        isIPAllowed = true;
		    }
		    else
		    {
		        List allowedAdminIPList = ServerNodeController.getController().getAllowedAdminIPList();
		        Iterator i = allowedAdminIPList.iterator();
		        while(i.hasNext())
		        {
		            String allowedIP = (String)i.next();
		            if(!allowedIP.trim().equals(""))
		            {
			            int index = allowedIP.indexOf(".*");
			            if(index > -1)
			                allowedIP = allowedIP.substring(0, index);
				            
			            if(remoteIP.startsWith(allowedIP) || getIsIPAllowed(request,allowedIP))
			            {
			                isIPAllowed = true;
			                break;
			            }
		            }
		        }
		    }
	    }
	    else
	        isIPAllowed = true;
	    
	    return isIPAllowed;
	}
	
	public String getAllowedAdminIP()
	{
	    Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    String allowedAdminIP = ps.getString("allowedAdminIP");
	    return allowedAdminIP;
	}

	public void setAllowedAdminIP(String allowedAdminIP)
	{
	    Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    ps.setString("allowedAdminIP", allowedAdminIP);
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new ServerNodeVO();
	}

}
 

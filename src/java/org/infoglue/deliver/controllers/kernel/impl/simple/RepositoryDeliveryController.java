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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

public class RepositoryDeliveryController extends BaseDeliveryController
{
    private final static Logger logger = Logger.getLogger(RepositoryDeliveryController.class.getName());
    
    private final static RepositoryDeliveryController repositoryDeliveryController = new RepositoryDeliveryController(); 

	/**
	 * Private constructor to enforce factory-use
	 */
	
	private RepositoryDeliveryController()
	{
	}
	
	/**
	 * Factory method
	 */
	
	public static RepositoryDeliveryController getRepositoryDeliveryController()
	{
		return repositoryDeliveryController;
	}
	

	/**
	 * This method returns the master repository.
	 */
	
	public RepositoryVO getMasterRepository(Database db) throws SystemException, Exception
	{
		RepositoryVO repositoryVO = (RepositoryVO)CacheController.getCachedObject("masterRepository", "masterRepository");
		if(repositoryVO != null)
			return repositoryVO;
		
     	OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");
		
    	QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
        {
        	Repository repository = (Repository)results.next();
        	repositoryVO = repository.getValueObject();
        }

		results.close();
		oql.close();

		if(repositoryVO != null)
			CacheController.cacheObject("masterRepository", "masterRepository", repositoryVO);
		
        return repositoryVO;	
	}

	public Set<RepositoryVO> getRepositoryVOListFromServerName(String serverName, String portNumber, String repositoryName, String URI) throws SystemException, Exception
    {
	    Set<RepositoryVO> repositories = new HashSet<RepositoryVO>();
	    
	    Database db = CastorDatabaseService.getDatabase();
	    
	    try
	    {
	    	db.begin();
	    	
	    	repositories = getRepositoryVOListFromServerName(db, serverName, portNumber, repositoryName, URI);
	    	
	    	db.commit();
	    }
	    catch (Exception e) 
	    {
	    	try
	    	{
	    		db.rollback();
	    	}
	    	catch (Exception e2) 
	    	{
	    		e.printStackTrace();
	    	}
	    }
	    finally
	    {
	    	db.close();
	    }
	    
	    return repositories;
    }

	public Set<RepositoryVO> getRepositoryVOListFromServerName(Database db, String serverName, String portNumber, String repositoryName, String url) throws SystemException, Exception
    {
	    Set<RepositoryVO> repositories = new HashSet<RepositoryVO>();
	    
	    String niceURIEncoding = CmsPropertyHandler.getNiceURIEncoding();
        if(niceURIEncoding == null || niceURIEncoding.length() == 0)
            niceURIEncoding = "UTF-8";
        
	    List cachedRepositories = (List)CacheController.getCachedObject("masterRepository", "allDNSRepositories");
		if(cachedRepositories == null)
		{
		    cachedRepositories = new ArrayList();

	        OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r WHERE is_defined(r.dnsName) ORDER BY r.repositoryId");
	        QueryResults results = oql.execute(Database.ReadOnly);
		
	        while (results.hasMore()) 
	        {
	            Repository repository = (Repository) results.next();
	            cachedRepositories.add(repository.getValueObject());
	        }
	        
	        results.close();
			oql.close();

			if(cachedRepositories.size() > 0)
				CacheController.cacheObject("masterRepository", "allDNSRepositories", cachedRepositories);
		}
		
		Iterator repositoriesIterator = cachedRepositories.iterator();
        while (repositoriesIterator.hasNext()) 
        {
            RepositoryVO repositoryVO = (RepositoryVO) repositoriesIterator.next();
            String fullDnsNames = repositoryVO.getDnsName();

            String workingPath = null;
            int workingPathIndex = fullDnsNames.indexOf("workingPath=");
            if(workingPathIndex > -1)
            {
	            workingPath = fullDnsNames.substring(workingPathIndex + 12);
	            int workingPathEndIndex = workingPath.indexOf(",");
	            if(workingPathEndIndex > -1)
	            	workingPath = workingPath.substring(0, workingPathEndIndex);
            }
            String livePath = null;
            int livePathIndex = fullDnsNames.indexOf("path=");
            if(livePathIndex > -1)
            {
            	livePath = fullDnsNames.substring(livePathIndex + 5);
	            int livePathEndIndex = livePath.indexOf(",");
	            if(livePathEndIndex > -1)
	            	livePath = livePath.substring(0, livePathEndIndex);
            }

            if(CmsPropertyHandler.getOperatingMode().equals("0"))
            {
	            String workingPathAlternative1 = workingPath;
	            if(workingPathAlternative1 != null)
	            	workingPathAlternative1 = URLEncoder.encode(workingPathAlternative1, niceURIEncoding);
	            String workingPathAlternative2 = workingPath;
	            if(workingPathAlternative2 != null)
	            	workingPathAlternative2 = URLEncoder.encode(workingPathAlternative2, (niceURIEncoding.indexOf("8859") > -1 ? "utf-8" : "iso-8859-1")).replaceAll("\\+", "%20");
 
	            if(workingPath != null && url.indexOf(workingPath) == -1 && url.indexOf(workingPathAlternative1) == -1 && url.indexOf(workingPathAlternative2) == -1)
	            {
	            	//System.out.println("This repo had a working path but the url did not include any sign of it - let's skip it");
	            	continue;
	            }
	        }
            else if(CmsPropertyHandler.getOperatingMode().equals("3"))
            {
	            if(livePath != null)
	            	livePath = URLEncoder.encode(livePath, niceURIEncoding);

	            String livePathAlternative1 = livePath;
	            if(livePathAlternative1 != null)
	            	livePathAlternative1 = URLEncoder.encode(livePathAlternative1, niceURIEncoding);
	            String livePathAlternative2 = livePath;
	            if(livePathAlternative2 != null)
	            	livePathAlternative2 = URLEncoder.encode(livePathAlternative2, (niceURIEncoding.indexOf("8859") > -1 ? "utf-8" : "iso-8859-1")).replaceAll("\\+", "%20");

	            if(livePath != null && url.indexOf(livePath) == -1 && url.indexOf(livePathAlternative1) == -1 && url.indexOf(livePathAlternative2) == -1)
	            {
	            	//System.out.println("This repo had a live path but the url did not include any sign of it - let's skip it");
	            	continue;
	            }
	        }
            
            String[] dnsNames = splitStrings(fullDnsNames.replaceAll("\\[.*?\\]", ""));

            if(logger.isInfoEnabled())
            	logger.info("dnsNames:" + dnsNames);
            
            for (int i=0;i<dnsNames.length;i++) 
            {
            	if(logger.isInfoEnabled())
            		logger.info("dnsNames["+i+"]:" + dnsNames[i]);
                String dnsName = dnsNames[i];
                
                if(dnsName.indexOf("undefined") > -1)
                	continue;
                
                int index = dnsName.indexOf("working=,");
                int indexMode = dnsName.indexOf("working=");
                if(CmsPropertyHandler.getOperatingMode().equals("2"))
                {    
                	index = dnsName.indexOf("preview=,");
                	indexMode = dnsName.indexOf("preview=");
                }
                else if(CmsPropertyHandler.getOperatingMode().equals("3"))
                {
                	index = dnsName.indexOf("live=,");
                	indexMode = dnsName.indexOf("live=");
                }

                boolean noHostName = (indexMode == -1);
                
                if(logger.isInfoEnabled())
                	logger.info("" + index + ":" + indexMode + ":" + noHostName + ":" + dnsName + " for operationMode:" + CmsPropertyHandler.getOperatingMode());
            	if(/*!noHostName && */index == -1 && indexMode == -1 && dnsName.indexOf("=") > -1)
            	{
        			if(logger.isInfoEnabled())
            			logger.info("Skipping this name [" + dnsName + "] as it was not a dnsName targeted toward this mode.");
            		continue;
            	}
            	
            	int protocolIndex = dnsName.indexOf("://");
                if(protocolIndex > -1)
                    dnsName = dnsName.substring(protocolIndex + 3);

            	int portIndex = dnsName.indexOf(":");
                if(portIndex > -1)
                    dnsName = dnsName.substring(0, portIndex);

                if(logger.isInfoEnabled())
                	logger.info("Matching only server name - removed protocol if there:" + dnsName);
                if(logger.isInfoEnabled())
                	logger.info("dnsName:" + dnsName + ", serverName:" + serverName + ", repositoryName:" + repositoryName);
                
                if(logger.isInfoEnabled())
                {
	                logger.info("dnsName.indexOf(':'):" + dnsName.indexOf(":"));
	                logger.info("dnsName.indexOf(serverName) == 0:" + dnsName.indexOf(serverName));
	                logger.info("dnsName.indexOf(serverName + ':' + portNumber):" + dnsName.indexOf(serverName + ":" + portNumber));
	                logger.info("index:" + index);
	                logger.info("indexMode:" + indexMode);
                }
                
            	if((dnsName.indexOf(":") == -1 && dnsName.indexOf(serverName) == 0) || dnsName.indexOf(serverName + ":" + portNumber) == 0 || index > -1 || indexMode == -1)
                {
            	    if(repositoryName != null && repositoryName.length() > 0)
            	    {
            	    	if(logger.isInfoEnabled())
                        	logger.info("Has to check repositoryName also:" + repositoryName);
                        if(repositoryVO.getName().equalsIgnoreCase(repositoryName))
                        {
                        	if((dnsName.indexOf(":") == -1 && dnsName.indexOf(serverName) == 0) || dnsName.indexOf(serverName + ":" + portNumber) == 0)
                        	{
	                        	//System.out.println("Adding " + repositoryVO.getName() + ":" + dnsName);
	                        	if(logger.isInfoEnabled())
	                            	logger.info("Adding " + repositoryVO.getName());
	           
	            	            repositories.add(repositoryVO);
                        	}
                        }
                    }
            	    else
            	    {
                    	//System.out.println("Adding " + repositoryVO.getName() + ":" + dnsName);
            	    	if(logger.isInfoEnabled())
                        	logger.info("Adding " + repositoryVO.getName());
            	        repositories.add(repositoryVO);
            	    }
            	}
            }
        }
        
        return repositories;
    }

	public List getRepositoryVOListFromServerName(Database db, String serverName, String portNumber, String repositoryName) throws SystemException, Exception
    {
	    List repositories = new ArrayList();
	    
        OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r WHERE is_defined(r.dnsName)");
        QueryResults results = oql.execute(Database.ReadOnly);
        while (results.hasMore()) 
        {
            Repository repository = (Repository) results.next();
            logger.info("repository:" + repository.getDnsName());
            String[] dnsNames = splitStrings(repository.getDnsName());
            logger.info("dnsNames:" + dnsNames);
            for (int i=0;i<dnsNames.length;i++) 
            {
            	logger.info("dnsNames[i]:" + dnsNames[i]);
                String dnsName = dnsNames[i];
            	int protocolIndex = dnsName.indexOf("://");
                if(protocolIndex > -1)
                    dnsName = dnsName.substring(protocolIndex + 3);
                
                logger.info("Matching only server name - removed protocol if there:" + dnsName);
                
            	if((dnsName.indexOf(":") == -1 && dnsName.indexOf(serverName) == 0) || dnsName.indexOf(serverName + ":" + portNumber) == 0)
                {
            	    if(repositoryName != null && repositoryName.length() > 0)
            	    {
            	        logger.info("Has to check repositoryName also:" + repositoryName);
                        if(repository.getValueObject().getName().equalsIgnoreCase(repositoryName))
            	            repositories.add(repository.getValueObject());
            	    }
            	    else
            	    {
            	        repositories.add(repository.getValueObject());
            	    }
            	}
            }
        }
        
        return repositories;
    }

	
    private String[] splitStrings(String str)
    {
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens()) 
        {
            String token = st.nextToken().trim();
            list.add(token);
        }
        
        return (String[]) list.toArray(new String[0]);
    } 
	
	/**
	 * This method returns all the repositories.
	 */
	
	public List getRepositoryVOList(Database db) throws SystemException, Exception
	{
		List repositoryVOList = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");
		
		QueryResults results = oql.execute(Database.ReadOnly);
		
		if (results.hasMore()) 
		{
			Repository repository = (Repository)results.next();
			RepositoryVO repositoryVO = repository.getValueObject();
			repositoryVOList.add(repositoryVO);
		}

		results.close();
		oql.close();

		return repositoryVOList;	
	}

	public RepositoryVO getRepositoryVOWithName(String name, Database db)
	{
		RepositoryVO repository = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl f WHERE f.name = $1");
			oql.bind(name);
			
			QueryResults results = oql.execute(Database.ReadOnly);

			if (results.hasMore()) 
			{
				repository = ((Repository)results.next()).getValueObject();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to fetch a named repository. Reason:" + e.getMessage(), e);
		}
		
		return repository;		
	}

	/**
	 * This method fetches a property for a repository.
	 */
	
	public String getPropertyValue(Integer repositoryId, String propertyName) 
	{
		String key = "parentRepository_" + repositoryId + "_" + propertyName;
		if(logger.isInfoEnabled())
        	logger.info("key:" + key);
	    Object object = CacheController.getCachedObject("parentRepository", key);
		
	    if(object instanceof NullObject)
		{
	    	if(logger.isInfoEnabled())
            	logger.info("There was an cached property but it was null:" + object);
			return null;
		}
		else if(object != null)
		{
			if(logger.isInfoEnabled())
            	logger.info("There was an cached property:" + object);
			return (String)object;
		}
		
		String propertyValue = null;
		
        Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
	    
	    propertyValue = ps.getString("repository_" + repositoryId + "_" + propertyName);
	    if(logger.isInfoEnabled())
        	logger.info("propertyValue:" + propertyValue);
	    
	    if(propertyValue != null)
	        CacheController.cacheObject("parentRepository", key, propertyValue);
	    else
	        CacheController.cacheObject("parentRepository", key, new NullObject());
	        
		return propertyValue;
	}

	/**
	 * This method fetches a property for a repository.
	 */
	
	public String getExtraPropertyValue(Integer repositoryId, String propertyName)
	{
		String key = "repository_" + repositoryId + "_" + propertyName;
	    logger.info("key:" + key);
	    Object object = CacheController.getCachedObject("parentRepository", key);
		
	    if(object instanceof NullObject)
		{
			logger.info("There was an cached property but it was null:" + object);
			return null;
		}
		else if(object != null)
		{
			logger.info("There was an cached property:" + object);
			return (String)object;
		}
		
		String propertyValue = null;
		
		try
	    {
			Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
		    
		    byte[] extraPropertiesBytes = ps.getData("repository_" + repositoryId + "_extraProperties");
		    if(extraPropertiesBytes != null)
		    {
		    	String extraProperties = new String(extraPropertiesBytes, "UTF-8");
			    
		    	Properties properties = new Properties();
				ByteArrayInputStream is = new ByteArrayInputStream(extraProperties.getBytes("UTF-8"));
				properties.load(is);
			    
				propertyValue = properties.getProperty(propertyName);
			}
	    
		    if(logger.isInfoEnabled())
            	logger.info("propertyValue:" + propertyValue);
		    if(propertyValue != null)
		        CacheController.cacheObject("parentRepository", key, propertyValue);
		    else
		        CacheController.cacheObject("parentRepository", key, new NullObject());
	    }
		catch(Exception e)
	    {
			e.printStackTrace();
	        logger.error("Could not fetch extra property: " + e.getMessage(), e);
	    }
		
		return propertyValue;
	}

}
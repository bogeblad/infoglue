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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.AvailableServiceBinding;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.services.BaseService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.filters.FilterConstants;
import org.infoglue.deliver.applications.filters.URIMapperCache;
import org.infoglue.deliver.applications.filters.ViewPageFilter;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.HttpHelper;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.Timer;


public class NodeDeliveryController extends BaseDeliveryController
{
    private final static Logger logger = Logger.getLogger(NodeDeliveryController.class.getName());

	private URLComposer urlComposer = null; 

    protected static final String META_INFO_BINDING_NAME 				= "Meta information";
    protected static final String NAV_TITLE_ATTRIBUTE_NAME 		 		= "NavigationTitle";

	private static final boolean USE_INHERITANCE = true;
	private static final boolean DO_NOT_USE_INHERITANCE = false;	
	
	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	private Integer siteNodeId = null;
	private Integer languageId = null;
	private Integer contentId 	= null;
	
	protected DeliveryContext deliveryContext = null;
	
	public void setDeliveryContext(DeliveryContext deliveryContext)
	{
		this.deliveryContext = deliveryContext;
	}
	
	/**
	 * Private constructor to enforce factory-use
	 */
	
	private NodeDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		this.siteNodeId = siteNodeId;
		this.languageId = languageId;
		this.contentId  = contentId;
		this.urlComposer = URLComposer.getURLComposer(); 
		this.deliveryContext = deliveryContext;
	}
	
	/**
	 * Factory method
	 */
	public static NodeDeliveryController getNodeDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		return new NodeDeliveryController(siteNodeId, languageId, contentId, null);
	}

	public static NodeDeliveryController getNodeDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		return new NodeDeliveryController(siteNodeId, languageId, contentId, deliveryContext);
	}

	/**
	 * Factory method
	 */
	
	public static NodeDeliveryController getNodeDeliveryController(DeliveryContext deliveryContext) throws SystemException, Exception
	{
		return new NodeDeliveryController(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), deliveryContext.getContentId(), deliveryContext);
	}

	/**
	 * This method returns which mode the delivery-engine is running in.
	 * The mode is important to be able to show working, preview and published data separate.
	 */
	
	private Integer getOperatingMode()
	{
		Integer operatingMode = new Integer(0); //Default is working
		try
		{
			operatingMode = new Integer(CmsPropertyHandler.getOperatingMode());
			//logger.info("Operating mode is:" + operatingMode);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the operating mode from the propertyFile:" + e.getMessage(), e);
		}
		return operatingMode;
	}
	
	/**
	 * This method gets the appropriate siteNodeVersion
	 */

	public SiteNodeVersionVO getActiveSiteNodeVersionVO(Database db, Integer siteNodeId) throws Exception
	{
		SiteNodeVersionVO siteNodeVersionVO = null;
		
		SiteNodeVersion siteNodeVersion = getActiveSiteNodeVersion(siteNodeId, db);
		if(siteNodeVersion != null)
			siteNodeVersionVO = siteNodeVersion.getValueObject();
		
		return siteNodeVersionVO;
	}
	
	/**
	 * This method gets the appropriate siteNodeVersion
	 */
	
	public SiteNodeVersion getActiveSiteNodeVersion(Integer siteNodeId, Database db) throws Exception
	{
		SiteNodeVersion siteNodeVersion = null;
		
		SiteNode siteNode = (SiteNode)this.getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		logger.info("Loaded siteNode " + siteNode.getName());
		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
		logger.info("Loaded versions " + siteNodeVersions.size());
		
		Iterator versionIterator = siteNodeVersions.iterator();
		while(versionIterator.hasNext())
		{
			SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
			logger.info("SiteNodeVersionCandidate " + siteNodeVersionCandidate.getId());
			if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() >= getOperatingMode().intValue())
			{
				if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNodeId.intValue())
				{
					if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
					{
						siteNodeVersion = siteNodeVersionCandidate;
					}
				}
			}
		}
		
		return siteNodeVersion;
	}
	
	/**
	 * This method checks if there is a serviceBinding with the name on this or any parent node.
	 */
	
	public ServiceDefinitionVO getInheritedServiceDefinition(List qualifyerList, Integer siteNodeId, AvailableServiceBindingVO availableServiceBindingVO, Database db, boolean inheritParentBindings) throws SystemException, Exception
	{
		ServiceDefinitionVO serviceDefinitionVO = null;
		
		if(siteNodeId == null || siteNodeId.intValue() <= 0)
			return serviceDefinitionVO;

		logger.info("Trying to find binding " + availableServiceBindingVO + " on siteNodeId:" + siteNodeId);

		SiteNode siteNode = (SiteNode)this.getObjectWithId(SmallSiteNodeImpl.class, siteNodeId, db);
		logger.info("Loaded siteNode " + siteNode.getName());

		serviceDefinitionVO = getServiceDefinitionVO(qualifyerList, siteNode, availableServiceBindingVO, db);
		logger.info("Loaded serviceDefinitionVO " + serviceDefinitionVO);
		
		if(serviceDefinitionVO == null)
		{
			//We check if the available service definition state that this is a inheritable binding            	
			//AvailableServiceBinding availableServiceBinding = getAvailableServiceBindingRecursive(siteNodeVersion.getOwningSiteNode(), availableServiceBindingName, inheritParentBindings);
			if(availableServiceBindingVO != null && availableServiceBindingVO.getIsInheritable().booleanValue() && inheritParentBindings)
        	{
            	logger.info("No binding found - lets try the parent.");
            	SiteNode parent = siteNode.getParentSiteNode();
            	if(parent != null)
            	    serviceDefinitionVO = getInheritedServiceDefinition(qualifyerList, parent.getSiteNodeId(), availableServiceBindingVO, db, inheritParentBindings);
        	}
		}
		
		logger.info("Loaded serviceDefinitionVO end...");
		
		return serviceDefinitionVO;
	}
	
	private ServiceDefinitionVO getServiceDefinitionVO(List qualifyerList, SiteNode siteNode, AvailableServiceBindingVO availableServiceBindingVO, Database db) throws Exception
	{
	    ServiceDefinitionVO serviceDefinitionVO = null;
	    //ServiceBinding serviceBinding = null;
		
	    String key = "" + siteNode.getSiteNodeId() + "_" + availableServiceBindingVO.getId();
		logger.info("key:" + key);
		Object object = CacheController.getCachedObject("serviceDefinitionCache", key);
		Object object2 = CacheController.getCachedObject("qualifyerListCache", key);
		if(object != null && object2 != null)
		{
			logger.info("There was an cached ServiceDefinitionVO:" + object);
			if(object instanceof ServiceDefinitionVO)
			    serviceDefinitionVO = (ServiceDefinitionVO)object;
			if(object2 instanceof List)
			    qualifyerList.addAll((List)object2);
		}
		else
		{
		    OQLQuery oql = db.getOQLQuery( "SELECT sb FROM org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl sb WHERE sb.siteNodeVersion.owningSiteNode = $1 AND sb.availableServiceBinding = $2 AND sb.siteNodeVersion.isActive = $3 AND sb.siteNodeVersion.stateId >= $4 order by sb.siteNodeVersion.siteNodeVersionId DESC");
			oql.bind(siteNode);
			oql.bind(availableServiceBindingVO.getId());
			oql.bind(new Boolean(true));
			oql.bind(getOperatingMode());
			
	    	QueryResults results = oql.execute(Database.READONLY);

			if (results.hasMore()) 
	        {
				ServiceBinding serviceBinding = (ServiceBinding)results.next();
				SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNode.getId());
				logger.info("serviceBinding sitenodeVersion:" + serviceBinding.getSiteNodeVersion().getId() + ":" + latestSiteNodeVersionVO.getId());
				if(serviceBinding.getSiteNodeVersion().getId().equals(latestSiteNodeVersionVO.getId()))
				{
					serviceDefinitionVO = serviceBinding.getServiceDefinition().getValueObject();
			        Collection qualifyers = serviceBinding.getBindingQualifyers();
					
					qualifyers = sortQualifyers(qualifyers);
					
					Iterator iterator = qualifyers.iterator();
					while(iterator.hasNext())
					{
						Qualifyer qualifyer = (Qualifyer)iterator.next();
						HashMap argument = new HashMap();
						argument.put(qualifyer.getName(), qualifyer.getValue());
						qualifyerList.add(argument);
					}
			        
			        object = serviceDefinitionVO;
			        object2 = qualifyerList;
				}
	        }
			else
			{
			    object = new NullObject();
		    	object2 = new NullObject();
			}
			
			
			results.close();
			oql.close();

			CacheController.cacheObject("serviceDefinitionCache", key, object);
			CacheController.cacheObject("qualifyerListCache", key, object2);

		}
			
			
		/*
	    Collection serviceBindings = siteNodeVersion.getServiceBindings();
		Iterator serviceBindingIterator = serviceBindings.iterator();
		while(serviceBindingIterator.hasNext())
		{
			ServiceBinding serviceBindingCandidate = (ServiceBinding)serviceBindingIterator.next();
			//logger.warn("siteNodeVersion " + siteNodeVersion.getId());
			if(serviceBindingCandidate.getAvailableServiceBinding().getAvailableServiceBindingId().intValue() == availableServiceBindingVO.getId().intValue())
			//if(serviceBindingCandidate.getValueObject().getAvailableServiceBindingId().intValue() == availableServiceBindingVO.getId().intValue())
			{
				serviceBinding = serviceBindingCandidate;
			}
		}
		*/
		
		return serviceDefinitionVO;
	}
	
	/**
	 * This method fetches an available service binding as long as there is one associated with this site nodes type definition or any
	 * of the parent site node type definitions.
	 */
	
	private AvailableServiceBinding getAvailableServiceBindingRecursive(SiteNode siteNode, String availableServiceBindingName, boolean inheritParentBindings)
	{
		if(siteNode == null || availableServiceBindingName == null)
			return null;
		
		AvailableServiceBinding availableServiceBinding = null;
		
		SiteNodeTypeDefinition siteNodeTypeDefinition = siteNode.getSiteNodeTypeDefinition();
		if(siteNodeTypeDefinition != null)
		{
			Collection availableServiceBindings = siteNodeTypeDefinition.getAvailableServiceBindings();
			
			Iterator availableServiceBindingsIterator = availableServiceBindings.iterator();
			while(availableServiceBindingsIterator.hasNext())
			{
				AvailableServiceBinding currentAvailableServiceBinding = (AvailableServiceBinding)availableServiceBindingsIterator.next();
				if(currentAvailableServiceBinding.getName().equalsIgnoreCase(availableServiceBindingName))
				{
					availableServiceBinding = currentAvailableServiceBinding;
				}
			}
		}
				
		if(availableServiceBinding == null)
			availableServiceBinding = getAvailableServiceBindingRecursive(siteNode.getParentSiteNode(), availableServiceBindingName, inheritParentBindings);
			
		return availableServiceBinding;
	}	
	
	/**
	 * This method returns the SiteNodeVO that is sent in.
	 */
	/*
	public SiteNode getSiteNode(Database db, Integer siteNodeId) throws SystemException
	{
		if(siteNodeId == null || siteNodeId.intValue() < 1)
			return null;
		
		SiteNode siteNode = null;
		
		siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		
		if(siteNode != null && deliveryContext != null)
			deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNode.getId()));

		return siteNode;
	}
	*/
	/**
	 * This method returns the SiteNodeVO that is sent in.
	 */
	
	public SiteNodeVO getSiteNodeVO(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		if(siteNodeId == null || siteNodeId.intValue() < 1)
			return null;

		if(deliveryContext != null)
			deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
		
		if(siteNodeVO.getIsDeleted())
			return null;
		
		return siteNodeVO;
	}

	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	
	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		String key = "" + siteNodeId;
		logger.info("key:" + key);
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObjectFromAdvancedCache("latestSiteNodeVersionCache", key);
		if(siteNodeVersionVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
			siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(siteNodeId, db);
			/*
			SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(siteNodeId, db);
			if(siteNodeVersion != null)
				siteNodeVersionVO = siteNodeVersion.getValueObject();
			*/
			
			if(siteNodeVersionVO != null)
			{
	        	String groupKey1 = CacheController.getPooledString(4, siteNodeVersionVO.getId());
	        	String groupKey2 = CacheController.getPooledString(3, siteNodeId);
	
	        	CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", key, siteNodeVersionVO, new String[]{groupKey1, groupKey2}, true);
			}
		}
				
		return siteNodeVersionVO;
	}

	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode and stores it in a cache special to the deliver page cache.
	 */
	
	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVOForPageCache(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		String key = "" + siteNodeId;
		logger.info("key:" + key);
		SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObject("pageCacheLatestSiteNodeVersions", key);
		if(siteNodeVersionVO != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
			siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(siteNodeId, db);
			/*
			SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(siteNodeId, db);
			if(siteNodeVersion != null)
				siteNodeVersionVO = siteNodeVersion.getValueObject();
			*/
			
			CacheController.cacheObject("pageCacheLatestSiteNodeVersions", key, siteNodeVersionVO);
		}
				
		return siteNodeVersionVO;
	}


	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	/*
	public SiteNodeVersion getLatestActiveSiteNodeVersion(Integer siteNodeId, Database db) throws SystemException, Exception
	{
	    SiteNodeVersion siteNodeVersion = null;
		
		logger.info("Loading siteNode " + siteNodeId);
		SiteNode siteNode = (SiteNode)this.getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
		logger.info("siteNode " + siteNode.getName());
		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
		logger.info("siteNodeVersions " + siteNodeVersions);
		
		Iterator versionIterator = siteNodeVersions.iterator();
		while(versionIterator.hasNext())
		{
			SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
			logger.info("SiteNodeVersionCandidate " + siteNodeVersionCandidate.getId());
			if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() >= getOperatingMode().intValue())
			{
				if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNodeId.intValue())
				{
					if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
					{
						siteNodeVersion = siteNodeVersionCandidate;
					}
				}
			}
		}
	    
		return siteNodeVersion;
	}
	*/
	
	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	
	public SiteNodeVersion getLatestActiveSiteNodeVersion(Integer siteNodeId, Database db) throws SystemException, Exception
	{
		SiteNodeVersion siteNodeVersion = null;
		
	    String versionKey = "" + siteNodeId + "_" + getOperatingMode() + "_siteNodeVersionId";		
	    
		//Integer siteNodeVersionId = (Integer)CacheController.getCachedObject("latestSiteNodeVersionCache", versionKey);
		Integer siteNodeVersionId = (Integer)CacheController.getCachedObjectFromAdvancedCache("latestSiteNodeVersionCache", versionKey);
		if(siteNodeVersionId != null)
		{
		    logger.info("There was a cached sitenode version id:" + siteNodeVersionId);
		    siteNodeVersion = (SiteNodeVersion)getObjectWithId(SiteNodeVersionImpl.class, siteNodeVersionId, db);
		}
		else
		{
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.stateId >= $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
			oql.bind(getOperatingMode());
			oql.bind(true);
			
			QueryResults results = oql.execute(Database.READONLY);
			
			if (results.hasMore()) 
		    {
		    	siteNodeVersion = (SiteNodeVersion)results.next();
			    //CacheController.cacheObject("latestSiteNodeVersionCache", versionKey, siteNodeVersion.getId());
	        	String groupKey1 = CacheController.getPooledString(4, siteNodeVersion.getId());
	        	String groupKey2 = CacheController.getPooledString(3, siteNodeId);

	        	CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", versionKey, siteNodeVersion.getId(), new String[]{groupKey1, groupKey2}, true);
	        }	
		
			results.close();
			oql.close();
		}
		
		//if(contentVersion != null)
		//    deliveryContext.addUsedContentVersion(CacheController.getPooledString(2, contentVersion.getId()));
	
		return siteNodeVersion;
	}
	
	/**
	 * This method returns the latest sitenodeVersion there is for the given siteNode.
	 */
	
	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Integer siteNodeId, Database db) throws SystemException, Exception
	{
	    String versionKey = "" + siteNodeId + "_" + getOperatingMode() + "_siteNodeVersionVO";		
	    
	    //SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObject("latestSiteNodeVersionCache", versionKey);
	    SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)CacheController.getCachedObjectFromAdvancedCache("latestSiteNodeVersionCache", versionKey);
		if(siteNodeVersionVO != null)
	    {
		    if(logger.isInfoEnabled())
		    	logger.info("There was a cached siteNodeVersionVO:" + siteNodeVersionVO);
		}
		else
		{
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeVersionImpl cv WHERE cv.siteNodeId = $1 AND cv.stateId >= $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
			oql.bind(siteNodeId);
			oql.bind(getOperatingMode());
			oql.bind(true);
			
			QueryResults results = oql.execute(Database.READONLY);
			
			if (results.hasMore()) 
		    {
		    	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
		    	siteNodeVersionVO = siteNodeVersion.getValueObject();
		    	
			    //CacheController.cacheObject("latestSiteNodeVersionCache", versionKey, siteNodeVersionVO);
	        	String groupKey1 = CacheController.getPooledString(4, siteNodeVersion.getId());
	        	String groupKey2 = CacheController.getPooledString(3, siteNodeId);

	        	CacheController.cacheObjectInAdvancedCache("latestSiteNodeVersionCache", versionKey, siteNodeVersionVO, new String[]{groupKey1, groupKey2}, true);
	        }
		
			results.close();
			oql.close();
		}
		
		//if(contentVersion != null)
		//    deliveryContext.addUsedContentVersion(CacheController.getPooledString(2, contentVersion.getId()));
	
		return siteNodeVersionVO;
	}

	/**
	 * This method returns the SiteNodeVO that is the parent to the one sent in.
	 * @throws Exception 
	 */
	
	public SiteNodeVO getParentSiteNode(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		String key = "" + siteNodeId;
		
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));
		
		Object object = CacheController.getCachedObject("parentSiteNodeCache", key);
		SiteNodeVO parentSiteNodeVO = null;

		if(object instanceof NullObject)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached parentSiteNodeVO but it was null:" + object);
		}
		else if(object != null)
		{
			parentSiteNodeVO = (SiteNodeVO)object;
		}
		else
		{
			//SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
		
			SiteNodeVO siteNode = getSiteNodeVO(db, siteNodeId);
			//SiteNode parentSiteNode = siteNode.getParentSiteNode();
            if(siteNode != null  && siteNode.getParentSiteNodeId() != null)		
            {
                parentSiteNodeVO = getSiteNodeVO(db, siteNode.getParentSiteNodeId());
            	CacheController.cacheObject("parentSiteNodeCache", key, parentSiteNodeVO);
    		}
            else
            {
                CacheController.cacheObject("parentSiteNodeCache", key, new NullObject());
            }
		}
		
		return parentSiteNodeVO;
	}

	/**
	 * This method returns the SiteNodeVO that is the parent to the one sent in.
	 */
	
	public SiteNodeVO getParentSiteNodeForPageCache(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		String key = "" + siteNodeId;
		logger.info("key getParentSiteNode:" + key);
		Object object = CacheController.getCachedObject("pageCacheParentSiteNodeCache", key);
		SiteNodeVO parentSiteNodeVO = null;
		logger.info("object:" + object);
		if(object instanceof NullObject)
		{
			logger.info("There was an cached parentSiteNodeVO but it was null:" + object);
		}
		else if(object != null)
		{
			parentSiteNodeVO = (SiteNodeVO)object;
		}
		else
		{
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId, db);
			//SiteNode siteNode = (SiteNode)getObjectWithId(SmallSiteNodeImpl.class, siteNodeId, db);
            //SiteNode parentSiteNode = siteNode.getParentSiteNode();
            if(siteNodeVO.getParentSiteNodeId() != null)		
            {
            	parentSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVO.getParentSiteNodeId(), db);
            	CacheController.cacheObject("pageCacheParentSiteNodeCache", key, parentSiteNodeVO);
            }
            else
            {
                CacheController.cacheObject("pageCacheParentSiteNodeCache", key, new NullObject());
            }
		}
		
		return parentSiteNodeVO;
	}


	/**
	 * This method returns true if the if the page in question (ie sitenode) has page-caching disabled.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public boolean getIsPageCacheDisabled(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		boolean isPageCacheDisabled = false;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getDisablePageCache() != null)
			{	
				if(latestSiteNodeVersionVO.getDisablePageCache().intValue() == NO.intValue())
					isPageCacheDisabled = false;
				else if(latestSiteNodeVersionVO.getDisablePageCache().intValue() == YES.intValue())
					isPageCacheDisabled = true;
				else if(latestSiteNodeVersionVO.getDisablePageCache().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						isPageCacheDisabled = getIsPageCacheDisabled(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return isPageCacheDisabled;
	}

	/**
	 * This method returns the pageCacheKey for the page.
	 */
	
	public String getPageCacheKey(Database db, HttpSession session, HttpServletRequest request, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, String extra)
	{
		return getPageCacheKey(db, session, request, siteNodeId, languageId, contentId, userAgent, queryString, extra, true);
	}
	
	/**
	 * This method returns the pageCacheKey for the page.
	 */
	
	public String getPageCacheKey(Database db, HttpSession session, HttpServletRequest request, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, String extra, boolean includeOriginalRequestURL)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

	    String pageKey = CacheController.getPageCacheKey(session, request, siteNodeId, languageId, contentId, userAgent, queryString, extra);
	    try
		{
			
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			Integer currentSiteNodeId = siteNodeId;
			
			while(latestSiteNodeVersionVO == null || latestSiteNodeVersionVO.getPageCacheKey() == null || latestSiteNodeVersionVO.getPageCacheKey().length() == 0 || latestSiteNodeVersionVO.getPageCacheKey().equalsIgnoreCase("default"))
			{
				if(currentSiteNodeId == null)
					break;
				
				SiteNodeVO parentSiteNodeVO = getParentSiteNode(db, currentSiteNodeId);
				if(parentSiteNodeVO != null)
				{
					latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVOForPageCache(db, parentSiteNodeVO.getId());
					if(latestSiteNodeVersionVO != null)
						currentSiteNodeId = latestSiteNodeVersionVO.getSiteNodeId();
					else
						currentSiteNodeId = null;	
				}
				else
					break;
			}
			
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getPageCacheKey() != null && latestSiteNodeVersionVO.getPageCacheKey().length() > 0 && !latestSiteNodeVersionVO.getPageCacheKey().equalsIgnoreCase("default"))
			{
		    	String originalRequestURL = request.getParameter("originalRequestURL");
		    	if(originalRequestURL == null || originalRequestURL.length() == 0)
		    		originalRequestURL = request.getRequestURL().toString();

			    pageKey = originalRequestURL + "_" + latestSiteNodeVersionVO.getPageCacheKey();
			    pageKey = pageKey.replaceAll("\\$siteNodeId", "" + siteNodeId);
			    pageKey = pageKey.replaceAll("\\$languageId", "" + languageId);
			    pageKey = pageKey.replaceAll("\\$contentId", "" + contentId);
			    pageKey = pageKey.replaceAll("\\$useragent", "" + userAgent);
			    pageKey = pageKey.replaceAll("\\$queryString", "" + queryString);
	    	
	    	    int sessionAttributeStartIndex = pageKey.indexOf("$session.");
	    	    while(sessionAttributeStartIndex > -1)
	    	    {
	        	    int sessionAttributeEndIndex = pageKey.indexOf("_", sessionAttributeStartIndex);
	        	    String sessionAttribute = null;
	        	    if(sessionAttributeEndIndex > -1)
	        	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9, sessionAttributeEndIndex);
	        	    else
	        	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9);
	        	    
	        	    Object sessionAttributeValue = session.getAttribute(sessionAttribute);
	        	    if(sessionAttributeValue == null && sessionAttribute.equalsIgnoreCase("principal"))
	        	    	sessionAttributeValue = session.getAttribute("infogluePrincipal");
	        	    
	        	    pageKey = pageKey.replaceAll("\\$session." + sessionAttribute, "" + sessionAttributeValue);    	    
	    	    
	        	    sessionAttributeStartIndex = pageKey.indexOf("$session.");
	    	    }

	    	    int cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
	    	    while(cookieAttributeStartIndex > -1)
	    	    {
	        	    int cookieAttributeEndIndex = pageKey.indexOf("_", cookieAttributeStartIndex);
		    	    String cookieAttribute = null;
	        	    if(cookieAttributeEndIndex > -1)
	        	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8, cookieAttributeEndIndex);
	        	    else
	        	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8);

	        	    HttpHelper httpHelper = new HttpHelper();
	        	    pageKey = pageKey.replaceAll("\\$cookie." + cookieAttribute, "" + httpHelper.getCookie(request, cookieAttribute));    	    
	    	    
	        	    cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
	    	    }

			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion had a different pageCacheKey:" + e.getMessage(), e);
		}

		return pageKey;
	}

	/**
	 * This method returns true if the if the page in question (ie sitenode) has editOnSight disabled.
	 */
	
	public boolean getIsEditOnSightDisabled(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		boolean isEditOnSightDisabled = false;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getDisableEditOnSight() != null)
			{	
				if(latestSiteNodeVersionVO.getDisableEditOnSight().intValue() == NO.intValue())
					isEditOnSightDisabled = false;
				else if(latestSiteNodeVersionVO.getDisableEditOnSight().intValue() == YES.intValue())
					isEditOnSightDisabled = true;
				else if(latestSiteNodeVersionVO.getDisableEditOnSight().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						isEditOnSightDisabled = getIsEditOnSightDisabled(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
			
		logger.info("getIsEditOnSightDisabled:" + isEditOnSightDisabled);
		
		return isEditOnSightDisabled;
	}
	
	/**
	 * This method returns true if the if the page in question (ie sitenode) is protected byt the exctranet fnctionality.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public boolean getIsPageProtected(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		boolean isPageProtected = false;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getIsProtected() != null)
			{	
				if(latestSiteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.NO.intValue())
					isPageProtected = false;
				else if(latestSiteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
					isPageProtected = true;
				else if(latestSiteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
					isPageProtected = true;
				else if(latestSiteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						isPageProtected = getIsPageProtected(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return isPageProtected;
	}
	
	/**
	 * This method returns true if the if the page in question (ie sitenode) is protected byt the exctranet fnctionality.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public Integer getInheritedPageCacheTimeout(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		Integer pageCacheTimeout = null;
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getPageCacheTimeout() != null && !latestSiteNodeVersionVO.getPageCacheTimeout().equals(""))
			{	
				try
				{
					pageCacheTimeout = new Integer(latestSiteNodeVersionVO.getPageCacheTimeout());
				}
				catch (Exception e) 
				{
					logger.warn("Wrong format on pageCacheTimeout on siteNode:" + latestSiteNodeVersionVO.getSiteNodeName(), e);
				}
			}
			if(pageCacheTimeout == null)
			{
				SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
				if(parentSiteNode != null)
					pageCacheTimeout = getInheritedPageCacheTimeout(db, parentSiteNode.getSiteNodeId()); 
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get page cache timeout:" + e.getMessage(), e);
		}
				
		return pageCacheTimeout;
	}
	
	/**
	 * This method returns the id of the siteNodeVersion that is protected if any.
	 */
	
	public Integer getProtectedSiteNodeVersionId(Database db, Integer siteNodeId, String interceptionPointName)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		Integer protectedSiteNodeVersionId = null;
		try
		{
			Boolean honourInheritanceFallback = true;
			if(interceptionPointName.equals("SiteNodeVersion.Read"))
				honourInheritanceFallback = false;
			
			InterceptionPointVO ipVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);

			SiteNodeVersionVO siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionVO.getId(), ipVO.getId(), honourInheritanceFallback, db);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
		
		/*
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
			logger.info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getIsProtected() != null)
			{	
				logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsProtected());
				if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					protectedSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
					if(parentSiteNode != null)
						protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
		*/
				
		return protectedSiteNodeVersionId;
	}


	/**
	 * This method returns the id of the siteNodeVersion that is protected if any.
	 */
	
	public Integer getProtectedSiteNodeVersionIdForPageCache(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getIsProtected() != null)
			{	
				if(logger.isInfoEnabled())
					logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsProtected());
				if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
					protectedSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
					protectedSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
					if(parentSiteNode != null)
						protectedSiteNodeVersionId = getProtectedSiteNodeVersionIdForPageCache(db, parentSiteNode.getSiteNodeId()); 
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return protectedSiteNodeVersionId;
	}


	/**
	 * This method returns the id of the siteNodeVersion that is protected if any.
	 */
	
	public Integer getForceProtocolChangeSettingForPageCache(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		Integer forceProtocolChangeSetting = SiteNodeVersionVO.NORMAL_SECURE;
		
		if(getOperatingMode() == 3)
		{
			try
			{
				SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
				if(siteNodeVersionVO != null && siteNodeVersionVO.getForceProtocolChange() != null)
				{	
					//logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getForceProtocolChange());
					if(logger.isInfoEnabled())
						logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getForceProtocolChange());
					
					if(siteNodeVersionVO.getForceProtocolChange().intValue() != SiteNodeVersionVO.INHERIT_SECURE.intValue())
						forceProtocolChangeSetting = siteNodeVersionVO.getForceProtocolChange();
					else
					{
						SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
						if(parentSiteNode != null)
							forceProtocolChangeSetting = getForceProtocolChangeSettingForPageCache(db, parentSiteNode.getSiteNodeId()); 
					}
				}
			}
			catch(Exception e)
			{
				logger.warn("An error occurred trying to get if the siteNodeVersion has forceProtocolChangeSetting:" + e.getMessage(), e);
			}
		}
		
		return forceProtocolChangeSetting;
	}

	
	/**
	 * This method returns the id of the siteNodeVersion that has disabled languages if any.
	 */
	
	public Integer getDisabledLanguagesSiteNodeVersionId(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		Integer disabledLanguagesSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			logger.info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getDisableLanguages() != null)
			{	
				logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getDisableLanguages());
				if(siteNodeVersionVO.getDisableLanguages().intValue() == NO.intValue())
				    disabledLanguagesSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getDisableLanguages().intValue() == YES.intValue())
				    disabledLanguagesSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getDisableLanguages().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
					if(parentSiteNode != null)
					    disabledLanguagesSiteNodeVersionId = getDisabledLanguagesSiteNodeVersionId(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled languages:" + e.getMessage(), e);
		}
				
		return disabledLanguagesSiteNodeVersionId;
	}

	/**
	 * This method returns the id of the siteNodeVersion that has disabled identity check if any.
	 */
	
	public Integer getDisableForceIdentityCheckSiteNodeVersionId(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		Integer disableForceIdentityCheckSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			logger.info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getDisableLanguages() != null)
			{	
				logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getDisableForceIdentityCheck());
				if(siteNodeVersionVO.getDisableForceIdentityCheck().intValue() == NO.intValue())
					disableForceIdentityCheckSiteNodeVersionId = null;
				else if(siteNodeVersionVO.getDisableForceIdentityCheck().intValue() == YES.intValue())
					disableForceIdentityCheckSiteNodeVersionId = siteNodeVersionVO.getId();
				else if(siteNodeVersionVO.getDisableForceIdentityCheck().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
					if(parentSiteNode != null)
						disableForceIdentityCheckSiteNodeVersionId = getDisableForceIdentityCheckSiteNodeVersionId(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled identity check:" + e.getMessage(), e);
		}
				
		return disableForceIdentityCheckSiteNodeVersionId;
	}

	/**
	 * This method returns the id of the siteNodeVersion that has disabled identity check if any.
	 */
	
	public boolean getIsForcedIdentityCheckDisabled(Database db, Integer siteNodeId)
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		boolean isForcedIdentityCheckDisabled = false;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = this.getLatestActiveSiteNodeVersionVOForPageCache(db, siteNodeId);
			logger.info("siteNodeId:" + siteNodeId);
			if(siteNodeVersionVO != null && siteNodeVersionVO.getDisableLanguages() != null)
			{	
				logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getDisableForceIdentityCheck());
				if(siteNodeVersionVO.getDisableForceIdentityCheck().intValue() == NO.intValue())
					isForcedIdentityCheckDisabled = false;
				else if(siteNodeVersionVO.getDisableForceIdentityCheck().intValue() == YES.intValue())
					isForcedIdentityCheckDisabled = true;
				else if(siteNodeVersionVO.getDisableForceIdentityCheck().intValue() == INHERITED.intValue())
				{
					SiteNodeVO parentSiteNode = this.getParentSiteNodeForPageCache(db, siteNodeId);
					if(parentSiteNode != null)
						isForcedIdentityCheckDisabled = getIsForcedIdentityCheckDisabled(db, parentSiteNode.getSiteNodeId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled identity check:" + e.getMessage(), e);
		}
				
		return isForcedIdentityCheckDisabled;
	}

	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, boolean inheritParentBindings, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, inheritParentBindings, false, deliveryContext);
		return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}


	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, boolean inheritParentBindings, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, inheritParentBindings, includeFolders, deliveryContext);
		return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}

	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, USE_INHERITANCE, true, deliveryContext);
		return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}
	
	/**
	 * This method return a single content bound. 
	 */
	
	public ContentVO getBoundContent(InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, DeliveryContext deliveryContext) throws SystemException, Exception
	{
	    List contents = null;
	    
	    Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		beginTransaction(db);

        try
        {
            contents = getBoundContents(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, USE_INHERITANCE, true, deliveryContext);
        
            closeTransaction(db);
	    }
	    catch(Exception e)
	    {
	        logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
	        throw new SystemException(e.getMessage());
	    }

        return (contents != null && contents.size() > 0) ? (ContentVO)contents.get(0) : null;
	}


	/**
	 * This method returns a list of contents bound to the named availableServiceBinding.
	 */
	
	public List getBoundContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, boolean useLanguageFallback, String availableServiceBindingName, boolean inheritParentBindings, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		List boundContentVOList = new ArrayList();
		
		Integer metaInfoContentId = null;
		if(availableServiceBindingName.equalsIgnoreCase("Meta information"))
		{
			SiteNodeVO siteNodeVO = getSiteNodeVO(db, siteNodeId);
			if(siteNodeVO != null)
				metaInfoContentId = siteNodeVO.getMetaInfoContentId();

			if(logger.isDebugEnabled())
			{
				logger.debug("siteNode for id: " + siteNodeId + "=" + siteNodeVO);
				logger.debug("metaInfoContentId: " + metaInfoContentId);
			}
		}
		
		if(metaInfoContentId != null && metaInfoContentId.intValue() > -1)
		{
			ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(db, metaInfoContentId, deliveryContext);
			boundContentVOList.add(contentVO);
		}
		else
		{
			StringBuilder boundContentsKey = new StringBuilder();
			boundContentsKey.append("")
			.append("").append(infoGluePrincipal.getName())
			.append("_").append(siteNodeId)
			.append("_").append(languageId)
			.append("_").append(useLanguageFallback)
			.append("_").append(includeFolders)
			.append("_").append(availableServiceBindingName)
			.append("_").append(USE_INHERITANCE);
			
			//String boundContentsKey = "" + infoGluePrincipal.getName() + "_" + siteNodeId + "_" + languageId + "_" + useLanguageFallback + "_" + includeFolders + "_" + availableServiceBindingName + "_" + USE_INHERITANCE;
			logger.warn("Strange... why:" + boundContentsKey);
			
			boundContentVOList = (List)CacheController.getCachedObjectFromAdvancedCache("boundContentCache", boundContentsKey.toString());
			if(boundContentVOList != null)
			{
				if(logger.isInfoEnabled())
					logger.info("There was an cached content boundContentVOList:" + boundContentVOList.size());
			}
			else
			{
				boundContentVOList = new ArrayList();
				
				if(availableServiceBindingName.equalsIgnoreCase("Meta information"))
					logger.warn("Entering the old logic - bad for performance - why is siteNode with id:" + siteNodeId + " not getting it's metaInfoContentId:" + metaInfoContentId);

				AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingDeliveryController.getAvailableServiceBindingDeliveryController().getAvailableServiceBindingVO(availableServiceBindingName, db);
	        
			    List qualifyerList = new ArrayList();
		    	ServiceDefinitionVO serviceDefinitionVO = getInheritedServiceDefinition(qualifyerList, siteNodeId, availableServiceBindingVO, db, inheritParentBindings);
				if(serviceDefinitionVO != null)
				{
					String serviceClassName = serviceDefinitionVO.getClassName();
					BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
	    		 
					HashMap arguments = new HashMap();
					arguments.put("method", "selectContentListOnIdList");
	        		arguments.put("arguments", qualifyerList);
					
					List contents = service.selectMatchingEntities(arguments, db);
					
					if(logger.isInfoEnabled())
						logger.info("Found bound contents:" + contents.size());	        		
					
					if(contents != null)
					{
						Iterator i = contents.iterator();
						while(i.hasNext())
						{
							ContentVO candidate = (ContentVO)i.next();
							
							if(logger.isInfoEnabled())
								logger.info("candidate:" + candidate.getName());
							
							//Checking to see that now is between the contents publish and expire-date. 
							//if(ContentDeliveryController.getContentDeliveryController().isValidContent(candidate.getId(), languageId, useLanguageFallback, infoGluePrincipal))
							//	boundContentVOList.add(candidate);        		
							//Content candidateContent = (Content)getObjectWithId(ContentImpl.class, candidate.getId(), db); 
							
							//Content candidateContent = (Content)getObjectWithId(ContentImpl.class, candidate.getId(), db); 
							ContentVO candidateContent = ContentController.getContentController().getSmallContentVOWithId(candidate.getId(), db, deliveryContext);
							
							if(logger.isInfoEnabled())
								logger.info("candidateContent:" + candidateContent.getName());
							
							if(ContentDeliveryController.getContentDeliveryController().isValidContent(infoGluePrincipal, candidateContent, languageId, useLanguageFallback, includeFolders, db, deliveryContext))
							{
								deliveryContext.addUsedContent(CacheController.getPooledString(1, candidate.getId()));
							    boundContentVOList.add(candidate);    
							}
						}
						CacheController.cacheObjectInAdvancedCache("boundContentCache", boundContentsKey.toString(), boundContentVOList);
					}
				}
			}
		}

		return boundContentVOList;

	}


	

	/**
	 * This method returns a list of children to the bound content with the named availableServiceBindingName.
	 * The collection of contents are also sorted on given arguments.
	 */
	
	public List getBoundFolderContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, String availableServiceBindingName, boolean searchRecursive, Integer maximumNumberOfLevels, String sortAttribute, String sortOrder, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List folderContents = new ArrayList();
		
		deliveryContext.addUsedContent("selectiveCacheUpdateNonApplicable");
		
        ContentVO contentVO = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, availableServiceBindingName, includeFolders, deliveryContext);
        
        if(contentVO != null)
        {
           	folderContents = ContentDeliveryController.getContentDeliveryController().getSortedChildContents(infoGluePrincipal, languageId, contentVO.getContentId(), siteNodeId, db, searchRecursive, maximumNumberOfLevels, sortAttribute, sortOrder, useLanguageFallback, includeFolders, deliveryContext);
        }
        
		return folderContents;
	}


	/**
	 * This method returns a list of children to the bound content with the named availableServiceBindingName.
	 * The collection of contents are also sorted on given arguments.
	 */
	
	public List getBoundFolderContents(Database db, InfoGluePrincipal infoGluePrincipal, Integer contentId, Integer languageId, boolean searchRecursive, Integer maximumNumberOfLevels, String sortAttribute, String sortOrder, boolean useLanguageFallback, boolean includeFolders, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		List folderContents = new ArrayList();
		        
		folderContents = ContentDeliveryController.getContentDeliveryController().getSortedChildContents(infoGluePrincipal, languageId, contentId, siteNodeId, db, searchRecursive, maximumNumberOfLevels, sortAttribute, sortOrder, useLanguageFallback, includeFolders, deliveryContext);
		
		return folderContents;
	}


	/**
	 * This method return a single siteNode bound. 
	 */
	
	public SiteNodeVO getBoundSiteNode(Database db, Integer siteNodeId, String availableServiceBindingName) throws SystemException, Exception
	{
		List siteNodes = getBoundSiteNodes(db, siteNodeId, availableServiceBindingName);
		return (siteNodes != null && siteNodes.size() > 0) ? (SiteNodeVO)siteNodes.get(0) : null;
	}
	

	/**
	 * This method return a single siteNode bound. 
	 */
	
	public SiteNodeVO getBoundSiteNode(Database db, Integer siteNodeId, String availableServiceBindingName, int position) throws SystemException, Exception
	{
		List siteNodes = getBoundSiteNodes(db, siteNodeId, availableServiceBindingName);
		return (siteNodes != null && siteNodes.size() > position) ? (SiteNodeVO)siteNodes.get(position) : null;
	}


	/**
	 * This method should be rewritten later....
	 * The concept is to fetch the bound siteNode
	 */
	
	public List getBoundSiteNodes(Database db, Integer siteNodeId, String availableServiceBindingName) throws SystemException, Exception
	{
		if(siteNodeId != null && this.deliveryContext != null)
			this.deliveryContext.addUsedSiteNode(CacheController.getPooledString(3, siteNodeId));

		String boundSiteNodesKey = "" + siteNodeId + "_" + availableServiceBindingName + "_" + USE_INHERITANCE;
		logger.info("boundSiteNodesKey:" + boundSiteNodesKey);
		List boundSiteNodeVOList = (List)CacheController.getCachedObject("boundSiteNodeCache", boundSiteNodesKey);
		if(boundSiteNodeVOList != null)
		{
			logger.info("There was an cached content boundSiteNodeVOList:" + boundSiteNodeVOList.size());
		}
		else
		{
			boundSiteNodeVOList = new ArrayList();

		    AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingDeliveryController.getAvailableServiceBindingDeliveryController().getAvailableServiceBindingVO(availableServiceBindingName, db);

		    List qualifyerList = new ArrayList();
	    	ServiceDefinitionVO serviceDefinitionVO = getInheritedServiceDefinition(qualifyerList, siteNodeId, availableServiceBindingVO, db, USE_INHERITANCE);
			
		    if(serviceDefinitionVO != null)
			{
				String serviceClassName = serviceDefinitionVO.getClassName();
				BaseService service = (BaseService)Class.forName(serviceClassName).newInstance();
    		 	
				HashMap arguments = new HashMap();
				arguments.put("method", "selectSiteNodeListOnIdList");
        	
				arguments.put("arguments", qualifyerList);
    		
				List siteNodes = service.selectMatchingEntities(arguments, db);
    		
				logger.info("Found bound siteNodes:" + siteNodes.size());
				if(siteNodes != null)
				{
					Iterator i = siteNodes.iterator();
					while(i.hasNext())
					{
						SiteNodeVO candidate = (SiteNodeVO)i.next();
						logger.info("candidate:" + candidate.getId());
						//Checking to see that now is between the contents publish and expire-date. 
						if(isValidSiteNode(db, candidate.getId()))
							boundSiteNodeVOList.add(candidate);
					}
				}
			}
	       	        						
			CacheController.cacheObject("boundSiteNodeCache", boundSiteNodesKey, boundSiteNodeVOList);
		}
		
		return boundSiteNodeVOList;
	}


	
	/**
	 * This method returns a url to the given page. The url is composed of siteNode, language and content
	 */

	public String getPageUrl(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		String pageUrl = "";

		pageUrl = urlComposer.composePageUrl(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext); 
		
		return pageUrl;
	}


	public String getPageUrlAfterLanguageChange(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
        return urlComposer.composePageUrlAfterLanguageChange(db, infoGluePrincipal, siteNodeId, languageId, contentId, deliveryContext);
    } 
	
	public String getPageAsDigitalAssetUrl(Database database, InfoGluePrincipal principal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext context, String fileSuffix, boolean cacheUrl) throws SystemException
	{
		String pageAsDigitalAssetUrl = null;
		String pageUrl = null;
		String pageContent = null;
		String fileName = null;

		boolean fullUrl = context.getUseFullUrl();
		context.setUseFullUrl(true);
		
		boolean disableNiceUri = context.getDisableNiceUri();
		context.setDisableNiceUri(true);
		
		String assetCacheKey = "pageAsDigitalAssetUrl_" + siteNodeId + "_" + languageId + "_" + contentId + "_" + fileSuffix;
		logger.info("assetCacheKey:" + assetCacheKey);
		String cacheName = "assetUrlCache";
		if(cacheUrl)
		{
			String cachedAssetUrl = (String)CacheController.getCachedObject(cacheName, assetCacheKey);
			if(cachedAssetUrl != null)
			{
				logger.info("There was an cached cachedAssetUrl:" + cachedAssetUrl);
				context.setDisableNiceUri(disableNiceUri);
				context.setUseFullUrl(fullUrl);
	
				return cachedAssetUrl;
			}
		}
		
		try
		{			
			int i = 0;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				try
				{
					if(pageContent == null)
					{
						pageUrl = getPageUrl(database, principal, siteNodeId, languageId, contentId, context);
						logger.info("pageUrl:" + pageUrl);
						pageUrl = pageUrl.replaceAll("&amp;", "&");
						if(pageUrl.indexOf("&") > -1)
						{
							pageUrl = pageUrl + "&includeUsedEntities=true";
						}
						else
						{
							pageUrl = pageUrl + "?includeUsedEntities=true";
						}
						logger.info("pageUrl:" + pageUrl);
						
						Map headers = new HashMap();

						Enumeration headersEnumeration = context.getHttpServletRequest().getHeaderNames();
						while(headersEnumeration.hasMoreElements())
						{
							String headerName  = (String)headersEnumeration.nextElement();
							String headerValue = (String)context.getHttpServletRequest().getHeader(headerName);
							logger.info(headerName + "=" + headerValue);
							headers.put(headerName, headerValue);
						}
						
						headers.put("User-Agent", context.getHttpServletRequest().getHeader("User-Agent") + ";Java");
						
						HttpHelper helper = new HttpHelper();
						pageContent = helper.getUrlContent(pageUrl, headers, 3000);
						logger.info("pageContent:" + pageContent);
							
						int usedEntitiesIndex = pageContent.indexOf("<usedEntities>");
						if(usedEntitiesIndex > -1)
						{
							int usedEntitiesEndIndex = pageContent.indexOf("</usedEntities>");
							String usedEntities = pageContent.substring(usedEntitiesIndex + 14, usedEntitiesEndIndex);
							logger.info("usedEntities:" + usedEntities);
							String[] usedEntitiesArray = usedEntities.split(",");
							for(int j=0; j < usedEntitiesArray.length; j++)
							{
								String entity = usedEntitiesArray[j];
								logger.info("entity:" + entity);

								if(entity.indexOf("content_") > -1)
									context.addUsedContent(entity);
								else if(entity.indexOf("contentVersion_") > -1)
									context.addUsedContentVersion(entity);
								else if(entity.indexOf("siteNode_") > -1)
									context.addUsedSiteNode(entity);
								else if(entity.indexOf("siteNodeVersion_") > -1)
									context.addUsedSiteNodeVersion(entity);	
							}
						}
						fileName = "" + pageContent.hashCode() + (fileSuffix == null ? "" : "." + fileSuffix);
						logger.info("fileName:" + fileName);
					}

					DigitalAssetDeliveryController.getDigitalAssetDeliveryController().dumpAttributeToFile(pageContent, fileName, filePath);
				}
				catch(Exception e)
				{
					logger.warn("An file could not be written or the content could not be fetched from the url:" + pageUrl + " - reason: " + e.getMessage(), e);
					context.setDisablePageCache(true);
				}
			    
			    i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}

			SiteNodeVO siteNodeVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId, deliveryContext).getSiteNodeVO(database, siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNodeVO != null)
			{
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNodeVO.getRepositoryId(), database);
				if(repositoryVO.getDnsName() != null && !repositoryVO.getDnsName().equals(""))
					dnsName = repositoryVO.getDnsName();
			}

			pageAsDigitalAssetUrl = urlComposer.composeDigitalAssetUrl(dnsName, fileName, context);
			
			if(cacheUrl)
			{
		        CacheController.cacheObject(cacheName, assetCacheKey, pageAsDigitalAssetUrl);
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred when we fetched the url which we wanted to persist as an digital asset: " + e.getMessage(), e);
			throw new SystemException("An error occurred when we fetched the url which we wanted to persist as an digital asset: " + e.getMessage(), e);
		}
		finally
		{
			context.setDisableNiceUri(disableNiceUri);
			context.setUseFullUrl(fullUrl);
		}
				
		return pageAsDigitalAssetUrl;
	}

	/**
	 * This method constructs a string representing the path to the page with respect to where in the
	 * structure the page is. It also takes the page title into consideration. It is done by recursively going
	 * up in the structure until the root is reached. On each node we collect the pageTitle.
	 */

	public String getPagePath(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, String bindingName, String attributeName, boolean useLanguageFallBack, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		String pagePath = "/";
		
		SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);		
		if(parentSiteNode != null)
		{
			pagePath = getPagePath(db, infoGluePrincipal, parentSiteNode.getId(), languageId, null, bindingName, attributeName, useLanguageFallBack, deliveryContext) + "/"; 
		}
		
		pagePath += this.getPageNavigationTitle(db, infoGluePrincipal, siteNodeId, languageId, contentId, bindingName, attributeName, useLanguageFallBack, deliveryContext, false);
		pagePath = pagePath.replaceAll(" ", "_");
		
		return pagePath;
	}


	/**
	 * This method returns a url to the delivery engine
	 */
	public String getPageBaseUrl(Database db) throws SystemException, Exception
	{
		String pageUrl = "";
		
		SiteNodeVO siteNodeVO = getSiteNodeVO(db, this.siteNodeId);
		String dnsName = CmsPropertyHandler.getWebServerAddress();
		if(siteNodeVO != null)
		{
			RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(siteNodeVO.getRepositoryId(), db);
			if(repositoryVO.getDnsName() != null && !repositoryVO.getDnsName().equals(""))
				dnsName = repositoryVO.getDnsName();
		}

		/*
		SiteNode siteNode = this.getSiteNode(db, this.siteNodeId);
		String dnsName = CmsPropertyHandler.getWebServerAddress();
		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
			dnsName = siteNode.getRepository().getDnsName();
		*/
						
		pageUrl = urlComposer.composePageBaseUrl(dnsName); 
		
		return pageUrl;
	}


	/**
	 * This method returns the navigation-title to the given page. 
	 * The title is based on the content sent in firstly, secondly the siteNode. 
	 * The actual text is fetched from either the content or the metacontent bound to the sitenode. 
	 */
	public String getPageNavigationTitle(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, String metaBindingName, String attributeName, boolean useLanguageFallback, DeliveryContext deliveryContext, boolean escapeHTML) throws SystemException, Exception
	{
		String navTitle = "";
		if(contentId == null || contentId.intValue() == -1)
		{
			ContentVO content = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, metaBindingName, deliveryContext);
			if(content != null)
				navTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, escapeHTML, true);
		}
		else
		{
			navTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, contentId, languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, escapeHTML, true);
		}
		
		return navTitle;
	}
	
		/**
	 * This method returns the navigation-title to the given page. 
	 * The title is based on the content sent in firstly, secondly the siteNode. 
	 * The actual text is fetched from either the content or the metacontent bound to the sitenode. 
	 */

	public String getPageNavigationTitle(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer repositoryId, Integer languageId, Integer contentId, String metaBindingName, String attributeName, boolean useLanguageFallback, DeliveryContext deliveryContext, boolean escapeHTML) throws SystemException, Exception
	{
		String navTitle = "";
		
		String attributeKey = "" + siteNodeId + "_" + languageId + "_" + attributeName;
    	String candidate = (String)CacheController.getCachedObjectFromAdvancedCache("metaInfoContentAttributeCache", attributeKey);
    	/*
    	if(candidate == null && useLanguageFallback)
    	{
			LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(db, repositoryId);
			if(!masterLanguageVO.getId().equals(languageId))
			{
				String attributeKeyFallback = "" + siteNodeId + "_" + masterLanguageVO.getId() + "_" + attributeName;
				candidate = (String)CacheController.getCachedObjectFromAdvancedCache("metaInfoContentAttributeCache", attributeKeyFallback);
			}
    	}
    	*/
    	
    	if(candidate != null)
    	{
    		//System.out.println("candidate:" + candidate + " for " + attributeKey);
    		navTitle = candidate;
    	}
    	else
    	{
    		//System.out.println("No candidate for " + attributeKey);
			if(contentId == null || contentId.intValue() == -1)
			{
				ContentVO content = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, useLanguageFallback, metaBindingName, deliveryContext);
				if(content != null)
					navTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, escapeHTML, true);
			}
			else
			{
				navTitle = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, contentId, languageId, attributeName, siteNodeId, useLanguageFallback, deliveryContext, infoGluePrincipal, escapeHTML, true);
			}

        	//String groupKey1 = CacheController.getPooledString(2, contentVersionId);
        	String groupKey2 = CacheController.getPooledString(1, contentId);

			//String attributeKey = "" + siteNode.getId() + "_" + languageId + "_" + name;
			//System.out.println("Caching " + attributeName + "=" + navTitle + " on " + attributeKey);
        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKey, navTitle, new String[]{"selectiveCacheUpdateNonApplicable", groupKey2}, true);
    	}
    	
		return navTitle;
	}
	
    public Integer getSiteNodeId(Database db, InfoGluePrincipal infogluePrincipal, Integer repositoryId, String path, String attributeName, Integer parentSiteNodeId, Integer languageId, DeliveryContext deliveryContext, String requestLanguageId) throws SystemException, Exception
    {
    	Timer t = new Timer();
        /*
        logger.info("repositoryId:" + repositoryId);
        logger.info("navigationTitle:" + navigationTitle);
        logger.info("parentSiteNodeId:" + parentSiteNodeId);
        logger.info("languageId:" + languageId);
        */
    	if (repositoryId == null || repositoryId.intValue() == -1) 
        {
            repositoryId = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getRepositoryId();
            logger.info("RepositoryId not specifed - Resolved master repository to "+repositoryId);
        }
        
        if (repositoryId == null)
            throw new SystemException("No repository given and unable to resolve master repository");

        List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguagesForRepository(db, repositoryId);

        List siteNodes = new ArrayList();
        
        if(parentSiteNodeId == null || parentSiteNodeId.intValue() == -1)
        {
            SiteNodeVO rootSiteNodeVO = this.getRootSiteNode(db, repositoryId);
            if(rootSiteNodeVO != null)
            	siteNodes.add(rootSiteNodeVO);
        }
        else
        {
        	try
        	{
        		siteNodes = this.getChildSiteNodes(db, parentSiteNodeId, 0, true, null, true);
        		//NEW!!! siteNodes = this.getChildSiteNodes(db, parentSiteNodeId, 1);
        	}
        	catch (Exception e) 
        	{
        		logger.error("Error getting siteNode from nice uri: " + e.getMessage(), e);
			}
        }

        Iterator siteNodeIterator = siteNodes.iterator();
        while (siteNodeIterator.hasNext()) 
        {
            SiteNodeVO siteNodeVO = (SiteNodeVO)siteNodeIterator.next();
	        if (path == null || path.length() == 0) 
	        {
	            logger.info("Returning siteNode:" + siteNodeVO.getName());
	            return siteNodeVO.getId();
	        }
            
	        logger.info("Continued with siteNode: " + siteNodeVO.getName());
	        
	        if(siteNodeVO.getMetaInfoContentId() == null)
	        	throw new SystemException("The site node " + siteNodeVO.getName() + "(" + siteNodeVO.getId() + ") had no meta info. Fix this by editing the site node. Should never happen.");
	        
	        ContentVO content = null;
	        try
	        {
	        	content = ContentDeliveryController.getContentDeliveryController().getContentVO(db, siteNodeVO.getMetaInfoContentId(), deliveryContext);
	        }
	        catch (Exception e) 
	        {
				logger.error("The site node " + siteNodeVO.getName() + "(" + siteNodeVO.getId() + ") had no valid meta info. Fix this by editing the site node. Should never happen. Message:" + e.getMessage());
				logger.warn("The site node " + siteNodeVO.getName() + "(" + siteNodeVO.getId() + ") had no valid meta info. Fix this by editing the site node. Should never happen. Message:" + e.getMessage(), e);
			}
	        
	        if(content != null) 
	        {
	            //logger.info("Content "+content.getContentId());
	            String pathCandidate = null;
	            for (int i=0;i<languages.size();i++) 
	            {
	                LanguageVO language = (LanguageVO) languages.get(i);
	                //logger.info("Language : "+language.getLanguageCode());
	                
	                if(attributeName.equals("SiteNode.name"))
	                    pathCandidate = siteNodeVO.getName();
	                else
	                {
	                	pathCandidate = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), language.getLanguageId(), attributeName, siteNodeVO.getSiteNodeId(), false, deliveryContext, infogluePrincipal, false, true);
	                    
	    	        	String fallbackAttributeName = CmsPropertyHandler.getNiceURIFallbackAttributeName();
	                    if((pathCandidate == null || pathCandidate.equals("")))
	                    {
		    	        	if(fallbackAttributeName.equals("SiteNode.name"))
		    	            {
		    	                pathCandidate = siteNodeVO.getName();
		    	            }
		    	        	else
		    	        	{
		                        pathCandidate = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), language.getLanguageId(), fallbackAttributeName, siteNodeVO.getSiteNodeId(), false, deliveryContext, infogluePrincipal, false, true);
		    	        	}
	                    }
	                    
		    	        if((pathCandidate == null || pathCandidate.equals("")) && !attributeName.equals(NAV_TITLE_ATTRIBUTE_NAME))
		    	        	pathCandidate = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, content.getContentId(), language.getLanguageId(), NAV_TITLE_ATTRIBUTE_NAME, siteNodeVO.getSiteNodeId(), false, deliveryContext, infogluePrincipal, false, true);
	                }
	                
                	logger.info(attributeName + " ["+pathCandidate.trim()+"]==[" + path + "]");
	                if (pathCandidate != null && pathCandidate.toLowerCase().trim().equals(path.toLowerCase())) 
	                {
	                	if(requestLanguageId != null && !requestLanguageId.equals("") && !requestLanguageId.equals("-1"))
	                	{
	                		logger.info("Not resetting languageID as it came from request: " + requestLanguageId);
	                	}
	                	else if(deliveryContext.getLanguageId() == null || deliveryContext.getLanguageId() == -1)
	                	{
	                		LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageIfSiteNodeSupportsIt(db, language.getId(), siteNodeVO.getId());
	                		logger.info("LanguageId was " + languageId + " and deliveryContext was " + deliveryContext.getLanguageId() + ". Is now:" + language + " vs " + languageVO);
	                		if(languageVO != null && languageVO.getId() == language.getId())
	                		{
	                			deliveryContext.setLanguageId(language.getId());
	                			logger.info("LanguageId was " + languageId + " and deliveryContext was " + deliveryContext.getLanguageId() + ". Is now:" + language);
	                		}
	                	}
	                	
	                	return siteNodeVO.getSiteNodeId();
	                }
	            }
	        }
	        else
	        {
	            throw new SystemException("You must run validation service in the management tool against this db - it needs to become up2date with the new model.");
	        }
	    }
        //t.printElapsedTime("getSiteNodeId took");
        
        return null;
    }

    public String getPageNavigationPath(Database db, InfoGluePrincipal infogluePrincipal, Integer siteNodeId, Integer languageId, Integer contentId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
    	StringBuilder path = null; //new StringBuffer("/");

        SiteNodeVO parentSiteNode = this.getParentSiteNode(db, siteNodeId);
        if (parentSiteNode != null)
        {
            path = new StringBuilder(getPageNavigationPath(db, infogluePrincipal, parentSiteNode.getId(), languageId, null, deliveryContext)).append("/");
        } 
        else 
        {
            return "";
        }
        
        String niceURIEncoding = CmsPropertyHandler.getNiceURIEncoding();
        if(niceURIEncoding == null || niceURIEncoding.length() == 0)
            niceURIEncoding = "UTF-8";
        
        String attributeName = ViewPageFilter.attributeName;
        if(attributeName == null)
        	attributeName = CmsPropertyHandler.getNiceURIAttributeName();
        
        String pathPart;
        
        if(attributeName.equals("SiteNode.name"))
        {
            SiteNodeVO siteNode = this.getSiteNodeVO(db, siteNodeId);
            pathPart = siteNode.getName();
        }
        else
        {
	        pathPart = this.getPageNavigationTitle(db, infogluePrincipal, siteNodeId, languageId, null, META_INFO_BINDING_NAME, attributeName, true, deliveryContext, false);
	        if((pathPart == null || pathPart.equals(""))) 
	        {
	        	String fallbackAttributeName = CmsPropertyHandler.getNiceURIFallbackAttributeName();
	        	if(fallbackAttributeName.equals("SiteNode.name"))
	            {
	                SiteNodeVO siteNode = this.getSiteNodeVO(db, siteNodeId);
	                pathPart = siteNode.getName();
	            }
	        	else
	        	{
	        		pathPart = this.getPageNavigationTitle(db, infogluePrincipal, siteNodeId, languageId, null, META_INFO_BINDING_NAME, fallbackAttributeName, true, deliveryContext, false);
	        	}
	        	
		        if((pathPart == null || pathPart.equals("")) && !attributeName.equals(NAV_TITLE_ATTRIBUTE_NAME)) 
	        		pathPart = this.getPageNavigationTitle(db, infogluePrincipal, siteNodeId, languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, true, deliveryContext, false);
	        }
        }
        
        String key = "" + pathPart + "_" + niceURIEncoding;
        String encodedPath = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", key);
        if(encodedPath == null)
        {
        	encodedPath = URLEncoder.encode(pathPart, niceURIEncoding);
        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", key, encodedPath);
        }
        
        path.append(encodedPath);
     
        return path.toString();
    }

  
    public static Integer getSiteNodeIdFromPath(InfoGluePrincipal infogluePrincipal, RepositoryVO repositoryVO, String[] path, String attributeName, Integer languageId, DeliveryContext deliveryContext) throws SystemException, Exception
    {
    	return getSiteNodeIdFromPath(infogluePrincipal, repositoryVO, path, attributeName, deliveryContext, null, languageId, null);
    }

    public static Integer getSiteNodeIdFromPath(InfoGluePrincipal infogluePrincipal, RepositoryVO repositoryVO, String[] path, String attributeName, Integer languageId, DeliveryContext deliveryContext, String requestLanguageId) throws SystemException, Exception
    {
    	return getSiteNodeIdFromPath(infogluePrincipal, repositoryVO, path, attributeName, deliveryContext, null, languageId, requestLanguageId);
    }
    
    
    public static Integer getSiteNodeIdFromPath(InfoGluePrincipal infogluePrincipal, RepositoryVO repositoryVO, String[] path, String attributeName, DeliveryContext deliveryContext, HttpSession session, Integer languageId, String requestLanguageId) throws SystemException, Exception
    {
        Integer siteNodeId = null;
        Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);

		try
		{
			siteNodeId = getSiteNodeIdFromPath(db, infogluePrincipal, repositoryVO, path, attributeName, deliveryContext, session, languageId, requestLanguageId);
	        commitTransaction(db);
	    }
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		finally
		{
			rollbackTransaction(db);
			closeDatabase(db);
		}
		
	    return siteNodeId;
    }

    public static Integer getSiteNodeIdFromPath(Database db, InfoGluePrincipal infogluePrincipal, RepositoryVO repositoryVO, String[] path, String attributeName, DeliveryContext deliveryContext, HttpSession session, Integer languageId, String requestLanguageId) throws SystemException, Exception
    {
    	return getSiteNodeIdFromPath(db, infogluePrincipal, repositoryVO, path, attributeName, deliveryContext, session, languageId, requestLanguageId, false);
    }
    
    public static Integer getSiteNodeIdFromPath(Database db, InfoGluePrincipal infogluePrincipal, RepositoryVO repositoryVO, String[] path, String attributeName, DeliveryContext deliveryContext, HttpSession session, Integer languageId, String requestLanguageId, boolean resetLanguageContextOnCacheHit) throws SystemException, Exception
    {
        Integer siteNodeId = null;

        URIMapperCache uriCache = URIMapperCache.getInstance();
	
        int numberOfPaths = path.length;
        while (numberOfPaths >= 0) 
        {
        	//logger.info("Looking for cached nodeName at index "+idx);
            siteNodeId = uriCache.getCachedSiteNodeId(repositoryVO.getId(), path, numberOfPaths, requestLanguageId);
            
            if (siteNodeId != null)
            {
            	logger.info("languageId:" + languageId);
            	logger.info("deliveryContext.getLanguageId():" + deliveryContext.getLanguageId());
            	logger.info("requestLanguageId:" + requestLanguageId);
                Integer siteNodeLanguageId = uriCache.getCachedSiteNodeLanguageId(repositoryVO.getId(), path, numberOfPaths, requestLanguageId);
                if(siteNodeLanguageId != null && resetLanguageContextOnCacheHit)
                {
                	deliveryContext.setLanguageId(siteNodeLanguageId);
                	logger.info("A languageId was " + languageId + " and deliveryContext was " + deliveryContext.getLanguageId() + ". Is now:" + siteNodeLanguageId);
                }
                
            	break;
            }
            
            numberOfPaths = numberOfPaths - 1;
        }
        
        String repositoryPath = null;
    	boolean noHostStated = true;
    	
    	if(!CmsPropertyHandler.getOperatingMode().equals("3"))
    	{
	    	int workingPathStartIndex = repositoryVO.getDnsName().indexOf("workingPath=");
	    	if(workingPathStartIndex != -1)
	    	{
	    		int workingPathEndIndex = repositoryVO.getDnsName().indexOf(",", workingPathStartIndex);
	    		if(workingPathEndIndex > -1)
		    		repositoryPath = repositoryVO.getDnsName().substring(workingPathStartIndex + 12, workingPathEndIndex);
	    		else
	    			repositoryPath = repositoryVO.getDnsName().substring(workingPathStartIndex + 12);
	    	}
	    	if(repositoryVO.getDnsName().indexOf("working=") > -1)
	    		noHostStated = false;
    	}

    	if(repositoryPath == null)
    	{
        	int pathStartIndex = repositoryVO.getDnsName().indexOf("path=");
        	if(pathStartIndex != -1)
        	{
        		int pathEndIndex = repositoryVO.getDnsName().indexOf(",", pathStartIndex);
	    		if(pathEndIndex > -1)
		    		repositoryPath = repositoryVO.getDnsName().substring(pathStartIndex + 5, pathEndIndex);
	    		else
	    			repositoryPath = repositoryVO.getDnsName().substring(pathStartIndex + 5);
        	}
	    	if(repositoryVO.getDnsName().indexOf("live=") > -1 || repositoryVO.getDnsName().indexOf("preview=") > -1)
	    		noHostStated = false;
    	}
    	
		if(logger.isInfoEnabled())
		{
	    	logger.info("repositoryPath:" + repositoryPath);    	
	    	logger.info("path:" + path.length);    	
		}
		
		if(repositoryPath == null && noHostStated)
		{
			if(logger.isInfoEnabled())
    			logger.info("The repo " + repositoryVO.getName() + " seems corrupt so this repository should be excluded.");
    		return null;
		}
		
    	if(repositoryPath != null && path.length <= 0)
    	{
    		if(logger.isInfoEnabled())
    			logger.info("There was a repository path:" + repositoryPath + " but the path.length was " + path.length + " so this repository should be excluded.");
    		return null;
    	}

    	if(repositoryPath != null && path.length > 0)
    	{
    		String[] repositoryPaths = repositoryPath.split("/");
    		for(int i=0; i<repositoryPaths.length; i++)
    		{
    			String repositoryPathPart = repositoryPaths[i];
    			if(path.length <= i)
    			{
    				logger.error("Could not match the repository paths so this repository should be excluded.");
    				return null;
    			}
    			
    			String pathPart = path[i];
    			if(logger.isInfoEnabled())
    			{
    				logger.info("repositoryPathPart:" + repositoryPathPart);
    				logger.info("pathPart:" + pathPart);
    			}
    			
    			if(!repositoryPathPart.equalsIgnoreCase(pathPart))
    			{
    				if(logger.isInfoEnabled())
    	    			logger.info("Could not match the repository paths so this repository should be excluded.");
    				return null;
    			}
    		}
    	}
    	
    	if(repositoryPath != null && path.length > 0)
    	{
    		String[] repositoryPaths = repositoryPath.split("/");
    		String[] newPath = path;
    		
    		if(logger.isInfoEnabled())
    		{
    			logger.info("repositoryPaths:" + repositoryPaths.length); 
	    		logger.info("newPath:" + newPath.length); 
    		}
    		
    		for(int repPathIndex = 0; repPathIndex < repositoryPaths.length; repPathIndex++)
    		{
    			String repPath = repositoryPaths[repPathIndex];
    			if(logger.isInfoEnabled())
	    			logger.info("repPath:" + repPath);
    	    	if(path.length > repPathIndex)
    	    	{
    	    		if(logger.isInfoEnabled())
    	    			logger.info("path:" + path[repPathIndex]);
    		    	if(path[repPathIndex].equals(repPath))
    		    	{
    		    		String[] tempNewPath = new String[newPath.length - 1];
    		    		for(int i=1; i<newPath.length; i++)
    		    			tempNewPath[i-1] = newPath[i];
    		    		
    		    		newPath = tempNewPath;
    		    	}    	    		
    	    	}
    		}
    		path = newPath;
    	}
    	
    	if(logger.isInfoEnabled())
    	{
		   	logger.info("new path:" + path.length);
	        logger.info("numberOfPaths = "+numberOfPaths);
    	}
        
        String enableNiceURIForLanguage = CmsPropertyHandler.getEnableNiceURIForLanguage();
    	if((enableNiceURIForLanguage == null || !enableNiceURIForLanguage.equals("false")) && path.length > 0 && path[0].length() == 2)
	    {
	    	LanguageVO language = LanguageDeliveryController.getLanguageDeliveryController().getLanguageWithCode(db, path[0].toLowerCase());
	    	if(language != null)
	    		enableNiceURIForLanguage = "true";
	    }

    	//logger.info("enableNiceURIForLanguage:" + enableNiceURIForLanguage);
        //logger.info("numberOfPaths:" + numberOfPaths);
    	if(enableNiceURIForLanguage.equalsIgnoreCase("true") && path.length > 0)
    	{
        	//logger.info("path[numberOfPaths]:" + path[numberOfPaths]);
    		//logger.info("path[0]:" + path[0]);
    		LanguageVO language = LanguageDeliveryController.getLanguageDeliveryController().getLanguageWithCode(db, path[0].toLowerCase());
    		//logger.info("language:" + language);
        	if(language != null)
        	{
        		//logger.info("YES - we should consider the first node as a language:" + language);
                session.setAttribute(FilterConstants.LANGUAGE_ID, language.getId());
                deliveryContext.setLanguageId(language.getId());
                languageId = language.getId();
                
        		String[] tempNewPath = new String[path.length - 1];
	    		for(int i=1; i<path.length; i++)
	    			tempNewPath[i-1] = path[i];
        		path = tempNewPath;
        	}
    	}
   
        for (int i = numberOfPaths;i < path.length; i++) 
        {
        	if (i < 0) 
            {
  	    		if(logger.isInfoEnabled())
	    	        logger.info("Getting root node");
                siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, deliveryContext.getLanguageId(), null, deliveryContext).getSiteNodeId(db, infogluePrincipal, repositoryVO.getId(), null, attributeName, null, languageId, deliveryContext, requestLanguageId);
  	        } 
            else 
            {
  	    		if(logger.isInfoEnabled())
	    	        logger.info("Getting normal");
                siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, deliveryContext.getLanguageId(), null, deliveryContext).getSiteNodeId(db, infogluePrincipal, repositoryVO.getId(), path[i], attributeName, siteNodeId, languageId, deliveryContext, requestLanguageId);
            }

            if (siteNodeId != null)
            {
            	Integer cacheLanguageId = deliveryContext.getLanguageId(); //languageId;
            	logger.info("cacheLanguageId:" + cacheLanguageId);
            	logger.info("deliveryContext.getLanguageId():" + deliveryContext.getLanguageId());
            	
            	if(cacheLanguageId != null && resetLanguageContextOnCacheHit)
            		uriCache.addCachedSiteNodeId(repositoryVO.getId(), path, i+1, siteNodeId, requestLanguageId, cacheLanguageId);
            }
        }
		
        return siteNodeId;
    }
    
    public static Integer getSiteNodeIdFromBaseSiteNodeIdAndPath(InfoGluePrincipal infogluePrincipal, String[] path, String attributeName, DeliveryContext deliveryContext, HttpSession session, Integer languageId, String siteNodeIdString, String remainingURI, String requestLanguageId) throws SystemException, Exception
    {
    	Integer siteNodeId = null;
    	Integer parentSiteNodeId = new Integer(siteNodeIdString);
    	
        Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);

		try
		{
			siteNodeId = getSiteNodeIdFromBaseSiteNodeIdAndPath(db, infogluePrincipal, path, attributeName, deliveryContext, session, languageId, siteNodeIdString, remainingURI, requestLanguageId);
			/*
			SiteNodeVO sitenodeVO = getNodeDeliveryController(deliveryContext).getSiteNodeVO(db, new Integer(siteNodeIdString));
	        for (int i = 0; i < path.length; i++) 
	        {
	        	siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, deliveryContext.getLanguageId(), null).getSiteNodeId(db, infogluePrincipal, sitenodeVO.getRepositoryId(), path[i], attributeName, parentSiteNodeId, languageId, deliveryContext);
    			if(siteNodeId != null)
    				parentSiteNodeId = siteNodeId;
	        }
	        */

	        commitTransaction(db);
	    }
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		finally
		{
			rollbackTransaction(db);
			closeDatabase(db);
		}
		
        return siteNodeId;
    }

    public static Integer getSiteNodeIdFromBaseSiteNodeIdAndPath(Database db, InfoGluePrincipal infogluePrincipal, String[] path, String attributeName, DeliveryContext deliveryContext, HttpSession session, Integer languageId, String siteNodeIdString, String remainingURI, String requestLanguageId) throws SystemException, Exception
    {
    	Integer siteNodeId = null;
    	Integer parentSiteNodeId = new Integer(siteNodeIdString);
    	
		SiteNodeVO sitenodeVO = getNodeDeliveryController(deliveryContext).getSiteNodeVO(db, new Integer(siteNodeIdString));
		for (int i = 0; i < path.length; i++) 
        {
        	siteNodeId = NodeDeliveryController.getNodeDeliveryController(null, deliveryContext.getLanguageId(), null, deliveryContext).getSiteNodeId(db, infogluePrincipal, sitenodeVO.getRepositoryId(), path[i], attributeName, parentSiteNodeId, languageId, deliveryContext, requestLanguageId);
			if(siteNodeId != null)
				parentSiteNodeId = siteNodeId;
        }

        return siteNodeId;
    }

	/**
	 * This method returns the contentId of the bound metainfo-content to the given page. 
	 */

	public Integer getMetaInfoContentId(Database db, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, String metaBindingName, boolean inheritParentBindings, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		ContentVO content = getBoundContent(db, infoGluePrincipal, siteNodeId, languageId, true, metaBindingName, inheritParentBindings, deliveryContext);
		if(content != null)
			return content.getContentId();
		
		return null;
	}



	/**
	 * This method returns the root siteNodeVO for the specified repository.
	 * If the repositoryName is null we fetch the name of the master repository.
	 */
	
	public static SiteNodeVO getRootSiteNode(Database db, String repositoryName) throws SystemException, Exception
	{
		if(repositoryName == null)
		{
			repositoryName = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getName();
			logger.info("Fetched name of master repository as none were given:" + repositoryName);
		}
		 
        SiteNode siteNode = null;

        logger.info("Fetching the root siteNode for the repository " + repositoryName);
		OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl c WHERE is_undefined(c.parentSiteNode) AND c.repository.name = $1");
		oql.bind(repositoryName);
		
    	QueryResults results = oql.execute(Database.READONLY);

		if (results.hasMore()) 
        {
        	siteNode = (SiteNode)results.next();
        	logger.info("The root node was found:" + siteNode.getName());
        }
        
		results.close();
		oql.close();

		logger.info("siteNode:" + siteNode);
		
        return (siteNode == null) ? null : siteNode.getValueObject();	
	}
	

	/**
	 * This method returns the root siteNodeVO for the specified repository.
	 * If the repositoryName is null we fetch the name of the master repository.
	 */
	
	public SiteNodeVO getRootSiteNode(Database db, Integer repositoryId) throws SystemException, Exception
	{
	    SiteNodeVO siteNodeVO = null;

        String key = "" + repositoryId;
		logger.info("key in getRootSiteNode:" + key);
		Object siteNodeVOCandidate = CacheController.getCachedObject("rootSiteNodeCache", key);
		if(siteNodeVOCandidate != null || siteNodeVOCandidate instanceof NullObject)
		{
			if(siteNodeVOCandidate instanceof SiteNodeVO)
			{
				logger.info("There was an cached master root siteNode:" + ((SiteNodeVO)siteNodeVOCandidate).getName());
				return (SiteNodeVO)siteNodeVOCandidate;
			}
			else
				return null;
		}
		else
		{
			if(repositoryId == null)
			{
				repositoryId = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db).getRepositoryId();
				logger.info("Fetched name of master repository as none were given:" + repositoryId);
			}
			
	        logger.info("Fetching the root siteNode for the repository " + repositoryId);
			//OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl c WHERE is_undefined(c.parentSiteNode) AND c.repository = $1");
			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl c WHERE is_undefined(c.parentSiteNode) AND c.repositoryId = $1");
			oql.bind(repositoryId);
			
	    	QueryResults results = oql.execute(Database.READONLY);
			
	    	if (results.hasMore()) 
	        {
	        	siteNodeVO = ((SiteNode)results.next()).getValueObject();
				if(logger.isInfoEnabled())
					logger.info("The root node was found:" + siteNodeVO.getName());
	        }

	    	results.close();
			oql.close();

			if(logger.isInfoEnabled())
				logger.info("siteNodeVO:" + siteNodeVO);
			
			if(siteNodeVO != null)
				CacheController.cacheObject("rootSiteNodeCache", key, siteNodeVO);
			else
				CacheController.cacheObject("rootSiteNodeCache", key, new NullObject());
		}

        return siteNodeVO;	
	}

	
	/**
	 * This method returns the list of siteNodeVO which is children to this one.
	 */
	
	public List getChildSiteNodes(Database db, Integer siteNodeId) throws SystemException, Exception
	{
		return getChildSiteNodes(db, siteNodeId, 0);
	}
	
	/**
	 * This method returns the list of siteNodeVO which is children to this one.
	 */

	public List getChildSiteNodes(Database db, Integer siteNodeId, Integer levelsToPopulate) throws SystemException, Exception
	{
		return getChildSiteNodes(db, siteNodeId, levelsToPopulate, false, null, false);
	}

	public List getChildSiteNodes(Database db, Integer siteNodeId, Integer levelsToPopulate, boolean showHidden, String nameFilter) throws SystemException, Exception
	{
		return getChildSiteNodes(db, siteNodeId, levelsToPopulate, showHidden, nameFilter, false);
	}

	public List getChildSiteNodes(Database db, Integer siteNodeId, Integer levelsToPopulate, boolean showHidden, String nameFilter, Boolean showLanguageDisabled) throws SystemException, Exception
	{
		return getChildSiteNodesOneLevel(db, siteNodeId, showHidden, nameFilter, showLanguageDisabled);
		/*
		//System.out.println("Query on siteNodeId:" + siteNodeId + "/" + levelsToPopulate);
		if(levelsToPopulate > 0)
			return getChildSiteNodesMultipleLevels(db, siteNodeId, levelsToPopulate);
		else
			return getChildSiteNodesOneLevel(db, siteNodeId);
		*/
	}

	/**
	 * This method returns the list of siteNodeVO which is children to this one.
	 */
	//private static Map<Integer,List<SiteNodeVO>> populatedSiteNodeVOList = new HashMap<Integer,List<SiteNodeVO>>();
	
	public List getChildSiteNodesMultipleLevels(Database db, Integer siteNodeId, Integer levelsToPopulate) throws SystemException, Exception
	{
		logger.info("getChildSiteNodes:" + siteNodeId);

    	if(siteNodeId == null)
		{
			return null;
		}
    	
    	SiteNodeVO cachedSiteNodeVO = SiteNodeController.getController().getSiteNodeVOWithIdIfInCache(siteNodeId, db);
    	if(cachedSiteNodeVO != null && (cachedSiteNodeVO.getChildCount() != null && cachedSiteNodeVO.getChildCount() == 0))
    	{
    		//System.out.println("No children - lets skip..");
    		return new ArrayList<SiteNodeVO>();
    	}
    	//else
    	//	System.out.println("Was unknown " + "(" + (cachedSiteNodeVO != null ? cachedSiteNodeVO.getChildCount() : " null ") + ")");
    	
    	List<SiteNodeVO> siteNodeVOList = (List<SiteNodeVO>)CacheController.getCachedObjectFromAdvancedCache("childPagesCache", "" + siteNodeId);
    	//List<SiteNodeVO> siteNodeVOList = populatedSiteNodeVOList.get(siteNodeId);
    	if(siteNodeVOList != null)
    	{
    		//System.out.println("Returning cached");
    		return siteNodeVOList;
    	}
    	else
    	{
    		Timer t = new Timer();
	   		
    		ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db);
    		List<ContentTypeAttribute> attributes = (List<ContentTypeAttribute>)ContentTypeDefinitionController.getController().getContentTypeAttributes(ctdVO.getSchemaValue());
    		
	        StringBuffer SQL = new StringBuffer();
	    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
	    	{
		   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.parentsiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentsiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, cv.languageId, ");
		   		//SQL.append("concat ");
				//SQL.append("( ");
				//SQL.append("  concat ");
				//SQL.append("  ( ");
		   		
		   		/*
				StringBuffer attributesSB = new StringBuffer();
				int i = 0;
				for(ContentTypeAttribute attribute : attributes)
				{
					if(!attribute.getName().equals("ComponentStructure"))
					{
						attributesSB.append("   \n '");
						if(i > 0)
							attributesSB.append("igcomma");
					
						attributesSB.append("" + attribute.getName() + "='||SUBSTR(verValue,INSTR(verValue,'<" + attribute.getName() + "><![CDATA[')+" + (attribute.getName().length() + 11) + ",INSTR(verValue,']]></" + attribute.getName() + ">') - (INSTR(verValue,'<" + attribute.getName() + "><![CDATA[') + " + (attribute.getName().length() + 11) + ")) ||");
					}
					i++;
				}
				attributesSB.deleteCharAt(attributesSB.length()-1).deleteCharAt(attributesSB.length()-1);
				//System.out.println("attributesSB:" + attributesSB);
				SQL.append(attributesSB.toString());
				*/
		   		SQL.append(" cv.verValue ");
				//SQL.append("  ) ");
				//SQL.append(") AS attributes ");
				SQL.append(" AS attributes ");
				SQL.append("from ");
				SQL.append("(");
				SQL.append("select sn1.siNoId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiNoId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siNoTypeDefId, sn1.creator from cmSiNo sn1 where sn1.parentsiNoId = " + siteNodeId + " ");
				if(levelsToPopulate > 1)
				{
					SQL.append("UNION ");
					SQL.append("select sn2.siNoId, sn2.name, sn2.publishDateTime, sn2.expireDateTime, sn2.isBranch, sn2.parentsiNoId, sn2.metaInfoContentId, sn2.repositoryId, sn2.siNoTypeDefId, sn2.creator ");
					SQL.append("from ");
					SQL.append("(");
					SQL.append("  select sn1.siNoId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiNoId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siNoTypeDefId, sn1.creator from cmSiNo sn1 where sn1.parentsiNoId = " + siteNodeId + " ");
					SQL.append(") sn1, cmSiNo sn2 where sn2.parentsiNoId = sn1.siNoId ");
				}
				if(levelsToPopulate > 2)
				{
					SQL.append("UNION ");
					SQL.append("select sn3.siNoId, sn3.name, sn3.publishDateTime, sn3.expireDateTime, sn3.isBranch, sn3.parentsiNoId, sn3.metaInfoContentId, sn3.repositoryId, sn3.siNoTypeDefId, sn3.creator ");
					SQL.append("from ");
					SQL.append(" (");
					SQL.append("  select sn2.siNoId, sn2.name, sn2.publishDateTime, sn2.expireDateTime, sn2.isBranch, sn2.parentsiNoId, sn2.metaInfoContentId, sn2.repositoryId, sn2.siNoTypeDefId, sn2.creator ");
					SQL.append("  from ");
					SQL.append("  (");
					SQL.append("    select sn1.siNoId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiNoId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siNoTypeDefId, sn1.creator from cmSiNo sn1 where sn1.parentsiNoId = " + siteNodeId + " ");
					SQL.append("  ) sn1, cmSiNo sn2 where sn2.parentsiNoId = sn1.siNoId ");
					SQL.append(") sn2, cmSiNo sn3 where sn3.parentsiNoId = sn2.siNoId ");
				}
				if(levelsToPopulate > 3)
				{
					SQL.append("UNION ");
					SQL.append("select sn4.siNoId, sn4.name, sn4.publishDateTime, sn4.expireDateTime, sn4.isBranch, sn4.parentsiNoId, sn4.metaInfoContentId, sn4.repositoryId, sn4.siNoTypeDefId, sn4.creator ");
					SQL.append("from ");
					SQL.append("(");
					SQL.append("  select sn3.siNoId, sn3.name, sn3.publishDateTime, sn3.expireDateTime, sn3.isBranch, sn3.parentsiNoId, sn3.metaInfoContentId, sn3.repositoryId, sn3.siNoTypeDefId, sn3.creator ");
					SQL.append("  from ");
					SQL.append("  (");
					SQL.append("    select sn2.siNoId, sn2.name, sn2.publishDateTime, sn2.expireDateTime, sn2.isBranch, sn2.parentsiNoId, sn2.metaInfoContentId, sn2.repositoryId, sn2.siNoTypeDefId, sn2.creator ");
					SQL.append("    from ");
					SQL.append("   (");
					SQL.append("     select sn1.siNoId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiNoId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siNoTypeDefId, sn1.creator from cmSiNo sn1 where sn1.parentsiNoId = " + siteNodeId + " ");
					SQL.append("   ) sn1, cmSiNo sn2 where sn2.parentsiNoId = sn1.siNoId ");
					SQL.append(" ) sn2, cmSiNo sn3 where sn3.parentsiNoId = sn2.siNoId ");
					SQL.append(" ) sn3, cmSiNo sn4 where sn4.parentsiNoId = sn3.siNoId ");
				}
				
				SQL.append(") sn, cmSiNoVer snv, cmContVer cv ");
				SQL.append("where ");
				SQL.append("snv.siNoId = sn.siNoId AND ");
				SQL.append("cv.contId = sn.metaInfoContentId AND ");
				SQL.append("cv.contVerId in (select max(contVerId) from cmContVer cv2 where cv2.contId=cv.contId group by cv2.languageId) ");
				SQL.append("AND snv.siNoVerId = ( ");
				SQL.append("  select max(siNoVerId) from cmSiNoVer snv2 ");
				SQL.append("  WHERE ");
				SQL.append("  snv2.siNoId = snv.siNoId AND ");
				SQL.append("  snv2.isActive = $1 AND snv2.stateId >= $2 ");
				SQL.append(") ");
				SQL.append("order by sn.parentSiNoId asc, sn.name ASC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");	    	
			}
	    	else
	    	{
		   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.parentsiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentsiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, cv.languageId, ");
		   		/*
		   		SQL.append("concat ");
				SQL.append("( ");
				SQL.append("  concat ");
				SQL.append("  ( ");
				
				StringBuffer attributesSB = new StringBuffer();
				int i = 0;
				for(ContentTypeAttribute attribute : attributes)
				{
					if(!attribute.getName().equals("ComponentStructure"))
					{
						attributesSB.append("   \n '");
						if(i > 0)
							attributesSB.append("igcomma");
					
						attributesSB.append("" + attribute.getName() + "=',SUBSTR(versionValue,INSTR(versionValue,'<" + attribute.getName() + "><![CDATA[')+" + (attribute.getName().length() + 11) + ",INSTR(versionValue,']]></" + attribute.getName() + ">') - (INSTR(versionValue,'<" + attribute.getName() + "><![CDATA[') + " + (attribute.getName().length() + 11) + ")),");
					}
					i++;
				}
				attributesSB.deleteCharAt(attributesSB.length()-1);
				//System.out.println("attributesSB:" + attributesSB);
				SQL.append(attributesSB.toString());
				
				SQL.append("  ) ");
				SQL.append(") AS attributes ");
				*/
		   		SQL.append(" cv.versionValue AS attributes ");
		   		
				SQL.append("from ");
				SQL.append("(");
				SQL.append("select sn1.siteNodeId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiteNodeId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siteNodeTypeDefinitionId, sn1.creator from cmSiteNode sn1 where sn1.parentsiteNodeId = " + siteNodeId + " ");
				if(levelsToPopulate > 1)
				{
					SQL.append("UNION ");
					SQL.append("select sn2.siteNodeId, sn2.name, sn2.publishDateTime, sn2.expireDateTime, sn2.isBranch, sn2.parentsiteNodeId, sn2.metaInfoContentId, sn2.repositoryId, sn2.siteNodeTypeDefinitionId, sn2.creator ");
					SQL.append("from ");
					SQL.append("(");
					SQL.append("  select sn1.siteNodeId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiteNodeId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siteNodeTypeDefinitionId, sn1.creator from cmSiteNode sn1 where sn1.parentsiteNodeId = " + siteNodeId + " ");
					SQL.append(") sn1, cmSiteNode sn2 where sn2.parentsiteNodeId = sn1.siteNodeId ");
				}
				if(levelsToPopulate > 2)
				{
					SQL.append("UNION ");
					SQL.append("select sn3.siteNodeId, sn3.name, sn3.publishDateTime, sn3.expireDateTime, sn3.isBranch, sn3.parentsiteNodeId, sn3.metaInfoContentId, sn3.repositoryId, sn3.siteNodeTypeDefinitionId, sn3.creator ");
					SQL.append("from ");
					SQL.append(" (");
					SQL.append("  select sn2.siteNodeId, sn2.name, sn2.publishDateTime, sn2.expireDateTime, sn2.isBranch, sn2.parentsiteNodeId, sn2.metaInfoContentId, sn2.repositoryId, sn2.siteNodeTypeDefinitionId, sn2.creator ");
					SQL.append("  from ");
					SQL.append("  (");
					SQL.append("    select sn1.siteNodeId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiteNodeId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siteNodeTypeDefinitionId, sn1.creator from cmSiteNode sn1 where sn1.parentsiteNodeId = " + siteNodeId + " ");
					SQL.append("  ) sn1, cmSiteNode sn2 where sn2.parentsiteNodeId = sn1.siteNodeId ");
					SQL.append(") sn2, cmSiteNode sn3 where sn3.parentsiteNodeId = sn2.siteNodeId ");
				}
				if(levelsToPopulate > 3)
				{
					SQL.append("UNION ");
					SQL.append("select sn4.siteNodeId, sn4.name, sn4.publishDateTime, sn4.expireDateTime, sn4.isBranch, sn4.parentsiteNodeId, sn4.metaInfoContentId, sn4.repositoryId, sn4.siteNodeTypeDefinitionId, sn4.creator ");
					SQL.append("from ");
					SQL.append("(");
					SQL.append("  select sn3.siteNodeId, sn3.name, sn3.publishDateTime, sn3.expireDateTime, sn3.isBranch, sn3.parentsiteNodeId, sn3.metaInfoContentId, sn3.repositoryId, sn3.siteNodeTypeDefinitionId, sn3.creator ");
					SQL.append("  from ");
					SQL.append("  (");
					SQL.append("    select sn2.siteNodeId, sn2.name, sn2.publishDateTime, sn2.expireDateTime, sn2.isBranch, sn2.parentsiteNodeId, sn2.metaInfoContentId, sn2.repositoryId, sn2.siteNodeTypeDefinitionId, sn2.creator ");
					SQL.append("    from ");
					SQL.append("   (");
					SQL.append("     select sn1.siteNodeId, sn1.name, sn1.publishDateTime, sn1.expireDateTime, sn1.isBranch, sn1.parentsiteNodeId, sn1.metaInfoContentId, sn1.repositoryId, sn1.siteNodeTypeDefinitionId, sn1.creator from cmSiteNode sn1 where sn1.parentsiteNodeId = " + siteNodeId + " ");
					SQL.append("   ) sn1, cmSiteNode sn2 where sn2.parentsiteNodeId = sn1.siteNodeId ");
					SQL.append(" ) sn2, cmSiteNode sn3 where sn3.parentsiteNodeId = sn2.siteNodeId ");
					SQL.append(" ) sn3, cmSiteNode sn4 where sn4.parentsiteNodeId = sn3.siteNodeId ");
				}
				
				SQL.append(") sn, cmSiteNodeVersion snv, cmContentVersion cv ");
				SQL.append("where ");
				SQL.append("snv.siteNodeId = sn.siteNodeId AND ");
				SQL.append("cv.contentId = sn.metaInfoContentId AND ");
				SQL.append("cv.contentVersionId in (select max(contentVersionId) from cmContentVersion cv2 where cv2.contentId=cv.contentId group by cv2.languageId) ");
				SQL.append("AND snv.siteNodeVersionId = ( ");
				SQL.append("  select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
				SQL.append("  WHERE ");
				SQL.append("  snv2.siteNodeId = snv.siteNodeId AND ");
				SQL.append("  snv2.isActive = $1 AND snv2.stateId >= $2 ");
				SQL.append(") ");
				SQL.append("order by sn.parentsiteNodeId asc, sn.name ASC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
	    	}
	    	
	    	logger.info("\n\n" + SQL);
	    	logger.info("Running SQL QUERY for children to " + siteNodeId + " and possibly below");
	    	//Thread.dumpStack();
	    	
	    	//logger.info("SQL:" + SQL);
	    	//logger.info("siteNodeId:" + siteNodeId);
	    	OQLQuery oql = db.getOQLQuery(SQL.toString());
			//oql.bind(siteNodeId);
			oql.bind(true);
			oql.bind(getOperatingMode());
	    	
	    	QueryResults results = oql.execute(Database.READONLY);
	    	t.printElapsedTime("Query took");
	    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getChildSiteNodes part 1", t.getElapsedTime());
	    	
	    	Map<Integer,SiteNodeVO> allSiteNodeVOMap = new HashMap<Integer,SiteNodeVO>();
	    	
	    	Integer parentSiteNodeId = null;
	    	siteNodeVOList = new ArrayList<SiteNodeVO>();
			CacheController.cacheObjectInAdvancedCache("childPagesCache", ""+siteNodeId, siteNodeVOList, new String[] {CacheController.getPooledString(3, siteNodeId)}, true);
			//populatedSiteNodeVOList.put(siteNodeId, siteNodeVOList);

			String groupKey1 = null;
			String groupKey2 = null;
			
			while (results.hasMore()) 
	        {
	        	SiteNode siteNode = (SiteNode)results.next();
	        	
				String siteNodeCacheKey = "" + siteNode.getValueObject().getId();
				CacheController.cacheObjectInAdvancedCache("siteNodeCache", siteNodeCacheKey, siteNode.getValueObject());

        		String versionValue = siteNode.getValueObject().getAttributes();
        		Integer contentId = siteNode.getValueObject().getMetaInfoContentId();
        		Integer languageId = siteNode.getValueObject().getLanguageId();
        		Integer contentVersionId = siteNode.getValueObject().getContentVersionId();
        		if(versionValue == null)
        		{
        			logger.info("Null version for " + siteNode.getSiteNodeId() + ":" + siteNode.getValueObject().getSiteNodeVersionId());
        		}
        		else
        		{
		        	groupKey1 = CacheController.getPooledString(2, contentVersionId);
		        	groupKey2 = CacheController.getPooledString(1, contentId);
		        	
		        	for(ContentTypeAttribute attribute : attributes)
					{
		        		if(!attribute.getName().equals("ComponentStructure"))
		        		{
		        			String attributeKey = "" + siteNode.getId() + "_" + languageId + "_" + attribute.getName();
		        			String attributeKeyContentId = "c_" + siteNode.getMetaInfoContentId() + "_" + languageId + "_" + attribute.getName();
		        			//System.out.println("Caching empty on " + attributeKey);
		    	        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKey, "", new String[]{groupKey1, groupKey2}, true);
		    	        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKeyContentId, "", new String[]{groupKey1, groupKey2}, true);
		    	        }
					}
					
		        	for(ContentTypeAttribute attribute : attributes)
	        		{
		        		//if(!attribute.getName().equals("ComponentStructure"))
		        		//{
		        			String value = ContentDeliveryController.getContentDeliveryController().getAttributeValue(versionValue, attribute.getName(), false);
		
		        			String attributeKey = "" + siteNode.getId() + "_" + languageId + "_" + attribute.getName();
		        			String attributeKeyContentId = "c_" + siteNode.getMetaInfoContentId() + "_" + languageId + "_" + attribute.getName();
		        			//System.out.println("Caching " + value + " on " + attributeKey);
		    	        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKey, value, new String[]{groupKey1, groupKey2}, true);
		    	        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKeyContentId, value, new String[]{groupKey1, groupKey2}, true);
		    	        	//}
	        		}    
        		}
	        	/*
	        	if(attributesString != null)
	        	{
	        		String[] attributesArray = attributesString.split("igcomma");
	        		for(String attr : attributesArray)
	        		{
	        			if(attr != null && !attr.equals(""))
	        			{
		        			String name = attr.substring(0, attr.indexOf("="));
		        			String value = attr.substring(attr.indexOf("=") + 1);
		
		        			String attributeKey = "" + siteNode.getId() + "_" + languageId + "_" + name;
		        			String attributeKeyContentId = "c_" + siteNode.getMetaInfoContentId() + "_" + languageId + "_" + name;
		        			//System.out.println("Caching " + name + "=" + value + " on " + attributeKey);
		    	        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKey, value, new String[]{groupKey1, groupKey2}, true);
		    	        	CacheController.cacheObjectInAdvancedCache("metaInfoContentAttributeCache", attributeKeyContentId, value, new String[]{groupKey1, groupKey2}, true);
	        			}
	        		}        		
	        	}
	        	else
	        		System.out.println("Error null on " + siteNode.getId());
	        	*/
	        	//if(allSiteNodeVOMap.get(siteNode.getId()) != null)
        		//{
	        	//	allSiteNodeVOMap.get(siteNode.getId()).addAttributes(siteNode.getValueObject().getLanguageId(), siteNode.getValueObject().getAttributes());
        		//	continue;
        		//}
	        	
	        	allSiteNodeVOMap.put(siteNode.getId(), siteNode.getValueObject());
	        	//System.out.println("siteNode:" + siteNode.getName());
				if(isValidSiteNode(siteNode, db))
	        	{
					//System.out.println("Caching empty list initially on " + siteNode.getId());
					//CacheController.cacheObjectInAdvancedCache("childPagesCache", ""+siteNode.getId(), new ArrayList<SiteNodeVO>(), new String[] {groupKey1, groupKey2, CacheController.getPooledString(3, siteNode.getId())}, true);
					//populatedSiteNodeVOList.put(siteNode.getId(), new ArrayList<SiteNodeVO>());
					if(parentSiteNodeId != null && !siteNode.getValueObject().getParentSiteNodeId().equals(parentSiteNodeId))
					{
						//System.out.println("Caching list:" + siteNodeVOList + " on " + parentSiteNodeId);
						CacheController.cacheObjectInAdvancedCache("childPagesCache", ""+parentSiteNodeId, siteNodeVOList, new String[] {groupKey1, groupKey2, CacheController.getPooledString(3, parentSiteNodeId)}, true);
						//populatedSiteNodeVOList.put(parentSiteNodeId, siteNodeVOList);
						siteNodeVOList = new ArrayList<SiteNodeVO>();
					}
					parentSiteNodeId = siteNode.getValueObject().getParentSiteNodeId();
					siteNodeVOList.add(siteNode.getValueObject());
				}
	    	}
			//System.out.println("Caching list:" + siteNodeVOList + " on " + parentSiteNodeId);
			if(groupKey1 == null) groupKey1 = "";
			if(groupKey2 == null) groupKey2 = "";
			CacheController.cacheObjectInAdvancedCache("childPagesCache", ""+parentSiteNodeId, siteNodeVOList, new String[] {groupKey1, groupKey2, CacheController.getPooledString(3, parentSiteNodeId)}, true);
			//populatedSiteNodeVOList.put(parentSiteNodeId, siteNodeVOList);
	    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getChildSiteNodes part 2", t.getElapsedTime());
	    	t.printElapsedTime("Read took");

			results.close();
			oql.close();
    	
			//System.out.println("SIZE:" + populatedSiteNodeVOList.size());
    	}
    	
    	return (List<SiteNodeVO>)CacheController.getCachedObjectFromAdvancedCache("childPagesCache", "" + siteNodeId); //populatedSiteNodeVOList.get(siteNodeId);
	}
	
	/**
	 * This method returns the list of siteNodeVO which is children to this one.
	 */
	
	public List getChildSiteNodesOneLevel(Database db, Integer siteNodeId, boolean showHidden, String nameFilter, Boolean showLanguageDisabled) throws SystemException, Exception
	{
		logger.info("getChildSiteNodes:" + siteNodeId);

    	if(siteNodeId == null)
		{
			return null;
		}
    	String keyTop = "" + siteNodeId + "_" + showHidden + "_" + showLanguageDisabled + (nameFilter == null ? "" : "_" + nameFilter) + "_" + languageId;
    	if(!CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
    		keyTop = "" + siteNodeId + "_" + showHidden + "_" + showLanguageDisabled + (nameFilter == null ? "" : "_" + nameFilter);
    		
    	List<SiteNodeVO> siteNodeVOList = (List<SiteNodeVO>)CacheController.getCachedObjectFromAdvancedCache("childPagesCache", keyTop); //populatedSiteNodeVOList.get(siteNodeId);
    	if(siteNodeVOList != null)
    	{
    		//System.out.println("Returning cached");
    		return siteNodeVOList;
    	}
    	else
    	{
	        String key = "" + siteNodeId + "_" + showHidden + "_" + showLanguageDisabled + (nameFilter == null ? "" : "_" + nameFilter) + "_" + languageId;
	    	if(!CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
	    		key = "" + siteNodeId + "_" + showHidden + "_" + showLanguageDisabled + (nameFilter == null ? "" : "_" + nameFilter);
	    	
	    	logger.info("key in getChildSiteNodes:" + key);
			siteNodeVOList = (List)CacheController.getCachedObjectFromAdvancedCache("childSiteNodesCache", key);
	
			if(siteNodeVOList == null)
			{
				SiteNodeVO parentSiteNodeVO = getSiteNodeVO(db, siteNodeId);
				if(parentSiteNodeVO != null && parentSiteNodeVO.getChildCount() != null && parentSiteNodeVO.getChildCount() == 0)
				{
					logger.info("Skipping node as it has no children...");
					return new ArrayList();
				}
			}
	
			
			if(siteNodeVOList != null)
			{
				logger.info("There was a cached list of child sitenodes:" + siteNodeVOList.size());
			}
			else
			{
		   		//logger.info("Querying for children to siteNode " + siteNodeId);
		   		Timer t = new Timer();
		   		
		        siteNodeVOList = new ArrayList();
			    
		        StringBuffer SQL = new StringBuffer();
		    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
		    	{
			   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiNo sn, cmSiNoVer snv ");
			   		SQL.append("where ");
			   		SQL.append("sn.parentSiNoId = $1 ");
			   		SQL.append("AND sn.isDeleted = $2 ");
			   		SQL.append("AND snv.siNoId = sn.siNoId ");
			   		SQL.append("AND snv.siNoVerId = ( ");
			   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
			   		SQL.append("	WHERE ");
			   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
			   		SQL.append("	snv2.isActive = $3 AND snv2.stateId >= $4 ");
			   		SQL.append("	) ");
			   		if(nameFilter != null && !nameFilter.equals(""))
			   			SQL.append(" AND sn.name LIKE $5 ");
			   		//if(!showHidden)
			   		//	SQL.append(" AND snv.isHidden = 0 ");
			   		SQL.append("order by snv.sortOrder, sn.name ASC, sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
		    	}
		    	else
		    	{
			   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime, 0 AS languageId, '' as attributes from cmSiteNode sn, cmSiteNodeVersion snv ");
			   		SQL.append("where ");
			   		SQL.append("sn.parentSiteNodeId = $1 ");
			   		SQL.append("AND sn.isDeleted = $2 ");
			   		SQL.append("AND snv.siteNodeId = sn.siteNodeId ");
			   		SQL.append("AND snv.siteNodeVersionId = ( ");
			   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
			   		SQL.append("	WHERE ");	
			   		SQL.append("	snv2.siteNodeId = snv.siteNodeId AND ");
			   		SQL.append("	snv2.isActive = $3 AND snv2.stateId >= $4 ");
			   		SQL.append("	) ");
			   		if(nameFilter != null && !nameFilter.equals(""))
			   			SQL.append(" AND sn.name LIKE $5 ");
			   		//if(!showHidden)
			   		//	SQL.append(" AND snv.isHidden = 0 ");
			   		SQL.append("order by snv.sortOrder, sn.name ASC, sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
		    	}
	
		    	logger.info("SQL:" + SQL);
		    	//logger.info("siteNodeId:" + siteNodeId);
		    	OQLQuery oql = db.getOQLQuery(SQL.toString());
				oql.bind(siteNodeId);
				oql.bind(false);
				oql.bind(true);
				oql.bind(getOperatingMode());
				if(nameFilter != null && !nameFilter.equals(""))
					oql.bind(nameFilter);
		    	
		    	QueryResults results = oql.execute(Database.READONLY);
		    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getChildSiteNodes part 1", t.getElapsedTime());
		    	
				while (results.hasMore()) 
		        {
		        	SiteNode siteNode = (SiteNode)results.next();
		        	t.getElapsedTime();
		        	
		        	if(isValidSiteNode(siteNode, db))
		        	{
		        		if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties() && languageId != null && deliveryContext != null)
		        		{
				        	Boolean isLanguageAvailable = true;
		        			if(!showLanguageDisabled)
		        				isLanguageAvailable = SiteNodeController.getController().getIsLanguageAvailable(siteNode.getId(), languageId, db, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()));

		        			if(!isLanguageAvailable)
							{
								continue;
							}
							else
							{
								String localizedIsHidden = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, siteNode.getMetaInfoContentId(), languageId, "HideInNavigation", siteNode.getId(), true, deliveryContext, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()), false, true);
								String localizedSortOrder = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, siteNode.getMetaInfoContentId(), languageId, "SortOrder", siteNode.getId(), true, deliveryContext, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()), false, true);
								if(localizedSortOrder == null || localizedSortOrder.equals(""))
									localizedSortOrder = ContentDeliveryController.getContentDeliveryController().getContentAttribute(db, siteNode.getMetaInfoContentId(), languageId, "sortOrder", siteNode.getId(), true, deliveryContext, UserControllerProxy.getController().getUser(CmsPropertyHandler.getAnonymousUser()), false, true);
								
					        	if(localizedIsHidden != null/* && !localizedIsHidden.equals("")*/)
								{
									if(localizedIsHidden.equals("true"))
										siteNode.getValueObject().setLocalizedIsHidden(true);
									else
										siteNode.getValueObject().setLocalizedIsHidden(false);
								}
								
								if(localizedSortOrder != null && !localizedSortOrder.equals(""))
								{
									try
									{
										siteNode.getValueObject().setLocalizedSortOrder(new Integer(localizedSortOrder));										
									}
									catch(Exception e)
									{
										logger.warn("The sitenode " + siteNode.getName() + " (ID: " + siteNode.getId() + ") had a bad localizedSortOrder:" + localizedSortOrder + ". Error:" + e.getMessage());
									}
								}
							}
						}
						
						if(siteNode.getValueObject().getIsHidden() == null || (siteNode.getValueObject().getIsHidden().booleanValue() == false || showHidden))
							siteNodeVOList.add(siteNode.getValueObject());
					}
		    	}
		    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getChildSiteNodes part 2", t.getElapsedTime());
	
				results.close();
				oql.close();
		        
				if(CmsPropertyHandler.getAllowLocalizedSortAndVisibilityProperties())
					Collections.sort(siteNodeVOList, new ReflectionComparator("sortOrder"));
				
				CacheController.cacheObjectInAdvancedCache("childSiteNodesCache", key, siteNodeVOList, new String[] {CacheController.getPooledString(3, siteNodeId)}, true);
			}
    	}
    	
		return siteNodeVOList;	
	}
	
	/**
	 * This method returns the list of siteNodeVO which is children to this one and has a given node name.
	 */
	
	public List getChildSiteNodeWithName(Database db, Integer siteNodeId, String name) throws SystemException, Exception
	{
		//logger.warn("getChildSiteNodes:" + siteNodeId);

    	if(siteNodeId == null)
		{
			return null;
		}
    	
        String key = "" + siteNodeId + "_" + name + "_" + false;
        logger.info("key in getChildSiteNodes:" + key);
		List siteNodeVOList = (List)CacheController.getCachedObjectFromAdvancedCache("childSiteNodesCache", key);

		if(siteNodeVOList == null)
		{
			SiteNodeVO parentSiteNodeVO = getSiteNodeVO(db, siteNodeId);
			if(parentSiteNodeVO != null && parentSiteNodeVO.getChildCount() != null && parentSiteNodeVO.getChildCount() == 0)
			{
				//logger.info("Skipping node as it has no children...");
				return new ArrayList();
			}
		}

		
		if(siteNodeVOList != null)
		{
			logger.info("There was a cached list of child sitenodes:" + siteNodeVOList.size());
		}
		else
		{
	   		//logger.info("Querying for children to siteNode " + siteNodeId);
	   		Timer t = new Timer();
	   		
	        siteNodeVOList = new ArrayList();
		    
	        StringBuffer SQL = new StringBuffer();
	    	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
	    	{
		   		SQL.append("CALL SQL select sn.siNoId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiNoId, sn.metaInfoContentId, sn.repositoryId, sn.siNoTypeDefId, sn.creator, (select count(*) from cmSiNo sn2 where sn2.parentSiNoId = sn.siNoId) AS childCount, snv.siNoVerId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime from cmSiNo sn, cmSiNoVer snv ");
		   		SQL.append("where ");
		   		SQL.append("sn.parentSiNoId = $1 ");
		   		SQL.append("AND sn.isDeleted = $2 ");
		   		SQL.append("AND snv.siNoId = sn.siNoId ");
		   		SQL.append("AND snv.siNoVerId = ( ");
		   		SQL.append("	select max(siNoVerId) from cmSiNoVer snv2 ");
		   		SQL.append("	WHERE ");
		   		SQL.append("	snv2.siNoId = snv.siNoId AND ");
		   		SQL.append("	snv2.isActive = $3 AND snv2.stateId >= $4 ");
		   		SQL.append("	) ");
		   		SQL.append("AND sn.name = $5 ");
		   		SQL.append("order by snv.sortOrder ASC, sn.name ASC, sn.siNoId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");
	    	}
	    	else
	    	{
		   		SQL.append("CALL SQL select sn.siteNodeId, sn.name, sn.publishDateTime, sn.expireDateTime, sn.isBranch, sn.isDeleted, sn.parentSiteNodeId, sn.metaInfoContentId, sn.repositoryId, sn.siteNodeTypeDefinitionId, sn.creator, (select count(*) from cmSiteNode sn2 where sn2.parentSiteNodeId = sn.siteNodeId) AS childCount, snv.siteNodeVersionId, snv.sortOrder, snv.isHidden, snv.stateId, snv.isProtected, snv.versionModifier, snv.modifiedDateTime from cmSiteNode sn, cmSiteNodeVersion snv ");
		   		SQL.append("where ");
		   		SQL.append("sn.parentSiteNodeId = $1 ");
		   		SQL.append("AND sn.isDeleted = $2 ");
		   		SQL.append("AND snv.siteNodeId = sn.siteNodeId ");
		   		SQL.append("AND snv.siteNodeVersionId = ( ");
		   		SQL.append("	select max(siteNodeVersionId) from cmSiteNodeVersion snv2 ");
		   		SQL.append("	WHERE ");	
		   		SQL.append("	snv2.siteNodeId = snv.siteNodeId AND ");
		   		SQL.append("	snv2.isActive = $3 AND snv2.stateId >= $4 ");
		   		SQL.append("AND sn.name = $5 ");
		   		SQL.append("	) ");
		   		SQL.append("order by snv.sortOrder ASC, sn.name ASC, sn.siteNodeId DESC AS org.infoglue.cms.entities.structure.impl.simple.SmallestSiteNodeImpl");    		
	    	}

	    	logger.info("SQL:" + SQL);
	    	logger.info("" + siteNodeId + ":" + false + ":" + true + ":" + getOperatingMode());
	    	//logger.info("SQL:" + SQL);
	    	//logger.info("siteNodeId:" + siteNodeId);
	    	OQLQuery oql = db.getOQLQuery(SQL.toString());
			oql.bind(siteNodeId);
			oql.bind(false);
			oql.bind(true);
			oql.bind(getOperatingMode());
			oql.bind(name);
	    	
	    	QueryResults results = oql.execute(Database.READONLY);
	    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getChildSiteNodes part 1", t.getElapsedTime());
	    	
			while (results.hasMore()) 
	        {
	        	SiteNode siteNode = (SiteNode)results.next();
				if(isValidSiteNode(siteNode, db))
	        	{
	        		siteNodeVOList.add(siteNode.getValueObject());
				}
	    	}
	    	//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getChildSiteNodes part 2", t.getElapsedTime());

			results.close();
			oql.close();
			
	        /*
	        OQLQuery oql = db.getOQLQuery( "SELECT s FROM org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl s WHERE s.parentSiteNode.siteNodeId = $1 ORDER BY s.siteNodeId");
			oql.bind(siteNodeId);
	        */
	        /*
		    SiteNode siteNode = getSiteNode(db, siteNodeId);
	        
			Iterator childrenIterator = siteNode.getChildSiteNodes().iterator();
			while (childrenIterator.hasNext()) 
	        {
	        	SiteNode childSiteNode = (SiteNode)childrenIterator.next();
				
	        	if(isValidSiteNode(childSiteNode, db))
	        	    siteNodeVOList.add(childSiteNode.getValueObject());
			}
	        */
	        
			CacheController.cacheObjectInAdvancedCache("childSiteNodesCache", key, siteNodeVOList, new String[] {CacheController.getPooledString(3, siteNodeId)}, true);
		}
				
		return siteNodeVOList;	
	}

	
	/**
	 * This method returns a sorted list of qualifyers.
	 */
	
	private List getBindingQualifyers(Integer serviceBindingId, Database db) throws SystemException, Bug, Exception
	{
		List qualifyers = new ArrayList();
		
		OQLQuery oql = db.getOQLQuery( "SELECT q FROM org.infoglue.cms.entities.structure.impl.simple.QualifyerImpl q WHERE q.serviceBinding.serviceBindingId = $1 ORDER BY q.sortOrder");
		oql.bind(serviceBindingId);
		
    	QueryResults results = oql.execute(Database.READONLY);
		while(results.hasMore()) 
        {
        	Qualifyer qualifyer = (Qualifyer)results.next();
			qualifyers.add(qualifyer);
		}
		
		results.close();
		oql.close();

		return qualifyers;
	}
	
	
	/**
	 * This method validates that right now is between publishdate and expiredate.
	 */
	
	private boolean isValidOnDates(Date publishDate, Date expireDate)
	{
		boolean isValid = true;
		Date now = new Date();
		
		if(publishDate.after(now) || expireDate.before(now))
			isValid = false;
		
		return isValid;
	}		
	
	/**
	 * Returns if a siteNode is between dates and has a siteNode version suitable for this delivery mode.
	 * @throws Exception
	 */
	
	public boolean isValidSiteNode(Database db, Integer siteNodeId) throws Exception
	{
		boolean isValidSiteNode = false;
		
		SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db); 
		isValidSiteNode = isValidSiteNode(siteNode, db);
    			
		return isValidSiteNode;					
	}

	/**
	 * Returns if a siteNode is between dates and has a SiteNode version suitable for this delivery mode.
	 * @throws Exception
	 */

	public boolean isValidSiteNode(SiteNode siteNode, Database db) throws Exception
	{
		boolean isValidContent = false;
		
		if(isValidOnDates(siteNode.getPublishDateTime(), siteNode.getExpireDateTime()))
		{
		    if(this.getLatestActiveSiteNodeVersionVO(siteNode.getId(), db) != null)
		        isValidContent = true;
		    /*
		    if(siteNode.getValueObject().getSiteNodeVersionId() != null)
				isValidContent = true;
			else if(this.getLatestActiveSiteNodeVersionVO(siteNode.getId(), db) != null)
		        isValidContent = true;
			*/
		}
		
		if(isValidContent && !siteNode.getExpireDateTime().before(new Date()))
		{
		    Date expireDateTimeCandidate = siteNode.getExpireDateTime();
		    if(CacheController.expireDateTime == null || expireDateTimeCandidate.before(CacheController.expireDateTime))
			{
			    CacheController.expireDateTime = expireDateTimeCandidate;
			}
		}
		else if(siteNode.getPublishDateTime().after(new Date())) //If it's a publish date to come we consider it
		{
		    Date publishDateTimeCandidate = siteNode.getPublishDateTime();
		    if(CacheController.publishDateTime == null || publishDateTimeCandidate.after(CacheController.publishDateTime))
			{
			    CacheController.publishDateTime = publishDateTimeCandidate;
			}
		}    	
		
		return isValidContent;					
	}
	
	/**
	 * Returns if a siteNode is between dates and has a SiteNode version suitable for this delivery mode.
	 * @throws Exception
	 */

	public boolean isValidSiteNode(SiteNodeVO siteNode, Database db) throws Exception
	{
		boolean isValidContent = false;
		
		if(isValidOnDates(siteNode.getPublishDateTime(), siteNode.getExpireDateTime()))
		{
		    if(this.getLatestActiveSiteNodeVersionVO(siteNode.getId(), db) != null)
		        isValidContent = true;
		}
		
		if(isValidContent && !siteNode.getExpireDateTime().before(new Date()))
		{
		    Date expireDateTimeCandidate = siteNode.getExpireDateTime();
		    if(CacheController.expireDateTime == null || expireDateTimeCandidate.before(CacheController.expireDateTime))
			{
			    CacheController.expireDateTime = expireDateTimeCandidate;
			}
		}
		else if(siteNode.getPublishDateTime().after(new Date())) //If it's a publish date to come we consider it
		{
		    Date publishDateTimeCandidate = siteNode.getPublishDateTime();
		    if(CacheController.publishDateTime == null || publishDateTimeCandidate.after(CacheController.publishDateTime))
			{
			    CacheController.publishDateTime = publishDateTimeCandidate;
			}
		}    	
		
		return isValidContent;					
	}
	
	/**
	 * This method just sorts the list of qualifyers on sortOrder.
	 */
	
	private List sortQualifyers(Collection qualifyers)
	{
		List sortedQualifyers = new ArrayList();

		try
		{		
			Iterator iterator = qualifyers.iterator();
			while(iterator.hasNext())
			{
				Qualifyer qualifyer = (Qualifyer)iterator.next();
				int index = 0;
				Iterator sortedListIterator = sortedQualifyers.iterator();
				while(sortedListIterator.hasNext())
				{
					Qualifyer sortedQualifyer = (Qualifyer)sortedListIterator.next();
					if(sortedQualifyer.getSortOrder().intValue() > qualifyer.getSortOrder().intValue())
			    	{
			    		break;
			    	}
			    	index++;
				}
				sortedQualifyers.add(index, qualifyer);
			    					
			}
		}
		catch(Exception e)
		{
			logger.warn("The sorting of qualifyers failed:" + e.getMessage(), e);
		}
			
		return sortedQualifyers;
	}

}
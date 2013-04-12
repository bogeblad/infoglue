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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightGroupVO;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightRoleVO;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.AccessRightUserVO;
import org.infoglue.cms.entities.management.AccessRightVO;
import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.QualifyerVO;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallQualifyerImpl;
import org.infoglue.cms.entities.structure.impl.simple.SmallServiceBindingImpl;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

public class SiteNodeStateController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SiteNodeStateController.class.getName());

    /**
	 * Factory method
	 */

	public static SiteNodeStateController getController()
	{
		return new SiteNodeStateController();
	}

	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */

    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, Integer stateId, String versionComment, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
    	return changeState(oldSiteNodeVersionId, stateId, versionComment, overrideVersionModifyer, null, infoGluePrincipal, siteNodeId, resultingEvents);
    }

	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */
    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, SiteNodeVO siteNodeVO, Integer stateId, String versionComment, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
    	return changeState(oldSiteNodeVersionId, siteNodeVO, stateId, versionComment, overrideVersionModifyer, null, infoGluePrincipal, resultingEvents);
    }

	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */
    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, Integer stateId, String versionComment, boolean overrideVersionModifyer, String recipientFilter, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
    	SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
    	return changeState(oldSiteNodeVersionId, siteNodeVO, stateId, versionComment, overrideVersionModifyer, recipientFilter, infoGluePrincipal, resultingEvents);
    }
    
	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */
	
    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, SiteNodeVO siteNodeVO, Integer stateId, String versionComment, boolean overrideVersionModifyer, String recipientFilter, InfoGluePrincipal infoGluePrincipal, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
        SiteNodeVersionVO newSiteNodeVersionVO = null; 
        
        Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);

		try
		{	
			newSiteNodeVersionVO = changeState(oldSiteNodeVersionId, siteNodeVO, stateId, versionComment, overrideVersionModifyer, recipientFilter, infoGluePrincipal, db, resultingEvents);
        	 	
			commitRegistryAwareTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
    	
        return newSiteNodeVersionVO;
    }        


	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */

    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, SiteNodeVO siteNodeVO, Integer stateId, String versionComment, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db, Integer siteNodeId, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
    	return changeState(oldSiteNodeVersionId, siteNodeVO, stateId, versionComment, overrideVersionModifyer, null, infoGluePrincipal, db, resultingEvents);
    }

	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */
	
    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, Integer stateId, String versionComment, boolean overrideVersionModifyer, String recipientFilter, InfoGluePrincipal infoGluePrincipal, Database db, Integer siteNodeId, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
    	SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
    	return changeState(oldSiteNodeVersionId, siteNodeVO, stateId, versionComment, overrideVersionModifyer, recipientFilter, infoGluePrincipal, db, resultingEvents);
    }
    
	/**
	 * This method handles versioning and state-control of siteNodes.
	 * Se inline documentation for further explainations.
	 */
	
    public SiteNodeVersionVO changeState(Integer oldSiteNodeVersionId, SiteNodeVO siteNodeVO, Integer stateId, String versionComment, boolean overrideVersionModifyer, String recipientFilter, InfoGluePrincipal infoGluePrincipal, Database db, List<EventVO> resultingEvents) throws ConstraintException, SystemException
    {
    	Timer t = new Timer();
    	
		MediumSiteNodeVersionImpl returnSiteNodeVersionImpl = null;
		
        try
        { 
			MediumSiteNodeVersionImpl oldMediumSiteNodeVersionImpl = SiteNodeVersionController.getController().getMediumSiteNodeVersionWithId(oldSiteNodeVersionId, db);
			//t.printElapsedTime("oldMediumSiteNodeVersionImpl:" + oldMediumSiteNodeVersionImpl.getSiteNodeId());
			
            //Here we create a new version if it was a state-change back to working, it's a copy of the publish-version
	    	if(stateId.intValue() == SiteNodeVersionVO.WORKING_STATE.intValue())
	    	{
	    		logger.info("About to create a new working version");
	    	    
				SiteNodeVersionVO newSiteNodeVersionVO = new SiteNodeVersionVO();
		    	newSiteNodeVersionVO.setStateId(stateId);
				newSiteNodeVersionVO.setVersionComment("New working version");
				newSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
				if(overrideVersionModifyer)
				    newSiteNodeVersionVO.setVersionModifier(infoGluePrincipal.getName());
				else
				    newSiteNodeVersionVO.setVersionModifier(oldMediumSiteNodeVersionImpl.getVersionModifier());
				
				newSiteNodeVersionVO.setContentType(oldMediumSiteNodeVersionImpl.getContentType());
				newSiteNodeVersionVO.setPageCacheKey(oldMediumSiteNodeVersionImpl.getPageCacheKey());
				newSiteNodeVersionVO.setPageCacheTimeout(oldMediumSiteNodeVersionImpl.getPageCacheTimeout());
				newSiteNodeVersionVO.setDisableEditOnSight(oldMediumSiteNodeVersionImpl.getDisableEditOnSight());
				newSiteNodeVersionVO.setDisableLanguages(oldMediumSiteNodeVersionImpl.getDisableLanguages());
				newSiteNodeVersionVO.setDisablePageCache(oldMediumSiteNodeVersionImpl.getDisablePageCache());
				newSiteNodeVersionVO.setIsProtected(oldMediumSiteNodeVersionImpl.getIsProtected());
				newSiteNodeVersionVO.setDisableForceIdentityCheck(oldMediumSiteNodeVersionImpl.getDisableForceIdentityCheck());
				newSiteNodeVersionVO.setForceProtocolChange(oldMediumSiteNodeVersionImpl.getForceProtocolChange());

				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState 1", t.getElapsedTime());

				//returnSiteNodeVersionVO = SiteNodeVersionController.createFull(siteNodeId, infoGluePrincipal, newSiteNodeVersionVO, db).getValueObject();
				returnSiteNodeVersionImpl = SiteNodeVersionController.createSmall(siteNodeVO.getId(), infoGluePrincipal, newSiteNodeVersionVO, db);
				
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState createSmall", t.getElapsedTime());
				
				returnSiteNodeVersionImpl.setSiteNodeId(oldMediumSiteNodeVersionImpl.getSiteNodeId());
				
				copyServiceBindings(oldMediumSiteNodeVersionImpl, returnSiteNodeVersionImpl, db);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState copyServiceBindings", t.getElapsedTime());
				
				if(returnSiteNodeVersionImpl.getIsProtected().intValue() == SiteNodeVersionVO.YES || returnSiteNodeVersionImpl.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK)
				{
					copyAccessRights("SiteNodeVersion", oldMediumSiteNodeVersionImpl.getId(), returnSiteNodeVersionImpl.getId(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState copyAccessRights", t.getElapsedTime());
				}
			}
	
	    	//If the user changes the state to publish we create a copy and set that copy to publish.
	    	if(stateId.intValue() == SiteNodeVersionVO.PUBLISH_STATE.intValue())
	    	{
	    		logger.info("About to copy the working copy to a publish-one");
	    		//First we update the old working-version so it gets a comment
	    		
	    		//SiteNodeVersionVO oldSiteNodeVersionVO = oldSiteNodeVersion.getValueObject();
				oldMediumSiteNodeVersionImpl.setVersionComment(versionComment);
	
	    		//Now we create a new version which is basically just a copy of the working-version	    	
		    	SiteNodeVersionVO newSiteNodeVersionVO = new SiteNodeVersionVO();
		    	newSiteNodeVersionVO.setSiteNodeId(siteNodeVO.getId());
		    	newSiteNodeVersionVO.setStateId(stateId);
		    	newSiteNodeVersionVO.setVersionComment(versionComment);
				if(overrideVersionModifyer)
				    newSiteNodeVersionVO.setVersionModifier(infoGluePrincipal.getName());
				else
				    newSiteNodeVersionVO.setVersionModifier(oldMediumSiteNodeVersionImpl.getVersionModifier());
				
				newSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate()); 
		    	
				newSiteNodeVersionVO.setContentType(oldMediumSiteNodeVersionImpl.getContentType());
				newSiteNodeVersionVO.setPageCacheKey(oldMediumSiteNodeVersionImpl.getPageCacheKey());
				newSiteNodeVersionVO.setPageCacheTimeout(oldMediumSiteNodeVersionImpl.getPageCacheTimeout());
				newSiteNodeVersionVO.setDisableEditOnSight(oldMediumSiteNodeVersionImpl.getDisableEditOnSight());
				newSiteNodeVersionVO.setDisableLanguages(oldMediumSiteNodeVersionImpl.getDisableLanguages());
				newSiteNodeVersionVO.setDisablePageCache(oldMediumSiteNodeVersionImpl.getDisablePageCache());
				newSiteNodeVersionVO.setIsProtected(oldMediumSiteNodeVersionImpl.getIsProtected());
				newSiteNodeVersionVO.setDisableForceIdentityCheck(oldMediumSiteNodeVersionImpl.getDisableForceIdentityCheck());
				newSiteNodeVersionVO.setForceProtocolChange(oldMediumSiteNodeVersionImpl.getForceProtocolChange());

				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState publish1", t.getElapsedTime());

				//returnSiteNodeVersionVO = SiteNodeVersionController.createFull(siteNodeId, infoGluePrincipal, newSiteNodeVersionVO, db).getValueObject();
				returnSiteNodeVersionImpl = SiteNodeVersionController.createSmall(siteNodeVO.getId(), infoGluePrincipal, newSiteNodeVersionVO, db);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState publish createSmall", t.getElapsedTime());

				//returnSiteNodeVersionVO.setSiteNodeId(oldSiteNodeVersion.getValueObject().getSiteNodeId());
				//copyServiceBindings(oldSiteNodeVersion, newSiteNodeVersion, db);
				if(returnSiteNodeVersionImpl.getIsProtected().intValue() == SiteNodeVersionVO.YES || returnSiteNodeVersionImpl.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK)
				{
					copyAccessRights("SiteNodeVersion", oldMediumSiteNodeVersionImpl.getId(), returnSiteNodeVersionImpl.getId(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState publish copyAccessRights", t.getElapsedTime());
				}

				//Creating the event that will notify the editor...
				EventVO eventVO = new EventVO();
				eventVO.setDescription(returnSiteNodeVersionImpl.getVersionComment());
				eventVO.setEntityClass(SiteNodeVersion.class.getName());
				eventVO.setEntityId(new Integer(returnSiteNodeVersionImpl.getId().intValue()));
		        eventVO.setName(siteNodeVO.getName());
				eventVO.setTypeId(EventVO.PUBLISH);
		        eventVO = EventController.create(eventVO, siteNodeVO.getRepositoryId(), infoGluePrincipal, db);			
		        
		        eventVO.setName(siteNodeVO.getName());
				
				resultingEvents.add(eventVO);

				if(recipientFilter != null && !recipientFilter.equals(""))
					PublicationController.mailPublishNotification(resultingEvents, siteNodeVO.getRepositoryId(), infoGluePrincipal, recipientFilter, db);
	
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState publish create event", t.getElapsedTime());
	    	}
	
	    	if(stateId.intValue() == SiteNodeVersionVO.PUBLISHED_STATE.intValue())
	    	{
	    		logger.info("About to publish an existing version");
	    		oldMediumSiteNodeVersionImpl.setStateId(stateId);
	    		oldMediumSiteNodeVersionImpl.setIsActive(new Boolean(true));
	    		returnSiteNodeVersionImpl = oldMediumSiteNodeVersionImpl;
	    	}

	    	changeStateOnMetaInfo(db, siteNodeVO, returnSiteNodeVersionImpl.getValueObject(), stateId, versionComment, overrideVersionModifyer, infoGluePrincipal, resultingEvents);
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("changeState changeStateOnMetaInfo", t.getElapsedTime());
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }    	  
          	
    	return returnSiteNodeVersionImpl.getValueObject();
    }        

    
    /**
     * This method checks if the siteNodes latest metainfo is working - if so - it get published with the sitenode.
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     * @throws Exception
     */
    public void changeStateOnMetaInfo(Database db, SiteNodeVO snVO, SiteNodeVersionVO siteNodeVersionVO, Integer stateId, String versionComment, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, List events) throws ConstraintException, SystemException, Exception
    {
    	Timer t = new Timer();
    	if(snVO == null)
    		return;
    	
        List<LanguageVO> languages = LanguageController.getController().getAvailableLanguageVOListForRepository(snVO.getRepositoryId(), db);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getAvailableLanguageVOListForRepository..", t.getElapsedTime());
        LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(snVO.getRepositoryId(), db);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMasterLanguage..", t.getElapsedTime());

    	ContentVO contentVO = null;
    	if(snVO.getMetaInfoContentId() != null)
    	{
    		contentVO = ContentController.getContentController().getSmallContentVOWithId(snVO.getMetaInfoContentId(), db, null);
    	}
    	else
    	{
        	logger.warn("There was no metaInfoContentId on the siteNode... run validation to improve performance..");

        	Integer metaInfoAvailableServiceBindingId = null;
    		Integer serviceBindingId = null;
    		AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information", db);
    		if(availableServiceBindingVO != null)
    			metaInfoAvailableServiceBindingId = availableServiceBindingVO.getAvailableServiceBindingId();

        	logger.info("after loading service binding for meta info");

    		List<SmallServiceBindingImpl> serviceBindings = ServiceBindingController.getController().getSmallServiceBindingsListForSiteNodeVersion(siteNodeVersionVO.getSiteNodeVersionId(), db);
    		Iterator<SmallServiceBindingImpl> serviceBindingIterator = serviceBindings.iterator();
    		while(serviceBindingIterator.hasNext())
    		{
    			SmallServiceBindingImpl serviceBinding = serviceBindingIterator.next();
    			if(serviceBinding.getAvailableServiceBinding().getId().intValue() == metaInfoAvailableServiceBindingId.intValue())
    			{
    				serviceBindingId = serviceBinding.getId();
    				break;
    			}
    		}

    		if(serviceBindingId != null)
    		{
    			List boundContents = ContentController.getBoundContents(db, serviceBindingId); 
    			logger.info("boundContents:" + boundContents.size());
    			if(boundContents.size() > 0)
    			{
    				contentVO = (ContentVO)boundContents.get(0);
    				logger.info("contentVO:" + contentVO.getId());
    			}
    		}
    	}
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getSmallContentVOWithId..", t.getElapsedTime());

    	if(contentVO != null)
    	{				
			Iterator<LanguageVO> languageIterator = languages.iterator();
			while(languageIterator.hasNext())
			{
				LanguageVO language = languageIterator.next();
				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), language.getId(), db);

				//if(contentVersion != null && contentVersion.getStateId().intValue() == siteNodeVersion.getStateId().intValue())
				if(contentVersionVO != null && contentVersionVO.getStateId().intValue() != stateId.intValue())
				{
					logger.info("State on current:" + contentVersionVO.getStateId());
				    logger.info("changing state on contentVersion:" + contentVersionVO.getId());
				    ContentVersion contentVersion = ContentStateController.changeState(contentVersionVO.getId(), contentVO, stateId, versionComment, overrideVersionModifyer, null, infoGluePrincipal, contentVO.getId(), db, events);
				    contentVersionVO = contentVersion.getValueObject();
				}

				if(language.getId().equals(masterLanguage.getId()) && contentVersionVO != null)
				{
				    //TODO - lets keep the ref to meta info alive...
				    //RegistryController.getController().updateSiteNodeVersion(siteNodeVersion, db);
				    //RegistryController.getController().updateContentVersion(contentVersionVO, siteNodeVersionVO, db);
					RegistryController.getController().updateContentVersionThreaded(contentVersionVO, siteNodeVersionVO);
				}
			}
		}
    }

    /**
     * This method checks if the siteNodes latest metainfo is working - if so - it get published with the sitenode.
     * @param db
     * @throws ConstraintException
     * @throws SystemException
     * @throws Exception
     */
    /*
    public void changeStateOnMetaInfo(Database db, SiteNodeVersion siteNodeVersion, Integer stateId, String versionComment, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, List events) throws ConstraintException, SystemException, Exception
    {
    	logger.info("start changeStateOnMetaInfo");
    	
        List languages = LanguageController.getController().getLanguageList(siteNodeVersion.getOwningSiteNode().getRepository().getId(), db);
		Language masterLanguage = LanguageController.getController().getMasterLanguage(db, siteNodeVersion.getOwningSiteNode().getRepository().getId());

    	logger.info("after languages");

		Integer metaInfoAvailableServiceBindingId = null;
		Integer serviceBindingId = null;
		AvailableServiceBinding availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithName("Meta information", db, true);
		if(availableServiceBinding != null)
			metaInfoAvailableServiceBindingId = availableServiceBinding.getAvailableServiceBindingId();

    	logger.info("after loading service binding for meta info");

		Collection serviceBindings = siteNodeVersion.getServiceBindings();
		Iterator serviceBindingIterator = serviceBindings.iterator();
		while(serviceBindingIterator.hasNext())
		{
			ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
			if(serviceBinding.getAvailableServiceBinding().getId().intValue() == metaInfoAvailableServiceBindingId.intValue())
			{
				serviceBindingId = serviceBinding.getId();
				break;
			}
		}

		if(serviceBindingId != null)
		{
			List boundContents = ContentController.getBoundContents(db, serviceBindingId); 
			logger.info("boundContents:" + boundContents.size());
			if(boundContents.size() > 0)
			{
				ContentVO contentVO = (ContentVO)boundContents.get(0);
				logger.info("contentVO:" + contentVO.getId());
				
				Iterator languageIterator = languages.iterator();
				while(languageIterator.hasNext())
				{
					Language language = (Language)languageIterator.next();
					ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVO.getId(), language.getId(), db);
					
					logger.info("language:" + language.getId());
					
					//if(language.getId().equals(masterLanguage.getId()) && contentVersion == null)
					//	throw new Exception("The contentVersion was null or states did not match.. the version and meta info content should allways match when it comes to master language version...");
	
					if(contentVersion != null)
					{
					    logger.info("contentVersion:" + contentVersion.getId() + ":" + contentVersion.getStateId());
					    logger.info("State wanted:" + stateId);
					}
					
					//if(contentVersion != null && contentVersion.getStateId().intValue() == siteNodeVersion.getStateId().intValue())
					if(contentVersion != null && contentVersion.getStateId().intValue() != stateId.intValue())
					{
					    logger.info("State on current:" + contentVersion.getStateId());
					    logger.info("changing state on contentVersion:" + contentVersion.getId());
					    contentVersion = ContentStateController.changeState(contentVersion.getId(), stateId, versionComment, overrideVersionModifyer, infoGluePrincipal, contentVO.getId(), db, events);
					}
					
					if(language.getId().equals(masterLanguage.getId()) && contentVersion != null)
					{
					    //TODO - lets keep the ref to meta info alive...
					    //RegistryController.getController().updateSiteNodeVersion(siteNodeVersion, db);
					    RegistryController.getController().updateContentVersion(contentVersion, siteNodeVersion, db);
					}	
				}
				
			}
		}
    }
    */
    
	/**
	 * This method copies all serviceBindings a siteNodeVersion has to the new siteNodeVersion.
	 */

    private static void copyServiceBindings(MediumSiteNodeVersionImpl originalSiteNodeVersion, MediumSiteNodeVersionImpl newSiteNodeVersion, Database db) throws ConstraintException, SystemException, Exception
	{
		Collection serviceBindings = originalSiteNodeVersion.getServiceBindings();	
		Iterator iterator = serviceBindings.iterator();
		while(iterator.hasNext())
		{
			ServiceBinding serviceBinding = (ServiceBinding)iterator.next();
			ServiceBindingVO serviceBindingVO = serviceBinding.getValueObject();			
			ServiceBindingVO newServiceBindingVO = new ServiceBindingVO();
			newServiceBindingVO.setBindingTypeId(serviceBindingVO.getBindingTypeId());
			newServiceBindingVO.setName(serviceBindingVO.getName());
			newServiceBindingVO.setPath(serviceBindingVO.getPath());
			ServiceBinding newServiceBinding = ServiceBindingController.create(newServiceBindingVO, serviceBinding.getAvailableServiceBinding().getAvailableServiceBindingId(), newSiteNodeVersion.getSiteNodeVersionId(), serviceBinding.getServiceDefinition().getServiceDefinitionId(), db);
			newSiteNodeVersion.getServiceBindings().add(newServiceBinding);
			copyQualifyers(serviceBinding, newServiceBinding, db);
		}
	}	


	/**
	 * This method copies all qualifyers a serviceBinding has to the new serviceBinding.
	 */
	private static void copyQualifyers(ServiceBinding originalServiceBinding, ServiceBinding newServiceBinding, Database db) throws ConstraintException, SystemException, Exception
	{
		Collection qualifyers = originalServiceBinding.getBindingQualifyers();	
		Collection newBindingQualifyers = new ArrayList();
		
		Iterator iterator = qualifyers.iterator();
		while(iterator.hasNext())
		{
			SmallQualifyerImpl qualifyer = (SmallQualifyerImpl)iterator.next();
			QualifyerVO qualifyerVO = qualifyer.getValueObject();
			SmallQualifyerImpl newQualifyer = new SmallQualifyerImpl();
			newQualifyer.setValueObject(qualifyerVO);
			newQualifyer.setServiceBinding((SmallServiceBindingImpl)newServiceBinding);
			newBindingQualifyers.add(newQualifyer);
			//QualifyerController.create(newQualifyerVO, newServiceBinding.getServiceBindingId(), db);
		}
		newServiceBinding.setBindingQualifyers(newBindingQualifyers);
					
	}	

	/**
	 * This method assigns the same access rights as the old content-version has.
	 */
	
	public void copyAccessRights(String interceptionPointCategory, Integer originalSiteNodeVersionId, Integer newSiteNodeVersionId, Database db) throws ConstraintException, SystemException, Exception
	{
		Timer t = new Timer();
		
		List<InterceptionPoint> interceptionPointList = InterceptionPointController.getController().getInterceptionPointList(interceptionPointCategory, db);
		List accessRightList = AccessRightController.getController().getAccessRightListForEntity(interceptionPointList, originalSiteNodeVersionId.toString(), db, true);

		logger.info("accessRightList:" + accessRightList.size());
		Iterator accessRightListIterator = accessRightList.iterator();
		while(accessRightListIterator.hasNext())
		{
			AccessRight accessRight = (AccessRight)accessRightListIterator.next();
			logger.info("accessRight:" + accessRight.getId());
			
			AccessRightVO copiedAccessRight = accessRight.getValueObject().createCopy();
			copiedAccessRight.setParameters(newSiteNodeVersionId.toString());
			
			InterceptionPoint icp = null;
			for(InterceptionPoint currentICP : interceptionPointList)
			{
				if(currentICP.getId().equals(copiedAccessRight.getInterceptionPointId()))
					icp = currentICP;
			}
			if(icp != null)
			{
				AccessRight newAccessRight = AccessRightController.getController().create(copiedAccessRight, icp, db);
				
				Iterator groupsIterator = accessRight.getGroups().iterator();
				while(groupsIterator.hasNext())
				{
				    AccessRightGroup accessRightGroup = (AccessRightGroup)groupsIterator.next();
				    AccessRightGroupVO newAccessRightGroupVO = new AccessRightGroupVO();
				    newAccessRightGroupVO.setGroupName(accessRightGroup.getGroupName());
				    AccessRightGroup newAccessRightGroup = AccessRightController.getController().createAccessRightGroup(db, newAccessRightGroupVO, newAccessRight);
				    newAccessRight.getGroups().add(newAccessRightGroup);
				}

				Iterator rolesIterator = accessRight.getRoles().iterator();
				while(rolesIterator.hasNext())
				{
				    AccessRightRole accessRightRole = (AccessRightRole)rolesIterator.next();
				    AccessRightRoleVO newAccessRightRoleVO = new AccessRightRoleVO();
				    newAccessRightRoleVO.setRoleName(accessRightRole.getRoleName());
				    AccessRightRole newAccessRightRole = AccessRightController.getController().createAccessRightRole(db, newAccessRightRoleVO, newAccessRight);
				    newAccessRight.getRoles().add(newAccessRightRole);
				}

				Iterator usersIterator = accessRight.getUsers().iterator();
				while(usersIterator.hasNext())
				{
				    AccessRightUser accessRightUser = (AccessRightUser)usersIterator.next();
				    AccessRightUserVO newAccessRightUserVO = new AccessRightUserVO();
				    newAccessRightUserVO.setUserName(accessRightUser.getUserName());
				    AccessRightUser newAccessRightUser = AccessRightController.getController().createAccessRightUser(db, newAccessRightUserVO, newAccessRight);
				    newAccessRight.getUsers().add(newAccessRightUser);
				}
			}
		}
	}	
	
	
	public void deleteAccessRights(String interceptionPointCategory, Integer siteNodeVersionId, Database db) throws ConstraintException, SystemException, Exception
	{
		List<InterceptionPoint> interceptionPointList = InterceptionPointController.getController().getInterceptionPointList(interceptionPointCategory, db);
		List accessRightList = AccessRightController.getController().getAccessRightListForEntity(interceptionPointList, siteNodeVersionId.toString(), db, false);

		//System.out.println("accessRightList:" + accessRightList.size());
		Iterator accessRightListIterator = accessRightList.iterator();
		while(accessRightListIterator.hasNext())
		{
			AccessRight accessRight = (AccessRight)accessRightListIterator.next();
			//System.out.println("accessRight:" + accessRight.getId());
			
			AccessRightController.getController().delete(accessRight, db);
		}
	}	
	
	/**
	 * This is a method that never should be called.
	 */

	public BaseEntityVO getNewVO()
	{
		return null;
	}

}
 

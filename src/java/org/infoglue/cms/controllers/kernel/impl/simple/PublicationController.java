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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.publishing.EditionBrowser;
import org.infoglue.cms.entities.publishing.Publication;
import org.infoglue.cms.entities.publishing.PublicationDetail;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.MediumSiteNodeVersionImpl;
import org.infoglue.cms.entities.workflow.Event;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.cms.util.mail.MailServiceFactory;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.util.HttpHelper;
import org.infoglue.deliver.util.LiveInstanceMonitor;
import org.infoglue.deliver.util.VelocityTemplateProcessor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * This controller is responsible for all publications management. 
 *  
 *
 * @author Stefan Sik, Mattias Bogeblad
 */

public class PublicationController extends BaseController
{
    private final static Logger logger = Logger.getLogger(PublicationController.class.getName());

	public static final int OVERIDE_WORKING = 1;
	public static final int LEAVE_WORKING   = 2;

	public static PublicationController getController()
	{
		return new PublicationController();
	}

	/**
	 * This method just returns the publication with the given id within the given transaction.
	 */
	public static Publication getPublicationWithId(Integer publicationId, Database db) throws SystemException
	{
		return (Publication) getObjectWithId(PublicationImpl.class, publicationId, db);
	}

	/**
	 * This method just returns the publication with the given id.
	 */
	public PublicationVO getPublicationVOWithId(Integer publicationId) throws SystemException
	{
		return (PublicationVO) getVOWithId(PublicationImpl.class, publicationId);
	}

	/**
	 * This method just returns the publication detail with the given id.
	 */
	public PublicationDetailVO getPublicationDetailVOWithId(Integer publicationDetailId) throws SystemException
	{
		return (PublicationDetailVO) getVOWithId(PublicationDetailImpl.class, publicationDetailId);
	}

	/**
	 * This method returns a list of those events that are publication events and
	 * concerns this repository
	 */
	public static List getPublicationEvents(Integer repositoryId) throws SystemException, Exception
	{
		return EventController.getPublicationEventVOListForRepository(repositoryId);
	}

	/**
	 * This method returns a list of those events that are publication events and
	 * concerns this repository and the submitter is in a group that the publisher also is in.
	 */
	public static List getPublicationEvents(Integer repositoryId, InfoGluePrincipal principal, String filter, boolean validate) throws SystemException, Exception
	{
		return EventController.getPublicationEventVOListForRepository(repositoryId, principal, filter, validate);
	}

	/**
	 * This method returns a list of earlier editions for this site.
	 */
	public static List getAllEditions(Integer repositoryId) throws SystemException
	{
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		List res = new ArrayList();
        try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl c WHERE c.repositoryId = $1 order by publicationDateTime desc");
			oql.bind(repositoryId);

        	QueryResults results = oql.execute(Database.READONLY);

			while (results.hasMore())
            {
            	Publication publication = (Publication)results.next();
            	res.add(publication.getValueObject());
            }

			results.close();
			oql.close();

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e);
            logger.warn("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return res;
	}

	/**
	 * This method returns a list of earlier editions for this site.
	 */
	public List<PublicationVO> getPublicationsSinceDate(Date startDate) throws SystemException
	{
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		List<PublicationVO> res = new ArrayList<PublicationVO>();
        try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT p FROM org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl p WHERE p.publicationDateTime > $1 order by publicationDateTime desc");
			oql.bind(startDate);

        	QueryResults results = oql.execute(Database.READONLY);

			while (results.hasMore())
            {
            	Publication publication = (Publication)results.next();
            	res.add(publication.getValueObject());
            }

			results.close();
			oql.close();

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return res;
	}
	
	/**
	 * This method returns a list of earlier editions for this site.
	 */
	public List<PublicationVO> getPublicationList(Date startDate, String entityName, String entityId) throws SystemException
	{
    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		List<PublicationVO> res = new ArrayList<PublicationVO>();
		
		try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT p FROM org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl p WHERE p.publicationDateTime > $1 AND p.publicationDetails.entityClass = $2 AND p.publicationDetails.entityClass = $3 order by p.publicationDateTime desc");
			oql.bind(startDate);
			oql.bind(entityName);
			oql.bind(entityId);

        	QueryResults results = oql.execute(Database.READONLY);

			while (results.hasMore())
            {
            	PublicationDetail publication = (PublicationDetail)results.next();
            	res.add(publication.getPublication().getValueObject());
            }

			results.close();
			oql.close();

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return res;
	}
	
	/**
	 * This method returns a list of earlier editions for this site.
	 */
	public static EditionBrowser getEditionPage(Integer repositoryId, int startIndex) throws SystemException
	{
		int pageSize = new Integer(CmsPropertyHandler.getEditionPageSize()).intValue();

    	Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        try
        {
            OQLQuery oql = db.getOQLQuery("SELECT c FROM org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl c WHERE c.repositoryId = $1 AND c.publicationDateTime > $2 order by publicationDateTime desc");

            oql.bind(repositoryId);
			Calendar publicationDateTimeCalendar = Calendar.getInstance();
			publicationDateTimeCalendar.add(Calendar.MONTH, -2);
			oql.bind(publicationDateTimeCalendar.getTime());

        	QueryResults results = oql.execute(Database.READONLY);

			List allEditions = Collections.list(results);

			List page = allEditions.subList(startIndex, Math.min(startIndex+pageSize, allEditions.size()));

			EditionBrowser browser = new EditionBrowser(allEditions.size(), pageSize, startIndex);

			List editionVOs = new ArrayList();
			for (Iterator iter = page.iterator(); iter.hasNext();)
			{
				Publication pub = (Publication) iter.next();
				PublicationVO pubVO = pub.getValueObject();
				//pubVO.setPublicationDetails(toVOList(pub.getPublicationDetails()));
				editionVOs.add(pubVO);
			}

			browser.setEditions(editionVOs);

			results.close();
			oql.close();

            commitTransaction(db);

			return browser;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}


	/**
	 * This method denies a requested publishing. What that means is that the entity specified in the
	 * event does not get published and that the request-event is deleted and a new one created to
	 * deliver the message back to the requester. If it is a deny of publishing we also deletes the
	 * publish-version as it no longer has any purpose.
	 */
	public static void denyPublicationRequest(Integer eventId, InfoGluePrincipal publisher, String comment, String referenceUrl) throws SystemException
	{
    	Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);

        try
        {
        	Event event = EventController.getEventWithId(eventId, db);
        	if(event.getTypeId().intValue() == EventVO.PUBLISH.intValue())
        	{
        		event.setTypeId(EventVO.PUBLISH_DENIED);
        		if(event.getEntityClass().equals(ContentVersion.class.getName()))
	        	{
	        		ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db);
        			if(contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue())
        			{
        				//If its a published version we just deletes the event - we don't want to delete the version.
	        			EventController.delete(event, db);
	        		}
        			else
        			{
	        			Content content = contentVersion.getOwningContent();
	        			Language language = contentVersion.getLanguage();
	        			//event.setEntityId(ContentVersionController.getPreviousContentVersionVO(content.getId(), language.getId(), contentVersion.getId()).getId());
	        			event.setEntityId(ContentVersionController.getContentVersionController().getPreviousActiveContentVersionVO(content.getId(), language.getId(), contentVersion.getId(), db).getId());
	        			ContentVersionController.getContentVersionController().delete(contentVersion, db);
        			}
	        	}
	        	else if(event.getEntityClass().equals(SiteNodeVersion.class.getName()))
	        	{
	        		SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(event.getEntityId(), db);
	        		if(siteNodeVersion.getStateId().intValue() == SiteNodeVersionVO.PUBLISHED_STATE.intValue())
        			{
        				//If its a published version we just deletes the event - we don't want to delete the version.
	        			EventController.delete(event, db);
	        		}
        			else
        			{
		        		SiteNode siteNode = siteNodeVersion.getOwningSiteNode();
	        			//event.setEntityId(SiteNodeVersionController.getPreviousSiteNodeVersionVO(siteNode.getId(), siteNodeVersion.getId()).getId());
	        			event.setEntityId(SiteNodeVersionController.getController().getPreviousActiveSiteNodeVersionVO(siteNode.getId(), siteNodeVersion.getId(), db).getId());
	        			SiteNodeVersionController.getController().delete(siteNodeVersion, db);
	        			//db.remove(siteNodeVersion);
        			}
	        	}
        	}
        	else if(event.getTypeId().intValue() == EventVO.UNPUBLISH_LATEST.intValue())
        	{
        		event.setTypeId(EventVO.UNPUBLISH_DENIED);
	        	if(event.getEntityClass().equals(ContentVersion.class.getName()))
	        	{
	        		event.setEntityClass(Content.class.getName());
        			event.setEntityId(ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db).getOwningContent().getId());
	        	}
	        	else if(event.getEntityClass().equals(SiteNodeVersion.class.getName()))
	        	{
	        		event.setEntityClass(SiteNode.class.getName());
        			event.setEntityId(SiteNodeVersionController.getController().getSiteNodeVersionWithId(event.getEntityId(), db).getOwningSiteNode().getId());
	        	}
        	}

        	//InfoGluePrincipal infoGluePrincipal = InfoGluePrincipalControllerProxy.getController().getInfoGluePrincipal(event.getCreator());
        	InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController().getUser(event.getCreator());

        	String email = (infoGluePrincipal!=null) ? infoGluePrincipal.getEmail() : publisher.getEmail();
			if(infoGluePrincipal == null) 
				comment += "\n\n\n(" +  event.getCreator() + " wasn't found.)"; 

        	mailNotification(event, publisher.getName(), publisher.getEmail(), email, comment, referenceUrl);

			commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}


	/**
	 * This method denies a list of requested publishing. What that means is that the entities specified in the
	 * event does not get published and that the request-event is deleted and a new one created to
	 * deliver the message back to the requester. If it is a deny of publishing we also deletes the
	 * publish-version as it no longer has any purpose.
	 */
	public static void denyPublicationRequest(List eventVOList, InfoGluePrincipal publisher, String comment, String referenceUrl) throws SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);

		try
		{
			Iterator eventIterator = eventVOList.iterator();
			while(eventIterator.hasNext())
			{
				EventVO eventVO = (EventVO)eventIterator.next();

				Event event = EventController.getEventWithId(eventVO.getId(), db);
				//InfoGluePrincipal infoGluePrincipal = InfoGluePrincipalControllerProxy.getController().getInfoGluePrincipal(event.getCreator());
	        	InfoGluePrincipal infoGluePrincipal = UserControllerProxy.getController().getUser(event.getCreator());

				if(event.getTypeId().intValue() == EventVO.PUBLISH.intValue())
				{
					event.setTypeId(EventVO.PUBLISH_DENIED);
					if(event.getEntityClass().equals(ContentVersion.class.getName()))
					{
						ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db);
						if(contentVersion.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue())
						{
							//If its a published version we just deletes the event - we don't want to delete the version.
							EventController.delete(event, db);
						}
						else
						{
							Content content = contentVersion.getOwningContent();
							Language language = contentVersion.getLanguage();
							//event.setEntityId(ContentVersionController.getPreviousContentVersionVO(content.getId(), language.getId(), contentVersion.getId()).getId());
							ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getPreviousActiveContentVersionVO(content.getId(), language.getId(), contentVersion.getId(), db);
							if(contentVersionVO != null && event != null)
								event.setEntityId(contentVersionVO.getId());
							
							ContentVersionController.getContentVersionController().delete(contentVersion, db);
						}
					}
					else if(event.getEntityClass().equals(SiteNodeVersion.class.getName()))
					{
						SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(event.getEntityId(), db);
						if(siteNodeVersion.getStateId().intValue() == SiteNodeVersionVO.PUBLISHED_STATE.intValue())
						{
							//If its a published version we just deletes the event - we don't want to delete the version.
							EventController.delete(event, db);
						}
						else
						{
							SiteNode siteNode = siteNodeVersion.getOwningSiteNode();
							//event.setEntityId(SiteNodeVersionController.getPreviousSiteNodeVersionVO(siteNode.getId(), siteNodeVersion.getId()).getId());
							SiteNodeVersion previousSiteNodeVersion = SiteNodeVersionController.getController().getPreviousActiveSiteNodeVersion(siteNode.getId(), siteNodeVersion.getId(), db);
							if(previousSiteNodeVersion != null && event != null)
								event.setEntityId(previousSiteNodeVersion.getId());
							SiteNodeVersionController.getController().delete(siteNodeVersion, db);
							SiteNodeStateController.getController().changeStateOnMetaInfo(db, siteNode.getValueObject(), previousSiteNodeVersion.getValueObject(), previousSiteNodeVersion.getStateId(), "Denied publication", true, infoGluePrincipal, new ArrayList());
							//db.remove(siteNodeVersion);
						}
					}
				}
				else if(event.getTypeId().intValue() == EventVO.UNPUBLISH_LATEST.intValue())
				{
					event.setTypeId(EventVO.UNPUBLISH_DENIED);
					if(event.getEntityClass().equals(ContentVersion.class.getName()))
					{
						event.setEntityClass(Content.class.getName());
						event.setEntityId(ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db).getOwningContent().getId());
					}
					else if(event.getEntityClass().equals(SiteNodeVersion.class.getName()))
					{
						event.setEntityClass(SiteNode.class.getName());
						event.setEntityId(SiteNodeVersionController.getController().getSiteNodeVersionWithId(event.getEntityId(), db).getOwningSiteNode().getId());
					}
				}

				String email = (infoGluePrincipal!=null) ? infoGluePrincipal.getEmail() : publisher.getEmail();
				if(infoGluePrincipal == null) 
					comment += "\n\n\n(" +  event.getCreator() + " wasn't found.)"; 
				
				mailNotification(event, publisher.getName(), publisher.getEmail(), email, comment, referenceUrl);
			}

			commitRegistryAwareTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}



	/**
	 * This method mails the rejection to the recipient.
	 */
	private static void mailNotification(Event event, String editorName, String sender, String recipient, String comment, String referenceUrl)
	{
	    String email = "";
	    
	    try
	    {
	        String template;
	        
	        String contentType = CmsPropertyHandler.getMailContentType();
	        if(contentType == null || contentType.length() == 0)
	            contentType = "text/html";
	        
	        if(contentType.equalsIgnoreCase("text/plain"))
	            template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/publishingtool/deniedPublication_plain.vm"));
		    else
	            template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/publishingtool/deniedPublication_html.vm"));
		        
		    Map parameters = new HashMap();
		    parameters.put("event", event);
		    parameters.put("editorName", editorName);
		    parameters.put("recipient", recipient);
		    parameters.put("comment", comment);
		    parameters.put("referenceUrl", referenceUrl);
			
			StringWriter tempString = new StringWriter();
			PrintWriter pw = new PrintWriter(tempString);
			new VelocityTemplateProcessor().renderTemplate(parameters, pw, template);
			email = tempString.toString();
	    
			String systemEmailSender = CmsPropertyHandler.getSystemEmailSender();
			if(systemEmailSender == null || systemEmailSender.equalsIgnoreCase(""))
				systemEmailSender = "InfoGlueCMS@" + CmsPropertyHandler.getMailSmtpHost();
			if(sender != null && !sender.equals("") && sender.indexOf("@") > -1)
				systemEmailSender = sender;
			
			logger.info("email:" + email);
			MailServiceFactory.getService().send(systemEmailSender, recipient, "CMS - Publishing was denied!!", email, contentType, "UTF-8");
		}
		catch(Exception e)
		{
			logger.error("The notification was not sent. Reason:" + e.getMessage());
			logger.info("The notification was not sent. Reason:" + e.getMessage(), e);
		}
	}

	private static Map<String, String> getNiceURIsForCurrentPublishedVersion(SiteNodeVO siteNodeVO, Database db) throws SystemException, Exception, Bug
	{
		Map<String, String> currentNiceUris = new HashMap<String, String>();
		String niceUriAttributeName = CmsPropertyHandler.getNiceURIAttributeName();
		List<LanguageVO> languageVOs = LanguageController.getController().getAvailableLanguageVOListForRepository(siteNodeVO.getRepositoryId(), db);
		for (LanguageVO languageVO : languageVOs)
		{
			ContentVersionVO currentPublishedMetainfoVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), languageVO.getLanguageId(), ContentVersionVO.PUBLISHED_STATE, db);
			if (currentPublishedMetainfoVersion != null)
			{
				String attributeValue = ContentVersionController.getContentVersionController().getAttributeValue(currentPublishedMetainfoVersion, niceUriAttributeName, false);
				if (logger.isTraceEnabled())
				{
					logger.trace("SiteNode has published meta info version for language: " + languageVO.getLanguageId() + ". SiteNode.id: " + siteNodeVO.getSiteNodeId() + ". Value: " + attributeValue);
				}
				currentNiceUris.put("" + languageVO.getLanguageId(), attributeValue);
			}
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("SiteNode's current NiceURIs: " + currentNiceUris + ". SiteNode.id: " + siteNodeVO.getSiteNodeId());
		}
		return currentNiceUris;
	}

	/**
	 * Format of the returned Map:
	 * <pre>
	 * SiteNodeId: {
	 * 	LanguageId: {
	 * 		'NiceURIAttributes' (OBS: static string): niceUriAttribute,
	 * 		'PageURLs' (OBS: static string): niceUriURL
	 * 	}
	 * }
	 * </pre>
	 */
	private Map<Integer, Map<String, Map<String, String>>> getPageUrlsForSiteNodes(Map<Integer,SiteNodeVO> newSiteNodeMap, InfoGluePrincipal infoGluePrincipal)
	{
		Map<Integer, Map<String, Map<String, String>>> result = new HashMap<Integer, Map<String,Map<String, String>>>();
		Database db = null;
		try
		{
			db = beginTransaction();
			for (SiteNodeVO siteNodeVO : newSiteNodeMap.values())
			{
				Map<String, Map<String, String>> siteNodeObject = new HashMap<String, Map<String, String>>();
				Map<String, String> niceUriAttributes = getNiceURIsForCurrentPublishedVersion(siteNodeVO, db);
				Map<String, String> pageUrls = RedirectController.getController().getNiceURIMapBeforeMove(db, siteNodeVO.getRepositoryId(), siteNodeVO.getSiteNodeId(), infoGluePrincipal);

				List<LanguageVO> languageVOs = LanguageController.getController().getAvailableLanguageVOListForRepository(siteNodeVO.getRepositoryId(), db);
				for (LanguageVO languageVO : languageVOs)
				{
					Map<String, String> languageObject = new HashMap<String, String>();
					languageObject.put("NiceURIAttributes", niceUriAttributes.get("" + languageVO.getLanguageId()));
					languageObject.put("PageURLs", pageUrls.get("" + languageVO.getLanguageId()));
					siteNodeObject.put("" + languageVO.getLanguageId(), languageObject);
				}

				result.put(siteNodeVO.getSiteNodeId(), siteNodeObject);
			}
			if (logger.isDebugEnabled())
			{
				logger.debug("NiceURIs from publications (NiceUri system redirect function). result: " + result);
			}
		}
		catch (Exception ex)
		{
			logger.error("Error when gettings page URLs for system redirect creation (NiceURI changes). Message: " + ex.getMessage());
			logger.warn("Error when gettings page URLs for system redirect creation (NiceURI changes).", ex);
		}
		finally
		{
			try
			{
				rollbackTransaction(db);
			}
			catch (Exception ex) { /* According to rollbackTransaction source this exception cannot occur. */ }
		}
		return result;
	}

	private void createSystemRedirectsForNiceUriChanges(Map<Integer, Map<String, Map<String, String>>> allCurrentNiceUris, Map<Integer, Map<String, Map<String, String>>> allNewNiceUris, InfoGluePrincipal infoGluePrincipal)
	{
		for (Map.Entry<Integer, Map<String, Map<String, String>>> siteNodeEntry : allCurrentNiceUris.entrySet())
		{
			Integer siteNodeId = siteNodeEntry.getKey();
			Map<String, Map<String, String>> newNiceUris = allNewNiceUris.get(siteNodeId);
			if (logger.isDebugEnabled())
			{
				logger.debug("Looking for NiceUri changes for SiteNode.id: " + siteNodeId);
			}
			if (newNiceUris == null)
			{
				logger.warn("There was no new NiceUri mapping. Will not create a system redirect. How can this happen? Debug.key: " + siteNodeEntry.getKey() + ". Debug.value: " + siteNodeEntry.getValue());
			}
			else
			{
					for (Map.Entry<String, Map<String, String>> languageEntry : siteNodeEntry.getValue().entrySet())
					{
						try
						{
							String languageId = languageEntry.getKey();
							Map<String, String> currentValues = languageEntry.getValue();
							Map<String, String> newValues = newNiceUris.get(languageEntry.getKey());
							String currentNiceUriAttribute = currentValues.get("NiceURIAttributes");
							String newNiceUriAttribute = newValues.get("NiceURIAttributes");
							if (currentNiceUriAttribute != null && !currentNiceUriAttribute.equals(newNiceUriAttribute))
							{
								try
								{
									logger.info("Found new NiceURI for SiteNode: " + siteNodeEntry.getKey());
									String currentPageUrl = currentValues.get("PageURLs");
									SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
									Map<String, String> pageUrls = new HashMap<String, String>();
									pageUrls.put("" + languageId, currentPageUrl);
									RedirectController.getController().createSystemRedirect(pageUrls, siteNodeVO.getRepositoryId(), siteNodeId, infoGluePrincipal);
								}
								catch (Exception ex)
								{
									logger.error("Failed to create system redirect for NiceURI change. SiteNode.id: " + siteNodeId + ". Message: " + ex.getMessage());
									logger.warn("Failed to create system redirect for NiceURI change. SiteNode.id: " + siteNodeId, ex);
								}
							}
						}
						catch (NullPointerException nex)
						{
							logger.warn("Some value was missing in the NiceUri redirect handling. Will not create a redirect for this entity. Debug.key: " + languageEntry.getKey() + ". Debug.value: " + languageEntry.getValue());
						}
					}
			}
		}
	}

	/**
	 * This method creates a new publication with the concerned events carried out.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<EventVO> events, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db) throws SystemException, Exception
	{
		return createAndPublish(publicationVO, events, overrideVersionModifyer, infoGluePrincipal, db, false);
	}
	/**
	 * This method creates a new publication with the concerned events carried out.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<EventVO> events, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db, boolean isDeleteOperation) throws SystemException, Exception
    {
        List<Integer> siteNodeVersionId = new ArrayList<Integer>();
        List<Integer> contentVersionId = new ArrayList<Integer>();
        
        Iterator<EventVO> eventIterator = events.iterator();
		while(eventIterator.hasNext())
		{
			EventVO event = eventIterator.next();
			
			if(event.getEntityClass().indexOf("SiteNodeVersion") > -1)
				siteNodeVersionId.add(event.getEntityId());
			else
				contentVersionId.add(event.getEntityId());
		}
		
		Map<Integer,SiteNodeVO> siteNodeMap = SiteNodeController.getController().getSiteNodeVOMapWithNoStateCheck(siteNodeVersionId);
		Map<Integer,ContentVO> contentMap = ContentController.getContentController().getContentVOMapWithNoStateCheck(contentVersionId);

        publicationVO = createAndPublish(publicationVO, events, siteNodeMap, contentMap, overrideVersionModifyer, infoGluePrincipal, db, isDeleteOperation);
	        
        return publicationVO;
    }

	/**
	 * This method creates a new publication with the concerned events carried out.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<EventVO> events, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal) throws SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();

		try
		{
	        beginTransaction(db);

	        List<Integer> siteNodeVersionId = new ArrayList<Integer>();
	        List<Integer> contentVersionId = new ArrayList<Integer>();
	        
	        Iterator<EventVO> eventIterator = events.iterator();
			while(eventIterator.hasNext())
			{
				EventVO event = eventIterator.next();
				
				if(event.getEntityClass().indexOf("SiteNodeVersion") > -1)
					siteNodeVersionId.add(event.getEntityId());
				else
					contentVersionId.add(event.getEntityId());
			}
			
			Map<Integer,SiteNodeVO> siteNodeMap = SiteNodeController.getController().getSiteNodeVOMapWithNoStateCheck(siteNodeVersionId);
			Map<Integer,ContentVO> contentMap = ContentController.getContentController().getContentVOMapWithNoStateCheck(contentVersionId);

	        publicationVO = createAndPublish(publicationVO, events, siteNodeMap, contentMap, overrideVersionModifyer, infoGluePrincipal, db);
	        
	        commitTransaction(db);
	        
	        // Notify the interceptors!!!
	        try
			{
	            Map hashMap = new HashMap();
	        	hashMap.put("publicationId", publicationVO.getId());
	        	
	    		intercept(hashMap, "Publication.Written", infoGluePrincipal, true, true);
			}
			catch (Exception e)
			{
				logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to commit the publication: " + e.getMessage(), e);
	    	rollbackTransaction(db);
		}
		
        return publicationVO;
    }

	//private static Object publishingLock = new Object();

	/**
	 * This method creates a new publication with the concerned events carried out.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<EventVO> events, Map<Integer,SiteNodeVO> newSiteNodeMap, Map<Integer,ContentVO> newContentMap, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal) throws SystemException
	{
		//synchronized (publishingLock) 
		//{
			Map<Integer, Map<String, Map<String, String>>> allPageUrlsBeforePublication = getPageUrlsForSiteNodes(newSiteNodeMap, infoGluePrincipal);

			Database db = CastorDatabaseService.getDatabase();
			try
			{
				beginTransaction(db);
				publicationVO = createAndPublish(publicationVO, events, newSiteNodeMap, newContentMap, overrideVersionModifyer, infoGluePrincipal, db);
				commitRegistryAwareTransaction(db);
	
				// Notify the interceptors!!!
				try
				{
					Map hashMap = new HashMap();
					hashMap.put("publicationId", publicationVO.getId());
	
					intercept(hashMap, "Publication.Written", infoGluePrincipal, true, true);
				}
				catch (Exception e)
				{
					logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
				}
			}
			catch(Exception e)
			{
				logger.error("An error occurred when we tried to commit the publication: " + e.getMessage(), e);
				rollbackTransaction(db);
			}
	
			Map<Integer, Map<String, Map<String, String>>> allPageUrlsAfterPublication = getPageUrlsForSiteNodes(newSiteNodeMap, infoGluePrincipal);
			createSystemRedirectsForNiceUriChanges(allPageUrlsBeforePublication, allPageUrlsAfterPublication, infoGluePrincipal);
		//}

		return publicationVO;
	}

	public PublicationVO createAndPublish(PublicationVO publicationVO, List<EventVO> events, Map<Integer,SiteNodeVO> newSiteNodeMap, Map<Integer,ContentVO> newContentMap, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db) throws SystemException, Exception
    {
		return createAndPublish(publicationVO, events, newSiteNodeMap, newContentMap, overrideVersionModifyer, infoGluePrincipal, db, false);
    }
	
	/**
	 * This method creates a new publication with the concerned events carried out.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<EventVO> events, Map<Integer,SiteNodeVO> newSiteNodeMap, Map<Integer,ContentVO> newContentMap, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db, boolean isDeleteOperation) throws SystemException, Exception
    {
	   	logger.info("*********************************");
    	logger.info("Creating edition ");
    	logger.info("*********************************");

        Publication publication = new PublicationImpl();
        publicationVO.setPublicationDateTime(Calendar.getInstance().getTime());
        publication.setValueObject(publicationVO);
		publication.setPublisher(infoGluePrincipal.getName());

		Iterator<EventVO> eventIterator = events.iterator();
		while(eventIterator.hasNext())
		{
			EventVO event = eventIterator.next();
			
			SiteNodeVO siteNodeVO = newSiteNodeMap.get(event.getEntityId());
			ContentVO contentVO = newContentMap.get(event.getEntityId());
			
			createPublicationInformation(publication, EventController.getEventWithId(event.getId(), db), siteNodeVO, contentVO, overrideVersionModifyer, infoGluePrincipal, db, isDeleteOperation);
		}

		db.create(publication);

        // Replicate database!!!
        try
		{
	    	logger.info("Starting replication...");
			ReplicationMySqlController.updateSlaveServer();
	    	logger.info("Finished replication...");
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

		try
		{
			if(!isDeleteOperation)
			{
			    for(EventVO event : events)
			    {
		    		List<Event> repoEvents = EventController.getPublicationEventVOListForRepository(event.getRepositoryId(), db);
				    for(Event repoEvent : repoEvents)
				    {
				    	if(!event.getId().equals(repoEvent.getId()) && event.getEntityClass().equals(repoEvent.getEntityClass()))
			   			{
					    	if(event.getEntityClass().equals(ContentVersion.class.getName()))
					    	{
					    		try
					    		{
						    		ContentVersionVO eventCVVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(event.getEntityId(), db);
						    		ContentVersionVO repoEventCVVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(repoEvent.getEntityId(), db);
						    		if(eventCVVO.getContentId().equals(repoEventCVVO.getContentId()) && eventCVVO.getLanguageId().equals(repoEventCVVO.getLanguageId()))
						    		{
						    			EventController.delete(repoEvent, db);
						    		}
					    		}
					    		catch(Exception e)
					    		{
					    			logger.warn("Problem with repoEvent: " + repoEvent.getId() + " AND/OR event: " + event.getId()+ ". Error: " + e.getMessage());
					    		}
					    	}
					    	else if(event.getEntityClass().equals(SiteNodeVersion.class.getName()))
					    	{
					    		try
					    		{
						    		SiteNodeVersionVO eventSNVVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(event.getEntityId(), db);
						    		SiteNodeVersionVO repoEventSNVVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(repoEvent.getEntityId(), db);
						    		if(eventSNVVO.getSiteNodeId().equals(repoEventSNVVO.getSiteNodeId()))
						    		{
						    			EventController.delete(repoEvent, db);
						    		}
					    		}
					    		catch(Exception e)
					    		{
					    			logger.warn("Problem with repoEvent: " + repoEvent.getId() + " AND/OR event: " + event.getId()+ ". Error: " + e.getMessage());
					    		}
					    	}
					    }
				    }
			    }
			}
		}
		catch (Exception e) 
		{
			logger.error("Error: " + e.getMessage(), e);
		}
		
        // Notify the listeners!!!
        try
		{
            Map hashMap = new HashMap();
        	hashMap.put("publicationId", publicationVO.getId());
        	
    		intercept(hashMap, "Publication.Write", infoGluePrincipal);
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

		// Update live site!!!
		try
		{
			logger.info("Notifying the entire system about a publishing...");
			NotificationMessage notificationMessage = new NotificationMessage("PublicationController.createAndPublish():", PublicationImpl.class.getName(), infoGluePrincipal.getName(), NotificationMessage.PUBLISHING, publicationVO.getId(), publicationVO.getName());
			//NotificationMessage notificationMessage = new NotificationMessage("PublicationController.createAndPublish():", NotificationMessage.PUBLISHING_TEXT, infoGluePrincipal.getName(), NotificationMessage.PUBLISHING, publicationVO.getId(), "org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl");
			ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	      	RemoteCacheUpdater.pushAndClearSystemNotificationMessages(infoGluePrincipal);
			//RemoteCacheUpdater.clearSystemNotificationMessages();
			logger.info("Finished Notifying...");
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

        return publicationVO;
    }


	/**
	 * This method creates a new publication with the given publicationDetails.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<PublicationDetailVO> publicationDetailVOList, String publisherName) throws SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();

		try
		{
	        beginTransaction(db);

	        publicationVO = createAndPublish(publicationVO, publicationDetailVOList, publisherName, db);
	        
	        commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to commit the publication: " + e.getMessage(), e);
	    	rollbackTransaction(db);
		}

        return publicationVO;
    }

	/**
	 * This method creates a new publication with the given publicationDetails.
	 */
	public PublicationVO createAndPublish(PublicationVO publicationVO, List<PublicationDetailVO> publicationDetailVOList, String publisherName, Database db) throws SystemException, Exception
    {
	   	logger.info("*********************************");
    	logger.info("Creating edition ");
    	logger.info("*********************************");

        Publication publication = new PublicationImpl();
        publicationVO.setPublicationDateTime(Calendar.getInstance().getTime());
        publication.setValueObject(publicationVO);
		publication.setPublisher("SYSTEM");

		for(PublicationDetailVO publicationDetailVO : publicationDetailVOList)
		{
			createPublicationInformation(publication, publicationDetailVO, db);
		}

		db.create(publication);

        // Replicate database!!!
        try
		{
	    	logger.info("Starting replication...");
			ReplicationMySqlController.updateSlaveServer();
	    	logger.info("Finished replication...");
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

        // Notify the listeners!!!
        try
		{
            Map hashMap = new HashMap();
        	hashMap.put("publicationId", publicationVO.getId());
        	
        	logger.info("*****************Calling Publication.Write");
    		intercept(hashMap, "Publication.Write", UserControllerProxy.getController().getUser(publisherName));
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

		// Update live site!!!
		try
		{
			logger.info("Notifying the entire system about a publishing...");
			NotificationMessage notificationMessage = new NotificationMessage("PublicationController.createAndPublish():", PublicationImpl.class.getName(), publisherName, NotificationMessage.PUBLISHING, publicationVO.getId(), publicationVO.getName());
			//NotificationMessage notificationMessage = new NotificationMessage("PublicationController.createAndPublish():", NotificationMessage.PUBLISHING_TEXT, infoGluePrincipal.getName(), NotificationMessage.PUBLISHING, publicationVO.getId(), "org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl");
			ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	      	RemoteCacheUpdater.pushAndClearSystemNotificationMessages(publisherName);
			//RemoteCacheUpdater.clearSystemNotificationMessages();
			logger.info("Finished Notifying...");
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

        return publicationVO;
    }

	/**
	 * Creates a connection between contentversion or siteNodeVersion and publication, ie adds a contentversion
	 * to the publication.
	 */
	private static void createPublicationInformation(Publication publication, Event event, SiteNodeVO siteNodeVO, ContentVO contentVO, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db) throws Exception
	{
		createPublicationInformation(publication, event, siteNodeVO, contentVO, overrideVersionModifyer, infoGluePrincipal, db, false);
	}
	/**
	 * Creates a connection between contentversion or siteNodeVersion and publication, ie adds a contentversion
	 * to the publication.
	 */
	private static void createPublicationInformation(Publication publication, Event event, SiteNodeVO siteNodeVO, ContentVO contentVO, boolean overrideVersionModifyer, InfoGluePrincipal infoGluePrincipal, Database db, boolean isDeleteOperation) throws Exception
	{
		String entityClass = event.getEntityClass();
		Integer entityId   = event.getEntityId();
		Integer typeId     = event.getTypeId();
		logger.info("entityClass:" + entityClass);
		logger.info("entityId:" + entityId);
		logger.info("typeId:" + typeId);
		
		// Publish contentversions
        if(entityClass.equals(ContentVersion.class.getName()))
		{
			ContentVersion contentVersion = null;
			ContentVersion oldContentVersion = null;
			if(!isDeleteOperation)
				oldContentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(entityId, db);
			else
				oldContentVersion = ContentVersionController.getContentVersionController().getReadOnlyMediumContentVersionWithId(entityId, db);
			
			if(oldContentVersion != null && typeId.intValue() == EventVO.UNPUBLISH_LATEST.intValue())
			{
			    oldContentVersion.setIsActive(new Boolean(false));
			    contentVersion = oldContentVersion;
			    /*
				contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(oldContentVersion.getOwningContent().getContentId(), oldContentVersion.getLanguage().getLanguageId(), db);
				if(contentVersion != null)
				{
					//We just set the published version to not active.
					contentVersion.setIsActive(new Boolean(false));
				}
				*/
			}
			/*
			else if(oldContentVersion != null && oldContentVersion.getOwningContent() != null && typeId.intValue() == EventVO.UNPUBLISH_ALL.intValue())
			{
				//We just set the published version to not active.
			    oldContentVersion.setIsActive(new Boolean(false));
			}
			*/
			else if(oldContentVersion != null && oldContentVersion.getValueObject().getContentId() != null)
			{
			    List events = new ArrayList();
				Integer contentId = oldContentVersion.getValueObject().getContentId();
	    		ContentVersion newContentVersion = ContentStateController.changeState(entityId, contentVO, ContentVersionVO.PUBLISHED_STATE, "Published", overrideVersionModifyer, null, infoGluePrincipal, contentId, db, events);
	    		contentVersion = null;
	    		if(!isDeleteOperation)
	    			contentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(newContentVersion.getContentVersionId(), db);
	    		else
	    			contentVersion = ContentVersionController.getContentVersionController().getReadOnlyMediumContentVersionWithId(newContentVersion.getContentVersionId(), db);
			}

			if(contentVersion != null)
			{
				//The contentVersion in here is the version we have done something with...
				PublicationDetail publicationDetail = new PublicationDetailImpl();
				publicationDetail.setCreationDateTime(DateHelper.getSecondPreciseDate());
				publicationDetail.setDescription(event.getDescription());
				publicationDetail.setEntityClass(entityClass);
				publicationDetail.setEntityId(contentVersion.getId());
				publicationDetail.setName(event.getName());
				publicationDetail.setTypeId(event.getTypeId());
				publicationDetail.setPublication((PublicationImpl)publication);
				publicationDetail.setCreator(event.getCreator());

				Collection publicationDetails = publication.getPublicationDetails();
				if(publicationDetails == null)
					publication.setPublicationDetails(new ArrayList());

				publication.getPublicationDetails().add(publicationDetail);
				db.remove(event);
			}
		}

		// Publish sitenodeversions
        if(entityClass.equals(SiteNodeVersion.class.getName()))
		{
			MediumSiteNodeVersionImpl siteNodeVersion = null;
			//SiteNodeVersion oldSiteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(entityId, db);
			MediumSiteNodeVersionImpl oldSiteNodeVersion = SiteNodeVersionController.getController().getMediumSiteNodeVersionWithId(entityId, db);
			if(oldSiteNodeVersion != null && oldSiteNodeVersion.getSiteNodeId() != null && typeId.intValue() == EventVO.UNPUBLISH_LATEST.intValue())
			{
			    oldSiteNodeVersion.setIsActive(new Boolean(false));
			    siteNodeVersion = oldSiteNodeVersion;
			}
			/*
			else if(oldSiteNodeVersion != null && oldSiteNodeVersion.getOwningSiteNode() != null && typeId.intValue() == EventVO.UNPUBLISH_ALL.intValue())
			{
				//We just set the published version to not active.
			    siteNodeVersion.setIsActive(new Boolean(false));
			}
			*/
			else if(oldSiteNodeVersion != null && oldSiteNodeVersion.getSiteNodeId() != null)
			{
				List<EventVO> events = new ArrayList<EventVO>();

				SiteNodeVersionVO newSiteNodeVersionVO = SiteNodeStateController.getController().changeState(entityId, siteNodeVO, SiteNodeVersionVO.PUBLISHED_STATE, "Published", overrideVersionModifyer, infoGluePrincipal, db, oldSiteNodeVersion.getSiteNodeId(), events);
				siteNodeVersion = SiteNodeVersionController.getController().getMediumSiteNodeVersionWithId(newSiteNodeVersionVO.getId(), db);
			}

			if(siteNodeVersion != null)
			{
				//The siteNodeVersion in here is the version we have done something with...
				PublicationDetail publicationDetail = new PublicationDetailImpl();
				publicationDetail.setCreationDateTime(DateHelper.getSecondPreciseDate());
				publicationDetail.setDescription(event.getDescription());
				publicationDetail.setEntityClass(entityClass);
				publicationDetail.setEntityId(siteNodeVersion.getId());
				publicationDetail.setName(event.getName());
				publicationDetail.setTypeId(event.getTypeId());
				publicationDetail.setPublication((PublicationImpl)publication);
				publicationDetail.setCreator(event.getCreator());

				Collection publicationDetails = publication.getPublicationDetails();
				if(publicationDetails == null)
					publication.setPublicationDetails(new ArrayList());

				publication.getPublicationDetails().add(publicationDetail);
				db.remove(event);
			}
		}
	}

	/**
	 * Creates a connection between contentversion or siteNodeVersion and publication, ie adds a contentversion
	 * to the publication.
	 */
	private static void createPublicationInformation(Publication publication, PublicationDetailVO publicationDetailVO, Database db) throws Exception
	{
		PublicationDetail publicationDetail = new PublicationDetailImpl();
		publicationDetail.setValueObject(publicationDetailVO);
		publicationDetail.setPublication((PublicationImpl)publication);

		Collection publicationDetails = publication.getPublicationDetails();
		if(publicationDetails == null)
			publication.setPublicationDetails(new ArrayList());

		publication.getPublicationDetails().add(publicationDetail);
	}
	
	/**
	 * This method (currently used for testing) will create a Publication with associated PublicationDetails children.
	 */
	public static PublicationVO create(PublicationVO publication) throws SystemException
    {
		Database db = beginTransaction();

        try
        {
			PublicationImpl p = new PublicationImpl();
			p.setValueObject(publication);
			p.setPublicationDetails(new ArrayList());
			for (Iterator iter = publication.getPublicationDetails().iterator(); iter.hasNext();)
			{
				PublicationDetailVO detailVO = (PublicationDetailVO) iter.next();
				PublicationDetail pd = new PublicationDetailImpl();
				pd.setPublication(p);
				pd.setValueObject(detailVO);
				p.getPublicationDetails().add(pd);
			}

			db.create(p);

			PublicationVO returnPub = p.getValueObject();
			returnPub.setPublicationDetails(toVOList(p.getPublicationDetails()));

			commitTransaction(db);
			return returnPub;
        }
        catch(Exception e)
        {
			e.printStackTrace();
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}

	/**
	 * This method returns a list of all details a publication has.
	 */
	public PublicationVO getPublicationVO(Integer publicationId) throws SystemException
	{
		PublicationVO publicationVO = null;
		
        try
        {
        	publicationVO = getPublicationVOWithId(publicationId);
        }
        catch(Exception e)
        {
            logger.warn("We could not find publication in database:" + e.getMessage(), e);
        }

        return publicationVO;
	}
	
	/**
	 * This method returns a list of all details a publication has.
	 */
	public List<PublicationDetailVO> getPublicationDetailVOList(Integer publicationId) throws SystemException
	{
		List<PublicationDetailVO> publicationDetails = new ArrayList<PublicationDetailVO>();

		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);

        try
        {
        	Publication publication = getPublicationWithId(publicationId, db);
        	Collection<PublicationDetailVO> details = publication.getPublicationDetails();
            publicationDetails = toVOList(details);

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return publicationDetails;
	}

	/**
	 * This method returns a list of all details a publication has.
	 */
	public List<PublicationDetailVO> getPublicationDetailVOList(Integer publicationId, Database db) throws SystemException
	{
		List<PublicationDetailVO> publicationDetails = new ArrayList<PublicationDetailVO>();

    	Publication publication = getPublicationWithId(publicationId, db);
    	Collection<PublicationDetailVO> details = publication.getPublicationDetails();
        publicationDetails = toVOList(details);

        return publicationDetails;
	}

	
	/**
	 * This method checks if the deliver server has processed a certain entity after a specified timestamp. This is as a check to ensure that the publication process is not 
	 * a problem causing contents/pages not to publish.  
	 */
	public Boolean getIsEntityPublicationProcessed(String entityName, String entityId) throws SystemException
	{
		Boolean isEntityPublicationProcessed = true;

		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);

        try
        {
        	isEntityPublicationProcessed = getIsEntityPublicationProcessed(entityName, entityId, db);

			commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return isEntityPublicationProcessed;
	}

	/**
	 * This method checks if the deliver server has processed a certain entity after a specified timestamp. This is as a check to ensure that the publication process is not 
	 * a problem causing contents/pages not to publish.  
	 */
	public Boolean getIsEntityPublicationProcessed(String entityName, String entityId, Database db) throws SystemException
	{
		Boolean isEntityPublicationProcessed = true;
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -24);
		
		List<PublicationVO> publications = getPublicationList(calendar.getTime(), entityName, entityId);
		for(PublicationVO publication : publications)
		{
			List<String[]> statuses = getPublicationStatusList(publication.getId());
			for(String[] status : statuses)
			{
				if(status[1].contains("Error") || status[1].contains("Received"))
				{
					isEntityPublicationProcessed = false;
				}
			}
			break;
		}
		
        return isEntityPublicationProcessed;
	}

	/**
	 * This method returns a list of all details a publication has.
	 */
	public static List<String[]> getPublicationStatusList(Integer publicationId) throws SystemException
	{
		List publicationDetails = new ArrayList();

		List<String> publicUrls = CmsPropertyHandler.getPublicDeliveryUrls();

		for(String deliverUrl : publicUrls)
		{
			String address = deliverUrl + "/UpdateCache!getPublicationState.action?publicationId=" + publicationId;
			
			if(address.indexOf("@") > -1)
			{
				publicationDetails.add(new String[]{"" + deliverUrl, "Error", "Not valid server url"});
			}
			else
			{
				try
				{
					Boolean serverStatus = LiveInstanceMonitor.getInstance().getServerStatus(deliverUrl);
					if(!serverStatus)
					{
						publicationDetails.add(new String[]{"" + deliverUrl, "Error", "Server not available for status query"});					
					}
					else
					{
						HttpHelper httpHelper = new HttpHelper();
						String response = httpHelper.getUrlContent(address, 2000);
						logger.info("response:" + response);
						if(response != null && response.indexOf("status=Unknown;") > -1)
						{
							if(response.indexOf("serverStartDateTime:") > -1)
							{
								String applicationStartupDateTimeString = response.substring(response.indexOf("serverStartDateTime:") + 20).trim();
								logger.info("applicationStartupDateTimeString:" + applicationStartupDateTimeString);
								VisualFormatter visualFormatter = new VisualFormatter();
								Date serverStartupDate = visualFormatter.parseDate(applicationStartupDateTimeString, "yyyy-MM-dd HH:mm:ss");
								PublicationVO publicationVO = PublicationController.getController().getPublicationVO(publicationId);
								if(publicationVO.getPublicationDateTime().before(serverStartupDate))
									publicationDetails.add(new String[]{"" + deliverUrl, "N/A", "Application restarted after the publication: " + applicationStartupDateTimeString});							
								else
									publicationDetails.add(new String[]{"" + deliverUrl, "N/A", "No information found"});							
							}
							else
							{
								publicationDetails.add(new String[]{"" + deliverUrl, "N/A", "No information found"});							
							}
						}
						else
						{
							Map<String,String> responseMap = httpHelper.toMap(response.trim(), "utf-8");
							CacheEvictionBean bean = CacheEvictionBean.getCacheEvictionBean(responseMap);
							if(bean == null)
								throw new Exception("No information found");
							
							VisualFormatter visualFormatter = new VisualFormatter();
							publicationDetails.add(new String[]{"" + deliverUrl, responseMap.get("status"), "" + visualFormatter.formatDate(bean.getProcessedTimestamp(), "yyyy-MM-dd HH:mm:ss")});
						}
					}
				}
				catch(Exception e)
				{
					logger.error("Problem getting publication status:" + e.getMessage());
					publicationDetails.add(new String[]{"" + deliverUrl, "Error", "" + e.getMessage()});
				}

			}
		}
		
        return publicationDetails;
	}
	
	/**
	 * This method returns a list of all details a publication has.
	 * @throws Exception 
	 */
	public static List<String[]> getCacheDebugList(String entityName, String entityId, boolean forceClear, boolean isEntityPublicationProcessed)
	{
		List<String[]> debugInfo = new ArrayList<String[]>();

		List<String> publicUrls = CmsPropertyHandler.getPublicDeliveryUrls();

		for(String deliverUrl : publicUrls)
		{
			String address = deliverUrl + "/UpdateCache!entityCacheDebugInformation.action?entityName=" + entityName + "&entityId=" + entityId + "&forceClear=" + forceClear;
			logger.info("address:" + address);
			if(address.indexOf("@") > -1)
			{
				debugInfo.add(new String[]{"" + deliverUrl, "Error", "Not valid server url"});
			}
			else
			{
				try
				{
					Boolean serverStatus = LiveInstanceMonitor.getInstance().getServerStatus(deliverUrl);
					logger.info("serverStatus:" + serverStatus);
					if(!serverStatus)
					{
						debugInfo.add(new String[]{"" + deliverUrl, "Error", "Server not available for status query"});					
					}
					else
					{
						HttpHelper httpHelper = new HttpHelper();
						String response = httpHelper.getUrlContent(address, 2000);
						logger.info("response:" + response);
						if(response != null && response.indexOf("status=Unknown;") > -1)
						{
							debugInfo.add(new String[]{"" + deliverUrl, "N/A", "No information found"});							
						}
						else
						{
							/*
							Map<String,String> responseMap = httpHelper.toMap(response.trim(), "utf-8");
							CacheEvictionBean bean = CacheEvictionBean.getCacheEvictionBean(responseMap);
							if(bean == null)
								throw new Exception("No information found");
							VisualFormatter visualFormatter = new VisualFormatter();
							*/
							debugInfo.add(new String[]{"" + deliverUrl, response.trim(), ""});
						}
					}
				}
				catch(Exception e)
				{
					logger.error("Problem getting publication status:" + e.getMessage());
					debugInfo.add(new String[]{"" + deliverUrl, "Error", "" + e.getMessage()});
				}

			}
		}
		
		if(forceClear)
		{
			StringBuffer message = new StringBuffer("<div><h1>Forced publication made (Was entity processed by all servers: " + isEntityPublicationProcessed + ")</h1>");
			for(String[] debugArray : debugInfo)
			{
				message.append("<b>Caches to clear on " + debugArray[0] + "</b><br/>");
				for(String row : debugArray[1].split(","))
				{
					message.append("" + row + "<br/>");
					message.append("" + debugArray[2] + "<br/>");
				}
			}

			String subject = "Forced publication made on " + CmsPropertyHandler.getServerName();
	        String warningEmailReceiver = CmsPropertyHandler.getWarningEmailReceiver();

			try 
			{
				logger.warn("" + subject + ": \n" + message);
				MailServiceFactory.getService().sendEmail(CmsPropertyHandler.getMailContentType(), warningEmailReceiver, warningEmailReceiver, null, null, null, null, subject, message.toString(), "utf-8");
			} 
			catch (Exception e) 
			{
				logger.warn("Could not mail info: " + e.getMessage(), e);
			}
		}
		
        return debugInfo;
	}
	
	/**
	 * This method returns a list of all details a publication has.
	 */
	public static Date getApplicationStartDate(String deliverUrl) throws SystemException
	{
		Date serverStartupDate = new Date();
		
		String address = deliverUrl + "/UpdateCache!getPublicationState.action?publicationId=1";
		try
		{
			Boolean serverStatus = LiveInstanceMonitor.getInstance().getServerStatus(deliverUrl);
			if(!serverStatus)
			{
				throw new Exception("Server down... could not get start date");					
			}
			else
			{
				HttpHelper httpHelper = new HttpHelper();
				String response = httpHelper.getUrlContent(address, 2000);
				logger.info("response:" + response);
				if(response != null && response.indexOf("status=Unknown;") > -1)
				{
					if(response.indexOf("serverStartDateTime:") > -1)
					{
						String applicationStartupDateTimeString = response.substring(response.indexOf("serverStartDateTime:") + 20).trim();
						logger.info("applicationStartupDateTimeString:" + applicationStartupDateTimeString);
						VisualFormatter visualFormatter = new VisualFormatter();
						serverStartupDate = visualFormatter.parseDate(applicationStartupDateTimeString, "yyyy-MM-dd HH:mm:ss");
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Problem getting application startup date:" + e.getMessage());
		}
		
        return serverStartupDate;
	}
	
	
	/**
	 * This method fetches a json-list from the live server in question with all ongoing publications.
	 */
	public static List<CacheEvictionBean> getOngoingPublicationStatusList(String baseUrl)
	{
		List<CacheEvictionBean> beans = new ArrayList<CacheEvictionBean>();
		
		String address = baseUrl + "/UpdateCache!getOngoingPublications.action";
		try
		{
			HttpHelper httpHelper = new HttpHelper();
			String response = httpHelper.getUrlContent(address, 2000);
			
			Gson gson = new Gson();
			
			java.lang.reflect.Type listOfCacheEvictionBeans = new TypeToken<List<CacheEvictionBean>>(){}.getType();
			beans = gson.fromJson(response, listOfCacheEvictionBeans);
		}
		catch(Exception e)
		{
			logger.error("Error getting ongoing publication status:" + e.getMessage());
		}
		
        return beans;
	}

	
	public List<PublicationVO> getFailedPublicationVOList(String baseUrl)
	{
		List<PublicationVO> failedPublications = new ArrayList<PublicationVO>();
		
		try
		{
			Calendar minus24hours = Calendar.getInstance();
			minus24hours.add(Calendar.HOUR_OF_DAY, -24);

			Date applicationStartupDate = getApplicationStartDate(baseUrl);
			if(applicationStartupDate.after(minus24hours.getTime()))
				minus24hours.setTime(applicationStartupDate);

			List<PublicationVO> publicationsToCheck = getPublicationsSinceDate(minus24hours.getTime());
			List<CacheEvictionBean> latestPublications = getLatestPublicationList(baseUrl);
			for(PublicationVO publication : publicationsToCheck)
			{
				boolean found = false;
				if(publication.getName().equals("Infoglue Calendar publication"))
				{
					found = true;
				}
				else
				{
					for(CacheEvictionBean bean : latestPublications)
					{
						if(bean.getPublicationId().equals(publication.getId()))
							found = true;
					}
					if(!found)
						failedPublications.add(publication);
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("Error getting failed publications:" + e.getMessage());
		}
		
		return failedPublications;
	}
	
	/**
	 * This method fetches a json-list from the live server in question with all ongoing publications.
	 */
	public static List<CacheEvictionBean> getLatestPublicationList(String baseUrl)
	{
		List<CacheEvictionBean> beans = new ArrayList<CacheEvictionBean>();
		
		String address = baseUrl + "/UpdateCache!getLatestPublications.action";
		try
		{
			HttpHelper httpHelper = new HttpHelper();
			String response = httpHelper.getUrlContent(address, 2000);
			
			Gson gson = new Gson();
			
			java.lang.reflect.Type listOfCacheEvictionBeans = new TypeToken<List<CacheEvictionBean>>(){}.getType();
			beans = gson.fromJson(response, listOfCacheEvictionBeans);
		}
		catch(Exception e)
		{
			logger.error("Error getting latest publication:" + e.getMessage());
		}
		
        return beans;
	}

	
	/**
	 * This method unpublishes all entities in an edition if they are not unpublish-events.
	 */
	public static PublicationVO unPublish(Integer publicationId, InfoGluePrincipal infoGluePrincipal) throws SystemException
	{
		logger.info("Starting unpublishing operation...");

		Database db = CastorDatabaseService.getDatabase();
		Publication publication = null;

        beginTransaction(db);

        try
        {
			publication = getPublicationWithId(publicationId, db);
			Collection publicationDetails = publication.getPublicationDetails();

			Iterator i = publicationDetails.iterator();
			while (i.hasNext())
			{
				PublicationDetail publicationDetail = (PublicationDetail)i.next();
				logger.info("publicationDetail:" + publicationDetail.getId() + ":" + publicationDetail.getTypeId());
				//We unpublish them as long as they are not unpublish-requests.
				if(publicationDetail.getTypeId().intValue() != PublicationDetailVO.UNPUBLISH_LATEST.intValue())
				{
					unpublishEntity(publicationDetail, infoGluePrincipal, db);
				}
				else
				{
				    republishEntity(publicationDetail, infoGluePrincipal, db);
				}
			}

            db.remove(publication);

			commitTransaction(db);
			logger.info("Done unpublishing operation...");
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        try
		{
			logger.info("Starting replication operation...");
			ReplicationMySqlController.updateSlaveServer();
			logger.info("Done replication operation...");
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

		//Update live site!!!
		try
		{
			logger.info("Notifying the entire system about an unpublishing...");
			NotificationMessage notificationMessage = new NotificationMessage("PublicationController.unPublish():", PublicationImpl.class.getName(), infoGluePrincipal.getName(), NotificationMessage.UNPUBLISHING, publication.getId(), publication.getName());
	      	ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
	      	RemoteCacheUpdater.pushAndClearSystemNotificationMessages(infoGluePrincipal);
	      	//RemoteCacheUpdater.clearSystemNotificationMessages();
	      	logger.info("Finished Notifying...");
		}
		catch (Exception e)
		{
			logger.error("An error occurred when we tried to replicate the data:" + e.getMessage(), e);
		}

        return publication.getValueObject();
	}


	/**
	 * Unpublished a entity by just setting it to isActive = false.
	 */
	private static void unpublishEntity(PublicationDetail publicationDetail, InfoGluePrincipal infoGluePrincipal, Database db) throws ConstraintException, SystemException
	{
		Integer repositoryId = null;

		try
		{
			if(publicationDetail.getEntityClass().equals(ContentVersion.class.getName()))
			{
				ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(publicationDetail.getEntityId(), db);
				contentVersion.setIsActive(new Boolean(false));
				repositoryId = contentVersion.getOwningContent().getRepository().getId();
			}
			else if(publicationDetail.getEntityClass().equals(SiteNodeVersion.class.getName()))
			{
			    SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(publicationDetail.getEntityId(), db);
			    siteNodeVersion.setIsActive(new Boolean(false));
				repositoryId = siteNodeVersion.getOwningSiteNode().getRepository().getId();
			}
	
			EventVO eventVO = new EventVO();
			eventVO.setDescription(publicationDetail.getDescription());
			eventVO.setEntityClass(publicationDetail.getEntityClass());
			eventVO.setEntityId(publicationDetail.getEntityId());
			eventVO.setName(publicationDetail.getName());
			eventVO.setTypeId(EventVO.PUBLISH);
			EventController.create(eventVO, repositoryId, infoGluePrincipal, db);
		}
		catch(Exception e)
		{
		    logger.info("Could not unpublish entity:" + e.getMessage(), e);
		}
	}
	
	/**
	 * Republished an entity by just setting it to isActive = true.
	 */
	private static void republishEntity(PublicationDetail publicationDetail, InfoGluePrincipal infoGluePrincipal, Database db) throws ConstraintException, SystemException
	{
		Integer repositoryId = null;

		try
		{
		    boolean createEvent = false;
		    
			if(publicationDetail.getEntityClass().equals(ContentVersion.class.getName()))
			{
				ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(publicationDetail.getEntityId(), db);
				if(contentVersion.getOwningContent() != null)
				{
					contentVersion.setIsActive(new Boolean(true));
					repositoryId = contentVersion.getOwningContent().getRepository().getId();
			 	    createEvent = true;
				}
			 	else
			 	{
			 	    logger.warn("The contentVersion:" + contentVersion.getId() + " had no content - this should never happen, investigate why. Removing invalid content version.");
					ContentVersionController.getContentVersionController().delete(contentVersion, db);
			 	}
			}
			else if(publicationDetail.getEntityClass().equals(SiteNodeVersion.class.getName()))
			{
			 	SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(publicationDetail.getEntityId(), db);
			 	if(siteNodeVersion.getOwningSiteNode() != null)
			 	{
			 	    siteNodeVersion.setIsActive(new Boolean(true));
			 	    repositoryId = siteNodeVersion.getOwningSiteNode().getRepository().getId();
			 	    createEvent = true;
			 	}
			 	else
			 	{
			 	    logger.warn("The siteNodeVersion:" + siteNodeVersion.getId() + " had no siteNode - this should never happen, investigate why. Removing invalid sitenode version.");
			 	    SiteNodeVersionController.getController().delete(siteNodeVersion, db);
			 	}
			}
	
			if(createEvent)
			{
				EventVO eventVO = new EventVO();
				eventVO.setDescription(publicationDetail.getDescription());
				eventVO.setEntityClass(publicationDetail.getEntityClass());
				eventVO.setEntityId(publicationDetail.getEntityId());
				eventVO.setName(publicationDetail.getName());
				eventVO.setTypeId(EventVO.UNPUBLISH_LATEST);
				EventController.create(eventVO, repositoryId, infoGluePrincipal, db);
			}
		}
		catch(Exception e)
		{
		    logger.warn("Could not republish entity:" + e.getMessage(), e);
		}
	}


	/**
	 * This method returns the owning content to a contentVersion.
	 */
	public static ContentVO getOwningContentVO(Integer id) throws SystemException
    {
	    ContentVO contentVO = null;

    	Database db = CastorDatabaseService.getDatabase();
		ContentVersion contentVersion = null;
        beginTransaction(db);
        try
        {
	    	contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(id, db);
	    	contentVO = contentVersion.getOwningContent().getValueObject();
	    	//Content content = ContentController.getContentController().getContentWithId(contentVersion.getValueObject().getContentId(), db);
	    	//contentVO = content.getValueObject();

	    	commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

    	return contentVO;
    }

	/**
	 * This method returns the owning siteNode to a siteNodeVersion.
	 */
    public static SiteNodeVO getOwningSiteNodeVO(Integer id) throws SystemException
    {
    	Database db = CastorDatabaseService.getDatabase();
		SiteNodeVersion siteNodeVersion = null;
        beginTransaction(db);
        try
        {
	    	siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(id, db);
	    	commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

    	return siteNodeVersion.getOwningSiteNode().getValueObject();
    }


	/**
	 * This method mails a notification about items available for publish to the recipient of choice.
	 */
	
	public static void mailPublishNotification(List<EventVO> resultingEvents, Integer repositoryId, InfoGluePrincipal principal, String recipientFilter, boolean unpublishRequest) throws Exception
	{
		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);
        try
        {
        	mailPublishNotification(resultingEvents, repositoryId, principal, recipientFilter, unpublishRequest, db);
	    	commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}
	
	/**
	 * This method mails a notification about items available for publish to the recipient of choice.
	 */
	
	public static void mailPublishNotification(List<EventVO> resultingEvents, Integer repositoryId, InfoGluePrincipal principal, String recipientFilter, boolean unpublishRequest, Database db)
	{
	    try
	    {
	    	logger.info("recipientFilter:" + recipientFilter);
	    	String recipients = recipientFilter;
	    	if(recipients == null || recipients.equals("") || !recipients.contains("@"))
	    		recipients = getRecipients(principal, repositoryId, recipientFilter, db);
	    	
		    logger.info("recipients:" + recipients);
		    if(recipients == null || recipients.length() == 0)
		    	return;
		    
	        String contentType = CmsPropertyHandler.getMailContentType();
	        if(contentType == null || contentType.length() == 0)
	            contentType = "text/html";
	        
	        String template;
	        if(contentType.equalsIgnoreCase("text/plain"))
	            template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/publishingtool/newPublishItem_plain.vm"));
		    else
	            template = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/publishingtool/newPublishItem_html.vm"));
		    
	        List<String> eventHref = new ArrayList<String>();
	        String comment = "";
	        for(EventVO event : resultingEvents)
	        {
	        	if(CmsPropertyHandler.getInternalDeliveryUrls().size() > 0)
	        	{
	        		String cmsBaseUrl = (String)CmsPropertyHandler.getCmsFullBaseUrl();
	        		Integer siteNodeId = null;
	        		Integer languageId = null;
	        		String approveEntityName = null;
	        		String approveEntityId = null;
	        		String extraInfo = null;
	        		
	        		if(event.getEntityClass().contains("SiteNodeVersion"))
	        		{
	        			SiteNodeVersionVO snVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(event.getEntityId(), db);
	        			siteNodeId = snVersionVO.getSiteNodeId();
	        			approveEntityName = "SiteNode";
	        			approveEntityId = ""+siteNodeId;
	        		}
	        		else if(event.getEntityClass().contains("ContentVersion"))
	        		{
	        			logger.info("event.getEntityClass():" + event.getEntityClass());
	        			ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(event.getEntityId(), db);
	        			LanguageVO language = LanguageController.getController().getLanguageVOWithId(cvVO.getLanguageId(), db);
	        			languageId = language.getId();
	        			approveEntityName = "Content";
	        			approveEntityId = ""+cvVO.getContentId();
	        			extraInfo = "" + language.getDisplayLanguage() + "";
	        			
	        			List<ReferenceBean> referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(cvVO.getContentId(), 100, true, true);
	        			for(ReferenceBean bean : referenceBeanList)
	        			{
	        				logger.info("bean.getReferencingCompletingObject():" + bean.getReferencingCompletingObject().getClass().getName());
	        				if(bean.getReferencingCompletingObject().getClass().getName().contains("SiteNodeVO"))
	        				{
	        					siteNodeId = ((SiteNodeVO)bean.getReferencingCompletingObject()).getId();
	        				}
	        				if(bean.getReferencingCompletingObject().getClass().getName().contains("ContentVersion"))
	        				{
		    		        	//String editOnSightUrl = deliveryBaseUrl + "/ViewPage!renderDecoratedPage.action?siteNodeId=" + siteNodeId;
		        				//eventHref.add("<a href=\"" + editOnSightUrl + "\">" + event.getName() + "-" + event.getDescription() + "</a>");
	        				}
	        				else if(bean.getReferencingCompletingObject().getClass().getName().contains("Content"))
	        				{
	        					ContentVO contentVO = ContentController.getContentController().getContentVOWithId(((ContentVO)bean.getReferencingCompletingObject()).getId(), db);
	    	        			List<ReferenceBean> referenceBeanListLevel2 = RegistryController.getController().getReferencingObjectsForContent(contentVO.getContentId(), 100, true, true);
	    	        			for(ReferenceBean beanLevel2 : referenceBeanListLevel2)
	    	        			{
	    	        				logger.info("beanLevel2.getReferencingCompletingObject():" + beanLevel2.getReferencingCompletingObject().getClass().getName());
	    	        				if(beanLevel2.getReferencingCompletingObject().getClass().getName().contains("SiteNode"))
	    	        				{
	    	        					logger.info("Page:" + ((SiteNodeVO)beanLevel2.getReferencingCompletingObject()).getName());
	    	        					siteNodeId = ((SiteNodeVO)beanLevel2.getReferencingCompletingObject()).getId();
	    		    		        	//String editOnSightUrl = deliveryBaseUrl + "/ViewPage!renderDecoratedPage.action?siteNodeId=" + ((SiteNodeVO)bean.getReferencingCompletingObject()).getId();
	    	        					//eventHref.add("<a href=\"" + editOnSightUrl + "\">" + event.getName() + "-" + event.getDescription() + "</a>");
	    	        				}
	    	        				/*
	    	        				else
	    	        				{
	    	        					
	    	        				}
	    	        				*/
	    	        			}	        					
	        				}
	        			}
	        		}
	        		
	        		String editOnSightUrl = cmsBaseUrl.replaceFirst("infoglueCMS", "infoglueDeliverWorking") + "/ViewPage!renderDecoratedPage.action?siteNodeId=" + siteNodeId + (languageId != null ? "&languageId=" + languageId : "") + "&approveEntityName=" + approveEntityName + "&approveEntityId=" + approveEntityId + "&publishingEventId=" + event.getEventId();
	        		if(unpublishRequest && approveEntityName.equals("SiteNode"))
	        			editOnSightUrl = cmsBaseUrl + "/Admin.action?siteNodeId=" + approveEntityId;
	        		else if(unpublishRequest && approveEntityName.equals("Content"))
	        			editOnSightUrl = cmsBaseUrl + "/Admin.action?contentId=" + approveEntityId;
	        		
	        		if(siteNodeId == null)
		        		editOnSightUrl = cmsBaseUrl + "/Admin.action?contentId=" + approveEntityId;
		        	
		        	if(resultingEvents.size() > 1)
		        		comment = event.getDescription();
		        						
		        	if(resultingEvents.size() > 1)
		        		eventHref.add("<a href=\"" + editOnSightUrl + "\">" + event.getName() + (extraInfo != null ? " (" + extraInfo + ")" : "") + "</a>");
		        	else
		        		eventHref.add("<a href=\"" + editOnSightUrl + "\">" + event.getName() + (extraInfo != null ? " (" + extraInfo + ")" : "") + " - " + event.getDescription() + "</a>");
	        	}
	        }

	        
	        LabelController lc = LabelController.getController(new Locale(CmsPropertyHandler.getPreferredLanguageCode(principal.getName())));
	        
	        Map<String,Object> parameters = new HashMap<String,Object>();
	        parameters.put("ui", lc);
	        parameters.put("unpublishRequest", unpublishRequest);
	        parameters.put("comment", comment);
	        parameters.put("events", resultingEvents);
		    parameters.put("principal", principal);
		    parameters.put("eventHrefs", eventHref);
		    //parameters.put("referenceUrl", referenceUrl);
			
			StringWriter tempString = new StringWriter();
			PrintWriter pw = new PrintWriter(tempString);
			new VelocityTemplateProcessor().renderTemplate(parameters, pw, template);
			String email = tempString.toString();
	    
			String systemEmailSender = CmsPropertyHandler.getSystemEmailSender();
			if(systemEmailSender == null || systemEmailSender.equalsIgnoreCase(""))
				systemEmailSender = "InfoGlueCMS@" + CmsPropertyHandler.getMailSmtpHost();

			String subjectLabel = lc.getString("tool.publishingtool.itemsSubmitted.emailSubject", principal.getFirstName() + " " + principal.getLastName(), resultingEvents.size());
			MailServiceFactory.getService().sendEmail(contentType, systemEmailSender, recipients, null, null, null, null, subjectLabel, email, "utf-8");
	    }
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("The notification was not sent. Reason:" + e.getMessage(), e);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			logger.error("The notification was not sent. Reason:" + t.getMessage(), t);
		}
	}

	private static String getRecipients(InfoGluePrincipal principal, Integer repositoryId, String recipientFilter, Database db) throws Exception
	{
		if(recipientFilter != null && recipientFilter.equals(""))
			return null;
		
		String recipients = "";

    	if(recipientFilter.indexOf("testUsers") > -1)
    	{
    		return "mattias.bogeblad@knowit.se;tomas.edquist@uadm.uu.se";
    	}

		List users = new ArrayList();
		
		/*
		if(recipientFilter.equalsIgnoreCase("all"))
    	{
    		users = UserControllerProxy.getController(db).getAllUsers();
    	}
    	else */
		if(recipientFilter.equalsIgnoreCase("groupBased"))
    	{
    		Iterator groupsIterator = principal.getGroups().iterator();
    		
    		while(groupsIterator.hasNext())
	    	{
    			InfoGlueGroup infoGlueGroup = (InfoGlueGroup)groupsIterator.next();
    			users = GroupControllerProxy.getController(db).getInfoGluePrincipals(infoGlueGroup.getName());
	    	}
    	}
    	else if(recipientFilter.indexOf("groupNameBased_") > -1)
    	{
    		String groupName = recipientFilter.substring(recipientFilter.indexOf("_") + 1);
    		users = GroupControllerProxy.getController(db).getInfoGluePrincipals(groupName);
    	}
    	
    	Iterator usersIterator = users.iterator();
		while(usersIterator.hasNext())
		{
			InfoGluePrincipal infogluePrincipal = (InfoGluePrincipal)usersIterator.next();
			if(infogluePrincipal.getGroups() == null || infogluePrincipal.getGroups().size() == 0)
				infogluePrincipal = UserControllerProxy.getController(db).getUser(infogluePrincipal.getName());
					
			boolean hasAccessToPublishingTool = hasAccessTo("PublishingTool.Read", infogluePrincipal);
			
			if(hasAccessToPublishingTool)
			{
				boolean hasAccessToRepository = hasAccessTo("Repository.Read", "" + repositoryId, infogluePrincipal);
				if(!hasAccessToRepository)
					hasAccessToRepository = hasAccessTo("Repository.Write", "" + repositoryId, infogluePrincipal);
				if(hasAccessToRepository)
				{
					if(recipients.indexOf(infogluePrincipal.getEmail()) == -1)
					{
						if(recipients.length() > 0)
			    			recipients += ";";

			    		recipients += infogluePrincipal.getEmail();
					}
				}
			}
		}
    	
		return recipients;
	}

	public static boolean hasAccessTo(String interceptionPointName, InfoGluePrincipal principal)
	{
		try
		{
			return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}

	public static boolean hasAccessTo(String interceptionPointName, String extraParameter, InfoGluePrincipal principal)
	{
		try
		{
		    return AccessRightController.getController().getIsPrincipalAuthorized(principal, interceptionPointName, extraParameter);
		}
		catch (SystemException e)
		{
		    logger.warn("Error checking access rights", e);
			return false;
		}
	}


	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */
	public BaseEntityVO getNewVO()
	{
		return new PublicationVO();
	}
}

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

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.Timer;
import org.infoglue.deliver.util.graphics.ColorHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SearchController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SearchController.class.getName());
    
    public static SearchController getController()
    {
    	return new SearchController();
    }
    
	public static String getAttributeValue(String xml,String key)
	{
		String value = "";
			try
	        {
		        InputSource inputSource = new InputSource(new StringReader(xml));
				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				Document document = parser.getDocument();
				NodeList nl = document.getDocumentElement().getChildNodes();
				Node n = nl.item(0);
				nl = n.getChildNodes();

				for(int i=0; i<nl.getLength(); i++)
				{
					n = nl.item(i);
					if(n.getNodeName().equalsIgnoreCase(key))
					{
						value = n.getFirstChild().getNodeValue();
						break;
					}
				}		        	
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		if(value.equalsIgnoreCase(""))value="This Content is Unititled";
		return value;
	}

	public static String setScoreImg(double score)
	{
		if( 2.0 <  score){
			return "5star.gif";					
		}
		else if( 1.0 <  score){
			return "4star.gif";					
		}
		else if( 0.6 <  score){
			return "3star.gif";					
		}
		else if( 0.4 <  score){
			return "2star.gif";					
		}
		else{
			return "1star.gif";	
		}
	}

   	public Set getContents(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
   		return getContents(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId, false);
   	}

   	public Set getContents(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId, boolean searchAsset) throws SystemException, Bug
   	{
   		return getContents(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId, searchAsset);
   	}

   	public Set getContents(Integer[] repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
   		Set contents = new HashSet();
   		List contentVersions = getContentVersionVOList(repositoryId, searchString, maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId);
   		Iterator contentVersionsIterator = contentVersions.iterator();
   		while(contentVersionsIterator.hasNext())
   		{
   			ContentVersionVO contentVersionVO = (ContentVersionVO)contentVersionsIterator.next();
   			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
   			contents.add(contentVO);
   		}
   		
   		return contents;
   	}

   	public Set getContents(Integer[] repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
   		Set contents = new HashSet();
   		List contentVersions = getContentVersionVOList(repositoryId, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, searchAssets, false, new HashMap<String,Integer>());
   		Iterator contentVersionsIterator = contentVersions.iterator();
   		while(contentVersionsIterator.hasNext())
   		{
   			ContentVersionVO contentVersionVO = (ContentVersionVO)contentVersionsIterator.next();
   			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
   			contents.add(contentVO);
   		}
   		
   		return contents;
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
		return getContentVersionVOList(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId);
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
		return getContentVersionVOList(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, searchAssets, false, new HashMap<String,Integer>());
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId, boolean searchAssets, boolean includeSiteNodes) throws SystemException, Bug
   	{
		return getContentVersionVOList(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, searchAssets, includeSiteNodes, new HashMap<String,Integer>());
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
   		return getContentVersionVOList(repositoryId, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, false, false, new HashMap<String,Integer>());
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
   		return getContentVersionVOList(repositoryId, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, searchAssets, false, new HashMap<String,Integer>());
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionId, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
   		return getContentVersionVOList(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets, false, new HashMap<String,Integer>());
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionId, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean searchAssets, boolean includeSiteNodes, Map<String,Integer> searchMetaData) throws SystemException, Bug
   	{
   		String internalSearchEngine = CmsPropertyHandler.getInternalSearchEngine();
   		if(internalSearchEngine.equalsIgnoreCase("sqlSearch"))
   			return getContentVersionVOListFromCastor(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets);
   		else
   			return getContentVersionVOListFromLucene(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets, includeSiteNodes, null, searchMetaData);
   	}

   	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionId, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean searchAssets, boolean includeSiteNodes, Map<String,Integer> searchMetaData) throws SystemException, Bug
   	{
   		//String internalSearchEngine = CmsPropertyHandler.getInternalSearchEngine();
   		//if(internalSearchEngine.equalsIgnoreCase("sqlSearch"))
   		//	return getSiteNodeVersionVOListFromCastor(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets);
   		//else
   			return getSiteNodeVersionVOListFromLucene(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets, includeSiteNodes, null, searchMetaData);
   	}

   	private List<ContentVersionVO> getContentVersionVOListFromCastor(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionId, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
		List<ContentVersionVO> matchingContents = new ArrayList<ContentVersionVO>();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);

			int index = 3;
			List repArguments = new ArrayList();

			String repositoryArgument = "";
			if(repositoryId != null && repositoryId.length > 0)
			{
				repositoryArgument = " AND (";
				for(int i=0; i<repositoryId.length; i++)
				{
					if(i > 0)
						repositoryArgument += " OR ";
					
					repositoryArgument += "cv.owningContent.repository.repositoryId = $" + index;
				    repArguments.add(repositoryId[i]);
					index++;
				}
				repositoryArgument += ")";
			}
			
			//int index = 4;
			String extraArguments = "";
			String inverse = "";
			List arguments = new ArrayList();
						
			if(userName != null && !userName.equalsIgnoreCase(""))
			{
			    extraArguments += " AND cv.versionModifier = $" + index;
			    arguments.add(userName);
				index++;
			}
			if(languageId != null)
			{
			    extraArguments += " AND cv.language = $" + index;
			    arguments.add(languageId);
				index++;
			}
			if(contentTypeDefinitionId != null && contentTypeDefinitionId.length > 0 && contentTypeDefinitionId[0] != null)
			{
				extraArguments += " AND(";
				for(int i=0; i<contentTypeDefinitionId.length; i++)
				{
					if(i==0)
						extraArguments += " cv.owningContent.contentTypeDefinition = $" + index;
					else
						extraArguments += " OR cv.owningContent.contentTypeDefinition = $" + index;
						
					arguments.add(contentTypeDefinitionId[i]);
					index++;
				}
				extraArguments += ")";
			}
			if(excludedContentTypeDefinitionIds != null && excludedContentTypeDefinitionIds.length > 0)
			{
				for(int i=0; i<excludedContentTypeDefinitionIds.length; i++)
				{
					extraArguments += " AND cv.owningContent.contentTypeDefinition != $" + index + "";
						
					arguments.add(excludedContentTypeDefinitionIds[i]);
					index++;
				}
			}
			if(stateId != null)
			{
			    extraArguments += " AND cv.stateId = $" + index;
			    arguments.add(stateId);
				index++;
			}
			    
			String sql = "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl cv WHERE cv.isActive = $1 AND cv.versionValue LIKE $2 " + repositoryArgument + extraArguments + " ORDER BY cv.owningContent asc, cv.language, cv.contentVersionId desc";
			logger.info("sql:" + sql);
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(new Boolean(true));
			oql.bind("%" + searchString + "%");
			Iterator repIterator = repArguments.iterator();
			while(repIterator.hasNext())
			{
				Integer repositoryIdAsInteger = (Integer)repIterator.next();
				oql.bind(repositoryIdAsInteger);
			}
	        
			Iterator iterator = arguments.iterator();
			while(iterator.hasNext())
			{
				Object value = iterator.next();
			    oql.bind(value);
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			Integer previousContentId  = new Integer(-1);
			Integer previousLanguageId = new Integer(-1);  	
			int currentCount = 0;
			while(results.hasMore() && currentCount < maxRows) 
			{
				ContentVersion contentVersion = (ContentVersion)results.next();
				logger.info("Found a version matching " + searchString + ":" + contentVersion.getId() + "=" + contentVersion.getOwningContent().getName());
				if(contentVersion.getOwningContent().getId().intValue() != previousContentId.intValue() || contentVersion.getLanguage().getId().intValue() != previousLanguageId.intValue())
				{
				    ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getOwningContent().getId(), contentVersion.getLanguage().getId(), db);
					if(latestContentVersion.getId().intValue() == contentVersion.getId().intValue() && (caseSensitive == null || contentVersion.getVersionValue().indexOf(searchString) > -1))
					{
						if(!searchAssets || (contentVersion.getDigitalAssets() != null && contentVersion.getDigitalAssets().size() > 0))
						{
						    matchingContents.add(contentVersion.getValueObject());
						    previousContentId = contentVersion.getOwningContent().getId();
						    previousLanguageId = contentVersion.getLanguage().getId();
						    currentCount++;
						}
					}
				}
			}

			results.close();
			oql.close();

			if(searchAssets)
			{
				String assetSQL = "SELECT da FROM org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl da WHERE (da.assetKey LIKE $1 OR da.assetFileName LIKE $2) ORDER BY da.digitalAssetId asc";
				logger.info("assetSQL:" + assetSQL);
				OQLQuery assetOQL = db.getOQLQuery(assetSQL);
				assetOQL.bind("%" + searchString + "%");
				assetOQL.bind("%" + searchString + "%");
		        
				QueryResults assetResults = assetOQL.execute(Database.ReadOnly);
				
				previousContentId  = new Integer(-1);
				previousLanguageId = new Integer(-1);  	
				currentCount = 0;
				
				while(assetResults.hasMore() && currentCount < maxRows) 
				{
					SmallDigitalAssetImpl smallAsset = (SmallDigitalAssetImpl)assetResults.next();
					DigitalAsset asset = DigitalAssetController.getMediumDigitalAssetWithId(smallAsset.getId(), db);
					logger.info("Found a asset matching " + searchString + ":" + asset.getId());
					Collection versions = asset.getContentVersions();
					Iterator versionsIterator = versions.iterator();
					while(versionsIterator.hasNext())
					{
						ContentVersion contentVersion = (ContentVersion)versionsIterator.next();
						if(contentVersion.getOwningContent().getId().intValue() != previousContentId.intValue() || contentVersion.getLanguage().getId().intValue() != previousLanguageId.intValue())
						{
						    ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getOwningContent().getId(), contentVersion.getLanguage().getId(), db);
							if(latestContentVersion.getId().intValue() == contentVersion.getId().intValue() && (caseSensitive == null || contentVersion.getVersionValue().indexOf(searchString) > -1))
							{
							    matchingContents.add(contentVersion.getValueObject());
							    previousContentId = contentVersion.getOwningContent().getId();
							    previousLanguageId = contentVersion.getLanguage().getId();
							    currentCount++;
							}
						}						
					}
				}
				
				assetResults.close();
				assetOQL.close();
			}
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingContents;
		
   	}
   	
   	/**
   	 * Gets all content versions last changed by a certain user.
   	 * 
   	 * @param userName
   	 * @return
   	 * @throws SystemException
   	 * @throws Bug
   	 */
   	
   	public static Set getContentVersions(Integer contentTypeDefinitionId, String userName, Date publishStartDate, Date publishEndDate, Date unpublishStartDate, Date unpublishEndDate) throws SystemException, Bug
   	{
		Set matchingContentVersions = new HashSet();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			int index = 2;
			/*
			String repositoryArgument = ""; //" AND (";
			List repArguments = new ArrayList();
			
			for(int i=0; i<repositoryId.length; i++)
			{
				if(i > 0)
					repositoryArgument += " OR ";
				
				repositoryArgument += "cv.owningContent.repository.repositoryId = $" + index;
			    repArguments.add(repositoryId[i]);
				index++;
			}
			//repositoryArgument += ")";
			*/
			
			String extraArguments = "";
			String inverse = "";
			List arguments = new ArrayList();
						
			if(userName != null && !userName.equalsIgnoreCase(""))
			{
			    extraArguments += " cv.versionModifier = $" + index;
			    //extraArguments += " AND cv.versionModifier = $" + index;
			    arguments.add(userName);
				index++;
			}
			/*
			if(languageId != null)
			{
			    extraArguments += " AND cv.language = $" + index;
			    arguments.add(languageId);
				index++;
			}
			*/
			
			if(contentTypeDefinitionId != null)
			{
			    extraArguments += " AND cv.owningContent.contentTypeDefinition = $" + index;
			    arguments.add(contentTypeDefinitionId);
				index++;
			}
			
			/*
			if(stateId != null)
			{
			    extraArguments += " AND cv.stateId = $" + index;
			    arguments.add(stateId);
				index++;
			}
			*/
			if(publishStartDate != null)
			{
			    extraArguments += " AND cv.owningContent.publishDateTime > $" + index;
			    arguments.add(publishStartDate);
				index++;
			}
			if(publishEndDate != null)
			{
			    extraArguments += " AND cv.owningContent.publishDateTime < $" + index;
			    arguments.add(publishEndDate);
				index++;
			}
			if(unpublishStartDate != null)
			{
			    extraArguments += " AND cv.owningContent.expireDateTime > $" + index;
			    arguments.add(unpublishStartDate);
				index++;
			}
			if(unpublishEndDate != null)
			{
			    extraArguments += " AND cv.owningContent.expireDateTime < $" + index;
			    arguments.add(unpublishEndDate);
				index++;
			}
			    
			String sql = "SELECT cv FROM org.infoglue.cms.entities.content.impl.simple.FullContentVersionImpl cv WHERE cv.isActive = $1 AND " /*+ repositoryArgument*/ + extraArguments + " ORDER BY cv.contentId asc, cv.language, cv.contentVersionId desc";
			if(logger.isInfoEnabled())
				logger.info("sql:" + sql);

			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(new Boolean(true));
			
			Iterator iterator = arguments.iterator();
			while(iterator.hasNext())
			{
			    oql.bind(iterator.next());
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			while(results.hasMore()) 
			{
				ContentVersion contentVersion = (ContentVersion)results.next();
				if(logger.isInfoEnabled())
					logger.info("Found a version matching:" + contentVersion.getId() + ":" + contentVersion.getOwningContent().getExpireDateTime());
				
				ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getValueObject().getContentId(), contentVersion.getValueObject().getLanguageId(), db);
				if(latestContentVersion.getId().intValue() == contentVersion.getId().intValue())
				{
					matchingContentVersions.add(contentVersion.getValueObject());
				}
			}

			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingContentVersions;
		
   	}
   	
   	public List<BaseEntityVO> getBaseEntityVOListFromCastor(Integer entityId) throws SystemException, Bug
   	{
   		List<BaseEntityVO> matchingEntities = new ArrayList<BaseEntityVO>();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			try
			{
				BaseEntityVO entityVO = ContentController.getContentController().getContentVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { logger.error("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { logger.error("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = DigitalAssetController.getController().getDigitalAssetVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { logger.error("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = SiteNodeController.getController().getSiteNodeVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { logger.error("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { logger.error("No entity found.."); }
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingEntities;
		
   	}
   	
   	public List<DigitalAssetVO> getDigitalAssets(Integer[] repositoryId, String searchString, String assetTypeFilter, int maxRows, Map<String,Integer> searchMetaData) throws SystemException, Bug
   	{
   		String internalSearchEngine = CmsPropertyHandler.getInternalSearchEngine();
   		if(internalSearchEngine.equalsIgnoreCase("sqlSearch"))
   			return getDigitalAssetsFromCastor(repositoryId, searchString, assetTypeFilter, maxRows);
   		else
   			return getDigitalAssetsFromLucene(repositoryId, searchString, assetTypeFilter, maxRows, searchMetaData);
   	}

   	public List<DigitalAssetVO> getDigitalAssetsFromCastor(Integer[] repositoryId, String searchString, String assetTypeFilter, int maxRows) throws SystemException, Bug
   	{
   		List<DigitalAssetVO> matchingAssets = new ArrayList<DigitalAssetVO>();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		Database db = CastorDatabaseService.getDatabase();
		
		try
		{
			beginTransaction(db);

			String assetSQL = "SELECT da FROM org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl da WHERE (da.assetKey LIKE $1 OR da.assetFileName LIKE $2) ORDER BY da.digitalAssetId asc";
			logger.info("assetSQL:" + assetSQL);
			OQLQuery assetOQL = db.getOQLQuery(assetSQL);
			assetOQL.bind("%" + searchString + "%");
			assetOQL.bind("%" + searchString + "%");
	        
			QueryResults assetResults = assetOQL.execute(Database.ReadOnly);
			
			Integer previousContentId  = new Integer(-1);
			Integer previousLanguageId = new Integer(-1);  	
			int currentCount = 0;

			while(assetResults.hasMore() && currentCount < maxRows) 
			{
				SmallDigitalAssetImpl smallAsset = (SmallDigitalAssetImpl)assetResults.next();
				//if(smallAsset.getAssetContentType().matches(assetTypeFilter))
				if(assetTypeFilter == null || assetTypeFilter.equals("*") || assetTypeFilter.indexOf(smallAsset.getAssetContentType()) > -1)
				{
					DigitalAsset asset = DigitalAssetController.getMediumDigitalAssetWithId(smallAsset.getId(), db);
					logger.info("Found a asset matching " + searchString + ":" + asset.getId());
					Collection versions = asset.getContentVersions();
					Iterator versionsIterator = versions.iterator();
					while(versionsIterator.hasNext())
					{
						ContentVersion contentVersion = (ContentVersion)versionsIterator.next();
						if(contentVersion.getValueObject().getContentId().intValue() != previousContentId.intValue() || contentVersion.getValueObject().getLanguageId().intValue() != previousLanguageId.intValue())
						{
						    ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getValueObject().getContentId(), contentVersion.getValueObject().getLanguageId(), db);
							if(latestContentVersion != null && latestContentVersion.getId().intValue() == contentVersion.getId().intValue())
							{
								asset.getValueObject().setContentPath(ContentController.getContentController().getContentPath(latestContentVersion.getValueObject().getContentId(), false, true, db));
								asset.getValueObject().setContentId(latestContentVersion.getValueObject().getContentId());

								String assetUrl = getDigitalAssetUrl(asset.getValueObject(), db);
								String assetThumbnailUrl = getDigitalAssetThumbnailUrl(asset.getValueObject().getId(), 100, 60, "ffffff", "center", "middle", 100, 60, 75, db);
								asset.getValueObject().setAssetUrl(assetUrl);
								asset.getValueObject().setAssetThumbnailUrl(assetThumbnailUrl);

								matchingAssets.add(asset.getValueObject());
							    previousContentId = contentVersion.getValueObject().getContentId();
							    previousLanguageId = contentVersion.getValueObject().getLanguageId();
							    currentCount++;
							}
						}						
					}
				}
			}
			
			assetResults.close();
			assetOQL.close();
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingAssets;
   	}
   	
   	public List<DigitalAssetVO> getDigitalAssetsFromLucene(Integer[] repositoryId, String searchString, String assetTypeFilter, int maxRows, Map<String,Integer> searchMetaData) throws SystemException, Bug
   	{
   		List<DigitalAssetVO> matchingAssets = new ArrayList<DigitalAssetVO>();

   		if(searchString != null && !searchString.endsWith("*"))
			searchString = searchString + "*";
   		
		Database db = CastorDatabaseService.getDatabase();
		
		try
		{
			beginTransaction(db);

			String[] fields = new String[]{"isAsset","contents"};
			String[] queries = new String[]{"true","" + searchString};
			BooleanClause.Occur[] flags = new BooleanClause.Occur[]{BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};

			if(assetTypeFilter != null && !assetTypeFilter.equals("*"))
			{
				fields = new String[]{"isAsset","contents","contents"};
				queries = new String[]{"true","" + searchString,"" + assetTypeFilter + "*"};
				flags = new BooleanClause.Occur[]{BooleanClause.Occur.MUST,BooleanClause.Occur.MUST,BooleanClause.Occur.MUST};
			}

			SortField sortField = new SortField("modificationDateTime", SortField.LONG, true);
			Sort sort = new Sort(sortField);
			List<org.apache.lucene.document.Document> documents = LuceneController.getController().queryDocuments(fields, flags, queries, sort, maxRows, searchMetaData);

			for(org.apache.lucene.document.Document document : documents)
			{
				logger.info("document for asset:" + document);
				logger.info("Doc:" + document);
				String digitalAssetIdString = document.get("digitalAssetId");
				if(digitalAssetIdString != null)
				{
					try
					{
						DigitalAssetVO digitalAssetVO = DigitalAssetController.getController().getSmallDigitalAssetVOWithId(Integer.parseInt(digitalAssetIdString), db);
						if(logger.isInfoEnabled())
							logger.info("document:" + document);
						digitalAssetVO.setContentPath(document.get("path"));
						if(document.get("contentId") != null && !document.get("contentId").equals(""))
							digitalAssetVO.setContentId(new Integer(document.get("contentId")));
						
						String assetUrl = getDigitalAssetUrl(digitalAssetVO, db);
						String assetThumbnailUrl = getDigitalAssetThumbnailUrl(digitalAssetVO.getId(), 100, 60, "ffffff", "center", "middle", 100, 60, 75, db);
						digitalAssetVO.setAssetUrl(assetUrl);
						digitalAssetVO.setAssetThumbnailUrl(assetThumbnailUrl);
						
						matchingAssets.add(digitalAssetVO);
					}
					catch (Exception e) 
					{
						logger.warn("Problem getting asset with id: " + digitalAssetIdString + ": " + e.getLocalizedMessage(), e);
					}
				}
			}
		
		
			/*
			while(assetResults.hasMore() && currentCount < maxRows) 
			{
				SmallDigitalAssetImpl smallAsset = (SmallDigitalAssetImpl)assetResults.next();
				//if(smallAsset.getAssetContentType().matches(assetTypeFilter))
				if(assetTypeFilter == null || assetTypeFilter.equals("*") || assetTypeFilter.indexOf(smallAsset.getAssetContentType()) > -1)
				{
					DigitalAsset asset = DigitalAssetController.getMediumDigitalAssetWithId(smallAsset.getId(), db);
					logger.info("Found a asset matching " + searchString + ":" + asset.getId());
					Collection versions = asset.getContentVersions();
					Iterator versionsIterator = versions.iterator();
					while(versionsIterator.hasNext())
					{
						ContentVersion contentVersion = (ContentVersion)versionsIterator.next();
						if(contentVersion.getValueObject().getContentId().intValue() != previousContentId.intValue() || contentVersion.getValueObject().getLanguageId().intValue() != previousLanguageId.intValue())
						{
						    ContentVersion latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVersion.getValueObject().getContentId(), contentVersion.getValueObject().getLanguageId(), db);
							if(latestContentVersion != null && latestContentVersion.getId().intValue() == contentVersion.getId().intValue())
							{
								matchingAssets.add(asset.getValueObject());
							    previousContentId = contentVersion.getValueObject().getContentId();
							    previousLanguageId = contentVersion.getValueObject().getLanguageId();
							    currentCount++;
							}
						}						
					}
				}
			}*/
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingAssets;
		
   	}
   	
   	public static List<DigitalAssetVO> getLatestDigitalAssets(Integer[] repositoryId, String assetTypeFilter, int maxRows) throws SystemException, Bug
   	{
   		List<DigitalAssetVO> matchingAssets = new ArrayList<DigitalAssetVO>();

		Database db = CastorDatabaseService.getDatabase();
		
		try
		{
			beginTransaction(db);
			
			logger.info("assetTypeFilter:" + assetTypeFilter);
			String assetFilterTerm = "";
			int bindIndex = 0;
			String[] assetTypeFilterArray = null;
			if(assetTypeFilter != null && !assetTypeFilter.equals("") && !assetTypeFilter.equals("*"))
			{
				assetTypeFilterArray = assetTypeFilter.split(",");
				StringBuffer contentTypeBindingMarkers = new StringBuffer();
				for(int i=0; i<assetTypeFilterArray.length; i++)
					contentTypeBindingMarkers.append((i>0 ? "," : "") + "$" + (i + 1));
				
				assetFilterTerm = "AND da.assetContentType IN (" + contentTypeBindingMarkers + ")";
				bindIndex = assetTypeFilterArray.length;
			}
			
			StringBuffer repositoryIdBindingMarkers = new StringBuffer();
			for(int i=0; i<repositoryId.length; i++)
				repositoryIdBindingMarkers.append((i>0 ? "," : "") + "$" + (i + 1 + bindIndex));
			
			String assetSQL = "CALL SQL SELECT top " + maxRows + " distinct(da.digitalAssetId),da.assetFileName,da.assetKey,da.assetFilePath,da.assetContentType,da.assetFileSize FROM cmDigitalAsset da, cmContentVersionDigitalAsset cvda, cmContentVersion cv, cmContent c WHERE cvda.digitalAssetId = da.digitalAssetId AND cvda.contentVersionId = cv.contentVersionId AND cv.contentId = c.contentId " + assetFilterTerm + " AND c.repositoryId IN (" + repositoryIdBindingMarkers + ") ORDER BY da.digitalAssetId desc AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl";
			if(CmsPropertyHandler.getDatabaseEngine().equalsIgnoreCase("mysql"))
			{
				assetSQL = "CALL SQL SELECT distinct(da.digitalAssetId),da.assetFileName,da.assetKey,da.assetFilePath,da.assetContentType,da.assetFileSize FROM cmDigitalAsset da, cmContentVersionDigitalAsset cvda, cmContentVersion cv, cmContent c WHERE cvda.digitalAssetId = da.digitalAssetId AND cvda.contentVersionId = cv.contentVersionId AND cv.contentId = c.contentId " + assetFilterTerm + " AND c.repositoryId IN (" + repositoryIdBindingMarkers + ") ORDER BY da.digitalAssetId desc LIMIT " + maxRows + " AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl";
			}
			else if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
				assetSQL = "CALL SQL SELECT * from (select distinct(da.DigAssetId),da.assetFileName,da.assetKey,da.assetFilePath,da.assetContentType,da.assetFileSize FROM cmDigAsset da, cmContVerDigAsset cvda, cmContVer cv, cmCont c WHERE cvda.DigAssetId = da.DigAssetId AND cvda.contVerId = cv.contVerId AND cv.contId = c.contId  " + assetFilterTerm + " AND c.repositoryId IN (" + repositoryIdBindingMarkers + ") ORDER BY da.DigAssetId desc) where ROWNUM < " + maxRows + " AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl";
			
			logger.info("assetSQL:" + assetSQL);
			
			OQLQuery assetOQL = db.getOQLQuery(assetSQL);
			
			if(assetTypeFilterArray != null)
			{
				for(int i=0; i<assetTypeFilterArray.length; i++)
				{
					assetOQL.bind(assetTypeFilterArray[i]);
				}
			}
			
			for(int i=0; i<repositoryId.length; i++)
			{
				assetOQL.bind(repositoryId[i]);
			}
			
			QueryResults assetResults = assetOQL.execute(Database.ReadOnly);

			int currentCount = 0;
			while(assetResults.hasMore() && currentCount < maxRows)
			{
				SmallDigitalAssetImpl smallAsset = (SmallDigitalAssetImpl)assetResults.next();
				if(logger.isInfoEnabled())
					logger.info("asset found:" + smallAsset.getDigitalAssetId() + ":" + smallAsset.getAssetKey() + ":" + smallAsset.getAssetContentType());
				
				matchingAssets.add(smallAsset.getValueObject());
			    currentCount++;
			}

			assetResults.close();
			assetOQL.close();
			
		    DigitalAssetController.getController().appendContentId(matchingAssets, db);
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingAssets;
   	}

   	
   	public static int replaceString(String searchString, String replaceString, Boolean caseSensitive, String[] contentVersionIds, InfoGluePrincipal infoGluePrincipal)throws SystemException, Bug
   	{
		int replacements = 0;
		
   	    ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);

			for(int i=0; i<contentVersionIds.length; i++)
			{
			    String contentVersionId = contentVersionIds[i];
			    logger.info("contentVersionId:" + contentVersionId);
			    ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(contentVersionIds[i]), db);
			    if(contentVersion.getStateId().intValue() != ContentVersionVO.WORKING_STATE.intValue())
			    {
		            List events = new ArrayList();
			       	ContentVersionVO contentVersionVO = ContentStateController.changeState(contentVersion.getId(), ContentVersionVO.WORKING_STATE, "Automatic by the replace function", true, infoGluePrincipal, contentVersion.getValueObject().getContentId(), db, events);
			        contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getId(), db);
				    logger.info("Setting the version to working before replacing string...");
			    }
			    
			    String value = contentVersion.getVersionValue();
			    
			    logger.info("searchString:" + searchString);
			    
			    if(!caseSensitive)			    
			    	searchString = "(?i)" + searchString;
			    logger.info("searchString:" + searchString);
			    
			    value = value.replaceAll(searchString, replaceString);
			    	
			    contentVersion.setVersionValue(value);

			    replacements++;
			}
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		return replacements;
	}
   	
   	
   	/**
   	 * This method searches with lucene
   	 */
   	public List<ContentVersionVO> getContentVersionVOListFromLucene(Integer[] repositoryIdAsIntegerToSearch, String searchString, Integer maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionIds, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean includeAssets, boolean includeSiteNodes, String categoriesExpression, Map<String,Integer> searchMetaData) throws SystemException, Bug
   	{
   		Timer t = new Timer();
   		if(!logger.isInfoEnabled())
   			t.setActive(false);
   		   		
   		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
   		
	   	try 
	    {
			if(searchString != null)
			{
				/*if(searchString.indexOf(" ") > -1)
					searchString = "\"" + searchString + "\"";
				else*/ if(!searchString.endsWith("*"))
					searchString = searchString + "*";
			}
			
			List<String> fieldNames = new ArrayList<String>();
			List<String> queryStrings = new ArrayList<String>();
			List<BooleanClause.Occur> booleanList = new ArrayList<BooleanClause.Occur>();
			
			if(repositoryIdAsIntegerToSearch != null && !repositoryIdAsIntegerToSearch.equals("null") && repositoryIdAsIntegerToSearch.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < repositoryIdAsIntegerToSearch.length; i++)
				{
					if(i > 0)
						sb.append(" OR ");
					sb.append("" + repositoryIdAsIntegerToSearch[i]);
				}
				
				if(sb.length() > 0)
				{
					fieldNames.add("repositoryId");
					queryStrings.add("" + sb.toString());
					booleanList.add(BooleanClause.Occur.MUST);
				}
			}
			if(languageId != null && languageId.intValue() > 0)
			{
				fieldNames.add("languageId");
				queryStrings.add("" + languageId);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < contentTypeDefinitionIds.length; i++)
				{
					Integer contentTypeDefinitionId = contentTypeDefinitionIds[i];
					if(contentTypeDefinitionId != null)
					{
						if(i > 0)
							sb.append(" OR ");
						sb.append("" +contentTypeDefinitionId);
					}
				}
				
				if(sb.length() > 0)
				{
					fieldNames.add("contentTypeDefinitionId");
					queryStrings.add("" + sb.toString());
					booleanList.add(BooleanClause.Occur.MUST);
				}
			}
			if(excludedContentTypeDefinitionIds != null && excludedContentTypeDefinitionIds.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < excludedContentTypeDefinitionIds.length; i++)
				{
					Integer contentTypeDefinitionId = excludedContentTypeDefinitionIds[i];
					if(contentTypeDefinitionId != null)
					{
						if(i > 0)
							sb.append(" OR ");
						sb.append("" +contentTypeDefinitionId);
					}
				}
				
				if(sb.length() > 0)
				{
					fieldNames.add("contentTypeDefinitionId");
					queryStrings.add("" + sb.toString());
					booleanList.add(BooleanClause.Occur.MUST_NOT);
				}
			}
			if(userName != null && !userName.equals(""))
			{
				fieldNames.add("lastModifier");
				queryStrings.add("" + userName);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			if(stateId != null && !stateId.equals(""))
			{
				if(stateId == 0)
				{
					fieldNames.add("stateId");
					queryStrings.add("0 OR 1 OR 2 OR 3");
					booleanList.add(BooleanClause.Occur.MUST);					
				}
				else if(stateId == 2)
				{
					fieldNames.add("stateId");
					queryStrings.add("2 OR 3");
					booleanList.add(BooleanClause.Occur.MUST);					
				}
				else
				{
					fieldNames.add("stateId");
					queryStrings.add("" + stateId);
					booleanList.add(BooleanClause.Occur.MUST);
				}
			}
			
			if(!includeAssets)
			{
				fieldNames.add("isAsset");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST_NOT);
			}
			
			if(categoriesExpression != null && !categoriesExpression.equals(""))
			{
				fieldNames.add("categories");
				queryStrings.add("" + categoriesExpression);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			
			
			if(searchString != null && searchString.length() > 0)
			{
				fieldNames.add("contents");
				queryStrings.add(searchString);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			
			if(!includeSiteNodes)
			{
				if(logger.isInfoEnabled())
					logger.info("Detta var inte metaInfoFråga");
				fieldNames.add("isSiteNode");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST_NOT);
			}
			else
			{
				if(logger.isInfoEnabled())
					logger.info("Detta var inte metaInfoFråga");
				fieldNames.add("isSiteNode");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST);
			}

			if(logger.isInfoEnabled())
			{
				for(String queryString : queryStrings)
				{
					logger.info("queryString:" + queryString);
				}
			}
			
			String[] fields = new String[fieldNames.size()];
			fields = (String[])fieldNames.toArray(fields);
			
			String[] queries = new String[fieldNames.size()];
			queries = (String[])queryStrings.toArray(queries);
			
			BooleanClause.Occur[] flags = new BooleanClause.Occur[fieldNames.size()];
			flags = (BooleanClause.Occur[])booleanList.toArray(flags);
			
			SortField sortField = new SortField("publishDateTime", SortField.LONG, true);
			Sort sort = new Sort(sortField);
			
			/////this.docs = LuceneController.getController().queryDocuments(searchField, searchString, maxHits);
			List<org.apache.lucene.document.Document> documents = LuceneController.getController().queryDocuments(fields, flags, queries, sort, maxRows, searchMetaData);
			t.printElapsedTime("Search took...");

			logger.info(documents.size() + " total matching documents");
	
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				beginTransaction(db);
				
				for(org.apache.lucene.document.Document doc : documents)
				{
					String contentVersionId = doc.get("contentVersionId");
					String contentId = doc.get("contentId");
					String siteNodeId = doc.get("siteNodeId");
					if(logger.isInfoEnabled())
					{
						logger.info("doc:" + doc);
						logger.info("contentVersionId:" + contentVersionId);
						logger.info("contentId:" + contentId);
						logger.info("siteNodeId:" + siteNodeId);
					}
					
					if(siteNodeId != null)
					{
						try
						{
							SiteNodeVO snVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeId), db);
							t.printElapsedTime("snVO");
							if(snVO.getMetaInfoContentId() != null)
							{
								if(languageId == null)
									languageId = LanguageController.getController().getMasterLanguage(snVO.getRepositoryId(), db).getId();
								ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(snVO.getMetaInfoContentId(), languageId, Integer.parseInt(CmsPropertyHandler.getOperatingMode()), db);
								logger.info("cvvo:" + cvVO.getContentName() + "(" + cvVO.getContentId() + ")");
								contentVersionVOList.add(cvVO);
								t.printElapsedTime("cvVO");
							}
						}
						catch (Exception e) 
						{
							logger.error("SiteNode with id:" + siteNodeId + " was not valid.");
						}						
					}
					else if(contentVersionId == null && contentId != null)
					{
						try
						{
							ContentVO cvo = ContentController.getContentController().getContentVOWithId(new Integer(contentId), db);
							t.printElapsedTime("cvVO");
							logger.info("cvo:" + cvo);

							String path = doc.get("path");
							if (path != null)
							{
								logger.info("" + path);
								String title = doc.get("title");
								if (title != null)
								{
									logger.info("   Title: " + doc.get("title"));
								}
							} 
							else
							{
								logger.info("No path for this document");
							}
						}
						catch (Exception e) 
						{
							logger.error("ContentVersion with id:" + contentVersionId + " was not valid - skipping but how did the index become corrupt?");
							LuceneController.getController().deleteVersionFromIndex(contentVersionId);
						}						
					}
					else
					{
						try
						{
							//ContentVersionVO cvvo = ContentVersionController.getContentVersionController().getFullContentVersionVOWithId(new Integer(contentVersionId), db);
							//ContentVersionVO cvvo = ContentVersionController.getContentVersionController().getContentVersionVOWithId(new Integer(contentVersionId), db);
							//t.printElapsedTime("Start fetching cvVO from lucene index");
							//logger.info("cvvo:" + cvvo.getContentName() + "(" + cvvo.getContentId() + ")");
							//logger.info("doc:" + doc);
							
							ContentVersionVO cvvo = new ContentVersionVO();
							cvvo.setContentId(new Integer(contentId));
							cvvo.setContentVersionId(new Integer(contentVersionId));
							cvvo.setContentName(doc.get("path"));
							cvvo.setLanguageId(new Integer(doc.get("languageId")));
							cvvo.setModifiedDateTime(new Date(new Long(doc.get("modificationDateTime"))));
							cvvo.setStateId(new Integer(doc.get("stateId")));
							cvvo.setContentTypeDefinitionId(new Integer(doc.get("contentTypeDefinitionId")));
							
							contentVersionVOList.add(cvvo);
							/*
							String path = doc.get("path");
							if (path != null)
							{
								logger.info("" + path);
								String title = doc.get("title");
								if (title != null)
								{
									logger.info("   Title: " + doc.get("title"));
								}
							} 
							else
							{
								logger.info("No path for this document");
							}
							*/
						}
						catch (Exception e) 
						{
							e.printStackTrace();
							logger.error("ContentVersion with id:" + contentVersionId + " was not valid - skipping but how did the index become corrupt?");
							//deleteVersionFromIndex(contentVersionId);
						}
					}
				}
				
				commitTransaction(db);
			} 
			catch (Exception e) 
			{
				logger.info("An error occurred so we should not complete the transaction:" + e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());
			}
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error searching:" + e.getMessage(), e);
	    }
	    
	    return contentVersionVOList;
	}
   	
   	/**
   	 * This method searches with lucene
   	 */
   	public List<SiteNodeVersionVO> getSiteNodeVersionVOListFromLucene(Integer[] repositoryIdAsIntegerToSearch, String searchString, Integer maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionIds, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean includeAssets, boolean includeSiteNodes, String categoriesExpression, Map<String,Integer> searchMetaData) throws SystemException, Bug
   	{
   		Timer t = new Timer();
   		if(!logger.isInfoEnabled())
   			t.setActive(false);
   		
   		List<SiteNodeVersionVO> siteNodeVersionVOList = new ArrayList<SiteNodeVersionVO>();
   		
	   	try 
	    {
			if(searchString != null)
			{
				/*if(searchString.indexOf(" ") > -1)
					searchString = "\"" + searchString + "\"";
				else*/ if(!searchString.endsWith("*"))
					searchString = searchString + "*";
			}
			
			List<String> fieldNames = new ArrayList<String>();
			List<String> queryStrings = new ArrayList<String>();
			List<BooleanClause.Occur> booleanList = new ArrayList<BooleanClause.Occur>();
			
			if(repositoryIdAsIntegerToSearch != null && !repositoryIdAsIntegerToSearch.equals("null") && repositoryIdAsIntegerToSearch.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < repositoryIdAsIntegerToSearch.length; i++)
				{
					if(i > 0)
						sb.append(" OR ");
					sb.append("" + repositoryIdAsIntegerToSearch[i]);
				}
				
				if(sb.length() > 0)
				{
					fieldNames.add("repositoryId");
					queryStrings.add("" + sb.toString());
					booleanList.add(BooleanClause.Occur.MUST);
				}
			}
			if(languageId != null && languageId.intValue() > 0)
			{
				fieldNames.add("languageId");
				queryStrings.add("" + languageId);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			if(contentTypeDefinitionIds != null && contentTypeDefinitionIds.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < contentTypeDefinitionIds.length; i++)
				{
					Integer contentTypeDefinitionId = contentTypeDefinitionIds[i];
					if(contentTypeDefinitionId != null)
					{
						if(i > 0)
							sb.append(" OR ");
						sb.append("" +contentTypeDefinitionId);
					}
				}
				
				if(sb.length() > 0)
				{
					fieldNames.add("contentTypeDefinitionId");
					queryStrings.add("" + sb.toString());
					booleanList.add(BooleanClause.Occur.MUST);
				}
			}
			if(excludedContentTypeDefinitionIds != null && excludedContentTypeDefinitionIds.length > 0)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < excludedContentTypeDefinitionIds.length; i++)
				{
					Integer contentTypeDefinitionId = excludedContentTypeDefinitionIds[i];
					if(contentTypeDefinitionId != null)
					{
						if(i > 0)
							sb.append(" OR ");
						sb.append("" +contentTypeDefinitionId);
					}
				}
				
				if(sb.length() > 0)
				{
					fieldNames.add("contentTypeDefinitionId");
					queryStrings.add("" + sb.toString());
					booleanList.add(BooleanClause.Occur.MUST_NOT);
				}
			}
			if(userName != null && !userName.equals(""))
			{
				fieldNames.add("lastModifier");
				queryStrings.add("" + userName);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			if(stateId != null && !stateId.equals(""))
			{
				if(stateId == 0)
				{
					fieldNames.add("stateId");
					queryStrings.add("0 OR 1 OR 2 OR 3");
					booleanList.add(BooleanClause.Occur.MUST);					
				}
				else if(stateId == 2)
				{
					fieldNames.add("stateId");
					queryStrings.add("2 OR 3");
					booleanList.add(BooleanClause.Occur.MUST);					
				}
				else
				{
					fieldNames.add("stateId");
					queryStrings.add("" + stateId);
					booleanList.add(BooleanClause.Occur.MUST);
				}
			}
			
			if(!includeAssets)
			{
				fieldNames.add("isAsset");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST_NOT);
			}
			
			if(categoriesExpression != null && !categoriesExpression.equals(""))
			{
				fieldNames.add("categories");
				queryStrings.add("" + categoriesExpression);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			
			
			if(searchString != null && searchString.length() > 0)
			{
				fieldNames.add("contents");
				queryStrings.add(searchString);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			
			if(!includeSiteNodes)
			{
				if(logger.isInfoEnabled())
					logger.info("Detta var inte metaInfoFråga");
				fieldNames.add("isSiteNode");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST_NOT);
			}
			else
			{
				if(logger.isInfoEnabled())
					logger.info("Detta var inte metaInfoFråga");
				fieldNames.add("isSiteNode");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST);
			}

			if(logger.isInfoEnabled())
			{
				for(String queryString : queryStrings)
				{
					logger.info("queryString:" + queryString);
				}
			}
			
			String[] fields = new String[fieldNames.size()];
			fields = (String[])fieldNames.toArray(fields);
			
			String[] queries = new String[fieldNames.size()];
			queries = (String[])queryStrings.toArray(queries);
			
			BooleanClause.Occur[] flags = new BooleanClause.Occur[fieldNames.size()];
			flags = (BooleanClause.Occur[])booleanList.toArray(flags);
			
			SortField sortField = new SortField("publishDateTime", SortField.LONG, true);
			Sort sort = new Sort(sortField);
			
			List<org.apache.lucene.document.Document> documents = LuceneController.getController().queryDocuments(fields, flags, queries, sort, maxRows, searchMetaData);
			t.printElapsedTime("Search took...");

			logger.info(documents.size() + " total matching documents");
	
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				beginTransaction(db);
				
				t.printElapsedTime("After begin trans.." + documents.size());
				
				for(org.apache.lucene.document.Document doc : documents)
				{
					if(logger.isInfoEnabled())
						logger.info("doc:" + doc);
					String contentVersionId = doc.get("contentVersionId");
					String contentId = doc.get("contentId");
					String siteNodeId = doc.get("siteNodeId");
					String siteNodeVersionId = doc.get("siteNodeVersionId");
					
					try
					{
						SiteNodeVersionVO snvo = new SiteNodeVersionVO();
						snvo.setSiteNodeId(new Integer(siteNodeId));
						if(siteNodeVersionId == null && siteNodeId != null)
						{
							SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, new Integer(siteNodeId));
							snvo.setSiteNodeVersionId(siteNodeVersionVO.getId());
						}
						else
							snvo.setSiteNodeVersionId(new Integer(siteNodeVersionId));
						
						snvo.setSiteNodeName(doc.get("path"));
						if(doc.get("modificationDateTime") != null)
							snvo.setModifiedDateTime(new Date(new Long(doc.get("modificationDateTime"))));
						else
							snvo.setModifiedDateTime(new Date(new Long(doc.get("modified"))));
						if(doc.get("stateId") != null)
							snvo.setStateId(new Integer(doc.get("stateId")));

						siteNodeVersionVOList.add(snvo);
					}
					catch (Exception e) 
					{
						logger.error("ContentVersion with id:" + contentVersionId + " was not valid - skipping but how did the index become corrupt?");
						//deleteVersionFromIndex(contentVersionId);
					}
				}
				t.printElapsedTime("After commit trans.." + documents.size());

				
				commitTransaction(db);
			} 
			catch (Exception e) 
			{
				logger.info("An error occurred so we should not complete the transaction:" + e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());
			}
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error searching:" + e.getMessage(), e);
	    }
	    
	    return siteNodeVersionVOList;
	}
   	

   	public List<SiteNodeVersionVO> getSiteNodeVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
   		//String internalSearchEngine = CmsPropertyHandler.getInternalSearchEngine();
   		//if(internalSearchEngine.equalsIgnoreCase("sqlSearch"))
   			return getSiteNodeVersionVOListFromCastor(repositoryId, searchString, maxRows, userName, languageId, caseSensitive, stateId);
   			//else
   			//	return getSiteNodeVersionVOListFromLucene(repositoryId, searchString, maxRows, userName, caseSensitive, stateId);
   	}

   	private List<SiteNodeVersionVO> getSiteNodeVersionVOListFromCastor(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
   		List<SiteNodeVersionVO> matching = new ArrayList<SiteNodeVersionVO>();

		List<SiteNodeVersionVO> matchingSiteNodeVersionVOList = getSiteNodeVersionVOList(repositoryId, searchString, maxRows, userName, languageId, null, null, caseSensitive, stateId, false, true, new HashMap<String,Integer>());
		matching.addAll(matchingSiteNodeVersionVOList);
		/*
		Iterator<ContentVersionVO> matchingMetaInfoContentVersionVOListIterator = matchingMetaInfoContentVersionVOList.iterator();
		while(matchingMetaInfoContentVersionVOListIterator.hasNext())
		{
			ContentVersionVO contentVersionVO = matchingMetaInfoContentVersionVOListIterator.next();
			try
			{
				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(contentVersionVO.getContentId());				
				SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeVO.getId());
		   		matching.add(siteNodeVersionVO);
			}
			catch (Exception e) 
			{
				logger.warn("An error - why:" + e.getMessage(), e);
			}
		}
		*/
		
		return matching;		
   	}
   	
   	
	public static org.apache.lucene.document.Document getDocument(String text) throws IOException, InterruptedException
	{
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

		doc.add(new Field("modified", DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contents", new StringReader(text)));

		return doc;
	}

   	public String getDigitalAssetUrl(DigitalAssetVO digitalAssetVO, Database db) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getController().getDigitalAssetUrl(digitalAssetVO, db);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
   	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer digitalAssetId, int canvasWidth, int canvasHeight, String canvasColorHexCode, String alignment, String valignment, int width, int height, int quality, Database db) throws Exception
	{
		String imageHref = null;
		try
		{
			ColorHelper ch = new ColorHelper();
			Color canvasColor = ch.getHexColor(canvasColorHexCode);
       		imageHref = DigitalAssetController.getController().getDigitalAssetThumbnailUrl(digitalAssetId, canvasWidth, canvasHeight, canvasColor, alignment, valignment, width, height, quality, db);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	/**
	 * This is a method that never should be called.
	 */

	public BaseEntityVO getNewVO()
	{
		return null;
	}
   	
}
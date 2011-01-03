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
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.Timer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SearchController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(SearchController.class.getName());

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
   		List contentVersions = getContentVersionVOList(repositoryId, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, searchAssets);
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
		List<ContentVersionVO> matchingContents = getContentVersionVOList(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId);
			
		return matchingContents;		
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
		List<ContentVersionVO> matchingContents = getContentVersionVOList(new Integer[]{repositoryId}, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, searchAssets);
			
		return matchingContents;		
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String name, Integer languageId, Integer[] contentTypeDefinitionId, Integer caseSensitive, Integer stateId) throws SystemException, Bug
   	{
   		return getContentVersionVOList(repositoryId, searchString, maxRows, name, languageId, contentTypeDefinitionId, null, caseSensitive, stateId, false);
   	}

   	public List<ContentVersionVO> getContentVersionVOList(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionId, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
   		String internalSearchEngine = CmsPropertyHandler.getInternalSearchEngine();
   		if(internalSearchEngine.equalsIgnoreCase("sqlSearch"))
   			return getContentVersionVOListFromCastor(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets);
   		else
   			return getContentVersionVOListFromLucene(repositoryId, searchString, maxRows, userName, languageId, contentTypeDefinitionId, excludedContentTypeDefinitionIds, caseSensitive, stateId, searchAssets);
   	}
   	
   	private List<ContentVersionVO> getContentVersionVOListFromCastor(Integer[] repositoryId, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionId, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean searchAssets) throws SystemException, Bug
   	{
		List<ContentVersionVO> matchingContents = new ArrayList<ContentVersionVO>();

		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);

			String repositoryArgument = " AND (";
			
			int index = 3;
			List repArguments = new ArrayList();
			
			for(int i=0; i<repositoryId.length; i++)
			{
				if(i > 0)
					repositoryArgument += " OR ";
				
				repositoryArgument += "cv.owningContent.repository.repositoryId = $" + index;
			    repArguments.add(repositoryId[i]);
				index++;
			}
			repositoryArgument += ")";
				
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
			catch (Exception e) { System.out.println("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { System.out.println("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = DigitalAssetController.getController().getDigitalAssetVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { System.out.println("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = SiteNodeController.getController().getSiteNodeVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { System.out.println("No entity found.."); }

			try
			{
				BaseEntityVO entityVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(entityId, db);
				matchingEntities.add(entityVO);
			}
			catch (Exception e) { System.out.println("No entity found.."); }
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingEntities;
		
   	}
   	
   	public static List<DigitalAssetVO> getDigitalAssets(Integer[] repositoryId, String searchString, String assetTypeFilter, int maxRows) throws SystemException, Bug
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
			
			String assetSQL = "CALL SQL SELECT da.digitalAssetId,da.assetFileName,da.assetKey,da.assetFilePath,da.assetContentType,da.assetFileSize FROM cmDigitalAsset da, cmContentVersionDigitalAsset cvda, cmContentVersion cv, cmContent c WHERE cvda.digitalAssetId = da.digitalAssetId AND cvda.contentVersionId = cv.contentVersionId AND cv.contentId = c.contentId " + assetFilterTerm + " AND c.repositoryId IN (" + repositoryIdBindingMarkers + ") ORDER BY da.digitalAssetId desc AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl";
			if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
				assetSQL = "CALL SQL SELECT da.DigAssetId,da.assetFileName,da.assetKey,da.assetFilePath,da.assetContentType,da.assetFileSize FROM cmDigAsset da, cmContVerDigAsset cvda, cmContVer cv, cmCont c WHERE cvda.DigAssetId = da.DigAssetId AND cvda.contVerId = cv.contVerId AND cv.contId = c.contId  " + assetFilterTerm + " AND c.repositoryId IN (" + repositoryIdBindingMarkers + ") ORDER BY da.DigAssetId desc AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl";
			
			logger.info("assetSQL:" + assetSQL);
			
			OQLQuery assetOQL = db.getOQLQuery(assetSQL);
			
			if(assetTypeFilterArray != null)
			{
				for(int i=0; i<assetTypeFilterArray.length; i++)
				{
					//System.out.println("Binding assetType:" + assetTypeFilterArray[i]);
					assetOQL.bind(assetTypeFilterArray[i]);
				}
			}
			
			for(int i=0; i<repositoryId.length; i++)
			{
				//System.out.println("Binding repId:" + repositoryId[i]);
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
			
			commitTransaction(db);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to search. Reason:" + e.getMessage(), e);			
		}
		
		return matchingAssets;
   	}

   	
   	public static int replaceString(String searchString, String replaceString, String[] contentVersionIds, InfoGluePrincipal infoGluePrincipal)throws SystemException, Bug
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
			    //System.out.println("contentVersionId:" + contentVersionId);
			    ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(contentVersionIds[i]), db);
			    if(contentVersion.getStateId().intValue() != ContentVersionVO.WORKING_STATE.intValue())
			    {
		            List events = new ArrayList();
			        contentVersion = ContentStateController.changeState(contentVersion.getId(), ContentVersionVO.WORKING_STATE, "Automatic by the replace function", true, null, infoGluePrincipal, null, db, events);
			        logger.info("Setting the version to working before replacing string...");
			    }
			    
			    String value = contentVersion.getVersionValue();
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
   	private List<ContentVersionVO> getContentVersionVOListFromLucene(Integer[] repositoryIdAsIntegerToSearch, String searchString, int maxRows, String userName, Integer languageId, Integer[] contentTypeDefinitionIds, Integer[] excludedContentTypeDefinitionIds, Integer caseSensitive, Integer stateId, boolean includeAssets) throws SystemException, Bug
   	{
   		List<ContentVersionVO> contentVersionVOList = new ArrayList<ContentVersionVO>();
   		
	   	try 
	    {
			String index = CmsPropertyHandler.getContextRootPath() + File.separator + "lucene" + File.separator + "index";
		    
			boolean indexExists = IndexReader.indexExists(new File(index));
			if(!indexExists)
			{
			    try 
			    {
			    	File INDEX_DIR = new File(index);
			    	IndexWriter writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer());
			    	logger.info("Indexing to directory '" + INDEX_DIR + "'...");
			    	writer.updateDocument(new Term("initializer", "true"), getDocument("initializer"));
			    	logger.info("Optimizing...");
			    	writer.optimize();
			    	writer.close();
			    } 
			    catch (Exception e) 
			    {
			    	logger.error("An error creating index:" + e.getMessage(), e);
			    }
			}

			/*if(searchString.indexOf(" ") > -1)
				searchString = "\"" + searchString + "\"";
			else*/ if(!searchString.endsWith("*"))
				searchString = searchString + "*";

			List<String> fieldNames = new ArrayList<String>();
			List<String> queryStrings = new ArrayList<String>();
			List<BooleanClause.Occur> booleanList = new ArrayList<BooleanClause.Occur>();
			
			if(repositoryIdAsIntegerToSearch != null && repositoryIdAsIntegerToSearch.length > 0)
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
				fieldNames.add("stateId");
				queryStrings.add("" + stateId);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			if(!includeAssets)
			{
				fieldNames.add("isAsset");
				queryStrings.add("true");
				booleanList.add(BooleanClause.Occur.MUST_NOT);
			}
			
			if(searchString != null && searchString.length() > 0)
			{
				fieldNames.add("contents");
				queryStrings.add(searchString);
				booleanList.add(BooleanClause.Occur.MUST);
			}
			
			String[] fields = new String[fieldNames.size()];
			fields = (String[])fieldNames.toArray(fields);
			
			String[] queries = new String[fieldNames.size()];
			queries = (String[])queryStrings.toArray(queries);
			
			BooleanClause.Occur[] flags = new BooleanClause.Occur[fieldNames.size()];
			flags = (BooleanClause.Occur[])booleanList.toArray(flags);
			
		    IndexReader reader = IndexReader.open(index);
	
		    Searcher searcher = new IndexSearcher(reader);
		    Analyzer analyzer = new StandardAnalyzer(new String[]{});

			Query query = MultiFieldQueryParser.parse(queries, fields, flags, analyzer);
			logger.info("Searching for: " + query.toString());
			//System.out.println("Searching for: " + query.toString());
	
			Hits hits = searcher.search(query);
	
			logger.info(hits.length() + " total matching documents");
	
			final int HITS_PER_PAGE = new Integer(maxRows);
			for (int start = 0; start < hits.length(); start += HITS_PER_PAGE)
			{
				int end = Math.min(hits.length(), start + HITS_PER_PAGE);
				for (int i = start; i < end; i++)
				{
					org.apache.lucene.document.Document doc = hits.doc(i);
					String contentVersionId = doc.get("contentVersionId");
					String contentId = doc.get("contentId");
					logger.info("doc:" + doc);
					logger.info("contentVersionId:" + contentVersionId);
					logger.info("contentId:" + contentId);
					
					if(contentVersionId == null && contentId != null)
					{
						try
						{
							ContentVO cvo = ContentController.getContentController().getContentVOWithId(new Integer(contentId));
							logger.info("cvo:" + cvo);

							String path = doc.get("path");
							if (path != null)
							{
								logger.info((i + 1) + ". " + path);
								String title = doc.get("title");
								if (title != null)
								{
									logger.info("   Title: " + doc.get("title"));
								}
							} 
							else
							{
								logger.info((i + 1) + ". " + "No path for this document");
							}
						}
						catch (Exception e) 
						{
							logger.error("ContentVersion with id:" + contentVersionId + " was not valid - skipping but how did the index become corrupt?");
							deleteVersionFromIndex(contentVersionId);
						}						
					}
					else
					{
						try
						{
							ContentVersionVO cvvo = ContentVersionController.getContentVersionController().getFullContentVersionVOWithId(new Integer(contentVersionId));
							logger.info("cvvo:" + cvvo);
							contentVersionVOList.add(cvvo);
						
							String path = doc.get("path");
							if (path != null)
							{
								logger.info((i + 1) + ". " + path);
								String title = doc.get("title");
								if (title != null)
								{
									logger.info("   Title: " + doc.get("title"));
								}
							} 
							else
							{
								logger.info((i + 1) + ". " + "No path for this document");
							}
						}
						catch (Exception e) 
						{
							logger.error("ContentVersion with id:" + contentVersionId + " was not valid - skipping but how did the index become corrupt?");
							deleteVersionFromIndex(contentVersionId);
						}
					}
				}
	
				if (queries != null) // non-interactive
					break;
			}
			
			reader.close();	
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error searching:" + e.getMessage(), e);
	    }
	    
	    return contentVersionVOList;
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

   		ContentTypeDefinitionVO ctd = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info");

		List<ContentVersionVO> matchingMetaInfoContentVersionVOList = getContentVersionVOList(repositoryId, searchString, maxRows, userName, languageId, new Integer[]{ctd.getId()}, null, caseSensitive, stateId, false);
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
		
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);

			String repositoryArgument = " AND (";
			
			int index = 3;
			List repArguments = new ArrayList();
			
			for(int i=0; i<repositoryId.length; i++)
			{
				if(i > 0)
					repositoryArgument += " OR ";
				
				repositoryArgument += "snv.owningSiteNode.repository.repositoryId = $" + index;
			    repArguments.add(repositoryId[i]);
				index++;
			}
			repositoryArgument += ")";
				
			//int index = 4;
			String extraArguments = "";
			String inverse = "";
			List arguments = new ArrayList();
						
			if(userName != null && !userName.equalsIgnoreCase(""))
			{
			    extraArguments += " AND snv.versionModifier = $" + index;
			    arguments.add(userName);
				index++;
			}
			if(stateId != null)
			{
			    extraArguments += " AND cv.stateId = $" + index;
			    arguments.add(stateId);
				index++;
			}
			    
			String sql = "SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl snv WHERE snv.isActive = $1 AND snv.owningSiteNode.name = $2 " + repositoryArgument + extraArguments + " ORDER BY snv.owningSiteNode asc, snv.siteNodeVersionId desc";
			logger.info("sql:" + sql);
			OQLQuery oql = db.getOQLQuery(sql);
			oql.bind(new Boolean(true));
			oql.bind(searchString);
			Iterator repIterator = repArguments.iterator();
			while(repIterator.hasNext())
			{
				Integer repositoryIdAsInteger = (Integer)repIterator.next();
				oql.bind(repositoryIdAsInteger);
			}
	        
			Iterator iterator = arguments.iterator();
			while(iterator.hasNext())
			{
			    oql.bind(iterator.next());
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			Integer previousSiteNodeId  = new Integer(-1);
			int currentCount = 0;
			while(results.hasMore() && currentCount < maxRows) 
			{
				SiteNodeVersion siteNodeVersionVersion = (SiteNodeVersion)results.next();
				logger.info("Found a version matching " + searchString + ":" + siteNodeVersionVersion.getId() + "=" + siteNodeVersionVersion.getOwningSiteNode().getName());
				if(siteNodeVersionVersion.getOwningSiteNode().getId().intValue() != previousSiteNodeId.intValue())
				{
				    matching.add(siteNodeVersionVersion.getValueObject());
				    previousSiteNodeId = siteNodeVersionVersion.getOwningSiteNode().getId();
				    currentCount++;
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
		
		return matching;
		
   	}
   	
   	
	private void deleteVersionFromIndex(String contentVersionId)
	{
		IndexWriter writer = null;
	    try 
	    {
			String index = CmsPropertyHandler.getContextRootPath() + File.separator + "lucene" + File.separator + "index";

	    	File INDEX_DIR = new File(index);
	    	writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer());
	    	logger.info("Indexing to directory '" + INDEX_DIR + "'...");
	    	logger.info("Deleting contentVersionId:" + contentVersionId);
		    writer.deleteDocuments(new Term("contentVersionId", "" + contentVersionId));
		    logger.info("Optimizing...");
	    	writer.optimize();
	    	writer.close();	    	
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error deleting index:" + e.getMessage(), e);
	    }
	}

	public static org.apache.lucene.document.Document getDocument(String text) throws IOException, InterruptedException
	{
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

		doc.add(new Field("modified", DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contents", new StringReader(text)));

		return doc;
	}

	/**
	 * This is a method that never should be called.
	 */

	public BaseEntityVO getNewVO()
	{
		return null;
	}
   	
}
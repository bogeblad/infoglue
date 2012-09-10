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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.CategoryAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationListener;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

public class LuceneController extends BaseController implements NotificationListener
{
    private final static Logger logger = Logger.getLogger(LuceneController.class.getName());
    private static int indexedDocumentsSinceLastOptimize = 0;
    private Integer lastCommitedContentVersionId = -1;
    
    private static AtomicBoolean indexingInitialized = new AtomicBoolean(false);
    
	/**
	 * Default Constructor	
	 */
	
	public static LuceneController getController()
	{
		return new LuceneController();
	}
	
	private static List<NotificationMessage> qeuedMessages = new ArrayList<NotificationMessage>();
	
	private StandardAnalyzer getStandardAnalyzer() throws Exception
	{
		return new StandardAnalyzer(Version.LUCENE_34);
	}
	
	private Directory getDirectory() throws Exception
	{
		String index = CmsPropertyHandler.getContextRootPath() + File.separator + "lucene" + File.separator + "index";
    	File INDEX_DIR = new File(index);
		Directory directory = new NIOFSDirectory(INDEX_DIR);
		boolean indexExists = IndexReader.indexExists(directory);
		if(!indexExists)
		{
			createIndex(directory);
		}
		
		return directory;
	}

	private void createIndex(Directory directory) throws Exception
	{
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, getStandardAnalyzer());
		IndexWriter indexWriter = new IndexWriter(directory, config);
		indexWriter.deleteDocuments(new Term("initializer", "true"));
		indexWriter.close();
	}
	
	private IndexWriter getIndexWriter() throws Exception
	{
		Directory directory = getDirectory();
		
    	StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, analyzer);

		if(IndexWriter.isLocked(directory))
		{
			logger.error("Directory is locked - leaving the messages in the qeuedMessages list...");
			throw new Exception("Lock not granted");
		}
		else
		{
			return new IndexWriter(directory, config);
		}
	}

	private IndexReader getIndexReader() throws Exception
	{
		return IndexReader.open(getDirectory(), true);
	}

	private IndexSearcher getIndexSearcher() throws Exception
	{
		IndexReader reader = IndexReader.open(getDirectory(), true);
	    return new IndexSearcher(reader);
	}
	
	private Boolean getIsIndexedLocked() throws Exception
	{
		Directory directory = getDirectory();
		return IndexWriter.isLocked(directory);
	}

	private void unlockIndex() throws Exception
	{
		Directory directory = getDirectory();
		IndexWriter.unlock(directory);
	}

	public Map<String,Object> getIndexInformation() throws Exception
	{
		Map<String,Object> info = new HashMap<String,Object>();
		
		try 
	    {
			Directory directory = getDirectory();
			
		    IndexReader reader = IndexReader.open(directory);
		    int maxDoc = reader.maxDoc();
		    int numDoc = reader.numDocs();
		    long lastModified = IndexReader.lastModified(directory);

		    info.put("maxDoc", new Integer(maxDoc));
		    info.put("numDoc", new Integer(numDoc));
		    info.put("lastModified", new Date(lastModified));
		    info.put("lastCommitedContentVersionId", getLastCommitedContentVersionId());

			reader.close();	
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error creating index:" + e.getMessage(), e);
	    	throw e;
	    }
	    
	    return info;
	}

	
	public void clearIndex() throws Exception
	{
		if (indexingInitialized.compareAndSet(false, true)) 
		{
			try
			{
				logger.info("NumDocs:" + getIndexReader().numDocs());
				IndexWriter writer = getIndexWriter();
				writer.deleteAll();
				writer.close();
				logger.info("NumDocs after delete:" + getIndexReader().numDocs());
			}
			catch (Exception e) 
			{
				logger.error("Error indexing notifications:" + e.getMessage(), e);
			}
			finally
			{
				logger.info("Releasing indexing flag");
				this.indexingInitialized.set(false);
			}
		}
	}

	public TopDocs query(String text, Integer numberOfHits) throws Exception 
	{
		return query("contents", text, numberOfHits);
	}

	public TopDocs query(String field, String text, Integer numberOfHits) throws Exception 
	{
		IndexSearcher searcher = getIndexSearcher();
		Query query = new QueryParser(Version.LUCENE_34, "contents", getStandardAnalyzer()).parse(text);
		TopDocs hits = searcher.search(query, numberOfHits);
		logger.info(hits.totalHits + " total matching documents for '" + text + "'");

		return hits;
	}
	
	public List<Document> queryDocuments(String field, String text, Integer numberOfHits) throws Exception 
	{
		IndexSearcher searcher = getIndexSearcher();
		Query query = new QueryParser(Version.LUCENE_34, field, getStandardAnalyzer()).parse(text);
		TopDocs hits = searcher.search(query, numberOfHits);
		logger.info(hits.totalHits + " total matching documents for '" + field + ":" + text + "'");
		List<Document> docs = new ArrayList<Document>();
		for(ScoreDoc scoreDoc : hits.scoreDocs)
		{
			org.apache.lucene.document.Document doc = searcher.doc(scoreDoc.doc);
			docs.add(doc);
		}
		searcher.close();
		return docs;
	}


	public TopDocs query(String[] fields, BooleanClause.Occur[] flags, String[] queries, Sort sort, Integer numberOfHits) throws Exception 
	{
		IndexSearcher searcher = getIndexSearcher();
		Query query = MultiFieldQueryParser.parse(Version.LUCENE_34, queries, fields, flags, getStandardAnalyzer());
		//Query query = new QueryParser(Version.LUCENE_34, "contents", getStandardAnalyzer()).parse(text);
		TopDocs hits = searcher.search(query, numberOfHits);
		logger.info(hits.totalHits + " total matching documents for '" + queries + "'");

		return hits;
	}

	public List<Document> queryDocuments(String[] fields, BooleanClause.Occur[] flags, String[] queries, Sort sort, Integer numberOfHits) throws Exception 
	{
		IndexSearcher searcher = getIndexSearcher();
		Query query = MultiFieldQueryParser.parse(Version.LUCENE_34, queries, fields, flags, getStandardAnalyzer());
		logger.info("query:" + query);
		//Query query = new QueryParser(Version.LUCENE_34, "contents", getStandardAnalyzer()).parse(text);
		TopDocs hits = searcher.search(query, numberOfHits);
		logger.info(hits.totalHits + " total matching documents for '" + queries + "'");
		List<Document> docs = new ArrayList<Document>();
		for(ScoreDoc scoreDoc : hits.scoreDocs)
		{
			org.apache.lucene.document.Document doc = searcher.doc(scoreDoc.doc);
			docs.add(doc);
		}
		searcher.close();
		return docs;
	}
	
	private void query(IndexSearcher searcher, Analyzer analyzer, String text) throws Exception 
	{
		Query query = new QueryParser(Version.LUCENE_34, "contents", analyzer).parse(text);
		TopDocs hits = searcher.search(query, 50);
		logger.info(hits.totalHits + " total matching documents for '" + text + "'");
		for(ScoreDoc scoreDoc : hits.scoreDocs)
		{
			org.apache.lucene.document.Document doc = searcher.doc(scoreDoc.doc);
			String cvId = doc.get("contentVersionId");
			logger.info("cvId: " + cvId);
		}
	}

	public boolean indexAll() throws Exception
	{
		Timer tt = new Timer();
		logger.warn("INDEXING ALL - correct?");
		Thread.dumpStack();
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		logger.info("------------------------------Got indexAll directive....");
		if (indexingInitialized.compareAndSet(false, true)) 
		{
			try
			{
				Timer t = new Timer();
				Timer t2 = new Timer();
				
				//Indexing all normal contents
				logger.info("Indexing all normal contents....");
				List<LanguageVO> languageVOList = LanguageController.getController().getLanguageVOList();
				Iterator<LanguageVO> languageVOListIterator = languageVOList.iterator();
				while(languageVOListIterator.hasNext())
				{
					LanguageVO languageVO = (LanguageVO)languageVOListIterator.next();
					
					List<NotificationMessage> notificationMessages = new ArrayList<NotificationMessage>();
					logger.info("Getting notification messages for " + languageVO.getName());
					int newLastContentVersionId = getContentNotificationMessages(notificationMessages, languageVO, 0);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getNotificationMessages", t.getElapsedTime());
					logger.info("newLastContentVersionId " + newLastContentVersionId);
					while(newLastContentVersionId != -1)
					{
						Thread.sleep(10000);
						newLastContentVersionId = getContentNotificationMessages(notificationMessages, languageVO, newLastContentVersionId);
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getNotificationMessages 2", t.getElapsedTime());
						logger.info("newLastContentVersionId " + newLastContentVersionId);
					}
				}

				languageVOList = LanguageController.getController().getLanguageVOList();
				languageVOListIterator = languageVOList.iterator();
				while(languageVOListIterator.hasNext())
				{
					LanguageVO languageVO = (LanguageVO)languageVOListIterator.next();
					
					List<NotificationMessage> notificationMessages = new ArrayList<NotificationMessage>();
					int newLastSiteNodeVersionId = getPageNotificationMessages(notificationMessages, languageVO, 0);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getNotificationMessagesForStructure", t.getElapsedTime());
					logger.info("newLastSiteNodeVersionId " + newLastSiteNodeVersionId);
					while(newLastSiteNodeVersionId != -1)
					{
						Thread.sleep(10000);
						newLastSiteNodeVersionId = getPageNotificationMessages(notificationMessages, languageVO, newLastSiteNodeVersionId);
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getNotificationMessages 2", t.getElapsedTime());
						logger.info("newLastSiteNodeVersionId " + newLastSiteNodeVersionId);
					}
				}
				
				t2.printElapsedTime("All indexing took");
			}
			catch (Exception e) 
			{
				logger.error("Error indexing notifications:" + e.getMessage(), e);
			}
			finally
			{
				logger.info("Releasing indexing flag");
				this.indexingInitialized.set(false);
			}
		}
		else
		{
			logger.warn("-------------------: Allready running index all...");
			return false;
		}
		return true;
	}
	
	
	/**
	 * This method gets called when a new notification has come. 
	 * It then iterates through the listeners and notifies them.
	 */
	public void addNotificationMessage(NotificationMessage notificationMessage)
	{
		if(notificationMessage.getClassName().equals(ContentImpl.class.getName()) ||
		   notificationMessage.getClassName().equals(ContentVersionImpl.class.getName()) ||
		   notificationMessage.getClassName().equals(SiteNodeImpl.class.getName()) ||
		   notificationMessage.getClassName().equals(SiteNodeVersionImpl.class.getName()) ||
		   notificationMessage.getClassName().equals(DigitalAssetImpl.class.getName()) ||
		   notificationMessage.getClassName().equals(MediumDigitalAssetImpl.class.getName()))
		{
			if(qeuedMessages.size() == 1000)
			{
				logger.warn("qeuedMessages went over 1000 - seems wrong");
				Thread.dumpStack();
			}
				
			synchronized (qeuedMessages)
			{
				qeuedMessages.add(notificationMessage);			
			}
		}
		else
		{
			logger.info("Skipping indexing:" + notificationMessage.getClassName());
		}
	}	

	/**
	 * This method gets called when a new NotificationMessage is available.
	 * The writer just calls the transactionHistoryController which stores it.
	 */

	public void notify(NotificationMessage notificationMessage)
	{
		try
		{
			if(logger.isInfoEnabled())
				logger.info("Indexing:" + notificationMessage.getName() + ":" + notificationMessage.getType() + ":" + notificationMessage.getObjectId() + ":" + notificationMessage.getObjectName());
			addNotificationMessage(notificationMessage);
		}
		catch(Exception e)
		{
			logger.error("Error notifying: " + e.getMessage());
		}
	}
	
	public void notifyListeners(boolean forceVersionIndexing, boolean checkForIndexingJobs) throws IOException, Exception
	{
		boolean initDoneLocally = false;
		boolean finishDoneLocally = false;

		logger.info("------------------------------->notifyListeners before check in " + CmsPropertyHandler.getContextRootPath());
		if (!checkForIndexingJobs || indexingInitialized.compareAndSet(false, true)) 
		{
			if(checkForIndexingJobs)
				initDoneLocally = true;
			
			List<NotificationMessage> internalMessageList = new ArrayList<NotificationMessage>();
			synchronized (qeuedMessages)
			{
				//logger.error("internalMessageList: " + internalMessageList.size() + "/" + qeuedMessages.size());
				internalMessageList.addAll(qeuedMessages);
				//logger.error("internalMessageList: " + internalMessageList.size() + "/" + qeuedMessages.size());
				qeuedMessages.clear();
				//logger.error("internalMessageList: " + internalMessageList.size() + "/" + qeuedMessages.size());
			}

			//Should implement equals on NotificationMessage later
			List<NotificationMessage> baseEntitiesToIndexMessageList = new ArrayList<NotificationMessage>();
			
			List<String> existingSignatures = new ArrayList<String>();
			logger.info("Before AAAAA:" + internalMessageList.size());
			Iterator cleanupInternalMessageListIterator = internalMessageList.iterator();
			while(cleanupInternalMessageListIterator.hasNext())
			{
				NotificationMessage notificationMessage = (NotificationMessage)cleanupInternalMessageListIterator.next();
				logger.info("Indexing........:" + notificationMessage.getClassName());

				if(notificationMessage.getClassName().equals(ContentImpl.class.getName()) || notificationMessage.getClassName().equals(Content.class.getName()))
				{
					ContentVO contentVO = ContentController.getContentController().getContentVOWithId((Integer)notificationMessage.getObjectId());

					ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
					if(ctdVO != null && ctdVO.getName().equals("Meta info"))
					{
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(contentVO.getContentId());
						NotificationMessage newNotificationMessage = new NotificationMessage("" + siteNodeVO.getName(), SiteNodeImpl.class.getName(), "SYSTEM", notificationMessage.getType(), siteNodeVO.getId(), "" + siteNodeVO.getName());
						String key = "" + newNotificationMessage.getClassName() + "_" + newNotificationMessage.getObjectId() + "_"  + "_" + newNotificationMessage.getType();
						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an META PAGE notification - just adding it AS A PAGE instead: " + newNotificationMessage.getObjectId());								
							baseEntitiesToIndexMessageList.add(newNotificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping Content notification - duplicate existed: " + notificationMessage.getObjectId());
						}

					}
					else
					{
						String key = "" + notificationMessage.getClassName() + "_" + notificationMessage.getObjectId() + "_"  + "_" + notificationMessage.getType();
						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an Content notification - just adding it: " + notificationMessage.getObjectId());
							baseEntitiesToIndexMessageList.add(notificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping Content notification - duplicate existed: " + notificationMessage.getObjectId());
						}
					}
				}
				else if(notificationMessage.getClassName().equals(ContentVersionImpl.class.getName()) || notificationMessage.getClassName().equals(ContentVersion.class.getName()))
				{
					logger.info("++++++++++++++Got an ContentVersion notification - focus on content: " + notificationMessage.getObjectId());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId((Integer)notificationMessage.getObjectId());
					ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
					
					ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
					if(ctdVO != null && ctdVO.getName().equals("Meta info"))
					{
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithMetaInfoContentId(contentVO.getContentId());
						NotificationMessage newNotificationMessage = new NotificationMessage("" + siteNodeVO.getName(), SiteNodeImpl.class.getName(), "SYSTEM", notificationMessage.getType(), siteNodeVO.getId(), "" + siteNodeVO.getName());
						String key = "" + newNotificationMessage.getClassName() + "_" + newNotificationMessage.getObjectId() + "_"  + newNotificationMessage.getType();
						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an META PAGE notification - just adding it AS A PAGE instead: " + newNotificationMessage.getObjectId());
							baseEntitiesToIndexMessageList.add(newNotificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping Content notification - duplicate existed: " + notificationMessage.getObjectId());
						}

					}
					else
					{
						NotificationMessage newNotificationMessage = new NotificationMessage("" + contentVersionVO.getContentName(), ContentImpl.class.getName(), "SYSTEM", notificationMessage.getType(), contentVersionVO.getContentId(), "" + contentVersionVO.getContentName());
						String key = "" + newNotificationMessage.getClassName() + "_" + newNotificationMessage.getObjectId() + "_" + newNotificationMessage.getType();
						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an Content notification - just adding it: " + newNotificationMessage.getObjectId());
							baseEntitiesToIndexMessageList.add(newNotificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping Content notification - duplicate existed: " + notificationMessage.getObjectId());
						}
					}
				}
				else if(notificationMessage.getClassName().equals(DigitalAssetImpl.class.getName()) || 
						notificationMessage.getClassName().equals(MediumDigitalAssetImpl.class.getName()) || 
						notificationMessage.getClassName().equals(DigitalAsset.class.getName()) || 
						notificationMessage.getClassName().equals(SiteNodeImpl.class.getName()) || 
						notificationMessage.getClassName().equals(SiteNode.class.getName()) || 
						notificationMessage.getClassName().equals(SiteNodeVersionImpl.class.getName()) || 
						notificationMessage.getClassName().equals(SiteNodeVersion.class.getName()))
				{
					logger.info("notificationMessage.getClassName():" + notificationMessage.getClassName());
					String key = "" + notificationMessage.getClassName() + "_" + notificationMessage.getObjectId() + "_"  + "_" + notificationMessage.getType();

					if(notificationMessage.getClassName().equals(SiteNodeVersionImpl.class.getName()) || notificationMessage.getClassName().equals(SiteNodeVersion.class.getName()))
					{
						logger.info("PPPPPPPPPPPPPPPPPPPPPPPPPP:" + notificationMessage.getObjectId());
						SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId((Integer)notificationMessage.getObjectId());
						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersionVO.getSiteNodeId());
						NotificationMessage newNotificationMessage = new NotificationMessage("" + siteNodeVO.getName(), SiteNodeImpl.class.getName(), "SYSTEM", notificationMessage.getType(), siteNodeVO.getId(), "" + siteNodeVO.getName());
						key = "" + newNotificationMessage.getClassName() + "_" + newNotificationMessage.getObjectId() + "_" + newNotificationMessage.getType();
					
						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an SiteNodeVersionImpl notification - just adding it as SiteNodeImpl: " + newNotificationMessage.getClassName() + ":" + newNotificationMessage.getObjectId());
							baseEntitiesToIndexMessageList.add(newNotificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping notification - duplicate existed: " + notificationMessage.getClassName() + ":" + notificationMessage.getObjectId());
						}
					}
					else if(notificationMessage.getClassName().equals(SiteNodeImpl.class.getName()) || notificationMessage.getClassName().equals(SiteNode.class.getName()))
					{
						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an Page notification - just adding it: " + notificationMessage.getClassName() + ":" + notificationMessage.getObjectId());
							baseEntitiesToIndexMessageList.add(notificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping notification - duplicate existed: " + notificationMessage.getClassName() + ":" + notificationMessage.getObjectId());
						}
					}
					else
					{
						NotificationMessage newNotificationMessage = new NotificationMessage("" + notificationMessage.getName(), DigitalAssetImpl.class.getName(), "SYSTEM", notificationMessage.getType(), notificationMessage.getObjectId(), "" + notificationMessage.getName());
						key = "" + newNotificationMessage.getClassName() + "_" + newNotificationMessage.getObjectId() + "_"  + "_" + newNotificationMessage.getType();

						if(!existingSignatures.contains(key))
						{
							logger.info("++++++++++++++Got an Content notification - just adding it: " + notificationMessage.getClassName() + ":" + notificationMessage.getObjectId());
							baseEntitiesToIndexMessageList.add(newNotificationMessage);
							existingSignatures.add(key);
						}
						else
						{
							logger.info("++++++++++++++Skipping notification - duplicate existed: " + notificationMessage.getClassName() + ":" + notificationMessage.getObjectId());
						}
					}
				}
			}
			internalMessageList = baseEntitiesToIndexMessageList;
			logger.info("After in [" + CmsPropertyHandler.getContextRootPath() + "]:" + internalMessageList.size());
			
			try
			{
				logger.info("notifyListeners actually running");
				if(getIsIndexedLocked())
				{
					logger.warn("The index should not be locked as no indexing is registered to be carried out. Lets unlock it as it may be the result of a crash.");
					unlockIndex();
				}
				
				//logger.error("Starting indexin of " + qeuedMessages.size());
				Timer t = new Timer();
				
				IndexWriter writer = getIndexWriter();
				//t.printElapsedTime("Creating writer took");
	

				Database db = CastorDatabaseService.getDatabase();
		
				beginTransaction(db);
				
				try
				{			
					int numberOfMessages = internalMessageList.size();
					Iterator internalMessageListIterator = internalMessageList.iterator();
					while(internalMessageListIterator.hasNext())
					{
						NotificationMessage notificationMessage = (NotificationMessage)internalMessageListIterator.next();
						try 
						{
							if(logger.isInfoEnabled())
								logger.info("Starting indexin of " + notificationMessage);
							indexInformation(notificationMessage, writer, internalMessageList, forceVersionIndexing, db);
							internalMessageListIterator.remove();
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}	
					//t.printElapsedTime("Indexing " + numberOfMessages + " documents took");
					//Map<String,String> commitUserData = new HashMap<String,String>();
					//internalMessageList.clear();
					//writer.commit(commitUserData);
					logger.info("##############lastCommitedContentVersionId before close:" + lastCommitedContentVersionId);
					if(lastCommitedContentVersionId > -1)
					{
						Integer previousLastCommittedContentVersionId = getLastCommitedContentVersionId();
						logger.info("##############previousLastCommittedContentVersionId before close:" + previousLastCommittedContentVersionId);
						if(previousLastCommittedContentVersionId < lastCommitedContentVersionId)
						{
							try 
							{
								logger.info("*************ADDING status doc " + lastCommitedContentVersionId + "**************");
								setLastCommitedContentVersionId(writer, lastCommitedContentVersionId);
							} 
							catch (Exception e) 
							{
								logger.error("*************ERROR: ADDING status doc**************", e);
							}			
						}
						else
						{
							logger.warn("The content version was not a higher number than what was allready indexed - lets not add status....");
						}
					}
					
					commitTransaction(db);
				}
				catch(Exception e)
				{
					logger.error("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
					rollbackTransaction(db);
				}
				
				writer.close();
				
				logger.info("OOOOOOOOOOOOOO:" + getLastCommitedContentVersionId());
			}
			catch (Exception e) 
			{
				logger.error("Error indexing notifications:" + e.getMessage(), e);
			}
			finally
			{
				logger.info("Releasing indexing flag");
				try
				{
					if(internalMessageList.size() > 0)
					{
						synchronized (qeuedMessages)
						{
							logger.info("Returning internalMessageList:" + internalMessageList.size() + " to qeuedMessages as some failed.");
							qeuedMessages.addAll(internalMessageList);
							internalMessageList.clear();
						}
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
				if(checkForIndexingJobs)
				{
					this.indexingInitialized.set(false);
					finishDoneLocally = true;
				}
			}
			
			if(initDoneLocally && !finishDoneLocally)
				logger.error("EEEEEEEEEEEEEEERRRRRRRRRRRRRRROOOOOOOOOOOORRRRRRRR aaaaaaa");

		}
		else
		{
			logger.warn("------------------------------->Indexing job allready running... skipping in " + CmsPropertyHandler.getContextRootPath());
		}
	}
	

	public void index() throws Exception
	{
		logger.warn("################# starting index");
		//if (indexingInitialized.compareAndSet(false, true)) 
		//{
			IndexReader indexReader = null;
			try
			{
				Integer lastCommitedContentVersionId = getLastCommitedContentVersionId();
				Date lastCommitedModifiedDate = getLastCommitedModifiedDate();
				
				Calendar yesterday = Calendar.getInstance();
				yesterday.add(Calendar.HOUR_OF_DAY, -1);
				
				logger.info("lastCommitedContentVersionId: " + lastCommitedContentVersionId);
				logger.info("lastCommitedModifiedDate: " + lastCommitedModifiedDate);
				indexReader = getIndexReader();
				boolean didIndex = false;
				if(lastCommitedContentVersionId == -1 || indexReader.numDocs() < 100)
				{
					logger.info("###########################IndexAll");
					didIndex = indexAll();
				}
				else //Skipping indexing for now..
				{
					logger.info("###########################indexIncremental");
					didIndex = indexIncremental(lastCommitedContentVersionId, yesterday.getTime());
				}
				
				if(didIndex)
				{
					CacheController.clearCache("pageCache");
					CacheController.clearCache("pageCacheExtra");
				}
			}
			catch (Exception e) 
			{
				logger.error("Error indexing notifications:" + e.getMessage(), e);
			}
			finally
			{
				logger.info("Releasing indexing flag");
				if(indexReader != null)
				{
					try { indexReader.close(); } catch (Exception e2) {}
				}
				//this.indexingInitialized.set(false);
			}
		/*
		}
		else
		{
			logger.info("################# skipping index");
		}
		*/
	}



	public boolean indexIncremental(Integer lastCommitedContentVersionId, Date lastCommitedDateTime) throws Exception
	{
		Timer t = new Timer();
		Timer t2 = new Timer();
		
		logger.info("Indexing incremental:" + lastCommitedContentVersionId + "/" + lastCommitedDateTime);
	    //Map<String,String> lastCommitData = reader.getCommitUserData();	    
	    
		List<LanguageVO> languageVOList = LanguageController.getController().getLanguageVOList();
		Iterator<LanguageVO> languageVOListIterator = languageVOList.iterator();
		while(languageVOListIterator.hasNext())
		{
			LanguageVO languageVO = (LanguageVO)languageVOListIterator.next();

			List<NotificationMessage> notificationMessages = new ArrayList<NotificationMessage>();
			//logger.error("Getting notification messages for " + languageVO.getName());
			int newLastContentVersionId = getNotificationMessages(notificationMessages, languageVO, lastCommitedContentVersionId, lastCommitedDateTime);
			while(newLastContentVersionId != -1)
			{
				if(logger.isInfoEnabled())
					t.printElapsedTime("Queueing " + notificationMessages.size() + " notificationMessages for indexing");
				for(NotificationMessage notificationMessage : notificationMessages)
				{
					notify(notificationMessage);
				}
				notifyListeners(true, false);
				notificationMessages.clear();
				//t.printElapsedTime("Indexing size():" + notificationMessages.size() + " took");
				
				Integer newLastContentVersionIdCandidate = getNotificationMessages(notificationMessages, languageVO, newLastContentVersionId, lastCommitedDateTime);
				logger.info("newLastContentVersionIdCandidate:" + newLastContentVersionIdCandidate + "=" + newLastContentVersionId);
				if(newLastContentVersionIdCandidate > newLastContentVersionId)
					newLastContentVersionId = newLastContentVersionIdCandidate;
				else
					break;
				//t.printElapsedTime("newLastContentVersionId:" + newLastContentVersionId + " took");
			}
			
		}
		if(logger.isInfoEnabled())
			t2.printElapsedTime("All indexing took");

		return true;
	}
	
	private int getNotificationMessagesForStructure(List<NotificationMessage> notificationMessages, LanguageVO languageVO, int lastSiteNodeVersionId) throws Exception
	{
		Timer t = new Timer();
		logger.info("getNotificationMessages:" + lastSiteNodeVersionId);

		int newLastSiteNodeVersionId = -1;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db);
			List<ContentVersionVO> versions = new ArrayList<ContentVersionVO>();
			if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
			{
				versions = ContentVersionController.getContentVersionController().getContentVersionVOList(contentTypeDefinitionVO.getId(), null, languageVO.getId(), false, 0, newLastSiteNodeVersionId, 250, true, db, true);
			}
			else
			{
				versions = ContentVersionController.getContentVersionController().getContentVersionVOList(contentTypeDefinitionVO.getId(), null, languageVO.getId(), false, Integer.parseInt(CmsPropertyHandler.getOperatingMode()), newLastSiteNodeVersionId, 250, true, db, true);				
			}
			
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Index all : getContentVersionVOList", t.getElapsedTime());

			logger.info("versions in getNotificationMessagesForStructure:" + versions.size());
			
			logger.info("Looping versions:" + versions.size());
			for(ContentVersionVO version : versions)
			{
				NotificationMessage notificationMessage = new NotificationMessage("LuceneController", ContentVersionImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, version.getId(), "dummy");
				notificationMessages.add(notificationMessage);
				newLastSiteNodeVersionId = version.getId().intValue();
			}
						
			logger.info("Finished round 1:" + notificationMessages.size() + ":" + newLastSiteNodeVersionId);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		commitTransaction(db);
		
		return newLastSiteNodeVersionId;
	}
	
	private int getContentNotificationMessages(List notificationMessages, LanguageVO languageVO, int lastContentVersionId) throws Exception
	{
		Timer t = new Timer();
		logger.info("getNotificationMessages:" + languageVO.getName() + " : " + lastContentVersionId);

		logger.info("notifyListeners actually running");
		if(getIsIndexedLocked())
		{
			logger.info("The index should not be locked as no indexing is registered to be carried out. Lets unlock it as it may be the result of a crash.");
			unlockIndex();
		}
		
		IndexWriter writer = getIndexWriter();
		//t.printElapsedTime("Creating writer took");
		
		int newLastContentVersionId = -1;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db);
			List<ContentVersionVO> versions = new ArrayList<ContentVersionVO>();
			if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
			{
				versions = ContentVersionController.getContentVersionController().getContentVersionVOList(null, contentTypeDefinitionVO.getId(), languageVO.getId(), false, 0, lastContentVersionId, 250, true, db, false);
			}
			else
			{
				versions = ContentVersionController.getContentVersionController().getContentVersionVOList(null, contentTypeDefinitionVO.getId(), languageVO.getId(), false, Integer.parseInt(CmsPropertyHandler.getOperatingMode()), lastContentVersionId, 250, true, db, false);				
			}
			
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Index all : getContentVersionVOList", t.getElapsedTime());

			logger.info("versions in getContentNotificationMessages:" + versions.size());
			
			logger.info("Looping versions:" + versions.size());
			for(ContentVersionVO version : versions)
			{
				Document document = getDocumentFromContentVersion(version, db);
				String uid = document.get("uid");

	    		writer.deleteDocuments(new Term("uid", "" + uid));
	    		
	    		if(logger.isInfoEnabled())
					logger.info("Adding document with uid:" + uid + " - " + document);
		    	if(document != null)
			    	writer.addDocument(document);

				logger.info("version assetCount:" + version.getAssetCount());
				if(version.getAssetCount() == null || version.getAssetCount() > 0)
				{
					List digitalAssetVOList = DigitalAssetController.getDigitalAssetVOList(version.getId(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDigitalAssetVOList", (t.getElapsedTimeNanos() / 1000));
	
					if(digitalAssetVOList.size() > 0)
					{
						logger.info("digitalAssetVOList:" + digitalAssetVOList.size());
						Iterator digitalAssetVOListIterator = digitalAssetVOList.iterator();
						while(digitalAssetVOListIterator.hasNext())
						{
							DigitalAssetVO assetVO = (DigitalAssetVO)digitalAssetVOListIterator.next();
							
							Document assetDocument = getDocumentFromDigitalAsset(assetVO, version, db);
							String assetUid = assetDocument.get("uid");
							
				    		writer.deleteDocuments(new Term("uid", "" + assetUid));

				    		if(logger.isInfoEnabled())
								logger.info("Adding document with assetUid:" + assetUid + " - " + assetDocument);
					    	if(assetDocument != null)
						    	writer.addDocument(assetDocument);
						}
					}
				}
				
				newLastContentVersionId = version.getId().intValue();
			}
						
			logger.info("Finished round 2:" + notificationMessages.size() + ":" + newLastContentVersionId);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		finally
		{
			try{setLastCommitedContentVersionId(writer, newLastContentVersionId); writer.optimize(); writer.close();}catch (Exception e) {e.printStackTrace();}
		}
		
		commitTransaction(db);
		
		return newLastContentVersionId;
	}

	private int getPageNotificationMessages(List notificationMessages, LanguageVO languageVO, int lastContentVersionId) throws Exception
	{
		Timer t = new Timer();
		logger.info("getNotificationMessages:" + languageVO.getName() + " : " + lastContentVersionId);

		logger.info("notifyListeners actually running");
		if(getIsIndexedLocked())
		{
			logger.info("The index should not be locked as no indexing is registered to be carried out. Lets unlock it as it may be the result of a crash.");
			unlockIndex();
		}
		
		IndexWriter writer = getIndexWriter();
		//t.printElapsedTime("Creating writer took");
		
		int newLastContentVersionId = -1;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db);
			List<ContentVersionVO> versions = new ArrayList<ContentVersionVO>();
			if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
			{
				versions = ContentVersionController.getContentVersionController().getContentVersionVOList(contentTypeDefinitionVO.getId(), null, languageVO.getId(), false, 0, lastContentVersionId, 250, true, db, true);
			}
			else
			{
				versions = ContentVersionController.getContentVersionController().getContentVersionVOList(contentTypeDefinitionVO.getId(), null, languageVO.getId(), false, Integer.parseInt(CmsPropertyHandler.getOperatingMode()), lastContentVersionId, 250, true, db, true);				
			}
			
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Index all : getContentVersionVOList", t.getElapsedTime());

			logger.info("versions in getContentNotificationMessages:" + versions.size());
			
			logger.info("Looping versions:" + versions.size());
			for(ContentVersionVO version : versions)
			{
				Document documents = getSiteNodeDocument(version, writer, db);
	    		String uid = documents.get("uid");
	    		
	    		writer.deleteDocuments(new Term("uid", "" + uid));
	    		
	    		if(logger.isInfoEnabled())
					logger.info("Adding document with uid:" + uid + " - " + documents);
	    		
		    	if(documents != null)
			    	writer.addDocument(documents);
	    		/*
				logger.info("version assetCount:" + version.getAssetCount());
				if(version.getAssetCount() == null || version.getAssetCount() > 0)
				{
					List digitalAssetVOList = DigitalAssetController.getDigitalAssetVOList(version.getId(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDigitalAssetVOList", (t.getElapsedTimeNanos() / 1000));
	
					if(digitalAssetVOList.size() > 0)
					{
						logger.info("digitalAssetVOList:" + digitalAssetVOList.size());
						Iterator digitalAssetVOListIterator = digitalAssetVOList.iterator();
						while(digitalAssetVOListIterator.hasNext())
						{
							DigitalAssetVO assetVO = (DigitalAssetVO)digitalAssetVOListIterator.next();
							NotificationMessage assetNotificationMessage = new NotificationMessage("LuceneController", DigitalAssetImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, assetVO.getId(), "dummy");
							notificationMessages.add(assetNotificationMessage);							
						}
					}
				}
				
				NotificationMessage notificationMessage = new NotificationMessage("LuceneController", ContentVersionImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, version.getId(), "dummy");
				notificationMessages.add(notificationMessage);
				*/
				newLastContentVersionId = version.getId().intValue();
			}
						
			logger.info("Finished round 3:" + notificationMessages.size() + ":" + newLastContentVersionId);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		finally
		{
			try{setLastCommitedContentVersionId(writer, newLastContentVersionId); writer.optimize(); writer.close();}catch (Exception e) {e.printStackTrace();}
		}
		
		commitTransaction(db);
		
		return newLastContentVersionId;
	}

	private int getNotificationMessages(List notificationMessages, LanguageVO languageVO, int lastContentVersionId, Date lastCheckDateTime) throws Exception
	{
		logger.info("getNotificationMessages:" + languageVO.getName() + " : " + lastContentVersionId + ":" + lastCheckDateTime);

		int newLastContentVersionId = -1;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
        	Calendar date = Calendar.getInstance();
        	date.setTime(lastCheckDateTime);
        	date.add(Calendar.DAY_OF_YEAR, -1);
        	
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM " + SmallestContentVersionImpl.class.getName() + " cv WHERE cv.languageId = $1 AND cv.isActive = $2 AND (cv.contentVersionId > $3 OR cv.modifiedDateTime > $4) ORDER BY cv.contentVersionId");
			oql.bind(languageVO.getId());
			oql.bind(true);
			oql.bind(lastContentVersionId);
			oql.bind(date.getTime());
					
			QueryResults results = oql.execute(Database.ReadOnly);
			
			int processedItems = 0;
			Integer previousContentId = null;
			while (results.hasMore()) 
			{
				SmallestContentVersionImpl smallestContentVersionImpl = (SmallestContentVersionImpl)results.next();
				if(previousContentId == null || !previousContentId.equals(smallestContentVersionImpl.getContentId()))
				{
					List digitalAssetVOList = DigitalAssetController.getDigitalAssetVOList(smallestContentVersionImpl.getId(), db);
					if(digitalAssetVOList.size() > 0)
					{
						logger.info("digitalAssetVOList:" + digitalAssetVOList.size());
						Iterator digitalAssetVOListIterator = digitalAssetVOList.iterator();
						while(digitalAssetVOListIterator.hasNext())
						{
							DigitalAssetVO assetVO = (DigitalAssetVO)digitalAssetVOListIterator.next();
							if(assetVO.getAssetFileSize() < 10000000) //Do not index large files
							{
								NotificationMessage assetNotificationMessage = new NotificationMessage("LuceneController", DigitalAssetImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, assetVO.getId(), "dummy");
								notificationMessages.add(assetNotificationMessage);
							}
						}
					}
					
					NotificationMessage notificationMessage = new NotificationMessage("LuceneController", ContentVersionImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, smallestContentVersionImpl.getId(), "dummy");
					notificationMessages.add(notificationMessage);
					previousContentId = smallestContentVersionImpl.getContentId();
				}
				newLastContentVersionId = smallestContentVersionImpl.getId().intValue();
				lastCommitedContentVersionId = newLastContentVersionId;
				processedItems++;
				logger.info("previousContentId:" + previousContentId + "/" + processedItems);
			}
						
			results.close();
			logger.info("Finished round 4:" + processedItems + ":" + newLastContentVersionId);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		commitTransaction(db);
		
		return newLastContentVersionId;
	}
	
	
	
	private void indexInformation(NotificationMessage notificationMessage, IndexWriter writer, List<NotificationMessage> internalMessageList, Boolean forceVersionIndexing, Database db)
	{
    	Timer t = new Timer();

		try 
	    {
		    try 
		    {
		    	//writer.setMaxMergeDocs(500000);
		    	if(logger.isInfoEnabled())
					logger.info("Indexing to directory '" + writer.getDirectory().toString() + "'...");

	    		List<Document> documents = getDocumentsForIncremental(notificationMessage, writer, forceVersionIndexing, db);
	    	
	    		Iterator<Document> documentsIterator = documents.iterator();
		    	while(documentsIterator.hasNext())
		    	{
		    		Document indexingDocument = documentsIterator.next();
		    		String uid = indexingDocument.get("uid");
		    		if(logger.isInfoEnabled())
						logger.info("Adding document with uid:" + uid + " - " + indexingDocument);
					//logger.error("Adding document with uid:" + uid + " - " + indexingDocument);
			    	if(indexingDocument != null)
				    	writer.addDocument(indexingDocument);
		    	}			    		
		    } 
		    catch (Exception e) 
		    {
		    	logger.error("Error indexing:" + e.getMessage(), e);
		    }
		    finally
		    {
		    	indexedDocumentsSinceLastOptimize++;
		    	if(indexedDocumentsSinceLastOptimize > 1000)
		    	{
		    		//logger.info("Optimizing...");
		    		writer.optimize();
		    		indexedDocumentsSinceLastOptimize = 0;
		    	}
		    	//writer.close();	    	
		    }
	    }
		catch (Exception e) 
	    {
			logger.error("Error indexing:" + e.getMessage(), e);
	    }
	}
	
	
	private List<Document> getDocumentsForIncremental(NotificationMessage notificationMessage, IndexWriter writer, Boolean forceVersionIndexing, Database db) throws Exception
	{
		Timer t = new Timer();
		List<Document> returnDocuments = new ArrayList<Document>();
		
		logger.info("2222222222 notificationMessage.getClassName():" + notificationMessage.getClassName() + " in " + CmsPropertyHandler.getApplicationName());
		Set<Integer> contentIdsToIndex = new HashSet<Integer>();
		Set<Integer> siteNodeIdsToIndex = new HashSet<Integer>();
		
		if(notificationMessage.getClassName().equals(ContentImpl.class.getName()) || notificationMessage.getClassName().equals(Content.class.getName()))
		{
			logger.info("++++++++++++++Got an Content notification: " + notificationMessage.getObjectId());
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId((Integer)notificationMessage.getObjectId(), db);
			//ContentVO contentVO = ContentController.getContentController().getContentVOWithId((Integer)notificationMessage.getObjectId());
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentVOWithId", (t.getElapsedTimeNanos() / 1000));
			contentIdsToIndex.add(contentVO.getId());
		}
		else if(notificationMessage.getClassName().equals(ContentVersionImpl.class.getName()) || notificationMessage.getClassName().equals(ContentVersion.class.getName()))
		{
			logger.info("++++++++++++++Got an ContentVersion notification: " + notificationMessage.getObjectId());
			
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId((Integer)notificationMessage.getObjectId(), db);
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentVersionVOWithId", t.getElapsedTime());

			contentIdsToIndex.add(contentVersionVO.getContentId());
		}
		else if(notificationMessage.getClassName().equals(DigitalAssetImpl.class.getName()) || notificationMessage.getClassName().equals(DigitalAsset.class.getName()))
		{
			logger.info("++++++++++++++Got an DigitalAssetImpl notification: " + notificationMessage.getObjectId());
			Database db2 = CastorDatabaseService.getDatabase();

			beginTransaction(db2);
			
			try
			{			
				//////////ANTAGLIGEN ON�DIGT MED MEDIUM h�r
				MediumDigitalAssetImpl asset = (MediumDigitalAssetImpl)DigitalAssetController.getMediumDigitalAssetWithIdReadOnly((Integer)notificationMessage.getObjectId(), db2);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMediumDigitalAssetWithIdReadOnly", t.getElapsedTime());
				Collection contentVersions = asset.getContentVersions();
				if(logger.isInfoEnabled())
					logger.info("contentVersions:" + contentVersions.size());
				Iterator contentVersionsIterator = contentVersions.iterator();
				while(contentVersionsIterator.hasNext())
				{
					ContentVersion version = (ContentVersion)contentVersionsIterator.next();
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("contentVersionsIterator", t.getElapsedTime());

					Document document = getDocumentFromDigitalAsset(asset.getValueObject(), version.getValueObject(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDocumentFromDigitalAsset", t.getElapsedTime());
					logger.info("00000000000000000: Adding asset document:" + document);
					if(document != null)
						returnDocuments.add(document);
				}
				
				commitTransaction(db2);
			}
			catch(Exception e)
			{
				logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db2);
				throw new SystemException(e.getMessage());
			}		
		}
		else if(notificationMessage.getClassName().equals(SiteNodeImpl.class.getName()) || notificationMessage.getClassName().equals(SiteNode.class.getName()))
		{
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId((Integer)notificationMessage.getObjectId(), db);
			siteNodeIdsToIndex.add(siteNodeVO.getId());
		}
		
		logger.info("Indexing:" + siteNodeIdsToIndex.size());
		for(Integer siteNodeId : siteNodeIdsToIndex)
		{
			//Deleting all info based on content
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
			logger.info("Deleting all info on:" + siteNodeId);
			//TODO - Fixa s� inte assets tas med h�r....
		    Query query = new QueryParser(Version.LUCENE_34, "siteNodeId", analyzer).parse("" + siteNodeId); 
			writer.deleteDocuments(query);
			//End

			logger.info("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ:" + notificationMessage.getObjectId());
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId((Integer)notificationMessage.getObjectId(), db);
			logger.info("$$$$$$$$$$Getting doc for " + siteNodeVO.getName());
			Document document = getDocumentFromSiteNode(siteNodeVO, writer, db);
			logger.info("document " + document);
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDocumentFromSiteNode", t.getElapsedTime());
			if(document != null)
				returnDocuments.add(document);
		}
		
		logger.info("Indexing contentIdsToIndex:" + contentIdsToIndex.size());
		for(Integer contentId : contentIdsToIndex)
		{
			//Deleting all info based on content
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
			logger.info("Deleting all info on:" + contentId);
			//TODO - Fixa s� inte assets tas med h�r....
			
			String[] fields = new String[]{"isAsset","contentId"};
			String[] queries = new String[]{"true","" + contentId};
			BooleanClause.Occur[] flags = new BooleanClause.Occur[]{BooleanClause.Occur.MUST_NOT,BooleanClause.Occur.MUST};
			Query query = MultiFieldQueryParser.parse(Version.LUCENE_34, queries, fields, flags, analyzer);
			
		    //Query query = new QueryParser(Version.LUCENE_34, "contentId", analyzer).parse("" + contentId); 
			writer.deleteDocuments(query);
			//End
			
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
			
			Document document = getDocumentFromContent(contentVO, notificationMessage, writer, forceVersionIndexing);
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDocumentFromContent", (t.getElapsedTimeNanos() / 1000));
			
			if(document != null)
			{
				returnDocuments.add(document);

				logger.info("++++++++++++++Forcing cv indexing");
				List<ContentVersionVO> versions = new ArrayList<ContentVersionVO>();
				if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
				{
					//List<LanguageVO> languages = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId());
					List<LanguageVO> languages = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLanguageVOList", (t.getElapsedTimeNanos() / 1000));
					for(LanguageVO language : languages)
					{
						ContentVersionVO latestVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), language.getId(), Integer.parseInt(CmsPropertyHandler.getOperatingMode()), db);
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionVO", (t.getElapsedTimeNanos() / 1000));

						if(latestVersion != null)
							versions.add(latestVersion);

						ContentVersionVO latestVersionPublishedVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), language.getId(), ContentVersionVO.PUBLISHED_STATE, db);
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionVO", (t.getElapsedTimeNanos() / 1000));
						if(latestVersionPublishedVersion != null && latestVersionPublishedVersion.getId().intValue() != latestVersion.getId().intValue())
							versions.add(latestVersionPublishedVersion);
					}

				}
				else
				{
					List<LanguageVO> languages = LanguageController.getController().getLanguageVOList(contentVO.getRepositoryId(), db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLanguageVOList", (t.getElapsedTimeNanos() / 1000));
					for(LanguageVO language : languages)
					{
						ContentVersionVO version = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), language.getId(), Integer.parseInt(CmsPropertyHandler.getOperatingMode()), db);
						RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getLatestActiveContentVersionVO", (t.getElapsedTimeNanos() / 1000));
						if(version != null)
							versions.add(version);
					}
				}

				logger.info("versions:" + versions.size());
				for(ContentVersionVO version : versions)
				{
					logger.info("version:" + version.getId());
					Document versionDocument = getDocumentFromContentVersion(version, db);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDocumentFromContentVersion", t.getElapsedTime());
					if(versionDocument != null)
						returnDocuments.add(versionDocument);
					
					if(version.getId() > this.lastCommitedContentVersionId)
						lastCommitedContentVersionId = version.getId();
				}
			}
		}
		
		return returnDocuments;
	}

	private List<Document> getDocumentsForContentVersion(ContentVersionVO contentVersionVO, Database db) throws Exception
	{
		Timer t = new Timer();
		List<Document> returnDocuments = new ArrayList<Document>();
				
		//ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
		//RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentVOWithId", (t.getElapsedTimeNanos() / 1000));
					
		Document document = getDocumentFromContentVersion(contentVersionVO, db);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDocumentFromContentVersion", t.getElapsedTime());
		if(document != null)
			returnDocuments.add(document);
			
		return returnDocuments;
	}

	public Document getDocumentFromSiteNode(SiteNodeVO siteNodeVO, IndexWriter writer, Database db) throws Exception, InterruptedException
	{
		logger.info("getDocumentFromSiteNode:" + siteNodeVO.getName() + ":" + siteNodeVO.getIsDeleted());

		if(siteNodeVO == null || siteNodeVO.getIsDeleted())
		{
			logger.info("Adding a delete directive to the indexer");
			
			String uid = "siteNodeId_" + siteNodeVO.getId();
    		logger.info("Deleting documents:" + "uid=" + uid);
    		
    		logger.info("Before delete:" + writer.numDocs());
    		//writer.deleteDocuments(new Term("uid", "" + uid));
    		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
		    Query query = new QueryParser(Version.LUCENE_34, "siteNodeId", analyzer).parse("" + siteNodeVO.getId()); 
    		writer.deleteDocuments(query);

    		logger.info("Before delete:" + writer.numDocs());
    		
			return null;
		}
		
		// make a new, empty document
		Document doc = new Document();

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new NumericField("publishDateTime", Field.Store.YES, true).setLongValue(siteNodeVO.getPublishDateTime().getTime()));
		doc.add(new Field("modified", DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("siteNodeId", "" + siteNodeVO.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("repositoryId", "" + siteNodeVO.getRepositoryId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModifier", "" + siteNodeVO.getCreatorName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isAsset", "false", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isSiteNode", "true", Field.Store.YES, Field.Index.NOT_ANALYZED));
		//doc.add(new Field("contentTypeDefinitionId", "" + ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db).getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "siteNodeId_" + siteNodeVO.getId(), Field.Store.NO, Field.Index.NOT_ANALYZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(siteNodeVO.getName())));
		
		if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId() > -1)
		{
			List<LanguageVO> languages = LanguageController.getController().getLanguageVOList(siteNodeVO.getRepositoryId(), db);
			for(LanguageVO language : languages)
			{
				ContentVersionVO cvVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(siteNodeVO.getMetaInfoContentId(), language.getId(), Integer.parseInt(CmsPropertyHandler.getOperatingMode()), db);
				if(cvVO != null)
					doc.add(new Field("contents", new StringReader(cvVO.getVersionValue())));
			}
		}
		
		// return the document
		return doc;
	}
	
	public Document getSiteNodeDocument(ContentVersionVO contentVersionVO, IndexWriter writer, Database db) throws Exception, InterruptedException
	{
		Timer t = new Timer();
		
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentVOWithId", (t.getElapsedTimeNanos() / 1000));

		if(contentVO.getIsDeleted())
			return null;

		// make a new, empty document
		Document doc = new Document();

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new NumericField("publishDateTime", Field.Store.YES, true).setLongValue(contentVersionVO.getModifiedDateTime().getTime()));
		doc.add(new Field("modified", DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("siteNodeId", "" + contentVersionVO.getSiteNodeId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModifier", "" + contentVersionVO.getVersionModifier(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isAsset", "false", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isSiteNode", "true", Field.Store.YES, Field.Index.NOT_ANALYZED));
		//doc.add(new Field("contentTypeDefinitionId", "" + ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Meta info", db).getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "siteNodeId_" + contentVersionVO.getSiteNodeId(), Field.Store.NO, Field.Index.NOT_ANALYZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(contentVersionVO.getSiteNodeName())));
		doc.add(new Field("contents", new StringReader(contentVersionVO.getVersionValue())));


		// return the document
		return doc;
	}
	
	public Document getDocumentFromContent(ContentVO contentVO, NotificationMessage message, IndexWriter writer, boolean indexVersions) throws Exception, InterruptedException
	{
		logger.info("getDocumentFromContent:" + contentVO.getName() + ":" + contentVO.getIsDeleted());

		if(contentVO == null || contentVO.getIsDeleted())
		{
			//NotificationMessage notificationMessage = new NotificationMessage(message.getName(), message.getClassName(), message.getSystemUserName(), NotificationMessage.TRANS_DELETE, message.getObjectId(), message.getObjectName());
			logger.info("Adding a delete directive to the indexer");
			//internalMessageList.add(notificationMessage);
			
			String uid = "contentId_" + contentVO.getId();
    		logger.info("Deleting documents:" + "uid=" + uid);
    		
    		logger.info("Before delete:" + writer.numDocs());
    		//writer.deleteDocuments(new Term("uid", "" + uid));
    		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
		    Query query = new QueryParser(Version.LUCENE_34, "contentId", analyzer).parse("" + contentVO.getId()); 
    		writer.deleteDocuments(query);

    		logger.info("Before delete:" + writer.numDocs());
    		
			return null;
		}
		
		// make a new, empty document
		Document doc = new Document();

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new NumericField("publishDateTime", Field.Store.YES, true).setLongValue(contentVO.getPublishDateTime().getTime()));
		doc.add(new Field("modified", DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentId", "" + contentVO.getContentId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentTypeDefinitionId", "" + contentVO.getContentTypeDefinitionId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModifier", "" + contentVO.getCreatorName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isAsset", "false", Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "contentId_" + contentVO.getId(), Field.Store.NO, Field.Index.NOT_ANALYZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(contentVO.getName())));

		// return the document
		return doc;
	}

	public Document getDocumentFromContentVersion(ContentVersionVO contentVersionVO, Database db) throws Exception, InterruptedException
	{
		logger.info("getting document from content version:" + contentVersionVO.getContentName());
		Timer t = new Timer();
		//ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentVOWithId", (t.getElapsedTimeNanos() / 1000));

		if(contentVO.getIsDeleted())
			return null;
			
		// make a new, empty document
		Document doc = new Document();

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		logger.info("contentVersionVO:" + contentVersionVO.getContentName());

		doc.add(new NumericField("publishDateTime", Field.Store.YES, true).setLongValue(contentVO.getPublishDateTime().getTime()));
		doc.add(new NumericField("modificationDateTime", Field.Store.YES, true).setLongValue(contentVersionVO.getModifiedDateTime().getTime()));
		doc.add(new Field("modified", DateTools.timeToString(contentVersionVO.getModifiedDateTime().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentVersionId", "" + contentVersionVO.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentId", "" + contentVersionVO.getContentId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentTypeDefinitionId", "" + contentVO.getContentTypeDefinitionId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("languageId", "" + contentVersionVO.getLanguageId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModifier", "" + contentVersionVO.getVersionModifier(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("stateId", "" + contentVersionVO.getStateId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isAsset", "false", Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Indexing normalFields", (t.getElapsedTimeNanos() / 1000));
		
		//Testing adding the categories for this version
		try
		{
			if(contentVO.getContentTypeDefinitionId() != null)
			{
				ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId(), db);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getContentTypeDefinitionVOWithId", (t.getElapsedTimeNanos() / 1000));
				List<CategoryAttribute> categoryKeys = ContentTypeDefinitionController.getController().getDefinedCategoryKeys(ctdVO, true);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getDefinedCategoryKeys", (t.getElapsedTimeNanos() / 1000));
				for(CategoryAttribute categoryKey : categoryKeys)
				{
					logger.info("categoryKey:" + categoryKey.getValue() + " for content:" + contentVO.getName());
					//List<ContentCategoryVO> contentCategoryVOList = ContentCategoryController.getController().findByContentVersionAttribute(categoryKey.getValue(), contentVersionVO.getId());
					List<ContentCategory> contentCategoryVOList = ContentCategoryController.getController().findByContentVersionAttribute(categoryKey.getValue(), contentVersionVO.getId(), db, true);
					RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Indexing categories", (t.getElapsedTimeNanos() / 1000));
					logger.info("contentCategoryVOList:" + contentCategoryVOList.size());
					for(ContentCategory contentCategory : contentCategoryVOList)
					{
						doc.add(new Field("categories", "" + contentCategory.getAttributeName().replaceAll(" ", "_").toLowerCase() + "eq" + contentCategory.getCategory().getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
						doc.add(new Field("categories", "" + contentCategory.getAttributeName() + "=" + contentCategory.getCategory().getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
						doc.add(new Field("" + contentCategory.getAttributeName() + "_categoryId", "" + contentCategory.getCategory().getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("Problem indexing categories for contentVO: " + contentVO.getName() + "(" + contentVO.getId() + "): " + e.getMessage(), e);
		}
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Indexing categories", (t.getElapsedTimeNanos() / 1000));
		//End test
				
		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "contentVersionId_" + contentVersionVO.getId(), Field.Store.NO, Field.Index.NOT_ANALYZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(contentVersionVO.getVersionValue())));
		doc.add(new Field("contents", new StringReader(contentVersionVO.getContentName())));

		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("Indexing end fields", (t.getElapsedTimeNanos() / 1000));

		// return the document
		return doc;
	}
	

	public Document getDocumentFromDigitalAsset(DigitalAssetVO digitalAssetVO, ContentVersionVO contentVersionVO, Database db) throws Exception, InterruptedException
	{
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
		if(contentVO == null || contentVO.getIsDeleted())
			return null;

		// make a new, empty document
		Document doc = new Document();
		
		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		//doc.add(new Field("modified", DateTools.timeToString(contentVersionVO.getModifiedDateTime().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new NumericField("modificationDateTime", Field.Store.YES, true).setLongValue(contentVersionVO.getModifiedDateTime().getTime()));
		doc.add(new Field("digitalAssetId", "" + digitalAssetVO.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentVersionId", "" + contentVersionVO.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentId", "" + contentVersionVO.getContentId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contentTypeDefinitionId", "" + contentVO.getContentTypeDefinitionId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("languageId", "" + contentVersionVO.getLanguageId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModifier", "" + contentVersionVO.getVersionModifier(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("stateId", "" + contentVersionVO.getStateId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("isAsset", "true", Field.Store.YES, Field.Index.NOT_ANALYZED));

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "digitalAssetId_" + digitalAssetVO.getId(), Field.Store.NO, Field.Index.NOT_ANALYZED));
		//doc.add(new Field("uid", "" + contentVersionVO.getId(), Field.Store.NO, Field.Index.NOT_ANALYZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(digitalAssetVO.getAssetKey() + " " + digitalAssetVO.getAssetFileName() + " " + digitalAssetVO.getAssetContentType())));

		//String url = DigitalAssetController.getController().getDigitalAssetUrl(digitalAssetVO, db);
		//if(logger.isInfoEnabled())
		//	logger.info("url if we should index file:" + url);
		String filePath = DigitalAssetController.getController().getDigitalAssetFilePath(digitalAssetVO, db);
		if(logger.isInfoEnabled())
			logger.info("filePath if we should index file:" + filePath);
		String text = extractTextToIndex(digitalAssetVO, new File(filePath));
	
		doc.add(new Field("contents", new StringReader(text)));
		
		return doc;
	}

	public Document createStatusDocument(Integer lastCommitedContentVersionId) throws Exception
	{
		logger.info("CCCCCCCCReatng status doc");
		Document doc = new Document();

		doc.add(new Field("lastCommitedContentVersionId", "" + lastCommitedContentVersionId, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastCommitedModifiedDate", "" + new Date().getTime(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		//doc.add(new Field("uid", "contentId_" + contentVO.getId(), Field.Store.NO, Field.Index.NOT_ANALYZED));
		//doc.add(new Field("meta", "lastCommitedContentVersionId", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("meta", new StringReader("lastCommitedContentVersionId")));

		return doc;
	}
	
	public Document getStatusDocument() throws Exception
	{
	    List<Document> docs = queryDocuments("meta", "lastCommitedContentVersionId", 5);
		logger.info(docs.size() + " total matching documents for 'lastCommitedContentVersionId'");
		return (docs != null && docs.size() > 0 ? docs.get(0) : null);
	}
	
	public Integer getLastCommitedContentVersionId() throws Exception
	{
		Integer lastCommitedContentVersionId = -1;
		
		Document doc = getStatusDocument();
		logger.info("STATUS doc:" + doc);
		if(doc != null)
		{
			String lastCommitedContentVersionIdString = doc.get("lastCommitedContentVersionId");
			
			logger.info("doc:" + doc);
			logger.info("lastCommitedContentVersionId:" + lastCommitedContentVersionIdString);
			
			lastCommitedContentVersionId = Integer.parseInt(lastCommitedContentVersionIdString);
		}
		
		return lastCommitedContentVersionId;
	}

	private void setLastCommitedContentVersionId(IndexWriter writer, Integer lastCommitedContentVersionId) throws Exception 
	{
		Integer prevLastCommitedContentVersionId = getLastCommitedContentVersionId();
		logger.info("prevLastCommitedContentVersionId:" + prevLastCommitedContentVersionId);
		logger.info("lastCommitedContentVersionId:" + lastCommitedContentVersionId);
		if(lastCommitedContentVersionId == -1 || prevLastCommitedContentVersionId > lastCommitedContentVersionId)
			return;
		
		logger.info("setLastCommitedContentVersionId:" + lastCommitedContentVersionId);
		Query query = new QueryParser(Version.LUCENE_34, "meta", getStandardAnalyzer()).parse("lastCommitedContentVersionId");
		writer.deleteDocuments(query);
		writer.addDocument(createStatusDocument(lastCommitedContentVersionId));
	}
	
	public Date getLastCommitedModifiedDate() throws Exception
	{
		Date lastCommitedModifiedDate = new Date(10000);
		
		Document doc = getStatusDocument();
		if(doc != null)
		{
			String lastCommitedModifiedDateString = doc.get("lastCommitedModifiedDate");
	
			logger.info("doc:" + doc);
			logger.info("lastCommitedModifiedDate:" + lastCommitedModifiedDateString);
			
			Date d = new Date();
			d.setTime(Long.parseLong(lastCommitedModifiedDateString));
			lastCommitedModifiedDate = d;
		}
		
		return lastCommitedModifiedDate;
	}

	private String extractTextToIndex(DigitalAssetVO digitalAssetVO, File file)
	{
		String text = "";
		
		if(logger.isInfoEnabled())
			logger.info("Asset content type:" + digitalAssetVO.getAssetContentType());
		
		if(digitalAssetVO.getAssetContentType().equalsIgnoreCase("application/pdf"))
		{
			try
            {
				Writer output = null;
	            PDDocument document = null;
	            try
	            {
	                document = PDDocument.load(file);
	                
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                if(!document.isEncrypted())
	                {
	                	output = new OutputStreamWriter(baos, "UTF-8");
	
	                    PDFTextStripper stripper = new PDFTextStripper();
	
	                    //stripper.setSortByPosition( sort );
	                    //stripper.setStartPage( startPage );
	                    //stripper.setEndPage( endPage );
	                    stripper.writeText( document, output );
	                    text = baos.toString("UTF-8");
	                    if(logger.isInfoEnabled())
	    					logger.info("PDF Document has " + text.length() + " chars\n\n" + text);
	                }
	            }
	            catch (Exception e) 
	            {
	            	logger.warn("Error indexing file: " + file + "\nMessage: " + e.getMessage());
				}
	            finally
	            {
	                if( output != null )
	                {
	                    output.close();
	                }
	                if( document != null )
	                {
	                    document.close();
	                }
	            }
            }
			catch (Exception e) 
			{
            	logger.warn("Error indexing:" + e.getMessage());
			}
		}
		else if(digitalAssetVO.getAssetContentType().equalsIgnoreCase("application/msword"))
		{
			try
			{
				POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));

				// Create a document for this file
				HWPFDocument doc = new HWPFDocument(fs);

				// Create a WordExtractor to read the text of the word document
				WordExtractor we = new WordExtractor(doc);

				// Extract all paragraphs in the document as strings
				text = we.getText();

				// Output the document
				if(logger.isInfoEnabled())
					logger.info("Word Document has " + text.length() + " chars\n\n" + text);
			} 
			catch (Exception e)
			{
				logger.warn("Error indexing file: " + file + "\nMessage: " + e.getMessage());
			}
		}
		
		return text;
	}
	
	/**
	 * This is a method that never should be called.
	 */

	public BaseEntityVO getNewVO()
	{
		return null;
	}

	public void setContextParameters(Map map)
	{
		// TODO Auto-generated method stub
	}
}
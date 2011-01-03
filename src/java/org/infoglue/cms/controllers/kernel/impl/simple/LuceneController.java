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
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallestContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationListener;
import org.infoglue.cms.util.NotificationMessage;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

public class LuceneController extends BaseController implements NotificationListener
{
    private final static Logger logger = Logger.getLogger(LuceneController.class.getName());
    private static int indexedDocumentsSinceLastOptimize = 0;
    
	/**
	 * Default Constructor	
	 */
	
	public static LuceneController getController()
	{
		return new LuceneController();
	}
	
	private static List<NotificationMessage> qeuedMessages = new ArrayList<NotificationMessage>();
	
	public void notifyListeners()
	{
		logger.info("Starting indexin of " + qeuedMessages.size());

		List<NotificationMessage> internalMessageList = new ArrayList<NotificationMessage>();

		synchronized (qeuedMessages)
		{
			internalMessageList.addAll(qeuedMessages);
			qeuedMessages.clear();
		}
		
		Iterator internalMessageListIterator = internalMessageList.iterator();
		while(internalMessageListIterator.hasNext())
		{
			NotificationMessage notificationMessage = (NotificationMessage)internalMessageListIterator.next();
			try 
			{
				if(logger.isInfoEnabled())
					logger.info("Starting indexin of " + notificationMessage);
				indexInformation(notificationMessage);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}		
	}
	
	private void indexInformation(NotificationMessage notificationMessage)
	{
		try 
	    {
			//Object objectIdentity = getObjectIdentity(object);
	
			IndexWriter writer = null;
		    try 
		    {
				String index = CmsPropertyHandler.getContextRootPath() + File.separator + "lucene" + File.separator + "index";
	
		    	File INDEX_DIR = new File(index);
		    	writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer(new String[]{}));
		    	writer.setMaxMergeDocs(500000);
		    	if(logger.isInfoEnabled())
					logger.info("Indexing to directory '" + INDEX_DIR + "'...");
	
		    	if(notificationMessage.getType() == NotificationMessage.TRANS_CREATE)
		    	{
		    		List<Document> documents = getDocuments(notificationMessage);
			    	Iterator<Document> documentsIterator = documents.iterator();
			    	while(documentsIterator.hasNext())
			    	{
			    		Document indexingDocument = documentsIterator.next();
			    		String uid = indexingDocument.get("uid");
			    		if(logger.isInfoEnabled())
							logger.info("Adding document with uid:" + uid + " - " + indexingDocument);
				    	if(indexingDocument != null)
					    	writer.addDocument(indexingDocument);
			    	}
		    	}
		    	else if(notificationMessage.getType() == NotificationMessage.TRANS_UPDATE)
				{
		    		List<Document> documents = getDocuments(notificationMessage);
			    	Iterator<Document> documentsIterator = documents.iterator();
			    	while(documentsIterator.hasNext())
			    	{
			    		Document indexingDocument = documentsIterator.next();
			    		String uid = indexingDocument.get("uid");
			    		if(logger.isInfoEnabled())
							logger.info("Updating document with uid:" + uid + " - " + indexingDocument);
				    	if(indexingDocument != null)
				    		writer.updateDocument(new Term("uid", "" + uid), indexingDocument);
			    	}
				}
		    	else if(notificationMessage.getType() == NotificationMessage.TRANS_DELETE)
				{
		    		String uid = "";
		    		if(notificationMessage.getClassName().equals(ContentImpl.class.getName()))
		    		{
		    			uid = "contentId_" + notificationMessage.getObjectId();
		    		}
		    		else if(notificationMessage.getClassName().equals(ContentVersionImpl.class.getName()))
		    		{
		    			uid = "contentVersionId_" + notificationMessage.getObjectId();
		    		}
		    		else if(notificationMessage.getClassName().equals(DigitalAssetImpl.class.getName()))
		    		{
		    			uid = "digitalAssetId_" + notificationMessage.getObjectId();		    			
		    		}
		    		
		    		if(logger.isInfoEnabled())
						logger.info("Deleting documents:" + "uid=" + uid);
		    		writer.deleteDocuments(new Term("uid", "" + uid));
				}
		    } 
		    catch (Exception e) 
		    {
		    	logger.error("Error indexing:" + e.getMessage(), e);
		    }
		    finally
		    {
		    	indexedDocumentsSinceLastOptimize++;
		    	if(indexedDocumentsSinceLastOptimize > 250)
		    	{
		    		logger.info("Optimizing...");
		    		writer.optimize();
		    		indexedDocumentsSinceLastOptimize = 0;
		    	}
		    	writer.close();	    	
		    }
	    }
		catch (Exception e) 
	    {
			logger.error("Error indexing:" + e.getMessage(), e);
	    }
	}

	
	private List<Document> getDocuments(NotificationMessage notificationMessage) throws Exception
	{
		List<Document> returnDocuments = new ArrayList<Document>();
		
		if(notificationMessage.getClassName().equals(ContentImpl.class.getName()))
		{
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId((Integer)notificationMessage.getObjectId());
			returnDocuments.add(getDocumentFromContent(contentVO));
		}
		else if(notificationMessage.getClassName().equals(ContentVersionImpl.class.getName()))
		{
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId((Integer)notificationMessage.getObjectId());
			returnDocuments.add(getDocumentFromContentVersion(contentVersionVO));
		}
		else if(notificationMessage.getClassName().equals(DigitalAssetImpl.class.getName()))
		{
			Database db = CastorDatabaseService.getDatabase();

			beginTransaction(db);
			
			try
			{			
				MediumDigitalAssetImpl asset = (MediumDigitalAssetImpl)DigitalAssetController.getMediumDigitalAssetWithIdReadOnly((Integer)notificationMessage.getObjectId(), db);
				Collection contentVersions = asset.getContentVersions();
				if(logger.isInfoEnabled())
					logger.info("contentVersions:" + contentVersions.size());
				Iterator contentVersionsIterator = contentVersions.iterator();
				while(contentVersionsIterator.hasNext())
				{
					ContentVersion cv = (ContentVersion)contentVersionsIterator.next();
					returnDocuments.add(getDocumentFromDigitalAsset(asset.getValueObject(), cv.getValueObject()));
				}
				
				commitTransaction(db);
			}
			catch(Exception e)
			{
				logger.error("An error occurred so we should not complete the transaction:" + e, e);
				rollbackTransaction(db);
				throw new SystemException(e.getMessage());
			}		
		}
		/*
		else if(object.getClass().getName().equals(SiteNodeVersionImpl.class.getName()))
		{
		}
		*/
		
		return returnDocuments;
	}

	public Document getDocumentFromContent(ContentVO contentVO) throws Exception, InterruptedException
	{
		// make a new, empty document
		Document doc = new Document();

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new Field("modified", DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentId", "" + contentVO.getContentId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentTypeDefinitionId", "" + contentVO.getContentTypeDefinitionId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("lastModifier", "" + contentVO.getCreatorName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("isAsset", "true", Field.Store.YES, Field.Index.UN_TOKENIZED));

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "contentId_" + contentVO.getId(), Field.Store.NO, Field.Index.UN_TOKENIZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(contentVO.getName())));

		// return the document
		return doc;
	}

	public Document getDocumentFromContentVersion(ContentVersionVO contentVersionVO) throws Exception, InterruptedException
	{
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
		
		// make a new, empty document
		Document doc = new Document();

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new Field("modified", DateTools.timeToString(contentVersionVO.getModifiedDateTime().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentVersionId", "" + contentVersionVO.getId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentId", "" + contentVersionVO.getContentId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentTypeDefinitionId", "" + contentVO.getContentTypeDefinitionId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("languageId", "" + contentVersionVO.getLanguageId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("lastModifier", "" + contentVersionVO.getVersionModifier(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("stateId", "" + contentVersionVO.getStateId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("isAsset", "false", Field.Store.YES, Field.Index.UN_TOKENIZED));

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "contentVersionId_" + contentVersionVO.getId(), Field.Store.NO, Field.Index.UN_TOKENIZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(contentVersionVO.getVersionValue())));
		doc.add(new Field("contents", new StringReader(contentVersionVO.getContentName())));

		// return the document
		return doc;
	}
	

	public Document getDocumentFromDigitalAsset(DigitalAssetVO digitalAssetVO, ContentVersionVO contentVersionVO) throws Exception, InterruptedException
	{
		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());

		// make a new, empty document
		Document doc = new Document();
		
		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new Field("modified", DateTools.timeToString(contentVersionVO.getModifiedDateTime().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentVersionId", "" + contentVersionVO.getId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentId", "" + contentVersionVO.getContentId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("contentTypeDefinitionId", "" + contentVO.getContentTypeDefinitionId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("languageId", "" + contentVersionVO.getLanguageId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("repositoryId", "" + contentVO.getRepositoryId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("lastModifier", "" + contentVersionVO.getVersionModifier(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("stateId", "" + contentVersionVO.getStateId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("isAsset", "true", Field.Store.YES, Field.Index.UN_TOKENIZED));

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", "digitalAssetId_" + digitalAssetVO.getId(), Field.Store.NO, Field.Index.UN_TOKENIZED));
		//doc.add(new Field("uid", "" + contentVersionVO.getId(), Field.Store.NO, Field.Index.UN_TOKENIZED));

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(new Field("contents", new StringReader(digitalAssetVO.getAssetKey() + " " + digitalAssetVO.getAssetFileName() + " " + digitalAssetVO.getAssetContentType())));

		String url = DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getId());
		if(logger.isInfoEnabled())
			logger.info("url if we should index file:" + url);
		String filePath = DigitalAssetController.getDigitalAssetFilePath(digitalAssetVO.getId());
		if(logger.isInfoEnabled())
			logger.info("filePath if we should index file:" + filePath);
		String text = extractTextToIndex(digitalAssetVO, new File(filePath));
	
		doc.add(new Field("contents", new StringReader(text)));
		
		return doc;
	}

	
	/**
	 * This method gets called when a new notification has come. 
	 * It then iterates through the listeners and notifies them.
	 */
	public void addNotificationMessage(NotificationMessage notificationMessage)
	{
		synchronized (qeuedMessages)
		{
			qeuedMessages.add(notificationMessage);			
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
	
	
	//Analytic and index management methods 
	
	public Map getIndexInformation()
	{
		Map info = new HashMap();
		
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
			    	writer.deleteDocuments(new Term("initializer", "true"));
			    	logger.info("Optimizing...");
			    	writer.optimize();
			    	writer.close();
			    } 
			    catch (Exception e) 
			    {
			    	logger.error("Error creating index:" + e.getMessage(), e);
			    }
			}
			
		    IndexReader reader = IndexReader.open(index);
		    int maxDoc = reader.maxDoc();
		    int numDoc = reader.numDocs();
		    long lastModified = IndexReader.lastModified(index);

		    info.put("maxDoc", new Integer(maxDoc));
		    info.put("numDoc", new Integer(numDoc));
		    info.put("lastModified", new Date(lastModified));

			reader.close();	
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error creating index:" + e.getMessage(), e);
	    }
	    
	    return info;
	}

	public void deleteIndex()
	{
		String index = CmsPropertyHandler.getContextRootPath() + File.separator + "lucene" + File.separator + "index";
	    try 
	    {
	    	File INDEX_DIR = new File(index);
	    	IndexWriter writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer(), true);
	    	logger.info("Indexing to directory '" + INDEX_DIR + "'...");
	    	writer.deleteDocuments(new Term("initializer", "true"));
	    	logger.info("Optimizing...");
	    	writer.optimize();
	    	writer.close();
	    } 
	    catch (Exception e) 
	    {
	    	logger.error("Error creating index:" + e.getMessage(), e);
		}
	}

	
	public void indexAll() throws Exception
	{
		List notificationMessages = new ArrayList();
		
		List languageVOList = LanguageController.getController().getLanguageVOList();
		Iterator languageVOListIterator = languageVOList.iterator();
		while(languageVOListIterator.hasNext())
		{
			LanguageVO languageVO = (LanguageVO)languageVOListIterator.next();
			
			int newLastContentVersionId = getNotificationMessages(notificationMessages, languageVO, 0);
			while(newLastContentVersionId != -1)
				newLastContentVersionId = getNotificationMessages(notificationMessages, languageVO, newLastContentVersionId);
		}
		
		logger.info("notificationMessages:" + notificationMessages.size());

		int i = 0;
		Iterator notificationMessagesIterator = notificationMessages.iterator();
		while(notificationMessagesIterator.hasNext())
		{
			if(i == 200)
			{
				notifyListeners();
				logger.info("Pausing....");
				Thread.sleep(1000);
				i = 0;
			}
			
			NotificationMessage notificationMessage = (NotificationMessage)notificationMessagesIterator.next();
			notify(notificationMessage);
			i++;
		}
	}

	private int getNotificationMessages(List notificationMessages, LanguageVO languageVO, int lastContentVersionId) throws Exception
	{
		logger.info("getNotificationMessages:" + languageVO.getName() + " : " + lastContentVersionId);

		int newLastContentVersionId = -1;
		
		Database db = CastorDatabaseService.getDatabase();
		try
		{
			beginTransaction(db);
			
			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM " + SmallestContentVersionImpl.class.getName() + " cv WHERE cv.languageId = $1 AND cv.isActive = $2 AND cv.contentVersionId > $3 ORDER BY cv.contentVersionId");
			oql.bind(languageVO.getId());
			oql.bind(true);
			oql.bind(lastContentVersionId);
			
			QueryResults results = oql.execute(Database.ReadOnly);
			
			int processedItems = 0;
			Integer previousContentId = null;
			while (results.hasMore() && processedItems < 100) 
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
							NotificationMessage assetNotificationMessage = new NotificationMessage("LuceneController", DigitalAssetImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, assetVO.getId(), "dummy");
							notificationMessages.add(assetNotificationMessage);							
						}
					}
					
					NotificationMessage notificationMessage = new NotificationMessage("LuceneController", ContentVersionImpl.class.getName(), "SYSTEM", NotificationMessage.TRANS_UPDATE, smallestContentVersionImpl.getId(), "dummy");
					notificationMessages.add(notificationMessage);
					previousContentId = smallestContentVersionImpl.getContentId();
				}
				newLastContentVersionId = smallestContentVersionImpl.getId().intValue();
				processedItems++;
			}
						
			results.close();
			logger.info("Finished round:" + processedItems + ":" + newLastContentVersionId);
		}
		catch ( Exception e )
		{
			rollbackTransaction(db);
			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
		}
		
		commitTransaction(db);
		
		return newLastContentVersionId;
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
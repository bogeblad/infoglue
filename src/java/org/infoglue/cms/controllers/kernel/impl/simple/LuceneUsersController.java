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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.NotificationListener;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.deliver.util.Timer;

public class LuceneUsersController extends BaseController implements NotificationListener
{
    private final static Logger logger = Logger.getLogger(LuceneUsersController.class.getName());
	private static Directory 	idx 				= null;
	private static long 		idxCreatedDateTime 	= 0L;
	private static AtomicBoolean idxReindexing 	= new AtomicBoolean(false);

	/**
	 * Default Constructor	
	 */
	
	public static LuceneUsersController getController()
	{
		return new LuceneUsersController();
	}
	
	private Analyzer getAnalyzer() throws Exception
	{
		return new ClassicAnalyzer(Version.LUCENE_34);
		//return new KeywordAnalyzer();
		//return new WhitespaceAnalyzer(Version.LUCENE_34);
		//return new StandardAnalyzer(Version.LUCENE_34);
	}

	
	private synchronized void validateIndex() throws Exception
	{
		long indexTimeout = 3600000;
		
		if(idx == null)
		{
			reIndex();
		}
		else
		{
			long diff = System.currentTimeMillis() - idxCreatedDateTime;
			if(idx == null || (diff > indexTimeout && !idxReindexing.getAndSet(true)))
			{
				new Thread()
		        {
		            public void run() 
		            {
		    			logger.info("Reindex...");
		    			try
		    			{
		    				reIndex();
		    			}
		    			catch (Exception e) 
		    			{
		    				logger.warn("Error reindexing users: " + e.getMessage());
						}
		    			finally
		    			{
		    				idxReindexing.set(false);
		    			}
		            }
		        }.start();
				
			}
		}
		logger.info("" + idx);
	}
	
	/**
	 * Call this method directly if you have a threaded implementation which syncs and caches the users on a regular basis.
	 * 
	 * @throws Exception
	 */

	public synchronized void reIndex() throws Exception
	{
		Directory newIndex = new RAMDirectory();
		logger.info("creating index....");
		createIndex(newIndex);
		idx = newIndex;
		idxCreatedDateTime = System.currentTimeMillis();
	}

	/**
	 * Call this method directly if you must have a forced reset right away.
	 * 
	 * @throws Exception
	 */

	public synchronized void reset() throws Exception
	{
		idx = null;
	}

	/**
	 * This method creates the index by indexing all users.
	 */

	private Directory createIndex(Directory directory) throws CorruptIndexException, LockObtainFailedException, IOException, Exception
	{
		Analyzer analyzer = getAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		IndexWriter iw = new IndexWriter(directory, config);
		
		Timer t = new Timer();
		addDocuments(iw, UserControllerProxy.getController().getAllUsers());
		t.printElapsedTime("Getting and indexing all users took", 50);
		
		iw.optimize();
		iw.close();
		return idx;
	}

	
	/**
	 * This method is used to fetch all or a subset of sorted users either filtered on a text or not.  
	 * If the index is not created or older than set interval the index is created.
	 */

	public List<InfoGluePrincipal> getFilteredUsers(Integer offset, Integer limit, String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws Exception
	{
		Timer t = new Timer();
		
		if(logger.isInfoEnabled())
		{
			logger.info("offset: " + offset);
			logger.info("limit: " + limit);
			logger.info("sortProperty: " + sortProperty);
			logger.info("direction: " + direction);
			logger.info("searchString: " + searchString);			
			logger.info("populateRolesAndGroups: " + populateRolesAndGroups);			
		}

		validateIndex();
		t.printElapsedTime("Validate index took", 50);
		
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		try
		{
			List<Document> docs;
			
			logger.info("sortProperty:" + sortProperty + ":" + direction);
			if(searchString == null || searchString.equals(""))
				docs = queryDocuments(offset, limit, sortProperty, direction);
			else
				docs = queryDocuments(offset, limit, sortProperty, direction, searchString);
			
			logger.info("docs:" + docs.size());
			for(Document doc : docs)
			{
				String userName = doc.get("userName");
				InfoGluePrincipal user = new InfoGluePrincipal(userName, doc.get("userName"), doc.get("firstName"), doc.get("lastName"), doc.get("email"), doc.get("source"), true, new Date(), new ArrayList(), new ArrayList(), new HashMap(), false, null);
				users.add(user);
			}
		}
		catch (Exception e) 
		{
			logger.warn("Error getting filtered users:" + e.getMessage(), e);
		}
		
		logger.info("Users took:" + t.getElapsedTime());
		return users;		
	}
	
	/**
	 * This method returns the number of users in the system (optionally filtered by text search).
	 */

	public Integer getUserCount(String searchString) throws Exception 
	{
		List<Document> docs = null;
		
		validateIndex();

		if(searchString == null || searchString.equals(""))
			docs = queryDocuments(0, 1000000, "userName", "asc");
		else
			docs = queryDocuments(0, 1000000, "userName", "asc", searchString);

		return docs.size();
	}
	
	/**
	 * This method adds all users to the.
	 */

	private void addDocuments(IndexWriter iw, List<InfoGluePrincipal> users) throws CorruptIndexException, IOException
	{
		logger.info("Indexing users:" + users.size());
		Timer t = new Timer();
		
		for (InfoGluePrincipal user : users)
		{
			Document doc = new Document();

			// Add user
			String contentToIndex = "" + user.getName().toLowerCase() + " " + user.getFirstName() + " " + user.getLastName() + " " + user.getEmail().toLowerCase() + " " + user.getSource();
			
			doc.add(new Field("contents", new StringReader(contentToIndex)));

			doc.add(new Field("userName", user.getName().toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field("firstName", user.getFirstName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field("lastName", user.getLastName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field("email", user.getEmail().toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field("source", user.getSource(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new NumericField("lastModified", Field.Store.YES, true).setLongValue(user.getModifiedDateTime().getTime()));
			
			for(InfoGlueRole role : user.getRoles())
			{
				if(!role.getName().equals("anonymous"))
					logger.info("role:" + role.getName());
				doc.add(new Field("roles", new StringReader("" + role.getName())));
			}
			
			for(InfoGlueGroup group : user.getGroups())
			{
				if(!group.getName().equals("anonymous"))
					logger.info("group:" + group.getName());
				doc.add(new Field("groups", new StringReader("" + group.getName())));
			}
			
			iw.addDocument(doc);
		}
		
		if(logger.isInfoEnabled())
			t.printElapsedTime("Indexing users took");
	}
	
	
	/**
	 * Getting the index searcher
	 */

	private IndexSearcher getIndexSearcher() throws Exception
	{
		IndexReader reader = IndexReader.open(idx, true);
	    return new IndexSearcher(reader);
	}

	/**
	 * A query api for getting all users sorted and paged
	 */

	public List<Document> queryDocuments(Integer offset, Integer limit, String sortProperty, String direction) throws Exception 
	{
		IndexSearcher searcher = getIndexSearcher();
		List<Document> docs = new ArrayList<Document>();

		if(offset == null)
			offset = 0;
		
		if(limit == null)
			limit = 10;
		
		if(sortProperty == null)
			sortProperty = "userName";

		boolean reverse = false;
		if(direction != null && direction.equalsIgnoreCase("desc"))
			reverse = true;
				
		logger.info("sortProperty:" + sortProperty);
		logger.info("reverse:" + reverse);
		SortField sf = new SortField(sortProperty, SortField.STRING, reverse);

		Query query = new MatchAllDocsQuery();
		
		TopFieldDocs topDocs = searcher.search(query, (Filter) null, 1000000, new Sort(sf));
		logger.info("offset:" + offset);
		logger.info("limit:" + limit);
		logger.info("topDocs.totalHits:" + topDocs.totalHits);
		
		int start = offset;
		int end = offset+limit;
		if(end > topDocs.totalHits)
			end = topDocs.totalHits;

		logger.info("start:" + start);
		logger.info("end:" + end);

		for(int i=start; i<end; i++)
		{
			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
			Document doc = searcher.doc(scoreDoc.doc);
			docs.add(doc);
		}

		searcher.close();
		return docs;
	}

	/**
	 * A query api for getting all users sorted and paged filtered on search text
	 */

	public List<Document> queryDocuments(Integer offset, Integer limit, String sortProperty, String direction, String searchText) throws Exception 
	{
		Analyzer analyzer = getAnalyzer();
		IndexSearcher searcher = getIndexSearcher();
		List<Document> docs = new ArrayList<Document>();

		if(offset == null)
			offset = 0;
		
		if(limit == null)
			limit = 10;
		
		if(sortProperty == null)
			sortProperty = "userName";

		boolean reverse = false;
		if(direction != null && direction.equalsIgnoreCase("desc"))
			reverse = true;
		
		SortField sf = new SortField(sortProperty, SortField.STRING, reverse);
		logger.info("searchText:" + searchText);
		Query query = new QueryParser(Version.LUCENE_34, "contents", analyzer).parse(searchText+"*");
		
		TopFieldDocs topDocs = searcher.search(query, (Filter) null, 1000000, new Sort(sf));
		logger.info("offset:" + offset);
		logger.info("limit:" + limit);
		logger.info("topDocs.totalHits:" + topDocs.totalHits);
		
		int start = offset;
		int end = offset+limit;
		if(end > topDocs.totalHits)
			end = topDocs.totalHits;

		logger.info("start:" + start);
		logger.info("end:" + end);

		for(int i=start; i<end; i++)
		{
			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
			Document doc = searcher.doc(scoreDoc.doc);
			//System.out.println("doc:" + doc);
			docs.add(doc);
		}

		searcher.close();
		return docs;
	}
	
	/**
	 * A query api for getting user docs found by role or group mainly sorted and paged and optinally filtered on search text
	 */

	public List<Document> getDocuments(String field, String entityName, Integer offset, Integer limit, String sortProperty, String direction, String searchText) throws Exception 
	{
		Analyzer analyzer = getAnalyzer();
		IndexSearcher searcher = getIndexSearcher();
		List<Document> docs = new ArrayList<Document>();

		if(offset == null)
			offset = 0;
		
		if(limit == null)
			limit = 10;
		
		if(sortProperty == null)
			sortProperty = "userName";

		boolean reverse = false;
		if(direction != null && direction.equalsIgnoreCase("desc"))
			reverse = true;
		
		SortField sf = new SortField(sortProperty, SortField.STRING, reverse);
		logger.info("searchText:" + searchText);
		Query query = new QueryParser(Version.LUCENE_34, field, analyzer).parse(entityName);
		if(searchText != null && !searchText.equals(""))
		{
			MultiFieldQueryParser mfqp = new MultiFieldQueryParser(Version.LUCENE_34, new String[]{field, "contents"}, analyzer);
			mfqp.setDefaultOperator(MultiFieldQueryParser.AND_OPERATOR);
			query = mfqp.parse(entityName + " " + searchText + "*");
		}
		
		logger.info("query" + query);
		
		TopFieldDocs topDocs = searcher.search(query, (Filter) null, 1000000, new Sort(sf));
		logger.info("offset:" + offset);
		logger.info("limit:" + limit);
		logger.info("topDocs.totalHits:" + topDocs.totalHits);
		
		int start = offset;
		int end = offset+limit;
		if(end > topDocs.totalHits)
			end = topDocs.totalHits;

		logger.info("start:" + start);
		logger.info("end:" + end);

		for(int i=start; i<end; i++)
		{
			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
			Document doc = searcher.doc(scoreDoc.doc);
			docs.add(doc);
		}

		searcher.close();
		return docs;
	}

	@Override
	public void setContextParameters(Map map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notify(NotificationMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BaseEntityVO getNewVO() {
		// TODO Auto-generated method stub
		return null;
	}

	

}

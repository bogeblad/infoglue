package org.infoglue.cms.security;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.infoglue.deliver.util.Timer;

/**
 * @author Mattias Bogeblad
 *
 * This authorization module can be used by all Authorization modules that cannot provide advanced paging/search directly from the source but
 * wants a search enabled / paged experience anyway. Works on cached data and the update interval can be decided by using the normal extra security settings (parameter name 'indexTimeout').
 */

public abstract class BasicIndexedAuthorizationModule implements AuthorizationModule
{
    private final static Logger logger = Logger.getLogger(TestAuthorizationModule.class.getName());

	private static Directory 	idx 				= null;
	private static long 		idxCreatedDateTime 	= 0L;

	public abstract List<InfoGlueRole> getAllRoles() throws Exception;
	public abstract List<InfoGlueGroup> getAllGroups() throws Exception;
	public abstract List<InfoGluePrincipal> getAllUsers(boolean populateRolesAndGroups) throws Exception;

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

	private synchronized void validateIndex() throws Exception
	{
		long indexTimeout = 30000;
		if(getExtraProperties() != null)
		{
			String indexTimeoutFromProperties = (String)getExtraProperties().get("indexTimeout");
			if(indexTimeoutFromProperties != null && !indexTimeoutFromProperties.equals(""))
			{
				try
				{
					indexTimeout = Long.parseLong(indexTimeoutFromProperties);
				}
				catch (Exception e) 
				{
					logger.error("Error parsing indexTimeout from the extra settings. Using default. Message:" + e.getMessage());
				}
			}
		}
		
		long diff = System.currentTimeMillis() - idxCreatedDateTime;
		if(idx == null || diff > indexTimeout)
		{
			reIndex();
		}
	}
	
	/**
	 * This method is used to fetch all users part of the named role.  
	 */
	public List<InfoGluePrincipal> getRoleUsers(String roleName) throws Exception
	{
		return getRoleUsers(roleName, null, 1000000, null, null, null);
	}
	
	/**
	 * This method is used to fetch all users part of the named group.  
	 */

	public List<InfoGluePrincipal> getGroupUsers(String groupName) throws Exception
	{
		return getGroupUsers(groupName, null, 1000000, null, null, null);
	}
	
	/**
	 * This method is used to fetch all or a subset of sorted users either filtered on a text or not.  
	 * If the index is not created or older than set interval the index is created.
	 */

	public List<InfoGluePrincipal> getFilteredUsers(Integer offset, Integer limit, String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws Exception
	{
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
		
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		try
		{
			List<Document> docs;
			
			if(searchString == null || searchString.equals(""))
				docs = queryDocuments(offset, limit, sortProperty, direction);
			else
				docs = queryDocuments(offset, limit, sortProperty, direction, searchString);
			
			logger.info("docs:" + docs.size());
			for(Document doc : docs)
			{
				String userName = doc.get("userName");
				InfoGluePrincipal user = new InfoGluePrincipal(userName, doc.get("userName"), doc.get("firstName"), doc.get("lastName"), doc.get("email"), doc.get("source"), true, new Date(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(user);
			}
		}
		catch (Exception e) 
		{
			logger.warn("Error getting filtered users:" + e.getMessage(), e);
		}
		
		return users;		
	}
		
	/**
	 * A method returning the number of users matching a roleName and also contains searched text
	 */
	public Integer getRoleUserCount(String roleName, String searchString) throws Exception 
	{
		return getRoleUsers(roleName, null, 1000000, null, null, searchString).size();
	}

	/**
	 * A method returning the number of users not matching a roleName and also contains searched text
	 */
	public Integer getRoleUserInvertedCount(String roleName, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, 1000000, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getRoleUsers(roleName, null, 1000000, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);
		
		return newAllUsers.size();
	}

	/**
	 * A method returning the number of users matching a groupName and also contains searched text
	 */
	public Integer getGroupUserCount(String groupName, String searchString) throws Exception 
	{
		return getGroupUsers(groupName, null, 1000000, null, null, searchString).size();
	}

	/**
	 * A method returning the number of users not matching a groupName and also contains searched text
	 */
	public Integer getGroupUserInvertedCount(String groupName, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, 1000000, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getGroupUsers(groupName, null, 1000000, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);
		
		return newAllUsers.size();
	}

	/**
	 * A method returning the all/subset of sorted users part of stated role and optionally contains searched text
	 */

	public List<InfoGluePrincipal> getRoleUsers(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		try
		{
			validateIndex();
			
			List<Document> docs = getDocuments("roles", roleName, offset, limit, sortProperty, direction, searchString);
			
			logger.info("docs:" + docs.size());
			for(Document doc : docs)
			{
				String userName = doc.get("userName");
				InfoGluePrincipal user = new InfoGluePrincipal(userName, doc.get("userName"), doc.get("firstName"), doc.get("lastName"), doc.get("email"), doc.get("source"), true, new Date(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(user);
			}
		}
		catch (Exception e) 
		{
			logger.error("Error getting roleUsers:" + e.getMessage(), e);
		}
		
	    return users;
	}

	/**
	 * A method returning the all/subset of sorted users not part of stated role and optionally contains searched text
	 */

	public List<InfoGluePrincipal> getRoleUsersInverted(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, 1000000, sortProperty, direction, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getRoleUsers(roleName, null, 1000000, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);

		return newAllUsers;
	}

	/**
	 * A method returning the all/subset of sorted users part of stated group and optionally contains searched text
	 */

	public List<InfoGluePrincipal> getGroupUsers(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		try
		{
			validateIndex();

			List<Document> docs = getDocuments("groups", groupName, offset, limit, sortProperty, direction, searchString);
			
			logger.info("docs:" + docs.size());
			for(Document doc : docs)
			{
				String userName = doc.get("userName");
				InfoGluePrincipal user = new InfoGluePrincipal(userName, doc.get("userName"), doc.get("firstName"), doc.get("lastName"), doc.get("email"), doc.get("source"), true, new Date(), new ArrayList(), new ArrayList(), new HashMap(), false, this);
				users.add(user);
			}
		}
		catch (Exception e) 
		{
			logger.error("Error getting groupUsers: " + e.getMessage(), e);
		}
		
	    return users;
	}

	/**
	 * A method returning the all/subset of sorted users not part of stated group and optionally contains searched text
	 */

	public List<InfoGluePrincipal> getGroupUsersInverted(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, sortProperty, direction, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getGroupUsers(groupName, null, 1000000, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);

		return newAllUsers;
	}

	/**
	 * This method returns the number of users in the system (optionally filtered by text search).
	 * A basic implementation just barely implementing the feature. Should be done better.
	 */

	public Integer getRoleCount(String searchString) throws Exception 
	{
		return getRoles().size();
	}

	/**
	 * This method returns the number of users in the system (optionally filtered by text search).
	 * A basic implementation just barely implementing the feature. Should be done better.
	 */

	public Integer getGroupCount(String searchString) throws Exception 
	{
		return getGroups().size();
	}

	/**
	 * This method returns the number of users in the system (optionally filtered by text search).
	 */

	public Integer getUserCount(String searchString) throws Exception 
	{
		List<Document> docs = null;
		
		if(searchString == null || searchString.equals(""))
			docs = queryDocuments(0, 1000000, "userName", "asc");
		else
			docs = queryDocuments(0, 1000000, "userName", "asc", searchString);

		return docs.size();
	}

	
	
	
	/**
	 * This method creates the index by indexing all users.
	 */

	private Directory createIndex(Directory directory) throws CorruptIndexException, LockObtainFailedException, IOException, Exception
	{
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		IndexWriter iw = new IndexWriter(directory, config);
		
		addDocuments(iw, getAllUsers(true));

		iw.optimize();
		iw.close();
		return idx;
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
			String contentToIndex = "" + user.getName() + " " + user.getFirstName() + " " + user.getLastName() + " " + user.getEmail() + " " + user.getSource();
			
			doc.add(new Field("contents", new StringReader(contentToIndex)));

			doc.add(new Field("userName", user.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("firstName", user.getFirstName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("lastName", user.getLastName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("email", user.getEmail(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("source", user.getSource(), Field.Store.YES, Field.Index.NOT_ANALYZED));
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
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
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
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
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


}



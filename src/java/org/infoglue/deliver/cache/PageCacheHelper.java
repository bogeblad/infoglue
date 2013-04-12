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

package org.infoglue.deliver.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.CompressionHelper;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;



public class PageCacheHelper implements Runnable
{ 
    public final static Logger logger = Logger.getLogger(PageCacheHelper.class.getName());
	private static VisualFormatter formatter = new VisualFormatter();

    private static PageCacheHelper singleton = null;

    public static Set<String> pageCacheEvicitionQueue = Collections.synchronizedSet(new HashSet<String>());
    public static Set<String> fileDeletionQueue = Collections.synchronizedSet(new HashSet<String>());
    	
    public static Map<String,Date> disabledPages = new HashMap<String,Date>();
    
    private CompressionHelper compressionHelper = new CompressionHelper();
   
	private static AtomicInteger numberOfPageCacheFiles = new AtomicInteger(0);
	
	public static PageCacheHelper getInstance() 
	{
		if(singleton == null)
		{
			singleton = new PageCacheHelper();
			new Thread(singleton).start();
		}
		return singleton;
	}
	
	private PageCacheHelper() {}

	public synchronized void run()
	{
		logger.info("Starting PageCacheHelper thread....");
		
		boolean run = true;
		while(run)
		{
			try
			{
				Thread.sleep(10000);
				
				List<String> localPageCacheEvicitionQueue = new ArrayList<String>();
				synchronized (pageCacheEvicitionQueue) 
				{
					localPageCacheEvicitionQueue.addAll(pageCacheEvicitionQueue);
					pageCacheEvicitionQueue.clear();
				}
				
				if(localPageCacheEvicitionQueue.size() > 0)
				{
					logger.info("Clearing page cache for");
					for(String entity : localPageCacheEvicitionQueue)
					{
						logger.info("entity:" + entity);
					}
					
					List<String> existingPageKeysForEntities = getMatchingPageKeysForGroups(localPageCacheEvicitionQueue);
					logger.info("existingPageKeysForEntities:" + existingPageKeysForEntities.size());
					for(String pageKey : existingPageKeysForEntities)
					{
						logger.info("Remove pageKey:" + pageKey);
						clearPageCache(pageKey);
						synchronized (pageKey) 
						{
							logger.info("pageKey in disabled:" + pageKey);
							disabledPages.remove(pageKey);
						}
					}
				}

				List<String> localFileDeletionQueue = new ArrayList<String>();
				synchronized (fileDeletionQueue) 
				{
					localFileDeletionQueue.addAll(fileDeletionQueue);
					fileDeletionQueue.clear();
				}
				
				if(localFileDeletionQueue.size() > 0)
				{
					logger.info("Deleting queued files");
					for(String pageKey : localFileDeletionQueue)
					{
						try
						{
							logger.info("Remove file:" + pageKey);
							clearPageCache(pageKey);
						}
						catch (Exception e) 
						{
							logger.error("Error deleting queued file:" + e.getMessage(), e);
						}
					}
				}

			}
			catch (Exception e) 
			{
				logger.error("Error in PageCacheHelper:" + e.getMessage(), e);
			}
		}
	}

	public void notify(String entityString)
	{
		Timer t = new Timer();
		//System.out.println("notify:" + entityString);
		//Thread.dumpStack();
		if(CmsPropertyHandler.getOperatingMode().equals("0"))
		{
			//System.out.println("This is working mode: let's clear fully.");
			CacheController.clearCache("pageCacheExtra");
			CacheController.clearCache("pageCache");
			
			/*
			logger.info("Forcing clear for: " + entityString);

			try
			{
				List<String> localPageCacheEvicitionQueue = new ArrayList<String>();
				localPageCacheEvicitionQueue.add(entityString);
		    	List<String> existingPageKeysForEntities = getMatchingPageKeysForGroups(localPageCacheEvicitionQueue);
				//System.out.println("existingPageKeysForEntities:" + existingPageKeysForEntities.size());
				for(String pageKey : existingPageKeysForEntities)
				{
					logger.info("Disable pageKey:" + pageKey);
					synchronized (pageKey) 
					{
						disabledPages.put(pageKey, new Date());
					}
	    			PageCacheHelper.getInstance().notifyKey(""+pageKey);
				}
			}
			catch (Exception e) 
			{
				logger.error("Error in notify:" + e.getMessage());
			}
			*/
			long elapsedTime = t.getElapsedTime();
			if(elapsedTime > 20)
				logger.warn("Notify took " + elapsedTime);
		}
		else
		{
			synchronized (pageCacheEvicitionQueue) 
			{
				this.pageCacheEvicitionQueue.add(entityString);
			}
		}
	}

	public void notify(String[] entities)
	{
		synchronized (pageCacheEvicitionQueue) 
		{
			this.pageCacheEvicitionQueue.addAll(Arrays.asList(entities));
		}
	}

	public void notifyKey(String pageKey)
	{
		synchronized (fileDeletionQueue) 
		{
			this.fileDeletionQueue.add(pageKey);
		}
	}

    public String getCachedPageString(String key)
    {
    	synchronized (disabledPages) 
    	{
    		//System.out.println("Checking for key in getPageString:" + key);
    		if(disabledPages.get(key) != null)
    		{
    			logger.info("Disabled as it's going to be removed:" + disabledPages.get(key));
    			if((System.currentTimeMillis() - disabledPages.get(key).getTime() > 60000))
    			{
    				logger.warn("This key has been here over 30 seconds. Let's remove it");
    				disabledPages.remove(key);
    				deleteFile(key);
    			}
    			return null;
    		}
		}
    	
    	Timer t = new Timer();
    	if(!logger.isInfoEnabled())
    		t.setActive(false);

    	String contents = null;
    	try
    	{
    		String checksum = getChecksum(key).toString();
    		String firstPart = checksum.substring(0, 3);
            String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + getPageCacheName() + File.separator + firstPart + File.separator + checksum;
            File file = new File(filePath);
            if(file.exists())
            {
        		byte[] cachedCompressedData = FileHelper.getFileBytes(file);
            	if(cachedCompressedData != null && cachedCompressedData.length > 0)
            		contents = compressionHelper.decompress(cachedCompressedData);		
            }
            else
            {
            	System.out.println("No caches page found");
            	if(logger.isInfoEnabled())
        			logger.info("No filecache existed:" + filePath);
            }
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem loading data from file:" + e.getMessage());
    	}
    	
    	return contents;
    }
    
    public String getCachedPageString(String key, File file)
    {
    	synchronized (disabledPages) 
    	{
    		logger.info("Checking for key in getPageString:" + key);
    		if(disabledPages.get(key) != null)
    		{
    			logger.info("Disabled as it's going to be removed:" + disabledPages.get(key));
    			if((System.currentTimeMillis() - disabledPages.get(key).getTime() > 30000))
    			{
    				logger.warn("This key has been here over 30 seconds. Let's remove it");
    				disabledPages.remove(key);
    				deleteFile(key);
    			}
    			return null;
    		}
		}

    	//System.out.println("file:" + file.getPath());
    	Timer t = new Timer();
    	if(!logger.isInfoEnabled())
    		t.setActive(false);

    	String contents = null;
    	try
    	{
    		if(file.exists())
            {
        		byte[] cachedCompressedData = FileHelper.getFileBytes(file);
            	if(cachedCompressedData != null && cachedCompressedData.length > 0)
            		contents = compressionHelper.decompress(cachedCompressedData);		
            }
            else
            {
            	if(logger.isInfoEnabled())
        			logger.info("No filecache existed:" + file.getPath());
            }
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem loading data from file:" + e.getMessage());
    	}
    	
    	return contents;
    }    

	public void cachePageString(String key, byte[] value /*String valueString*/)
	{
    	try
    	{
    		//byte[] value = compressionHelper.compress(valueString);
    		
    		String checksum = getChecksum(key).toString();
    		String firstPart = checksum.substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + getPageCacheName() + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + checksum);
            File tmpOutputFile = new File(dir + File.separator + Thread.currentThread().getId() + "_tmp_" + checksum);
            
    		FileHelper.writeToFile(tmpOutputFile, value);
    		
			CacheController.cacheObjectInAdvancedCache("pageCache", key, file.getPath());
    		
            if(tmpOutputFile.exists())
			{
				if(tmpOutputFile.length() == 0)
				{
					tmpOutputFile.delete();
				}
				else
				{
					if(logger.isInfoEnabled())
		    			logger.info("Renaming file " + tmpOutputFile.getAbsolutePath() + " to " + file.getAbsolutePath());
					if(logger.isInfoEnabled())
		    			logger.info("file:" + file.exists() + ":" + file.length());
					if(file.exists())
					{
						file.delete();
						numberOfPageCacheFiles.decrementAndGet();
					}
					
					boolean renamed = tmpOutputFile.renameTo(file);
					if(logger.isInfoEnabled())
		    			logger.info("renamed:" + renamed + " to " + file.getPath());
					
					numberOfPageCacheFiles.incrementAndGet();
				}	
			}
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
	}
/*
	public void disablePageCache(String key)
    {        
    	CacheController.clearCache("pageCache", key);
    	CacheController.clearCache("pageCacheExtra", key);
    	CacheController.clearCache("pageCacheExtra", key + "_pageCacheTimeout");
    	CacheController.clearCache("pageCacheExtra", key + "_entitiesAsByte");
    }
*/
	
	private void clearPageCache(String key)
    {        
		CacheController.clearCacheHard("pageCache", key);
    	CacheController.clearCacheHard("pageCacheExtra", key);
    	CacheController.clearCacheHard("pageCacheExtra", key + "_pageCacheTimeout");
    	CacheController.clearCacheHard("pageCacheExtra", key + "_entitiesAsByte");

    	deleteFile(key);
    }

	private void deleteFile(String key) 
	{
		try
    	{
    		String checksum = getChecksum(key).toString();
    		String firstPart = checksum.substring(0, 3);
            String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches" + File.separator + getPageCacheName() + File.separator + firstPart;
            File dirFile = new File(dir);
            dirFile.mkdirs();
            File file = new File(dir + File.separator + checksum);
         	if(logger.isInfoEnabled())
            	logger.info("Deleting " + file.getPath() + ":" + file.exists());
            if(file.exists())
            {
            	boolean deleted = file.delete();
            	if(logger.isInfoEnabled())
            		logger.info("Deleted: " + deleted);
            }
    	}
    	catch (Exception e) 
    	{
    		logger.warn("Problem storing data to file:" + e.getMessage());
		}
	}
    
	public void clearPageCache()
	{
        String dir = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "caches";
        File dirFile = new File(dir);
        //System.out.println("dirFile:" + dirFile.exists());
        if(dirFile.exists())
        {
            File[] subCaches = dirFile.listFiles();
            for(int i=0; i<subCaches.length; i++)
            {
            	File subCacheDir = subCaches[i];
            	logger.info("subCacheDir:" + subCacheDir.getName());
            	if(subCacheDir.isDirectory() && subCacheDir.getName().equals(getPageCacheName()))
            	{
            		logger.info("clearing:" + subCacheDir.getName());
            		try
            		{
	                	File[] subSubCacheFiles = subCacheDir.listFiles();
	                	for(int j=0; j<subSubCacheFiles.length; j++)
	                	{
	                		File subSubCacheDir = subSubCacheFiles[j];
	                    	if(subSubCacheDir.isDirectory())
	                    	{
	                        	File[] cacheFiles = subSubCacheDir.listFiles();
	                        	if(cacheFiles != null)
	                        	{
		                        	for(int k=0; k<cacheFiles.length; k++)
		                        	{
		                        		File cacheFile = cacheFiles[k];
		                        		//System.out.println("cacheFile:" + cacheFile.getName());
		                    			cacheFile.delete();
		                        	}
	                        	}
	                        	
	                    		subCacheDir.delete();
	                    	}			                
	
	                		//System.out.println("cacheFile:" + cacheFile.getName());
	                    	subSubCacheDir.delete();
	                	}
	            		subCacheDir.delete();

            		}
            		catch (Exception e) 
            		{
            			logger.warn("It seems the cache dir: " + getPageCacheName() + " was allready empty or removed. Error: " + e.getMessage());
					}
            	}
            }
        }
	}
	
	private List<String> getMatchingPageKeysForGroups(List<String> entities) throws Exception
	{
		if(entities.size() == 0)
			System.out.println("Why is it 0");
		//System.out.println("entities:" + entities.size());
		Timer t = new Timer();
		Timer t2 = new Timer();
	
		List<String> matchingPageKeysForGroups = new ArrayList<String>();
		
		Map<String,Object> cachedValuesCopy = CacheController.getCachedObjectsFromAdvancedCacheFilteredOnKeyEnd("pageCacheExtra", "_entitiesAsByte");
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMatchingPageKeysForGroups.getCachedObjectsFromAdvancedCacheFilteredOnKeyEnd (now milli)", (t2.getElapsedTimeNanos() / 1000));
		
		//System.out.println("cachedValuesCopy:" + cachedValuesCopy.size());
		
		for(String key : cachedValuesCopy.keySet())
		{
			//System.out.println("Key:" + key);
			String value = null;
			if(CmsPropertyHandler.getOperatingMode().equals("0"))
			{
				value = (String)cachedValuesCopy.get(key);
			}
			else
			{
				byte[] byteValue = (byte[])cachedValuesCopy.get(key);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMatchingPageKeysForGroups.cachedValuesCopy (now milli)", (t2.getElapsedTimeNanos() / 1000));
				value = compressionHelper.decompress(byteValue);
				RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMatchingPageKeysForGroups.decompress (now milli)", (t2.getElapsedTimeNanos() / 1000));
			}
			
			if(entities.size() > 5)
				System.out.println("entities: " + entities.size());
			for(String matchToTest : entities)
			{
				//System.out.println("value:" + value);
				if(value.indexOf(matchToTest) > -1)
				{
					//matchingPageKeysForGroups.add(key.replaceAll("_entitiesAsByte", ""));
					matchingPageKeysForGroups.add(key.substring(0,key.indexOf("_entitiesAsByte")));
					break;
				}
			}
			RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMatchingPageKeysForGroups.matchToTest (now milli)", (t2.getElapsedTimeNanos() / 1000));
		}
		
		long elapsedTime = t.getElapsedTimeNanos();
		if(elapsedTime / 1000 / 1000 > 20)
			logger.warn("Found " + matchingPageKeysForGroups.size() + " pages. Matching pages took " + elapsedTime);
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMatchingPageKeysForGroups (now milli)", (elapsedTime / 1000));
		
		return matchingPageKeysForGroups;
	}

	public void clearPageCacheInThread(String pageKey) 
	{
		logger.warn("Clearing file cache for pageKey:" + pageKey);
		class ClearPageCacheTask implements Runnable 
		{
	        String pageKey;
	        ClearPageCacheTask(String pageKey) { this.pageKey = pageKey; }
	        
	        public void run() 
	        {
	        	Timer t = new Timer();
	    		
	        	logger.info("Precaching all access rights for this user");
				
				try
				{
					clearPageCache(pageKey);
				}
				catch (Exception e) 
				{
					logger.error("Could not start PreCacheTask:" + e.getMessage(), e);
				}
	        }
	    }
	    Thread thread = new Thread(new ClearPageCacheTask(pageKey));
	    thread.start();
	}
	
	private Object getPageCacheName() 
	{
		return "pageCache" + CmsPropertyHandler.getServerName();
	}

	public Long getChecksum(String key) 
	{
		//Timer t = new Timer();
		CRC32 localCRC32Generator = new CRC32();
		localCRC32Generator.update(key.getBytes());
		Long checksum = localCRC32Generator.getValue();
		localCRC32Generator.reset();
		//t.printElapsedTime("CRC32: " + checksum + " took");
		
		return checksum;
	}

}
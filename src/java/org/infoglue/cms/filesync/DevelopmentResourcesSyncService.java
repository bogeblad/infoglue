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


package org.infoglue.cms.filesync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.SmallestContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.util.Timer;


public class DevelopmentResourcesSyncService implements Runnable
{
	private final static Logger logger = Logger.getLogger(DevelopmentResourcesSyncService.class.getName());

    //String basePath = "/Applications/infoglue/eclipse/workspace/bogeblad";
    public static String basePath = CmsPropertyHandler.getDiskBasedDeploymentBasePath();
    		
	private static DevelopmentResourcesSyncService singleton = null;
	
	private WatchService watcher;
	private Map<WatchKey, Path> watchPathKeyMap;

	private List<Path> ignorePaths;
	
	private AtomicBoolean exit = new AtomicBoolean(false);

    private static ContentVersionControllerProxy contentVersionControllerProxy = ContentVersionControllerProxy.getController();

	public static DevelopmentResourcesSyncService getInstance() throws Exception
	{
		return getInstance(false);
	}

	public static DevelopmentResourcesSyncService getInstance(boolean skipCaches) throws Exception
	{
		if(!CmsPropertyHandler.getEnableDiskBasedDeployment(skipCaches) || CmsPropertyHandler.getDiskBasedDeploymentBasePath().trim().equals(""))
		{
			if(singleton != null)
			{
				logger.info("Setting singleton to null and Closing watchers...");
				singleton.setExit();
				singleton = null;
			}

			logger.info("Not starting disk sync area - just returning a new instance");
			return null;
		}

		String basePathCurrently = CmsPropertyHandler.getDiskBasedDeploymentBasePath();
		if(!basePathCurrently.equals(basePath))
		{
			logger.info("basePath changed - restart thread..");
			if(singleton != null)
			{
				singleton.setExit();
			}
		}
			
		if(singleton == null)
		{
			singleton = new DevelopmentResourcesSyncService();
			Thread thread = new Thread (singleton);
			thread.start();
		}
		
		return singleton;
	}

	public DevelopmentResourcesSyncService() throws Exception
	{
		watcher = FileSystems.getDefault().newWatchService();
		watchPathKeyMap = new HashMap<WatchKey, Path>();
	}
	
	public synchronized void run()
	{
		logger.info("Running DevelopmentResourcesSyncService...");
		
		try
		{
			
			Path dirContent = Paths.get(basePath + "/content");
			walkTreeAndSetWatches(dirContent);

			Path dirConfig = Paths.get(basePath + "/configuration");
			walkTreeAndSetWatches(dirConfig);

			for (;;) {
				
				if(!CmsPropertyHandler.getEnableDiskBasedDeployment() || CmsPropertyHandler.getDiskBasedDeploymentBasePath().trim().equals(""))
				{
					logger.info("Exiting thread");
					watcher.close();
					break;
				}
				
			    // wait for key to be signaled
			    WatchKey key;
			    try 
			    {
			        key = watcher.take();
			    } 
			    catch (InterruptedException x) 
			    {
			        return;
			    }
			    
			    Path dir = watchPathKeyMap.get(key);
			    
			    for (WatchEvent<?> event: key.pollEvents()) 
			    {
			        WatchEvent.Kind<?> kind = event.kind();
	
			        if (kind == StandardWatchEventKinds.OVERFLOW) 
			        {
			            continue;
			        }
	
			        WatchEvent<Path> ev = (WatchEvent<Path>)event;
			        Path filename = ev.context();
	
		            logger.info("child: " + filename);
		            Path child = dir.resolve(filename);
		            logger.info("child: " + child);
		            processEvent(child, kind);
			    }
	
			    boolean valid = key.reset();
			    if (!valid) 
			    {
			        break;
			    }
			}
		}
		catch(Exception e)
		{
			logger.error("Could not monitor file system: " + e.getMessage(), e);
			return;
		}
	}
	
	
	private synchronized void walkTreeAndSetWatches(Path root) 
	{
		try 
		{
			Files.walkFileTree(root, new FileVisitor<Path>() 
			{
			
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException 
				{
					if (ignorePaths != null && ignorePaths.contains(dir)) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					else {
						logger.info("Registering watcher on: " + dir);
						logger.info("watcher: " + watcher);
						WatchKey keyContent = dir.register(watcher,
								StandardWatchEventKinds.ENTRY_CREATE,
								StandardWatchEventKinds.ENTRY_DELETE,
								StandardWatchEventKinds.ENTRY_MODIFY);
						
						watchPathKeyMap.put(keyContent, dir);
						
						return FileVisitResult.CONTINUE;
					}
				}


				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file,
						IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e) {
			// Don't care
		}
	}
	
	private void processEvent(Path path, WatchEvent.Kind kind) throws Bug, ConstraintException, Exception
	{
		String relativePath = getRelativePath(path);
		logger.info("relativePath: " + relativePath + " - kind: " + kind + "/" + kind.name());

		if(path.toFile().isDirectory())
		{
			watchAndHandleNewDirectory(path);
		}
		else
		{
			if(kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE)
			{
				if(relativePath.startsWith("content"))
				{
					handleContentFileChange(path, relativePath);
				}
				else if(relativePath.startsWith("configuration"))
				{
					handleConfigurationFileChange(path, relativePath);
				}
			}
		}
	}

	private String getRelativePath(Path path) 
	{
		String relativePath = path.toAbsolutePath().toString().replaceFirst(basePath, "");
		if(relativePath.startsWith(File.separator))
			relativePath = relativePath.substring(1);
		
		return relativePath;
	}

	private void handleContentFileChange(Path path, String relativePath) throws SystemException, Bug, ConstraintException, Exception 
	{
		//System.out.println("path:" + path);
		String repoName = relativePath.replaceFirst("content", "");
		String[] splitted = repoName.split(""+File.separator);
		logger.info("splitted 0:" + splitted[0]);
		logger.info("splitted 1:" + splitted[1]);
		logger.info("splitted 2:" + splitted[2]);
		String repoNameFromPath = splitted[1];
		logger.info("relativePath:" + relativePath);
		//System.out.println("repoName:" + repoName);
		String pathLeft = relativePath.replaceFirst(repoNameFromPath, "");
		logger.info("pathLeft:" + pathLeft);
		pathLeft = pathLeft.replaceFirst("content" + File.separator, "");
		logger.info("pathLeft:" + pathLeft);
		RepositoryVO repoVO = RepositoryController.getController().getRepositoryVOWithName(repoNameFromPath);
		logger.info("repoVO:" + repoVO);
		
		pathLeft = pathLeft.substring(0, pathLeft.lastIndexOf(File.separator));
		logger.info("pathLeft:" + pathLeft);
		
		if(pathLeft.endsWith("Assets"))
			pathLeft = pathLeft.replaceFirst("/Assets", "");
		logger.info("pathLeft:" + pathLeft);
		
		InfoGluePrincipal principal = UserControllerProxy.getController().getUser("Administrator");
		
		//System.out.println("repoName:" + repoName);
		//System.out.println("pathLeft:" + pathLeft);

		ContentVO contentVO = ContentController.getContentController().getContentVOWithPath(repoVO.getId(), pathLeft, true, principal);
		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());

		//Now lets look at versions
		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId());

		ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("HTMLTemplate");
		if(contentVersionVO == null)
		{
			contentVO.setIsBranch(false);
			ContentController.getContentController().update(contentVO, ctdVO.getId());
		}
		logger.info("contentVO" + contentVO);
				    
		if(contentVersionVO != null)
		{
			try
			{
				logger.info("contentVersionVO:" + contentVersionVO);
				contentVersionVO = ContentStateController.changeState(contentVersionVO.getId(), ContentVersionVO.WORKING_STATE, "Remote update from deliver", false, principal, contentVersionVO.getContentId(), new ArrayList<EventVO>());
			}
			catch (Exception e)
			{
				logger.error("Error when changing state to working: " + e.getMessage(), e);
			}
			if (logger.isInfoEnabled())
			{
				logger.info("contentVersionVO (after state change):" + contentVersionVO);
			}
		}
		boolean isNewlyCreatedVersion;
		if(contentVersionVO == null)
		{
			ContentVersionVO newContentVersionVO = new ContentVersionVO();
			newContentVersionVO.setVersionComment("New version created by filesync deploy");
			newContentVersionVO.setModifiedDateTime(new Date());
			newContentVersionVO.setVersionModifier("" + principal.getName());

		    contentVersionVO = contentVersionControllerProxy.acCreate(principal, contentVO.getId(), masterLanguageVO.getId(), newContentVersionVO);            		
			isNewlyCreatedVersion = true;
			logger.info("contentVersionVO (newly created):" + contentVersionVO);
		}
		else
		{
		    contentVersionVO.setVersionComment("Version updated by filesync deploy");
		    contentVersionVO.setModifiedDateTime(new Date());
		    contentVersionVO.setVersionModifier("" + principal.getName());
			isNewlyCreatedVersion = false;
		}

		logger.info("Parent name:" + path.getParent().toFile().getName());
		if(path.getParent().toFile().getName().equals("Assets"))
		{
			logger.info("This is an asset: " + path.getParent().toFile().getName());
			String assetKey = FilenameUtils.removeExtension(path.getFileName().toString());
			DigitalAssetVO assetVO = DigitalAssetController.getController().getDigitalAssetVO(contentVO.getId(), masterLanguageVO.getId(), assetKey, false);
			logger.info("assetVO:" + assetVO);
			if(assetVO == null)
			{
				assetVO = new DigitalAssetVO();
				assetVO.setAssetContentType("image/png");
				assetVO.setAssetKey(assetKey);
				assetVO.setAssetFileName(path.toFile().getName());
				assetVO.setAssetFilePath(path.toFile().getPath());
				assetVO.setAssetFileSize(new Integer(new Long(path.toFile().length()).intValue()));
				logger.info("Creating asset....:" + contentVersionVO.getId() + ":" + path.toFile().getName() + ":" + assetVO.getAssetKey());

				try
				{
					logger.info("Asset file:" + path.toFile().getPath() + ": " + path.toFile().length());
					InputStream is = new FileInputStream(path.toFile());	

					List<Integer> newContentVersionIdList = new ArrayList<Integer>();
					assetVO = DigitalAssetController.create(assetVO, is, contentVersionVO.getId(), principal, newContentVersionIdList);

					is.close();
				}
				catch(Throwable e)
				{ 
				    logger.error("An error occurred when we tried to close the fileinput stream and delete the file:" + e.getMessage(), e);
				}
			}
		}
		else
		{
			//System.out.println("path:" + path.toFile().getPath() + ":" + path.toFile().length() + ":" + path.toFile().getParentFile().exists() + ":" + path.toFile().getParentFile().getParentFile().exists() + ":" + path.toFile().getParentFile().getParentFile().getParentFile().exists());
			String escaped = path.toString().replace(" ", "\\ ");
			//System.out.println("escaped:" + escaped + ":" + path.toFile().getPath() + ":" + path.toFile().exists());
			//String fileContentTest = FileHelper.getFileAsStringOpt(new File(escaped), "utf-8");
			String fileContent = null;
			try
			{
				fileContent = FileHelper.getFileAsStringOpt(path.toFile(), "utf-8");
			}
			catch(Exception e)
			{
				logger.error("Could not read content in UTF-8 (trying iso-8859-1 next): " + e.getMessage());
				try
				{
					fileContent = FileHelper.getFileAsStringOpt(path.toFile(), "iso-8859-1");	
				}
				catch(Exception e2)
				{
					logger.error("Could not read content in iso-8859-1: " + e2.getMessage());
				}
			}
			
			if(fileContent != null)
			{
				Map<String, String> attributes = new HashMap<String,String>();
				String attributeNameFromFile = FilenameUtils.removeExtension(path.getFileName().toString());
				logger.info("Adding:" + attributeNameFromFile + "=" + fileContent);
				attributes.put(attributeNameFromFile, fileContent);
				
				if(attributes != null && attributes.size() > 0)
				{
					DOMBuilder domBuilder = new DOMBuilder();
		
					Element attributesRoot = null;
					Document document = null;
					
					if (!isNewlyCreatedVersion)
					{
						String existingXML = contentVersionVO.getVersionValue();
						document = domBuilder.getDocument(existingXML);
						attributesRoot = (Element)document.getRootElement().element("attributes");
					}
					else
					{
				        document = domBuilder.createDocument();
				        Element rootElement = domBuilder.addElement(document, "root");
				        domBuilder.addAttribute(rootElement, "xmlns", "x-schema:Schema.xml");
				        attributesRoot = domBuilder.addElement(rootElement, "attributes");
					}
		
					if(logger.isDebugEnabled())
						logger.info("attributesRoot:" + attributesRoot);
					
				    Iterator<String> attributesIterator = attributes.keySet().iterator();
				    while(attributesIterator.hasNext())
				    {
				        String attributeName  = attributesIterator.next();
				        String attributeValue = attributes.get(attributeName);
				        
				        //attributeValue = cleanAttributeValue(attributeValue, allowHTMLContent, allowExternalLinks, allowDollarSigns, allowAnchorSigns);
				        
				    	Element attribute = attributesRoot.element(attributeName);
						if (attribute == null)
						{
							attribute = domBuilder.addElement(attributesRoot, attributeName);
						}
						attribute.clearContent();
						domBuilder.addCDATAElement(attribute, attributeValue);
				    }	                
		
				    contentVersionVO.setVersionValue(document.asXML());
				}
				//System.out.println("Updating template with new version:" + contentVO.getName());
				ContentVersionControllerProxy.getController().acUpdate(principal, contentVO.getId(), masterLanguageVO.getId(), contentVersionVO);

				try
		        {
		        	if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
		        		ChangeNotificationController.getInstance().notifyListeners();
		        }
		        catch(Exception e)
		        {
		        	logger.error("Error notifying listener " + e.getMessage());
		        	logger.warn("Error notifying listener " + e.getMessage(), e);
		        }
			}
			else
				logger.error("Skipped updating due to error");
		}
	}

	
	private void handleConfigurationFileChange(Path path, String relativePath) throws SystemException, Bug, ConstraintException, Exception 
	{
		String repoName = relativePath.replaceFirst("configuration", "");
		String[] splitted = repoName.split(""+File.separator);
		String configAspectFromPath = splitted[1];
		logger.info("relativePath:" + relativePath);
		logger.info("repoName:" + repoName);
		String pathLeft = relativePath.replaceFirst(configAspectFromPath, "");
		logger.info("pathLeft:" + pathLeft);
		pathLeft = pathLeft.replaceFirst("configuration" + File.separator, "");
		logger.info("pathLeft:" + pathLeft);

		logger.info("configAspectFromPath:" + configAspectFromPath);
		if(configAspectFromPath.equalsIgnoreCase("ContentTypeDefinitions"))
		{
			String fileContent = FileHelper.getFileAsStringOpt(path.toFile(), "utf-8"); 
			logger.info("fileContent content type: " + path.toFile().getName());
			ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(FilenameUtils.removeExtension(path.toFile().getName()));
			if(ctdVO != null)
			{
				ctdVO.setSchemaValue(fileContent);
				logger.info("Set schema value to: " + fileContent + " - Name: " + FilenameUtils.removeExtension(path.toFile().getName()));
				ContentTypeDefinitionController.getController().update(ctdVO);
			}
			else
			{
				ContentTypeDefinitionVO contentTypeDefinitionVO = new ContentTypeDefinitionVO();
				contentTypeDefinitionVO.setName(FilenameUtils.removeExtension(path.toFile().getName()));
				contentTypeDefinitionVO.setSchemaValue(fileContent);
				logger.info("Create schema value to: " + fileContent + " - Name: " + FilenameUtils.removeExtension(path.toFile().getName()));
				ContentTypeDefinitionController.getController().create(contentTypeDefinitionVO);
			}
		}
		else if(configAspectFromPath.equalsIgnoreCase("Portlets"))
		{
			//TBD
		}
		else if(configAspectFromPath.equalsIgnoreCase("InterceptionPoints"))
		{
			//TBD
		}
		else if(configAspectFromPath.equalsIgnoreCase("Interceptors"))
		{
			//TBD
		}
		else if(configAspectFromPath.equalsIgnoreCase("Categories"))
		{
			//TBD
		}
		else if(configAspectFromPath.equalsIgnoreCase("Languages"))
		{
			//TBD
		}
		else if(configAspectFromPath.equalsIgnoreCase("Repositories"))
		{
			//TBD
		}
	}
	
	
	private void watchAndHandleNewDirectory(Path path) throws SystemException, Bug, ConstraintException, Exception 
	{
		logger.info("watchAndHandleNewDirectory path:" + path);
		if(!watchPathKeyMap.containsValue(path))
		{
			logger.info("What to do with a dir????? - registering it!!");
			WatchKey keyContent = path.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			
			watchPathKeyMap.put(keyContent, path);
			
			for(File file : path.toFile().listFiles())
			{
				//logger.info("file:" + file);
				if(file.getName().startsWith("."))
				{
					logger.info("Skipping files with . as start:" + file);				
					continue;
				}
				
				if(!file.isDirectory())
				{
					String relativePath = getRelativePath(file.toPath());
					logger.info("Handling new file: " + relativePath);
					handleContentFileChange(file.toPath(), relativePath);
				}
			}
		}
		else
			logger.info("Allready a listener on path: " + path);
		
		for(File file : path.toFile().listFiles())
		{
			//logger.info("file:" + file);
			if(file.getName().startsWith("."))
			{
				logger.info("Skipping files with . as start:" + file);				
				continue;
			}
			
			if(file.isDirectory())
			{
				watchAndHandleNewDirectory(file.toPath());
			}
		}
	}
	
	private static List<Integer> componentVersionIdsToSync = new ArrayList<Integer>();

	public void writeChangesToDiskDelayed(Integer contentVersionId) throws Exception
	{
		boolean loop = false;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stack)
		{
			if(element.getClassName().contains("DevelopmentResourcesSyncService"))
			{
				if(loop == true)
				{
					logger.info("Was looping - skip it");
					return;
				}
				else
					loop = true;
			}
		}
		
		logger.info("contentVersionId:" + contentVersionId);
		componentVersionIdsToSync.add(contentVersionId);
		class WriteChangesToDiskTask implements Runnable 
		{
	        public void run() 
	        {
	        	try
	        	{
	        		logger.info("sleep:" + componentVersionIdsToSync);
	        		
	        		Thread.sleep(1000);
		        	Timer t = new Timer();
		        	Set<Integer> localComponentVersionIdsToSync = new HashSet<Integer>();
		        	localComponentVersionIdsToSync.addAll(componentVersionIdsToSync);
		        	componentVersionIdsToSync.clear();
		        	for(Integer localComponentVersionId : localComponentVersionIdsToSync)
		        	{
		        		writeChangesToDisk(localComponentVersionId);
		        	}
		        	long time = t.getElapsedTime();
		        	if(time > 100)
		        		logger.warn("writeChangesToDiskDelayed took a bit to long:" + time);
	        	}
	        	catch (Exception e) 
	        	{
					logger.warn("Failed to sync changes to disc: " + e.getMessage(), e);
				}
	        }
	    }
	    Thread thread = new Thread(new WriteChangesToDiskTask());
	    thread.start();
	}
	
	public void writeChangesToDisk(Integer contentVersionId) throws Exception
	{
		logger.info("contentVersionId:" + contentVersionId);
    	ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId);
		logger.info("contentVersionVO:" + contentVersionVO);
		
    	if(contentVersionVO != null)
    	{
    		try
    		{
		    	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
		    	logger.info("contentVO:" + contentVO);
				
		    	ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
		    	logger.info("ctdVO:" + ctdVO);
				List<ContentTypeAttribute> attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(ctdVO.getSchemaValue());
				logger.info("attributes:" + attributes);
				
		    	for(ContentTypeAttribute attribute : attributes)
		    	{
		    		String attributeValue = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attribute.getName(), false);
		    		logger.info("attributeValue:" + attributeValue);
		    		if(attributeValue.length() > 0)
		    		{
			    		RepositoryVO repoVO = RepositoryController.getController().getRepositoryVOWithId(contentVO.getRepositoryId());
			    		String path = basePath + File.separator + "content" + File.separator + repoVO.getName();
			    		String addition = contentVO.getName() + File.separator + attribute.getName();
			    		if(attribute.getName().equals("Template"))
			    			addition = addition + ".jsp";
			    		else if(attribute.getName().equals("ComponentLabels"))
			    			addition = addition + ".properties";
			    		else if(attribute.getName().equals("ComponentProperties"))
			    			addition = addition + ".xml";
			    		else if(attribute.getName().equals("Name"))
			    			addition = addition + ".txt";
			    		else
			    			addition = addition + ".txt";
			    		
			    		logger.info("path:" + path);
			    		ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
			    		while(parentContentVO != null)
			    		{
			    			addition = parentContentVO.getName() + File.separator + addition; 
			    			parentContentVO = ContentController.getContentController().getContentVOWithId(parentContentVO.getParentContentId());
			    			if(parentContentVO.getParentContentId() == null || parentContentVO.getParentContentId() == -1)
			    				break;
			    		}
			    		logger.info("FullPath: " + path + File.separator + addition);
			    		File file = new File(path + File.separator + addition);
			    		file.getParentFile().mkdirs();
			    		FileHelper.writeToFile(file, attributeValue, false);
		    		}
		    	}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
	}
	
	
	private static List<Integer> assetIdsToSync = new ArrayList<Integer>();

	public void writeAssetChangesToDiskDelayed(Integer assetId) throws Exception
	{
		boolean loop = false;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stack)
		{
			if(element.getClassName().contains("DevelopmentResourcesSyncService"))
			{
				if(loop == true)
				{
					logger.info("Was looping - skip it");
					return;
				}
				else
					loop = true;
			}
		}
		
		logger.info("assetId:" + assetId);
		assetIdsToSync.add(assetId);
		class WriteChangesToDiskTask implements Runnable 
		{
	        public void run() 
	        {
	        	try
	        	{
	        		logger.info("sleep:" + componentVersionIdsToSync);
	        		
	        		Thread.sleep(1000);
		        	Timer t = new Timer();
		        	Set<Integer> localAssetIdsToSync = new HashSet<Integer>();
		        	localAssetIdsToSync.addAll(assetIdsToSync);
		        	assetIdsToSync.clear();
		        	for(Integer localAssetId : localAssetIdsToSync)
		        	{
		        		writeAssetChangesToDisk(localAssetId);
		        	}
		        	long time = t.getElapsedTime();
		        	if(time > 100)
		        		logger.warn("writeAssetChangesToDiskDelayed took a bit to long:" + time);
	        	}
	        	catch (Exception e) 
	        	{
					logger.warn("Failed to sync changes to disc: " + e.getMessage(), e);
				}
	        }
	    }
	    Thread thread = new Thread(new WriteChangesToDiskTask());
	    thread.start();
	}
	
	public void writeAssetChangesToDisk(Integer digitalAssetId) throws Exception
	{
		logger.info("digitalAssetId:" + digitalAssetId);
		DigitalAssetVO assetVO = DigitalAssetController.getDigitalAssetVOWithId(digitalAssetId);
		logger.info("assetVO:" + assetVO.getAssetKey());
		
		List<SmallestContentVersionVO> contentVersionVOList = DigitalAssetController.getController().getContentVersionVOListConnectedToAssetWithId(digitalAssetId);
    	for(SmallestContentVersionVO contentVersionVO : contentVersionVOList)
    	{
			logger.info("contentVersionVO:" + contentVersionVO);
    		try
    		{
		    	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
		    	logger.info("contentVO:" + contentVO);
				
    			RepositoryVO repoVO = RepositoryController.getController().getRepositoryVOWithId(contentVO.getRepositoryId());
    			String fileName = assetVO.getAssetKey() + assetVO.getAssetFileName().substring(assetVO.getAssetFileName().lastIndexOf("."));
	    		String addition = contentVO.getName() + File.separator + "Assets";
	    		String path = basePath + File.separator + "content" + File.separator + repoVO.getName();
	    		
	    		logger.info("path:" + path);
	    		ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
	    		while(parentContentVO != null)
	    		{
	    			addition = parentContentVO.getName() + File.separator + addition; 
	    			if(parentContentVO.getParentContentId() == null)
	    			{
	    				System.out.println("Stopping at addition: " + addition);
	    				break;
	    			}
	    			else
	    			{
		    			parentContentVO = ContentController.getContentController().getContentVOWithId(parentContentVO.getParentContentId());
		    			if(parentContentVO.getParentContentId() == null || parentContentVO.getParentContentId() == -1)
		    				break;
	    			}
	    		}
	    		logger.info("FullPath: " + path + File.separator + addition);
	    		File file = new File(path + File.separator + addition);
	    		file.mkdirs();
	    		
	    		Database db = CastorDatabaseService.getDatabase();
	    		try
	    		{
	    			db.begin();
	    			
	    			DigitalAssetController.dumpDigitalAsset(assetVO, fileName, path + File.separator + addition, db);
	    		
	    			db.commit();
	    		}
	    		catch(Exception e)
	    		{
	    			db.rollback();
	    		}
	    		finally
	    		{
	    			db.close();
	    		}
	    	}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
	}

	public void setExit() 
	{
		this.exit.set(true);
		try 
		{
			logger.info("Closing watchers...");
			watcher.close();
			singleton = null;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}

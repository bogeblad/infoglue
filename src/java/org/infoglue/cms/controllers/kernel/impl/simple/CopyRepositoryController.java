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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.managementtool.actions.ImportRepositoryAction;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
* This class handles Importing copying of a repository - by processing it as a thread in a process bean.
* 
* @author Mattias Bogeblad
*/

public class CopyRepositoryController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(CopyRepositoryController.class.getName());

    private String onlyLatestVersions;
    private String standardReplacement;
    private String replacements;
    private InfoGluePrincipal principal;
    private ProcessBean processBean;
    private String[] repositoryIds;
    
	private VisualFormatter visualFormatter = new VisualFormatter();
	
	private CopyRepositoryController(String[] repositoryIds, InfoGluePrincipal principal, String onlyLatestVersions, String standardReplacement, String replacements, ProcessBean processBean)
	{
		this.principal = principal;
		this.repositoryIds = repositoryIds;
		this.onlyLatestVersions = onlyLatestVersions;
		this.standardReplacement = standardReplacement;
		this.replacements = replacements;
		this.processBean = processBean;
	}
	
	/**
	 * Factory method to get object
	 */
	
	public static void importRepositories(String[] repositoryIds, InfoGluePrincipal principal, String onlyLatestVersions, String standardReplacement, String replacements, ProcessBean processBean) throws Exception
	{
		CopyRepositoryController copyController = new CopyRepositoryController(repositoryIds, principal, onlyLatestVersions, standardReplacement, replacements, processBean);
		Thread thread = new Thread(copyController);
		thread.start();
	}
	   	
	public synchronized void run()
	{
		logger.info("Starting Copy Thread....");
		try
		{
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				Map<String,String> replaceMap = new HashMap<String,String>();
				
				String expectNormalString = CmsPropertyHandler.getExpectFormPostToBeUnicodeAllready();
				
				if(expectNormalString.equals("true"))
				{
					Properties properties = new Properties();
					try
					{
						properties.load(new StringReader(replacements));
						Iterator propertySetIterator = properties.keySet().iterator();
						while(propertySetIterator.hasNext())
						{
							String key = (String)propertySetIterator.next();
							String value = properties.getProperty(key);
							replaceMap.put(key, value);
						}
					}	
					catch(Exception e)
					{
					    logger.error("Error loading properties from string. Reason:" + e.getMessage());
					}
				}
				else
				{
					try
					{
						boolean isUTF8 = false;
						boolean hasUnicodeChars = false;
						if(replacements.indexOf((char)65533) > -1)
							isUTF8 = true;
						
						for(int i=0; i<replacements.length(); i++)
						{
							int c = (int)replacements.charAt(i);
							if(c > 255 && c < 65533)
								hasUnicodeChars = true;
						}
	
						if(!isUTF8 && !hasUnicodeChars)
						{
							String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
							if(fromEncoding == null)
								fromEncoding = "iso-8859-1";
							
							String toEncoding = CmsPropertyHandler.getUploadToEncoding();
							if(toEncoding == null)
								toEncoding = "utf-8";
							
							if(replacements.indexOf("å") == -1 && 
							   replacements.indexOf("ä") == -1 && 
							   replacements.indexOf("ö") == -1 && 
							   replacements.indexOf("Å") == -1 && 
							   replacements.indexOf("Ä") == -1 && 
							   replacements.indexOf("Ö") == -1)
							{
								replacements = new String(replacements.getBytes(fromEncoding), toEncoding);
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					
					Properties properties = new Properties();
					try
					{
						properties.load(new ByteArrayInputStream(replacements.getBytes("ISO-8859-1")));
						Iterator propertySetIterator = properties.keySet().iterator();
						while(propertySetIterator.hasNext())
						{
							String key = (String)propertySetIterator.next();
							String value = properties.getProperty(key);
							replaceMap.put(key, value);
						}
					}	
					catch(Exception e)
					{
					    logger.error("Error loading properties from string. Reason:" + e.getMessage());
						e.printStackTrace();
					}
				}

				String exportId = "Copy_Repository_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
				ProcessBean processBean = ProcessBean.createProcessBean(ImportRepositoryAction.class.getName(), exportId);
		
				Map args = new HashMap();
			    args.put("globalKey", "infoglue");
			    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
				Collection keys = ps.getKeys();
				logger.info("keys:" + keys.size());
				processBean.updateProcess("Propertyset fetched...");
				
				RepositoryVO repository = RepositoryController.getController().getRepositoryVOWithId(new Integer(repositoryIds[0]));
				
				RepositoryVO repositoryVO = new RepositoryVO();
				repositoryVO.setName(visualFormatter.replaceAccordingToMappings(replaceMap, repository.getName()));
				repositoryVO.setDescription(visualFormatter.replaceAccordingToMappings(replaceMap, repository.getDescription()));
				repositoryVO.setDnsName(visualFormatter.replaceAccordingToMappings(replaceMap, repository.getDnsName()));
		
				RepositoryVO repo = RepositoryController.getController().create(repositoryVO);
				logger.info("repo: " + repo.getId());
				
				List<LanguageVO> languages = LanguageController.getController().getLanguageVOList(repository.getId());
		    	String[] values = new String[languages.size()];
		    	int index = 0;
		    	for(LanguageVO languageVO : languages)
		    	{
		    		values[index] = ""+languageVO.getId();
		    		index++;
		    	}
				RepositoryLanguageController.getController().updateRepositoryLanguages(repo.getId(),values);

				db.begin();

			    SiteNodeStateController.getController().copyAccessRights("Repository", repository.getId(), repo.getId(), replaceMap, db);

				db.commit();
				db.begin();
				
				RepositoryController.getController().copyRepositoryProperties(ps, repository.getId(), repo.getId());
				processBean.updateProcess("Copied repository properties...");

				db.commit();
				db.begin();
				
				SiteNodeController.getController().copyRepository(repository, repo, this.principal, onlyLatestVersions, standardReplacement, replaceMap, processBean, db, ps);
				
				db.commit();
				processBean.updateProcess("Creating index registry...");
				Thread.sleep(1000);

				db.begin();
				RegistryController.getController().rebuildRepositoryRegistry(db, repo.getId());
				db.commit();

				processBean.setStatus(processBean.FINISHED);
			} 
			catch ( Exception e) 
			{
				try
		        {
		            db.rollback();
		            db.close();
		        } 
				catch (Exception e1)
		        {
		            logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
					throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
		        }
				
				logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
				throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
			}
		}
		catch (Exception e) 
		{
			//TODO: Fix this error message better. Support illegal xml-chars
			processBean.setError("Something went wrong with the import. Please consult the logfiles.");
			logger.error("Error in monitor:" + e.getMessage(), e);
		}
	}
	
    public BaseEntityVO getNewVO()
    {
        return null;
    }

}

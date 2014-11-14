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

	public synchronized void run()
	{
		logger.info("Starting Copy Thread....");
		try
		{
			Database db = CastorDatabaseService.getDatabase();

			try 
			{
				Map<String,String> replaceMap = new HashMap<String,String>();
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

				String exportId = "Copy_Repository_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
				ProcessBean processBean = ProcessBean.createProcessBean(ImportRepositoryAction.class.getName(), exportId);
		
				RepositoryVO repository = RepositoryController.getController().getRepositoryVOWithId(new Integer(repositoryIds[0]));
				
				RepositoryVO repositoryVO = new RepositoryVO();
				repositoryVO.setName(repository.getName() + " Copy " + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm"));
				repositoryVO.setDescription(repository.getDescription());
				repositoryVO.setDnsName(repository.getDnsName());
		
				RepositoryVO repo = RepositoryController.getController().create(repositoryVO);
				logger.info("repo: " + repo.getId());
				
				List<LanguageVO> languages = RepositoryLanguageController.getController().getLanguageVOListForRepositoryId(repository.getId());
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
				
				SiteNodeController.getController().copyRepository(repository, repo, this.principal, onlyLatestVersions, standardReplacement, replaceMap, processBean, db);
				
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
	   	
    public BaseEntityVO getNewVO()
    {
        return null;
    }

}

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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;


/**
 * This class handles Exporting of a repository to an XML-file.
 * 
 * @author mattias
 */

public class RebuildRegistryAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(RebuildRegistryAction.class.getName());

	private Integer repositoryId = null;
	
	private String fileUrl 	= "";
	private String fileName = "";

	/**
	 * This shows the dialog before export.
	 * @return
	 * @throws Exception
	 */	

	public String doInput() throws Exception
	{
		return "input";
	}
	
	/**
	 * This handles the actual exporting.
	 */
	
	protected String doExecute() throws Exception 
	{
	    RegistryController registryController = RegistryController.getController();
		
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			db.begin();

			
			//Checks the relations from sitenodes
			List siteNodes = SiteNodeController.getController().getRepositorySiteNodes(this.repositoryId, db);
			
			Iterator siteNodesIterator = siteNodes.iterator();
			while(siteNodesIterator.hasNext())
			{
			    SiteNode siteNode = (SiteNode)siteNodesIterator.next();
			    logger.info("Going to index all versions of " + siteNode.getName());
			    
			    Iterator siteNodeVersionsIterator = siteNode.getSiteNodeVersions().iterator();
				while(siteNodeVersionsIterator.hasNext())
				{
				    SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionsIterator.next();
				    registryController.updateSiteNodeVersion(siteNodeVersion.getValueObject(), db);
				}
			}

			//Checks the relations from contents
			List contents = ContentController.getContentController().getRepositoryContents(this.repositoryId, db);
			
			Iterator iterator = contents.iterator();
			while(iterator.hasNext())
			{
			    Content content = (Content)iterator.next();
			    logger.info("Going to index all version of " + content.getName());
			    
			    Iterator versionsIterator = content.getContentVersions().iterator();
				while(versionsIterator.hasNext())
				{
				    ContentVersion contentVersion = (ContentVersion)versionsIterator.next();
				    registryController.updateContentVersion(contentVersion.getValueObject(), db);
				}
			}
						
			db.commit();
		} 
		catch (Exception e) 
		{
			logger.error("An error was found rebuilding the registry: " + e.getMessage(), e);
			db.rollback();
		}
		finally
		{
			db.close();    
		}
		
		return "success";
	}


	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

}

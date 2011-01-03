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
 * $Id: InitializeCreateNews.java,v 1.8 2008/07/03 11:48:23 mattias Exp $
 */
package org.infoglue.cms.applications.workflowtool.functions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;


/**
 * THIS IS VERY TEMPORARY SOLUTION FOR ASSESSING WHERE TO PUT THE NEWS ITEMS.
 * @version $Revision: 1.8 $ $Date: 2008/07/03 11:48:23 $
 */
public class InitializeCreateNews implements FunctionProvider
{
    private final static Logger logger = Logger.getLogger(InitializeCreateNews.class.getName());

	public void execute(Map transientVars, Map args, PropertySet propertySet)
	{
		try
		{
		    Integer repositoryId 	= new Integer(((String[])transientVars.get("repositoryId"))[0]);
		    Integer parentContentId = new Integer(((String[])transientVars.get("parentContentId"))[0]);
		    Integer languageId 		= new Integer(((String[])transientVars.get("languageId"))[0]);
		    Integer contentTypeDefinitionId = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Article").getContentTypeDefinitionId();
			
		    logger.info("parentContentId:" + parentContentId);
		    logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
		    logger.info("repositoryId:" + repositoryId);
		    logger.info("languageId:" + languageId);

			propertySet.setString("parentContentId", parentContentId.toString());
			propertySet.setString("contentTypeDefinitionId", contentTypeDefinitionId.toString());
			propertySet.setString("repositoryId", repositoryId.toString());
			propertySet.setString("languageId", languageId.toString());
		}
		catch (Exception e)
		{
		    logger.info("An error occurred trying to assess the place where to put it.");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

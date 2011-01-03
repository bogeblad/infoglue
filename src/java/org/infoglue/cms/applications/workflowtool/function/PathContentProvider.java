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
package org.infoglue.cms.applications.workflowtool.function;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.Repository;

import com.opensymphony.workflow.WorkflowException;

/**
 *
 */
public class PathContentProvider extends ContentProvider 
{
    private final static Logger logger = Logger.getLogger(PathContentProvider.class.getName());

	/**
	 * 
	 */
	private static final String CONTENT_PARAMETER_NAME_ARGUMENT = "contentParameterName";
	
	/**
	 * 
	 */
	private static final String CONTENT_VERSION_PARAMETER_NAME_ARGUMENT = "contentVersionParameterName";
	
	/**
	 * The name of the path argument.
	 */
	private static final String PATH_ARGUMENT = "path";
	
	/**
	 * The name of the repository argument.
	 */
	private static final String REPOSITORY_NAME_ARGUMENT = "repository";
	
	/**
	 * The name of the repository.
	 */
	private String repositoryName;
	
	/**
	 * The path identifying the content inside the specified <code>repository</code>.
	 */
	private String path;
	
	/**
	 * 
	 */
	private String contentParameterName;
	
	/**
	 * 
	 */
	private String contentVersionParameterName;
	
	/**
	 * Default constructor.
	 */
	public PathContentProvider() 
	{
		super();
	}

	/**
	 * 
	 */
	protected String getContentParameterName()
	{
		return contentParameterName;
	}
	
	/**
	 * 
	 */
	protected String getContentVersionParameterName()
	{
		return contentVersionParameterName;
	}
	
	/**
	 * 
	 */
	protected void initializeContentVO() throws WorkflowException
	{
		try
		{
			logger.debug("Using repository=["+ repositoryName + "] path=["+ path + "]");
			final Repository repository = RepositoryController.getController().getRepositoryWithName(repositoryName, getDatabase());
			if(repository == null)
			{
				throwException("No repository with the name [" + repositoryName + "] found.");
			}
			setContentVO(ContentController.getContentController().getContentVOWithPath(repository.getId(), path, false, getPrincipal(), getDatabase()));
		} 
		catch(Exception e) 
		{
			throwException(e);
		}
	}

	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		path                        = getArgument(PATH_ARGUMENT);
		repositoryName              = getArgument(REPOSITORY_NAME_ARGUMENT);
		contentParameterName        = getArgument(CONTENT_PARAMETER_NAME_ARGUMENT, ContentFunction.CONTENT_PARAMETER);
		contentVersionParameterName = getArgument(CONTENT_VERSION_PARAMETER_NAME_ARGUMENT, ContentFunction.CONTENT_VERSION_PARAMETER);
	}
}

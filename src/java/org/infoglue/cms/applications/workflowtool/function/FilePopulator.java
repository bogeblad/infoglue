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

import java.io.File;

import org.apache.log4j.Logger;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;

import com.opensymphony.workflow.WorkflowException;

/**
 * This function is used for populating the propertyset with the contents of a file (stored relative the <code>contextRootPath</code>). 
 * <p>The variables (that is <code>${...}</code>) in the content of the file will be translated.</p>
 * 
 * <h1 class="workflow">Context in</h1>
 * <table class="workflow">
 *   <thead class="workflow">
 *     <tr class="workflow"><th class="workflow">Name</th><th class="workflow">Type</th><th class="workflow">Class</th><th class="workflow">Required</th><th class="workflow">Default</th><th class="workflow">Comments</th></tr>
 *   </thead>
 *   <tbody class="workflow">
 *     <tr class="workflow"><td class="workflow">path</td><td class="workflow">argument</td><td class="workflow">String</td><td class="workflow">true</td><td class="workflow">-</td><td class="workflow_comment">The path of the file (relative the <code>contextRootPath</code>).</td></tr>
 *     <tr class="workflow"><td class="workflow">key</td><td class="workflow">argument</td><td class="workflow">String</td><td class="workflow">true</td><td class="workflow">-</td><td class="workflow_comment">The key to use when storing the result in the propertyset.</td></tr>
 *   </tbody>
 * </table>
 * <h1 class="workflow">Context out</h1>
 * <table class="workflow">
 *   <thead class="workflow">
 *     <tr class="workflow"><th class="workflow">Name</th><th class="workflow">Type</th><th class="workflow">Class</th><th class="workflow">Comments</th></tr>
 *   </thead>
 *   <tbody class="workflow">
 *     <tr class="workflow"><td class="workflow">&lt;key&gt;</td><td class="workflow">propertyset</td><td class="workflow">DataString</td><td class="workflow_comment">The translated file contents.</td></tr>
 *   </tbody>
 * </table>
 */
public class FilePopulator extends InfoglueFunction 
{
    private final static Logger logger = Logger.getLogger(FilePopulator.class.getName());

	/**
	 * The name of the path argument.
	 */
	private static final String PATH_ARGUMENT = "path";
	
	/**
	 * The name of the key argument.
	 */
	private static final String PROPERTYSET_KEY_ARGUMENT = "key";
	
	/**
	 * The path of the file (relative the <code>contextRootPath</code>).
	 */
	private String path;
	
	/**
	 * The key to use when storing the result in the propertyset.
	 */
	private String key;
	
	/**
	 * Default constructor.
	 */
	public FilePopulator() 
	{
		super();
	}

	/**
	 * Loads the file, translates the content and stores the result in the propertyset.
	 * 
	 * @throws WorkflowException if an error occurs during the execution.
	 */
	protected void execute() throws WorkflowException 
	{
		try
		{
			final String fullPath = getFullPath();
			final String unparsed = FileHelper.getFileAsString(new File(fullPath));
			final String parsed   = translate(unparsed);
			
			logger.debug("path=[" + fullPath + "],unparsed=[" + unparsed + "],parsed=[" + parsed + "]");
			setPropertySetDataString(key, parsed);
		}
		catch(Exception e)
		{
			throwException(e);
		}
	}
	
	/**
	 * Returns the full path of the file.
	 * 
	 * @return the full path of the file.
	 */
	private String getFullPath()
	{
		return CmsPropertyHandler.getContextRootPath() + path;
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
		this.path = getArgument(PATH_ARGUMENT); 
		this.key  = getArgument(PROPERTYSET_KEY_ARGUMENT);
	}
}

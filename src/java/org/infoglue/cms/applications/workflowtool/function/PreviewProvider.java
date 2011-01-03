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

import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.URLHelper;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class PreviewProvider extends InfoglueFunction
{
	/**
	 * 
	 */
	public static final String SITENODE_PARAMETER = "previewSiteNode";
	
	/**
	 * 
	 */
	public static final String PREVIEW_URL_PROPERTYSET_KEY = "previewURL";

	/**
	 * 
	 */
	private ContentVO content;

	/**
	 * 
	 */
	private LanguageVO language;

	/**
	 * 
	 */
	private SiteNodeVO previewSiteNode;

	
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		final String baseURL   = CmsPropertyHandler.getPreviewDeliveryUrl();
		final URLHelper helper = new URLHelper(baseURL, content.getId(), previewSiteNode.getId(), language.getId());
		setPropertySetString(PREVIEW_URL_PROPERTYSET_KEY, helper.getURL());
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
		content         = (ContentVO)  getParameter(ContentFunction.CONTENT_PARAMETER);
		language        = (LanguageVO) getParameter(LanguageProvider.LANGUAGE_PARAMETER);
		previewSiteNode = (SiteNodeVO) getParameter(SITENODE_PARAMETER);
	}
}

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

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.InconsistenciesController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.FileUploadHelper;
import org.infoglue.cms.util.sorters.FileComparator;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.MathHelper;

import webwork.action.ActionContext;

/**
 * This class acts as a system tail on the logfiles available.
 * 
 * @author Mattias Bogeblad
 */

public class ViewInconsistenciesAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private List inconsistencies = null;
	private Integer registryId = null;
	
    public String doInput() throws Exception
    {
    	return "input";
    }

    public String doExecute() throws Exception
    {
    	inconsistencies = InconsistenciesController.getController().getAllInconsistencies();
        
    	return "success";
    }

    public String doRemoveReference() throws Exception
    {
    	InconsistenciesController.getController().removeReferences(registryId, this.getInfoGluePrincipal());
    	
    	inconsistencies = InconsistenciesController.getController().getAllInconsistencies();
        
    	return "success";
    }
    
    public List getInconsistencies() 
	{
		return inconsistencies;
	}
    
    public SiteNodeVO getSiteNodeVO(String siteNodeId) throws NumberFormatException, SystemException, Bug
    {
    	return SiteNodeController.getController().getSiteNodeVOWithId(new Integer(siteNodeId));
    }

    public ContentVO getContentVO(String contentId) throws NumberFormatException, SystemException, Bug
    {
    	return ContentController.getContentController().getContentVOWithId(new Integer(contentId));
    }

	public Integer getRegistryId() 
	{
		return registryId;
	}

	public void setRegistryId(Integer registryId) 
	{
		this.registryId = registryId;
	}
}

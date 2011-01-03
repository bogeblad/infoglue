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

package org.infoglue.cms.treeservice.ss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.util.Timer;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * ContentNodeSupplier.java
 * Created on 2002-sep-30 
 * @author Stefan Sik, ss@frovi.com 
 * @author Frank Febbraro (frank@phase2technology.com) Refactoring and Sorting
 */

public class ContentNodeSupplier extends BaseNodeSupplier
{
    private final static Logger logger = Logger.getLogger(ContentNodeSupplier.class.getName());

	private ArrayList cacheLeafs;
	private boolean showLeafs = true;
	private String[] allowedContentTypeIds = null;
	private InfoGluePrincipal infogluePrincipal = null;
	private Integer repositoryId = null;
	private List languageVOList = null;
	
	public ContentNodeSupplier(Integer repositoryId, InfoGluePrincipal infogluePrincipal) throws SystemException
	{
		ContentVO vo =null;
		try
		{
		    this.infogluePrincipal = infogluePrincipal;
		    this.repositoryId = repositoryId;
		    this.languageVOList = LanguageController.getController().getLanguageVOList(repositoryId);

			Timer t = new Timer();
			if(repositoryId != null && repositoryId.intValue() > 0)
			{
				try
				{
					vo = ContentControllerProxy.getController().getRootContentVO(repositoryId, infogluePrincipal.getName());
				}
				catch (Exception e) 
				{
					logger.warn("Not a valid repository");
				}
				BaseNode rootNode =  new ContentNodeImpl();
				rootNode.setChildren(true);
				rootNode.setId(vo.getId());
				rootNode.setTitle(vo.getName());
				rootNode.setContainer(vo.getIsBranch().booleanValue());	
				
				setRootNode(rootNode);
			}
			
			if(logger.isDebugEnabled())
				t.printElapsedTime("root node processed");
		}
		catch (ConstraintException e)
		{
			e.printStackTrace();
		}
			
	}
	/**
	 * @see com.frovi.ss.Tree.BaseNodeSupplier#hasChildren()
	 */
	public boolean hasChildren()
	{
		if (showLeafs)
			return false;
		else
			return true;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	public Collection getChildContainerNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();
		
		Timer t = new Timer();

		List children = null;
		try
		{
			//children = ContentController.getContentController().getContentChildrenVOList(parentNode);
			children = ContentController.getContentController().getContentChildrenVOList(parentNode, allowedContentTypeIds, false);
		}
		catch (ConstraintException e)
		{
			logger.warn("Error getting Content Children", e);
		}
		catch (SystemException e)
		{
			logger.warn("Error getting Content Children", e);
		}
		
		if(logger.isDebugEnabled())
			t.printElapsedTime("got children");
		
		//Filter list on content type names if set such is stated
		try
		{
		    if(allowedContentTypeIds != null)
			{
		        List filteredList = new ArrayList();
		        Iterator iterator = children.iterator();
				while(iterator.hasNext())
				{
					ContentVO contentVO = (ContentVO) iterator.next();
					if(contentVO.getContentTypeDefinitionId() != null && !contentVO.getIsBranch().booleanValue())
					{
						ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
						boolean exists = false;
						for(int i=0; i<allowedContentTypeIds.length; i++)
						{
						    String allowedId = allowedContentTypeIds[i];
						    
						    if(allowedId.equalsIgnoreCase(contentTypeDefinitionVO.getId().toString()))
							{
						        exists = true;
						        break;
							}
						}

						if(exists)
						{
						    filteredList.add(contentVO);
						}
					}
					else
					{
					    filteredList.add(contentVO);
					}
				}
				
				children = filteredList;
			}
		}
		catch(Exception e)
		{
		    logger.warn("Error filtering Content Children", e);
		}
		
		if(logger.isDebugEnabled())
			t.printElapsedTime("Done filtering children");
				
		//Sort the tree nodes if setup to do so
		String sortProperty = CmsPropertyHandler.getContentTreeSort();
		if(sortProperty != null)
			Collections.sort(children, new ReflectionComparator(sortProperty));
		
		Iterator i = children.iterator();
		while(i.hasNext())
		{
			ContentVO vo = (ContentVO) i.next();
			
			boolean hasUserContentAccess = true;
			String useAccessRightsOnContentTreeString = CmsPropertyHandler.getUseAccessRightsOnContentTree();
			if(useAccessRightsOnContentTreeString != null && useAccessRightsOnContentTreeString.equalsIgnoreCase("true"))
				hasUserContentAccess = getHasUserContentAccess(this.infogluePrincipal, vo.getId());

			if(vo.getName().equals("Meta info folder"))
			{
				try
				{
					hasUserContentAccess = AccessRightController.getController().getIsPrincipalAuthorized(this.infogluePrincipal, "ContentTool.ShowMetaInfoFolders", false, true);
				}
				catch (Exception e) 
				{
					logger.warn("Problem getting access to meta info:" + e.getMessage(), e);
				}
			}
			
			if(hasUserContentAccess)
			{
				BaseNode node =  new ContentNodeImpl();
				node.setId(vo.getId());
				node.setTitle(vo.getName());
				
				String disableCustomIcons = CmsPropertyHandler.getDisableCustomIcons();
				if(disableCustomIcons == null || !disableCustomIcons.equals("true"))
					node.getParameters().put("contentTypeDefinitionId", vo.getContentTypeDefinitionId());
				
				if(vo.getIsProtected().intValue() == ContentVO.YES.intValue())
					node.getParameters().put("isProtected", "true");
				
				try
				{
					Iterator languageVOListIterator = languageVOList.iterator();
					while(languageVOListIterator.hasNext())
					{
						LanguageVO languageVO = (LanguageVO)languageVOListIterator.next();
						ContentVersionVO latestContentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(vo.getId(), languageVO.getId());
						if(latestContentVersion != null && !latestContentVersion.getStateId().equals(ContentVersionVO.PUBLISHED_STATE))
						{
							node.getParameters().put("stateId", "" + latestContentVersion.getStateId());							
							break;
						}
					}
				}
				catch (Exception e) 
				{
					logger.warn("A problem when fecthing latest master content version: " + e.getMessage(), e);
				}
				
				if (vo.getIsBranch().booleanValue())
				{
					node.setContainer(true);
					node.setChildren((vo.getChildCount().intValue() > 0));
					
					ret.add(node);
				}
				else if(showLeafs)
				{
					node.setContainer(false);
					
				    cacheLeafs.add(node);				
				}
			}			
		}

		if(logger.isDebugEnabled())
			t.printElapsedTime("Done sorting children");
		
		return ret;
	}
	

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		return (cacheLeafs == null ) ? new ArrayList() : cacheLeafs;
	}

	/**
	 * Sets the showLeafs.
	 * @param showLeafs The showLeafs to set
	 */
	public void setShowLeafs(boolean showLeafs)
	{
		this.showLeafs = showLeafs;
	}

    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
    }
}

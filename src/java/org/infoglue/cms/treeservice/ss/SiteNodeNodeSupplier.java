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
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCC;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCCFactory;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.cms.util.sorters.SiteNodeComparator;
import org.infoglue.deliver.util.Timer;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * ContentNodeSupplier.java
 * Created on 2002-sep-30 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 */
public class SiteNodeNodeSupplier extends BaseNodeSupplier
{

    private final static Logger logger = Logger.getLogger(SiteNodeNodeSupplier.class.getName());

	private ViewSiteNodeTreeUCC ucc;
	private ArrayList cacheLeafs;
	private InfoGluePrincipal infogluePrincipal = null;

	
	public SiteNodeNodeSupplier(Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws SystemException
	{
	    this.infogluePrincipal = infoGluePrincipal;

		SiteNodeVO vo =null;
		ucc = ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC();	
		try
		{
			if(repositoryId != null && repositoryId.intValue() > 0)
			{
				vo = ucc.getRootSiteNode(repositoryId, infoGluePrincipal);
				BaseNode rootNode =  new SiteNodeNodeImpl();
				rootNode.setChildren(true);
				rootNode.setId(vo.getId());
				rootNode.setTitle(vo.getName());
				rootNode.setContainer(vo.getIsBranch().booleanValue());	
				
				setRootNode(rootNode);
			}
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
		return false;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	/*
	public Collection getChildContainerNodes(Integer parentNode) throws SystemException, Exception
	{
		Timer timer = new Timer();

		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();
		
		List children = null;
		try
		{
			children = ucc.getSiteNodeChildren(parentNode);
		}
		catch (ConstraintException e)
		{
			logger.warn("Error getting SiteNode Children", e);
		}
		catch (SystemException e)
		{
			logger.warn("Error getting SiteNode Children", e);
		}
		
		//Sort the tree nodes if setup to do so
		String sortProperty = CmsPropertyHandler.getStructureTreeSort();
		if(sortProperty != null)
			Collections.sort(children, new ReflectionComparator(sortProperty));
		
		Iterator i = children.iterator();
		while(i.hasNext())
		{
			SiteNodeVO vo = (SiteNodeVO) i.next();
			
			BaseNode node =  new SiteNodeNodeImpl();
			node.setId(vo.getId());
			node.setTitle(vo.getName());
			
			if (vo.getIsBranch().booleanValue())
			{
				node.setContainer(true);
				node.setChildren((vo.getChildCount().intValue() > 0)); // 
				ret.add(node);
			}
			else
			{
				node.setContainer(false);
				cacheLeafs.add(node);				
			}
			
		}
		
        timer.printElapsedTime("ChildNodes took...");

		return ret;
	}
*/
	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	public Collection getChildContainerNodes(Integer parentNode) throws SystemException, Exception
	{
		Timer timer = new Timer();
		
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();
		//List children = null;

		String sortProperty = CmsPropertyHandler.getStructureTreeSort();
		String isHiddenProperty = CmsPropertyHandler.getStructureTreeIsHiddenProperty();

        beginTransaction(db);

        try
        {
        	/*
            SiteNode parentSiteNode = SiteNodeController.getSiteNodeWithId(parentNode, db, true);
	        Collection children = parentSiteNode.getChildSiteNodes();
	    	List childrenVOList = SiteNodeController.toVOList(children);
			*/
        	List<SiteNodeVO> childrenVOList = SiteNodeController.getController().getChildSiteNodeVOList(parentNode, false, db);
        	
        	/*
			Iterator childrenVOListIterator = childrenVOList.iterator();
			while(childrenVOListIterator.hasNext())
			{
				SiteNodeVO siteNodeVO = (SiteNodeVO)childrenVOListIterator.next();
				if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().intValue() > -1)
				{
					try
					{
						ContentVO contentVO = ContentController.getContentController().getContentVOWithId(siteNodeVO.getMetaInfoContentId(), db);
						
					    //LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db);
						//ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVO.getContentId(), masterLanguage.getId(), db);
						
						if(sortProperty != null)
						{
							String[] sortOrders = sortProperty.split(",");
							for(int i=sortOrders.length - 1; i > -1; i--)
							{
								String sortOrderProperty = sortOrders[i].trim();
								
								if(sortOrderProperty.startsWith("extra:"))
								{
									LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), db);
									ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVO.getContentId(), masterLanguage.getId(), db);
									if(contentVersion != null)
									{
										sortOrderProperty = sortOrderProperty.substring(6);
									    String propertyValue = ContentVersionController.getContentVersionController().getAttributeValue(contentVersion.getValueObject(), sortOrderProperty, false);
									    siteNodeVO.getExtraProperties().put(sortOrderProperty, propertyValue);
			
										if(isHiddenProperty != null && !isHiddenProperty.equals(""))
										{
										    String hiddenProperty = ContentVersionController.getContentVersionController().getAttributeValue(contentVersion.getValueObject(), isHiddenProperty, false);
										    if(hiddenProperty == null || hiddenProperty.equals(""))
										    	hiddenProperty = "false";
										    
										    siteNodeVO.getExtraProperties().put("isHidden", hiddenProperty);
										}
									}
								}
							}
						}
					}
					catch(Exception e)
					{
						logger.warn("The site node " + siteNodeVO.getName() + "[" + siteNodeVO.getId() + "] has problems: " + e.getMessage(), e);
					}
				}
			}
			*/
        	
			//Sort the tree nodes if setup to do so
			/*
        	if(sortProperty != null)
			{
				String[] sortOrders = sortProperty.split(",");
				for(int i=sortOrders.length - 1; i > -1; i--)
				{
					String sortOrderProperty = sortOrders[i].trim();
					if(sortOrderProperty.startsWith("extra:"))
						sortOrderProperty = sortOrderProperty.substring(6);
						
					Collections.sort(childrenVOList, new SiteNodeComparator(sortOrderProperty, "asc", null));
					
					Iterator siteNodeChildrenIterator = childrenVOList.iterator();
					while(siteNodeChildrenIterator.hasNext())
					{
						SiteNodeVO vo = (SiteNodeVO) siteNodeChildrenIterator.next();
					}
				}
			}
			*/

        	Integer expectedSortOrder = 0;
        	
			Iterator<SiteNodeVO> i = childrenVOList.iterator();
			while(i.hasNext())
			{
				SiteNodeVO vo = i.next();
				
				boolean hasUserPageAccess = true;
				String useAccessRightsOnStructureTreeString = CmsPropertyHandler.getUseAccessRightsOnStructureTree();
				if(useAccessRightsOnStructureTreeString != null && useAccessRightsOnStructureTreeString.equalsIgnoreCase("true"))
					hasUserPageAccess = getHasUserPageAccess(this.infogluePrincipal, vo.getId());
				
				Object isHiddenObject = vo.getExtraProperties().get("isHidden");
				//System.out.println("isHiddenObject: " + isHiddenObject);
					
				if(hasUserPageAccess)
				{
					BaseNode node =  new SiteNodeNodeImpl();
					node.setId(vo.getId());
					node.setTitle(vo.getName());
					if(isHiddenObject == null)
						node.getParameters().put("isHidden", "false");
					else
						node.getParameters().put("isHidden", "" + isHiddenObject);

					//System.out.println("latestSiteNodeVO.getIsHidden(): " + vo.getIsHidden() + " on " + vo.getId());
					if(vo.getIsHidden() != null)
						node.getParameters().put("isHidden", "" + vo.getIsHidden());

					SiteNodeVersionVO latestSiteNodeVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, vo.getId());
					if(latestSiteNodeVO != null)
					{
						if(latestSiteNodeVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
							node.getParameters().put("isProtected", "true");
	
						if(!latestSiteNodeVO.getStateId().equals(SiteNodeVersionVO.PUBLISHED_STATE))
							node.getParameters().put("stateId", "" + latestSiteNodeVO.getStateId());							
					}
					
					if (vo.getIsBranch().booleanValue())
					{
						node.setContainer(true);
						node.setChildren((vo.getChildCount().intValue() > 0)); // 
						ret.add(node);
					}
					else
					{
						node.setContainer(false);
						cacheLeafs.add(node);				
					}
				}	
				
				//Checks if the current node has the correct sortOrder
				//System.out.println("vo.getSortOrder():" + vo.getSortOrder() + "=" + expectedSortOrder);
				if(!vo.getSortOrder().equals(expectedSortOrder))
				{
					//System.out.println("Changing sortOrder from:" + vo.getSortOrder() + " to " + expectedSortOrder);
					SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, vo.getId());
					if(latestSiteNodeVersion != null)
					{
						latestSiteNodeVersion.setSortOrder(expectedSortOrder);
						if(isHiddenObject != null)
						{
							//System.out.println("Setting hidden:" + isHiddenObject + " on " + latestSiteNodeVersion.getId());
							latestSiteNodeVersion.setIsHidden(new Boolean("" + isHiddenObject));
						}
					}
				}
				else if(vo.getIsHidden() == null && isHiddenObject != null)
				{
					SiteNodeVersion latestSiteNodeVersion = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, vo.getId());
					if(latestSiteNodeVersion != null)
					{
						//System.out.println("Setting hidden:" + isHiddenObject + " on " + latestSiteNodeVersion.getId());
						latestSiteNodeVersion.setIsHidden(new Boolean("" + isHiddenObject));
					}
				}
				
				expectedSortOrder++;
			}
			
            commitTransaction(db);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        logger.info("Getting the sitenodes for the tree took " + timer.getElapsedTime() + "ms");
        
		return ret;
	}

	
	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		return cacheLeafs;
	}

}

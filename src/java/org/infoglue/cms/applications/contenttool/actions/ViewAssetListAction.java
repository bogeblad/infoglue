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

/**
 * Work in progress
 * @author Stefan Sik
 */
package org.infoglue.cms.applications.contenttool.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.util.MathHelper;


public class ViewAssetListAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewAssetListAction.class.getName());

	/**
	 * Action class for the DigitalAsset Browser. 
     * TODO: Improve performance by adding more specialized
     * methods in DigitalAssetController to search and filter assets
	 */
	private static final long serialVersionUID = 1707633990701035545L;

	public ContentTypeDefinitionVO contentTypeDefinitionVO;
	private Integer maxResultCount = new Integer(20); // TODO: Configuration
	public List availableLanguages = null;
	
	private Integer languageId;
	private Integer repositoryId;
	private ContentVO contentVO;
	public List attributes = null;

	private List repositories;
	private List filters;
	private String filter = "";
	
	private String assetKey 		= null;
	private boolean treatAsLink    = false;
	private boolean showLeafs		=false;
	
	private HashMap contentMap = new HashMap();
    
    public ViewAssetListAction()
    {
        this(new ContentVO());
    }
    
    public ViewAssetListAction(ContentVO contentVO)
    {
        this.contentVO = contentVO;
    }

    private void createContentIdList(ContentVO parent) throws ConstraintException, SystemException
    {
        contentMap.put(parent.getContentId(), parent.getName());
        
        List children = ContentControllerProxy.getController().getContentChildrenVOList(parent.getContentId(), null, false);
        for(Iterator i=children.iterator();i.hasNext();)
        {
            ContentVO cvo = (ContentVO) i.next();
            createContentIdList(cvo);
        }
    }
    
    protected void initialize(Integer contentId, Integer languageId) throws Exception
    {
        this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), contentId);
        createContentIdList(this.contentVO);
    } 

    public String doExecute() throws Exception
    {
        if(getContentId() != null && getContentId().intValue() != -1)
            this.initialize(getContentId(), this.languageId);
        
   	    return "success";
    }
    
    public String doBrowser() throws Exception
    {
        this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
        
        /*
         * TODO: Create the filters through some configuration
         */
        
        filters = new ArrayList();
        filters.add(new FilterVO("All", ""));
        filters.add(new FilterVO("Images", "image/.*"));
        filters.add(new FilterVO("GIF Images", ".*gif"));
        filters.add(new FilterVO("JPEG Images", ".*jpeg"));
        filters.add(new FilterVO("PNG Images", ".*png"));
        filters.add(new FilterVO("Documents", ".*word.*|.*excel.*|.*pdf.*"));
        filters.add(new FilterVO("compressed", ".*compressed"));
        
        return "browser";
    }
    
    
    public String getContentPath(Integer contentId) throws ConstraintException, SystemException, Bug, Exception
    {
        ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), contentId);
        StringBuffer ret = new StringBuffer();

        while (contentVO.getParentContentId() != null)
        {
            try 
            {
                contentVO = ContentControllerProxy.getController().getContentVOWithId(contentVO.getParentContentId());
            } 
            catch (SystemException e) 
            {
                e.printStackTrace();
            } 
            catch (Bug e) 
            {
                e.printStackTrace();
            }
            ret.insert(0, "" + contentVO.getContentId() + ",");
        }
        ret.append("" + contentId);
        return ret.toString();
    }
    

    public MathHelper getMathHelper()
    {
        return new MathHelper();
    }
    
    public java.lang.Integer getContentId()
    {
        return this.contentVO.getContentId();
    }
        
    public void setContentId(java.lang.Integer contentId)
    {
	    this.contentVO.setContentId(contentId);
    }
    
    public java.lang.Integer getContentTypeDefinitionId()
    {
        return this.contentTypeDefinitionVO.getContentTypeDefinitionId();
    }

    public String getContentTypeDefinitionName()
    {
        return this.contentTypeDefinitionVO.getName();
    }
            
   	public void setLanguageId(Integer languageId)
	{
   	    this.languageId = languageId;
	}

    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
	
    public String getName()
    {
        return this.contentVO.getName();
    }

    public java.lang.Integer getRepositoryId()
    {
        if(this.contentVO != null && this.contentVO.getRepositoryId() != null)
            return this.contentVO.getRepositoryId();
        else
            return this.repositoryId;
    }

	public List getAvailableLanguages()
	{
		return this.availableLanguages;
	}	

	/**
	 * Returns a list of digital assets available for this content and all the child contents.
	 */
	public List getInheritedDigitalAssets()
	{
		List digitalAssets = new ArrayList();
		
		try
		{
            for(Iterator i = contentMap.keySet().iterator();i.hasNext();)
            {
                Integer _contentId = (Integer) i.next();
                DigitalAssetCollection collection = new DigitalAssetCollection(_contentId, (String) contentMap.get(_contentId));
                collection.setContentPath(getContentPath(_contentId));
                
                if(filter.length() > 0)
                {
                	for(Iterator assetIterator=DigitalAssetController.getDigitalAssetVOList(_contentId, this.languageId, true).iterator();assetIterator.hasNext();)
                	{
                		DigitalAssetVO digitalAssetVO = (DigitalAssetVO) assetIterator.next();
                		if(digitalAssetVO.getAssetContentType().matches(filter))
                		{
                			collection.getAssets().add(digitalAssetVO);
                		}
                	}
                }
                else
                {
                	collection.getAssets().addAll(DigitalAssetController.getDigitalAssetVOList(_contentId, this.languageId, true));
                }
                
                
                ContentVersionVO contentVersionVO = getLatestContentVersionVO(_contentId);
                if(contentVersionVO != null)
                {
                    collection.setContentVersionId(contentVersionVO.getContentVersionId());
                    collection.setLocked(ContentVersionVO.WORKING_STATE.compareTo(contentVersionVO.getStateId())!=0);
                }
                
                digitalAssets.add(collection);
            }
		}
		catch(Exception e)
		{
			logger.warn("We could not fetch the list of digitalAssets: " + e.getMessage(), e);
		}
		
		return digitalAssets;
	}	


	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer digitalAssetId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}

	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetUrl(Integer contentId, Integer languageId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetUrl(contentId, languageId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	
	
	/**
	 * This method fetches the blob from the database and saves it on the disk.
	 * Then it returnes a url for it
	 */
	
	public String getDigitalAssetThumbnailUrl(Integer contentId, Integer languageId) throws Exception
	{
		String imageHref = null;
		try
		{
       		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(contentId, languageId);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
			imageHref = e.getMessage();
		}
		
		return imageHref;
	}
	

    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public List getRepositories()
    {
        return repositories;
    }
    
    public String getAssetKey()
    {
        return assetKey;
    }
    
    public void setAssetKey(String assetKey)
    {
        this.assetKey = assetKey;
    }
    
    public boolean getTreatAsLink()
    {
        return treatAsLink;
    }
    
    public void setTreatAsLink(boolean treatAsLink)
    {
        this.treatAsLink = treatAsLink;
    }
    
	public ContentVO getContentVO() 
	{
		return contentVO;
	}
	
	
	protected ContentVersionVO getLatestContentVersionVO(Integer contentId) throws SystemException, Exception
	{
		Integer contentVersionId = null;
		ContentVersionVO contentVersionVO = null;
		contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);

		Integer usedRepositoryId = this.repositoryId;
	    if(this.repositoryId == null && this.contentVO != null)
	        usedRepositoryId = this.contentVO.getRepositoryId();
	    
	    LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(usedRepositoryId);
	    contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
		
		if(contentVersionVO != null)
			contentVersionId = contentVersionVO.getContentVersionId();
		

        if(contentVersionId != null)	
			contentVersionVO = ContentVersionControllerProxy.getController().getACContentVersionVOWithId(this.getInfoGluePrincipal(), contentVersionId);
        
		return contentVersionVO;    		 	

        /*
		if(this.forceWorkingChange && contentVersionVO != null && !contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
		{
		    ContentVersion contentVersion = ContentStateController.changeState(contentVersionVO.getContentVersionId(), ContentVersionVO.WORKING_STATE, "Edit on sight", false, this.getInfoGluePrincipal(), this.getContentId(), new ArrayList());
		    contentVersionId = contentVersion.getContentVersionId();
		    contentVersionVO = contentVersion.getValueObject();
		}
		*/
		
	}
	
	public class FilterVO
	{
		private String name = null;
		private String value = null;
		public FilterVO(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
    public class DigitalAssetCollection
    {
        List assets = new ArrayList();
        String contentPath = null;
        Integer contentId = null;
        Integer contentVersionId = null;
        boolean locked = false;
        Integer languageId = null;
        String contentName = null;
        
        
        public DigitalAssetCollection(Integer contentId, String contentName)
        {
            this.contentId = contentId;
            this.contentName = contentName;
        }
        public List getAssets()
        {
            return assets;
        }
        public void setAssets(List assets)
        {
            this.assets = assets;
        }
        public Integer getContentId()
        {
            return contentId;
        }
        public void setContentId(Integer contentId)
        {
            this.contentId = contentId;
        }
        public String getContentName()
        {
            return contentName;
        }
        public void setContentName(String contentName)
        {
            this.contentName = contentName;
        }
        public String getContentPath()
        {
            return contentPath;
        }
        public void setContentPath(String contentPath)
        {
            this.contentPath = contentPath;
        }
		public Integer getContentVersionId() {
			return contentVersionId;
		}
		public void setContentVersionId(Integer contentVersionId) {
			this.contentVersionId = contentVersionId;
		}
		public Integer getLanguageId() {
			return languageId;
		}
		public void setLanguageId(Integer languageId) {
			this.languageId = languageId;
		}
		public boolean isLocked() {
			return locked;
		}
		public void setLocked(boolean locked) {
			this.locked = locked;
		}
        
    }

	public boolean isShowLeafs() {
		return showLeafs;
	}

	public void setShowLeafs(boolean showLeafs) {
		this.showLeafs = showLeafs;
	}

	public List getFilters() {
		return filters;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

}

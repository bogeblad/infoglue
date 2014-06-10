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

package org.infoglue.cms.entities.structure;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;

public class SiteNodeVersionVO implements BaseEntityVO
{

	public static final Integer WORKING_STATE   = new Integer(0);
	public static final Integer FINAL_STATE     = new Integer(1);
	public static final Integer PUBLISH_STATE   = new Integer(2);
	public static final Integer PUBLISHED_STATE = new Integer(3);
	public static final Integer UNPUBLISH_STATE = new Integer(4);

	public static final Integer NO 			= new Integer(0);
	public static final Integer YES 		= new Integer(1);
	public static final Integer INHERITED 	= new Integer(2);
	public static final Integer YES_WITH_INHERIT_FALLBACK = new Integer(3);

	public static final Integer INHERIT_SECURE 	= new Integer(0);
	public static final Integer NORMAL_SECURE 	= new Integer(1);
	public static final Integer ALLOW_SECURE 	= new Integer(2);
	public static final Integer FORCE_SECURE 	= new Integer(3);

    private Integer siteNodeVersionId;
    private Integer stateId       		= WORKING_STATE;
    private Integer versionNumber 		= new Integer(1);
    private Date modifiedDateTime 		= DateHelper.getSecondPreciseDate();
    private String versionComment 		= "No comment";;
    private String versionModifier		= null;
    private Boolean isCheckedOut  		= new Boolean(false);
  	private Boolean isActive      		= new Boolean(true);
  	
	private Integer isProtected			= INHERITED;
	private Integer disablePageCache	= INHERITED;
	private Integer disableEditOnSight	= INHERITED;
	private Integer disableLanguages    = INHERITED;
	private Integer disableForceIdentityCheck = INHERITED;
	private Integer forceProtocolChange = INHERIT_SECURE;
	private String contentType 			= "text/html";
  	private String pageCacheKey			= "default";
  	private String pageCacheTimeout		= null;
	private Integer sortOrder 			= new Integer(100);
	private Boolean isHidden			= new Boolean(false);

    private Integer siteNodeId			= null;
	private String siteNodeName			= "";
  
	//Fields only here for performance - not allways populated, only in some views
    private String versionModifierDisplayName = null;
	private String path = null;
	private Boolean hasAnonymousUserAccess = new Boolean(true);
  
	public java.lang.Integer getSiteNodeVersionId()
    {
        return this.siteNodeVersionId;
    }
                
    public void setSiteNodeVersionId(java.lang.Integer siteNodeVersionId)
    {
        this.siteNodeVersionId = siteNodeVersionId;
    }

    public java.lang.Integer getSiteNodeId()
    {
        return this.siteNodeId;
    }
                
    public void setSiteNodeId(java.lang.Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
    }
    
    public java.lang.Integer getStateId()
    {
        return this.stateId;
    }
                
    public void setStateId(java.lang.Integer stateId)
    {
        this.stateId = stateId;
    }
    
    public java.lang.Integer getVersionNumber()
    {
        return this.versionNumber;
    }
                
    public void setVersionNumber(java.lang.Integer versionNumber)
    {
        this.versionNumber = versionNumber;
    }
    
    public Date getModifiedDateTime()
    {
        return this.modifiedDateTime;
    }
                
    public void setModifiedDateTime(Date modifiedDateTime)
    {
    	if(modifiedDateTime != null)
    		this.modifiedDateTime = modifiedDateTime;
    	else
    		Thread.dumpStack();
    }
    
    public java.lang.String getVersionComment()
    {
        return this.versionComment;
    }
                
    public void setVersionComment(java.lang.String versionComment)
    {
    	if(versionComment != null && !versionComment.equals(""))
    		this.versionComment = versionComment;
    }
    
    public java.lang.Boolean getIsCheckedOut()
    {
        return this.isCheckedOut;
    }
                
    public void setIsCheckedOut(java.lang.Boolean isCheckedOut)
    {
        this.isCheckedOut = isCheckedOut;
    }
      
    public java.lang.Boolean getIsActive()
    {
    	return this.isActive;
	}
    
    public void setIsActive(java.lang.Boolean isActive)
	{
		this.isActive = isActive;
	}
	
	public Integer getId() 
	{
		return getSiteNodeVersionId();
	}
	
	public ConstraintExceptionBuffer validate() 
	{ 
		return null;
	}
	
	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public String getPageCacheKey()
    {
        return pageCacheKey;
    }
    
	public void setPageCacheKey(String pageCacheKey)
    {
	    if(pageCacheKey != null && !pageCacheKey.equalsIgnoreCase(""))
	        this.pageCacheKey = pageCacheKey;
    }

	public String getPageCacheTimeout()
    {
        return pageCacheTimeout;
    }
    
	public void setPageCacheTimeout(String pageCacheTimeout)
    {
	    if(pageCacheTimeout != null && !pageCacheTimeout.equalsIgnoreCase(""))
	        this.pageCacheTimeout = pageCacheTimeout;
	    else
	        this.pageCacheTimeout = null;	
    }

	public Integer getDisableEditOnSight()
	{
		return (disableEditOnSight == null) ? INHERITED : disableEditOnSight;
	}

	public void setDisableEditOnSight(Integer disableEditOnSight)
	{
		this.disableEditOnSight = disableEditOnSight;
	}

	public Integer getDisableLanguages()
	{
		return (disableLanguages == null) ? INHERITED : disableLanguages;
	}

	public void setDisableLanguages(Integer disableLanguages)
	{
		this.disableLanguages = disableLanguages;
	}

	public Integer getDisablePageCache()
	{
		return (disablePageCache == null) ? INHERITED : disablePageCache;
	}

	public void setDisablePageCache(Integer disablePageCache)
	{
		this.disablePageCache = disablePageCache;
	}

	public Integer getIsProtected()
	{
		return (isProtected == null) ? INHERITED : isProtected;
	}

	public void setIsProtected(Integer isProtected)
	{
		this.isProtected = isProtected;
	}

	public Integer getDisableForceIdentityCheck()
	{
		return disableForceIdentityCheck;
	}

	public void setDisableForceIdentityCheck(Integer disableForceIdentityCheck)
	{
		this.disableForceIdentityCheck = disableForceIdentityCheck;
	}

	public Integer getForceProtocolChange()
	{
		return forceProtocolChange;
	}

	public void setForceProtocolChange(Integer forceProtocolChange)
	{
		this.forceProtocolChange = forceProtocolChange;
	}

	public String getVersionModifier()
	{
		return this.versionModifier;
	}

	public void setVersionModifier(String versionModifier)
	{
		this.versionModifier = versionModifier;
	}

	public String getSiteNodeName()
	{
		return siteNodeName;
	}

	public void setSiteNodeName(String siteNodeName)
	{
		this.siteNodeName = siteNodeName;
	}

    /**
	 * @return the versionModifierDisplayName if set by the view. Not allways populated so do not depend on it.
	 */
	public String getVersionModifierDisplayName() 
	{
		return (versionModifierDisplayName != null ? versionModifierDisplayName : versionModifier);
	}

	/**
	 * @param versionModifierDisplayName the versionModifierDisplayName to set
	 */
	public void setVersionModifierDisplayName(String versionModifierDisplayName) 
	{
		this.versionModifierDisplayName = versionModifierDisplayName;
	}

	/**
	 * @return the path
	 */
	public String getPath() 
	{
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) 
	{
		this.path = path;
	}

    public Integer getSortOrder()
    {
        return this.sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsHidden()
    {
        return this.isHidden;
    }
    
    public void setIsHidden(Boolean isHidden)
    {
    	this.isHidden = isHidden;
    }

	public Boolean getHasAnonymousUserAccess() 
	{
		return hasAnonymousUserAccess;
	}

	public void setHasAnonymousUserAccess(Boolean hasAnonymousUserAccess) 
	{
		this.hasAnonymousUserAccess = hasAnonymousUserAccess;
	}

	public String toString()
	{
	    StringBuffer sb = new StringBuffer();
	    sb.append("siteNodeVersionId:" + siteNodeVersionId + '\n');
	    sb.append("stateId:" + stateId + '\n');
	    sb.append("versionNumber:" + versionNumber + '\n');
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    sb.append("modifiedDateTime:" + sdf.format(modifiedDateTime) + ":" + modifiedDateTime.getClass().getName() + '\n');
	    sb.append("versionComment:" + versionComment + '\n');
	    sb.append("versionModifier:" + versionModifier + '\n');
	    sb.append("isCheckedOut:" + isCheckedOut + '\n');
	    sb.append("isActive:" + isActive + '\n');
	    sb.append("isProtected:" + isProtected + '\n');
	    sb.append("disablePageCache:" + disablePageCache + '\n');
	    sb.append("disableEditOnSight:" + disableEditOnSight + '\n');
	    sb.append("disableLanguages:" + disableLanguages + '\n');
	    sb.append("disableForceIdentityCheck:" + disableForceIdentityCheck + '\n');
	    sb.append("forceProtocolChange:" + forceProtocolChange + '\n');
	    sb.append("contentType:" + contentType + '\n');
	    sb.append("pageCacheKey:" + pageCacheKey + '\n');
	    sb.append("pageCacheTimeout:" + pageCacheTimeout + '\n');
	    sb.append("sortOrder:" + sortOrder + '\n');
	    sb.append("isHidden:" + isHidden + '\n');
	    sb.append("siteNodeId:" + siteNodeId + '\n');
	    sb.append("siteNodeName:" + siteNodeName + '\n');
	    
	    return sb.toString();
	}
	
	public boolean equals(Object o)
	{
	    boolean equals = false;
	    
	    if(o instanceof SiteNodeVersionVO)
	    {
	        SiteNodeVersionVO sv = (SiteNodeVersionVO)o;
	        if(sv != null && sv.getSiteNodeVersionId().equals(this.siteNodeVersionId))
	            equals = true;
	    }
	    return equals;
	}
	
	public int hashCode()
	{
	    return this.siteNodeVersionId.intValue();
	}


	
}
        

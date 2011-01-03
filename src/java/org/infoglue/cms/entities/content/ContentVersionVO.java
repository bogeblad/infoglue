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

package org.infoglue.cms.entities.content;

import java.util.Date;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;
import org.infoglue.cms.util.validators.ContentVersionValidator;

public class ContentVersionVO implements BaseEntityVO
{

	public static final Integer WORKING_STATE   = new Integer(0);
	public static final Integer FINAL_STATE     = new Integer(1);
	public static final Integer PUBLISH_STATE   = new Integer(2);
	public static final Integer PUBLISHED_STATE = new Integer(3);
	public static final Integer UNPUBLISH_STATE = new Integer(4);
	public static final Integer UNPUBLISHED_STATE = new Integer(5);
	
	private Integer contentVersionId;
    private Integer stateId						= new Integer(0);
    private Date modifiedDateTime				= DateHelper.getSecondPreciseDate();
    private String versionComment				= "No comment";
    private Boolean isCheckedOut				= new Boolean(false);
   	private Boolean isActive					= new Boolean(true);
	
	private Integer languageId					= null;
	private String languageName 				= "";
   	private Integer contentId					= null;
    private String contentName 					= "";
    private Integer contentTypeDefinitionId		= null;
    private String versionModifier				= null;
	private String versionValue   	 			= "";
	    
    public java.lang.Integer getContentVersionId()
    {
        return this.contentVersionId;
    }
                
    public void setContentVersionId(java.lang.Integer contentVersionId)
    {
        this.contentVersionId = contentVersionId;
    }
  
    public Integer getContentId()
    {
        return this.contentId;
    }
                
    public void setContentId(Integer contentId)
    {
        this.contentId = contentId;
    }
    
    public Integer getContentTypeDefinitionId()
	{
		return contentTypeDefinitionId;
	}

	public void setContentTypeDefinitionId(Integer id)
	{
		contentTypeDefinitionId = id;
	}
	
    public Integer getStateId()
    {
        return this.stateId;
    }
                
    public void setStateId(Integer stateId)
    {
        this.stateId = stateId;
    }
    
    public String getVersionValue()
    {
        return this.versionValue;
    }
                
    public void setVersionValue(String versionValue)
    {
    	this.versionValue = versionValue;
    }
    
    public Date getModifiedDateTime()
    {
        return this.modifiedDateTime;
    }
                
    public void setModifiedDateTime(Date modifiedDateTime)
    {
        this.modifiedDateTime = modifiedDateTime;
    }
    
    public String getVersionComment()
    {
        return this.versionComment;
    }
                
    public void setVersionComment(String versionComment)
    {
    	if(versionComment != null && !versionComment.equals(""))
    		this.versionComment = versionComment;
    }
    
    public Boolean getIsCheckedOut()
    {
        return this.isCheckedOut;
    }
                
    public void setIsCheckedOut(Boolean isCheckedOut)
    {
        this.isCheckedOut = isCheckedOut;
    }
    
   	public Boolean getIsActive()
    {
    	return this.isActive;
	}
    
    public void setIsActive(Boolean isActive)
	{
		this.isActive = isActive;
	}

    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return getContentVersionId();
	}
	
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{ 
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		return ceb;
	}

	public ConstraintExceptionBuffer validateAdvanced(ContentTypeDefinitionVO contentTypeDefinition) 
	{ 
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		ceb.add(new ContentVersionValidator().validate(contentTypeDefinition, this));
		
		return ceb;
	}
        
	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}


	public String getVersionModifier()
	{
		return this.versionModifier;
	}

	public void setVersionModifier(String versionModifier)
	{
		this.versionModifier = versionModifier;
	}


	public ContentVersionVO copy()
	{
		ContentVersionVO copy = new ContentVersionVO();
		
		copy.setContentId(new Integer(this.contentId.intValue()));
		copy.setContentTypeDefinitionId(new Integer(this.contentTypeDefinitionId.intValue()));
		copy.setIsActive(new Boolean(this.isActive.booleanValue()));
		copy.setIsCheckedOut(new Boolean(this.isCheckedOut.booleanValue()));
		copy.setLanguageId(new Integer(this.languageId.intValue()));
		copy.setVersionModifier(this.versionModifier);
		copy.setModifiedDateTime(new Date(this.modifiedDateTime.getTime()));
		copy.setStateId(new Integer(this.stateId.intValue()));
		copy.setVersionComment(new String(this.versionComment));
		copy.setVersionValue(new String(this.versionValue));
		
		return copy;		
	}

	public String getContentName() 
	{
		return contentName;
	}

	public void setContentName(String string) 
	{
		contentName = string;
	}

	public String getLanguageName() 
	{
		return languageName;
	}

	public void setLanguageName(String string) 
	{
		languageName = string;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("id=").append(contentVersionId)
			.append(" contentId=").append(contentId)
			.append(" contentName=").append(contentName)
			.append(" contentTypeDefinitionId=").append(contentTypeDefinitionId)
			.append(" languageId=").append(languageId)
			.append(" languageName=").append(languageName)
			.append(" isActive=").append(isActive)
			.append(" isCheckedOut=").append(isCheckedOut)
			.append(" stateId=").append(stateId)
			.append(" versionModifier=").append(versionModifier)
			.append(" versionComment=").append(versionComment);
		return sb.toString();
	}
	
	public boolean equals(Object o)
	{
	    boolean equals = false;
	    
	    if(o instanceof ContentVersionVO)
	    {
	        ContentVersionVO cv = (ContentVersionVO)o;
	        if(cv != null && cv.getContentVersionId().equals(this.contentVersionId))
	            equals = true;
	    }
	    
	    return equals;
	}
	
	public int hashCode()
	{
	    return this.contentVersionId.intValue();
	}
}
        

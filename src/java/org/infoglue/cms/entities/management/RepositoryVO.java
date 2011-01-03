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

package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ValidatorFactory;

public class RepositoryVO implements BaseEntityVO
{
    private Integer repositoryId;
    private String name;
    private String description;
    private String dnsName;
    private Boolean isDeleted = new Boolean(false);
        
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	
    public Integer getId() 
	{
		return getRepositoryId();
	}

	public String toString()
	{  
		return getName();
	}
  
    public java.lang.Integer getRepositoryId()
    {
        return this.repositoryId;
    }
                
    public void setRepositoryId(java.lang.Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public java.lang.String getName()
    {
        return this.name;
    }
                
    public void setName(java.lang.String name)
    {
        this.name = name;		
    }
  
    public java.lang.String getDescription()
    {
        return this.description;
    }
                
    public void setDescription(java.lang.String description)
    {
        this.description = description;		
    }

    public java.lang.String getDnsName()
    {
        return this.dnsName;
    }
                
    public void setDnsName(java.lang.String dnsName)
    {
    	if(dnsName == null || dnsName.length() == 0)
        	this.dnsName = "undefined";
    	else
    		this.dnsName = dnsName;
    }

    public Boolean getIsDeleted()
    {
    	return this.isDeleted;
	}
    
    public void setIsDeleted(Boolean isDeleted)
	{
		this.isDeleted = isDeleted;
	}

	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
	 */
	public ConstraintExceptionBuffer validate() 
	{
    	ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
    	
    	ValidatorFactory.createStringValidator("Repository.name", true, 2, 50, true, RepositoryImpl.class, this.getId(), null).validate(this.name, ceb);
        ValidatorFactory.createStringValidator("Repository.description", true, 1, 100).validate(description, ceb); 
   	    ValidatorFactory.createStringValidator("Repository.dnsName", false, 0, 2048).validate(dnsName, ceb); 
    	
    	return ceb;
	}

	public String getValueFromDNSNameByKey(String keyword)
	{
		String value = "";
		
		int startIndex = dnsName.indexOf(keyword);
	    if(startIndex != -1)
	    {
	        int endIndex = dnsName.indexOf(",", startIndex);
		    String part = null;
	        if(endIndex > -1)
	        	part = dnsName.substring(startIndex, endIndex);
	        else
	        	part = dnsName.substring(startIndex);
	        
	        String[] partArr = part.split("=");
	        part = (partArr.length > 1)?partArr[1]:"";
	        //part = part.split("=")[1];
	        
	        value = part;
	    }
	    
	    return value;
	}
	
	public String getWorkingBaseUrl()
	{
		return getValueFromDNSNameByKey("working=");
	}

	public String getPreviewBaseUrl()
	{
		return getValueFromDNSNameByKey("preview=");
	}

	public String getLiveBaseUrl()
	{
		return getValueFromDNSNameByKey("live=");
	}

	public String getExtraUrls()
	{
		String extraUrls = "";
		
		String[] strings = dnsName.split(",");
		for(int i=0; i<strings.length; i++)
		{
			String string = strings[i];
			if(string.indexOf("=") == -1)
			{
				if(!extraUrls.equals(""))
					extraUrls = extraUrls + ",";
				extraUrls = extraUrls + string;
			}
		}
	    
	    return extraUrls;
	}
	
	public String getExtraUrlsFormatted()
	{
	    return getExtraUrls().replaceAll(",", "\n");
	}

	public String getWorkingPath()
	{
		return getValueFromDNSNameByKey("workingPath=");
	}

	public String getPath()
	{
		return getValueFromDNSNameByKey("path=");
	}

}
        

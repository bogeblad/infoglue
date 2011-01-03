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

package org.infoglue.deliver.applications.databeans;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author mattias
 * This class represents a slot in a page component structure.
 * A slot can contain any number of components. 
 */

public class Slot
{
	VisualFormatter vf = new VisualFormatter();
	
	private String id;
	private String number;
	private String name;
	private boolean inherit;
	private boolean disableAccessControl = false;
	private List components = new ArrayList();
	private String[] allowedComponentsArray = null;
	private String[] disallowedComponentsArray = null;
	private String[] allowedComponentGroupsArray = null;
	private String addComponentText;
	private String addComponentLinkHTML;
	private int allowedNumberOfComponents = new Integer(-1);
	
	public List getComponents()
	{
		return this.components;
	}

	public String getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setComponents(List components)
	{
		this.components = components;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getNumber()
	{
		return this.number;
	}

	public void setNumber(String number)
	{
		this.number = number;
	}

    public boolean isInherit()
    {
        return inherit;
    }

    public void setInherit(boolean inherit)
    {
        this.inherit = inherit;
    }

    public void setDisableAccessControl(boolean disableAccessControl)
    {
        this.disableAccessControl = disableAccessControl;
    }

    public boolean getDisableAccessControl()
    {
        return this.disableAccessControl;
    }

    public String getLimitationClasses()
    {
    	StringBuilder sb = new StringBuilder();
	    
    	try
    	{
    		//System.out.println("allowedComponentsArray:" + allowedComponentsArray);
    		//System.out.println("disallowedComponentsArray:" + disallowedComponentsArray);
    		//System.out.println("allowedComponentGroupsArray:" + allowedComponentGroupsArray);
    		if(allowedComponentsArray != null)
		    	for(int i=0; i<allowedComponentsArray.length; i++)
		    		sb.append("okName" + vf.replaceNonAscii(vf.escapeForAdvancedJavascripts(allowedComponentsArray[i]), '_') + " ");
	    	if(disallowedComponentsArray != null)
		    	for(int i=0; i<disallowedComponentsArray.length; i++)
		    		sb.append("nokName" + vf.replaceNonAscii(vf.escapeForAdvancedJavascripts(disallowedComponentsArray[i]), '_') + " ");
	    	if(allowedComponentGroupsArray != null)
		    	for(int i=0; i<allowedComponentGroupsArray.length; i++)
		    		sb.append("okGroupName" + vf.replaceNonAscii(vf.escapeForAdvancedJavascripts(allowedComponentGroupsArray[i]), '_') + " ");
	    	
	    	if(sb.toString().trim().equals(""))
	    		sb = new StringBuilder("okAny");
	    	
	    	//System.out.println("limitationClasses:" + sb.toString());
	    	return sb.toString();
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    	return "";
    }
    
    public String[] getAllowedComponentsArray()
    {
        return allowedComponentsArray;
    }
    
    public void setAllowedComponentsArray(String[] allowedComponentsArray)
    {
        this.allowedComponentsArray = allowedComponentsArray;
    }

    public String getAllowedComponentsArrayAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        if(allowedComponentsArray != null)
        {
	        for(int i=0; i<allowedComponentsArray.length; i++)
	        {
	            if(i > 0)
	                sb.append("&");
	            
	            //sb.append("allowedComponentNames=" + URLEncoder.encode(allowedComponentsArray[i], "UTF-8"));
	            String encoding = CmsPropertyHandler.getURIEncoding();
	            
	            sb.append("allowedComponentNames=" + URLEncoder.encode(allowedComponentsArray[i], encoding));
	        }
        }
        else
            return null;
        
        return sb.toString();
    }

    public String[] getDisallowedComponentsArray()
    {
        return disallowedComponentsArray;
    }
    
    public void setDisallowedComponentsArray(String[] disallowedComponentsArray)
    {
        this.disallowedComponentsArray = disallowedComponentsArray;
    }

    public String getDisallowedComponentsArrayAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        if(disallowedComponentsArray != null)
        {
	        for(int i=0; i<disallowedComponentsArray.length; i++)
	        {
	            if(i > 0)
	                sb.append("&");
	            
	            String encoding = CmsPropertyHandler.getURIEncoding();
	            
	            sb.append("disallowedComponentNames=" + URLEncoder.encode(disallowedComponentsArray[i], encoding));
	        }
        }
        else
            return null;
        
        return sb.toString();
    }

    public String[] getAllowedComponentGroupsArray()
    {
        return allowedComponentGroupsArray;
    }
    
    public void setAllowedComponentGroupsArray(String[] allowedComponentGroupsArray)
    {
        this.allowedComponentGroupsArray = allowedComponentGroupsArray;
    }

    public String getAllowedComponentGroupsArrayAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        if(allowedComponentGroupsArray != null)
        {
	        for(int i=0; i<allowedComponentGroupsArray.length; i++)
	        {
	            if(i > 0)
	                sb.append("&");
	            
	            String encoding = CmsPropertyHandler.getURIEncoding();
	            sb.append("allowedComponentGroupNames=" + URLEncoder.encode(allowedComponentGroupsArray[i], encoding));
	        }
        }
        else
            return null;
        
        return sb.toString();
    }

	public String getAddComponentText()
	{
		return addComponentText;
	}

	public void setAddComponentText(String addComponentText)
	{
		this.addComponentText = addComponentText;
	}

	public String getAddComponentLinkHTML()
	{
		return addComponentLinkHTML;
	}

	public void setAddComponentLinkHTML(String addComponentLinkHTML)
	{
		this.addComponentLinkHTML = addComponentLinkHTML;
	}

	public int getAllowedNumberOfComponents()
	{
		return allowedNumberOfComponents;
	}

	public void setAllowedNumberOfComponents(int allowedNumberOfComponents)
	{
		this.allowedNumberOfComponents = allowedNumberOfComponents;
	}

}

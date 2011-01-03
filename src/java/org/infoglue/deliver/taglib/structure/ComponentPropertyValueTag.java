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

package org.infoglue.deliver.taglib.structure;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.applications.actions.InfoGlueComponent;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class ComponentPropertyValueTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private Integer siteNodeId;
	private String propertyName;
	private boolean useLanguageFallback = true;
	private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private boolean parse = false;
    private boolean parseToMap = false;
    private InfoGlueComponent component = null;
    
    public ComponentPropertyValueTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
	    try
	    {
	    	String propertyValue = null;
	    	
	        if(siteNodeId == null)
	        {
	        	if(component != null)
	        		propertyValue = getComponentLogic().getPropertyValue(propertyName, useLanguageFallback, false, false, false, false, component);
	        	else
	        		propertyValue = getComponentLogic().getPropertyValue(propertyName, useLanguageFallback, useInheritance, useRepositoryInheritance, useStructureInheritance);
	        }
	        else
	        {
	        	if(component != null)
	        		propertyValue = getComponentLogic().getPropertyValue(siteNodeId, propertyName, useLanguageFallback, false, component);	
	        	else
	        		propertyValue = getComponentLogic().getPropertyValue(siteNodeId, propertyName, useLanguageFallback, useInheritance);	
	        }
	        
	        if(parse && propertyValue != null && !propertyValue.equals(""))
	        {
	        	propertyValue = getController().getParsedText(propertyValue);
	        }
	        
	        if(parseToMap)
	        {
	        	Map orderedMap = new LinkedHashMap(); 
	        	if(propertyValue != null)
	        	{
		        	String separator = System.getProperty("line.separator");
		        	String[] lines = propertyValue.split(separator);
		        	for(int i=0; i<lines.length; i++)
		        	{
		        		String line = lines[i];
		        		String[] nameValue = line.split("=");
		        		if(nameValue.length == 2)
		        			orderedMap.put(nameValue[0], nameValue[1]);
		        	}
	        	}
	        	setResultAttribute(orderedMap);	        	
	        }
	        else
	        {
	        	setResultAttribute(propertyValue);
	        }
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    
	    parseToMap = false;
	    
		return EVAL_PAGE;
    }

    public void setSiteNodeId(String siteNodeId) throws JspException
    {
        this.siteNodeId = evaluateInteger("componentPropertyValue", "siteNodeId", siteNodeId);
    }

    public void setComponent(String component) throws JspException
    {
        this.component = (InfoGlueComponent)evaluate("componentPropertyValue", "component", component, InfoGlueComponent.class);
    }

    public void setPropertyName(String propertyName) throws JspException
	{
		this.propertyName = evaluateString("componentPropertyValue", "propertyName", propertyName);
	}
	
	public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }
	
    public void setUseRepositoryInheritance(boolean useRepositoryInheritance)
    {
        this.useRepositoryInheritance = useRepositoryInheritance;
    }

    public void setUseStructureInheritance(boolean useStructureInheritance)
    {
        this.useStructureInheritance = useStructureInheritance;
    }

    public void setParse(boolean parse)
    {
        this.parse = parse;
    }

    public void setParseToMap(boolean parseToMap)
    {
        this.parseToMap = parseToMap;
    }

}

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

package org.infoglue.deliver.taglib.content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class ChildContentsTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;
	private final static Logger logger = Logger.getLogger(ChildContentsTag.class);

	private Integer contentId;
	private String propertyName;
	private boolean useInheritance 	= true;
	private boolean useRepositoryInheritance = true;
    private boolean useStructureInheritance = true;
    private boolean searchRecursive = false;
	private String sortAttribute 	= "contentId";
	private String sortOrder		= "asc";
    private boolean includeFolders 	= false;
    private String matchingName		= null;
    private boolean returnOnlyFirst = false;
	
    public ChildContentsTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		List contents = null;
		
	    if(this.contentId != null)
	    	contents = this.getController().getChildContents(this.contentId, this.searchRecursive, this.sortAttribute, this.sortOrder, this.includeFolders);
        else if(this.propertyName != null)
        	contents = getComponentLogic().getChildContents(this.propertyName, this.useInheritance, this.searchRecursive, this.sortAttribute, this.sortOrder, this.includeFolders, useRepositoryInheritance, useStructureInheritance);
        else
            throw new JspException("You must state either propertyName or siteNodeId");
	    
	    if(contents != null && contents.size() > 0 && matchingName != null)
	    {
	    	try
	    	{
		    	List<ContentVO> matchingContents = new ArrayList<ContentVO>();
		    	Pattern p = Pattern.compile(matchingName);
		    	Iterator contentsIterator = contents.iterator();
		    	while(contentsIterator.hasNext())
		    	{
		    		ContentVO contentVO = (ContentVO)contentsIterator.next();
		    		if(p.matcher(contentVO.getName()).matches())
		    			matchingContents.add(contentVO);
		    	}
		    	contents = matchingContents;
	    	}
	    	catch (PatternSyntaxException ex)
	    	{
	    		logger.info("The given matchingName '" + this.matchingName + "' was not a valid regex-pattern. Message: " + ex.getMessage());
	    	}
	    }
	    
	    if(returnOnlyFirst && contents != null && contents.size() > 0)
	    	setResultAttribute(contents.get(0));
	    else
	    	setResultAttribute(contents);
        
	    contentId 				= null;
	    propertyName 			= null;
	    useInheritance 			= true;
	    useRepositoryInheritance = true;
	    useStructureInheritance = true;
	    searchRecursive 		= false;
		sortAttribute 			= "contentId";
		sortOrder				= "asc";
	    includeFolders 			= false;
	    matchingName 			= null;
	    returnOnlyFirst 		= false;
	    
	    return EVAL_PAGE;
    }

	public void setPropertyName(String propertyName) throws JspException
	{
        this.propertyName = evaluateString("childContents", "propertyName", propertyName);
	}
	
    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("childContents", "contentId", contentId);
    }
    
    public void setIncludeFolders(boolean includeFolders)
    {
        this.includeFolders = includeFolders;
    }
    
    public void setSearchRecursive(boolean searchRecursive)
    {
        this.searchRecursive = searchRecursive;
    }
    
    public void setSortAttribute(String sortAttribute) throws JspException
    {
        this.sortAttribute = evaluateString("childContents", "sortAttribute", sortAttribute);
    }
    
    public void setSortOrder(String sortOrder) throws JspException
    {
        this.sortOrder = evaluateString("childContents", "sortOrder", sortOrder);;
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

    public void setMatchingName(String matchingName) throws JspException
    {
        this.matchingName = evaluateString("childContents", "matchingName", matchingName);;
    }
    
    public void setReturnOnlyFirst(boolean returnOnlyFirst)
    {
        this.returnOnlyFirst = returnOnlyFirst;
    }

}

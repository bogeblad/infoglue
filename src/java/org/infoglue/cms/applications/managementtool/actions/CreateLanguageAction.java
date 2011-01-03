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

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action represents the CreateLanguage Usecase.
 */

public class CreateLanguageAction extends InfoGlueAbstractAction
{

    private Integer languageId;
    private String name;
    private String languageCode;
    private String charset;
   	private ConstraintExceptionBuffer ceb;
   	private LanguageVO languageVO;
  
  
  	public CreateLanguageAction()
	{
		this(new LanguageVO());
	}
	
	public CreateLanguageAction(LanguageVO languageVO)
	{
		this.languageVO = languageVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	
	
    public java.lang.String getName()
    {
    	if(this.name != null)
    		return this.name;
    		
        return this.languageVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
        try
    	{
        	this.languageVO.setName(name);
    	}
    	catch(ConstraintException ce)
    	{
    		this.name = name;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }
     
    public java.lang.String getLanguageCode()
    {
    	if(this.languageCode != null)
    		return this.languageCode;
    		
        return this.languageVO.getLanguageCode();
    }
        
    public void setLanguageCode(java.lang.String languageCode)
    {
        try
    	{
        	this.languageVO.setLanguageCode(languageCode);
    	}
    	catch(ConstraintException ce)
    	{
    		this.languageCode = languageCode;
    		this.ceb.add(new ConstraintExceptionBuffer(ce));
    	}
    }

	public java.lang.String getCharset()
	{
		return this.languageVO.getCharset();
	}
        
	public void setCharset(java.lang.String charset)
	{
		this.languageVO.setCharset(charset);
	}
      
    public String doExecute() throws Exception
    {
		ceb.add(this.languageVO.validate());
    	ceb.throwIfNotEmpty();				
    	
		LanguageVO languageVO = LanguageController.getController().create(this.languageVO);
	    
        return "success";
    }

    public String doInput() throws Exception
    {
        return "input";
    }
        
}

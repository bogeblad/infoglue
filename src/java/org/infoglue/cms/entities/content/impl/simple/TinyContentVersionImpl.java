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

package org.infoglue.cms.entities.content.impl.simple;

import org.infoglue.cms.entities.content.TinyContentVersion;
import org.infoglue.cms.entities.content.TinyContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

public class TinyContentVersionImpl implements TinyContentVersion
{
    private TinyContentVersionVO valueObject = new TinyContentVersionVO();
     
     
    /**
	 * @see org.infoglue.cms.entities.kernel.BaseEntity#getVO()
	 */
	public BaseEntityVO getVO() 
	{
		return (BaseEntityVO) getValueObject();
	}
	public void setVO(BaseEntityVO valueObject) 
	{
		setValueObject((TinyContentVersionVO) valueObject);
	}
 	public Integer getId() 
	{
		return getTinyContentVersionId();
	}
	
	public Object getIdAsObject()
	{
		return getId();
	}
	
    public TinyContentVersionVO getValueObject()
    {
        return this.valueObject;
    }
    
    public void setValueObject(TinyContentVersionVO valueObject)
    {
        this.valueObject = valueObject;
    }

 	public Integer getTinyContentVersionId()
 	{
 		return this.getTinyContentVersionId();	
 	}
   	
   	public Integer getLanguageId()
	{
		return this.getLanguageId();	
	}
	public String getTitle(){
		return this.getTitle();	
	}
	public void setTinyContentVersionId(Integer i)
	{
		this.setTinyContentVersionId(i);	
	}
	public void setLanguageId(Integer i)
	{
		this.setLanguageId(i);	
	}		
	public void setScore(double s)
	{
		this.setScore(s);	
	}
	public double getScore()
	{
		return this.getScore();	
	}
	public void setTitle(String s)
	{
		this.setTitle(s);	
	}
 	public void setScoreImg(String s)
 	{
 		this.setScoreImg(s);	
 	}
 	public String getScoreImg()
 	{
 		return this.getScoreImg();	
 	}
	public void setContentTitle(String t)
	{
		this.setContentTitle(t);
	}
	public void setRepositoryName(String r){this.setRepositoryName(r);}
	public void setRepositoryId(int i){this.setRepositoryId(i);}
	public String getContentTitle(){return this.getContentTitle();}
	public String getRepositoryName(){return this.getRepositoryName();}
	public int getRepositoryId(){return this.getRepositoryId();}
	public String getType(){return this.getType();}		
	public void setType(String t){this.setType(t);}	

 	
  }        

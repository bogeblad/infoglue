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

import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface TinyContentVersion extends IBaseEntity
{        
    public TinyContentVersionVO getValueObject();
    
    public void setValueObject(TinyContentVersionVO valueObject);

	public Integer getTinyContentVersionId();

	public Integer getLanguageId();

	public String getTitle();
	
	public double getScore();

	public String getScoreImg();
	
	public String getContentTitle();

	public String getRepositoryName();

	public int getRepositoryId();

	public void setTinyContentVersionId(Integer i);

	public void setLanguageId(Integer i);		

	public void setScore(double s);

	public void setTitle(String s);
	
	public void setScoreImg(String s);

	public void setContentTitle(String t);

	public void setRepositoryName(String r);

	public void setRepositoryId(int i);

	public String getType();

	public void setType(String t);

		
}

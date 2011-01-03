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

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class TinyContentVersionVO implements BaseEntityVO
{

	public Integer getId() 
	{
		return getTinyContentVersionId();
	}
	public ConstraintExceptionBuffer validate() 
	{ 
		return null;
	}
	

	Integer id;
	Integer languageId;	
	double score;
	String scoreImg;
	String title;
	String contentTitle;
	String repositoryName;
	int repositoryId;
	String type;

	//Getters
	public Integer getTinyContentVersionId(){return this.id;}
	public Integer getLanguageId(){return this.languageId;}
	public double getScore(){return this.score;}
	public String getScoreImg(){return this.scoreImg;}	
	public String getTitle(){return this.title;}
	public String getContentTitle(){return this.contentTitle;}
	public String getRepositoryName(){return this.repositoryName;}
	public int getRepositoryId(){return this.repositoryId;}
	public String getType(){return this.type;}		

	
	//Setters
	public void setTinyContentVersionId(Integer i){this.id=i;}
	public void setLanguageId(Integer i){this.languageId=i;}
	public void setScore(double s){this.score=s;}
	public void setScoreImg(String s){this.scoreImg=s;}
	public void setTitle(String t){this.title=t;}
	public void setContentTitle(String t){this.contentTitle = t;}
	public void setRepositoryName(String r){this.repositoryName = r;}
	public void setRepositoryId(int i){this.repositoryId = i;}
	public void setType(String t){this.type = t;}
}
        

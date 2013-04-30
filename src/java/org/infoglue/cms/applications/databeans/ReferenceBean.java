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
package org.infoglue.cms.applications.databeans;

import java.util.ArrayList;
import java.util.List;

/**
 * This bean is really just to give the reference-screens a nice datastructure.
 * 
 * @author mattias
 */

public class ReferenceBean
{
    private String name;
    private String path;
    private String contactPersonEmail;
    //private List registryVOList;
    private Object referencingCompletingObject;
    private List<ReferenceVersionBean> versions = new ArrayList<ReferenceVersionBean>();
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

    public Object getReferencingCompletingObject()
    {
        return referencingCompletingObject;
    }
    
    public void setReferencingCompletingObject(Object referencingCompletingObject)
    {
        this.referencingCompletingObject = referencingCompletingObject;
    }
    
    /*
    public List getRegistryVOList()
    {
        return registryVOList;
    }
    
    public void setRegistryVOList(List registryVOList)
    {
        this.registryVOList = registryVOList;
    }
    */
    public List<ReferenceVersionBean> getVersions()
    {
        return versions;
    }

	public String getContactPersonEmail()
	{
		return contactPersonEmail;
	}

	public void setContactPersonEmail(String contactPersonEmail)
	{
		this.contactPersonEmail = contactPersonEmail;
	}
}

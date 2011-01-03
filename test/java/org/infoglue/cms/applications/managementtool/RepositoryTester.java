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

package org.infoglue.cms.applications.managementtool;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author Mattias Bogeblad
 */

public class RepositoryTester extends TestCase
{
    private RepositoryVO newRepositoryVO = null;

	public void testCreate()
	{
	    CmsPropertyHandler.setApplicationName("cms");

	    try
	    {
	        RepositoryVO repositoryVO = new RepositoryVO();
		    repositoryVO.setName("Repository_" + Math.random());
		    repositoryVO.setDescription("The testcase made a new repository...");
		    repositoryVO.setDnsName("");
		    newRepositoryVO = RepositoryController.getController().create(repositoryVO);
		    assertNotNull(newRepositoryVO.getId());
		    assertNotNull(newRepositoryVO);
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	        fail(e.getMessage());
	    }

	    try
	    {
	        this.newRepositoryVO.setName(this.newRepositoryVO.getName() + "_updated");
	        this.newRepositoryVO.setDescription("Updated....");
	        this.newRepositoryVO.setDnsName("http://localhost:8080");
	        RepositoryController.getController().update(this.newRepositoryVO);
		}
	    catch(Exception e)
	    {
	        e.printStackTrace();
	        fail(e.getMessage());
	    }

	    try
	    {
	        RepositoryController.getController().delete(this.newRepositoryVO, "testcaseUser", new InfoGluePrincipal("test-user", "first", "last", "email", Collections.singletonList(new InfoGlueRole("cmsUser", "test description", null)), new ArrayList(), false, null));
		}
	    catch(Exception e)
	    {
	        e.printStackTrace();
	        fail(e.getMessage());
	    }
	}

	/*
	 * Class under test for RepositoryVO update(RepositoryVO, String[])
	 */
	public void testUpdateRepositoryVOStringArray() {
	}

	public void testGetRepositoryWithId() {
	}

	public void testGetRepositoryVOWithId() {
	}

	public void testGetRepositoryVOList() {
	}

	public void testGetAuthorizedRepositoryVOList() {
	}

	public void testGetFirstRepositoryVO() {
	}

	public void testDeleteRepository() {
	}

	public void testUpdateRepositoryRoles() {
	}

	public void testGetIsAccessApproved() {
	}

	public void testGetAssignedRoles() {
	}

}

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

package org.infoglue.cms.applications.managementtool.actions.deployment;

import java.util.HashMap;
import java.util.Map;

public class DeploymentCompareBean
{
	private Object localVersion;
	private Object remoteVersion;
	private boolean repositoryMissing;
	
	public boolean isRepositoryMissing() 
	{
		return repositoryMissing;
	}

	public void setRepositoryMissing(boolean repositoryMissing) 
	{
		this.repositoryMissing = repositoryMissing;
	}

	public Object getLocalVersion()
	{
		return localVersion;
	}
	
	public void setLocalVersion(Object localVersion)
	{
		this.localVersion = localVersion;
	}
	
	public Object getRemoteVersion()
	{
		return remoteVersion;
	}
	
	public void setRemoteVersion(Object remoteVersion)
	{
		this.remoteVersion = remoteVersion;
	}

}

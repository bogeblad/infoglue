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

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;

/**
 * This class implements the action class for generating passwords. Also let's an admin change users passwords.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewPasswordToolAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

    private String password;
    private String encryptedPassword;

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doInput() throws Exception
    {
        return "input";
    }

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws Exception
    {
		byte[] encryptedPassRaw = DigestUtils.sha(this.password);
		String encryptedPass = new String(new Base64().encode(encryptedPassRaw), "ASCII");
		encryptedPassword = encryptedPass;

        return "success";
    }

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getEncryptedPassword()
	{
		return encryptedPassword;
	}


}

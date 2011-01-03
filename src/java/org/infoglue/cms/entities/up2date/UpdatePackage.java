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

/*
 * Created on 2003-apr-04
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.infoglue.cms.entities.up2date;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


/**
 * @author ss
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdatePackage  {
	private String packageId;
	private String url;
	private String detailsUrl;
	private String description;
	private String title;
	private Integer binarySize;
	private String binaryUrl;


	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return
	 */
	public String getPackageId() {
		return packageId;
	}

	/**
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	public String getDecodedUrl() {
		String ret = "";
		try {
			ret = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getDecodedDetailsUrl() {
		String ret = "";
		try {
			ret = URLDecoder.decode(detailsUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getDecodedBinaryUrl() {
		String ret = "";
		try {
			ret = URLDecoder.decode(binaryUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @param string
	 */
	public void setPackageId(String string) {
		packageId = string;
	}

	/**
	 * @param string
	 */
	public void setUrl(String string) {
		url = string;
	}

	/**
	 * @return
	 */
	public String getDetailsUrl() {
		return detailsUrl;
	}

	/**
	 * @param string
	 */
	public void setDetailsUrl(String string) {
		detailsUrl = string;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}

    public Integer getBinarySize()
    {
        return binarySize;
    }
    public void setBinarySize(Integer binarySize)
    {
        this.binarySize = binarySize;
    }
    public String getBinaryUrl()
    {
        return binaryUrl;
    }
    public void setBinaryUrl(String binaryUrl)
    {
        this.binaryUrl = binaryUrl;
    }
}

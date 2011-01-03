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

package org.infoglue.deliver.applications.databeans;


public class ConvertedDocumentBean 
{
	private String pdfFileUrl;
	private String htmlFileUrl;
	private String odtFileUrl;
	private String tocString;
	
	public String getHtmlFileUrl() 
	{
		return htmlFileUrl;
	}
	public void setHtmlFileUrl(String htmlFileUrl) 
	{
		this.htmlFileUrl = htmlFileUrl;
	}
	public String getPdfFileUrl() 
	{
		return pdfFileUrl;
	}
	public void setPdfFileUrl(String pdfFileUrl) 
	{
		this.pdfFileUrl = pdfFileUrl;
	}
	public String getTocString() 
	{
		return tocString;
	}
	public void setTocString(String tocString) 
	{
		this.tocString = tocString;
	}
	
	public void setOdtFileUrl(String odtFileUrl)
	{
		this.odtFileUrl = odtFileUrl;
	}
}

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

/**
 * @author Stefan Sik
 * @version 0.1
 * @since 1.3
 * 
 * TODO: Quick due to sudden changes. Refactor as soon as possible
 * 
 * @deprecated
 */

package org.infoglue.cms.applications.contenttool.actions;

import java.io.PrintWriter;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.entities.content.DigitalAssetVO;

public class SimpleContentXmlServiceAction extends InfoGlueAbstractAction 
{	
	private static final long serialVersionUID = 1L;
	
	private String serviceRequest = null;
	private Integer contentVersionId = null;
	private Integer languageId = null;
	private Integer contentId = null;
	private Integer digitalAssetId = null;
	private String digitalAssetKey = null;

	public SimpleContentXmlServiceAction() {
	}

	public String getDigitalAssetInfo() throws Exception {
		String ret = "";
		DigitalAssetVO digitalAssetVO = null;

		if (digitalAssetId != null) {
			digitalAssetVO = DigitalAssetController
					.getDigitalAssetVOWithId(digitalAssetId);
		} else {
			digitalAssetVO = DigitalAssetController.getDigitalAssetVO(
					contentId, languageId, digitalAssetKey, true);
		}

		ret = "<digitalAssetInfo>"
				+ "<assetURL>"
				+ DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getId()) 
				+ "</assetURL>" 
				+ "<assetId>"
				+ digitalAssetVO.getId() 
				+ "</assetId>" 
				+ "</digitalAssetInfo>";

		return ret;
	}

	public String doExecute() throws Exception {
		String resp;
		try {
			resp = getDigitalAssetInfo();
		} catch (Exception e) {
			resp = "<exception>" + e.toString() + "</exception>";
		}

		getResponse().setContentType("text/xml");
		getResponse().setContentLength(resp.length());
		PrintWriter out = getResponse().getWriter();
		out.println(resp);

		return null;
	}

	public java.lang.Integer getContentVersionId() {
		return this.contentVersionId;
	}

	public void setContentVersionId(java.lang.Integer contentVersionId) {
		this.contentVersionId = contentVersionId;
	}

	public Integer getDigitalAssetId() {
		return digitalAssetId;
	}

	public void setDigitalAssetId(Integer digitalAssetId) {
		this.digitalAssetId = digitalAssetId;
	}

	public String getServiceRequest() {
		return serviceRequest;
	}

	public void setServiceRequest(String serviceRequest) {
		this.serviceRequest = serviceRequest;
	}

	public void setDigitalAssetKey(String digitalAssetKey) {
		this.digitalAssetKey = digitalAssetKey;
	}

	public String getDigitalAssetKey() {
		return digitalAssetKey;
	}

	/**
	 * @return Returns the contentId.
	 */
	public Integer getContentId() {
		return contentId;
	}

	/**
	 * @param contentId
	 *            The contentId to set.
	 */
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	/**
	 * @return Returns the languageId.
	 */
	public Integer getLanguageId() {
		return languageId;
	}

	/**
	 * @param languageId
	 *            The languageId to set.
	 */
	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}
}
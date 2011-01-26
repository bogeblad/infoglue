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
 * @since 1.4
 */

package org.infoglue.cms.applications.contenttool.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.treeservice.ss.ContentNodeSupplier;
import org.infoglue.deliver.util.CompressionHelper;

import com.frovi.ss.Tree.INodeSupplier;
import com.thoughtworks.xstream.core.util.Base64Encoder;

public class SimpleContentXmlAction extends SimpleXmlServiceAction
{
    private final static Logger logger = Logger.getLogger(SimpleContentXmlAction.class.getName());

	private static final long serialVersionUID = 1L;
	
    private static String TYPE_FOLDER = "ContentFolder";
    private static String TYPE_ITEM = "ContentItem";
	private String digitalAssetKey;
	private Integer digitalAssetId;
	private Integer languageId;
	private boolean enableCompression = false;

	public INodeSupplier getNodeSupplier() throws SystemException
	{
		ContentNodeSupplier sup =  new ContentNodeSupplier(getRepositoryId(), this.getInfoGluePrincipal());
		sup.setShowLeafs(showLeafs.compareTo("yes")==0);		
		sup.setAllowedContentTypeIds(allowedContentTypeIds);
		return sup;
	}
	
	public String doDigitalAssets() throws Exception
	{
		String ret = "";
		DigitalAssetVO digitalAssetVO = null;

		if (digitalAssetId != null) {
			digitalAssetVO = DigitalAssetController
					.getDigitalAssetVOWithId(digitalAssetId);
		} else {
			digitalAssetVO = DigitalAssetController.getDigitalAssetVO(
					parent, languageId, digitalAssetKey, true);
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
	
	
	
    public ContentVersionVO getLatestContentVersionVO(Integer contentId, Integer languageId)
	{
		ContentVersionVO contentVersionVO = null;
		try
		{
			contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
		}
		catch(Exception e)
		{
			logger.error("An error occurred when we tried to get the latest version for the content:" + e.getMessage(), e);
		}
		
		return contentVersionVO;
	}

    public Element getContentElement(Integer contentId) throws Bug, Exception
    {
		ContentController contentController = ContentController.getContentController();
        ContentVO vo = contentController.getContentVOWithId(contentId);
        return getContentElement(vo);
    }
    
    public Element getContentElement(ContentVO vo) throws Bug, Exception
    {
        Element elm = DocumentHelper.createElement("content");
        
		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
        ContentVersionVO activeVersion = contentVersionController.getLatestActiveContentVersionVO(vo.getContentId(), LanguageController.getController().getMasterLanguage(vo.getRepositoryId()).getLanguageId());
        if(activeVersion!=null)
        {
        	elm.addAttribute("id", "" + vo.getContentId());
        	elm.addAttribute("creatorName", "" + vo.getCreatorName());
        	elm.addAttribute("name", "" + vo.getName());
        	elm.addAttribute("typedefid", "" + vo.getContentTypeDefinitionId());
        	elm.addAttribute("expiredatetime", "" + vo.getExpireDateTime().getTime());
        	elm.addAttribute("publishdatetime", "" + vo.getPublishDateTime().getTime());
        	elm.addAttribute("isbranch", "" + vo.getIsBranch());
            elm.addAttribute("activeVersion", "" + activeVersion.getContentVersionId());
            elm.addAttribute("activeVersionStateId", "" + activeVersion.getStateId());
            elm.addAttribute("activeVersionModifier", "" + activeVersion.getVersionModifier());
            Element versionsElement = DocumentHelper.createElement("versions");
            elm.add(versionsElement);
            
            List<ContentVersionVO> versions = contentVersionController.getContentVersionVOWithParent(vo.getContentId());
            for(ContentVersionVO version: versions)
            {
            	Element contentVersionElement = DocumentHelper.createElement("contentVersion");
            	contentVersionElement.add(getContentVersionHeadElement(version));
            	versionsElement.add(contentVersionElement);
            }
            
        }
        
        return elm;
    }

    public Element getPlainContentElement(ContentVO vo) throws Bug, Exception
    {
        Element elm = DocumentHelper.createElement("content");
        
    	elm.addAttribute("id", "" + vo.getContentId());
    	elm.addAttribute("creatorName", "" + vo.getCreatorName());
    	elm.addAttribute("name", "" + vo.getName());
    	elm.addAttribute("typedefid", "" + vo.getContentTypeDefinitionId());
    	elm.addAttribute("expiredatetime", "" + vo.getExpireDateTime().getTime());
    	elm.addAttribute("publishdatetime", "" + vo.getPublishDateTime().getTime());
    	elm.addAttribute("isbranch", "" + vo.getIsBranch());
        
        return elm;
    }

    public Element getContentVersionElement(Integer contentVersionId) throws SystemException, Bug, UnsupportedEncodingException
    {
		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
        ContentVersionVO vo = contentVersionController.getContentVersionVOWithId(contentVersionId);
        if(vo.getLanguageName() == null || vo.getLanguageName().equals("") && vo.getLanguageId() != null)
        {
        	try
        	{
        		vo.setLanguageName(LanguageController.getController().getLanguageVOWithId(vo.getLanguageId()).getLanguageCode());
        	}
        	catch (Exception e) 
        	{
        		logger.error("No language found:" + e.getMessage(), e);
			}
        }
        return getContentVersionElement(vo);
    }
    
    public Element getContentVersionHeadElement(Integer contentVersionId) throws SystemException, Bug, UnsupportedEncodingException
    {
		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
        ContentVersionVO vo = contentVersionController.getContentVersionVOWithId(contentVersionId);
        if(vo.getLanguageName() == null || vo.getLanguageName().equals("") && vo.getLanguageId() != null)
        {
        	try
        	{
        		vo.setLanguageName(LanguageController.getController().getLanguageVOWithId(vo.getLanguageId()).getLanguageCode());
        	}
        	catch (Exception e) 
        	{
        		logger.error("No language found:" + e.getMessage(), e);
			}
        }
        
        return getContentVersionHeadElement(vo);
    }
    
    public Element getContentVersionElement(ContentVersionVO vo) throws SystemException, Bug, UnsupportedEncodingException
    {
        Element element = DocumentHelper.createElement("contentVersion");
        element.add(getContentVersionHeadElement(vo));
        element.add(getContentVersionValueElement(vo));
        return element;
    }

    public Element getContentVersionHeadElement(ContentVersionVO vo) throws SystemException, Bug, UnsupportedEncodingException
    {
        Element head = DocumentHelper.createElement("head");
        head.addAttribute("id", "" + vo.getContentVersionId());
	    head.addAttribute("languageId", "" + vo.getLanguageId());
	    head.addAttribute("contentId", "" + vo.getContentId());
	    head.addAttribute("languageName", vo.getLanguageName());
	    head.addAttribute("isActive", "" + vo.getIsActive());
        head.addAttribute("mod", "" + vo.getModifiedDateTime().getTime());
        head.addAttribute("activeVersionModifier", "" + vo.getVersionModifier());
        head.addAttribute("activeVersionStateId", "" + vo.getStateId());
        
        return head;
    }
    public Element getContentVersionValueElement(ContentVersionVO vo) throws SystemException, Bug, UnsupportedEncodingException
    {
        Element value = DocumentHelper.createElement("value");
        if(enableCompression )
        {
            Base64Encoder encoder = new Base64Encoder();
            CompressionHelper zip = new CompressionHelper();
            byte[] val = zip.compress(vo.getVersionValue());
            value.addCDATA(encoder.encode(val));
        }
        else
        {
        	value.addCDATA(URLEncoder.encode(vo.getVersionValue(),"UTF-8"));
        }
        
        return value;
    }
    
    
    /*
     * Returns document for a single contentVersion (parent)
     */
    public String doContentVersion() throws Exception
	{
        Document doc = DocumentHelper.createDocument();
        doc.add(getContentVersionElement(parent));
	    return out(getFormattedDocument(doc));
	}

    public String doContent() throws Exception
	{
        Document doc = DocumentHelper.createDocument();
        doc.add(getContentElement(parent));
	    return out(getFormattedDocument(doc));
	}
    
    public String doRootContent() throws Exception
	{
        Document doc = DocumentHelper.createDocument();
        ContentVO rootContent = ContentController.getContentController().getRootContentVO(repositoryId, getInfoGluePrincipal().getName(), true);
        doc.add(getPlainContentElement(rootContent));
	    return out(getFormattedDocument(doc));
	}

    public String doMasterLanguage() throws Exception
	{
        LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryId);
	    return out("" + masterLanguageVO.getId());
	}

    /*
     * Returns head only for a single contentVersion (parent)
     */
    public String doContentVersionHead() throws Exception
	{
        Document doc = DocumentHelper.createDocument();
        Element element = DocumentHelper.createElement("contentVersion");

        element.add(getContentVersionHeadElement(parent));
        element.add(DocumentHelper.createElement("value"));
        doc.add(element);
	    return out(getFormattedDocument(doc));
	}
    
    /*
     * Returns all contentVersions for a given content (parent)
     */
    public String doContentVersions() throws Exception
	{
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("contentVersions");
        Collection availableLanguages = ContentController.getContentController().getRepositoryLanguages(parent);
        for(Iterator i=availableLanguages.iterator();i.hasNext();)
        {
        	LanguageVO lvo = (LanguageVO) i.next();
        	ContentVersionVO vo = getLatestContentVersionVO(parent, lvo.getLanguageId());
        	if(vo!=null)
        		root.add(getContentVersionElement(vo));
        }
        
		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
        return out(getFormattedDocument(doc));
	}

	/* (non-Javadoc)
	 * @see org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction#getRootEntityVO(java.lang.Integer, org.infoglue.cms.security.InfoGluePrincipal)
	 */
	protected BaseEntityVO getRootEntityVO(Integer repositoryId, InfoGluePrincipal principal) throws ConstraintException, SystemException {
		return ContentControllerProxy.getController().getRootContentVO(repositoryId, principal.getName());
	}

	public boolean isEnableCompression() {
		return enableCompression;
	}

	public void setEnableCompression(boolean enableCompression) {
		this.enableCompression = enableCompression;
	}
	
}

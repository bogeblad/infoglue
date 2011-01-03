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
 * @since 1.3
 */

package org.infoglue.cms.applications.contenttool.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.TransactionHistoryVO;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.treeservice.ss.ContentNodeSupplier;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.INodeSupplier;

public class ContentTreeXMLAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ContentTreeXMLAction.class.getName());

	private static final long serialVersionUID = 3321168219082615063L;

	private static String TYPE_FOLDER = "ContentFolder";
    private static String TYPE_ITEM = "ContentItem";
    private static String TYPE_REPOSITORY = "Repository";
	private String showLeafs = "yes";
	private Integer parent = null;
	private Integer repositoryId = null;
	private String urlArgSeparator = "&";
	private String action = "";
	private boolean createAction = false;
	private boolean useTemplate = false;
	private String[] allowedContentTypeIds = null;

	public INodeSupplier getNodeSupplier() throws SystemException
	{
		return new ContentNodeSupplier(getRepositoryId(), this.getInfoGluePrincipal());
	}
	
	public List getContentTypeDefinitions() throws Exception
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
	}      
	
	private String makeAction(BaseNode node) throws UnsupportedEncodingException
	{
		String action = "javascript:onTreeItemClick(this,";
		action+="'" + node.getId() + "','" + repositoryId + "','" + URLEncoder.encode(node.getTitle(),"UTF-8") + "');";
        return action;
	}
	
	private String getFormattedDocument(Document doc)
	{
	    return getFormattedDocument(doc, true);
	}
	
	private String getFormattedDocument(Document doc, boolean compact)
	{
	    OutputFormat format = compact ? OutputFormat.createCompactFormat() : OutputFormat.createPrettyPrint(); 
		// format.setEncoding("iso-8859-1");
		format.setEncoding("UTF-8");
		format.setExpandEmptyElements(false);
		StringWriter stringWriter = new StringWriter();
		XMLWriter writer = new XMLWriter(stringWriter, format);
		try
        {
            writer.write(doc);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
		return stringWriter.toString();
	}
	
	public String doDigitalAssets() throws Exception
	{
		/*String ret = "";
		DigitalAssetVO digitalAssetVO = null;

		if (parent != null) {
			digitalAssetVO = DigitalAssetController
					.getDigitalAssetVOWithId(parent);
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

		return ret;*/
	    return null;
	}
	
	
    /*
     * Returns all Languages for a given repository (repositoryId)
     */
    public String doLanguage() throws Exception
	{
        return null;
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

    public Element getContentVersionElement(Integer contentVersionId) throws SystemException, Bug, UnsupportedEncodingException
    {
		ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
        ContentVersionVO vo = contentVersionController.getContentVersionVOWithId(contentVersionId);
        return getContentVersionElement(vo);
    }
    
    public Element getContentVersionElement(ContentVersionVO vo) throws SystemException, Bug, UnsupportedEncodingException
    {
        Element element = DocumentHelper.createElement("contentVersion");
        Element head = DocumentHelper.createElement("head");
        Element value = DocumentHelper.createElement("value");

        head.addAttribute("id", "" + vo.getContentVersionId());
	    head.addAttribute("languageId", "" + vo.getLanguageId());
	    head.addAttribute("languageName", vo.getLanguageName());
	    head.addAttribute("isActive", "" + vo.getIsActive());

	    TransactionHistoryController transactionHistoryController = TransactionHistoryController.getController();
        TransactionHistoryVO transactionHistoryVO = transactionHistoryController.getLatestTransactionHistoryVOForEntity(ContentVersionImpl.class, vo.getContentVersionId());
	    if(transactionHistoryVO!=null)
	        head.addAttribute("mod", formatDate(transactionHistoryVO.getTransactionDateTime()));
        // head.addAttribute("mod", formatDate(vo.getModifiedDateTime()));
        value.addCDATA(URLEncoder.encode(vo.getVersionValue(),"UTF-8"));
        element.add(head);
        element.add(value);
        return element;
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
    
    /*
     * Returns all contentTypeDefinitions
     */
    public String doContentTypeDefinitions() throws Exception
	{
    	List contentTypeDefinitions = getContentTypeDefinitions();
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("definitions");
	    TransactionHistoryController transactionHistoryController = TransactionHistoryController.getController();
    	
    	for(Iterator i=contentTypeDefinitions.iterator();i.hasNext();)
    	{
    		ContentTypeDefinitionVO vo = (ContentTypeDefinitionVO) i.next();
    		if(vo.getType().compareTo(ContentTypeDefinitionVO.CONTENT)==0)
    		{
    		    TransactionHistoryVO transactionHistoryVO = transactionHistoryController.getLatestTransactionHistoryVOForEntity(ContentTypeDefinitionImpl.class, vo.getContentTypeDefinitionId());
    		    
	    		Element definition = DocumentHelper.createElement("definition");
	    		definition
					.addAttribute("id", "" + vo.getContentTypeDefinitionId())
					.addAttribute("type", "" + vo.getType())
					.addAttribute("name", vo.getName())
				;
	    		
	    		if(transactionHistoryVO!=null)
	    		    definition.addAttribute("mod", formatDate(transactionHistoryVO.getTransactionDateTime()));
	    		
	    		Element schemaValue = definition.addElement("schemaValue");
	    		schemaValue.addCDATA(vo.getSchemaValue());
	    		root.add(definition);
    		}
    	}
		return out(getFormattedDocument(doc));
    	
	}
	
    private String formatDate(Date date)
    {
        return "" + date;
    }

    /*
     * Main action, returns the content tree
     */
    public String doExecute() throws Exception
    {
        if (useTemplate) return "success";
        
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("tree");
        
    	INodeSupplier sup;
		// VisualFormatter vf = new VisualFormatter();
	    // TransactionHistoryController transactionHistoryController = TransactionHistoryController.getController();

    	if(repositoryId == null)
    	{
    	    List repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
    	    for(Iterator i=repositories.iterator();i.hasNext();)
    	    {
    	        RepositoryVO r = (RepositoryVO) i.next();
    			ContentVO contentVO = ContentControllerProxy.getController().getRootContentVO(r.getId(), this.getInfoGluePrincipal().getName());
    	        
    			String src= action + "?repositoryId=" + r.getId() + urlArgSeparator + "parent=" + contentVO.getId();
				if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
				if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
				String allowedContentTypeIdsUrlEncodedString = getAllowedContentTypeIdsAsUrlEncodedString();
    			if(allowedContentTypeIdsUrlEncodedString.length()>0 && src.length() >0) src += urlArgSeparator + allowedContentTypeIdsUrlEncodedString;
    	        String text=r.getName();
    	        Element element = root.addElement("tree");
    	        element
	        	.addAttribute("id", "" + r.getId())
    	        	.addAttribute("repositoryId", "" + r.getId())
    	        	.addAttribute("text", text)
    	        	.addAttribute("src", src)
    	        	.addAttribute("type", TYPE_REPOSITORY);
    	    }
    	    out(getFormattedDocument(doc));
    		return null;
    	}
    	
    	sup = getNodeSupplier();
		((ContentNodeSupplier) sup).setShowLeafs(showLeafs.compareTo("yes")==0);
		((ContentNodeSupplier) sup).setAllowedContentTypeIds(allowedContentTypeIds);
    	
    	if(parent == null)
    	{
    		BaseNode node = sup.getRootNode();
			String text = node.getTitle();
	        String type = TYPE_FOLDER;
			String src = action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + node.getId();
			if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
			if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
			String allowedContentTypeIdsUrlEncodedString = getAllowedContentTypeIdsAsUrlEncodedString();
			if(allowedContentTypeIdsUrlEncodedString.length()>0 && src.length() >0) src += urlArgSeparator + allowedContentTypeIdsUrlEncodedString;
	        
			
	        Element elm = root.addElement("tree");
	        elm
	        	.addAttribute("id", "" + node.getId())
	        	.addAttribute("repositoryId", "" + repositoryId)
	        	.addAttribute("text", text)
	        	.addAttribute("src", src)
	        	.addAttribute("type", type);
			
    	    out(getFormattedDocument(doc));
    		return null;
    	}
    	
    	if(parent.intValue() > -1)
    	{
			Collection containerNodes = sup.getChildContainerNodes(parent);
			Collection childNodes = sup.getChildLeafNodes(parent);
			
			ContentController contentController = ContentController.getContentController();
			ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();

			Iterator it = containerNodes.iterator();
			while (it.hasNext())
			{
				BaseNode theNode = (BaseNode) it.next();
				if (theNode.isContainer() && sup.hasChildren())
				{
					theNode.setChildren(sup.hasChildren(theNode.getId()));
				}
				
				String src = theNode.hasChildren() ? action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + theNode.getId(): "";
				if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
				if(createAction && src.length() >0) src += urlArgSeparator + "showLeafs=" + showLeafs;
				if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
				String allowedContentTypeIdsUrlEncodedString = getAllowedContentTypeIdsAsUrlEncodedString();
    			if(allowedContentTypeIdsUrlEncodedString.length()>0 && src.length() >0) src += urlArgSeparator + allowedContentTypeIdsUrlEncodedString;
    	        
		        Element elm = root.addElement("tree");
		        elm
		        	.addAttribute("id", "" + theNode.getId())
		        	.addAttribute("parent", "" + parent)
		        	.addAttribute("repositoryId", "" + repositoryId)
		        	.addAttribute("text", theNode.getTitle())
		        	.addAttribute("src", src)
		        	.addAttribute("type", TYPE_FOLDER);
		        
		        
		        if(createAction) elm.addAttribute("action", makeAction(theNode));
			}
			
			it = childNodes.iterator();
			while (it.hasNext())
			{
				BaseNode theNode = (BaseNode) it.next();
				String text = theNode.getTitle();
				String action = makeAction(theNode);
		        String type = TYPE_ITEM;
		        Element elm = root.addElement("tree");
		        elm
		        	.addAttribute("id", "" + theNode.getId())
		        	.addAttribute("parent", "" + parent)
		        	.addAttribute("repositoryId", "" + repositoryId)
		        	.addAttribute("text", text)
		        	.addAttribute("type", type)
				;
		        if(createAction) 
		        	elm.addAttribute("action", action);
		        else
		        {
			        ContentVersionVO activeVersion = contentVersionController.getLatestActiveContentVersionVO(theNode.getId(), LanguageController.getController().getMasterLanguage(repositoryId).getLanguageId());
			        if(activeVersion!=null && !useTemplate)
			            elm.addAttribute("activeVersion", "" + activeVersion.getContentVersionId());
		        }
		        
		        if(!useTemplate)
		        	elm.addAttribute("contentTypeId","" + contentController.getContentTypeDefinition(theNode.getId()).getContentTypeDefinitionId());
			}
			
    	    out(getFormattedDocument(doc));
    		return null;
    	}
    	
    	return null;
    }

    private String out(String string) throws IOException
    {
		getResponse().setContentType("text/xml");
		getResponse().setContentLength(string.length());
		PrintWriter out = getResponse().getWriter();
		out.println(string);
		return null;
    }

	public Integer getParent() 
	{
		return parent;
	}

	public void setParent(Integer integer) 
	{
		parent = integer;
	}

	public Integer getRepositoryId() 
	{
		return repositoryId;
	}

	public void setRepositoryId(Integer integer) 
	{
		repositoryId = integer;
	}

    public boolean isCreateAction()
    {
        return createAction;
    }
    
    public void setCreateAction(boolean createAction)
    {
        this.createAction = createAction;
    }
    
    public boolean isUseTemplate()
    {
        return useTemplate;
    }
    
    public void setUseTemplate(boolean useTemplate)
    {
        this.useTemplate = useTemplate;
    }
    
    public String getAction()
    {
        return action;
    }
    
    public void setAction(String action)
    {
        this.action = action;
    }
	
    public String getShowLeafs() 
    {
		return showLeafs;
	}
	
	public void setShowLeafs(String showLeafs) 
	{
		this.showLeafs = showLeafs;
	}
	
    public String[] getAllowedContentTypeIds()
    {
        return allowedContentTypeIds;
    }
    
    public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
    {
        this.allowedContentTypeIds = allowedContentTypeIds;
    }
    
    public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<allowedContentTypeIds.length; i++)
        {
            if(i > 0)
                sb.append("&");
            
            sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
        }
        
        return sb.toString();
    }

}

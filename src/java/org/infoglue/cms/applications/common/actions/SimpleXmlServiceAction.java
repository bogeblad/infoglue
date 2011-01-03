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

package org.infoglue.cms.applications.common.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.TransactionHistoryVO;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.XMLNotificationWriter;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.INodeSupplier;

public abstract class SimpleXmlServiceAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(SimpleXmlServiceAction.class.getName());

    private static final String protectedPropertyFragments = "password,administrator,authorizer,authenticator,masterserver,slaveserver,log";
    
    protected static final String SERVICEREVISION = "$Revision: 1.26 $"; 
	protected static String ENCODING = "UTF-8";
    protected static String TYPE_FOLDER = "Folder";
    protected static String TYPE_ITEM = "Item";
    protected static String TYPE_REPOSITORY = "Repository";
    protected String showLeafs = "yes";
    protected Integer parent = null;
    protected Integer repositoryId = null;
    protected String urlArgSeparator = "&";
    protected String action = "";
    protected boolean createAction = false;
    protected boolean useTemplate = false;
    protected VisualFormatter formatter = new VisualFormatter();
	protected String[] allowedContentTypeIds = null;
	
	/*
	 * 
	 * Experimental
	 *
	 */
	protected static Map changeNotificationBuffer = new HashMap();


	public abstract INodeSupplier getNodeSupplier() throws SystemException;
	
	protected abstract BaseEntityVO getRootEntityVO(Integer repositoryId, InfoGluePrincipal principal) throws ConstraintException, SystemException;
	
	public List getContentTypeDefinitions() throws Exception
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
	}      
	
	public String encode(String text)
	{
		return text;
	}
	
	protected String makeAction(BaseNode node) throws UnsupportedEncodingException
	{
		String action = "javascript:onTreeItemClick(this,";
		//action+="'" + node.getId() + "','" + repositoryId + "','" + URLEncoder.encode(node.getTitle(),ENCODING) + "');";
		//action+="'" + node.getId() + "','" + repositoryId + "','" + new VisualFormatter().escapeForAdvancedJavascripts(node.getTitle()) + "');";
		action+="'" + node.getId() + "','" + repositoryId + "','" + new VisualFormatter().escapeForAdvancedJavascripts(node.getTitle()) + "');";
        return action;
	}
	
	protected String getFormattedDocument(Document doc)
	{
	    return getFormattedDocument(doc, true, false);
	}
	
	protected String getFormattedDocument(Document doc, boolean compact, boolean supressDecl)
	{
	    OutputFormat format = compact ? OutputFormat.createCompactFormat() : OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(supressDecl);
		format.setEncoding(ENCODING);
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
	
    protected String out(String string) throws IOException
    {
		getResponse().setContentType("text/xml; charset=" + ENCODING);
    	getResponse().setHeader("Cache-Control","no-cache"); 
    	getResponse().setHeader("Pragma","no-cache");
    	getResponse().setDateHeader ("Expires", 0);

		PrintWriter out = getResponse().getWriter();
		out.println(string);
		return NONE;
    }
	
    /*
     * Returns all Languages for a given repository (repositoryId)
     */
    public String doLanguage() throws Exception
	{
        return null;
	}
    
    public String doApplicationSettings() throws Exception
    {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("applicationSettings");
        Properties props = CmsPropertyHandler.getProperties();
        for(Iterator i = props.keySet().iterator(); i.hasNext();)
        {
            String key = (String) i.next();
        	String elmKey = key;
            if(key.matches("^\\d.*")) elmKey = "_" + key;
            String value = (String) props.get(key);
            if(!isProtectedProperty(key))
                root.addElement(elmKey).setText(value);
        }
        
        root.addElement("serviceRevision").setText(SERVICEREVISION);
		return out(getFormattedDocument(doc));
    }

    private boolean isProtectedProperty(String key)
    {
        String [] fragments = protectedPropertyFragments.split(",");
        for(int i=0; i<fragments.length;i++)
            if(key.toLowerCase().indexOf(fragments[i].toLowerCase()) > -1)
                return true;
        return false;
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

    public String doGetChangeNotifications() throws IOException
    {
        String id = getRequest().getSession().getId();
        StringWriter buffer = (StringWriter) changeNotificationBuffer.get(id);
        if(buffer==null)
        {
            buffer = new StringWriter();
            buffer.write("<changeNotifications>");
            changeNotificationBuffer.put(id, buffer);
    		XMLNotificationWriter streamWriter = new XMLNotificationWriter(buffer, ENCODING, "", null, true, true);
            ChangeNotificationController.getInstance().registerListener(streamWriter);
        }

        buffer.write("</changeNotifications>");
        try
        {
            out(getFormattedDocument(DocumentHelper.parseText(buffer.toString())));
        }
        catch(Exception e)
        {
            out("<exception/>");
        }
        buffer.getBuffer().delete(0, buffer.getBuffer().length());
        buffer.write("<changeNotifications>");
        return null;
    }
    
    public String doGetChangeNotificationsStream() throws IOException
    {
        boolean open = true;
        String remoteId = getRequest().getRemoteAddr() + " / " + getInfoGluePrincipal().getName();
        
        String boundary = getRequest().getParameter("boundary");
        if(boundary==null) boundary = "-----------------infoglue-multipart-1d4faa3ac353573";
        getResponse().setHeader("boundary", boundary);
		getResponse().setBufferSize(0);
		getResponse().setContentType("text/plain; charset=" + ENCODING);
		getResponse().flushBuffer();
		Thread thread = Thread.currentThread();
		OutputStream out = getResponse().getOutputStream();
		InputStream in = getRequest().getInputStream();
		
		XMLNotificationWriter streamWriter = new XMLNotificationWriter(new OutputStreamWriter(out), ENCODING, boundary, thread, true, false);
		   
		logger.info("Notification stream listen started from:"  + remoteId);
        ChangeNotificationController.getInstance().registerListener(streamWriter);
        
        Thread streamWriterThread = new Thread(streamWriter);
		streamWriterThread.start();    
	    
		while(open)
		{
            try
            {
                Thread.sleep(Long.MAX_VALUE);
                out.flush();
            }
            catch (Exception e)
            {
                open = false;
            }
		}
        ChangeNotificationController.getInstance().unregisterListener(streamWriter);
        logger.info("Notification stream listen ended from:"  + remoteId);
        return null;
    }

    protected String formatDate(Date date)
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

    	if(repositoryId == null)
    	{
    	    List repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
    	    for(Iterator i=repositories.iterator();i.hasNext();)
    	    {
    	        RepositoryVO r = (RepositoryVO) i.next();
    			BaseEntityVO entityVO = getRootEntityVO(r.getId(), this.getInfoGluePrincipal());
    	        
    	        String src= action + "?repositoryId=" + r.getId() + urlArgSeparator + "parent=" + entityVO.getId();
				if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
				if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
				String allowedContentTypeIdsUrlEncodedString = getAllowedContentTypeIdsAsUrlEncodedString();
				logger.info("allowedContentTypeIdsUrlEncodedString1:" + allowedContentTypeIdsUrlEncodedString);
				if(allowedContentTypeIdsUrlEncodedString.length()>0 && src.length() >0) 
				    src += urlArgSeparator + allowedContentTypeIdsUrlEncodedString;
    	        
				logger.info("src:" + src);
    			
				String text=r.getName();
    	        Element element = root.addElement("tree");
    	        element
	        		.addAttribute("id", "" + r.getId())
    	        	.addAttribute("repositoryId", "" + r.getId())
    	        	.addAttribute("text", encode(text))
    	        	.addAttribute("src", src)
    	        	.addAttribute("hasChildren", "true")
    	        	.addAttribute("type", TYPE_REPOSITORY);
    	    }
    	    out(getFormattedDocument(doc));
    		return null;
    	}
    	
    	sup = getNodeSupplier();
    	    	
    	if(parent == null)
    	{
    		BaseNode node = sup.getRootNode();
			String text = node.getTitle();
	        String type = TYPE_FOLDER;
			String src = action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + node.getId();
			if(createAction && src.length() >0) src += urlArgSeparator + "createAction=true";
			if(action.length()>0 && src.length() >0) src += urlArgSeparator + "action=" + action;
			String allowedContentTypeIdsUrlEncodedString = getAllowedContentTypeIdsAsUrlEncodedString();
			logger.info("allowedContentTypeIdsUrlEncodedString2:" + allowedContentTypeIdsUrlEncodedString);
			if(allowedContentTypeIdsUrlEncodedString.length()>0 && src.length() >0) 
			    src += urlArgSeparator + allowedContentTypeIdsUrlEncodedString;
	        
			//logger.info("src2:" + src);
			
			Element elm = root.addElement("tree");
	        elm
	        	.addAttribute("id", "" + node.getId())
	        	.addAttribute("repositoryId", "" + repositoryId)
	        	.addAttribute("text", encode(text))
	        	.addAttribute("src", src)
   	        	.addAttribute("isHidden", (String)node.getParameters().get("isHidden"))
   	        	.addAttribute("hasChildren", "true")
	        	.addAttribute("type", type);
			
	        if(node.getParameters().containsKey("contentTypeDefinitionId"))
	        	elm.addAttribute("contentTypeDefinitionId", (String)node.getParameters().get("contentTypeDefinitionId"));

	        if(node.getParameters().containsKey("isProtected"))
	        	elm.addAttribute("isProtected", (String)node.getParameters().get("isProtected"));

	        if(node.getParameters().containsKey("stateId"))
	        	elm.addAttribute("stateId", (String)node.getParameters().get("stateId"));

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
				
				// String src = theNode.hasChildren() ? action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + theNode.getId(): "";
				String src = action + "?repositoryId=" + repositoryId + urlArgSeparator + "parent=" + theNode.getId();
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
		        	.addAttribute("text", encode(theNode.getTitle()))
		        	.addAttribute("src", src)
		        	.addAttribute("isHidden", (String)theNode.getParameters().get("isHidden"))
		        	.addAttribute("type", TYPE_FOLDER)
		        	.addAttribute("hasChildren", "" + theNode.hasChildren());
		        
		        if(theNode.getParameters().containsKey("contentTypeDefinitionId"))
		        	elm.addAttribute("contentTypeDefinitionId", "" + theNode.getParameters().get("contentTypeDefinitionId"));
		        
		        if(theNode.getParameters().containsKey("isProtected"))
		        	elm.addAttribute("isProtected", (String)theNode.getParameters().get("isProtected"));

		        if(theNode.getParameters().containsKey("stateId"))
		        	elm.addAttribute("stateId", (String)theNode.getParameters().get("stateId"));

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
		        	.addAttribute("text", encode(text))
		        	.addAttribute("type", type)
				;

		        if(theNode.getParameters().containsKey("contentTypeDefinitionId"))
		        	elm.addAttribute("contentTypeDefinitionId", "" + theNode.getParameters().get("contentTypeDefinitionId"));

		        if(theNode.getParameters().containsKey("isProtected"))
		        	elm.addAttribute("isProtected", (String)theNode.getParameters().get("isProtected"));

		        if(theNode.getParameters().containsKey("stateId"))
		        	elm.addAttribute("stateId", (String)theNode.getParameters().get("stateId"));

		        if(createAction) 
		        	elm.addAttribute("action", action);
		        else
		        {
			        ContentVersionVO activeVersion = contentVersionController.getLatestActiveContentVersionVO(theNode.getId(), LanguageController.getController().getMasterLanguage(repositoryId).getLanguageId());
			        if(activeVersion!=null && !useTemplate)
			        {
			            elm.addAttribute("activeVersion", "" + activeVersion.getContentVersionId());
			            elm.addAttribute("activeVersionStateId", "" + activeVersion.getStateId());
			            elm.addAttribute("activeVersionModifier", "" + activeVersion.getVersionModifier());
			        }
		        }
		        
		        //TODO - this was a quickfix only
		        if(!useTemplate && sup.getClass().getName().indexOf("Content") > -1)
		        {
		            ContentTypeDefinitionVO contentTypeDefinitionVO = contentController.getContentTypeDefinition(theNode.getId());
		        	if(contentTypeDefinitionVO != null)
		        	    elm.addAttribute("contentTypeId","" + contentTypeDefinitionVO.getContentTypeDefinitionId());
		        }
		    }
			
    	    out(getFormattedDocument(doc));
    		return null;
    	}
    	
    	return null;
    }

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer integer) {
		parent = integer;
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer integer) {
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
	public String getShowLeafs() {
		return showLeafs;
	}
	public void setShowLeafs(String showLeafs) {
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
	    if(allowedContentTypeIds == null)
	        return "";
	    
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
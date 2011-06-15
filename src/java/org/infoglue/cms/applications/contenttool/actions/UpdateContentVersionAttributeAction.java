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

package org.infoglue.cms.applications.contenttool.actions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.deliver.controllers.kernel.impl.simple.PageEditorHelper;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
  * This is the action-class for UpdateContentVersionVersion
  * 
  * @author Mattias Bogeblad
  */

public class UpdateContentVersionAttributeAction extends ViewContentVersionAction 
{
    private final static Logger logger = Logger.getLogger(UpdateContentVersionAttributeAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private ContentVersionVO contentVersionVO;
	private Integer contentId;
	private Integer languageId;
	private Integer contentVersionId;
	private String attributeName;
	private String deliverContext = "infoglueDeliverWorking";

	private ConstraintExceptionBuffer ceb;
		
	public UpdateContentVersionAttributeAction()
	{
		this(new ContentVersionVO());
	}
	
	public UpdateContentVersionAttributeAction(ContentVersionVO contentVersionVO)
	{
		this.contentVersionVO = contentVersionVO;
		this.ceb = new ConstraintExceptionBuffer();	
	}
	
	public String doExecute() throws Exception
    {
		logger.info("Updating content version attribute....");
		logger.info("contentId:" + contentId);
		logger.info("languageId:" + languageId);
		logger.info("attributeName:" + attributeName);
		
    	super.initialize(this.contentVersionId, this.contentId, this.languageId);

		this.contentVersionVO = this.getContentVersionVO();

		String attributeValue = getRequest().getParameter(this.attributeName);
		logger.info("attributeValue:" + attributeValue);
		if(attributeValue != null)
		{
			setAttributeValue(this.contentVersionVO, this.attributeName, attributeValue);
			ceb.throwIfNotEmpty();
			
			this.contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
    		ContentVersionController.getContentVersionController().update(this.contentId, this.languageId, this.contentVersionVO, this.getInfoGluePrincipal());
		}
		
		return "success";
	}

	private static Boolean active = new Boolean(false);
	
	public String doSaveAndReturnValue()
    {
		while(active)
		{
			logger.info("Waiting for previous thread..");
			try
			{
				Thread.sleep(50);
			} 
			catch (Exception e)
			{
			}
		}
		
		synchronized(active)
		{
			active = new Boolean(true);
		}

		try
		{
			logger.info("Updating content version attribute through ajax....");
			logger.info("contentId:" + contentId);
			logger.info("languageId:" + languageId);
			logger.info("attributeName:" + attributeName);
			
	    	super.initialize(this.contentVersionId, this.contentId, this.languageId);
	
			this.contentVersionVO = this.getContentVersionVO();
			if(this.contentVersionVO == null)
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
				ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
	
				StringBuffer sb = new StringBuffer();
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes>");
				List contentTypeAttributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(contentTypeDefinitionVO, true);
				Iterator contentTypeAttributesIterator = contentTypeAttributes.iterator();
				while(contentTypeAttributesIterator.hasNext())
				{
					ContentTypeAttribute contentTypeAttribute = (ContentTypeAttribute)contentTypeAttributesIterator.next();
					String initialValue = contentTypeAttribute.getContentTypeAttribute("initialData").getContentTypeAttributeParameterValue().getValue("label");
					if(initialValue == null || initialValue.trim().equals(""))
						initialValue = "State " + contentTypeAttribute.getName();
					sb.append("<" + contentTypeAttribute.getName() + "><![CDATA[" + initialValue + "]]></" + contentTypeAttribute.getName() + ">");
				}
				sb.append("</attributes></article>");
				
				ContentVersionVO contentVersionVO = new ContentVersionVO();
				contentVersionVO.setVersionComment("Autocreated");
				contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
				contentVersionVO.setVersionValue(sb.toString());
				this.contentVersionVO = ContentVersionController.getContentVersionController().create(contentId, languageId, contentVersionVO, null);
			}
			else if(!this.contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
			{
			    ContentVersion contentVersion = ContentStateController.changeState(this.contentVersionVO.getContentVersionId(), ContentVersionVO.WORKING_STATE, "Edit on sight", false, null, this.getInfoGluePrincipal(), this.contentVersionVO.getContentId(), new ArrayList());
			    this.contentVersionId = contentVersion.getContentVersionId();
			    this.contentVersionVO = contentVersion.getValueObject();
			}
			
			String attributeValue = getRequest().getParameter(this.attributeName);
			logger.info("*************************************************");
			logger.info("** SAVING **");
			logger.info("*************************************************");
			logger.info("attributeValue real:" + attributeValue);
			
			/*
			for(int i=0; i<attributeValue.length(); i++)
				logger.info("c:" + (int)attributeValue.charAt(i) + "-" + Integer.toHexString((int)attributeValue.charAt(i)));
				
			logger.info("attributeValue real:" + attributeValue);
			*/
			
			if(attributeValue != null)
			{
				boolean isUTF8 = false;
				boolean hasUnicodeChars = false;
				if(attributeValue.indexOf((char)65533) > -1)
					isUTF8 = true;
				
				for(int i=0; i<attributeValue.length(); i++)
				{
					int c = (int)attributeValue.charAt(i);
					//logger.info("c2:" + c + "-" + Integer.toHexString(c));
					if(c > 255 && c < 65533)
						hasUnicodeChars = true;
				}

				if(!isUTF8 && !hasUnicodeChars)
				{
					String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
					if(fromEncoding == null)
						fromEncoding = "iso-8859-1";
					
					String toEncoding = CmsPropertyHandler.getUploadToEncoding();
					if(toEncoding == null)
						toEncoding = "utf-8";
					
					if(attributeValue.indexOf("å") == -1 && 
					   attributeValue.indexOf("ä") == -1 && 
					   attributeValue.indexOf("ö") == -1 && 
					   attributeValue.indexOf("Å") == -1 && 
					   attributeValue.indexOf("Ä") == -1 && 
					   attributeValue.indexOf("Ö") == -1)
					{
						//logger.info("Converting...");
						attributeValue = new String(attributeValue.getBytes(fromEncoding), toEncoding);
					}
				}
				/*
				String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
				if(fromEncoding == null)
					fromEncoding = "iso-8859-1";
				
				String toEncoding = CmsPropertyHandler.getUploadToEncoding();
				if(toEncoding == null)
					toEncoding = "utf-8";

				String testattributeValue = new String(attributeValue.getBytes(fromEncoding), toEncoding);
				if(testattributeValue.indexOf((char)65533) == -1)
					attributeValue = testattributeValue;
				*/
				
				/*
				for(int i=0; i<attributeValue.length(); i++)
					logger.info("c2:" + (int)attributeValue.charAt(i) + "-" + Integer.toHexString((int)attributeValue.charAt(i)));

				logger.info("attributeValue after:" + attributeValue);
				*/
				
				logger.info("\n\nattributeValue original:" + attributeValue);
				attributeValue = parseInlineAssetReferences(attributeValue);
				logger.info("attributeValue transformed:" + attributeValue + "\n\n");
	
				setAttributeValue(this.contentVersionVO, this.attributeName, attributeValue);
				ceb.throwIfNotEmpty();
				
				this.contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
	    		ContentVersionController.getContentVersionController().update(this.contentId, this.languageId, this.contentVersionVO, this.getInfoGluePrincipal());
	    		logger.info("*************************************************");
			
	    		attributeValue = PageEditorHelper.parseAttributeForInlineEditing(attributeValue, true, getDeliverContext(), contentId, languageId);
			}
	
			this.getResponse().setContentType("text/plain; charset=utf-8");
			this.getResponse().setCharacterEncoding("utf-8");
			//this.getResponse().setContentType("text/plain");
			this.getResponse().getWriter().println(attributeValue);
		}
		catch (ConstraintException ce) 
		{
			logger.warn("Error saving attribute - not allowed by validation: " + ce.getMessage());
			this.getResponse().setStatus(this.getResponse().SC_NOT_ACCEPTABLE);
			return ERROR;
		}
		catch (Throwable t) 
		{
			logger.error("Error saving attribute: " + t.getMessage(), t);
			this.getResponse().setStatus(this.getResponse().SC_INTERNAL_SERVER_ERROR);
			return ERROR;
		}
		finally
		{
			synchronized(active)
			{
				active = new Boolean(false);
			}
		}
		
		return NONE;
	}

	public String doGetAttributeValue() throws Exception
	{
		try
		{
			super.initialize(this.contentVersionId, this.contentId, this.languageId);
			this.contentVersionVO = this.getContentVersionVO();
			if(this.contentVersionVO == null)
			{
				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
				ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVO.getContentTypeDefinitionId());
			
				StringBuffer sb = new StringBuffer();
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes>");
				List contentTypeAttributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(contentTypeDefinitionVO, true);
				Iterator contentTypeAttributesIterator = contentTypeAttributes.iterator();
				while(contentTypeAttributesIterator.hasNext())
				{
					ContentTypeAttribute contentTypeAttribute = (ContentTypeAttribute)contentTypeAttributesIterator.next();
					String initialValue = contentTypeAttribute.getContentTypeAttribute("initialData").getContentTypeAttributeParameterValue().getValue("label");
					if(initialValue == null || initialValue.trim().equals(""))
						initialValue = "State " + contentTypeAttribute.getName();
					sb.append("<" + contentTypeAttribute.getName() + "><![CDATA[" + initialValue + "]]></" + contentTypeAttribute.getName() + ">");
				}
				sb.append("</attributes></article>");
				
				ContentVersionVO contentVersionVO = new ContentVersionVO();
				contentVersionVO.setVersionComment("Autocreated");
				contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
				contentVersionVO.setVersionValue(sb.toString());
				this.contentVersionVO = ContentVersionController.getContentVersionController().create(contentId, languageId, contentVersionVO, null);
			}

			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
			
			Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(this.contentVersionVO.getContentId());
			logger.info("protectedContentId:" + protectedContentId);
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.Write", protectedContentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1001"));
			
			ceb.throwIfNotEmpty();
			
			String attributeValue = "";
			if(this.contentVersionVO != null)
			{
				attributeValue = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
			}
			
			logger.info("attributeValue before parse:" + attributeValue);
			
    		attributeValue = PageEditorHelper.parseAttributeForInlineEditing(attributeValue, false, getDeliverContext(), contentId, languageId);
			//logger.info("parseAttributeForInlineEditing done");
			
			logger.info("attributeValue:" +attributeValue);
			/*
			logger.info("attributeValue:" +attributeValue);
			for(int i=0; i<attributeValue.length(); i++)
				logger.info("c3:" + (int)attributeValue.charAt(i) + "-" + Integer.toHexString((int)attributeValue.charAt(i)));
			*/
			
			this.getResponse().setContentType("text/plain; charset=utf-8");
			this.getResponse().setCharacterEncoding("utf-8");
			//this.getResponse().setContentType("text/plain");
	        this.getResponse().getWriter().println(attributeValue);			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
		
		return NONE;		
	}
		
	private String parseInlineAssetReferences(String attributeValue) throws Exception
	{
		Map<String,String> replacements = new HashMap<String,String>();
		
		logger.info("********************\n\n");
	    Pattern pattern = Pattern.compile("\"DownloadAsset.action.*?\"");
	    Matcher matcher = pattern.matcher(attributeValue);
	    while ( matcher.find() ) 
	    { 
	        String match = matcher.group();
	        logger.info("Found a inline asset: " + match);
	        String parsedContentId = match.substring(match.indexOf("contentId=") + 10, match.indexOf("&", 10));
	        logger.info("parsedContentId: " + parsedContentId);
	        int langStartIndex = match.indexOf("languageId=") + 11;
	        String parsedLanguageId = match.substring(langStartIndex, match.indexOf("&", langStartIndex));
	        logger.info("parsedLanguageId: " + parsedLanguageId);
	        int assetStartIndex = match.indexOf("assetKey=") + 9;
	        String parsedAssetKey = match.substring(assetStartIndex, match.indexOf("\"", assetStartIndex));
	        logger.info("parsedAssetKey: " + parsedAssetKey);
	        
	        String url = "$templateLogic.getInlineAssetUrl(" + parsedContentId + ", \"" + parsedAssetKey + "\")";
    	    logger.info("url:" + url);
            replacements.put(match.substring(1, match.length() - 1), url);
	    }
		logger.info("********************\n\n");
	    
	    Iterator<String> replacementsIterator = replacements.keySet().iterator();
	    while(replacementsIterator.hasNext())
	    {
	    	String patternToReplace = replacementsIterator.next();
	    	String replacement = replacements.get(patternToReplace);
	    	
	    	logger.info("Replacing " + patternToReplace + " with " + replacement);
	    	patternToReplace = patternToReplace.replaceAll("\\?", "\\\\?");
	    	logger.info("patternToReplace " + patternToReplace);
	    	
	    	replacement = replacement.replaceAll("\\$", "\\\\\\$");
	    	logger.info("replacement " + replacement);
	    	replacement = replacement.replaceAll("\\.", "\\\\.");
	    	logger.info("replacement " + replacement);
	    	replacement = replacement.replaceAll("\\(", "\\\\(");
	    	logger.info("replacement " + replacement);
	    	replacement = replacement.replaceAll("\\)", "\\\\)");
	    	logger.info("replacement " + replacement);

	    	logger.info("attributeValue before " + attributeValue);
	    	attributeValue = attributeValue.replaceAll(patternToReplace, replacement);
	    	logger.info("attributeValue after " + attributeValue);
	    }
	    
	    return attributeValue;
	}

	/**
	 * This method sets a value to the xml that is the contentVersions Value. 
	 */
	 
	private void setAttributeValue(ContentVersionVO contentVersionVO, String attributeName, String attributeValue)
	{
		String value = "";
		if(this.contentVersionVO != null)
		{
			try
	        {
		        logger.info("VersionValue:" + this.contentVersionVO.getVersionValue());
		        InputSource inputSource = new InputSource(new StringReader(this.contentVersionVO.getVersionValue()));
				
				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				Document document = parser.getDocument();
				
				NodeList nl = document.getDocumentElement().getChildNodes();
				Node n = nl.item(0);
				
				nl = n.getChildNodes();
				for(int i=0; i<nl.getLength(); i++)
				{
					n = nl.item(i);
					if(n.getNodeName().equalsIgnoreCase(attributeName))
					{
					    logger.info("Setting attributeValue: " + attributeValue);
					    if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
						{
							n.getFirstChild().setNodeValue(attributeValue);
							break;
						}
						else
						{
							CDATASection cdata = document.createCDATASection(attributeValue);
							n.appendChild(cdata);
							break;
						}
						/*
						Node valueNode = n.getFirstChild();
						n.getFirstChild().setNodeValue(attributeValue);
						break;
						*/
					}
				}
				contentVersionVO.setVersionValue(XMLHelper.serializeDom(document, new StringBuffer()).toString());		        	
	        }
	        catch(Exception e)
	        {
	        	logger.error("Problem updating version value:" + attributeName + "=" + attributeValue + " reason:" + e.getMessage(), e);
	        }
		}
	}
	

	public void setContentVersionId(Integer contentVersionId)
	{
		this.contentVersionVO.setContentVersionId(contentVersionId);	
	}

    public java.lang.Integer getContentVersionId()
    {
        return this.contentVersionVO.getContentVersionId();
    }

	public void setStateId(Integer stateId)
	{
		this.contentVersionVO.setStateId(stateId);	
	}

    public java.lang.Integer getStateId()
    {
        return this.contentVersionVO.getStateId();
    }

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;	
	}

    public java.lang.Integer getContentId()
    {
        return this.contentId;
    }

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

    public java.lang.Integer getLanguageId()
    {
        return this.languageId;
    }
        
    public java.lang.String getVersionValue()
    {
        return this.contentVersionVO.getVersionValue();
    }
        
    public void setVersionValue(java.lang.String versionValue)
    {
    	this.contentVersionVO.setVersionValue(versionValue);
    }
    
	public String getAttributeName()
	{
		return attributeName;
	}

	public void setAttributeName(String attributeName)
	{
		this.attributeName = attributeName;
	}

	public String getDeliverContext()
	{
		return deliverContext;
	}

	public void setDeliverContext(String deliverContext)
	{
		this.deliverContext = deliverContext;
	}

}

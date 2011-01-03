package org.infoglue.deliver.taglib.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class DeleteDigitalAssetTag extends InfoGlueWebServiceTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    /**
     *  
     */
    private String operationName = "deleteDigitalAsset";

    /**
     * The map containing the content that should be updated.
     */

    private Map digitalAsset = new HashMap();

	private Integer contentVersionId;
	private Integer languageId;
   	private Integer contentId;
    private String assetKey;

    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public DeleteDigitalAssetTag()
    {
        super();
    }

    /**
     * Initializes the parameters to make it accessible for the children tags
     * (if any).
     * 
     * @return indication of whether to evaluate the body or not.
     * @throws JspException
     *             if an error occurred while processing this tag.
     */
    public int doStartTag() throws JspException
    {
        return EVAL_BODY_INCLUDE;
    }

    /**
     *  
     */
    public int doEndTag() throws JspException
    {
        try
        {
            if(this.contentVersionId != null)
                digitalAsset.put("contentVersionId", this.contentVersionId);
            if(this.contentId != null)
                digitalAsset.put("contentId", this.contentId);
            if(this.languageId != null)
                digitalAsset.put("languageId", this.languageId);
            if(this.assetKey != null)
                digitalAsset.put("assetKey", this.assetKey);
                
            this.invokeOperation("digitalAsset", digitalAsset);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }

        digitalAsset.clear();
        this.contentVersionId = null;
        this.contentId = null;
        this.languageId = null;
        this.assetKey = null;
        
        return EVAL_PAGE;
    }

    /**
     *  
     */
    public void setOperationName(final String operationName)
    {
        this.operationName = operationName;
    }

    /**
     *  
     */
    public void setPrincipal(final String principalString) throws JspException
    {
        this.principal = (InfoGluePrincipal) this.evaluate("remoteContentService", "principal", principalString, InfoGluePrincipal.class);
    }

    public void setContentVersionId(String contentVersionId) throws JspException
    {
        this.contentVersionId = evaluateInteger("deleteDigitalAsset", "contentVersionId", contentVersionId);
    }

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("deleteDigitalAsset", "contentId", contentId);
    }

    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("deleteDigitalAsset", "languageId", languageId);
    }

    public void setAssetKey(String assetKey) throws JspException
    {
        this.assetKey = evaluateString("deleteDigitalAsset", "assetKey", assetKey);
    }


    public String getOperationName()
    {
        return this.operationName;
    }
}
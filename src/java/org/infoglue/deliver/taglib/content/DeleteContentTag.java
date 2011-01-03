package org.infoglue.deliver.taglib.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class DeleteContentTag extends InfoGlueWebServiceTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    /**
     *  
     */
    private String operationName = "deleteContent";

    /**
     * The map containing the content that should be updated.
     */

    private Map content = new HashMap();

	private Integer contentId;
	private Boolean forceDelete = null;
	
    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public DeleteContentTag()
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
            if(this.contentId != null)
                content.put("contentId", this.contentId);
            if(this.forceDelete != null)
                content.put("forceDelete", this.forceDelete);
            
            this.invokeOperation("content", content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }

        content.clear();
        this.contentId = null;
        
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

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("deleteContent", "contentId", contentId);
    }

    public void setForceDelete(String forceDelete) throws JspException
    {
        this.forceDelete = (Boolean)evaluate("deleteContent", "forceDelete", forceDelete, Boolean.class);
    }

    public String getOperationName()
    {
        return this.operationName;
    }
}
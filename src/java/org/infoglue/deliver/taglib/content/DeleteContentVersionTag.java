package org.infoglue.deliver.taglib.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class DeleteContentVersionTag extends InfoGlueWebServiceTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    /**
     *  
     */
    private String operationName = "deleteContentVersion";

    /**
     * The map containing the content that should be updated.
     */

    private Map contentVersion = new HashMap();

	private Integer contentVersionId;

    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public DeleteContentVersionTag()
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
                contentVersion.put("contentVersionId", this.contentVersionId);
                
            this.invokeOperation("contentVersion", contentVersion);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }

        contentVersion.clear();
        this.contentVersionId = null;
        
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
        this.contentVersionId = evaluateInteger("deleteContentVersion", "contentVersionId", contentVersionId);
    }

    public String getOperationName()
    {
        return this.operationName;
    }
}
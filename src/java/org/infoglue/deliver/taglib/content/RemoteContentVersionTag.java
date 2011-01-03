package org.infoglue.deliver.taglib.content;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps get a ContentVersionVO in the cms from the delivery application.
 */

public class RemoteContentVersionTag extends InfoGlueWebServiceTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    /**
     *  
     */
    private String operationName = "getContentVersion";

    /**
     * The map containing the keys to fetch by.
     */

    private Map content = new HashMap();

	private Integer contentId;
	private Integer languageId;
	
    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public RemoteContentVersionTag()
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
            if(this.languageId != null)
                content.put("languageId", this.languageId);
            
            this.invokeOperation("content", content, ContentVersionVO.class, "infoglue");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }

        content.clear();
        this.contentId = null;
        this.languageId = null;
        this.principal = null;
        
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

    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("deleteContent", "languageId", languageId);
    }
    
    public String getOperationName()
    {
        return this.operationName;
    }
}
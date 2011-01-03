package org.infoglue.deliver.taglib.content;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class UpdateContentTag extends InfoGlueWebServiceTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    /**
     *  
     */
    private String operationName = "updateContent";

    /**
     * The map containing the content that should be updated.
     */

    private Map content = new HashMap();

    private Integer contentId;
    private String name;
    private Date publishDateTime;
    private Date expireDateTime;

    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public UpdateContentTag()
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
            content.put("contentId", this.contentId);
            if(this.name != null)
                content.put("name", this.name);
            if(this.publishDateTime != null)
                content.put("publishDateTime", this.publishDateTime);
            if(this.expireDateTime != null)
                content.put("expireDateTime", this.expireDateTime);
            
            this.invokeOperation("content", content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }

        content.clear();
        this.name = null;
        this.contentId = null;
        this.expireDateTime = null;
        this.publishDateTime = null;

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
        this.contentId = evaluateInteger("updateContent", "contentId", contentId);
    }

    public void setName(String name) throws JspException
    {
        this.name = evaluateString("updateContent", "name", name);
    }

    public void setExpireDateTime(String expireDateTime) throws JspException
    {
        this.expireDateTime = (Date) this.evaluate("updateContent", "expireDateTime", expireDateTime, Date.class);
    }

    public void setPublishDateTime(String publishDateTime) throws JspException
    {
        this.publishDateTime = (Date) this.evaluate("updateContent", "publishDateTime", publishDateTime, Date.class);
    }

    public String getOperationName()
    {
        return this.operationName;
    }
}
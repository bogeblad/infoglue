package org.infoglue.deliver.taglib.management;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.content.InfoGlueWebServiceTag;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public class DeleteFormEntryTag extends InfoGlueWebServiceTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteFormService";

    /**
     *  
     */
    private String operationName = "deleteFormEntry";

    /**
     * The map containing the content that should be updated.
     */

    private Map formEntry = new HashMap();

	private Integer formEntryId;
	private Boolean forceDelete = null;
	
    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public DeleteFormEntryTag()
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
        	this.setTargetEndpointAddress(targetEndpointAddress);
        	
            if(this.formEntryId != null)
            	formEntry.put("formEntryId", this.formEntryId);
            if(this.forceDelete != null)
            	formEntry.put("forceDelete", this.forceDelete);
            
            this.invokeOperation("formEntry", formEntry);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }

        formEntry.clear();
        this.formEntryId = null;
        
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

    public void setFormEntryId(String formEntryId) throws JspException
    {
        this.formEntryId = evaluateInteger("deleteContent", "formEntryId", formEntryId);
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
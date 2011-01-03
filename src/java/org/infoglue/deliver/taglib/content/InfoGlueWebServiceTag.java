package org.infoglue.deliver.taglib.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.xml.namespace.QName;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.webservices.DynamicWebservice;

/**
 * This tag helps update a content in the cms from the delivery application.
 */

public abstract class InfoGlueWebServiceTag extends TemplateControllerTag
{
    /**
     * The universal version identifier.
     */
    private static final long serialVersionUID = -1904980538720103871L;

    /**
     *  
     */
    private String targetEndpointAddress = CmsPropertyHandler.getWebServicesBaseUrl() + "RemoteContentService";

    /**
     *  
     */
    private InfoGluePrincipal principal;

    /**
     *  
     */
    public InfoGlueWebServiceTag()
    {
        super();
    }


    /**
     *  
     */
    public void setTargetEndpointAddress(final String targetEndpointAddress) throws JspException
    {
        this.targetEndpointAddress = evaluateString("infoGlueWebService", "targetEndpointAddress", targetEndpointAddress);
    }

    /**
     *  
     */
    public void setPrincipal(final String principalString) throws JspException
    {
        this.principal = (InfoGluePrincipal) this.evaluate("infoGlueWebService", "principal", principalString, InfoGluePrincipal.class);
    }

    protected void invokeOperation(String name, Object argument) throws JspException
    {
        try
        {
            if (this.principal == null)
                this.principal = this.getController().getPrincipal();

            final DynamicWebservice ws = new DynamicWebservice(principal);

            ws.setTargetEndpointAddress(targetEndpointAddress);
            ws.setOperationName(getOperationName());
            ws.setReturnType(Boolean.class);

            if(argument instanceof Map || argument instanceof HashMap)
                ws.addArgument(name, (Map)argument);
            else if(argument instanceof List || argument instanceof ArrayList)
                ws.addArgument(name, (List)argument);
            else
                ws.addArgument(name, argument);
            
            ws.callService();
            setResultAttribute(ws.getResult());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
    }

    protected void invokeOperation(String name, Object argument, Class returnType, String nameSpace) throws JspException
    {
        try
        {
            if (this.principal == null)
                this.principal = this.getController().getPrincipal();

            final DynamicWebservice ws = new DynamicWebservice(principal);

            ws.setTargetEndpointAddress(targetEndpointAddress);
            ws.setOperationName(getOperationName());
            ws.setReturnType(returnType, new QName(nameSpace, ws.getClassName(returnType)));

            if(argument instanceof Map || argument instanceof HashMap)
                ws.addArgument(name, (Map)argument);
            else if(argument instanceof List || argument instanceof ArrayList)
                ws.addArgument(name, (List)argument);
            else
                ws.addArgument(name, argument);
            
            ws.callService();
            setResultAttribute(ws.getResult());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
    }


    public String getTargetEndpointAddress()
    {
        return targetEndpointAddress;
    }

    public abstract void setOperationName(final String operationName);

    public abstract String getOperationName();
    
}
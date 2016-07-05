package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.actions.ModelAction;
import org.infoglue.cms.controllers.kernel.impl.simple.PropertiesCategoryController;
import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.entities.management.PropertiesCategoryVO;
import org.infoglue.cms.exception.SystemException;

/**
 * This action will manage the category to properties relations
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class PropertiesCategoryAction extends ModelAction
{
	private PropertiesCategoryController controller = PropertiesCategoryController.getController();
	private Integer propertiesId;
	private Integer languageId;
	private String returnAddress;
	
	protected Persistent createModel() { return new PropertiesCategoryVO(); }
	
	public PropertiesCategoryVO getPropertiesCategory()	{ return (PropertiesCategoryVO)getModel(); }

	public Integer getPropertiesCategoryId()		{ return getPropertiesCategory().getPropertiesCategoryId(); }
	public void setPropertiesCategoryId(Integer i)	{ getPropertiesCategory().setPropertiesCategoryId(i); }

	public Integer getPropertiesId()			{ return propertiesId; }
	public void setPropertiesId(Integer i)		{ propertiesId = i; }

	public Integer getLanguageId()			{ return languageId; }
	public void setLanguageId(Integer i)	{ languageId = i;	}

    public String getReturnAddress()
    {
        return returnAddress;
    }

    public void setReturnAddress(String returnAddress)
    {
        this.returnAddress = returnAddress;
    }
	
	public String doAdd() throws SystemException, Exception
	{
		setModel(controller.save(getPropertiesCategory()));
		
		this.getResponse().sendRedirect(returnAddress);
	    
	    return NONE;
	}

	public String doDelete() throws SystemException, Exception
	{
		controller.delete(getPropertiesCategoryId());
		
		this.getResponse().sendRedirect(returnAddress);
	    
	    return NONE;
	}
}

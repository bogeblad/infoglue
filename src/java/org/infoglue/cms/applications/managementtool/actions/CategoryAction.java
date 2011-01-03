package org.infoglue.cms.applications.managementtool.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.ModelAction;
import org.infoglue.cms.applications.common.actions.SubscriptionsAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CategoryAction extends ModelAction
{
    private final static Logger logger = Logger.getLogger(CategoryAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	public static final String MAIN = "main";

	private CategoryController controller = CategoryController.getController();
	private ContentCategoryController contentCategoryController = ContentCategoryController.getController();
	private boolean forceDelete = false;
		
	protected Persistent createModel()	{ return new CategoryVO(); }

	public CategoryVO getCategory()		{ return (CategoryVO)getModel(); }

	public Integer getCategoryId()			{ return getCategory().getCategoryId(); }
	public void setCategoryId(Integer i)	{ getCategory().setCategoryId(i); }

	public List getReferences() throws Exception
	{
		return contentCategoryController.findByCategory(getCategoryId());
	}
	
	public String doList() throws SystemException
	{
		setModels(controller.findRootCategories());
		return SUCCESS;
	}

	public String doNew() throws SystemException
	{
		return SUCCESS;
	}

	public String doEdit() throws SystemException
	{
		setModel(controller.findWithChildren(getCategoryId()));
		return SUCCESS;
	}

	public String doDisplayTreeForMove() throws SystemException
	{
		return SUCCESS;
	}

	public String doMove() throws SystemException
	{
		setModel(controller.moveCategory(getCategoryId(), getCategory().getParentId()));
		return SUCCESS;
	}

	public String doSave() throws SystemException, ConstraintException
	{
		validateModel();
		setModel(controller.save(getCategory()));
		return (getCategory().isRoot())? MAIN : SUCCESS;
	}

	public String doDelete() throws Exception
	{
		List references = new ArrayList();
		try
		{
			references = getReferences();
		}
		catch (Exception e) 
		{
			logger.error("Error getting references:" + e.getMessage());
		}
		
		if(references.size() > 0 && !forceDelete)
			throw new ConstraintException("Category.name", "3608");
		
		// So we have the parent and know which page to go to
		setModel(controller.findById(getCategoryId()));
		controller.delete(getCategoryId());

		return (getCategory().getParentId() == null) ? MAIN : SUCCESS;
	}

	// Needed as part of WebworklAbstractAction
	public String doExecute() throws Exception
	{ 
		return SUCCESS; 
	}

	public void setForceDelete(boolean forceDelete)
	{
		this.forceDelete = forceDelete;
	}
}

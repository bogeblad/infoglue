package org.infoglue.cms.applications.contenttool.actions;

import org.infoglue.cms.applications.common.actions.ModelAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.exception.SystemException;

/**
 * This action will manage the category to content relations
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentCategoryAction extends ModelAction
{
	private static final long serialVersionUID = 9134962841574157622L;

	private ContentCategoryController controller = ContentCategoryController.getController();
	private Integer contentId;
	private Integer languageId;

	protected Persistent createModel()				{ return new ContentCategoryVO(); }
	public ContentCategoryVO getContentCategory()	{ return (ContentCategoryVO)getModel(); }

	public Integer getContentCategoryId()		{ return getContentCategory().getContentCategoryId(); }
	public void setContentCategoryId(Integer i)	{ getContentCategory().setContentCategoryId(i); }

	public Integer getContentId()			{ return contentId; }
	public void setContentId(Integer i)		{ contentId = i; }

	public Integer getLanguageId()			{ return languageId; }
	public void setLanguageId(Integer i)	{ languageId = i;	}

	public String getAnchor() {return "categoriesBlock"; }
	
	public String doAdd() throws SystemException
	{
		logUserActionInfo(getClass(), "doAdd");
		setModel(controller.save(getContentCategory(), this.getInfoGluePrincipal()));
		return SUCCESS;
	}

	public String doDelete() throws SystemException
	{
		logUserActionInfo(getClass(), "doDelete");
		controller.delete(getContentCategoryId(), this.getInfoGluePrincipal());
		return SUCCESS;
	}

	public String doAddStandalone() throws SystemException
	{
		logUserActionInfo(getClass(), "doAddStandalone");
		setModel(controller.save(getContentCategory(), this.getInfoGluePrincipal()));
		return SUCCESS;
	}

	public String doDeleteStandalone() throws SystemException
	{
		logUserActionInfo(getClass(), "doDeleteStandalone");
		controller.delete(getContentCategoryId(), this.getInfoGluePrincipal());
		return SUCCESS;
	}

	public String doAddWizard() throws SystemException
	{
		logUserActionInfo(getClass(), "doAddWizard");
		setModel(controller.save(getContentCategory(), this.getInfoGluePrincipal()));
		return SUCCESS;
	}

	public String doDeleteWizard() throws SystemException
	{
		logUserActionInfo(getClass(), "doDeleteWizard");
		controller.delete(getContentCategoryId(), this.getInfoGluePrincipal());
		return SUCCESS;
	}

}

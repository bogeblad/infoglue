/*
 * Created on 2004-nov-28
 *
 */
package org.infoglue.cms.applications.structuretool.actions;

import org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCCFactory;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.treeservice.ss.SiteNodeNodeSupplier;

import com.frovi.ss.Tree.INodeSupplier;

/**
 * @author Stefan Sik
 * @since 1.4
 */
public class SimpleStructureXmlAction extends SimpleXmlServiceAction {

	/* (non-Javadoc)
	 * @see org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction#getNodeSupplier()
	 */
	public INodeSupplier getNodeSupplier() throws SystemException {
	    if(this.repositoryId != null && this.repositoryId.intValue() > -1) 
	        return new SiteNodeNodeSupplier(getRepositoryId(), this.getInfoGluePrincipal());
	    else 
	        return null;
	}
	/* (non-Javadoc)
	 * @see org.infoglue.cms.applications.common.actions.SimpleXmlServiceAction#getRootEntityVO(java.lang.Integer, org.infoglue.cms.security.InfoGluePrincipal)
	 */
	protected BaseEntityVO getRootEntityVO(Integer repositoryId, InfoGluePrincipal principal) throws ConstraintException, SystemException {
		return ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC().getRootSiteNode(repositoryId, principal);	
	}
	

}

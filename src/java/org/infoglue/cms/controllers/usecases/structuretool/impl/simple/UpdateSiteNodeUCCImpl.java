/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.controllers.usecases.structuretool.impl.simple;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.BaseUCCController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.controllers.usecases.structuretool.UpdateSiteNodeUCC;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DateHelper;

public class UpdateSiteNodeUCCImpl extends BaseUCCController implements UpdateSiteNodeUCC
{
    private final static Logger logger = Logger.getLogger(UpdateSiteNodeUCCImpl.class.getName());

    public SiteNodeVO updateSiteNode(InfoGluePrincipal infoGluePrincipal, SiteNodeVO siteNodeVO, SiteNodeVersionVO updatedSiteNodeVersionVO) throws ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNode siteNode = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeVO.getSiteNodeId(), db);
            siteNode.setValueObject(siteNodeVO);

			SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersion(db, siteNodeVO.getSiteNodeId(), false).getValueObject();
			latestSiteNodeVersionVO.setContentType(updatedSiteNodeVersionVO.getContentType());
			latestSiteNodeVersionVO.setPageCacheKey(updatedSiteNodeVersionVO.getPageCacheKey());
			latestSiteNodeVersionVO.setPageCacheTimeout(updatedSiteNodeVersionVO.getPageCacheTimeout());
			latestSiteNodeVersionVO.setDisableEditOnSight(updatedSiteNodeVersionVO.getDisableEditOnSight());
			latestSiteNodeVersionVO.setDisablePageCache(updatedSiteNodeVersionVO.getDisablePageCache());
			latestSiteNodeVersionVO.setDisableLanguages(updatedSiteNodeVersionVO.getDisableLanguages());
			
			Integer protectecSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(latestSiteNodeVersionVO.getId(), db);
			if(protectecSiteNodeVersionId == null || AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.ChangeAccessRights", "" + protectecSiteNodeVersionId))
			{
				latestSiteNodeVersionVO.setIsProtected(updatedSiteNodeVersionVO.getIsProtected());
				latestSiteNodeVersionVO.setVersionModifier(updatedSiteNodeVersionVO.getVersionModifier());
			}

			latestSiteNodeVersionVO.setIsHidden(updatedSiteNodeVersionVO.getIsHidden());
			latestSiteNodeVersionVO.setDisableForceIdentityCheck(updatedSiteNodeVersionVO.getDisableForceIdentityCheck());
			latestSiteNodeVersionVO.setForceProtocolChange(updatedSiteNodeVersionVO.getForceProtocolChange());
			latestSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());
			
			SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(infoGluePrincipal, latestSiteNodeVersionVO, db);

            //If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }


        return siteNode.getValueObject();
    }        

    public SiteNodeVO updateSiteNode(InfoGluePrincipal infoGluePrincipal, SiteNodeVO siteNodeVO, Integer siteNodeTypeDefinitionId, SiteNodeVersionVO updatedSiteNodeVersionVO) throws AccessConstraintException, ConstraintException, SystemException
    {
        Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

        SiteNode siteNode = null;

        beginTransaction(db);

        try
        {
            //add validation here if needed
            siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeVO.getSiteNodeId(), db);
            siteNode.setValueObject(siteNodeVO);
            
            if(siteNodeTypeDefinitionId != null)
	            siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithId(siteNodeTypeDefinitionId, db));

            
			SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersion(db, siteNodeVO.getSiteNodeId()).getValueObject();
			
			latestSiteNodeVersionVO.setContentType(updatedSiteNodeVersionVO.getContentType());
			latestSiteNodeVersionVO.setPageCacheKey(updatedSiteNodeVersionVO.getPageCacheKey());
			latestSiteNodeVersionVO.setPageCacheTimeout(updatedSiteNodeVersionVO.getPageCacheTimeout());
			latestSiteNodeVersionVO.setDisableEditOnSight(updatedSiteNodeVersionVO.getDisableEditOnSight());
			latestSiteNodeVersionVO.setDisableLanguages(updatedSiteNodeVersionVO.getDisableLanguages());
			latestSiteNodeVersionVO.setDisablePageCache(updatedSiteNodeVersionVO.getDisablePageCache());

			Integer protectecSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(latestSiteNodeVersionVO.getId(), db);
			if(protectecSiteNodeVersionId == null || AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.ChangeAccessRights", "" + protectecSiteNodeVersionId))
			{
				latestSiteNodeVersionVO.setIsProtected(updatedSiteNodeVersionVO.getIsProtected());
				latestSiteNodeVersionVO.setVersionModifier(infoGluePrincipal.getName());
			}
			
			latestSiteNodeVersionVO.setIsHidden(updatedSiteNodeVersionVO.getIsHidden());
			latestSiteNodeVersionVO.setDisableForceIdentityCheck(updatedSiteNodeVersionVO.getDisableForceIdentityCheck());
			latestSiteNodeVersionVO.setForceProtocolChange(updatedSiteNodeVersionVO.getForceProtocolChange());
			latestSiteNodeVersionVO.setModifiedDateTime(DateHelper.getSecondPreciseDate());

			SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().acUpdate(infoGluePrincipal, latestSiteNodeVersionVO, db);

			//If any of the validations or setMethods reported an error, we throw them up now before create.
            ceb.throwIfNotEmpty();
            
            commitTransaction(db);
        }
        catch(ConstraintException ce)
        {
            logger.warn("An error occurred so we should not complete the transaction:" + ce, ce);
            rollbackTransaction(db);
            throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }


        return siteNode.getValueObject();
    }        

}
        

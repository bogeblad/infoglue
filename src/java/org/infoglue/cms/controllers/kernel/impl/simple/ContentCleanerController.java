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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.LockNotGrantedException;
import org.exolab.castor.jdo.TransactionAbortedException;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * This controller is made for cleaning old versions.
 */

@SuppressWarnings({"unused", "unchecked", "static-access"})
public class ContentCleanerController  extends BaseController
{
    private final static Logger logger                                     = Logger.getLogger(ContentCleanerController.class);
    private static final LanguageController languageController             = LanguageController.getController();
    private static final ContentController contentController               = ContentController.getContentController();
    private static final ContentVersionController contentVersionController = ContentVersionController.getContentVersionController();
    private static final DigitalAssetController digitalAssetController     = DigitalAssetController.getController();    
    public static final int FACTOR_KB                                      = 1024,
                            FACTOR_MB                                      = 1048576, 
                            FACTOR_GB                                      = 1073741824;    
    private float recoveredDiskSpaceCnt                                    = 0.0f;
    private Long elapsedTime                                               = 0l;
    private Integer deletedContentVersionsCnt                              = 0, 
                    deletedDigitalAssetsCnt                                = 0;
    
    public static ContentCleanerController getContentCleanerController()
    {
        return new ContentCleanerController();
    }
    
    private ContentCleanerController() {}
    
    
    /**
     * This method will clean all content versions foreach language foreach content that exists in the db.
     * Except thoose content versions that are supposed to be retianed specified by hitSize. 
     * @param hitSize2Retain Is used look up the last active content versions by language 
     * for the given number, and also the last published if that one wasnt included in 
     * the resulting query from the database. 
     * @throws Exception
     */
    public void cleanSweep(final int hitSize2Retain, InfoGluePrincipal principal) throws Exception
    {
        try {
            final Database db = CastorDatabaseService.getDatabase();
            beginTransaction(db);
            final List<LanguageVO> languageVOList = languageController.getLanguageVOList(db);
            final List<ContentVO> contentVOList = contentController.getContentVOList();             
            commitTransaction(db);
            for (final ContentVO contentVO : contentVOList)
            {                   
                clean(contentVO, hitSize2Retain, languageVOList, principal);
            }           
        }   
        catch(Exception e)
        {
            e.printStackTrace();
            //logger.error(e);
        }       
    }
    
    /**
     * This method will clean a single content up on it's content versions for the specified content id.
     * @param contentId The content to perform a clean up on.
     * @param hitSize2Retain The number of content versions to retain.
     * @throws Exception
     */
    public void clean(final Integer contentId, final int hitSize2Retain, InfoGluePrincipal principal) throws Exception
    {
        final Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        final ContentVO contentVO = contentController.getContentVOWithId(contentId);                
        final List<LanguageVO> languageVOList = languageController.getLanguageVOList(db);
        commitTransaction(db);
        clean(contentVO, hitSize2Retain, languageVOList, principal);
    }
    
    /**
     * This method will clean a single content up on it's content versions for the specified content id 
     * and for the specified languages.
     * @param contentId The content to perform a clean up on.
     * @param hitSize2Retain The number of content versions to retain.
     * @param languageVOList The specified languages to clean this content for.
     * @throws Exception
     */
    public void clean(final Integer contentId, final int hitSize2Retain, final List<LanguageVO> languageVOList, InfoGluePrincipal principal) throws Exception
    {
        final Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        final ContentVO contentVO = contentController.getContentVOWithId(contentId);                
        commitTransaction(db);
        clean(contentVO, hitSize2Retain, languageVOList, principal);
    }
    
    /**
     * This method will clean a single content up on it's content versions for the specified ContentVO object  
     * and for the specified languages. If the ContentVO object is a branch this method will perform a recursively
     * clean up on all child Contents and their content versions.
     * @param contentVO The content to perform a clean up on.
     * @param hitSize2Retain The number of content versions to retain.
     * @param languageVOList The specified languages to clean this content for.
     * @throws Exception
     */
    public void clean(final ContentVO contentVO, final int hitSize2Retain, final List<LanguageVO> languageVOList, InfoGluePrincipal principal) throws Exception
    {
        // Recursive clean for branches
        if (contentVO.getIsBranch())
        {
            final List<ContentVO> childs = contentController.getContentChildrenVOList(contentVO.getContentId(), null, false);
            for (final ContentVO child : childs)
            {
                clean(child, hitSize2Retain, languageVOList, principal);
            }
        }
        // Start cleaning content
        for (final LanguageVO languageVO : languageVOList)
        {
            clean(contentVO, hitSize2Retain, languageVO, principal);
        }                   
    }
    
    /**
     * This method is called internally by either of the clean methods above. 
     * This method is responsible for collecting content versions up on a content 
     * and validateing wheter or not a clean is neccessary. 
     * @param contentVO The content to perform a clean up on.
     * @param hitSize2Retain The number of content versions to retain.
     * @param languageVOList The specified languages to clean this content for.
     * @throws Exception
     */
    private void clean(final ContentVO contentVO, final int hitSize2Retain, final LanguageVO languageVO, InfoGluePrincipal principal) throws Exception
    {
        final List<ContentVersionVO> contentVersionsList2Retain = 
            collectContentVersionsList2Retain(contentVO, languageVO, hitSize2Retain);
        // Do this only when we have a sufficient number of content versions, if less there is no need to do this.
        // The list of content versions can hold both the <hitSize> quantity and one copy of the latest published content version,
        // this happens if none of the content versions found was the last published.  
        if (contentVersionsList2Retain.size() >= hitSize2Retain)
        {
            cleanContent(contentVO, languageVO, contentVersionsList2Retain, principal);
        }
    }
    
    /**
     * This is the method where the actually cleaning process occurrs. 
     * This method is called internally from the private clean method above.
     * @param contentVO The content to perform a clean up on.
     * @param languageVO The content language to collect content versions up on. 
     * @param contentVersionsList2Retain The list of content versions to retain.
     * @throws Exception
     */
    private void cleanContent(final ContentVO contentVO, final LanguageVO languageVO, 
            final List<ContentVersionVO> contentVersionsList2Retain, InfoGluePrincipal principal) throws Exception
    {
        final long startTime = System.currentTimeMillis();
        final Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        // Retrive all conten verisons for this content
        final List<ContentVersionVO> contentVerisionList = contentVersionController.getContentVersionsWithParentAndLanguage(contentVO.getContentId(), languageVO.getLanguageId(), db);      
        commitTransaction(db);
        logger.info(contentVerisionList.size() + "  content versions found for contentId " + contentVO.getContentId());
        logger.info("I will hunt them down and clean them all.");
        for (final ContentVersionVO contentVersion : contentVerisionList)
        {
            // Should this content version be retained?
            if (!isRetainedContentVersion(contentVersion, contentVersionsList2Retain))
            {
                final Integer contentVersionId = contentVersion.getContentVersionId();
                logger.info("Listing digital assets for content version:" + contentVersionId);
                // Retrive all digital assets for this content version
                final List<DigitalAssetVO> items = digitalAssetController.getDigitalAssetVOList(contentVersionId);
                logger.info(items.size() + " digital assets for content version " + contentVersionId + " found.");
                for (final DigitalAssetVO digitalAsset : items) 
                {
                    final Integer digitalAssetId = digitalAsset.getDigitalAssetId();
                    recoveredDiskSpaceCnt += digitalAsset.getAssetFileSize();
                    logger.info("\tDead Digital Asset: " + digitalAsset.getAssetFileName());
                    // Delete all digital assets and their references that belongs to this content version
                    contentVersionController.deleteDigitalAssetRelation(contentVersionId, digitalAssetId, principal);
                    digitalAssetController.delete(digitalAssetId);
                    deletedDigitalAssetsCnt += 1;                   
                }
                // Delete the content version as well
                contentVersionController.forceDelete(contentVersion);
                deletedContentVersionsCnt += 1;
                logger.info("ContentVersion: " + contentVersion.getContentVersionId() + " Is Dead Meat.");                              
            }
        }
        elapsedTime += System.currentTimeMillis() - startTime;
    }
    
    /**
     * This method checks wheter a content version exist in the retained list.
     * @param contentVersion the content version to check.
     * @param contentVerisionList2Retain the lists that holds the retained content versions.
     * @return Wheterthis is a retained content version or not. 
     */
    private boolean isRetainedContentVersion(final ContentVersionVO contentVersion, 
            final List<ContentVersionVO> contentVerisionList2Retain)
    {
        for (final ContentVersionVO retainedContentVersion : contentVerisionList2Retain)
        {
            if (contentVersion.getContentVersionId().intValue() == 
                retainedContentVersion.getContentVersionId().intValue())
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This method collects the latest active content versions for the specified hitSize 
     * or as many as there is if the number of content versions in the db are insufficient.
     * It is also responsible of making sure that the latest published content version 
     * is collected and retained.
     * @param contentVO The content to collect content versions for.
     * @param languageVO The language up on the contenet versions should be collected for.
     * @param hitSize The number of content versions to retain.
     * @return A list containing the latest published content version for this content and
     * the latest active content versions for the specified hitSize or as many as there is if
     * the hitsSize number of content versions in the db are insufficient. 
     * @throws Exception
     */
    private List<ContentVersionVO> collectContentVersionsList2Retain(final ContentVO contentVO, final LanguageVO languageVO, 
            final int hitSize) throws Exception
    {
        final Integer contentId = contentVO.getContentId(), languageId = languageVO.getLanguageId();
        final Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
        // Retrive the latest content versions for hitSize
        final List<ContentVersionVO> contentVersionsList2Retain = 
            contentVersionController.getLatestActiveContentVersionsForHitSize(contentId, languageId, hitSize, db);
        // If none of them is published, fetch the latest published to this list also
        if (!hasState(contentVersionsList2Retain, ContentVersionVO.PUBLISHED_STATE))
        {
            final ContentVersion latestPublished = 
                contentVersionController.getLatestPublishedContentVersion(contentId, languageId, db);
            if (latestPublished != null)
            {
                contentVersionsList2Retain.add(latestPublished.getValueObject());
            }
        }
        commitTransaction(db);
        return contentVersionsList2Retain;
    }
    
    /**
     * This method checks if any of the content version in the contentVerisionsList2Retain  
     * has a specific state.
     * @param contentVerisionsList2Retain The list to perform check against. 
     * @param state The state to look for.
     * @return Wheter or not the list had a content version in a specific state.
     */
    private boolean hasState(final List<ContentVersionVO> contentVerisionsList2Retain, final Integer state)
    {
        for (final ContentVersionVO contentVersion : contentVerisionsList2Retain)
        {
            if (contentVersion.getStateId().intValue() == state.intValue())
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param factor
     * @return
     */
    public float getCDSFactor(final int factor)
    {
        return recoveredDiskSpaceCnt / factor;
    }
    
    /**
     * @return
     */
    public Integer getDeletedDigitalAsstesCounter()
    {
        return deletedDigitalAssetsCnt;
    }
    
    /**
     * @return
     */
    public Integer getDeletedContentVersionsCounter()
    {
        return deletedContentVersionsCnt;
    }
    
    /**
     * @return
     */
    public Long getElapsedTime()
    {
        return elapsedTime;
    }
    
    /* (non-Javadoc)
     * @see org.infoglue.cms.controllers.kernel.impl.simple.BaseController#getNewVO()
     */
    public BaseEntityVO getNewVO() {
        return null;
    }

}

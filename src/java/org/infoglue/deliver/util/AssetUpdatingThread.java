/**
 * 
 */
package org.infoglue.deliver.util;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.ObjectNotFoundException;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.MediumDigitalAssetImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

public class AssetUpdatingThread extends Thread
{
	private static final Logger logger = Logger.getLogger(AssetUpdatingThread.class);
	private String contentVersionIdString;

	private static AtomicInteger numberOfRunningThreads = new AtomicInteger(0);
	
	AssetUpdatingThread(String contentVersionIdString)
	{
		this.contentVersionIdString = contentVersionIdString;
		
		// TEMPORARY
		logger.setLevel(Level.TRACE);
		// TEMPORARY
	}

	@SuppressWarnings("static-access")
	@Override
	public void run()
	{		
		while(numberOfRunningThreads.get() > 10)
		{
			logger.info("To many threads - let's wait: ");
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}

		numberOfRunningThreads.incrementAndGet();
		logger.info("Running thread");
		
		Database db = null;
		try
		{
			db = CastorDatabaseService.getDatabase();
			CacheController.beginTransaction(db);

			// Setup
			Integer contentVersionId = new Integer(contentVersionIdString);
			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, db);
			int operationMode = Integer.parseInt(CmsPropertyHandler.getOperatingMode());
			if (contentVersionVO.getStateId() < operationMode)
			{
				logger.debug("Updated version had a to low state. State: " + contentVersionVO.getStateId() + ". Operation mode: " + operationMode);
			}
			else
			{
				Integer contentId = contentVersionVO.getContentId();
				Integer languageId = contentVersionVO.getLanguageId();
				Set<Integer> existingAssetIds = new HashSet<Integer>();

				logger.info("Starting asset on disc clean for ContentVersion. Id: " + contentId);

				// Get stuff from previous version
				ContentVersionVO previousContentVersionVO = ContentVersionController.getContentVersionController().getSecondLatestActiveContentVersionVO(contentId, languageId, false, db);
				if (logger.isInfoEnabled())
				{
					logger.info("Previous content version for cache cleaning of ContentVersion: " + contentVersionId + ". ContentVersion.id:" + (previousContentVersionVO == null ? "null" : previousContentVersionVO.getContentVersionId()));
				}
				if (previousContentVersionVO != null)
				{
					List<DigitalAssetVO> previousDigitalAssets = DigitalAssetController.getController().getDigitalAssetVOList(previousContentVersionVO.getContentVersionId(), db);
					for (DigitalAssetVO digitalAssetVO : previousDigitalAssets)
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("Adding digital asset to previous assets. DigitalAsset.id: " + digitalAssetVO.getDigitalAssetId());
						}
						existingAssetIds.add(digitalAssetVO.getDigitalAssetId());
					}
				}
				
				if (logger.isInfoEnabled())
				{
					logger.info("existingAssets: " + existingAssetIds);
				}

				// Handling assets in current version
				List<DigitalAssetVO> digitalAssetVOs = DigitalAssetController.getDigitalAssetVOList(contentVersionId, db);
				if (digitalAssetVOs.size() == 0)
				{
					logger.info("ContentVersion had no assets so we leave things as they are. ContentVersion.id: " + contentVersionIdString);
				}
				else
				{
					
					for (DigitalAssetVO digitalAssetVO : digitalAssetVOs)
					{
						Class typesExtraMedium = MediumDigitalAssetImpl.class;
						Object[] idsExtraMedium = {new Integer(digitalAssetVO.getDigitalAssetId())};
						CacheController.clearCache(typesExtraMedium, idsExtraMedium);
						
						if (logger.isDebugEnabled())
						{
							logger.debug("Checking DigitalAsset.id: " + digitalAssetVO.getDigitalAssetId() + " for ContentVersion.id: " + contentVersionIdString);
						}
						boolean wasOldAsset = existingAssetIds.remove(digitalAssetVO.getDigitalAssetId());
						if (wasOldAsset)
						{
							logger.debug("Asset still exists. AssetId: " + digitalAssetVO.getDigitalAssetId());
						}

						// Check if we should update file on disc
						File currentAssetFile = DigitalAssetController.getController().getAssetFile(digitalAssetVO, contentId, languageId, db);
						if (currentAssetFile != null)
						{
							logger.debug("File is currently stored on disc, let's update!. File: " + currentAssetFile.getAbsolutePath());
							File assetFolder = DigitalAssetController.getController().getAssetFolderFile(digitalAssetVO, contentId, languageId, db);
							String assetFilename = DigitalAssetController.getController().getAssetFileName(digitalAssetVO, contentId, languageId, db);
							logger.debug("Let's use the folder " + assetFolder.getAbsolutePath() + " and the file " + assetFilename);
							DigitalAssetController.getController().dumpDigitalAsset(digitalAssetVO, assetFilename, assetFolder.getAbsolutePath(), true, db);
						}
					}
				}

				logger.info("Number of assets to remove after cache clean: " + existingAssetIds.size());
				for (Integer digitalAssetId : existingAssetIds)
				{
					DigitalAssetVO digitalAssetVO = DigitalAssetController.getController().getDigitalAssetVOWithId(digitalAssetId, db);
					File assetFile = DigitalAssetController.getController().getAssetFile(digitalAssetVO, contentId, languageId, db);
					if (assetFile != null)
					{
						logger.debug("Found asset file to remove. File: " + assetFile.getAbsolutePath());
						boolean success = assetFile.delete();
						if (logger.isDebugEnabled() && success)
						{
							logger.debug("Successfully deleted file. File: " + assetFile.getAbsolutePath());
						}
					}
				}
			}

			CacheController.commitTransaction(db);
		}
		catch (NumberFormatException nex)
		{
			CacheController.rollbackTransaction(db);
			logger.warn("Failed to parse integer in asset clean thread. Message: " + nex);
		}
		catch (SystemException se)
		{
			CacheController.rollbackTransaction(db);
			if(se.getCause() instanceof ObjectNotFoundException)
			{
				logger.info("Error when cleaning assets from disc for ContentVersion.id: " + contentVersionIdString + ". Object is probably deleted. Message: " + se.getMessage());				
			}
			else
			{
				logger.error("Error when cleaning assets from disc for ContentVersion.id: " + contentVersionIdString + ". Message: " + se.getMessage());
				logger.warn("Error when cleaning assets from disc for ContentVersion.id: " + contentVersionIdString, se);
			}
		}
		catch (Throwable ex)
		{
			CacheController.rollbackTransaction(db);
			logger.error("Error when cleaning assets from disc for ContentVersion.id: " + contentVersionIdString + ". Message: " + ex.getMessage());
			logger.warn("Error when cleaning assets from disc for ContentVersion.id: " + contentVersionIdString, ex);
		}
		finally
		{
			numberOfRunningThreads.decrementAndGet();
		}
	}
}
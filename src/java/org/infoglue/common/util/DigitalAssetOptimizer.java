package org.infoglue.common.util;

import java.awt.Image;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.util.CmsPropertyHandler;

public class DigitalAssetOptimizer
{
	private static Logger logger = Logger.getLogger(DigitalAssetOptimizer.class);
	private static DigitalAssetOptimizer instance;

	/* Public methods */

	public File optimizeDigitalAsset(DigitalAssetVO assetVO, File file)
	{
		if (!canOptimizeDigitalAsset(assetVO))
		{
			return null;
		}
		try
		{
			File outputFile = new File(file.getAbsolutePath() + ".opti");
			Image image = javax.imageio.ImageIO.read(file);
			String[] args = new String[6];

			args[0] = CmsPropertyHandler.getExternalImageOptimizer();
			args[1] = "" + image.getWidth(null);
			args[2] = "" + image.getHeight(null);
			args[3] = assetVO.getAssetContentType();
			args[4] = file.getAbsolutePath();
			args[5] = outputFile.getAbsolutePath();

			if (logger.isDebugEnabled())
			{
				logger.debug("Will optimize image <" + file.getAbsolutePath() + "> with command: " + Arrays.toString(args));
			}

			Process p = Runtime.getRuntime().exec(args);
			p.waitFor();
			if (p.exitValue() == 0 && outputFile.exists())
			{
				return outputFile;
			}
			else
			{
				logger.warn("Failed to optimize file. Status code: " + p.exitValue() + ", File exists: " + outputFile.exists());
				if (logger.isDebugEnabled())
				{
					InputStream errorStream = p.getErrorStream();
					if (errorStream == null)
					{
						logger.debug("Got no error stream for image optimization error.");
					}
					else
					{
						logger.debug("Error stream for image optimization error: " + IOUtils.toString(errorStream));
					}
				}
				return null;
			}
		}
		catch (Exception ex)
		{
			logger.error("Error optimizing image. Message: " + ex.getMessage());
			logger.warn("Error optimizing image.", ex);
			return null;
		}
	}

	/**
	 * Determine if the system can optimize the given DigitalAsset.
	 *
	 * @param digitalAssetVO
	 * @return true if calling {@linkplain #optimizeImage(DigitalAssetVO, File)} would optimize the image, false otherwise.
	 */
	private boolean canOptimizeDigitalAsset(DigitalAssetVO digitalAssetVO)
	{
		if (!hasDigitalAssetOptimizerEnabled())
		{
			return false;
		}
		// TODO property for customizing
		String validContentTypes = "image/jpeg,image/jpg,image/png";
		return validContentTypes.indexOf(digitalAssetVO.getAssetContentType()) > -1;
	}
	
	public static boolean hasDigitalAssetOptimizerEnabled()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Has image optimizer: " + CmsPropertyHandler.getExternalImageOptimizer());
		}
		return CmsPropertyHandler.getExternalImageOptimizer() != null;
	}

	/* Singleton methods */
	
	private static synchronized void setupInstance()
	{
		if (instance == null)
		{
			instance = new DigitalAssetOptimizer();
		}
	}
	
	public static DigitalAssetOptimizer getInstance()
	{
		if (instance == null)
		{
			setupInstance();
		}
		return instance;
	}
}

/*
public static boolean isAssetImage(DigitalAssetVO assetVO)
	{
		// TODO property for customizing
		String validContentTypes = "image/jpeg,image/jpg,image/png";
		return validContentTypes.indexOf(assetVO.getAssetContentType()) > -1;
	}
	
	public static boolean hasImageOptimizedEnabled()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Has image optimized: " + CmsPropertyHandler.getExternalImageOptimizer());
		}
		return CmsPropertyHandler.getExternalImageOptimizer() != null;
	}
	
	public static File optimizeImage(DigitalAssetVO assetVO, File file)
	{
		try
		{
			File outputFile = new File(file.getAbsolutePath() + ".opti");
			Image image = javax.imageio.ImageIO.read(file);
			String[] args = new String[6];

			args[0] = CmsPropertyHandler.getExternalImageOptimizer();
			args[1] = "" + image.getWidth(null);
			args[2] = "" + image.getHeight(null);
			args[3] = assetVO.getAssetContentType();
			args[4] = file.getAbsolutePath();
			args[5] = outputFile.getAbsolutePath();

			if (logger.isDebugEnabled())
			{
				logger.debug("Will optimize image <" + file.getAbsolutePath() + "> with command: " + Arrays.toString(args));
			}

			Process p = Runtime.getRuntime().exec(args);
			p.waitFor();
			if (p.exitValue() == 0 && outputFile.exists())
			{
				return outputFile;
			}
			else
			{
				logger.warn("Failed to optimize file. Status code: " + p.exitValue() + ", File exists: " + outputFile.exists());
				if (logger.isDebugEnabled())
				{
					InputStream errorStream = p.getErrorStream();
					if (errorStream == null)
					{
						logger.debug("Got no error stream for image optimization error.");
					}
					else
					{
						logger.debug("Error stream for image optimization error: " + IOUtils.toString(errorStream));
					}
				}
				return null;
			}
		}
		catch (Exception ex)
		{
			logger.error("Error optimizing image. Message: " + ex.getMessage());
			logger.warn("Error optimizing image.", ex);
			return null;
		}	
	}
*/
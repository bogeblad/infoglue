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

package org.infoglue.cms.util.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;

public class ThumbnailGenerator
{
    private final static Logger logger = Logger.getLogger(ThumbnailGenerator.class.getName());

    private static ThumbnailGenerator generator = null;
    
    private ThumbnailGenerator()
    {
    }
    
    public static ThumbnailGenerator getInstance()
    {
    	if(generator == null)
    		generator = new ThumbnailGenerator();
    	
    	return generator;
    }
   
    private void execCmd(String command) throws Exception
    {
		logger.error(command);
		String line;
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		while ((line = input.readLine()) != null)
		{
		    logger.error(line);
		}
		input.close();
    }
   
    public synchronized void transform(String originalFile, String thumbnailFile, int thumbWidth, int thumbHeight, int quality) throws Exception
    {
		Image image = javax.imageio.ImageIO.read(new File(originalFile));
		
		double thumbRatio = (double)thumbWidth / (double)thumbHeight;
		int imageWidth    = image.getWidth(null);
		int imageHeight   = image.getHeight(null);
		double imageRatio = (double)imageWidth / (double)imageHeight;
		if (thumbRatio < imageRatio)
		{
		    thumbHeight = (int)(thumbWidth / imageRatio);
		}
		else
		{
		    thumbWidth = (int)(thumbHeight * imageRatio);
		}
		
		if(imageWidth < thumbWidth && imageHeight < thumbHeight)
		{
		    thumbWidth = imageWidth;
		    thumbHeight = imageHeight;
		}
		else if(imageWidth < thumbWidth)
		    thumbWidth = imageWidth;
		else if(imageHeight < thumbHeight)
		    thumbHeight = imageHeight;
		
		if(thumbWidth < 1)
		    thumbWidth = 1;
		if(thumbHeight < 1)
		    thumbHeight = 1;
		    
		if(CmsPropertyHandler.getExternalThumbnailGeneration() != null && !CmsPropertyHandler.getExternalThumbnailGeneration().equalsIgnoreCase("") && !CmsPropertyHandler.getExternalThumbnailGeneration().equalsIgnoreCase("@externalThumbnailGeneration@"))
		{
		    String[] args = new String[5];
		    
		    args[0] = CmsPropertyHandler.getExternalThumbnailGeneration();
		    args[1] = "-resize";
		    args[2] = String.valueOf(thumbWidth) + "x" + String.valueOf(thumbHeight);
		    args[3] = originalFile;
		    args[4] = thumbnailFile;
		    
		    try
		    {
		        Process p = Runtime.getRuntime().exec(args);
		        p.waitFor();
		    }
		    catch(InterruptedException e)
		    {
		        new Exception("Error resizing image for thumbnail", e); 
		    }		    
		}
		else
		{
		    BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
		    Graphics2D graphics2D = thumbImage.createGraphics();
		    graphics2D.setBackground(Color.WHITE);
		    graphics2D.setPaint(Color.WHITE);
		    graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
		    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		    
		    //graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
		    drawImage(graphics2D, image, 0, 0, thumbWidth, thumbHeight);
		    
		    javax.imageio.ImageIO.write(thumbImage, "JPG", new File(thumbnailFile));
		}
	}

    public synchronized void transform(String originalFile, String thumbnailFile, int thumbWidth, int thumbHeight, int quality, int canvasWidth, int canvasHeight, Color canvasColor, String alignment, String valignment) throws Exception
    {
		Image image = javax.imageio.ImageIO.read(new File(originalFile));
		
		double thumbRatio = (double)thumbWidth / (double)thumbHeight;
		int imageWidth    = image.getWidth(null);
		int imageHeight   = image.getHeight(null);
		double imageRatio = (double)imageWidth / (double)imageHeight;
		if (thumbRatio < imageRatio)
		{
		    thumbHeight = (int)(thumbWidth / imageRatio);
		}
		else
		{
		    thumbWidth = (int)(thumbHeight * imageRatio);
		}
		
		if(imageWidth < thumbWidth && imageHeight < thumbHeight)
		{
		    thumbWidth = imageWidth;
		    thumbHeight = imageHeight;
		}
		else if(imageWidth < thumbWidth)
		    thumbWidth = imageWidth;
		else if(imageHeight < thumbHeight)
		    thumbHeight = imageHeight;
		
		if(thumbWidth < 1)
		    thumbWidth = 1;
		if(thumbHeight < 1)
		    thumbHeight = 1;

		if(thumbWidth > canvasWidth)
			canvasWidth = thumbWidth;
		if(thumbHeight > canvasHeight)
			canvasHeight = thumbHeight;

		if(CmsPropertyHandler.getExternalThumbnailGeneration() != null && !CmsPropertyHandler.getExternalThumbnailGeneration().equalsIgnoreCase("") && !CmsPropertyHandler.getExternalThumbnailGeneration().equalsIgnoreCase("@externalThumbnailGeneration@"))
		{
		    String[] args = new String[5];
		    
		    args[0] = CmsPropertyHandler.getExternalThumbnailGeneration();
		    args[1] = "-resize";
		    args[2] = String.valueOf(thumbWidth) + "x" + String.valueOf(thumbHeight);
		    args[3] = originalFile;
		    args[4] = thumbnailFile;
		    
		    try
		    {
		        Process p = Runtime.getRuntime().exec(args);
		        p.waitFor();
		    }
		    catch(InterruptedException e)
		    {
		        new Exception("Error resizing image for thumbnail", e); 
		    }		    
		}
		else
		{	        
		    BufferedImage thumbImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
		    Graphics2D graphics2D = thumbImage.createGraphics();
		    graphics2D.setBackground(canvasColor);
		    graphics2D.setPaint(canvasColor);
		    
		    int startX = 0;
		    if(alignment.equalsIgnoreCase("center"))
		    	startX = (canvasWidth - thumbWidth) / 2;
		    
		    int startY = 0;
		    if(valignment.equalsIgnoreCase("middle"))
		    	startY = (canvasHeight - thumbHeight) / 2;
		    else if(valignment.equalsIgnoreCase("bottom"))
		    	startY = canvasHeight - thumbHeight;
		    
		    graphics2D.fillRect(0, 0, canvasWidth, canvasHeight);
		    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		    
		    //graphics2D.drawImage(image, startX, startY, thumbWidth, thumbHeight, null);
		    drawImage(graphics2D, image, startX, startY, thumbWidth, thumbHeight);
		    
		    javax.imageio.ImageIO.write(thumbImage, "JPG", new File(thumbnailFile));
		}
	}
    
    private synchronized void drawImage(Graphics2D graphics2D, Image image, int startX, int startY, int thumbWidth, int thumbHeight)
    {
    	graphics2D.drawImage(image, startX, startY, thumbWidth, thumbHeight, null);
    }

}

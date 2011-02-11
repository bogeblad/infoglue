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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.thebuzzmedia.imgscalr.Scalr;

/**
 * An generic wrapper for all image-handling logic. Currently uses simple classes and standard java-api:s.
 * 
 * @author mattias.bogeblad@gmail.com
 */

public class Imaging 
{
	public static void main(String[] args) 
	{
        try 
        {
			BufferedImage image = javax.imageio.ImageIO.read(new File("/logs/orginal.jpg"));
			BufferedImage scaledImage = Scalr.resize(image, 60);
			javax.imageio.ImageIO.write(scaledImage, "JPG", new File("/logs/output1.jpg"));
			BufferedImage image2 = javax.imageio.ImageIO.read(new File("/logs/orginal2.png"));
			BufferedImage scaledImage2 = Scalr.resize(image2, 60);
			javax.imageio.ImageIO.write(scaledImage2, "PNG", new File("/logs/output2.png"));

			BufferedImage image3 = javax.imageio.ImageIO.read(new File("/logs/orginal2.png"));
			BufferedImage croppedImage3 = image3.getSubimage(300, 200, 600, 300);
			javax.imageio.ImageIO.write(croppedImage3, "PNG", new File("/logs/output3.png"));
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

	public static void resize(File input, File output, int width, int height, String format, boolean constrainProportions) throws Exception
	{
		if(height <= 0) height = width;
		if(width <= 0) width = height;

		BufferedImage image = javax.imageio.ImageIO.read(input);
		
		BufferedImage scaledImage = null;
		if(constrainProportions)
		{			
			scaledImage = Scalr.resize(image, width, height);
		}
		else
		{
			scaledImage = Scalr.resize(image, width, height);
		}
		
		javax.imageio.ImageIO.write(scaledImage, format, output);
	}

	public static void crop(File input, File output, int x, int y, int width, int height, String format) throws Exception
	{
		BufferedImage image = javax.imageio.ImageIO.read(input);
		
		BufferedImage croppedImage = image.getSubimage(x, y, width, height);
		
		javax.imageio.ImageIO.write(croppedImage, format, output);
	}

}

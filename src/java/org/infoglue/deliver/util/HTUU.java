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

package org.infoglue.deliver.util;

/*
** HTUU.CLASS**** ACKNOWLEDGEMENT:
**	Main code is taken from the HTUU.C distribution, and was originally
**	written by Mark Riordan (riordanmr@clvax1.cl.msu.edu)
** and Ari Luotonen (luotonen@dxcern.cern.ch).**** AUTHORS:
** IG  Ian Goh         ian.goh@jhu.edu**** HISTORY:
** Converted HTUU.C "HTUU_encode" function into Java (1.0.2): IG 13 July 1996
** -------------------------------------------------------------
**  File contains a routine to convert a buffer
**  of bytes to RFC 1113 printable encoding format.**
**  This technique is similar to the familiar Unix uuencode
**  format in that it maps 6 binary bits to one ASCII
**  character (or more aptly, 3 binary bytes to 4 ASCII
**  characters).  However, RFC 1113 does not use the same
**  mapping to printable characters as uuencode.**
**  Mark Riordan   12 August 1990 and 17 Feb 1991.
**  This code is hereby placed in the public domain.
** -------------------------------------------------------------*****/

public class HTUU 
{	
    static String version = "HTUU Class v1.0 7/13/96";
    
	// the Base64 printable encoding characters	
	static char[] ENC = {
		'A','B','C','D','E','F','G','H','I','J','K','L','M',
		'N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
		'a','b','c','d','e','f','g','h','i','j','k','l','m',
		'n','o','p','q','r','s','t','u','v','w','x','y','z',
		'0','1','2','3','4','5','6','7','8','9','+','/'	};		
	
	// function encode takes the "username:password" string and
	// converts it into the printable encoding format			
	
	public static String encode(String string) 
	{
	    int i, j;
		byte[] byte_array = new byte[3]; 
		StringBuffer buf_coded = new StringBuffer();
		
		// get length of input string
 		int nbytes = string.length();
 		for (i = 0; i < nbytes; i+= 3) 
 		{
			// check to make sure we don't run off the end of input string
			if (i + 3 < nbytes)
			    j = i + 3;
			else
			    j = nbytes;
			
			string.getBytes(i, j, byte_array, 0);		// get bytes i..j		        
			
			if (j - i == 1) 
			{				// missing last two bytes
			    byte_array[1] = 0;
				byte_array[2] = 0;
			}
		    if (j - i == 2) 
		    {				// missing last byte
				byte_array[2] = 0;
			}         
			// convert the three bytes into four Base64 characters
			// and append to the buf_coded string buffer
			buf_coded.append(ENC[byte_array[0] >> 2]);           
			buf_coded.append(ENC[((byte_array[0] << 4) & 060) | ((byte_array[1] >> 4) & 017)]); 
			buf_coded.append(ENC[((byte_array[1] << 2) & 074) | ((byte_array[2] >> 6) & 03)]);
			buf_coded.append(ENC[byte_array[2] & 077]);		} // end for loop
		// If nbytes was not a multiple of 3, then we have encoded too
		// many characters.  Adjust appropriately.
		int buf_length = buf_coded.length();
		if (i == nbytes+1) 
		{
     		/* There were only 2 bytes in that last group */
     		buf_coded.setCharAt(buf_length - 1, '=');
     	} 
     	else if (i == nbytes+2) 
     	{
     		/* There was only 1 byte in that last group */
     		buf_coded.setCharAt(buf_length - 1, '=');
     		buf_coded.setCharAt(buf_length - 2, '='); 
     	}           
		// return the Base64 encoded string
		return buf_coded.toString();      
	} 
}
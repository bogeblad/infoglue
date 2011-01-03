package org.infoglue.cms.util;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

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

public class DesEncryptionHelper 
{
    private static SecretKey key = null;
    
    static
    {
        try 
        {
            // Generate a temporary key. In practice, you would save this key.
            // See also e464 Encrypting with DES Using a Pass Phrase.
            key = KeyGenerator.getInstance("DES").generateKey(); 
        } 
        catch (Exception e) 
        {
        }
    }

    Cipher ecipher;
    Cipher dcipher;

    public DesEncryptionHelper() 
    {
        try 
        {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);

        } 
        catch (javax.crypto.NoSuchPaddingException e) 
        {
        } 
        catch (java.security.NoSuchAlgorithmException e) 
        {
        } 
        catch (java.security.InvalidKeyException e) 
        {
        }
    }

    public String encrypt(String str) 
    {
        try 
        {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            //System.out.println("Nytt sätt:" + new String(new Base64().encode(enc), "ASCII"));
            //System.out.println("Gammalt sätt:" + new sun.misc.BASE64Encoder().encode(enc));
            
            // Encode bytes to base64 to get a string
			return new String(new Base64().encode(enc), "ASCII");
            //return new sun.misc.BASE64Encoder().encode(enc);
        } 
        catch (javax.crypto.BadPaddingException e) 
        {
        } 
        catch (IllegalBlockSizeException e) 
        {
        } 
        catch (UnsupportedEncodingException e) 
        {
        } 
        catch (java.io.IOException e) 
        {
        }
        
        return null;
    }

    public String decrypt(String str) 
    {
        try 
        {
            // Decode base64 to get bytes        	
        	byte[] dec = new Base64().decode(str.getBytes());
           
            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);
            
            //byte[] decOld = new sun.misc.BASE64Decoder().decodeBuffer(str);
            //byte[] utf8Old = dcipher.doFinal(decOld);

            //System.out.println("Nytt sätt decrypt:" + new String(utf8, "UTF8"));
            //System.out.println("Gammalt sätt decrypt:" + new String(utf8Old, "UTF8"));

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } 
        catch (javax.crypto.BadPaddingException e) 
        {
        } 
        catch (IllegalBlockSizeException e) 
        {
        } 
        catch (UnsupportedEncodingException e) 
        {
        } 
        catch (java.io.IOException e) 
        {
        }
        
        return null;
    }
}
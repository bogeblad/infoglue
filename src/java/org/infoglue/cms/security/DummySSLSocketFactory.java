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

package org.infoglue.cms.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


/**
 * <p>Socket factory for SSL jndi links that returns an SSL socket.
 * It incorporates a keystore, which must contain the certs used
 * to authenticate the client.</p>
 *
 * <p>This code is based on sample code made freely available by author
 * Spencer W. Thomas on his web site http://hubris.engin.umich.edu/java/
 * On Wed 24 May, 2000.</p>
 *
 * <p><b>Warning</b></p>
 *
 * <p>This class relies heavily on an internal, single, static SSLSocketFactory.
 * multiple objects of this type in fact will use the same internal SSLSocketFactory.
 * (This is why a single static init() method sets up everything for the entire
 * class.)  The reason for this structure is that JndiSocketFactory is dynmaically
 * invoked by the jndi connection, and we have no other chance to initialise the
 * object.</p>
 */

public class DummySSLSocketFactory extends SSLSocketFactory
{
    private SSLSocketFactory factory;

    public DummySSLSocketFactory() 
    {
    	try 
    	{
    		SSLContext sslcontext = SSLContext.getInstance( "TLS");
    		sslcontext.init( null, // No KeyManager required
            new TrustManager[] { new DummyTrustManager()},
            new java.security.SecureRandom());
    		factory = ( SSLSocketFactory) sslcontext.getSocketFactory();

    	} 
    	catch( Exception ex) 
    	{
    		ex.printStackTrace();
    	}
    }

    public static SocketFactory getDefault() 
    {
      return new DummySSLSocketFactory();
    }

    public Socket createSocket() throws IOException 
    {
    	return factory.createSocket();
    }

    public Socket createSocket( Socket socket, String s, int i, boolean flag) throws IOException 
    {
    	return factory.createSocket( socket, s, i, flag);
    }

    public Socket createSocket( InetAddress inaddr, int i, InetAddress inaddr1, int j) throws IOException 
    {
    	return factory.createSocket( inaddr, i, inaddr1, j);
    }

    public Socket createSocket( InetAddress inaddr, int i) throws IOException 
    {
    	return factory.createSocket( inaddr, i);
    }

    public Socket createSocket( String s, int i, InetAddress inaddr, int j) throws IOException 
    {
    	return factory.createSocket( s, i, inaddr, j);
    }

    public Socket createSocket( String s, int i) throws IOException 
    {
    	return factory.createSocket( s, i);
    }

    public String[] getDefaultCipherSuites() 
    {
    	return factory.getSupportedCipherSuites();
    }

    public String[] getSupportedCipherSuites() 
    {
    	return factory.getSupportedCipherSuites();
    }
}
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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class DummyTrustManager implements X509TrustManager 
{

    public boolean isClientTrusted( X509Certificate[] cert) 
    {
    	return true;
    }

    public boolean isServerTrusted( X509Certificate[] cert) 
    {
    	return true;
    }

    public X509Certificate[] getAcceptedIssuers() 
    {
    	return new X509Certificate[ 0];
    }

	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException 
	{
		// TODO Auto-generated method stub
	}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException 
	{
		// TODO Auto-generated method stub
	}
}
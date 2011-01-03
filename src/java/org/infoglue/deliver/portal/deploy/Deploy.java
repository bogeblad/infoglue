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
package org.infoglue.deliver.portal.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.apache.pluto.descriptors.services.WebAppDescriptorService;
import org.apache.pluto.descriptors.services.impl.AbstractWebAppDescriptorService;
import org.apache.pluto.descriptors.services.impl.StreamPortletAppDescriptorServiceImpl;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.portalImpl.om.portlet.impl.PortletApplicationDefinitionImpl;
import org.apache.pluto.portalImpl.services.ServiceManager;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.infoglue.deliver.portal.ServletConfigContainer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Slightly modified version of jakarta-plutos deploy target. This utility
 * requires that portletdefinitionmapping.xml and servletdefinitionmapping.xml
 * are located somewhere in the classpath.
 */
public class Deploy {
    
    public static class StreamWebAppDescriptorServiceImpl extends AbstractWebAppDescriptorService {

        private InputStream in;
        private OutputStream out;

        public StreamWebAppDescriptorServiceImpl(String contextPath,
                                                 InputStream in,
                                                 OutputStream out) {
            super(contextPath);
            this.in = in;
            this.out = out;
        }

        protected InputStream getInputStream() throws IOException {
            return in;
        }

        protected OutputStream getOutputStream() throws IOException {
            return out;
        }
    }

    private static final Log log = LogFactory.getLog(Deploy.class);

    private static final int MAX_POLL_DEPLOY = 30;

    private static final String PORTLET_XML = "WEB-INF/portlet.xml";

    private static final String WEB_XML = "WEB-INF/web.xml";

    private static final String PORTLET_MAPPING = "portletdefinitionmapping.xml";

    private static final String SERVLET_MAPPING = "servletdefinitionmapping.xml";

    /**
     * Deploy a portlet. Creates a .war-file in webapps and waits until it is
     * deployed by the servlet container (max 30 seconds).
     * 
     * @param webappsDir
     *            webapps directory
     * @param warName
     *            name of .war-file (portlet)
     * @param is
     *            stream of .war-file
     * @param containerName
     *            name of portlet container to be updated
     * @return true if portlet was deployed
     * @throws FileNotFoundException
     *             in case webapps directory not found
     * @throws IOException
     */
    public static boolean deployArchive(String webappsDir, String warName, InputStream is,
            String containerName) throws FileNotFoundException, IOException {
        File webapps = new File(webappsDir);
        if (!webapps.exists() || !webapps.isDirectory()) {
            throw new FileNotFoundException("Webapps directory not found: " + webappsDir);
        }
        String appName = warName;
        int dot = appName.lastIndexOf(".");
        if (dot > 0) {
            appName = appName.substring(0, dot);
        } else {
            warName += ".war";
        }

        // Create .war-file in webapps
        File war = new File(webapps, warName);
        if (war.exists()) {
            // .war-file already exists - skip?
            log.info(".war-file already exists: " + war.getAbsolutePath());
        } else {
            FileOutputStream os = new FileOutputStream(war);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int num = copyStream(is, bos);
            bos.close();
            os.close();
            log.info(num + " bytes written to " + war.getAbsolutePath());
        }

        // Expand .war-file into application directory (NOT necessary in Tomcat
        // 4.1. others?)
        /*
         * File appDir = new File(webapps, appName); if (appDir.exists()) { //
         * app dir already exists - skip? log.warn("Application directory
         * already exists: " + appDir.getAbsolutePath()); return false; } if
         * (!appDir.mkdirs()) { throw new IOException( "Unable to create
         * application directory: " + appDir.getAbsolutePath()); } int numFiles =
         * expandArchive(war, appDir); log.warn(numFiles + " files extracted to " +
         * appDir.getAbsolutePath());
         */

        // Wait until portlet is deployed in servlet container
        File appDir = new File(webapps, appName);
        File webInfDir = new File(appDir, "WEB-INF");
        File portletXml = new File(webInfDir, "portlet.xml");
        for (int i = 0; !portletXml.exists() && i < MAX_POLL_DEPLOY; i++) {
            log.debug("Waiting for servlet container to deploy portlet, try " + i + "/"
                    + MAX_POLL_DEPLOY);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Deployment paus interrupted", e);
            }
        }
        if (!portletXml.exists()) {
            log.error("Failed to deploy portlet: " + war.getAbsolutePath());
        }

        // Update pluto service manager
        log.info("Updating pluto service manager: " + containerName);
        try {
            PortletContainerServices.prepare(containerName);
            ServiceManager.init(ServletConfigContainer.getContainer().getServletConfig());
        } catch (Exception e) {
            log.error("Error during update of pluto service manager", e);
        }
        return true;
    }

    /**
     * Prepare a portlet according to Pluto (switch web.xml)
     * 
     * @param file .war to prepare
     * @param tmp the resulting updated .war
     * @param appName name of application (context name)
     * @return the portlet application definition (portlet.xml)
     * @throws IOException
     */
    public static PortletApplicationDefinition prepareArchive(File file, File tmp, String appName) throws IOException 
    {
        PortletApplicationDefinitionImpl portletApp = null;
        try 
        {
        	Mapping pdmXml = new Mapping();
            try 
            {
            	URL url = Deploy.class.getResource("/" + PORTLET_MAPPING);
                pdmXml.loadMapping(url);
            } 
            catch (Exception e) 
            {
                throw new IOException("Failed to load mapping file " + PORTLET_MAPPING);
            }
             
            // Open the jar file.
            JarFile jar = new JarFile(file);

            // Extract and parse portlet.xml
            ZipEntry portletEntry = jar.getEntry(PORTLET_XML);
            if (portletEntry == null) 
            {
                throw new IOException("Unable to find portlet.xml");
            }
            
            InputStream pisDebug = jar.getInputStream(portletEntry);
            StringBuffer sb = new StringBuffer();
            int i;
            while((i = pisDebug.read()) > -1)
            {
                sb.append((char)i);
            }
            pisDebug.close();
            
			InputSource xmlSource = new InputSource(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Document portletDocument = parser.getDocument();
            
            //InputStream pis = jar.getInputStream(portletEntry);
            //Document portletDocument = XmlParser.parsePortletXml(pis);
            //pis.close();
            InputStream pis = jar.getInputStream(portletEntry);
            
            ZipEntry webEntry = jar.getEntry(WEB_XML);
            InputStream wis = null;
            if (webEntry != null) 
            {
                wis = jar.getInputStream(webEntry);
                /*  webDocument = XmlParser.parseWebXml(wis);
                wis.close();
                */
            }
            
            Unmarshaller unmarshaller = new Unmarshaller(pdmXml);
            unmarshaller.setWhitespacePreserve(true);
            unmarshaller.setIgnoreExtraElements(true);
            unmarshaller.setIgnoreExtraAttributes(true);

            portletApp = (PortletApplicationDefinitionImpl) unmarshaller.unmarshal(portletDocument);

            // refill structure with necessary information
            Vector structure = new Vector();
            structure.add(appName);
            structure.add(null);
            structure.add(null);
            portletApp.preBuild(structure);

            /*
            // now generate web part
            WebApplicationDefinitionImpl webApp = null;
            if (webDocument != null) {
                Unmarshaller unmarshallerWeb = new Unmarshaller(sdmXml);

                // modified by YCLI: START :: to ignore extra elements and
                // attributes
                unmarshallerWeb.setWhitespacePreserve(true);
                unmarshallerWeb.setIgnoreExtraElements(true);
                unmarshallerWeb.setIgnoreExtraAttributes(true);
                // modified by YCLI: END

                webApp = (WebApplicationDefinitionImpl) unmarshallerWeb.unmarshal(webDocument);
            } else {
                webApp = new WebApplicationDefinitionImpl();
                DisplayNameImpl dispName = new DisplayNameImpl();
                dispName.setDisplayName(appName);
                dispName.setLocale(Locale.ENGLISH);
                DisplayNameSetImpl dispSet = new DisplayNameSetImpl();
                dispSet.add(dispName);
                webApp.setDisplayNames(dispSet);
                DescriptionImpl desc = new DescriptionImpl();
                desc.setDescription("Automated generated Application Wrapper");
                desc.setLocale(Locale.ENGLISH);
                DescriptionSetImpl descSet = new DescriptionSetImpl();
                descSet.add(desc);
                webApp.setDescriptions(descSet);
            }

            org.apache.pluto.om.ControllerFactory controllerFactory = new org.apache.pluto.portalImpl.om.ControllerFactoryImpl();

            ServletDefinitionListCtrl servletDefinitionSetCtrl = (ServletDefinitionListCtrl) controllerFactory
                    .get(webApp.getServletDefinitionList());
            Collection servletMappings = webApp.getServletMappings();

            Iterator portlets = portletApp.getPortletDefinitionList().iterator();
            while (portlets.hasNext()) {

                PortletDefinition portlet = (PortletDefinition) portlets.next();

                // check if already exists
                ServletDefinition servlet = webApp.getServletDefinitionList()
                        .get(portlet.getName());
                if (servlet != null) {
                    ServletDefinitionCtrl _servletCtrl = (ServletDefinitionCtrl) controllerFactory
                            .get(servlet);
                    _servletCtrl.setServletClass("org.apache.pluto.core.PortletServlet");
                } else {
                    servlet = servletDefinitionSetCtrl.add(portlet.getName(),
                            "org.apache.pluto.core.PortletServlet");
                }

                ServletDefinitionCtrl servletCtrl = (ServletDefinitionCtrl) controllerFactory
                        .get(servlet);

                DisplayNameImpl dispName = new DisplayNameImpl();
                dispName.setDisplayName(portlet.getName() + " Wrapper");
                dispName.setLocale(Locale.ENGLISH);
                DisplayNameSetImpl dispSet = new DisplayNameSetImpl();
                dispSet.add(dispName);
                servletCtrl.setDisplayNames(dispSet);
                DescriptionImpl desc = new DescriptionImpl();
                desc.setDescription("Automated generated Portlet Wrapper");
                desc.setLocale(Locale.ENGLISH);
                DescriptionSetImpl descSet = new DescriptionSetImpl();
                descSet.add(desc);
                servletCtrl.setDescriptions(descSet);
                ParameterSet parameters = servlet.getInitParameterSet();

                ParameterSetCtrl parameterSetCtrl = (ParameterSetCtrl) controllerFactory
                        .get(parameters);

                Parameter parameter1 = parameters.get("portlet-class");
                if (parameter1 == null) {
                    parameterSetCtrl.add("portlet-class", portlet.getClassName());
                } else {
                    ParameterCtrl parameterCtrl = (ParameterCtrl) controllerFactory.get(parameter1);
                    parameterCtrl.setValue(portlet.getClassName());

                }
                Parameter parameter2 = parameters.get("portlet-guid");
                if (parameter2 == null) {
                    parameterSetCtrl.add("portlet-guid", portlet.getId().toString());
                } else {
                    ParameterCtrl parameterCtrl = (ParameterCtrl) controllerFactory.get(parameter2);
                    parameterCtrl.setValue(portlet.getId().toString());

                }

                boolean found = false;
                Iterator mappings = servletMappings.iterator();
                while (mappings.hasNext()) {
                    ServletMappingImpl servletMapping = (ServletMappingImpl) mappings.next();
                    if (servletMapping.getServletName().equals(portlet.getName())) {
                        found = true;
                        servletMapping.setUrlPattern("/" + portlet.getName().replace(' ', '_')
                                + "/*");
                    }
                }
                if (!found) {
                    ServletMappingImpl servletMapping = new ServletMappingImpl();
                    servletMapping.setServletName(portlet.getName());
                    servletMapping.setUrlPattern("/" + portlet.getName().replace(' ', '_') + "/*");
                    servletMappings.add(servletMapping);
                }

                SecurityRoleRefSet servletSecurityRoleRefs = ((ServletDefinitionImpl) servlet)
                        .getInitSecurityRoleRefSet();

                SecurityRoleRefSetCtrl servletSecurityRoleRefSetCtrl = (SecurityRoleRefSetCtrl) controllerFactory
                        .get(servletSecurityRoleRefs);

                SecurityRoleSet webAppSecurityRoles = webApp.getSecurityRoles();

                SecurityRoleRefSet portletSecurityRoleRefs = portlet.getInitSecurityRoleRefSet();

                Iterator p = portletSecurityRoleRefs.iterator();
                while (p.hasNext()) {
                    SecurityRoleRef portletSecurityRoleRef = (SecurityRoleRef) p.next();

                    if (portletSecurityRoleRef.getRoleLink() == null
                            && webAppSecurityRoles.get(portletSecurityRoleRef.getRoleName()) == null) {
                        System.out
                                .println("Note: The web application has no security role defined which matches the role name \""
                                        + portletSecurityRoleRef.getRoleName()
                                        + "\" of the security-role-ref element defined for the wrapper-servlet with the name '"
                                        + portlet.getName() + "'.");
                        break;
                    }
                    SecurityRoleRef servletSecurityRoleRef = servletSecurityRoleRefs
                            .get(portletSecurityRoleRef.getRoleName());
                    if (null != servletSecurityRoleRef) {
                        System.out
                                .println("Note: Replaced already existing element of type <security-role-ref> with value \""
                                        + portletSecurityRoleRef.getRoleName()
                                        + "\" for subelement of type <role-name> for the wrapper-servlet with the name '"
                                        + portlet.getName() + "'.");
                        servletSecurityRoleRefSetCtrl.remove(servletSecurityRoleRef);
                    }
                    servletSecurityRoleRefSetCtrl.add(portletSecurityRoleRef);
                }

            }
            */

            /*
             * TODO is this necessary? TagDefinitionImpl portletTagLib = new
             * TagDefinitionImpl(); Collection taglibs =
             * webApp.getCastorTagDefinitions(); taglibs.add(portletTagLib);
             */
        

            // Duplicate jar-file with replaced web.xml
            FileOutputStream fos = new FileOutputStream(tmp);
            JarOutputStream tempJar = new JarOutputStream(fos);
            byte[] buffer = new byte[1024];
            int bytesRead;
            for (Enumeration entries = jar.entries(); entries.hasMoreElements();) 
            {
                JarEntry entry = (JarEntry) entries.nextElement();
                JarEntry newEntry = new JarEntry(entry.getName());
                tempJar.putNextEntry(newEntry);
                
                if (entry.getName().equals(WEB_XML)) {
                    // Swap web.xml
                    /*
                    log.debug("Swapping web.xml");
                    OutputFormat of = new OutputFormat();
                    of.setIndenting(true);
                    of.setIndent(4); // 2-space indention
                    of.setLineWidth(16384);
                    of.setDoctype(Constants.WEB_PORTLET_PUBLIC_ID, Constants.WEB_PORTLET_DTD);

                    XMLSerializer serializer = new XMLSerializer(tempJar, of);
                    Marshaller marshaller = new Marshaller(serializer.asDocumentHandler());
                    marshaller.setMapping(sdmXml);
                    marshaller.marshal(webApp);
                    */
                    
                    PortletAppDescriptorService portletAppDescriptorService = new StreamPortletAppDescriptorServiceImpl(appName, pis, null);
                    File tmpf = File.createTempFile("infoglue-web-xml", null);
                    WebAppDescriptorService webAppDescriptorService = new StreamWebAppDescriptorServiceImpl(appName, wis, new FileOutputStream(tmpf));
                    
                    org.apache.pluto.driver.deploy.Deploy d = new org.apache.pluto.driver.deploy.Deploy(webAppDescriptorService, portletAppDescriptorService);
                    d.updateDescriptors();
                    FileInputStream fis = new FileInputStream(tmpf);
                    while ((bytesRead = fis.read(buffer)) != -1) 
                    {
                        tempJar.write(buffer, 0, bytesRead);
                    }
                    tmpf.delete();
                } 
                else 
                {
                    InputStream entryStream = jar.getInputStream(entry);
                    if(entryStream != null)
                    {
	                    while ((bytesRead = entryStream.read(buffer)) != -1) 
	                    {
	                        tempJar.write(buffer, 0, bytesRead);
	                    }
                    }
                }
            }
            tempJar.flush();
            tempJar.close();
            fos.flush();
            fos.close();
            /*
             * String strTo = dirDelim + "WEB-INF" + dirDelim + "tld" + dirDelim +
             * "portlet.tld"; String strFrom = "webapps" + dirDelim + strTo;
             * 
             * copy(strFrom, webAppsDir + webModule + strTo);
             */
        } catch (Exception e) {
            log.error("Failed to prepare archive", e);
            throw new IOException(e.getMessage());
        }

        return portletApp;
    }

    private static void copy(String from, String to) throws IOException {
        File f = new File(to);
        f.getParentFile().mkdirs();

        InputStream fis = new FileInputStream(from);
        FileOutputStream fos = new FileOutputStream(f);
        copyStream(fis, fos);
        fos.close();
    }

    private static int copyStream(InputStream is, OutputStream os) throws IOException {
        int total = 0;
        byte[] buffer = new byte[1024];
        int length = 0;

        while ((length = is.read(buffer)) >= 0) {
            os.write(buffer, 0, length);
            total += length;
        }
        os.flush();
        return total;
    }

    private static int expandArchive(File warFile, File destDir) throws IOException {
        int numEntries = 0;

        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        JarFile jarFile = new JarFile(warFile);
        Enumeration files = jarFile.entries();
        while (files.hasMoreElements()) {
            JarEntry entry = (JarEntry) files.nextElement();
            String fileName = entry.getName();

            File file = new File(destDir, fileName);
            File dirF = new File(file.getParent());
            dirF.mkdirs();

            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                InputStream fis = jarFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(file);
                copyStream(fis, fos);
                fos.close();
            }
            numEntries++;
        }
        return numEntries;
    }

    private static int createArchive(File dir, File archive) throws IOException {
        int BUFFER = 2048;
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(archive);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        //out.setMethod(ZipOutputStream.DEFLATED);
        byte data[] = new byte[BUFFER];
        // get a list of files from current directory
        File files[] = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File curr = files[i];
            if (curr.isDirectory()) {
                // TODO
            } else {
                FileInputStream fi = new FileInputStream(curr);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(curr.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
        out.close();
        return files.length;
    }

    private static void removeAll(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                removeAll(files[i]);
            }
        }
        file.delete();
    }

    public static void main(String[] args) {
        try {
            File file = new File("c:\\infoglueHome\\RssPortlet.war");
            File tmp = new File("c:\\infoglueHome\\RssPortlet2.war");
            prepareArchive(file, tmp, "test-portlet.war");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
package org.infoglue.common.util.cvsclient;

/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.

 * Contributor(s): Robert Greig.
 *****************************************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.text.MessageFormat;

import javax.net.SocketFactory;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AbstractConnection;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.ConnectionModifier;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.util.LoggedDataInputStream;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

/**
 * Implements a connection to a pserver. See the cvs documents for more
 * information about different connection methods. PServer is popular where
 * security is not an issue. For secure connections, consider using a
 * kserver (Kerberos) or the GSSAPI.
 *
 * @author  Robert Greig
 */
public class MyConnection extends AbstractConnection {
    /**
     * The string that is sent at the beginning of the request to open
     * a connection.
     */
    protected static final String OPEN_PREAMBLE = "BEGIN AUTH REQUEST\n"; //NOI18N

    /**
     * The string that is sent at the end of the request to open a connection.
     */
    protected static final String OPEN_POSTAMBLE = "END AUTH REQUEST\n"; //NOI18N
    

    /**
     * The string that is sent at the beginning of the request to
     * verify a connection.
     * Note the difference between opening a connection and simply verifying.
     */
    protected static final String VERIFY_PREAMBLE =
            "BEGIN VERIFICATION REQUEST\n"; //NOI18N

    /**
     * The string that is sent at the end of a verify request.
     */
    protected static final String VERIFY_POSTAMBLE =
            "END VERIFICATION REQUEST\n"; //NOI18N

    /**
     * A response indicating that authorisation has succeeded.
     */
    protected static final String AUTHENTICATION_SUCCEEDED_RESPONSE =
            "I LOVE YOU"; //NOI18N

    private static final String AUTHENTICATION_SUCCEEDED_RESPONSE_RAW =
            "I LOVE YOU\n"; //NOI18N

    /**
     * A response indicating that the authorisation has failed.
     */
    protected static final String AUTHENTICATION_FAILED_RESPONSE =
            "I HATE YOU"; //NOI18N

    private static final String AUTHENTICATION_FAILED_RESPONSE_RAW =
            "I HATE YOU\n"; //NOI18N



    /**
     * The user name to use.
     */
    protected String userName;

    /**
     * The password, encoded appropriately.
     */
    protected String encodedPassword;

    /**
     * The default port number to use.
     */
    public static final int DEFAULT_PORT = 2401;

    /**
     * The port number to use.
     */
    protected int port = DEFAULT_PORT;

    /**
     * The host to use.
     */
    protected String hostName;

    /**
     * The socket used for the connection.
     */
    protected Socket socket;

    /**
     * The socket factory that will be used to create sockets.
     */
    protected SocketFactory socketFactory;
    
    /**
     * Create an uninitialized PServerConnection. All properties needs to be set
     * explicitly by appropriate setters before this connection can be opened.
     */
    public MyConnection (){
    }

    /**
     * Create PServerConnection and setup it's properties from the supplied
     * CVSRoot object.
     * @throws IllegalArgumentException if the cvsRoot does not represent pserver
     *         connection type.
     */
    public MyConnection(CVSRoot cvsRoot) {
        this(cvsRoot, null);
    }

    /**
     * Create PServerConnection and setup it's properties from the supplied
     * CVSRoot object.
     * @throws IllegalArgumentException if the cvsRoot does not represent pserver
     *         connection type.
     */
    public MyConnection(CVSRoot cvsRoot, SocketFactory factory) {
        if (!CVSRoot.METHOD_PSERVER.equals(cvsRoot.getMethod())) {
            throw new IllegalArgumentException("CVS Root '"+cvsRoot+"' does not represent :pserver: connection type.");
        }
        socketFactory = factory;
        String userName = cvsRoot.getUserName();
        if (userName == null) {
            userName = System.getProperty("user.name");
        }
        setUserName(userName);
        String password = cvsRoot.getPassword();
        if (password != null) {
            setEncodedPassword(StandardScrambler.getInstance().scramble(password));
        }
        setHostName(cvsRoot.getHostName());
        setRepository(cvsRoot.getRepository());
        int port = cvsRoot.getPort();
        if (port == 0) {
            port = 2401; // The default pserver port
        }
        setPort(port);
    }
    
    /**
     * Authenticate a connection with the server, using the specified
     * postamble and preamble.
     *
     * @param preamble the preamble to use
     * @param postamble the postamble to use
     *
     * @throws AuthenticationException if an error occurred
     * @return the socket used to make the connection. The socket is
     * guaranteed to be open if an exception has not been thrown
     */
    private void openConnection(String preamble, String postamble)
            throws AuthenticationException, CommandAbortedException {
        if (hostName == null) {
            String locMessage = "The hostname was null, can't continue."; //NOI18N
            throw new AuthenticationException("HostIsNull", locMessage); //NOI18N
        }

        try {
            SocketFactory sf = (socketFactory != null) ? socketFactory : SocketFactory.getDefault();
            socket = sf.createSocket(hostName, port);

            BufferedOutputStream bos =
                    new BufferedOutputStream(socket.getOutputStream(), 32768);
            LoggedDataOutputStream outputStream = new LoggedDataOutputStream(bos);
            setOutputStream(outputStream);

            BufferedInputStream bis =
                    new BufferedInputStream(socket.getInputStream(), 32768);
            LoggedDataInputStream inputStream = new LoggedDataInputStream(bis);
            setInputStream(inputStream);
			
            outputStream.writeBytes(preamble, "US-ASCII");
            outputStream.writeBytes(getRepository() + "\n"); //NOI18N
            outputStream.writeBytes(userName + "\n"); //NOI18N
            outputStream.writeBytes(getEncodedPasswordNotNull() + "\n", "US-ASCII"); //NOI18N
            outputStream.writeBytes(postamble, "US-ASCII");
            outputStream.flush();

            if (Thread.interrupted()) {
                reset();
                String localMsg = CommandException.getLocalMessage("Client.connectionAborted", null); //NOI18N
                throw new CommandAbortedException("Aborted during connecting to the server.", localMsg); // NOI18N
            }

            // read first 11 bytes only (AUTHENTICATION_SUCCEEDED_RESPONSE\n)
            // I observed lock caused by missing '\n' in reponse
            // this method then blocks forever
            byte rawResponse[] = inputStream.readBytes(AUTHENTICATION_SUCCEEDED_RESPONSE_RAW.length());
            String response = new String(rawResponse, "utf8");  // NOI18N

            if (Thread.interrupted()) {
                reset();
                String localMsg = CommandException.getLocalMessage("Client.connectionAborted", null); //NOI18N
                throw new CommandAbortedException("Aborted during connecting to the server.", localMsg); // NOI18N
            }

            if (AUTHENTICATION_SUCCEEDED_RESPONSE_RAW.equals(response)) {
                return;
            }

            if (AUTHENTICATION_FAILED_RESPONSE_RAW.equals(response)) {
                String localizedMsg = getLocalMessage("AuthenticationException.badPassword",
                                                      null);
                throw new AuthenticationException("AuthenticationFailed", //NOI18N
                                                  localizedMsg);
            }

            if (response == null) response = ""; // NOI18N
            String locMessage = getLocalMessage("AuthenticationException.AuthenticationFailed", //NOI18N
                                                new Object[]{ response });
            throw new AuthenticationException("AuthenticationFailed", //NOI18N
                                              locMessage);
        }
        catch (AuthenticationException ex) {
            reset();
            throw ex;
        }
        catch (ConnectException ex) {
            reset();
            String locMessage =
                    getLocalMessage("AuthenticationException.ConnectException", //NOI18N
                                    new Object[]{hostName, Integer.toString(port)});
            throw new AuthenticationException("ConnectException", ex, //NOI18N
                                              locMessage);
        }
        catch (NoRouteToHostException ex) {
            reset();
            String locMessage =
                    getLocalMessage("AuthenticationException.NoRouteToHostException", //NOI18N
                                    new Object[]{hostName});
            throw new AuthenticationException("NoRouteToHostException", ex, //NOI18N
                                              locMessage);
        }
        catch (IOException ex) {
            reset();
            String locMessage =
                    getLocalMessage("AuthenticationException.IOException", //NOI18N
                                    new Object[]{hostName});
            throw new AuthenticationException("IOException", ex, locMessage); //NOI18N
        }
/*        catch (Throwable t) {
            reset();
            String locMessage = AuthenticationException.getBundleString(
                    "AuthenticationException.Throwable"); //NOI18N
            throw new AuthenticationException("General error", t, locMessage); //NOI18N
        }
 */
    }

    private void reset() {
        socket = null;
        setInputStream(null);
        setOutputStream(null);
    }

    /**
     * Authenticate with the server.
     * Closes the connection immediately. Clients can use this method to ensure
     * that they are capable of authenticating with the server. If no exception
     * is thrown, you can assume that authentication was successful.
     *
     * @throws AuthenticationException if the connection with the server
     * cannot be established
     */
    public void verify() throws AuthenticationException {
        try {
            openConnection(VERIFY_PREAMBLE, VERIFY_POSTAMBLE);
        } catch (CommandAbortedException caex) {
            // Ignore, follow the next steps
        }
        if (socket == null) {
            return;
        }

        try {
            socket.close();
        }
        catch (IOException exc) {
            String locMessage = "An IO Exception occured when verifying: " + exc.getMessage(); //NOI18N
            throw new AuthenticationException("General error", exc, locMessage); //NOI18N
        }
        finally {
            reset();
        }
    }

    /**
     * Authenticate with the server and open a channel of communication with
     * the server.
     * This Client will call this method before interacting with the server. It
     * is up to implementing classes to ensure that they are configured to talk
     * to the server (e.g. port number etc.).
     *
     * @throws AutenticationException if the connection with the server
     * cannot be established
     */
    public void open() throws AuthenticationException, CommandAbortedException {
        openConnection(OPEN_PREAMBLE, OPEN_POSTAMBLE);
    }

    /**
     * Get the username.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the userName.
     * @param name the userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get the encoded password.
     * @return the encoded password
     */
    public String getEncodedPassword() {
        return encodedPassword;
    }

    private String getEncodedPasswordNotNull() {
        if (encodedPassword == null) {
            return StandardScrambler.getInstance().scramble("");
        }
        return encodedPassword;
    }

    /**
     * Set the encoded password.
     * @param password the encoded password to use for authentication
     */
    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    /**
     * Get the port number to use.
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port number to use.
     * @param thePort the port number to use. If you do not set this, 2401
     * is used by default for pserver.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get the host name to use.
     * @return the host name of the server to connect to. If you do not set
     * this, localhost is used by default for pserver.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Get the host name to use.
     * @param theHostName the host name of the server to connect to. If you
     * do not set this, localhost is used by default for pserver.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Close the connection with the server.
     */
    public void close() throws IOException {
        if (!isOpen()) {
            return;
        }

        try {
            socket.close();
        }
        finally {
            reset();
        }
    }

    /**
     * Modify the underlying inputstream.
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    public void modifyInputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyInputStream(getInputStream());
    }

    /**
     * Modify the underlying outputstream.
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    public void modifyOutputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyOutputStream(getOutputStream());
    }

    private String getLocalMessage(String key, Object[] arguments) {
        String locMessage = "An error occured: " + key;
        if (locMessage == null) {
            return null;
        }
        locMessage = MessageFormat.format(locMessage, arguments);
        return locMessage;
    }

    /**
     * Returns true to indicate that the connection was successfully established.
     */
    public boolean isOpen() {
        return socket != null;
    }
    
}


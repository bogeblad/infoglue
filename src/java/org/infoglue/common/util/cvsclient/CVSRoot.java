package org.infoglue.common.util.cvsclient;

/**
 * A struct containing the various bits of information in a CVS root
 * string, allowing easy retrieval of individual items of information
 */

public class CVSRoot
{
    public String connectionType;
    public String user;
    public String host;
    public String repository;
    
    public CVSRoot(String root) throws IllegalArgumentException
    {
        if (!root.startsWith(":"))
            throw new IllegalArgumentException();
        
        int oldColonPosition = 0;
        int colonPosition = root.indexOf(':', 1);
        if (colonPosition==-1)
            throw new IllegalArgumentException();
        connectionType = root.substring(oldColonPosition + 1, colonPosition);
        oldColonPosition = colonPosition;
        colonPosition = root.indexOf('@', colonPosition+1);
        if (colonPosition==-1)
            throw new IllegalArgumentException();
        user = root.substring(oldColonPosition+1, colonPosition);
        oldColonPosition = colonPosition;
        colonPosition = root.indexOf(':', colonPosition+1);
        if (colonPosition==-1)
            throw new IllegalArgumentException();
        host = root.substring(oldColonPosition+1, colonPosition);
        repository = root.substring(colonPosition+1);
        if (connectionType==null || user==null || host==null ||
            repository==null)
            throw new IllegalArgumentException();
    }

	public String getConnectionType()
	{
		return connectionType;
	}

	public String getUser()
	{
		return user;
	}

	public String getHost()
	{
		return host;
	}

	public String getRepository()
	{
		return repository;
	}
}


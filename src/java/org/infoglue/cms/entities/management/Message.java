package org.infoglue.cms.entities.management;

public class Message
{
    private int id;
    private String userName;
    private Integer type;
    private String text;

    public Message(int id, String userName, Integer type, String newtext)
    {
    	this.id = id;
    	this.userName = userName;
    	this.type = type;
    	
        text = newtext;
        if (text.length() > 256)
        {
            text = text.substring(0, 256);
        }
        text = text.replace('<', '[');
        text = text.replace('&', '_');
    }

    public long getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

	public String getUserName() 
	{
		return userName;
	}

    public Integer getType()
    {
        return type;
    }
}
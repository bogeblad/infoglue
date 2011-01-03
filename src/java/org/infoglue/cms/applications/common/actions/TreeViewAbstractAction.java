package org.infoglue.cms.applications.common.actions;

import java.util.Collection;

import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;

import com.frovi.ss.Tree.INodeSupplier;
import com.frovi.ss.Tree.MakeTree;

/**
 * @author Stefan Sik
 *  
 */

public abstract class TreeViewAbstractAction extends InfoGlueAbstractAction
{
	private Collection nodes;
	private String exp="";
	private String rkey = "";
	private String akey = "";
    	        
    // Implement this method
	protected abstract INodeSupplier getNodeSupplier() throws Exception;


    protected Collection initialize(String expString) throws Exception
    {
    	INodeSupplier ns = getNodeSupplier();
    	if(ns != null)
    		return new MakeTree(ns).makeNodeList(expString);
    	else
    		return null;
    } 

    public String doExecute() throws Exception
    {
    	// Fix key
		setExp(getExp().replaceAll(getRkey(), "") + getAkey());
		
		// Set nodes
        setNodes(this.initialize(getExp()));
    	getResponse().setBufferSize(0);
        
    	getResponse().setHeader("Cache-Control","no-cache"); 
    	getResponse().setHeader("Pragma","no-cache");
    	getResponse().setDateHeader ("Expires", 0);
	   
        // return
        return "success";
    }
    
    public String doV3() throws Exception
    {
    	doExecute();
    	
        return "successV3";
    }


	/**
	 * Sets the nodes.
	 * @param nodes The nodes to set
	 */
	protected void setNodes(Collection nodes)
	{
		this.nodes = nodes;
	}
		
	public Collection getNodes() throws Exception
	{
		return this.nodes;
	}        
         
	/**
	 * Returns the expStr.
	 * @return String
	 */
	public String getExp() {
		return exp;
	}

	/**
	 * Sets the expStr.
	 * @param expStr The expStr to set
	 */
	public void setExp(String expStr) {
		this.exp = expStr;
	}


	/**
	 * Returns the akey.
	 * @return String
	 */
	public String getAkey()
	{
		return akey;
	}

	/**
	 * Returns the rkey.
	 * @return String
	 */
	public String getRkey()
	{
		return rkey;
	}

	/**
	 * Sets the akey.
	 * @param akey The akey to set
	 */
	public void setAkey(String akey)
	{
		this.akey = akey;
	}

	/**
	 * Sets the rkey.
	 * @param rkey The rkey to set
	 */
	public void setRkey(String rkey)
	{
		this.rkey = rkey;
	}

  }

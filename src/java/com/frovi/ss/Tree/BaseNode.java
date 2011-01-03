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

package com.frovi.ss.Tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * BaseNode.java
 * PureHtmlTree node object
 * Created on 2002-sep-27 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 */
public abstract class BaseNode extends Object
{
	
	private String title;
	private Integer id;

	// Gaphical stuff
	private boolean container;
	private boolean children;
	private Vector treeStuff;
	private boolean open = false;
	private String openCloseKey = "";
	private String openCloseKeyEx = "";
	private String thisKey = "";
	private int level = 0;
	private boolean root = false;
	private Map parameters = new HashMap();

	
	/**
	 * Returns the id.
	 * @return Integer
	 */
	public Integer getId()
	{
		return id;
	}

	/**
	 * Returns the title.
	 * @return String
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(Integer id)
	{
		this.id = id;
	}

	/**
	 * Sets the title.
	 * @param title The title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns the treeStuff.
	 * @return ArrayList
	 */
	public Vector getTreeStuff() {
		return treeStuff;
	}
	
	// TODO: Clean up this
	public List getRowList() {
		return treeStuff;
	}

	/**
	 * Sets the treeStuff.
	 * @param treeStuff The treeStuff to set
	 */
	public void setTreeStuff(Vector treeStuff) {
		this.treeStuff = treeStuff;
	}


	/**
	 * Returns the open.
	 * @return boolean
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Sets the open.
	 * @param open The open to set
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * Returns the level.
	 * @return int
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level.
	 * @param level The level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Returns the openCloseKey.
	 * @return String
	 */
	public String getOpenCloseKey() {
		return openCloseKey;
	}

	/**
	 * Sets the openCloseKey.
	 * @param openCloseKey The openCloseKey to set
	 */
	public void setOpenCloseKey(String openCloseKey) {
		this.openCloseKey = openCloseKey;
	}


	/**
	 * Returns the children.
	 * @return boolean
	 */
	public boolean hasChildren() {
		return children;
	}

	/**
	 * Sets the children.
	 * @param children The children to set
	 */
	public void setChildren(boolean children) {
		this.children = children;
	}

	/**
	 * Returns the container.
	 * @return boolean
	 */
	public boolean isContainer() {
		return container;
	}

	/**
	 * Sets the container.
	 * @param container The container to set
	 */
	public void setContainer(boolean container) {
		this.container = container;
	}

	/**
	 * Returns the thisKey.
	 * @return String
	 */
	public String getThisKey()
	{
		return thisKey;
	}

	/**
	 * Sets the thisKey.
	 * @param thisKey The thisKey to set
	 */
	public void setThisKey(String thisKey)
	{
		this.thisKey = thisKey;
	}

	/**
	 * Returns the root.
	 * @return boolean
	 */
	public boolean isRoot()
	{
		return root;
	}

	/**
	 * Sets the root.
	 * @param root The root to set
	 */
	public void setIsRoot(boolean root)
	{
		this.root = root;
	}

	/**
	 * Returns the openCloseKeyEx.
	 * @return String
	 */
	public String getOpenCloseKeyEx()
	{
		return openCloseKeyEx;
	}

	/**
	 * Sets the openCloseKeyEx.
	 * @param openCloseKeyEx The openCloseKeyEx to set
	 */
	public void setOpenCloseKeyEx(String openCloseKeyEx)
	{
		this.openCloseKeyEx = openCloseKeyEx;
	}

	public Map getParameters() 
	{
		return parameters;
	}
}

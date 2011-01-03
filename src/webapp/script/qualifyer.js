//////////////////////////////////////////////////////
// File: qualifyer.js
//
// Author: Mattias Bogeblad
// 
// Purpose: To have a generic qualifyer for infoglue entities.
//
//////////////////////////////////////////////////////


/**
 * Qualifyer object
 */

function Qualifyer(entityName, entityId, path)
{
	this.entityName = entityName;
	this.entityId 	= entityId;
	this.path 		= path;
  
  	this.getPath 		= getPath;
  	this.getEntityName 	= getEntityName;
  	this.getEntityId 	= getEntityId;
  	this.setPath 		= setPath;
  	this.setEntityName 	= setEntityName;
  	this.setEntityId 	= setEntityId;
}
  
function getPath()
{
  	return this.path;
}

function getEntityName()
{
  	return this.entityName;
}

function getEntityId()
{
  	return this.entityId;
}

function setPath(path)
{
  	this.path = path;
}

function setEntityName(entityName)
{
  	this.entityName = entityName;
}

function setEntityId(entityId)
{
	this.entityId = entityId;
}

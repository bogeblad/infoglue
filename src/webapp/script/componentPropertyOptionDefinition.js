//////////////////////////////////////////////////////
// File: ComponentPropertyOptionDefinition.js
//
// Author: Mattias Bogeblad
// 
// Purpose: To have a generic ComponentPropertyOptionDefinition.
//
//////////////////////////////////////////////////////


/**
 * ComponentPropertyOptionDefinition object
 */

function ComponentPropertyOptionDefinition(name, value)
{
	this.name 						= name;
	this.value 						= value;
	  
  	this.getName 					= getName;
  	this.getValue 					= getValue;
	
  	this.setName 					= setName;
  	this.setValue 					= setValue;
}
  
function getName()
{
  	return this.name;
}

function getValue()
{
  	return this.value;
}

function setName(name)
{
  	this.name = name;
}

function setValue(value)
{
  	this.value = value;
}
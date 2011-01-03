
//////////////////////////////////////////////////////
// File: vector.js
//
// Author: Jason Geissler
// 
// Date: Sept 3, 2003
//
// Purpose: To have a dynamic collection instead
//          of using arrays when the total quantity
//          is unknown
//////////////////////////////////////////////////////

// Vector Constructor -- constructs the object
function Vector(inc) {
	if (inc == 0) {
		inc = 100;
	}
	
	/* Properties */
	this.data = new Array(inc);
	this.increment = inc;
	this.size = 0;
	
	/* Methods */
	this.getCapacity = getCapacity;
	this.getSize = getSize;
	this.isEmpty = isEmpty;
	this.getLastElement = getLastElement;
	this.getFirstElement = getFirstElement;
	this.getElementAt = getElementAt;
	this.addElement = addElement;
	this.removeElementAt = removeElementAt;
	this.removeAllElements = removeAllElements;
	this.indexOf = indexOf;
	this.contains = contains
	this.resize = resize;
	this.toString = toString;
	this.sort = sort;
	this.trimToSize = trimToSize;
}

// getCapacity() -- returns the number of elements the vector can hold
function getCapacity() {
	return this.data.length;
}

// getSize() -- returns the current size of the vector
function getSize() {
	return this.size;
}

// isEmpty() -- checks to see if the Vector has any elements
function isEmpty() {
	return this.getSize() == 0;
}

// getLastElement() -- returns the last element
function getLastElement() {
	if (this.data[this.getSize() - 1] != null) {
		return this.data[this.getSize() - 1];
	}
}

// getFirstElement() -- returns the first element
function getFirstElement() {
	if (this.data[0] != null) {
		return this.data[0];
	}
}

// getElementAt() -- returns an element at a specified index
function getElementAt(i) {
	try {
		return this.data[i];
	} 
	catch (e) {
		return "Invalid index " + i;	
	}	
}

// addElement() -- adds a element at the end of the Vector
function addElement(obj) {
	if(this.getSize() == this.data.length) {
		this.resize();
	}
	
	this.data[this.size++] = obj;
}

// removeElementAt() -- removes an element at a specific index
function removeElementAt(index) {
	try {
		var element = this.data[index];
		
		for(var i=index; i<(this.getSize()-1); i++) {
			this.data[i] = this.data[i+1];
		}
		
		this.data[getSize()-1] = null;
		this.size--;
		return element;
	}
	catch(e) {
		return "Invalid index " + index;
	}
} 

// removeAllElements() -- removes all elements in the Vector
function removeAllElements() {
	this.size = 0;
	
	for (var i=0; i<this.data.length; i++) {
		this.data[i] = null;
	}
}

// indexOf() -- returns the index of a searched element
function indexOf(obj) {
	for (var i=0; i<this.getSize(); i++) {
		if (this.data[i] == obj) {
			return i;
		}
	}
	return -1;
}

// contains() -- returns true if the element is in the Vector, otherwise false
function contains(obj) {
	for (var i=0; i<this.getSize(); i++) {
		if (this.data[i] == obj) {
			return true;
		}
	}
	return false;
}

// resize() -- increases the size of the Vector
function resize() {
	newData = new Array(this.data.length + this.increment);
	
	for	(var i=0; i< this.data.length; i++) {
		newData[i] = this.data[i];
	}
	
	this.data = newData;
}


// trimToSize() -- trims the vector down to it's size
function trimToSize() {
	var temp = new Array(this.getSize());
	
	for (var i = 0; i < this.getSize(); i++) {
		temp[i] = this.data[i];
	}
	this.size = temp.length - 1;
	this.data = temp;
} 

// sort() - sorts the collection based on a field name - f
function sort(f) {
	var i, j;
	var currentValue;
	var currentObj;
	var compareObj;
	var compareValue;
	
	for(i=1; i<this.getSize();i++) {
		currentObj = this.data[i];
		currentValue = currentObj[f];
		
		j= i-1;
		compareObj = this.data[j];
		compareValue = compareObj[f];
		
		while(j >=0 && compareValue > currentValue) {
			this.data[j+1] = this.data[j];
			j--;
			if (j >=0) {
				compareObj = this.data[j];
				compareValue = compareObj[f];
			}				
		}	
		this.data[j+1] = currentObj;
	}
}

// toString() -- returns a string rep. of the Vector
function toString() {
	var str = "Vector Object properties:\n" +
	          "Increment: " + this.increment + "\n" +
	          "Size: " + this.size + "\n" +
	          "Elements:\n";
	
	for (var i=0; i<getSize(); i++) {
		for (var prop in this.data[i]) {
			var obj = this.data[i];
			str += "\tObject." + prop + " = " + obj[prop] + "\n";
		}
	}
	return str;	
}

	



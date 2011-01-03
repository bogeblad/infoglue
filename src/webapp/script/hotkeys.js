/*==============================================================================

This routine enables standard keys to be used as hotkeys to either invoke a
link, or to fire some JavaScript code.  The hotkey should be pressed down at the 
aame time as either the control key, the alt key, or the Mac metakey.

   Calling Sequence: <body onkeydown="hotKeys(event);">
 
Don't worry about the parameter - just call as above. 

A hot key may either have a link associated with it, or some JavaScript code 
(typically a function).

Note that because of the different keyboard layouts in use, and because of the 
way that JavaScript handles keyboard events, it is best to limit your range of 
hotkeys to the following characters:

   0 to 9
   A to Z
   a to z
   - . ,

Author:     John Gardner
Date:       September 2004

Note that some of the techniques here have been picked up from Chapter 9 of 
"JavaScript and DHTML Cookbook", by Danny Goodman, published by O'Reilly.

The routine should degrade gracefully in older browsers.

The action keys are specified in a table. To add a new key, add a new array 
element - e.g. KeyActions [2]. The code below has been set up for the example 
page:
   
    htt://www.braemoor.co.uk/software/hotkeys.shtml
   
Modify it as required for your web page.
   
The three parts of the array element are as follows:
   
   character:      the hot key
   actionType:     either "link" or "code" 
   param:          either the URL for a hyperlink or the JavaScript code.
   
When using hot keys in forms for buttons, radiobuttons, or checkboxes, ensure 
that you provide each with id and name attributes, with the same value. e.g.
   
     document.myform.submit.click(); 
     document.myform.reset.click();
     document.myform.radio[2].checked=true;
   
==============================================================================*/

// This array should be set up as required for YOUR web page.

var keyActions = new Array ();

/*                  
keyActions [0] = {character:  "c", 
                  actionType: "code", 
                  param:      "top.frames[\"header\"].changeTool(0);"};

keyActions [1] = {character:  "s", 
                  actionType: "code", 
                  param:      "top.frames[\"header\"].changeTool(1);"};

keyActions [2] = {character:  "m", 
                  actionType: "code", 
                  param:      "top.frames[\"header\"].changeTool(2);"};

keyActions [3] = {character:  "p", 
                  actionType: "code", 
                  param:      "top.frames[\"header\"].changeTool(3);"};

keyActions [4] = {character:  "d", 
                  actionType: "code", 
                  param:      "top.frames[\"header\"].changeTool(4);"};
*/

keyActions [0] = {character:  "s", 
                  actionType: "code", 
                  param:      "hotkeyS();"};
                  
// End of user defined array

function hotKeys (event) {

  // Get details of the event dependent upon browser
  event = (event) ? event : ((window.event) ? event : null);
  
  // We have found the event.
  if (event) {   
    
    // Hotkeys require that either the control key or the alt key is being held down
    if (event.ctrlKey || event.altKey || event.metaKey) {
    
      // Pick up the Unicode value of the character of the depressed key.
      var charCode = (event.charCode) ? event.charCode : ((event.which) ? event.which : event.keyCode);
      
      // Convert Unicode character to its lowercase ASCII equivalent
      var myChar = String.fromCharCode (charCode).toLowerCase();
      
      // Convert it back into uppercase if the shift key is being held down
      if (event.shiftKey) {myChar = myChar.toUpperCase();}
          
      // Now scan through the user-defined array to see if character has been defined.
      for (var i = 0; i < keyActions.length; i++) {
         
        // See if the next array element contains the Hotkey character
        if (keyActions[i].character == myChar) { 
      
          // Yes - pick up the action from the table
          var action;
            
          // If the action is a hyperlink, create JavaScript instruction in an anonymous function
          if (keyActions[i].actionType.toLowerCase() == "link") {
            action = new Function ('location.href  ="' + keyActions[i].param + '"');
          }
            
          // If the action is JavaScript, embed it in an anonymous function
          else if (keyActions[i].actionType.toLowerCase()  == "code") {
            action = new Function (keyActions[i].param);
          }
            
          // Error - unrecognised action.
          else {
            alert ('Hotkey Function Error: Action should be "link" or "code"');
            break;
          }
           
          // At last perform the required action from within an anonymous function.
          action ();
         
          // Hotkey actioned - exit from the for loop.
          //break;
          return false;
        }
      }
    }
  }
}
/*============================================================================*/

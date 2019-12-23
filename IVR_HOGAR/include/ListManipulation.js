var ListManipulation_version = "8.1.4";
/**
 * ECMA implementation for IRD List Manipulation Functions. 
 */

/**
 *  Return value type: INTEGER. 
 *  This function parses out an integer value for a specified key from a key-value list. 
 *  This value is obtained from the leading sequence of digits before the first non-digit character in the value. 
 *  If the value starts with a character other than a digit, or if the key searched for does not appear in the list, the function returns 0. 
 *  If the key is repeated in a list, its first occurrence is retrieved.
 * @param key
 * @return
 */

function irdGetIntegerKey(key,list){
	var endResult = 0;
	var sArray;
	var splitItem;
	var tempItem;
	var i;
	
    sArray = list.split("\|");
    for (i=0; i<sArray.length; i++) {
    	tempItem = sArray[i];
    	splitItem = tempItem.split(":");
    	if(splitItem[0].match(key)){
    		if(isNaN(splitItem[1])){
    			return endResult;
    		}
    		else{
    			return Number(splitItem[1]);
    		}
    	}
    }
    
    return endResult;
}


/**
 * Internal utility func
 * @param a
 * @param b
 * @return
 */
function maxSort(a, b) 
{ 
  var aA;
  var aB;
  aA = a.split(":");
  aB = b.split(":");
   if(aA[1] > aB[1]) 
      return -1 ;
   if(aA[1] < aB[1]) 
      return 1 ;
   return 0 ;
}


/**
* IRD Function: getMaxSubList(List)
*Return value type: STRING.
*This function extracts sublist containing only pairs with maximum value.
*All values are interpreted as numbers.
*/
function irdGetMaxSubList(list)
{
	var sArray;
	var sortArray;
	var endResult = null;
	var sortedItem;
	var tmpItem;
	var i;
	
	sArray = list.split("\|");
	sortArray = sArray.sort(maxSort);
	sortedItem = sortArray[0].split(":");
	for (i=0; i<sortArray.length; i++) {
		tmpItem = sortArray[i].split(":");
	     if (sortedItem[1] == tmpItem[1]) { 
	    	 endResult = endResult + sortArray[i];
	     } else {
	          return endResult;
	     }
	}
	 return endResult;
}


/**
 * Internal utility func
 * @param a
 * @param b
 * @return
 */
function minSort(a, b) 
{ 
  var aA;
  var aB;
  aA = a.split(":");
  aB = b.split(":");
   if(aA[1] < aB[1]) 
      return -1 ;
   if(aA[1] > aB[1]) 
      return 1 ;
   return 0 ;
}

/**
 * IRD Function: getMinSubList(List)
 *  Return value type: STRING. 
 *  This function extracts sublist containing only pairs with minimum value. 
 *  All values are interpreted as numbers.
 * @param list
 * @return
 */
function irdGetMinSubList(list)
{
    var sArray;
    var sortArray;
    var endResult = null;
	var splitArray;
	var tmpArray;
	var i;
	
    sArray = list.split("\|");
    sortArray = sArray.sort(maxSort);
    splitArray = sortArray[0].split(":");
    for (i=0; i<sortArray.length; i++) {
    	tmpArray = sortArray[i].split(":");
         if (splitArray[1] == tmpArray[1]) { 
        	 endResult = endResult + sortArray[i];
         } else {
              return endResult;
         }
    }
    return endResult;
}

/**
 * IRD Function: GetStringKey(key,list)
 *  Return value type: STRING. Parses out the value for a specified key from a key-value list. 
 *  If the key searched for does not appear in the list, the function returns the empty string. 
 *  If the key is repeated in the list, its first occurrence is retrieved.
 */
function irdGetStringKey(key,list){
	var endResult = "";
	var sArray;
	var splitItem;
	var tempItem;
	var i;
	
    sArray = list.split("\|");
    for (i=0; i<sArray.length; i++) {
    	tempItem = sArray[i];
    	splitItem = tempItem.split(":");
    	if(splitItem[0].match(key)){
    			return splitItem[1];
    	}
    }
   
    return endResult;
}


/**
 * IRD Function: ListGetInteger(list,key)
 *  Return value type: INTEGER. This function extracts integer value from a key-value list by index starting from 1. 
 *  If index exceeds list size, the function returns 0.
 * @param list
 * @param key
 * @return
 */ 

function irdListGetInteger(index,list){
	var endResult = 0;
	var sArray;
	var tempItem;
	var splitItem;
	var index;
	
    sArray = list.split("\|");
    index = index - 1;
    if (index<=0 && index <= sArray.length) {
    	tempItem = sArray[index];
    	splitItem = tempItem.split(":");
        if (isNaN(splitItem[1])) {
           return endResult;
        } else {
           return splitItem[1];
        }
    }
    return endResult;
}

/**
 *  IRD Function: ListGetKey(index,list)
 *  Return value type: STRING. 
 *  This function extracts key from a key-value list by index starting from 1. 
 *  If index exceeds list size, the function returns the empty string.
 * @param index
 * @param list
 * @return
 */
function irdListGetKey(index,list){
	var endResult = "";
	var sArray;
	var tempItem;
	var splitItem;
	var index;
	
    sArray = list.split("\|");
    index = index -1 ;
    
    if (index>=0 && index <= sArray.length) {
    	tempItem = sArray[index];
    	splitItem = tempItem.split(":");
        return splitItem[0];
        }
    return endResult;
}

/**
 * IRD Function: ListGetSize(index,bDoNotCountEmptyItems)
 *  Return value type: INTEGER. 
 *  This function returns number of pairs in key-value list. 
 *  Second parameter if set to true results in items presented as empty strings will not be counted. 
 */
function irdListGetSize(list,bDoNotCountEmptyItems){
	var endResult = 0;
	var sArray;
	var tempItem;
	var splitItem;
	var i;
	
    sArray = list.split("\|");
    endResult = sArray.length;
    
    if(bDoNotCountEmptyItems){
    	for (i=0; i<sArray.length; i++) {
    		tempItem = sArray[i];
        	splitItem = tempItem.split(":");
        	if(splitItem[0].match("")){
        		endResult = endResult - 1;
        	}
    	}
    }
    
    return endResult;
}

/**
 * IRD Function: ListGetString(index,list)
 *  Return value type: STRING. 
 *  This function extracts string value from a key-value list by index starting from 1. 
 *  If index exceeds list size, the function returns the empty string.
 * @param index
 * @param list
 * @return
 */
function irdListGetString(index,list){
	var endResult = "";
	var sArray;
	var tempItem;
	var splitItem;
	var index;
	
    sArray = list.split("\|");
    index = index - 1;
    if (index<=0 && index <= sArray.length) {
    	tempItem = sArray[index];
    	splitItem = tempItem.split(":");
        return splitItem[1];
    }
    return endResult;
}
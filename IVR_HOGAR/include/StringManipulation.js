var StringManipulation_version = "8.1.4";

/**
 * ECMA implementation for IRD String Manipulation Functions
 */

/** 
* Function to concatenate two strings
*  Replacement to IRD Function Name: Cat
*  Return value type: STRING. 
*  This function returns the concatenation of the strings supplied as arguments.
*  e.g. irdCat('Good ', 'Day ', 'Sir')
*/
function irdCat(strA) {
	var resultString = "";
	var i = 0;

	if (strA != null && arguments.length > 1) {

		for (i = 0; i <= arguments.length - 1; i++) {
			resultString = resultString + arguments[i];
		}
	} else {
		return strA;
	}

	return resultString;
}

/**
*  Function to get the Index of a search string
*  Replacement to IRD Function Name: "StrAsciiBreak"
*  Return value type: INTEGER. 
*  This function returns the index of the first occurrence in String of any of the characters in Char Set, 
*  such that its index is greater than or equal to the value of From Index. 
*  The index of the first character in a string is 1. 
*  If From Index is negative, the returned value is the same as if From Index had been 1, 
*  i. e. the search proceeds from the first character of the string.
*/
function irdStrAsciiBreak(orgString, srchString, nOffset) {
	var nEndRes = -1;
	if(orgString!= null && srchString!= null){
		nEndRes = orgString.indexOf(srchString, nOffset);	
		nEndRes = nEndRes + 1;
	}
	
	return nEndRes;
}

/**
*  Function to parse out a string value from a string consisting of multiple substrings
*  Replacement to IRD Function Name: "StrAsciiTok"
*  Return value type: STRING. 
*  The purpose of this function is to parse out a string value from a string consisting of 
*  multiple substrings delimited by characters functioning as separators; 
*  the separators are the characters occurring in Char Set.
*/
function irdStrAsciiTok(orgString, seprString, nOffset) {
	var resultString = "";
		
	if(orgString != null && seprString != null) {
		resultString = orgString.split(seprString);
	}
	return resultString;
}

/**
* Function to find the position of first occurrence of a string
* Replacement to IRD Function Name: "StrChar"
* Return value type: INTEGER. 
* This function returns the index of the first character of the first 
* occurrence of Character as a substring of the tail segment of String starting at From Index. 
* The index of the first character in a string is 1. 
* If From Index is negative, the returned value is the same as if From Index had been 1, 
* i.e. the search proceeds from the first character of the string.
*/
function irdStrChar(orgString, srchString, nOffset) {
	var nEndRes = -1;
	if(orgString != null && srchString != null) 
	{
		nEndRes = orgString.indexOf(srchString, nOffset);	
		nEndRes = nEndRes + 1;
	}
	return nEndRes;
}

/**
* Function to get the character at the specified index
* Replacement to IRD Function Name: "StrGetChar"
* Return value type: STRING. 
* This function returns the character with a specified index in a string. 
* If Index is negative or greater than the length of String, the function returns the empty string.
*/
function irdStrGetChar(orgString, nIndex)
{
	var resultString = "";
	if(orgString != null && nIndex != null)
	{
		resultString = orgString.charAt(nIndex);
	}
return resultString;
}

/**
* Function to get the string length
* Replacement to IRD Function Name: "StrLen"
* Return value type: INTEGER. 
* This function returns the length of the string supplied as an argument, 
* i.e. the number of characters in the string.
*/
function irdStrLen(orgString)
{
	var nIndex = -1;
	if(orgString != null)
	{
		nIndex = orgString.length;
	}
	return nIndex;
}

/**
* Function to replace old string with a given string
* Replacement to IRD Function Name: "StrReplace"
* Return value type: STRING. 
* This function returns copy of string Source where all occurrences of 
* the string To Find are replaced with the string Replace With.
*/
function irdStrReplace(orgString, findString, replaceString)
{
	var resultString = "";
	if(orgString != null && findString != null && replaceString != null)
	{
	resultString = orgString.replace(findString, replaceString);
	}
	return resultString;
}

/**
* Function to find the given string from the original string
* Replacement to IRD Function Name: "StrStr"
* Return value type: STRING. 
* This function finds the first occurrence of the string To Find as a substring of Source, 
* and returns the tail segment of Source starting with that occurrence. 
* If To Find is not a substring of Source, the empty string is returned.
*/	
function irdStrStr(orgString, toFindString)
{
	var resultString = "";
	var nMatchPos = -1;
	var nStrlen = -1;
	if(orgString != null && toFindString != null)
	{
		nMatchPos = orgString.search(toFindString);
		if(nMatchPos != -1)
		{
	    nStrlen = orgString.length;
		resultString = orgString.slice(nMatchPos);
		}
	}
	return resultString;
}

/**
* Function to get the Substring between indexes
* Replacement to IRD Function Name: "StrSub"
* Return value type: STRING. 
* This function returns the substring of Source beginning with the character 
* with index First and ending with the character with index Last.
*/
function irdStrSub(orgString, nFromIndex, nToIndex,fromTheEnd)
{
	var resultString = "";
	var splitext;
	var revertext;
	
	if(orgString != null && nFromIndex != null && nToIndex != null)
	{
		if(fromTheEnd){
			splitext = orgString.split("");
			revertext = splitext.reverse();
			orgString= revertext.join("");
		}
	resultString = orgString.substring(nFromIndex, nToIndex);	
	}
	return resultString;
}

/**
* Function to convert string to lower case
* Replacement to IRD Function Name: "StrToLower"
* Return value type: STRING. 
* This function converts a string to lower case.
*/	
function irdStrToLower(orgString)
{
	var resultString = "";
	if(orgString != null)
	{
		resultString = orgString.toLowerCase();
	}
	return resultString;
}

/**
* Function to convert string to Upper Case
* Replacement to IRD Function Name: "StrToUpper"
* Return value type: STRING. 
* This function converts a string to upper case.
*/
function irdStrToUpper(orgString)
{
	var resultString ="";
	if(orgString != null)
	{
	resultString = orgString.toUpperCase();
	}
	return resultString;
}
var version = "8.1.44";

/**
 * This function will call the locale specific PlayBuiltinType function.
 * 
 * @param locale The locale language ID. e.g. en-US
 * @param voxFileDir The audio file directory.
 * @param value The value to translate.
 * @param type The type of the value.
 * @param format The output format [optional].
 * 
 * @return An array of URL strings.
 */
function PlayPromptSwitch(locale, voxFileDir, value, type, format)
{
	// The audio directory path for the specified language.
	var promptUrl = voxFileDir + "/" + locale + "/";

	// removes dash(-) and single quotes(').
	// i.e. 'en-US' >>> enUS
	locale = locale.replace('-','').replace(/'/g,"");

	var resultArray;
	// Invoke the locale specific PlayBuiltinType function. 
	// i.e. enUSPlayBuiltinType() from the en-US.js file.
	if (!(typeof format == 'undefined'))
		eval( "resultArray = " + locale + "PlayBuiltinType(\'"+ value +"\', \'"+ type +"\', \'"+ promptUrl +"\', \'"+ format +"\')" );
	else
		eval( "resultArray = " + locale + "PlayBuiltinType(\'"+ value +"\', \'"+ type +"\', \'"+ promptUrl +"\')" );

	return resultArray;
}

/**
 * This function will call the local specific customprompts.js file methods.
 * @param locale
 * @param value
 * @param functionName
 * @param params
 * @return
 */
function PlayCustomPromptSwitch(locale,value, functionName,params)
{
	locale = locale.replace('-','').replace(/'/g,"");
	var result;
    var paramString="";
    
	for (var index = 0; index < params.length; index++) {
		paramString = paramString+",\'"+params[index]+"\'"; 
	}
	
	eval( "result = " + locale +functionName.substr(4)+"(\'"+ value +"\'"+paramString+")"); 

	return result;
}
/**
 * This function will call the local specific customprompts.js file methods which doesn't have Custom arguments.
 * @param locale
 * @param value
 * @param functionName
 * @return
 */
function PlayCustomPromptSwitchNoParams(locale,value, functionName)
{
	locale = locale.replace('-','').replace(/'/g,"");
	var result;
    
	eval( "result = " + locale +functionName.substr(4)+"(\'"+ value +"\')"); 

	return result;
}

/**
 * Dummy class to hold constants
 */
function Format() {}

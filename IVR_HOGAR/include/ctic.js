var ctic_version = "8.1.4";
/*
************************************************
 * ctic.js
 * For common utility functions
 * Note: Do not put any comments as the starting line
************************************************
*/

//--- Checking validity of a results received after a <receive> -----------
function isCTICResult ( lastMessage )
{
	var strOut = "false";
	
	if( lastMessage != undefined ) {
	
		// 1. application.lastmessage$.sourcetype == "sip"
		if( lastMessage.sourcetype != undefined && lastMessage.sourcetype.indexOf("sip") >= 0 ) {
			
		// 2. application.lastmessage$.contenttype == "application/x-www-form-urlencoded;charset=utf-8"
			if(lastMessage.contenttype != undefined && lastMessage.contenttype.indexOf("application/x-www-form-urlencoded;charset=utf-8") >= 0) {
				strOut = "true";
			}
		}
	}
	
	return strOut;
}

//--- Determine if this is a CTIC call from X-Genesys-GVP-Session-ID -----------
function isCTICCall ( strUserData )
{
	// X-Genesys-GVP-Session-ID: 800DB7A5-33C6-45CB-7E9F-410507B7097A;gvp.rm.datanodes=1;gvp.rm.cti-call=1
	if ( strUserData != undefined) {
		if ( strUserData.indexOf( "gvp.rm.cti-call=1" ) >= 0 ) {
			return "true";
		}
	}

	return "false";
}

//--- Retrieve keys from the associative array containing CTIC returned results -----------
function utilGetReceiveDataValue( strKeyName, arrReceiveData )
{
	var strOut = "";
            
	if ( arrReceiveData != undefined && strKeyName != undefined ) {
		strOut = unescape( arrReceiveData[ unescape( strKeyName ) ] );
		if (strKeyName == "Result") {
			strOut = strOut.toUpperCase();
		}
	}
	else {
		strOut = undefined;
	}  
	return strOut;
}

function getUserDataArray( dataMessage )
{
	if(dataMessage == undefined)
		return undefined;
	
	var resultArray = new Array();
	var message = dataMessage.split("&");

	var startIndex = 0;
	var endIndex = 0;

	var keyName;
	var keyValue;
	var arrayIndex;
	
	for (arrayIndex in message)	
	{
		if(message[arrayIndex]!=null && message[arrayIndex].length >0)
		{
			startIndex = message[arrayIndex].indexOf ("=", startIndex) + 1;
			endIndex = message[arrayIndex].length;
			
			keyName = message[arrayIndex].substring (0, startIndex-1);
			keyValue = message[arrayIndex].substring (startIndex, endIndex);
	
			if(keyValue != undefined) {
				resultArray[keyName] = unescape("" + keyValue);
			}
			
			startIndex = endIndex = 0;
		}
	}
	return resultArray;
}

function getUserDataVariable(varName){
    
	if (typeof session.com.genesyslab.userdata != 'undefined' && session.com.genesyslab.userdata != null) {
    
    	if(varName !=undefined){
    		if(AppState.USE_LCASE_USERDATAKEY == 1){
    				return session.com.genesyslab.userdata[varName.toLowerCase()];
    		}
    		else{
    			return session.com.genesyslab.userdata[varName];
    		}
    	}
        
	}
	return undefined;
}

function getRequestURIVariable(varName){
	if(varName !=undefined){
		return session.connection.protocol.sip.requesturi[varName];
	}
	return undefined;
}

function initSystemTypeVariable(varName,defaultValue){
	if(session.connection.protocol.sip.requesturi != undefined && getRequestURIVariable(varName)){
			return getRequestURIVariable(varName);
	}
	else{
			return defaultValue;
	}
	
}

function initUserInputTypeVariable(varName,defaultValue,returnDefaultValue){
	
	var returnValue = undefined;
	if(isCTICCall(AppState.GVPSessionID) == 'true' || isDebuggerCall()){
		returnValue = getRequestURIVariable(varName);
	}else{
		returnValue = getUserDataVariable(varName);
	}
	if(returnValue==undefined){
		if(returnDefaultValue){
			return defaultValue;
		}
		else{
			return returnValue;
		}
	}
	else{
		return returnValue;
	}
}

function isDebuggerCall(){
	if(session.connection.protocol.sip.requesturi != undefined){
		var gvpObj = session.connection.protocol.sip.requesturi['gvp'];
		if(typeof gvpObj != 'undefined' && gvpObj != null && gvpObj.debug == 'true'){
			return true;
		}
	}
	return false;
}

//OCS methods
function getCallflowRecordURI() {
    if (typeof session.com.genesyslab.userdata != 'undefined' && typeof session.com.genesyslab.userdata.GSW_RECORD_URI!='undefined'){ 
    	/*pattern: http://ocs.us.int.genesyslab.com:8080/records/15*/
        return session.com.genesyslab.userdata.GSW_RECORD_URI;
    }
    return undefined;

}
function getCallflowOCSURI() {
	var recordURI = getCallflowRecordURI();
	if (typeof recordURI == 'undefined' || recordURI == null) return undefined;
	var ocsUri = recordURI.match(/(https?:\x2F\x2F[^\x2F]*)/i);
	return (ocsUri != null && ocsUri.length > 0) ? ocsUri[0] : '';
}
function getCallflowOCSRecord() {
    if (typeof session.com.genesyslab.userdata != 'undefined' && typeof session.com.genesyslab.userdata.GSW_RECORD_HANDLE != 'undefined') {
    return session.com.genesyslab.userdata.GSW_RECORD_HANDLE;
}


	var ocsURI = getCallflowOCSURI();
	if (typeof ocsURI == 'undefined' || ocsURI == null) return undefined;
	var prefix = ocsURI + '/records/';
	
	var recordURI = getCallflowRecordURI();
	if (recordURI.indexOf(prefix) != 0) return '';
	return recordURI.substring(prefix.length);
}



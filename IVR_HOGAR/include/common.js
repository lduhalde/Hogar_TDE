var common_version = "8.1.4";
/*
************************************************
 * common.js
 * For common utility functions
 * Note: Do not put any comments as the starting line
************************************************
*/

// Check is the value is a URI
function isURIResource(value)
{
    var isURI = false;
    
    if (value.length == 0)
    	return false;
   
   value = value.toLowerCase();
   
   if (value.match("^http://*|^https://*|^file://*|^rtsp://*|^rtsps://*")) 
   		isURI = true;
   else if (value.indexOf(".jsp") != -1)
   		isURI = true;
   else if (value.substring(0,1) == "." || value.substring(0, 2) == "..")
   		isURI = true;

   return isURI;
}

function getGrammarURI(value,isDTMF)
{
    var grammarURI = "";
    
    if (isURIResource(value))
    	grammarURI = encodeURI(value);
    else if (isDTMF == false) //Voice Grammar
    	grammarURI = AppState.GRAMMARFILEDIR + '/' + AppState.APP_ASR_LANGUAGE + '/' + value;
    else //DTMF
    	grammarURI = AppState.GRAMMARFILEDIR + '/DTMF/' + value;
    
    return grammarURI;
}

function getGBuilderGrammarURI(value, isDTMF)
{
	if ( value == undefined || (value.indexOf("/") == -1 && value.indexOf("\\") == -1 ) ) {
		return value;
	}
	
	var	nSlashPos		= -1;
	var strGrammarURI	= "";
	var strGrammarFile	= "";
		
	nSlashPos = value.lastIndexOf( "/" );
	
	strGrammarFile = value.substr( nSlashPos, value.length - nSlashPos - 1 );
	strGrammarFile = strGrammarFile.substr( 0, strGrammarFile.lastIndexOf(".") );
	strGrammarFile += ".grxml";
		
	// pick up the grxml file name from the input value. GRXML files are expected to be in the 
	// "Resources/Grammars/<language> folder only
	strGrammarURI = AppState.GRAMMARFILEDIR + "/" + AppState.APP_ASR_LANGUAGE + "/" + strGrammarFile;
	strGrammarURI = strGrammarURI.replace( "//", "/" );
	
    if ( isDTMF == true ) {
    	strGrammarURI = strGrammarURI.replace( '/' + AppState.APP_LANGUAGE + '/', '/' + "DTMF" + '/');
    }
    
    return strGrammarURI;
}


function getAudioURI(value)
{
    var audioURI = "";
    
    if (isURIResource(value))
    	audioURI = value;
    else
    	audioURI = AppState.VOXFILEDIR + '/' + AppState.APP_LANGUAGE + '/' + value;
    
    return audioURI;
}

function checkIsObject(result) 
{
	if(result instanceof Object){
		return true;
	}
	return false;
}

function getDNIS()
{

	var address = "";
	
	// OCN takes precedence if available
	var ocn = session.connection.redirect[0];
	if (typeof ocn != 'undefined' && ocn != null) {
		address = ocn.uri;
	} 
	
	if (address == "") {
		address = session.connection.local.uri;
	}

	return getUserPart(address);
}

function getANI()
{

	var address = "";
	
	// this header takes precedence if available
	var pai = session.connection.protocol.sip.headers['p-asserted-identity'];
	if (typeof pai != 'undefined' && pai != null) {
		address = pai;
	} 
	
	if (address == "") {
		address = session.connection.remote.uri;
	}

	return getUserPart(address);
}

function getSIPHeaderValue(headerName)
{
	if (typeof session.connection.protocol.sip.headers == 'undefined' || session.connection.protocol.sip.headers == null) {
		return null;
	}
	
	// as per section 2.4 of RFC 5552, the SIP header names are converted to lower case
	var headerNameLC = headerName.toLowerCase();
	
	return session.connection.protocol.sip.headers[headerNameLC];
}

function getUserPart(address)
{
	var re = new RegExp("(?:sip|sips|tel):([^@]+)@.*");
	var result = re.exec(address);
	
	if (result == null || result.length < 2) return "";
	
	return result[1];

}

// try to convert a string to a number
function toNumber(value) { 
	
	if (typeof value == "number") {
		return value;
	}
	
	if (typeof value == "string") {
		var num = new Number(value);
		
		// test for not-a-number
		if (!isNaN(num)) {
			return num.valueOf();
		} else {
			return value;
		}
	}
	
	// return the original value by default
	return value;
	
}

function resetLanguage() {
    AppState.APP_LANGUAGE = AppState.PREV_APP_LANGUAGE;
    AppState.APP_ASR_LANGUAGE = AppState.PREV_APP_ASR_LANGUAGE;
}

function getCaptureLocation(location, prefix) {
	
	if (location.length == 0) return prefix;
	
	var endChar = location.charAt(location.length - 1);
	if (endChar != '\\' || endChar != '/') {
		location = location + '/';
	}
	
	return location + prefix;
	
}

function br_getLoginRequest(username, password) {
	
	var loginRequest = new Object();
	var loginRequestBody = new Object();
	            
	loginRequestBody.username = username;
	loginRequestBody.password = password;
	            
	loginRequest["login-request"] = loginRequestBody;
	
	return loginRequest;
	
}

function br_getSessionId(loginResponse) {
	
	if (typeof loginResponse == 'undefined' || loginResponse == null ||
		typeof loginResponse['login-response'] == 'undefined' || loginResponse == null ||
		typeof loginResponse['login-response']['sessionid'] == 'undefined' || loginResponse['login-response']['sessionid'] == null) {
		
		return null;
	}
	
	return loginResponse['login-response']['sessionid'];
}

function br_getPackageInfoRequest(sessionid) {
	
	var getPackageInfoRequest = new Object();
	var getPackageInfoRequestBody = new Object();
			       
	getPackageInfoRequestBody.sessionid = sessionid;
	
	getPackageInfoRequest["get-package-info-request"] = getPackageInfoRequestBody;
	
	return getPackageInfoRequest;
}

function br_getLocation(packageInfoResponse) {
	
	if (typeof packageInfoResponse == 'undefined' || packageInfoResponse == null ||
		typeof packageInfoResponse['get-package-info-response'] == 'undefined' || 
		packageInfoResponse['get-package-info-response'] == null || 
		typeof packageInfoResponse['get-package-info-response']['package-info'] == 'undefined' || 
		packageInfoResponse['get-package-info-response']['package-info'] == null || 
		typeof packageInfoResponse['get-package-info-response']['package-info']['location'] == 'undefined' || 
		packageInfoResponse['get-package-info-response']['package-info']['location'] == null)
	{
		return null;
	}
	
	var locationList = 	packageInfoResponse['get-package-info-response']['package-info']['location'];
	if (locationList.length == 0) {
		return null;
	}
	
	if (typeof locationList == 'string') {
		return locationList;
	} else {
		return locationList[0];
	}
}

function br_getLogoutRequest(sessionid) {
	
	var logoutRequest = new Object();
	var logoutRequestBody = new Object();
    
	logoutRequestBody.sessionid = sessionid;
	
	logoutRequest["logout-request"] = logoutRequestBody;
	
	return logoutRequest;
}

function br_initKbRequest() {
	
	var kbRequest = new Object();
	var kbRequestBody = new Object();
	    
	var inOutFacts = new Object();
	var namedFact = new Array();
	    
	inOutFacts['named-fact'] = namedFact;
	kbRequestBody['inOutFacts'] = inOutFacts;
	    
	kbRequest['knowledgebase-request'] = kbRequestBody;
	
	return kbRequest;
}

function br_addFact(kbRequest, fact) {
	
	var fArray = kbRequest['knowledgebase-request']['inOutFacts']['named-fact'];
	fArray.push(fact);
	
}

function br_getResultFacts(result) {
	
	if (typeof result == 'undefined' || result == null ||
		typeof result['knowledgebase-response'] == 'undefined' || result['knowledgebase-response'] == null || 
		typeof result['knowledgebase-response']['inOutFacts'] == 'undefined' || result['knowledgebase-response']['inOutFacts'] == null || 
		typeof result['knowledgebase-response']['inOutFacts']['named-fact'] == 'undefined' || 
		result['knowledgebase-response']['inOutFacts']['named-fact'] == null) {
		
		return null;
	}
	
	return result['knowledgebase-response']['inOutFacts']['named-fact'];
}

function getContextServicesUserName() {
	return (typeof _data.context_management_services_username == 'undefined' ? '' : _data.context_management_services_username);
}

function getContextServicesPassword() {
	return (typeof _data.context_management_services_password == 'undefined' ? '' : _data.context_management_services_password);
}

function getBaseURL() {
	var url = __GetDocumentURL();
	var iPos = url.lastIndexOf("/", url.length);
	return url.substring(0, iPos+1);
}

function getRelativePathURL() {
	var baseURL = getBaseURL();
	var iPos = baseURL.lastIndexOf("//");
	var arr = new Array(3);
	arr[0] = baseURL.substring(0,iPos+2);
	var tempStr  = baseURL.substring(iPos+2,baseURL.length);
	iPos = tempStr.indexOf("/");
	arr[1] = tempStr.substring(0,iPos+1);
	tempStr  = tempStr.substring(iPos+1,tempStr.length);
	iPos = tempStr.indexOf("/");
	arr[2] = tempStr.substring(0,iPos+1);
	return arr.join("");
}

// event/exception handling methods

function resetEvents() {
	_data.system.context.events = [];
	_data.system.context.errors = [];

	_data.system.context.LastErrorEvent = '';
	_data.system.context.LastErrorEventName = '';
	_data.system.context.LastErrorDescription = '';
}

function storeEvent(blockName, event) {
	if (_data.system.context.currentBlock != blockName) resetEvents();
	_data.system.context.currentBlock = blockName;
	_data.system.context.events[_data.system.context.events.length] = event;
}

function storeException(blockName, event) {
	if (_data.system.context.currentBlock != blockName) resetEvents();
	_data.system.context.currentBlock = blockName;
	_data.system.context.errors[_data.system.context.errors.length] = event;
	
	_data.system.context.LastErrorEvent = _event;
	_data.system.context.LastErrorEventName = _event.name;
	_data.system.context.LastErrorDescription = '';
	if (typeof _event.data != 'undefined' && typeof _event.data.description != 'undefined') {
		_data.system.context.LastErrorDescription = _event.data.description;
	}
}

function getLastEvent() {
	return typeof _data.system.context.events == 'undefined' || _data.system.context.events.length == 0
		? undefined : _data.system.context.events[_data.system.context.events.length - 1];
}

function getEvent(eventName) {
	if (typeof eventName == 'undefined') return undefined;
	if (typeof _data.system.context.events == 'undefined' || _data.system.context.events.length == 0) return undefined;
	for (var index = 0; index < _data.system.context.events.length; index++) {
		var evt = _data.system.context.events[index];
		if (evt.name == eventName) return evt;
	}
	return undefined;
}

function getLastEventData(data) {
	var lastEvent = getLastEvent();
	return typeof lastEvent == 'undefined' || typeof lastEvent.data == 'undefined'
		? undefined
		: typeof data == 'undefined' ? lastEvent.data : lastEvent.data[data];
}

function getEventData(eventName, data) {
	var evt = getEvent(eventName);
	return typeof evt == 'undefined' || typeof evt.data == 'undefined'
		? undefined
		: typeof data == 'undefined' ? evt.data : evt.data[data];
}

function getLastException() {
	return typeof _data.system.context.errors == 'undefined' || _data.system.context.errors.length == 0
		? undefined : _data.system.context.errors[_data.system.context.errors.length - 1];
}

function getException(exceptionName) {
	if (typeof exceptionName == 'undefined') return undefined;
	if (typeof _data.system.context.errors == 'undefined' || _data.system.context.errors.length == 0) return undefined;
	for (var index = 0; index < _data.system.context.errors.length; index++) {
		var err = _data.system.context.errors[index];
		if (err.name == exceptionName) return err;
	}
	return undefined;
}

function getLastExceptionData(data) {
	var lastException = getLastException();
	return typeof lastException == 'undefined' || typeof lastException.data == 'undefined'
		? undefined
		: typeof data == 'undefined' ? lastException.data : lastException.data[data];
}

function getExceptionData(exceptionName, data) {
	var err = getException(exceptionName);
	return typeof err == 'undefined' || typeof err.data == 'undefined'
		? undefined
		: typeof data == 'undefined' ? err.data : err.data[data];
}

function storeUnhandledEvent() {
	_data.system.context.uncaughtApplicationEvents[_data.system.context.uncaughtApplicationEvents.length] = _event;
}

function isEventHandledByApplication() {
	return (_data.system.context.uncaughtApplicationEvents.indexOf(_event) == -1);
}

// OPM methods
function getOPMParameters() {
	
	if (typeof _data.OPM_Transaction != 'undefined'){
		return _genesys.session.getListItemValue(_data.OPM_Transaction,'OPM');
	}
	return undefined;
}

function getTenantDBID(gvpSessionID, p)
{
	var key='gvp.rm.tenant-id=';
	if(typeof gvpSessionID != 'undefined'){
	  	var head=gvpSessionID.indexOf(key);
	  	
		var idstring=gvpSessionID.substr(head+key.length, gvpSessionID.length-head-key.length);
		var end = idstring.indexOf('_');
		var tenantstring=idstring.substr(0,end);
		if(tenantstring.lastIndexOf('.')<0);
		
		var temp=tenantstring;
		var temp=tenantstring.substr(tenantstring.lastIndexOf('.')+1);
	
		if(typeof p!=undefined && p<30) return "1";  // Personality ID <30, use environment/admin level voice files
		return temp;
	}
	return "1";
	
}

//OCS methods
function getSeconds(time/*pattern: HH:mm*/) {
	var strs = time.split(':');
	if (strs.length != 2) return null;
	if (isNaN(strs[0]) || isNaN(strs[1])) return null;
	return (strs[0] * 3600) + (strs[1] * 60);
}

//toISOString is not implemented in ORS 8.1.2 
if (!Date.prototype.toISOString) {
	Date.prototype.toISOString = function() {
		function pad10(n) {
			return n < 10 ? '0' + n : n
		}
		function pad100(n) {
			return n < 10 ? '00' + n : (n < 100 ? '0' + n : n)
		}
		return this.getUTCFullYear() + '-'
			+ pad10(this.getUTCMonth() + 1) + '-'
			+ pad10(this.getUTCDate()) + 'T'
			+ pad10(this.getUTCHours()) + ':'
			+ pad10(this.getUTCMinutes()) + ':'
			+ pad10(this.getUTCSeconds()) + '.'
			+ pad100(this.getUTCMilliseconds()) + 'Z';
	};
}
//parse does not support ISO dates in ORS 8.1.2.
//so adding this for convenience
if (!Date.fromISOString) {
	Date.fromISOString = function(q) {
		var reg = new RegExp("^[0-9]{4}-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])T(2[0-3]|[0-1][0-9]):[0-5][0-9]:[0-5][0-9].[0-9]{3}Z$", "g");
		if (!reg.test(q)) {
			throw ("Invalid format for UTC timestamp : " + q)
		}
		return new Date(Date.UTC(Number(q.substring(0, 4)),
								Number(q.substring(5, 7)) - 1,
								Number(q.substring(8, 10)),
								Number(q.substring(11, 13)),
								Number(q.substring(14, 16)),
								Number(q.substring(17, 19))));
	};
}

//Returns the name of the view that the interaction was pulled from.
function getInteractionView() {
	var ixnId = _data.system.context.InteractionID;
	if (typeof ixnId == 'undefined' || typeof _genesys.ixn.interactions[ixnId] == 'undefined') {
		return null;
	}
	// first priority is msgbased, and if that doesn't exist, fall back to xdata.
	if (typeof _genesys.ixn.interactions[ixnId].msgbased != 'undefined' && 
				typeof _genesys.ixn.interactions[ixnId].msgbased.view != 'undefined') {
		return _genesys.ixn.interactions[ixnId].msgbased.view;
	} else if (typeof _genesys.ixn.interactions[ixnId].xdata != 'undefined' && 
			   typeof _genesys.ixn.interactions[ixnId].xdata.ixn_queue_view != 'undefined') {
		return _genesys.ixn.interactions[ixnId].xdata.ixn_queue_view;
	} else {
		return null;
	}
}

// implements view segmentation
// Returns true if the interaction's view matches the given viewName.
function transitionOnView(viewName) {
	var ixnView = getInteractionView();
	if (ixnView != null) {
		return viewName == ixnView;
	}
	return false;
}

function decodeBinary(hexArray) {
	if (typeof hexArray == 'undefined') throw "decodeBinary: undefined array parameter";
	if (!Array.isArray(hexArray)) throw "decodeBinary: not an array parameter";
	var result = "";
    for (var index = 0; index < hexArray.length; index++) {
		var hex = hexArray[index];
		if (hex == 0) break;		//string terminator
		if (isNaN(hex)) throw "decodeBinary: not a number at index:" + index;
		result += String.fromCharCode(hex);
	}
	return result;
}

// Returns the name of the view that the interaction was pulled from.
function getInteractionView() {
	var ixnId = _data.system.context.InteractionID;
	if (typeof ixnId == 'undefined' || typeof _genesys.ixn.interactions[ixnId] == 'undefined') {
		return null;
	}
	// first priority is msgbased, and if that doesn't exist, fall back to xdata.
	if (typeof _genesys.ixn.interactions[ixnId].msgbased != 'undefined' && 
		typeof _genesys.ixn.interactions[ixnId].msgbased.view != 'undefined') {
		
		return _genesys.ixn.interactions[ixnId].msgbased.view;
	} else if (typeof _genesys.ixn.interactions[ixnId].xdata != 'undefined' && 
			   typeof _genesys.ixn.interactions[ixnId].xdata.ixn_queue_view != 'undefined') {
		return _genesys.ixn.interactions[ixnId].xdata.ixn_queue_view;
	} else {
		return null;
	}
}


// implements view segmentation
// Returns true if the interaction's view matches the given viewName.
function transitionOnView(viewName) {
	var ixnView = getInteractionView();
	if (ixnView != null) {
		return viewName == ixnView;
	}
	return false;
}

function generateComposerTrtRequestID() {
	return Math.random().toString().substring(2,7);
}

function setNxtTreatmentBlockName(blockName){
	_data.gvpNxtTreatmentBlockName = blockName;			
}

function clearNxtTreatmentBlockName(){
	_data.gvpNxtTreatmentBlockName = undefined;			
}

function isGVPTreatmentPending(){
	if(_data.gvpNxtTreatmentBlockName==undefined){
		return false;
	}
	return true;
}

var irdEnumMapper_version = "8.1.4";

/**
 * getMediaTypeEnum utility Function.
 * Used for IRD FindServiceObjective[] function.
 */
function getMediaTypeEnum(param) {
	
	if (param != null) {

		switch (param) {
		case "TMediaVoice":
			return _genesys.ixn.mediaType.TMediaVoice;
		case "TMediaVoIP":
			return _genesys.ixn.mediaType.TMediaVoIP;
		case "TMediaEMail":
			return _genesys.ixn.mediaType.TMediaEMail;
		case "TMediaVMail":
			return _genesys.ixn.mediaType.TMediaVMail;
		case "TMediaSMail":
			return _genesys.ixn.mediaType.TMediaSMail;
		case "TMediaChat":
			return _genesys.ixn.mediaType.TMediaChat;
		case "TMediaVideo":
			return _genesys.ixn.mediaType.TMediaVideo;
		case "TMediaCobrowsing":
			return _genesys.ixn.mediaType.TMediaCobrowsing;
		case "TMediaWhiteboard":
			return _genesys.ixn.mediaType.TMediaWhiteboard;
		case "TMediaAppSharing":
			return _genesys.ixn.mediaType.TMediaAppSharing;
		case "TMediaWebform":
			return _genesys.ixn.mediaType.TMediaWebform;
		case "TMediaWorkItem":
			return _genesys.ixn.mediaType.TMediaWorkItem;
		case "TMediaCallback":
			return _genesys.ixn.mediaType.TMediaCallback;
		case "TMediaFax":
			return _genesys.ixn.mediaType.TMediaFax;
		case "TMediaIMChat":
			return _genesys.ixn.mediaType.TMediaIMChat;
		case "TMediaBusinessEvent":
			return _genesys.ixn.mediaType.TMediaBusinessEvent;
		case "TMediaAlert":
			return _genesys.ixn.mediaType.TMediaAlert;
		case "TMediaSMS":
			return _genesys.ixn.mediaType.TMediaSMS;
		case "TMediaOutboundPreview":
			return _genesys.ixn.mediaType.TMediaOutboundPreview;
		case "TMediaOpenMedia":
			return _genesys.ixn.mediaType.TMediaOpenMedia;
		case "TMediaNativeSMS":
			return _genesys.ixn.mediaType.TMediaNativeSMS;
		}
	}
	return param;
}



/**
* getMediaTypeString For IRD UseMediaType function
*/
function getMediaTypeString(param1)
{
	if(param1!=null){
		if(param1 == "alert"){
			return "TMediaAlert";
		}
		else if(param1 == "appsharing"){
			return "TMediaAppSharing";
		}
		else if(param1 == "callback"){
			return "TMediaCallback";
		}
		else if(param1 == "chat"){
			return "TMediaChat";
		}
		else if(param1 == "cobrowsing"){
			return "TMediaCobrowsing";
		}
		else if(param1 == "email"){
			return "TMediaEMail";
		}
		else if(param1 == "fax"){
			return "TMediaFax";
		}
		else if(param1 == "imchat"){
			return "TMediaIMChat";
		}
		else if(param1 == "outboundpreview"){
			return "TMediaOutboundPreview";
		}
		else if(param1 == "smail"){
			return "TMediaSMail";
		}
		else if(param1 == "sms"){
			return "TMediaSMS";
		}
		else if(param1 == "video"){
			return "TMediaVideo";
		}
		else if(param1 == "vmail"){
			return "TMediaVMail";
		}
		else if(param1 == "voice"){
			return "TMediaVoice";
		}
		else if(param1 == "voip"){
			return "TMediaVoIP";
		}
		else if(param1 == "webform"){
			return "TMediaWebform";
		}
		else if(param1 == "whiteboard"){
			return "TMediaWhiteboard";
		}
		else if(param1 == "workitem"){
			return "TMediaWorkItem";
		}
}
	return param1;
}

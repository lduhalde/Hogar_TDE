<%@page trimDirectiveWhitespaces="true"%>
<%@page import="javax.naming.InitialContext,java.net.*,java.io.BufferedInputStream,java.io.*,java.util.*"%>
<%@page import="org.json.JSONTokener"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="org.json.JSONStringer"%>
<%@page import="org.apache.commons.codec.binary.Base64"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="javax.net.ssl.SSLContext"%>
<%@page import="javax.net.ssl.TrustManager"%>
<%@page import="javax.net.ssl.X509TrustManager"%>
<%@page import="javax.net.ssl.HttpsURLConnection"%>
<%@page import="javax.net.ssl.HostnameVerifier"%>
<%@page import="javax.net.ssl.SSLSession"%>
<%@page import="com.genesyslab.studio.backendlogic.BackendLogManager"%>
<%@page language="java"%>
<%@page contentType="application/json;charset=UTF-8"%>
<%!
Logger logger = BackendLogManager.getLogger("getWebRequest");

private String buildErrorResponse(String message) {
	logger.info("buildErrorResponse() in");
    try{
	    JSONStringer js = new JSONStringer();
    	js.object().key("errorMsg").value(message).endObject();
    	logger.error("buildErrorResponse:" + js.toString());
    	logger.info("buildErrorResponse() out");
    	return js.toString();
    } catch (JSONException e){
    	logger.error("JSON Exception:" + e.getMessage());
    	StringBuffer sb = new StringBuffer();
        sb.append("{'errorMsg':'error in building error response'}");     
        return sb.toString();
    }
};

private String buildTextResponse(String message) {
	logger.info("buildTextResponse() in");
    try{
	    JSONStringer js = new JSONStringer();
    	js.object().key("result").value(message).endObject();
    	logger.info("buildTextResponse:" + js.toString());
    	logger.info("buildTextResponse() out");
    	return js.toString();
    } catch (JSONException e){
    	logger.error("JSON Exception:" + e.getMessage());
    	return buildErrorResponse(e.getMessage());
    }
};
private String parseResultData(String initialData){
	logger.info("parseResultData() in");
	JSONObject result = null;
	try {
        // handle XML or JSON input	
        result = org.json.XML.toJSONObject(initialData);
        if (!(result.length() == 0)) {
            // XML input
            logger.info("Detected XML data: " + result.toString());
        	logger.info("parseResultData() out");
            return(result.toString());
        } else {
            // JSON input
            result = new JSONObject(initialData);
            logger.info("Detected JSON data, returning as-is: " + result.toString());
        	logger.info("parseResultData() out");
            return(result.toString());
        }
    } catch (Exception e) {
    	logger.error(BackendLogManager.printStackTrace(e));
        logger.error("Returning as plain text: " + initialData);
        return(buildTextResponse(initialData));
    }
};
%>
<%
    String urlStr = "";
    String protocol = "";
    String encType = "";
    boolean authenAccess = false;
    String userName = "";
    String password = "";
    HttpURLConnection con = null;
    String readTimeout = "20000"; // timeout in milliseconds
    String conTimeout = "20000"; // timeout in milliseconds
    String appJson = "application/json";
    String appUrlEncoded = "application/x-www-form-urlencoded";
    String textXml = "text/xml";
    String appXml = "application/xml";
    String textPlain = "text/plain";
    Boolean verifyJSON = true;
    
    logger.info("getWebRequest() in");
    BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(),"utf-8"));
    StringWriter writer = new StringWriter();
    char[] buf = new char[256];
    for (;;) {
    	int read = reader.read(buf);
    	if (read == -1) break;
    	
    	writer.write(buf, 0, read);
    }
    
    String jsonStr = writer.toString();
    
    JSONObject requestObj = new JSONObject(jsonStr);
    logger.info("requestObj: " + requestObj.toString());
    urlStr = requestObj.getString("WebUrl");
    protocol = requestObj.getString("Protocol");
    encType = requestObj.getString("Enctype");
    authenAccess = requestObj.getBoolean("AuthenAccess");
    verifyJSON = requestObj.getBoolean("VerifyJSON");
    if (authenAccess) {
    	userName = requestObj.optString("UserName");
    	password = requestObj.optString("Password");
    }
    StringBuilder sb1 = new StringBuilder();
    String paramName = "";
    String paramValue = "";
    String paramStr = "";
    int queryPos = urlStr.indexOf('?');
    if ((protocol.endsWith("get") || protocol.endsWith("delete")) && (queryPos > 0)) {
	    String queryString = urlStr.substring(queryPos + 1, urlStr.length());
	    urlStr = urlStr.substring(0, queryPos);
	    String[] pairs = queryString.split("&");
	    for (String pair : pairs) {
	        int pos = pair.indexOf('=');
         	if (pos == -1) {
         		paramName = pair;
         		paramValue = null;
	        } else {
	       		try {
	       			paramName = URLDecoder.decode(pair.substring(0, pos), "UTF-8");
	       			paramValue = URLDecoder.decode(pair.substring(pos+1, pair.length()), "UTF-8");            
	       	    } catch (Exception e) {
	       	    	logger.error(BackendLogManager.printStackTrace(e));
	       	    }
	       	}
           	if (sb1.length() > 0) {
               	sb1.append("&");
           	}
           	sb1.append(URLEncoder.encode(paramName, "UTF-8"))
	    		.append("=")
        		.append(URLEncoder.encode(paramValue, "UTF-8"));
		}
    }
    
    JSONObject parameters = requestObj.optJSONObject("Parameters");
    
    if (parameters != null) {
        for (Iterator<String> iter = parameters.keys(); iter.hasNext();) {
            paramName = iter.next();
            paramValue = parameters.getString(paramName);
            if (sb1.length() > 0) {
                sb1.append("&");
            }
            sb1.append(URLEncoder.encode(paramName, "UTF-8"))
	    		.append("=")
        		.append(URLEncoder.encode(paramValue, "UTF-8"));
        }
    }
    paramStr = sb1.toString();
    logger.info("paramStr: " + paramStr);

    if (urlStr.startsWith(".")) {

        urlStr = urlStr.substring(urlStr.indexOf("/") + 1, urlStr.length());
        String relativePath = "http://localhost:";
        relativePath += request.getServerPort();
        
        relativePath = relativePath + request.getRequestURI();
        relativePath = relativePath.substring(0, relativePath
                .indexOf("include"));
        urlStr = relativePath + urlStr;
        logger.info("urlStr: " + urlStr);
    }

    FileInputStream ipStr = null;
    try {
        
        Properties properties = new Properties();
        ipStr = new FileInputStream(request
                .getRealPath("/WEB-INF/composer.properties"));
        properties.load(ipStr);
        if (properties.getProperty("web.connectionTimeout") != null) {
            conTimeout = properties
                    .getProperty("web.connectionTimeout");
        }
        if (properties.getProperty("web.readTimeout") != null) {
            readTimeout = properties.getProperty("web.readTimeout");
        }
    } catch (Exception e) {
    	logger.warn("Could not read composer.properties file, using default timeouts", e);
    } finally {
        if (ipStr != null) {
            ipStr.close();
        }
    }
        
    try {
    	
        // the value passed from the block property overrides the 
        // global value in the composer.properties
        String timeout = requestObj.optString("Timeout");
        if (timeout != null && timeout.trim().length() > 0) {
            try {
                int timeoutInt = Integer.parseInt(timeout);
                if (timeoutInt != -1) {
                    conTimeout = Integer.toString(timeoutInt * 1000);
                    readTimeout = Integer.toString(timeoutInt * 1000);
                }
                
            } catch (NumberFormatException e) {
                // ignore an invalid value
            }
        }
        
    } catch (Exception e) {
    	logger.warn("Could not parse timeout property, using default timeouts", e);
    }
    logger.info("conTimeout: " + conTimeout + ", readTimeout: " + readTimeout);

    StringBuffer value = new StringBuffer("");

    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType) {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType) {
            logger.info("authType is " + authType);
            logger.info("cert issuers");
            for (int i = 0; i < certs.length; i++) {
                logger.info("\t"
                        + certs[i].getIssuerX500Principal().getName());
                logger.info("\t"
                        + certs[i].getIssuerDN().getName());
            }
        }
    } };

    try {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());
    } catch (Exception e) {
    	logger.error(BackendLogManager.printStackTrace(e));
        
        out.print(buildErrorResponse(e.getMessage()));
        return;
    }
    HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
            logger.warn("Warning: URL Host: " + urlHostName
                    + " vs. " + session.getPeerHost());
            return true;
        }
    };

    HttpsURLConnection.setDefaultHostnameVerifier(hv);

    try {
	    URL url = null;

	    logger.info("url: " + urlStr + (paramStr.length() > 0 ? ("?" + paramStr) : ""));
        if (protocol.endsWith("get") || protocol.endsWith("delete")) {
            url = new URL(urlStr + (paramStr.length() > 0 ? ("?" + paramStr) : ""));
        } else {
            url = new URL(urlStr);
        }
        con = (HttpURLConnection) url.openConnection();
        if (authenAccess) {
            String encodedAuthenDetails = userName + ":" + password;
            byte[] Inputbytes = encodedAuthenDetails.getBytes();
            String encodedString = new String(Base64
                    .encodeBase64(Inputbytes));
            con.setRequestProperty("Authorization", "Basic "
                    + encodedString);
        }
        
        con.setRequestProperty("Content-Type", encType);
        
        /** Set Custom Headers **/
        try {
   			JSONObject customHeaders = requestObj.optJSONObject("CustomHeaders");
   			if (customHeaders != null) {
	    		for(Iterator<String> iter = customHeaders.keys(); iter.hasNext();){
	    			String headerName = iter.next();
	    			String headerValue = customHeaders.getString(headerName);
	    			logger.info("headerName: " + headerName + ", headerValue" + headerValue);
	    			con.setRequestProperty(headerName, headerValue);
	    		}
   			}
    			  
    	} catch (JSONException e) {
    		logger.error("Custom Header exception: " + e.getMessage());
        	logger.error(BackendLogManager.printStackTrace(e));
    	}  
        
        
        con.setRequestMethod(protocol.toUpperCase());
        
        if (protocol.endsWith("put") || protocol.endsWith("post")) {
            con.setDoOutput(true);
            
            PrintWriter write = new PrintWriter(new OutputStreamWriter(con.getOutputStream(),"utf-8"));
            
            if (encType.equals(appJson)) {
            	Object content = requestObj.opt("JsonContent");
            	if (content != null) {
            		if (content instanceof JSONObject) {
            			JSONObject obj = (JSONObject)content;
            			write.print(obj.toString());
            		}
            		else if (content instanceof JSONArray) {
            			JSONArray obj = (JSONArray)content;
            			write.print(obj.toString());
					} 
            		else {
            			JSONObject obj = new JSONObject();
            			obj.put("content", content);
            			write.print(obj.toString());
            		}
            	}
            } else if (encType.equals(appUrlEncoded)) {
	            // Construct data
	            write.print(paramStr);
            }

            write.close();
        }
        con.setConnectTimeout(Integer.valueOf(conTimeout).intValue());
        con.setReadTimeout(Integer.valueOf(readTimeout).intValue());
        
        con.connect();
        int code = con.getResponseCode();
        logger.info("code: " + code);
        if (code >= 200 && code < 300) {

  			// Get an input stream for reading
            
            StringBuffer sb = new StringBuffer();
            String line = null;
            BufferedReader or = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
			while ((line = or.readLine()) != null) 
			{
				sb.append(line+"\n"); //$NON-NLS-1$
			}
          

            or.close();
            String postData = "";
            try {
                
                String responseData = sb.toString();
				responseData = responseData.replaceAll("\\+","%2b");
				postData = URLDecoder.decode(responseData, "UTF-8");
                               
                logger.debug("postData:" + postData);
                
            } catch (Exception e) {
            	logger.error(BackendLogManager.printStackTrace(e));
                postData = sb.toString();

            }
            String contentType = con.getContentType();
			JSONObject result = null;
			
			if(contentType != null){
				logger.info ("Content-Type:" + contentType.toString());
				if( contentType.toLowerCase().startsWith(textXml)  ||
						contentType.toLowerCase().startsWith(appXml) ) {
					try {
		                // handle XML or JSON input	
		                result = org.json.XML.toJSONObject(postData);
		                if (!(result.length() == 0)) {
		                    // XML input
		                    value.append(result.toString());
		
		                } else {
		                	logger.error("ContentTypeXMLFalse: Error in decoding XML: " + postData);
		                    out.print(buildErrorResponse("Error in decoding XML: " + postData));
		                    return;
		                }
		            } catch (Exception e) {
		            	logger.error(BackendLogManager.printStackTrace(e));
		                out.print(buildErrorResponse(e.getMessage()));
		                return;
		            }
				} else if(contentType.toLowerCase().startsWith(appJson)) {
                    logger.info("JSON, appending as-is: " + value);
                    try{
    					result = new JSONObject(postData);
    					logger.info("JSON result: " + result.toString());
    					value.append(result.toString());
    					logger.info ("ContentTypeJSON:" + value.toString());
    				} catch (JSONException e) {
    					logger.error ("JSON parse error:",e);
    					if(verifyJSON){
    						logger.info ("VerifyJSON option set to true, return error response.");    						
    						out.print(buildErrorResponse(e.getMessage()));
    					}
    					else{
    						logger.info ("VerifyJSON option set to false, return response as it is.");
    						value.append(postData.toString());
    					}
    				}
				} else if( contentType.toLowerCase().startsWith(textPlain)) {
					value.append(buildTextResponse(postData));
                    logger.info("TEXT, appending as text: " + value);
				} else {
					logger.warn ("Content-Type unknown, trying to detect the type from the body.");
		            value.append( parseResultData(postData) );
				}
	            
			}
			else {
				// content-type NULL
				logger.info ("Content-Type NULL, trying to detect the type from the body.");
				value.append( parseResultData(postData) );
			}
        } // if HTTP OK 
        else {
            out.print(buildErrorResponse(con.getResponseMessage()));
            return;
        }

    } catch (Exception e) {
    	logger.error(BackendLogManager.printStackTrace(e));

        out.print(buildErrorResponse(e.getMessage()));
        return;
    }

    finally {
        if (con != null) {
            con.disconnect();
        }

    }

    out.print(value.toString());
%>
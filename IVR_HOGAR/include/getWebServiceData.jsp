<%@page language="java"%>
<%@page contentType="application/json;charset=UTF-8"%>
<%@page
	import="javax.naming.InitialContext,java.net.*,java.io.BufferedInputStream,java.io.*,java.util.*"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="org.json.JSONObject"%>
<%@page
	import="com.genesyslab.studio.backendlogic.WebServiceBackendProcessor"%>
<%@page import="com.genesyslab.studio.backendlogic.BackendLogManager"%>
<%@page import="javax.net.ssl.SSLContext"%>
<%@page import="javax.net.ssl.TrustManager"%>
<%@page import="javax.net.ssl.X509TrustManager"%>
<%@page import="javax.net.ssl.HttpsURLConnection"%>
<%@page import="javax.net.ssl.HostnameVerifier"%>
<%@page import="javax.net.ssl.SSLSession"%>
<%@page import="org.apache.log4j.Logger"%>
<%!
Logger logger = BackendLogManager.getLogger("getWebService");

private String buildErrorResponse(String message) {
	logger.info("buildErrorResponse() in");
    StringBuffer sb = new StringBuffer();
    sb.append("'errorMsg'");
    sb.append(":");
    sb.append("'").append(message).append("'");

	logger.error("buildErrorResponse:" + "{" + sb.toString() + "}");
	logger.info("buildErrorResponse() in");
    return "{" + sb.toString() + "}";

};
%>
<%
    // This backend logic will process WebServices requests and responses for URS
    logger.info("getWebServiceData:Start");
    HttpURLConnection con = null;
    String readTimeout = "20000"; // timeout in milliseconds
    String conTimeout = "20000"; // timeout in milliseconds

    WebServiceBackendProcessor processor = new WebServiceBackendProcessor(
            request);
    processor.parseRequest();

    try {
        Properties properties = new Properties();
        FileInputStream ipStr = new FileInputStream(request
                .getRealPath("/WEB-INF/composer.properties"));
        properties.load(ipStr);
        if (properties.getProperty("web.connectionTimeout") != null) {
            conTimeout = properties
                    .getProperty("web.connectionTimeout");
        }
        if (properties.getProperty("web.readTimeout") != null) {
            readTimeout = properties.getProperty("web.readTimeout");
        }
        if (ipStr != null) {
            ipStr.close();
        }
        logger.info("conTimeout: " + conTimeout + ", readTimeout: " + readTimeout);
    } catch (Exception e) {
    	//logger.error(BackendLogManager.printStackTrace(e));
        logger.error("Could not find properties file: "+ e.getMessage());
        logger.info("Hence using default connectionTimeout and readTimeout values");
    }

    String value = null;

    URL url = processor.formURI();
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

    if (url != null) {
        try {
            processor.formHTTPMessage(conTimeout, readTimeout);
            processor.connectToURL();
            value = processor.readWebServiceresponse();
            if (value == null) {
                // URS will generate error.session.fetch when receiving response of 500
                out.print(buildErrorResponse("error.com.genesyslab.composer.webservice.badFetch - Input Stream cannot be read"));
                logger.error("error.com.genesyslab.composer.webservice.badFetch - Input Stream cannot be read");
                return;
            } else {
                value = processor.processSOAPMessage(value);
                String postData = value;
                JSONObject result = org.json.XML.toJSONObject(postData);
                value = result.toString();
                logger.info("result:" + result.toString());
                logger.info("value:" + value.toString());
                if (processor.isSOAPFault()) {
                    out.print(buildErrorResponse(value));
                    return;
                }
            }
        } catch (Exception e) {
        	logger.error(BackendLogManager.printStackTrace(e));
            out.print(buildErrorResponse(e.getMessage()));

            return;
        }

    } else {
        out.print(buildErrorResponse("error.com.genesyslab.composer.webservice.badFetch - Requested URL cannot be fetched"));
        logger.error("error.com.genesyslab.composer.webservice.badFetch - Requested URL cannot be fetched");
        return;

    }

    processor.disconnectConnection();
    if (null != value) {
        // value should not be null here 
        out.print(value);
        logger.info("value:" + value.toString());
    }
    logger.info("getWebServiceData:End");
%>



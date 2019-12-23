<?xml version="1.0" encoding="utf-8"?>
<%@ page language="java" contentType="text/xml;charset=utf-8"%>
<%@page session="false" %>
<%@ page import="org.json.JSONArray" %>
<%@page import="com.genesyslab.studio.backendlogic.BackendLogManager"%>
<%@page import="org.apache.log4j.Logger"%>

<%
Logger logger = BackendLogManager.getLogger("dbInputForm");

String dbValues = request.getParameter("dbValues");
logger.info("dbValues: " + dbValues);
String strTimeout = request.getParameter( "timeout" );
logger.info("strTimeout: " + strTimeout);
String strAppLanguage = request.getParameter( "app_language" );
logger.info("strAppLanguage: " + strAppLanguage);
String strAppASRLanguage = request.getParameter( "app_asr_language" );
logger.info("strAppASRLanguage: " + strAppASRLanguage);
String strSecurity = request.getParameter( "security" );
logger.info("strSecurity: " + strSecurity);

JSONArray rows = new JSONArray(dbValues);
int numRows = rows.length();

%>

<vxml version="2.1" xml:lang="<%=strAppLanguage%>" xmlns="http://www.w3.org/2001/vxml" xmlns:gvp="http://www.genesyslab.com/2006/vxml21-extension" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<form id="DbInputForm">

		<field name="DbInput">
			<% if ( strTimeout.length() > 0 ) { %>
				<property name="timeout" value="<%=strTimeout%>s" />
			<%}%>
			<% if ( strSecurity.length() > 0 && strSecurity.equalsIgnoreCase("true")) { %>
				<property name="com.genesyslab.private" value="true" />
			<%}%>
            <grammar xml:lang="<%=strAppASRLanguage%>" root="TOPLEVEL" version="1.0">
				<rule id="TOPLEVEL" scope="public">
    				<one-of>
    				<% for (int i = 0; i < numRows; i++) { 
                        JSONArray row = rows.getJSONArray(i);
                        String value = row.getString(0);
                        logger.info("row: " + i +  " value: " + value);
                    %>
        				<item><%=value%></item>
    				<% } %>
    				</one-of>
				</rule>
			</grammar>
			
            <filled>
				<return namelist="DbInput DbInput$" />
    		</filled>
			<noinput><return event="noinput"/></noinput>
			<nomatch><return event="nomatch"/></nomatch>
			<catch event="error.noresource"><return event="error.noresource"/></catch>
    		<catch event="error"><return event="error"/></catch>
        </field>
    </form>
</vxml>
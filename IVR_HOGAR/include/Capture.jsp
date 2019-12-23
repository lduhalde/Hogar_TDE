<?xml version="1.0" encoding="utf-8"?>
<%@ page language="java" import="java.util.*, java.io.*" %>
<%@ page session="true" %>
<%@ page contentType="text/xml; charset=utf-8" %>
<%@page import="com.genesyslab.studio.backendlogic.RecordCapturingBackendProcessor"%>
<result>
<%
RecordCapturingBackendProcessor processor = new RecordCapturingBackendProcessor(request);
processor.parseRequest();
%>
<status>
<%
String status = processor.result;
out.print(status);
%>
</status>
<filePath>
<%
if(status.equals(RecordCapturingBackendProcessor.SAVE_FILE_SUCCESS)) {
	out.print(processor.recFileName);
}
else{
	out.print("undefined");
}
%>
</filePath>
</result>
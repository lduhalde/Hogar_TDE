<%@page contentType="application/json;charset=UTF-8"%>
<%@page session="false" %>
<%@page import="com.genesyslab.studio.backendlogic.db.CVDBBackendHandler"%>
<%
CVDBBackendHandler.handleRequest(request, response, getServletContext());
%>
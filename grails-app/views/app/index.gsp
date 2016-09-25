<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.StringUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentType"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<g:set var="topNavClean" value="true" scope="request"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <meta name="layout" content="topNav"/>
    <title>Transition Manager</title>
</head>
<body>
<div ng-app="TDSTM">
    <div ui-view="headerView"></div>
    <div ui-view="bodyView"></div>
</div>
</body>

</html>
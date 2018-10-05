<g:set var="topNavClean" value="true" scope="request"/>
<html>
<head>
    <style>
    div.content-wrapper .container section.content-header {
        margin-bottom: 0px;
    }
    </style>
    <base href="${createLink( uri: '/app/' )}">
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <meta name="layout" content="topNav"/>
    <title>TransitionManager</title>
</head>
<body>
<div ng-app="TDSTM">
    <div ui-view="headerView"></div>
    <div ui-view="bodyView"></div>
</div>
</body>

</html>

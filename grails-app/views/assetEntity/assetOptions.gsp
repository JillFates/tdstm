<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Asset Options</title>
</head>
<body>
<div align="left" style="margin-top: 30px;">
   <h1>Administrative Setting</h1>
   <span>The following options are used for asset fields and are system-wide, not project specific.  Please take care when editing.</span>
</div>
 <div>
	<g:render template="assetEnvironment"/>
 </div>
 <div>
	<g:render template="assetOptions"/>
 </div>
 <div>
	<g:render template="assetPriority"/>
 </div>
 <div>
	<g:render template="dependencyType"/>
 </div>
 <div>
	<g:render template="dependencyStatus"/>
 </div>
</body>
</html>
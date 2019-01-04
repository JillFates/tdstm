<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="topNav" />
<title>Asset Options</title>
</head>
<body>
    <tds:subHeader title="Administrative Setting" crumbs="['Admin','Administrative Setting']"/>
    <div align="left" style="margin-top: 14px;">
       <span>The following options are used for asset fields and are system-wide, not project specific.  Please take care when editing.</span>

        <div class="asset-options" style="margin-top: -54px;">
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
            <div>
                <g:render template="assetType"/>
            </div>
        </div>
    </div>
</body>
</html>

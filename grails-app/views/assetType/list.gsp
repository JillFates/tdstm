

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>AssetType List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New AssetType</g:link></span>
        </div>
        <div class="body">
            <h1>AssetType List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="assetType" title="Asset Type" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${assetTypeInstanceList}" status="i" var="assetTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${assetTypeInstance.id}">${fieldValue(bean:assetTypeInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:assetTypeInstance, field:'assetType')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${AssetType.count()}" />
            </div>
        </div>
    </body>
</html>

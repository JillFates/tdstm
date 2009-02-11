

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Asset List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Asset</g:link></span>
        </div>
        <div class="body">
            <h1>Asset List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <th>Project Name</th>
                   	    
                   	        <th>Asset Type</th>
                   	    
                   	        <g:sortableColumn property="assetName" title="Asset Name" />
                        
                   	        <g:sortableColumn property="assetTag" title="Asset Tag" />
                        
                   	        <g:sortableColumn property="serialNumber" title="Serial Number" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${assetInstanceList}" status="i" var="assetInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${assetInstance.id}">${fieldValue(bean:assetInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:assetInstance, field:'projectName')}</td>
                        
                            <td>${fieldValue(bean:assetInstance, field:'assetType')}</td>
                        
                            <td>${fieldValue(bean:assetInstance, field:'assetName')}</td>
                        
                            <td>${fieldValue(bean:assetInstance, field:'assetTag')}</td>
                        
                            <td>${fieldValue(bean:assetInstance, field:'serialNumber')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Asset.count()}" />
            </div>
        </div>
    </body>
</html>

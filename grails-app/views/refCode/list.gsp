

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>RefCode List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New RefCode</g:link></span>
        </div>
        <div class="body">
            <h1>RefCode List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="domain" title="Domain" />
                        
                   	        <g:sortableColumn property="value" title="Value" />
                        
                   	        <g:sortableColumn property="abbreviation" title="Abbreviation" />
                        
                   	        <g:sortableColumn property="meaning" title="Meaning" />
                        
                   	        <g:sortableColumn property="sortOrder" title="Sort Order" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${refCodeInstanceList}" status="i" var="refCodeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${refCodeInstance.id}">${fieldValue(bean:refCodeInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:refCodeInstance, field:'domain')}</td>
                        
                            <td>${fieldValue(bean:refCodeInstance, field:'value')}</td>
                        
                            <td>${fieldValue(bean:refCodeInstance, field:'abbreviation')}</td>
                        
                            <td>${fieldValue(bean:refCodeInstance, field:'meaning')}</td>
                        
                            <td>${fieldValue(bean:refCodeInstance, field:'sortOrder')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${RefCode.count()}" />
            </div>
        </div>
    </body>
</html>

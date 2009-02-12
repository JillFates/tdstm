

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>RoleType List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New RoleType</g:link></span>
        </div>
        <div class="body">
            <h1>RoleType List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="roleTypeCode" title="Role Type Code" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${roleTypeInstanceList}" status="i" var="roleTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${roleTypeInstance.id}">${fieldValue(bean:roleTypeInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:roleTypeInstance, field:'roleTypeCode')}</td>
                        
                            <td>${fieldValue(bean:roleTypeInstance, field:'description')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${RoleType.count()}" />
            </div>
        </div>
    </body>
</html>

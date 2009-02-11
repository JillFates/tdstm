<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>UserLogin List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New UserLogin</g:link></span>
        </div>
        <div class="body">
            <h1>UserLogin List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                        
                   	        <g:sortableColumn property="username" title="Username" />
                        
                   	        <g:sortableColumn property="createdDate" title="Created Date" />
                        
                   	        <g:sortableColumn property="lastLogin" title="Last Login" />
                        
                   	        <th>Person</th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${userLoginInstanceList}" status="i" var="userLoginInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                        
                            <td><g:link action="show" id="${userLoginInstance.id}">${fieldValue(bean:userLoginInstance, field:'username')}</g:link></td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'createdDate')}</td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'lastLogin')}</td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'person')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${UserLogin.count()}" />
            </div>
        </div>
    </body>
</html>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>UserLogin List</title>
    </head>
    <body>
    
        <div class="body">
            <h1>UserLogin List</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">New UserLogin</g:link></span>
        </div>
        <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                        	<g:sortableColumn property="username" title="Username" />
                        	
                   	        <th>Person</th>
                   	        
                   	        <g:sortableColumn property="lastLogin" title="Last Login" />
                   	    
                   	        <g:sortableColumn property="createdDate" title="Created Date" />
                        
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${userLoginInstanceList}" status="i" var="userLoginInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${userLoginInstance.id}" params="[companyId:companyId]">${fieldValue(bean:userLoginInstance, field:'username')}</g:link></td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'person')}</td>
                        
                            <td><tds:convertDateTime date="${userLoginInstance?.lastLogin}"/></td>
                            
                             <td><tds:convertDateTime date="${userLoginInstance?.createdDate}"/></td>
                        
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

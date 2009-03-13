

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>UserLogin List</title>
    </head>
    <body>
    <div class="menu2">
		<ul>
			<li><g:link class="home" controller="partyGroup" action="show" id="${companyId}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${companyId}">Staff</g:link></li>
			<li><g:link class="home" controller="application" id="${companyId}">Applications </g:link></li>
			<li><a href="#">Locations </a></li>
			<li><a href="#">Rooms </a></li>
		</ul>
	</div>
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
                   	    
                   	        <g:sortableColumn property="createdDate" title="Created Date" />
                        
                   	        <g:sortableColumn property="lastLogin" title="Last Login" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${userLoginInstanceList}" status="i" var="userLoginInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${userLoginInstance.id}" params="[companyId:companyId]">${fieldValue(bean:userLoginInstance, field:'username')}</g:link></td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'person')}</td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'createdDate')}</td>
                        
                            <td>${fieldValue(bean:userLoginInstance, field:'lastLogin')}</td>
                        
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

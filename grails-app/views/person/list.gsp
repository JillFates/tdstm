

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Person List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Person</g:link></span>
        </div>
        <div class="body">
            <h1>Person List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                        
                   	        <g:sortableColumn property="firstName" title="First Name" />
                        
                   	        <g:sortableColumn property="lastName" title="Last Name" />
                        
                   	        <th>Party</th>
                   	    
                   	        <g:sortableColumn property="active" title="Active" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                        
                            <td><g:link action="show" id="${personInstance.id}">${fieldValue(bean:personInstance, field:'firstName')}</g:link></td>
                        
                            <td>${fieldValue(bean:personInstance, field:'lastName')}</td>
                        
                            <td>${fieldValue(bean:personInstance, field:'party')}</td>
                        
                            <td>
                            <g:if test="${personInstance.active =='N'}" >
									No
								</g:if> 
								<g:if test="${personInstance.active =='Y'}" >
									Yes
							</g:if> 
                         </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Person.count()}" />
            </div>
        </div>
    </body>
</html>

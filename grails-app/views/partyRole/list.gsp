
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>PartyRole List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">Create PartyRole</g:link></span>
        </div>
        <div class="body">
            <h1>PartyRole List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                   	        <th>Party</th>
                   	    
                   	        <th>Role Type</th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${partyRoleInstanceList}" status="i" var="partyRoleInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" params="[partyId:partyRoleInstance?.party.id, roleTypeId:partyRoleInstance.roleType.id]"> ${partyRoleInstance?.party}</g:link></td>
                        
                            <td>${fieldValue(bean:partyRoleInstance, field:'roleType')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${PartyRole.count()}" />
            </div>
        </div>
    </body>
</html>

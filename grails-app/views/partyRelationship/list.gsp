

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>PartyRelationship List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PartyRelationship</g:link></span>
        </div>
        <div class="body">
            <h1>PartyRelationship List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <th>Party Relationship Type</th>
                   	    
                   	        <th>Party Id From</th>
                   	    
                   	        <th>Party Id To</th>
                   	    
                   	        <th>Role Type Code From</th>
                   	    
                   	        <th>Role Type Code To</th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${partyRelationshipInstanceList}" status="i" var="partyRelationshipInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${partyRelationshipInstance.id}">${fieldValue(bean:partyRelationshipInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:partyRelationshipInstance, field:'partyRelationshipType')}</td>
                        
                            <td>${fieldValue(bean:partyRelationshipInstance, field:'partyIdFrom')}</td>
                        
                            <td>${fieldValue(bean:partyRelationshipInstance, field:'partyIdTo')}</td>
                        
                            <td>${fieldValue(bean:partyRelationshipInstance, field:'roleTypeCodeFrom')}</td>
                        
                            <td>${fieldValue(bean:partyRelationshipInstance, field:'roleTypeCodeTo')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${PartyRelationship.count()}" />
            </div>
        </div>
    </body>
</html>

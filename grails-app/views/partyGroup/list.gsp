

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>PartyGroup List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PartyGroup</g:link></span>
        </div>
        <div class="body">
            <h1>PartyGroup List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="dateCreated" title="Date Created" />
                        
                   	        <g:sortableColumn property="lastUpdated" title="Last Updated" />
                        
                   	        <th>Party Type</th>
                   	    
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="comment" title="Comment" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${partyGroupInstanceList}" status="i" var="partyGroupInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${partyGroupInstance.id}">${fieldValue(bean:partyGroupInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:partyGroupInstance, field:'dateCreated')}</td>
                        
                            <td>${fieldValue(bean:partyGroupInstance, field:'lastUpdated')}</td>
                        
                            <td>${fieldValue(bean:partyGroupInstance, field:'partyType')}</td>
                        
                            <td>${fieldValue(bean:partyGroupInstance, field:'name')}</td>
                        
                            <td>${fieldValue(bean:partyGroupInstance, field:'comment')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${PartyGroup.count()}" />
            </div>
        </div>
    </body>
</html>

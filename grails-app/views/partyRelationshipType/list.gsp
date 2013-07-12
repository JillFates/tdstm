<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>PartyRelationshipType List</title>
    </head>
    <body>
        <div class="body">
            <h1>PartyRelationshipType List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Code" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${partyRelationshipTypeInstanceList}" status="i" var="partyRelationshipTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${partyRelationshipTypeInstance.id}">${fieldValue(bean:partyRelationshipTypeInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:partyRelationshipTypeInstance, field:'description')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${PartyRelationshipType.count()}" />
            </div>
             <div class="buttons">
                <g:form>
                    <span class="button"><g:actionSubmit class="create" value="Create PartyRelationshipType" action="create" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
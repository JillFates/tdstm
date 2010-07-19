

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Company List</title>
    </head>
    <body>
    	
        <div class="body">
            <h1>Company List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th></th>
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="dateCreated" title="Date Created" />
                        
                   	        <g:sortableColumn property="lastUpdated" title="Last Updated" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${partyGroupInstanceList}" status="i" var="partyGroupInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>
                            <g:form name="selectForm_$i">
                            <input type="hidden" name="id" value="${partyGroupInstance.id}"/>
                            <g:actionSubmit action="show" value="Select"  /></g:form>
                             </td>
                            <td><g:link action="edit" id="${partyGroupInstance.id}" title="Edit  '${fieldValue(bean:partyGroupInstance, field:'name')}'">${fieldValue(bean:partyGroupInstance, field:'name')}</g:link></td>
                        
                            <td><tds:convertDateTime date="${partyGroupInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                        
                            <td><tds:convertDateTime date="${partyGroupInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                        	
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${partyGroupSize}" />
            </div>
            <div class="buttons">
                <g:form>
                    <span class="button"><g:actionSubmit class="create" value="New" action="create" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>

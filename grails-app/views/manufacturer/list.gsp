

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Manufacturer List</title>
    </head>
    <body>
        <div class="body">
            <h1>Manufacturer List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${manufacturerInstanceList}" status="i" var="manufacturerInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${manufacturerInstance.id}">${fieldValue(bean:manufacturerInstance, field:'name')}</g:link></td>
                        
                            <td>${fieldValue(bean:manufacturerInstance, field:'description')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Manufacturer.count()}" />
            </div>
            <div class="buttons">
				<g:form>
			    	<span class="button"><g:actionSubmit class="create" value="New Manufacturer" action="create" /></span>
			    </g:form>
			</div>
        </div>
    </body>
</html>

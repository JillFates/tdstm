

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Model List</title>
    </head>
    <body>
        <div class="body">
            <h1>Model List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                   	        <th>Device Type</th>
                   	    
                   	        <th>Manufacturer</th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${modelInstanceList}" status="i" var="modelInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${modelInstance.id}">${fieldValue(bean:modelInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:modelInstance, field:'name')}</td>
                        
                            <td>${fieldValue(bean:modelInstance, field:'description')}</td>
                        
                            <td>${fieldValue(bean:modelInstance, field:'deviceType')}</td>
                        
                            <td>${fieldValue(bean:modelInstance, field:'manufacturer')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Model.count()}" />
            </div>
            <div class="buttons">
				<g:form>
			    	<span class="button"><g:actionSubmit class="create" value="New Model" action="create" /></span>
			    </g:form>
			</div>
        </div>
    </body>
</html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Move Event List</title>
    </head>
    <body>
        <div class="body">
            <h1>Move Event List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                   	        
                   	        <g:sortableColumn property="inProgress" title="In Progress" />
                   	        
                   	        <g:sortableColumn property="project" title="Project" />
                   	        
                   	        <th>Move Bundles</th>
                   	        
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${moveEventInstanceList}" status="i" var="moveEventInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${moveEventInstance.id}">${fieldValue(bean:moveEventInstance, field:'name')}</g:link> </td>
                        
                            <td>${fieldValue(bean:moveEventInstance, field:'description')}</td>
                            
                             <td>${fieldValue(bean:moveEventInstance, field:'inProgress')}</td>

                             <td>${fieldValue(bean:moveEventInstance, field:'project')}</td>
                             
                             <td>
                             	<g:each in="${moveEventInstance.moveBundles}" status="j" var="moveBundle">${moveBundle} 
                             		<g:if test="${moveEventInstance.moveBundles.size()  > j+1 }">,</g:if>
                             	</g:each>
                             </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div>
		            <div class="paginateButtons">
		            <span class="menuButton"><g:link class="create" action="create">Create New</g:link></span>
		                <g:paginate total="${MoveEvent.findAll('from MoveEvent where project = '+projectId).size()}" />
		            </div>
            </div>
        </div>
    </body>
</html>

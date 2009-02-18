

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Project List</title>
    </head>
    <body>
        <div class="body">
            <h1>Project List</h1>
            <jsec:hasRole name="ADMIN">
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
        	</div>
        	</jsec:hasRole>
        	<br>
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
                        
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="comment" title="Comment" />
                        
                   	        <g:sortableColumn property="projectCode" title="Project Code" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${projectInstanceList}" status="i" var="projectInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${projectInstance.id}">${fieldValue(bean:projectInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:projectInstance, field:'dateCreated')}</td>
                        
                            <td>${fieldValue(bean:projectInstance, field:'lastUpdated')}</td>
                        
                            <td>${fieldValue(bean:projectInstance, field:'name')}</td>
                        
                            <td>${fieldValue(bean:projectInstance, field:'comment')}</td>
                        
                            <td>${fieldValue(bean:projectInstance, field:'projectCode')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Project.count()}" />
            </div>
        </div>
    </body>
</html>

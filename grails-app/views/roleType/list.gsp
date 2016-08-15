<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Team (RoleType) List</title>
    </head>
    <body>
       
        <div class="body">
            <h1>Team (RoleType) List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="type" title="Type" />

                            <g:sortableColumn property="level" title="Level" />

                   	        <g:sortableColumn property="id" title="Code" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                   	        
                   	        <g:sortableColumn property="help" title="Help" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${roleTypeInstanceList}" status="i" var="roleTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean:roleTypeInstance, field:'type')}</td>

                            <td>${fieldValue(bean:roleTypeInstance, field:'level')}</td>

                            <td><g:link action="show" id="${roleTypeInstance.id}">${fieldValue(bean:roleTypeInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:roleTypeInstance, field:'description')}</td>
                            
                            <td>${fieldValue(bean:roleTypeInstance, field:'help')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${RoleType.count()}" />
            </div>
            <div class="buttons">
                <g:form>
                    <span class="button"><g:actionSubmit class="create" action="Create" value="Create Role Type"/></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
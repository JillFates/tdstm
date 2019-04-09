<%@page import="net.transitionmanager.security.RoleType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        
        <title>Team (RoleType) List</title>
    </head>
    <body>
        <tds:subHeader title="Teams" crumbs="['Admin','Portal','Role Type','List']"/><br/>
        <div class="body">
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>


                   	        <g:sortableColumn property="id" title="Code" />

                   	        <g:sortableColumn property="description" title="Description" />

                   	        <g:sortableColumn property="help" title="Help" />

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${roleTypeInstanceList}" status="i" var="roleTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td><g:link action="show" id="${roleTypeInstance.id}">${fieldValue(bean:roleTypeInstance, field:'id')}</g:link></td>

                            <td>${fieldValue(bean:roleTypeInstance, field:'description')}</td>

                            <td>${fieldValue(bean:roleTypeInstance, field:'help')}</td>

                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>

            <div class="buttons">
                <g:form>
                    <span class="button"><g:actionSubmit class="create" action="Create" value="Create Role Type"/></span>
                </g:form>
            </div>
        </div>
    </body>
</html>

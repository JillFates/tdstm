

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="companyHeader" />
        <title>Model List</title>
    </head>
    <body>
        <div class="body"><g:form action="create" method="post">
            <h1>Model List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'model.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="modelName" title="${message(code: 'model.modelName.label', default: 'Model Name')}" />
                        
                            <th><g:message code="model.manufacturer.label" default="Manufacturer" /></th>
                        
                            <g:sortableColumn property="description" title="${message(code: 'model.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="assetType" title="${message(code: 'model.assetType.label', default: 'Asset Type')}" />
                        
                            <g:sortableColumn property="powerUse" title="${message(code: 'model.powerUse.label', default: 'powerUse')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${modelInstanceList}" status="i" var="modelInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${modelInstance.id}">${fieldValue(bean: modelInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: modelInstance, field: "modelName")}</td>
                        
                            <td>${fieldValue(bean: modelInstance, field: "manufacturer")}</td>
                        
                            <td>${fieldValue(bean: modelInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: modelInstance, field: "assetType")}</td>
                        
                            <td>${fieldValue(bean: modelInstance, field: "powerUse")}W</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${modelInstanceTotal}" />
            </div>
            <div class="buttons"> 
				<span class="button"><g:actionSubmit class="save" action="Create" value="Create Project" /></span>
			</div>
			</g:form>
        </div>
    </body>
</html>

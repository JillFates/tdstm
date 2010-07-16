<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Project List</title>
</head>
<body>
<div class="body"><br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="list"><g:form controller="project" action="create" method="post">
	<table>
		<thead>
			<tr>
				<g:sortableColumn property="projectCode" title="Project Code" />

				<g:sortableColumn property="name" title="Name" />

				<g:sortableColumn property="dateCreated" title="Date Created" />

				<g:sortableColumn property="lastUpdated" title="Last Updated" />

				<g:sortableColumn property="comment" title="Comment" />
			</tr>
		</thead>
		<tbody>
			<g:each in="${projectList}" status="i" var="projectInstance">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

					<td style="padding-left: 10px;"><g:link controller="project" action="addUserPreference" params="['selectProject':projectInstance.projectCode]">${fieldValue(bean:projectInstance, field:'projectCode')}</g:link></td>

					<td>${fieldValue(bean:projectInstance, field:'name')}</td>

					<td>${fieldValue(bean:projectInstance, field:'dateCreated')}</td>

					<td>${fieldValue(bean:projectInstance, field:'lastUpdated')}</td>

					<td>${fieldValue(bean:projectInstance, field:'comment')}</td>

				</tr>
			</g:each>
		</tbody>
	</table>
	<div class="buttons"> <span class="button"><g:actionSubmit class="save" action="Create" value="Create" /></span></div>
</g:form></div>
</div>
</body>
</html>

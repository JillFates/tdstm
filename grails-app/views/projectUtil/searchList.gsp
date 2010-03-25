

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
<div class="list"><g:form action="addUserPreference" method="post">
	<table>
		<thead>
			<tr>
				<th></th>

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

					<td><input type="radio" name="selectProject"
						value="${fieldValue(bean:projectInstance, field:'projectCode')}" /></td>

					<td><g:link controller="project" action="show" id="${projectInstance.id}">${fieldValue(bean:projectInstance, field:'projectCode')}</g:link></td>

					<td>${fieldValue(bean:projectInstance, field:'name')}</td>

					<td>${fieldValue(bean:projectInstance, field:'dateCreated')}</td>

					<td>${fieldValue(bean:projectInstance, field:'lastUpdated')}</td>

					<td>${fieldValue(bean:projectInstance, field:'comment')}</td>

				</tr>
			</g:each>
		</tbody>
	</table>
	<div class="buttons">
				<span class="button"><input class="select" type="submit" name="submit" value="Select" /> </span> </div>
</g:form></div>
</div>
</body>
</html>

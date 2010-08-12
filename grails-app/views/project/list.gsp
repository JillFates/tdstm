

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Project List</title>
</head>
<body>

<div class="body"><br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="list"><g:form action="create" method="post">
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
			<g:each in="${projectInstanceList}" status="i" var="projectInstance">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">


					<td style="padding-left: 10px;"><g:link controller="project" action="addUserPreference" params="['selectProject':projectInstance.projectCode]">${fieldValue(bean:projectInstance, field:'projectCode')}</g:link></td>

					<td>${fieldValue(bean:projectInstance, field:'name')}</td>

					<td><tds:convertDateTime date="${projectInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>

					<td><tds:convertDateTime date="${projectInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>

					<td>${fieldValue(bean:projectInstance, field:'comment')}</td>

				</tr>
			</g:each>
		</tbody>
	</table>
	<div class="buttons"> 
		<span class="button"><g:actionSubmit class="save" action="Create" value="Create Project" /></span>
		<span class="button"><input type="button" class="save" onclick="javascript:location.href='../projectUtil/createDemo'" value="Create Demo Project" /></span>
	</div>
</g:form></div>
</div>
</body>
</html>

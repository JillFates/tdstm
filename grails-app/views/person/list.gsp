<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Staff List</title>
<g:javascript library="prototype" />
</head>
<body>
<div class="menu2">
<ul>
	<li><g:link class="home" controller="partyGroup" action="show" id="${companyId}">Company</g:link></li>
	<li><g:link class="home" controller="person" id="${companyId}">Staff</g:link></li>
	<li><a href="#">Applications </a></li>
	<li><a href="#">Locations </a></li>
	<li><a href="#">Rooms </a></li>
</ul>
</div>
<div class="body">
<h1>Staff List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>

<div class="list">
<table>
	<thead>
		<tr>
			<g:sortableColumn property="firstName" title="First Name" />

			<g:sortableColumn property="lastName" title="Last Name" />

			<th>User Login</th>

			<g:sortableColumn property="dateCreated" title="Date Created" />

			<g:sortableColumn property="lastUpdated" title="Last Updated" />

		</tr>
	</thead>
	<tbody>
		<g:each in="${personInstanceList}" status="i" var="personInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">


				<td><g:link action="show" id="${personInstance.id}" params="[companyId:companyId]">${fieldValue(bean:personInstance, field:'firstName')}</g:link></td>

				<td><g:link action="show" id="${personInstance.id}" params="[companyId:companyId]">${fieldValue(bean:personInstance, field:'lastName')}</g:link> </td>

				<td>
				<%
				def userLogin = UserLogin.findByPerson(personInstance);
				%> <g:if test="${userLogin}">
					<g:link controller="userLogin" action="edit" id="${userLogin.id}" params="[companyId:companyId]">${userLogin}</g:link>
				</g:if> <g:else>
					<g:link controller="userLogin" action="create"
						id="${personInstance.id}" params="[companyId:companyId]">CREATE</g:link>
				</g:else></td>

				<td><my:convertDateTime date="${personInstance?.dateCreated}" /></td>

				<td><my:convertDateTime date="${personInstance?.lastUpdated}" /></td>

			</tr>
		</g:each>
	</tbody>
</table>
</div>
<jsec:hasRole name="ADMIN">
	<div class="buttons"><g:form>
	<input type="hidden" value="${companyId}" name="companyId" >
		<span class="button"><g:actionSubmit class="create"
			value="New Staff" action="create" /></span>
	</g:form></div>
</jsec:hasRole></div>
</body>
</html>

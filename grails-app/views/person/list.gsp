

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Staff List</title>
</head>
<body>

<div class="body">
<h1>Staff List</h1>
<br>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="list">
<table>
	<thead>
		<tr>

			<g:sortableColumn property="id" title="Id" />

			<th>Party Type</th>

			<g:sortableColumn property="firstName" title="First Name" />

			<g:sortableColumn property="lastName" title="Last Name" />
			
			<g:sortableColumn property="dateCreated" title="Date Created" />

			<g:sortableColumn property="lastUpdated" title="Last Updated" />
			
			<th>User Login</th>

		</tr>
	</thead>
	<tbody>
		<g:each in="${personInstanceList}" status="i" var="personInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td><g:link action="show" id="${personInstance.id}">${fieldValue(bean:personInstance, field:'id')}</g:link></td>

				<td>${fieldValue(bean:personInstance, field:'partyType')}</td>

				<td>${fieldValue(bean:personInstance, field:'firstName')}</td>

				<td>${fieldValue(bean:personInstance, field:'lastName')}</td>
				
				<td><my:convertDateTime date="${personInstance?.dateCreated}"/></td>
                        
				<td><my:convertDateTime date="${personInstance?.lastUpdated}" /></td>
				
				<td>
				<% def userLogin = UserLogin.findByPerson(personInstance); %>
				<g:if test="${userLogin}">
					<g:link controller="userLogin" action="edit" id="${userLogin.id}">${userLogin}</g:link>
				</g:if>
				<g:else>
					<g:link controller="userLogin" action="create" >New UserLogin</g:link>
				</g:else>
				</td>

			</tr>
		</g:each>
	</tbody>
</table>
</div>
<div class="paginateButtons"><g:paginate total="${Person.count()}" />
</div>
<jsec:hasRole name="ADMIN">
<div class="buttons"><g:form>
	<span class="button"><g:actionSubmit class="create" value="New Staff" action="create" /></span>
</g:form></div>
</jsec:hasRole>
</div>
</body>
</html>

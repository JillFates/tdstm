<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>
</head>
<body>
<div class="body">
<div>
	<h1>Import Accounts</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<g:if test="${step == 'start'}">
		<div>
			<h3>Upload Import File:</h3>


			<g:uploadForm action="importAccounts">
				<input type="file" name="myFile" />
				<input type="hidden" name="step" value="upload" />
				<br />
				<input type="checkbox" name="header" value="Y"> CSV contains a header record
				<br />
				<input type="checkbox" name="verifyProject" value="Y"> YES - I want to import into project ${projectName}
				<br />
				<input type="submit" />
			</g:uploadForm> 
	</g:if>

	<g:if test="${step == 'review'}">
		<div>
			Review Accounts:<br/>
			<table>
				<thead>
					<tr>
						<th>Username</th>
						<th>First Name</th>
						<th>Middle Name</th>
						<th>Last Name</th>
						<th>Phone</th>
						<th>Email</th>
						<th>Match</th>
					</tr>
				</thead>
				<tbody>
					<g:set var="counter" value="${0}" />
					<g:each in="${people}" var="person">
						<tr class="${(counter % 2) == 0 ? 'even' : 'odd'}">
								<td>${person.username}</td>
								<td>${person.firstName}</td>
								<td>${person.middleName}</td>
								<td>${person.lastName}</td>
								<td>${person.phone}</td>
								<td>${person.email}</td>
								<td>${person.match}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
			
			<br />

			<h2>Matched Users</h2>
			<table>
				<thead>
					<tr>
						<th>Username</th>
						<th>First Name</th>
						<th>Middle Name</th>
						<th>Last Name</th>
						<th>Matched On</th>
					</tr>
				</thead>
				<tbody>
					<g:set var="counter" value="${0}" />
					<g:each in="${matches}" var="person">
						<tr class="${(counter % 2) == 0 ? 'even' : 'odd'}">
								<td>${person.username}</td>
								<td>${person.firstName}</td>
								<td>${person.middleName}</td>
								<td>${person.lastName}</td>
								<td>${person.match}</td>
						</tr>
					</g:each>
				</tbody>
			</table>

			<br />

			<g:form action="importAccounts">
			<input type="hidden" name="step" value="post" />
			<input type="hidden" name="header" value="${header}" />
			<input type="checkbox" name="createUserlogin" value="Y"> Create user logins <br />
			<input type="checkbox" name="activateLogin" value="Y"> Activate user logins <br />
			<input type="checkbox" name="forcePasswordChange" value="Y" checked> Force change password at next login<br />
			<input type="checkbox" name="randomPassword" value="Y"> Generate random passwords or  <br />
			<input type="text" name="password" size="10"> Password to use<br />
			<input type="text" name="role" size="10" value="USER"> Security Role [USER,EDITOR,SUPERVISOR]<br />
			<input type="text" name="expireDays" value="90" size="4"> Days before account expires<br />
			<g:submitButton name="submit" value="Create Accounts" />
			</g:form>

		</div>
	</g:if>

	<g:if test="${step == 'results'}">
		<div>
			<h2>Results</h2>

			${created} accounts were created

			<g:if test="${failedPeople.size() > 0}">
				<h3>Accounts that failed:</h3>
				<table>
					<thead>
						<tr>
							<th>Username</th>
							<th>First Name</th>
							<th>Middle Name</th>
							<th>Last Name</th>
							<th>Phone</th>
							<th>Email</th>
							<th>Error</th>
						</tr>
					</thead>
					<tbody>
						<g:set var="counter" value="${0}" />
						<g:each in="${failedPeople}" var="person">
							<tr class="${(counter % 2) == 0 ? 'even' : 'odd'}">
									<td>${person.username}</td>
									<td>${person.firstName}</td>
									<td>${person.middleName}</td>
									<td>${person.lastName}</td>
									<td>${person.phone}</td>
									<td>${person.email}</td>
									<td>${person.error}</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</g:if>
		</div>
	</g:if>

</body>
</html>
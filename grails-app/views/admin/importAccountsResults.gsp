<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>
</head>
<body>
	<div class="body">
		<h1>Import Accounts - Step 3 &gt; Results</h1>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<div>
			${created} accounts were created

			<g:if test="${failedPeople.size() > 0}">
				<h3>Accounts status:</h3>
				<table>
					<thead>
						<tr>
							<th>Username</th>
							<th>First Name</th>
							<th>Middle Name</th>
							<th>Last Name</th>
							<th>Phone</th>
							<th>Email</th>
							<th>Error/Message</th>
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
									<td>									
										<g:each in="${person.errors}" var="error">${error}<br></g:each>
									</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</g:if>
		</div>
	</div>
</body>
</html>
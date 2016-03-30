<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Export Accounts</title>
</head>
<body>
	<div class="body">
		<div>
			<h1>Export Accounts</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:form id="exportAccounts" action="exportAccountsProcess">

			<table>
				<tbody>
					<tr class="prop">
						<td class="name">Client:</td>
						<td class="valueNW">${client}</td>
					</tr>
					<tr class="prop">
						<td class="name">Project:</td>
						<td class="valueNW">${project}</td>
					</tr>

					<tr class="prop">
						<td class="name">Staffing:</td>
						<td class="valueNW">
							<input type="radio" name="partyRelTypeCode" value="STAFF">&nbsp; All available staff
							<br>
							<input type="radio" name="partyRelTypeCode" value="PROJ_STAFF" checked>&nbsp; Only staff assigned to the project
						</td>
					</tr>

					<tr class="prop">
						<td class="name" rowspan="2">User Login:</td>
						<td class="valueNW">
							<input type="checkbox" name="login" value="Y" checked onClick="toggleLoginChoice(this);">&nbsp; Export Login Information
						</td>
					</tr>
					<tr class="prop">
						<td class="valueNW">
							<input type="radio" name="loginChoice" value="A">&nbsp; All Logins 
							<br>  
							<input type="radio" name="loginChoice" value="Y" checked>&nbsp; Active Logins
							<br> 
							<input type="radio" name="loginChoice" value="N">&nbsp; Inactive Logins
							<br><br>
							<span class="footnote">Note that passwords are never exported</span>
						</td>
					</tr>

					<tr>
						<td colspan="2">			
							<input type="submit" value="Export to Excel" class="button">
						</td>
					</tr>

				</tbody>
			</table>
			</g:form> 
		</div>
	</div>

	<script type="text/javascript">
		$(window).load(function() {
			currentMenuId = "#adminMenu";
			$("#projectMenuId a").css('background-color','#003366');
		});

		// Used to enable/disable the LoginChoice radio button
		function toggleLoginChoice(chkbox) {
			$('[name="loginChoice"]').attr('disabled', ! chkbox.checked);
		}

		// Used to disable the button after it is clicked so users don't double-click
		function disableButton(button) {
			button.disabled = true;
			$('#exportAccounts').submit();
		}
	</script>
</body>
</html>
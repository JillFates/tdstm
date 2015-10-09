<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>User Activation Emails</title>
</head>
<body>
<div class="body">
<div>
	<h1>User Activation Emails</h1>
	<p>This form is used to initiate the user account activation process for staff of the project whom have new account properly configured in TransitionManager. By selecting the users and submitting the form, an activation email notification will be sent to each individual so that they can login and set their own passwords.</p><br/>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

		<div>
			<g:form action="sendAccountActivationEmails">

				<span>Email Sent From:</span><br/>
	          	<input type="radio" name="sendFrom" value="DEFAULT" checked>&nbsp;<span>System Email (${defaultEmail})</span><br/>
				<input type="radio" name="sendFrom" value="ADMIN">&nbsp;<span>Your Email (${adminEmail})</span><br/><br/>
				<span>Custom Message:</span><br>
				<textarea name="customMessage" rows="5" cols="80">Welcome to TransitionManager.</textarea><br/><br/>
				<table>
					<thead>
						<tr>
							<th style="text-align: center;"><input type="checkbox" id="checkAllAccountActivation" /></th>
							<th>First Name</th>
							<th>Last Name</th>
							<th>Email Address</th>
							<th>Company</th>
							<th>Default Project</th>
							<th>Roles</th>
							<th>Expiry</th>
							<th>Last Act Notice</th>
							<th>Created</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${accounts}" var='account'>
							
							<td style="text-align: center;"> 
							  <input type="checkbox" name="person" value="${account.personId}"> 
							</td>

							<td style="text-align: left;">
								${account.firstName}
							</td>

							<td style="text-align: left;">
								${account.lastName}
							</td>
							
							<td style="text-align: left;">
								${account.email}
							</td>
							
							<td style="text-align: left;">
								${account.company}
							</td>
							
							<td style="text-align: left;">
								${account.currentProject ? account.currentProject.projectCode : '' }
							</td>
							
							<td style="text-align: left;">
								${account.roles ? account.roles.join(', ') : ''}
							</td>

							<td style="text-align: left;">
								${account.expiry}
							</td>
							
							<td style="text-align: left;">
								<tds:convertDateTime date="${account?.lastActivationNotice}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>

							<td style="text-align: left;">
								<tds:convertDateTime date="${account?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>

						</g:each>

					</tbody>
				</table>

				<input type="submit" value="Send Activation Emails" />
			</g:form>
			<script type="text/javascript">
				$(document).ready(function(){
					$("#checkAllAccountActivation").click(function(){
   						$('input:checkbox').not(this).prop('checked', this.checked);
					});

					$("form").submit(function(e){
						var preventDefault = true
						var selectedAccounts = $("input:checkbox[name^=person]:checked").length
						if(selectedAccounts > 0){
							preventDefault = !(confirm("You are about to send an email notification to " + selectedAccounts + " accounts. Do you want to proceed?"))
						}else{
							alert("You must select at least one account.")
						}

						if(preventDefault){
							e.preventDefault()
						}
					});
				});


			</script>
</body>
</html>
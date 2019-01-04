<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Export Accounts</title>

</head>
<body>
	<tds:subHeader title="Export Accounts" crumbs="['Admin','Client', 'Export']"/>
	<div class="body body-disabled-float">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div style="margin-top: 7px;">
				<div class="panel panel-default account-export">
					<div class="panel-heading">This form is used to export staff and user login accounts for the current project.</div>
					<div class="panel-body">
						<g:form id="exportAccounts" action="exportAccountsProcess" class="form-horizontal">
							<div class="form-group">
								<label for="clientName" class="col-sm-2 control-label">Client:</label>
								<div class="col-sm-10">
									<label class="form-control input-max-size" id="clientName" >${client}</label>
								</div>
							</div>

							<div class="form-group">
								<label for="projectName" class="col-sm-2 control-label">Project:</label>
								<div class="col-sm-10">
									<label class="form-control input-max-size" id="projectName" >${project}</label>
								</div>
							</div>


							<div class="form-group">
								<label class="col-sm-2 control-label radio-staff-group">Staffing:</label>
								<div class="col-sm-10">
									<label><input type="radio" name="staffType" value="CLIENT_STAFF" ${staffType=='CLIENT_STAFF'?'checked':''}>&nbsp; Client staff</label>
									<br>
									<label><input type="radio" name="staffType" value="AVAIL_STAFF" ${staffType=='AVAIL_STAFF'?'checked':''}>&nbsp; All available staff from client, partners and ${company}</label>
									<br>
									<label><input type="radio" name="staffType" value="PROJ_STAFF" ${staffType=='PROJ_STAFF'?'checked':''}>&nbsp; Only staff assigned to the project</label>
								</div>
							</div>


							<div class="form-group">
								<label class="col-sm-2 control-label select-login-choice">User Logins:</label>
								<div class="col-sm-10">
									<div>
										<select id="loginChoice" name="loginChoice">
											<option value="0" ${!loginChoice ? 'checked':''}>Select Optional Filter</option>
											<option value="1" ${loginChoice=='1'?'checked':''}>With Login Account</option>
											<option value="2" ${loginChoice=='2'?'checked':''}>Without Login Account</option>
											<option value="3" ${loginChoice=='3'?'checked':''}>Active Accounts</option>
											<option value="4" ${loginChoice=='4'?'checked':''}>Inactive Accounts</option>
										</select>
									</div>
									<br>
									
									<input type="hidden" name="includeLogin" value="Y"/>
									<span class="footnote">Note that passwords are never exported</span>
								</div>
							</div>

							<br>
							<div class="form-inline">
								<%--
								<g:link controller="assetEntity" action="exportSpecialReport">
									<input class="button" type="button" value="Generate Special" onclick="window.location=this.parentNode.href;"/>
								</g:link>
								--%>
								<%-- div class="form-group col-sm-6">
									<label>Export Format:</label>
									<select name="exportFormat" class="form-control selectpicker show-tick">
										<option value="xlsx" selected="selected">Excel Workbook (.xlsx)</option>
										<option value="xls">Excel 97-2004 (.xls)</option>
									</select>
								</div --%>
								<button type="submit" class="btn btn-default">
									Export Excel (.xlsx)
									<span class="exportIcon glyphicon glyphicon-download" aria-hidden="true"></span>
								</button>
							</div>
						</g:form>
					</div>
				</div>
			</div>
	</div>

	<script type="text/javascript">
		$(window).load(function() {
			currentMenuId = "#adminMenu";
			$('.menu-client-export-accounts').addClass('active');
			$('.menu-parent-admin').addClass('active');
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

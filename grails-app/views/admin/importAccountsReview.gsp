<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>

	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.common.min.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.default.min.css')}" />
	<script src="${resource(dir:'/dist/js/vendors/kendo',file:'kendo.all.min.js')}"></script>
</head>
<body>
<div class="body">
	<h1>Import Accounts - Review Accounts</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<div id="grid" style="margin:1em; width=1000px;"></div>

	<script>
		$(document).ready(function() {
			$("#grid").kendoGrid({
				dataSource: {
					type: "json",
					transport: {
						read: "${createLink(action:'importAccountsReviewData', params:[filename:filename])}"
					},
					schema: {
						model: {
							fields: {
								<g:each var="propName" in="${properties}">${propName}: { type: "string" },</g:each>
								errors: { type: "string" },
								match: { type: "string" }
							}
						}
					},
					pageSize: 30
				},
				columns: [
					<g:each var="propName" in="${properties}"><g:set var="gridOpt" value="${gridMap[propName]}" />{
						field: "${propName}",
						title: "${gridOpt.label}",
						locked: ${gridOpt.locked},
						lockable: false,
						width: ${gridOpt.width}
					},</g:each>
					{
						field: "errors",
						title: "Errors",
						locked: false,
						width: 100
					},  
					{
						field: "match",
						title: "Matched on",
						lockable: false,
						width: 50
					}
				],
				height: 540,
				sortable: true,
				reorderable: false,
				groupable: false,
				resizable: true,
				filterable: true,
				columnMenu: true,
				pageable: false,

			});
		});
		</script>


		<div>
			<br />

			<g:form action="importAccounts">
			<input type="hidden" name="step" value="post" />
			<input type="hidden" name="header" value="${header}" />
			<input type="hidden" name="timezone" value="${timezone}" />
			<input type="hidden" name="filename" value="${filename}" />
			<input type="checkbox" name="createUserlogin" value="Y"> Create user logins <br />
			<input type="checkbox" name="activateLogin" value="Y"> Activate user logins <br />
			<input type="checkbox" name="forcePasswordChange" value="Y" checked> Force change password at next login<br />
			<input type="checkbox" name="randomPassword" value="Y"> Generate random passwords or  <br /-->
			<input type="text" name="password" size="10"> Default password to use (if blank in import)<br />
			<input type="text" name="role" size="10" value="USER"> Default Security Role [USER,EDITOR,SUPERVISOR] (if not in import)<br />
			<input type="text" name="expireDays" value="90" size="4"> Days before account expires<br />
			<br>
			<g:submitButton name="submit" value="Create/Update Accounts" />
			</g:form>

		</div>
	</div>
</div>
</body>
</html>
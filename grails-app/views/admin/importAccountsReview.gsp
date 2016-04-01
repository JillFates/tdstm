<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>

	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.common.min.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.default.min.css')}" />
	<script src="${resource(dir:'/dist/js/vendors/kendo',file:'kendo.all.min.js')}"></script>

	<style type="text/css">
.wrapper .post {
-moz-border-radius:7px 7px 7px 7px;
border:1px solid silver;
float:left;
margin:10px;
min-height:100px;
padding:5px;
width:200px;
}

</style>
</head>
<body>
<div class="body import-review">
	<h1>Import Accounts - Step 2 &gt; Review Accounts</h1>
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
					},
					</g:each>
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
			<p>
				Please review the above information for accuracy before submitting the form.
			</p>
			<br>
			<g:form action="importAccounts">
				<input type="hidden" name="step" value="post" />
				<input type="hidden" name="header" value="${header}" />
				<input type="hidden" name="timezone" value="${timezone}" />
				<input type="hidden" name="filename" value="${filename}" />
			<table>
				<tr>
					<td>
						<input type="checkbox" name="createUserlogin" value="Y"> Create user logins <br />
						<input type="checkbox" name="activateLogin" value="Y"> Activate user logins <br />
						<input type="checkbox" name="forcePasswordChange" value="Y" checked> Force change password on next login<br />
					</td>
					<td>
						<input type="checkbox" name="randomPassword" value="Y"> Generate random passwords or <br/>
						<input type="text" name="password" size="10"> Default password to use <i>(if blank in import)</i>
					</td>
					<td>
						<select name="role"> 
							<option value="USER">USER</option>
							<option value="EDITOR">EDITOR</option>
							<option value="SUPERVISOR">SUPERVISOR</option>
						</select>
						Default Security Role <i>(if blank in import)</i><br/>
						<input type="text" name="expireDays" value="90" size="4"> Days before account(s) expires<br />
					</td>
					<td>
						<g:submitButton name="submit" value="Create/Update Accounts" />
					</td>
				</tr>
			</table>
			</g:form>
		</div>
	</div>
</div>
</body>
</html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>

	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.common.min.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.default.min.css')}" />

	<script src="${resource(dir:'/dist/js/vendors/kendo',file:'kendo.all.min.js')}"></script>

	<g:javascript src="bootstrap.js" />

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

			/**
			 * Handle errors when the import process fails
			 * It follows the format status: "customerror", errorThrown: "custom error", errors: Array[2],
			 * @param data
             */
			function processErrors(data) {
				var errorMsg = "";

				if(data.status) {
					errorMsg += '<strong> ' + data.status + ' </strong><br />';
				}

				if(data.errors && data.errors.length > 0) {
					errorMsg += '<ul>';
					for(var i = 0; i < data.errors.length; i++){
						errorMsg +=	'<li> ' + data.errors[i] + ' </li>'
					}
					errorMsg += '</ul>';
				}

				$('#errorModalText').html(errorMsg);
				$('#errorModal').modal('show');

				var gridElement = $("#grid");
				gridElement.find('.k-grid-content').remove();
				gridElement.find('.k-grid-content-locked').remove();
				gridElement.height(50);

				$("#createSubmit").hide();
			}

			$("#grid").kendoGrid({
				dataSource: {
					type: "json",
					transport: {
						read: "${createLink(action:'importAccountsReviewData', params:[filename:filename])}"
					},
					error: processErrors,
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
				<g:set var="isFirstElement" value="${true}"/>
				<g:each var="propName" in="${properties}"><g:set var="gridOpt" value="${gridMap[propName]}" />
					<g:if test="${isFirstElement}"><g:set var="isFirstElement" value="${false}"/></g:if>
					<g:else>,</g:else>
					{
						field: "${propName}",
						title: "${gridOpt.label}",
						locked: ${gridOpt.locked},
						lockable: false,
						width: ${gridOpt.width}
					}
				</g:each>
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
						<g:submitButton id="createSubmit" name="submit" value="Create/Update Accounts" />
					</td>
				</tr>
			</table>
			</g:form>
		</div>
	</div>
</div>
<g:include view="/layouts/_error.gsp" />
</body>
</html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>

	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.common.min.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'/dist/css/kendo',file:'kendo.default.min.css')}" />

	<script src="${resource(dir:'/dist/js/vendors/kendo',file:'kendo.all.min.js')}"></script>

	<g:javascript src="bootstrap.js" />

	<%-- 
		TODO : JPM 4/2016 : Add logic to resize the table to adjust for the size of the browser
		See http://jsfiddle.net/dimodi/4eNu4/2/ as an example

		TODO : JPM 4/2016 : Apply row level template to grey out the rows that are not going to be changing
		See http://jsfiddle.net/FcWBQ/
	--%>

	<style type="text/css">
		.k-grid  .k-grid-header  .k-header  .k-link {
		    height: auto;
		}
		  
		.k-grid  .k-grid-header  .k-header {
		    white-space: normal;
		}
	</style>

</head>
<body>
<div class="body account-import-review">
	<h1>Import Accounts - Step 2 &gt; Review Accounts</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<div id="grid" style="margin:1em; width=1000px;"></div>

	<script>
		// Used to render the changes made in a view
		function showChanges(model, propertyName) {
			var origPropName = propertyName + '${originalSuffix}';

			// if (model[origPropName]) {
			if (model.hasOwnProperty(origPropName)) {
				return '<span class="change">' + model[propertyName] + 
					'</span>' + 
					(model[origPropName] ? '<br><span class="current">' + model[origPropName] + '</span>' : '');
			} else {
				return model[propertyName];
			}
		}

		// alert(errorsTemplate({personId: 123, age:50}));
	</script>

	<script type="text/x-kendo-tmpl" id="error-template">
		#if (errors) {#
			<div class="">
				<ul>
					#var errorList = errors.split('|');#
					#for (var i=0,len=errorList.length; i<len; i++) {#
						<li>${'$'}{ errorList[i] }</li>
					#}#
				</ul>
			</div>
		#}#


	</script>

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

				if (data.errors && data.errors.length > 0) {
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
						read: "${createLink(action:'importAccountsReviewData', params:optionsAsParams)}"
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
					{ template: '<img src="#= icon #" />', width:30, locked: true },
		
				<g:set var="isFirstElement" value="${true}"/>
				<g:each var="propName" in="${properties}">
					<g:set var="gridOpt" value="${gridMap[propName]}" />
					<g:if test="${isFirstElement}"><g:set var="isFirstElement" value="${false}"/></g:if>
					<g:else>,</g:else>
					{
						field: "${propName}",
						title: "${gridOpt.label}",
						locked: ${gridOpt.locked},
						<g:if test="${gridOpt.template}">template: ${gridOpt.template},</g:if>
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
						<label><input type="checkbox" name="createUserlogin" value="Y"> Create/Update user logins</label>
						<br />
						<label><input type="checkbox" name="activateLogin" value="Y"> Activate user logins</label>
						<br />
						<label><input type="checkbox" name="forcePasswordChange" value="Y" checked> Force change password on next login</label>
					</td>
					<%-- 
					<td>
						For new User Logins only:<ul>
							<li><input type="checkbox" name="randomPassword" value="Y"> Generate random passwords or </li>
							<li><input type="text" name="password" size="10"> Default password to use <i>(if blank in import)</i></li>
						</ul>
					</td>
					--%>
					<td>
						<select name="role"> 
							<label><option value="USER">USER</option></label>
							<label><option value="EDITOR">EDITOR</option></label>
							<label><option value="SUPERVISOR">SUPERVISOR</option></label>
						</select>
						Default Security Role <i>(if blank in import)</i>
						<br/>
						<label><input type="text" name="expireDays" value="90" size="4"> Days before account(s) expires</label>
						<br />
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
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Import Accounts</title>

	<style type="text/css">
		.k-grid-toolbar {
			border-color: #5f9fcf;
			background-color: #5f9fcf;
			padding: 7px;
			font-size: 15px;
		}

		th a:link {
			width: inherit !important;
		}

		.k-grid .k-alt {
			background-color: #f1f1f1;
		}

		.btn-post {
			margin-right: 10px;
		}

		.panel-default {
			margin-left: 13px;
			margin-right: 13px;
			margin-top: 10px;
		}
		.panel-body {
			padding-top: 0px;
			padding-bottom: 0px
		}
		.list-group {
			width: 300px;
		}

		.k-loading-image {
			background-image: url('${resource(dir:'dist/css/kendo/Default',file:'loading-image.gif')}');
		}

		/* we refactored the ui to auto size, the bottom border*/
		.k-grid-content tr:last-child>td {
			border-bottom-width: 1px;
		}
	</style>

</head>
<body>
<div class="body account-import-review">
	<h1>Import Accounts - Posting Results</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>



	<div class="panel panel-default">
		<div class="panel-body">
			<h3>Posting Summary</h3>
			<div class="container-fluid">
				<div class="row">
					<div class="col-md-3">
						<ul class="list-group">
							<li class="list-group-item">
								<span class="badge">${results.personCreated}</span>
								People Created:
							</li>
							<li class="list-group-item">
								<span class="badge">${results.personUpdated}</span>
								People Updated:
							</li>
							<li class="list-group-item">
								<span class="badge">${results.personError}</span>
								People With Errors:
							</li>
						</ul>

					</div>
					<div class="col-md-9">
						<ul class="list-group">
							<li class="list-group-item">
								<span class="badge">${results.userLoginCreated}</span>
								User Logins  Created:
							</li>
							<li class="list-group-item">
								<span class="badge">${results.userLoginUpdated}</span>
								User Logins  Updated:
							</li>
							<li class="list-group-item">
								<span class="badge">${results.userLoginError}</span>
								User Logins  With Errors:
							</li>
						</ul>
					</div>
				</div>
			</div>
			<%--
			personSkipped: 0,
			personUnchanged: 0,
			teamsUpdated: 0,
			teamsError: 0,
			userLoginError:0
			--%>
		</div>
	</div>

	<div id="grid" style="margin:1em; width=1000px;"></div>

	<script>
		// Used to render the changes made in a view
		function showChanges(model, propertyName) {
			var originalPropName = propertyName + '${originalSuffix}';
			var defaultPropName = propertyName + '${defaultSuffix}';
			var errorPropName = propertyName + '${errorSuffix}';
			var hasCurrVal = model.hasOwnProperty(propertyName);
			var hasOrigVal = model.hasOwnProperty(originalPropName);
			var hasDefVal  = model.hasOwnProperty(defaultPropName);
			var hasErrVal  = model.hasOwnProperty(errorPropName);
			
			var str = '';
			
			if (propertyName == '')
				console.log('hasCurrVal='+hasCurrVal + ', hasOrigVal=' + hasOrigVal + ', hasDefVal=' + hasDefVal);

			if (hasErrVal) {
				// Display an error message
				str = (hasCurrVal ? '<span class="change">' + model[propertyName] + '</span><br>' : '') +
					'<span class="error">' + model[errorPropName] + '</span>';

			} else if (hasCurrVal && hasOrigVal && hasDefVal) {
				// A unique case with Security Roles and Teams where we need to show original, the changes and the results
				str = '<br><span class="change">' + model[defaultPropName] + '</span>';
				if (model[originalPropName]) {
					str = str + '<br><span class="original">' + model[originalPropName] + '</span>';
				}
				if (model[propertyName]) {
					str = str + '<br><span class="userData">' + model[propertyName] + '</span>';
				}
			} else if(hasDefVal) {
				// A value was defaulted in from pre-existing user or application defaults
				str = '<span class="default">' + model[defaultPropName] + '</span>';
				if (hasCurrVal && ! (model[defaultPropName] == model[propertyName]) ) {
					str += '<br><span class="change">' + model[propertyName] + '</span>';
				}
			} else if(hasOrigVal) {
				str = '<span class="change">' + model[propertyName] + '</span>' + 
					(model[originalPropName] ? '<br><span class="original">' + model[originalPropName] + '</span>' : '');
			} else {
				str = '<span class="unchanged">' + model[propertyName] + '</span>';
			}

			return str;


		}

		// Used by the cancel button to call the cancel action
		function callCancelImport(fn) {
			var url = '${createLink(action: 'cancelImport')}'
			window.location = url + '/' + fn;
		}

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
			// Track load of the Grid has been processed correctly
			var loadValidation = true;

			/**
			 * Handle errors when the import process fails
			 * It follows the format status: "customerror", errorThrown: "custom error", errors: Array[2],
			 * @param data
			 */
			function processErrors(data) {
				loadValidation = false;
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
				gridElement.height(80);

				$("#createSubmit").hide();
			}

			$("#grid").kendoGrid({
				dataSource: {
					type: "json",
					transport: {
						read: "${raw(createLink(action:'importAccountsPostResultsData', params:paramsForReviewDataRequest))}"
					},
					error: processErrors,
					schema: {
						model: {
							fields: {
								<g:each var="propName" in="${properties}">${propName}: { type: "string" },</g:each>
								errors: { type: "string" },
								matches: { type: "string" }
							}
						}
					},
					pageSize: 0
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
						<g:if test="${gridOpt.template}">template: ${raw(gridOpt.template)},</g:if>
						<g:if test="${gridOpt.templateClass}">attributes: { "class": "${gridOpt.templateClass}" },</g:if>
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
				dataBound: function() {
					resizeGrid();
				}
			});

			function resizeGrid() {
				var gridElement = $("#grid");
				var dataArea = gridElement.find(".k-grid-content");
				// Grid with locked columns has two  containers
				var dataAreaLocked = gridElement.find(".k-grid-content-locked");
				var newHeight = $(window).innerHeight() - $('.panel-body').height() - 140;
				var diff = gridElement.innerHeight() - dataArea.innerHeight();
				gridElement.height(newHeight);
				dataArea.height(newHeight - diff);
				dataAreaLocked.height(newHeight - diff);
			}

			$(window).resize(function(){
				if(loadValidation) {
					resizeGrid();
				}
			});
		});

		</script>

	</div>
</div>
<g:include view="/layouts/_error.gsp" />
<script>
	$('.menu-client-import-accounts').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>

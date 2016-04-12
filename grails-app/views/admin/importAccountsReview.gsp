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
			var hasCurrVal = model.hasOwnProperty(propertyName);
			var hasOrigVal = model.hasOwnProperty(propertyName + '${originalSuffix}');
			var hasDefVal  = model.hasOwnProperty(propertyName + '${defaultSuffix}');
			var originalPropName = propertyName + '${originalSuffix}';
			var defaultPropName = propertyName + '${defaultSuffix}';
			var str = '';
			
			if (propertyName == '')
			console.log('hasCurrVal='+hasCurrVal + ', hasOrigVal=' + hasOrigVal + ', hasDefVal=' + hasDefVal);

			if (hasCurrVal && hasOrigVal && hasDefVal) {
				// A unique case with Security Roles and Teams where we need to show original, the changes and the results
				str = '<span class="default">' + model[defaultPropName] + '</span>';
				if (model[originalPropName]) {
					str = str + '<br><span class="original">' + model[originalPropName] + '</span>';
				}
				if (model[propertyName]) {
					str = str + '<br><span class="change">' + model[propertyName] + '</span>';
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

/*
			var originalPropName = propertyName + '${originalSuffix}';
			var defaultPropName = propertyName + '${defaultSuffix}';
			if (model.hasOwnProperty(defaultPropName)) {
				return '<span class="default">' + model[defaultPropName] + '</span>';
			} else if (model.hasOwnProperty(originalPropName)) {
				return '<span class="change">' + model[propertyName] + 
					'</span>' + 
					(model[originalPropName] ? '<br><span class="original">' + model[originalPropName] + '</span>' : '');
			} else {
				return model[propertyName];
			}
*/
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
				dataBound: function() {
					resizeGrid();
				}
			});

			function resizeGrid() {
				var gridElement = $("#grid");
				var dataArea = gridElement.find(".k-grid-content");
				// Grid with locked columns has two  containers
				var dataAreaLocked = gridElement.find(".k-grid-content-locked");
				var newHeight = $(window).innerHeight() - 200;
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

				<g:submitButton id="createSubmit" name="submit" value="Post changes to ${processOptionDesc}" />
			</g:form>
		</div>
	</div>
</div>
<g:include view="/layouts/_error.gsp" />
</body>
</html>
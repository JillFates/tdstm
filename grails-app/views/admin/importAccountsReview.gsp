<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Import Accounts</title>

	<asset:stylesheet href="css/spinner.css" />

	<%--

		TODO : JPM 4/2016 : Apply row level template to grey out the rows that are not going to be changing
		See http://jsfiddle.net/FcWBQ/
	--%>

	<script>
		// watch out, this code detect when the screen has just finished his resize event.
		var toResize;
		var heightToTop = 0;

		function applySuperHeader(){
			var sc = $(window).scrollTop();
			if(sc > heightToTop) {
				$(".k-grid-header").addClass("modify-header");
			} else if(sc < heightToTop) {
				$(".k-grid-header").removeClass("modify-header");
			}
		}

		$(window).resize(function() {
			$(".k-grid-header").removeClass("modify-header");
			clearTimeout(toResize);
			toResize = setTimeout(function() {
				applySuperHeader();
			}, 100);
		});

		function onDataBound() {
			var contentHeight = $('.k-grid-content-locked').height();
			if(contentHeight && contentHeight > 81) {
				$('.k-grid-content-locked').height($('.k-grid-content').height());
			}
			heightToTop = $(".k-grid-header").offset().top;
		}

		// Apply the fix only after the scroll has reached
		$(window).scroll(function (event) {
			applySuperHeader();
		});

	</script>
	<style>
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

		#grid {
			min-height: 200px;
		}

		.k-grid-content-locked {
			min-height: 81px;
		}

		.k-loading-image {
			background-image: url('${resource(dir:'images',file:'loading-image.gif')}');
		}

		.modify-header {
			position: fixed;
			z-index: 9999;
			top: 0px;
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

		// Used to popup a confirmation message if the user is removing STAFF from the project
		function validateBeforePost() {
			var atrfp=${accountsToRemoveFromProject};
			if (atrfp > 0) {
				var areIs = (atrfp == 1 ? ' is ' : ' are ');
				var msg = 'There' + areIs + atrfp + ' account' + (atrfp == 1 ? '' : 's') +
					' that' +  areIs + 'identified with "-STAFF" to be removed from the project. This action will remove the person from all ' +
					'assigned tasks; event teams; association to the project and for non-client staff unassignment of Application Owner ' +
					'and/or SME references. These changes can not be undone. Please click OK to proceed otherwise press Cancel.';

					$("#confirmDialog").html(msg)
					$("#overlay").css('display', 'inline')
					$("#confirmDialog").dialog({
	      				buttons : {
	        				"Confirm" : function() {
								$(this).dialog("close");
								$("#overlay").css('display', 'none')
								$("#postForm").submit();
	        				},
	        				"Cancel" : function() {
								$(this).dialog("close");
	          					$("#overlay").css('display', 'none')
	        				}
	      				}
	    			});
	    			$("#confirmDialog").dialog("open");
	    			$("#confirmDialog").parent().find(".ui-dialog-buttonpane").css('width', 'auto')

			} else {
				$("#postForm").submit();
			}
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
				toolbar: kendo.template('Please review the following information for accuracy before submitting the form.'),
				dataSource: {
					type: "json",
					transport: {
						read: "${raw(createLink(action:'importAccountsReviewData', params:paramsForReviewDataRequest))}"
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
					{ template: '<img src="#= icon #" />', width:50, locked: true },

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
				sortable: true,
				reorderable: false,
				groupable: false,
				resizable: true,
				filterable: true,
				columnMenu: true,
				pageable: false,
				dataBound: onDataBound
			});

		});

		</script>

		<div>
			<g:form action="importAccounts" class="form-inline" name="postForm">
				<input type="hidden" name="step" value="post" />
				<input type="hidden" name="header" value="${header}" />
				<input type="hidden" name="timezone" value="${timezone}" />
				<input type="hidden" name="filename" value="${filename}" />
				<input type="hidden" name="importOption" value="${importOption}" />
				<div>
					<g:if env="development">
						<div style="float: left">
							<label><input type="checkbox" name="testMode" value="Y" checked /> Test Mode (disable committing changes)</label>
						</div>
					</g:if>
					<div style="float: right; margin-right: 14px;">
						<button type="button" class="btn btn-default btn-post" onclick="validateBeforePost();">
							POST changes to ${importOptionDesc} <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
						</button>
						<button type="button" id="cancelImport" class="btn btn-default" onclick="callCancelImport('${filename}');">Cancel <span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>
					</div>
				</div>
				<br />
				<br />
				<br />
			</g:form>

		</div>
	</div>
</div>
<div id="confirmDialog" title="Confirm before proceeding">
</div>

<div id="overlay">
	<div id="overlay-wrapper">
	</div>
</div>

<g:include view="/layouts/_error.gsp" />
<script>
	$('.menu-client-import-accounts').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>

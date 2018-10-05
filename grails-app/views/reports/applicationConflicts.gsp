<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<asset:stylesheet href="css/rackLayout.css" />
<asset:stylesheet href="css/spinner.css" />
<title>Application Conflicts</title>
<g:javascript src="report.js"/>
</head>
<body>
	<tds:subHeader title="Application Conflicts" crumbs="['Reports', 'Application']"/>
	<div class="body">
		<div class="message" id="preMoveErrorId" style="display: none">Please select the bundle to start the report.</div>

		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<g:form action="generateApplicationConflicts" name="applicationConflicts" method="post" onsubmit="return disableGenerateButton(this.name)">
			<table>
				<tbody>
					<tr>
						<td>Bundle: <g:select from="${moveBundles}" id="moveBundleId" name="moveBundle"
								optionKey="id" optionValue="name" value="${moveBundleId}" onChange="changeSmeSelect(this.value,'conflict')"/></td>
					</tr>
					<tbody id="smeAndAppOwnerTbody">
						<g:render template="smeSelectByBundle"  model="[smeList:'', appOwnerList:appOwnerList, forWhom:'conflict']" />
					</tbody>
					<tr>
						<td>
							<input type="checkbox" name="conflicts" id="conflicts" checked="checked" />&nbsp;<b>Bundle Conflict</b> - Having dependency references to assets assigned to unrelated bundles
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="unresolved" id="unresolved" checked="checked" />&nbsp;<b>Unresolved Dependencies</b> - Having dependencies with status <em>Unknown</em> or <em>Questioned</em>
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="missing" id="missing" checked="checked" />&nbsp;<b>Missing Dependencies</b> - Having no defined <em>Supports</em> or <em>Requires</em> dependencies
						</td>
					</tr>

					<tr>
							<td>Maximum Applications to report:
								<select id="assetCap" name="report_max_assets">
									<option value="100">100</option>
									<option value="250">250</option>
									<option value="500">500</option>
								</select>
							</td>
						</tr>

					<tr class="buttonR">
					<tds:hasPermission permission="${Permission.ReportViewEventPrep}">
						<td>
							<button type="button" class="btn btn-default" id="applicationConflictsButton" onclick="return verifyBundle();"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Generate</button>
						</td>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>

		<div id="overlay">
		    <div id="overlay-wrapper">
		        <div id="floatingBarsG">
		            <div class="blockG" id="rotateG_01"></div>
		            <div class="blockG" id="rotateG_02"></div>
		            <div class="blockG" id="rotateG_03"></div>
		            <div class="blockG" id="rotateG_04"></div>
		            <div class="blockG" id="rotateG_05"></div>
		            <div class="blockG" id="rotateG_06"></div>
		            <div class="blockG" id="rotateG_07"></div>
		            <div class="blockG" id="rotateG_08"></div>
		        </div>
		    </div>
		</div>



	<script type="text/javascript">

	currentMenuId = "#reportsMenu"
	$('.menu-reports-application-conflicts').addClass('active');
	$('.menu-parent-reports').addClass('active');

	$(document).ready(function() {
		$("#moveBundleId").prepend("<option value='' disabled >──────────</option>")
				.prepend("<option value='useForPlanning' id='planningBundlesId'>Planning Bundles</option>");
		$("#applicationConflictsButton").removeAttr('disabled');

		$("#applicationConflictsButton").click(function(){
					$("#overlay").css('display', 'inline')
					$("#applicationConflictsButton").attr('disabled', true)
					var form = $("form")[0]
					form.submit()
		})

	});
	function submitForm(form){
		if($("form input:radio:checked").val() == "web"){
			$('#checkListId').html('Loading...')
			jQuery.ajax({
				url: $(form).attr('action'),
				data: $(form).serialize(),
				type:'POST',
				success: function(data) {
					$('#checkListId').html(data)
				}
			});
			return false
		} else {
			return true
		}
	}

	function verifyBundle(){
		if( $('#moveBundleId').val() == null ) {
			$('#preMoveErrorId').css('display','block')
			return false
		} else {
			return true
		}
	}

	</script>
</body>
</html>

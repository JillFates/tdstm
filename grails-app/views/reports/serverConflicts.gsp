<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<asset:stylesheet href="css/rackLayout.css" />
<asset:stylesheet href="css/spinner.css" />
<title>Server Conflicts</title>
<g:javascript src="report.js"/>
</head>
<body>
	<tds:subHeader title="Server Conflicts" crumbs="['Reports', 'Server']"/>
	<div class="body">
		<div class="message" id="preMoveErrorId" style="display: none">Please select the bundle to start the report.</div>

		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<g:form action="generateServerConflicts" name="serverConflicts" method="post" onsubmit="return disableGenerateButton(this.name)">
			<table>
				<tbody>
					<tr>
						<td>Bundle: <g:select from="${moveBundles}" id="moveBundleId" name="moveBundle"
								optionKey="id" optionValue="name" value="${moveBundleId}"/></td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="bundleConflicts" id="bundleConflicts" checked="checked" />&nbsp; <b>Bundle Conflict</b> - Having dependency references to assets assigned to unrelated bundles
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="unresolvedDep" id="unresolvedDep" checked="checked" />&nbsp;<b>Unresolved Dependencies</b> - Having dependencies with status <em>Unknown</em> or <em>Questioned</em>
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="noRuns" id="noRuns" checked="checked" />&nbsp; <b>No Supports Dependencies</b> - Having no <em>Supports</em> relationship depicting its purpose
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="vmWithNoSupport" id="vmWithNoSupport" checked="checked" />&nbsp; <b>No VM Host</b>- VMs with no associated Host environment
						</td>
					</tr>

					<tr>
							<td>Maximum Servers to report:
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
							<button type="button" class="btn btn-default" id="serverConflictsButton"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Generate</button>
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
	$('.menu-reports-server-conflicts').addClass('active');
	$('.menu-parent-reports').addClass('active');

	$(document).ready(function() {
		$("#moveBundleId").prepend("<option value='' disabled >──────────</option>")
				.prepend("<option value='useForPlanning' id='planningBundlesId'>Planning Bundles</option>");
		$("#serverConflictsButton").removeAttr('disabled');
		$("#serverConflictsButton").click(function(){
					$("#overlay").css('display', 'inline')
					$("#serverConflictsButton").attr('disabled', true)
					var form = $("form")[0]
					form.submit()
		})
	});
	</script>
</body>
</html>

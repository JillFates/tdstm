<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<title>Server Conflicts</title>
<g:javascript src="report.js"/>
</head>
<body>
	<div class="body">
		<h1>Server Conflicts</h1>
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
					<tr class="buttonR">
					<tds:hasPermission permission="ShowMovePrep">
						<td><input type="submit" class="submit" value="Generate" id="serverConflictsButton"/></td>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
	<script type="text/javascript">
	
	currentMenuId = "#reportsMenu"
	$("#reportsMenuId a").css('background-color','#003366')
	
	$(document).ready(function() {
		$("#moveBundleId").prepend("<option value='' disabled >──────────</option>")
				.prepend("<option value='useForPlanning' id='planningBundlesId'>Planning Bundles</option>");
		$("#serverConflictsButton").removeAttr('disabled');
	});
	</script>
</body>
</html>

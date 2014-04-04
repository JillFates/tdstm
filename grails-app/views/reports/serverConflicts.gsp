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
							<input type="checkbox" name="bundleConflicts" id="bundleConflicts" checked="checked" />&nbsp; Bundle conflicts
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="unresolvedDep" id="unresolvedDep" checked="checked" />&nbsp;Unresolved dependencies
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="noRuns" id="noRuns" checked="checked" />&nbsp; No Runs On
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="vmWithNoSupport" id="vmWithNoSupport" checked="checked" />&nbsp; VM with no support
						</td>
					</tr>
					<tr>
						<td>
							<div style="width: 150px; float: left;">
								<label><strong>Output:</strong></label>&nbsp;<br />
								<label for="web"><input type="radio" name="output" id="web" checked="checked" value="web" />&nbsp;Web</label><br />
								<label for="pdf"><input type="radio" name="output" id="pdf" value="pdf" />&nbsp;PDF</label><br />
							</div>
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
		$("#moveBundleId").append("<option value='useForPlanning'>Planning Bundles</option>");
		$("#serverConflictsButton").removeAttr('disabled');
	});
	</script>
</body>
</html>

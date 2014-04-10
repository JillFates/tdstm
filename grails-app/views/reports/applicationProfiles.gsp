<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<title>Application Profiles</title>
<g:javascript src="report.js"/>
</head>
<body>
	<div class="body">
		<h1>Application Profiles</h1>
		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:form action="generateApplicationProfiles" name="applicationProfiles" method="post" onsubmit="return disableGenerateButton(this.name)">
			<table>
				<tbody>
					<tr>
						<td>Bundle : </td>
						<td><g:select from="${moveBundles}" id="moveBundleId" name="moveBundle" onChange="changeSmeSelect(this.value)"
								optionKey="id" optionValue="name" value="${moveBundleId}"/></td>
					</tr>
					<tbody id="smeAndAppOwnerTbody">
						<g:render template="smeSelectByBundle"  model="[smeList:smeList, appOwnerList:appOwnerList]" />
					</tbody>
					<tr>
						<td colspan="2">
							<div style="width: 150px; float: left;">
								<label><strong>Output:</strong></label>&nbsp;<br />
								<label for="web"><input type="radio" name="output" id="web" checked="checked" value="web" />&nbsp;Web</label><br />
								<label for="pdf"><input type="radio" name="output" id="pdf" value="pdf" />&nbsp;PDF</label><br />
							</div>
						</td>
					</tr>
					<tr class="buttonR">
					<tds:hasPermission permission="ShowMovePrep">
						<td colspan="2"><input type="submit" class="submit" value="Generate" id="applicationProfilesButton"/></td>
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
			$("#applicationProfilesButton").removeAttr('disabled');
		});
		
	</script>
</body>
</html>
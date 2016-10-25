<%@page import="com.tds.asset.AssetComment"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<title>Application Migration Results</title>
<g:javascript src="report.js"/>
</head>
<body>
	<div class="body">
		<h1>Application Migration Results</h1>
		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:form action="generateApplicationMigration" name="applicationMigration" method="post" onsubmit="return disableGenerateButton(this.name)">
			<table class="event-day-table">
				<tbody>
					<tr>
						<td><label for="moveBundle">Bundle:</label></td>
						<td><g:select from="${moveBundles}" id="moveBundleId" name="moveBundle" onChange="changeSmeSelect(this.value,'migration')"
								optionKey="id" optionValue="name" value="${moveBundleId}"/></td>
					</tr>
					<tbody id="smeAndAppOwnerTbody">
						<g:render template="smeSelectByBundle"  model="[smeList:smeList, appOwnerList:'', forWhom:'migration']" />
					</tbody>
					<tr>
						<td><label for="startCateory">start of with:</label></td>
						<td><g:select from="${AssetComment.constraints.category.inList}" id="startCateory" name="startCateory" value='shutdown'/></td>
					</tr>
					<tr>
						<td><label for="workflowTransId">Testing:</label></td>
						<td>
							<g:select id="workflowTransId.id" name="workflowTransId" from="${workflowTransitions}" optionKey="id" optionValue="name"
								noSelection="['':'Please Select']"></g:select>
						</td>
					</tr>
					<tr>
						<td><label for="stopCateory">end with:</label></td>
						<td><g:select from="${AssetComment.constraints.category.inList}" id="stopCateory" name="stopCateory" value='startup'/></td>
					</tr>
					<tr>
						<td><label for="outageWindow">Outage window:</label></td>
						<td><g:select from="${appAttributes}" id="outageWindow" name="outageWindow" optionKey="attributeCode" optionValue="frontendLabel" 
							value="drRtoDesc" noSelection="['':'Please Select']"/></td>
					</tr>
					<tr class="buttonR">
					<tds:hasPermission permission="ShowMovePrep">
						<td>
							<button type="submit" class="btn btn-default" value="Generate" id="applicationMigrationButton"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Generate</button>
						</td>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
	<script type="text/javascript">
		currentMenuId = "#reportsMenu"
		$('.menu-reports-application-migration').addClass('active');
		$('.menu-parent-reports').addClass('active');
		
		$(document).ready(function() {
			$("#moveBundleId").prepend("<option value='' disabled >──────────</option>")
				.prepend("<option value='useForPlanning' id='planningBundlesId'>Planning Bundles</option>");
			$("#applicationMigrationButton").removeAttr('disabled');
			
		});
		
	</script>	
</body>
</html>
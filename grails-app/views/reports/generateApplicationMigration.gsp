<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Application Conflicts</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<script>
	$(document).ready(function() {
		$("#showEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		currentMenuId = "#reportsMenu";
		$("#reportsMenuId a").css('background-color','#003366')
	});
</script>
</head>
<body>
	<div class="body" style="width:1000px;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Application Conflicts - ${project.name} : ${moveBundle}</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div> 
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}

		<table>
			<tbody>
				<tr>
					<td >
					</td>
					<td>
						<g:each var="appList" in="${appList}" var="application" status="i">
							<table class="conflictApp">
									<thead>
										<tr>
											<th colspan="3">
												<a href="javascript:getEntityDetails('Application','Application',${application.app.id})" class="inlineLink">${application.app.assetName}</a>
												<g:if test="${application.app.moveBundle.useForPlanning}"> (${application.app.moveBundle})</g:if>
											</th>
										</tr>
									</thead>
								<tbody class="conflictAppBody">
									<tr>
										<td>Application Name:</td>
										<td>${application.app.assetName}</td>
									</tr>
									<tr>
										<td>Application SME:</td>
										<td>${sme}</td>
									</tr>
									<tr>
										<td>Start:</td>
										<td>${application.startTime}</td>
									</tr>
									<tr>
										<td>Test:</td>
										<td>${application.workflow }</td>
									</tr>
									<tr>
										<td>Finish:</td>
										<td>${application.finishTime}</td>
									</tr>
									<tr>
										<td>Duration:</td>
										<td>${application.duration}</td>
									</tr>
									<g:if test="${application.customParam}">
										<tr>
											<td>Window:</td>
											<td><span style="color:${application.windowColor};" > ${application.customParam}</span></td>
										</tr>
									</g:if>
								</tbody>
							</table>
						</g:each>
					</td>
				</tr>
			</tbody>
		</table>
	<div id="showEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>

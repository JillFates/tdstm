<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Application Migration Results</title>
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
			<b>Application Migration Results - ${project.name} : ${moveBundle}</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div> 
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}
		<br/>
		<table style="margin-left:5%;">
			<thead>
				<tr>
					<th>Name</th>
					<th>SME</th>
					<th>Start</th>
					<th>Test</th>
					<th>Finish</th>
					<th>Duration (hh:mm)</th>
					<th>Window</th>
				</tr>
			</thead>
			<tbody>
			<g:each var="appList" in="${appList}" var="application" status="i">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" align="center">
					<td>
						<a href="javascript:getEntityDetails('Application','Application',${application.app.id})" class="inlineLink">${application.app.assetName}</a>
					</td>
					<td>${application.app.sme}</td>
					<td>${application.startTime}</td>
					<td>${application.workflow }</td>
					<td>${application.finishTime}</td>
					<td>${application.duration}</td>
					<td>
						<g:if test="${application.customParam}">
							<span style="color:${application.windowColor};" > ${application.customParam}</span>
						</g:if>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	<div id="showEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>

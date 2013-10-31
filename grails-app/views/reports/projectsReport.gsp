<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Projects Summary Report</title>
<script type="text/javascript">
	$(document).ready(function() {
		currentMenuId = "#adminMegaMenu";
		$("#adminMenuId a").css('background-color', '#003366')
	});
</script>
</head>
<body>
	<div class="body">
		<h1>Projects Summary Report</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<h3>Generated for ${person} on <tds:convertDateTime  timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" date="${time}" />.</h3>
		<table>
			<thead>
				<tr>
					<th>Client</th>
					<th>Partner</th>
					<th>Project</th>
					<th>Start</th>
					<th>Finish</th>
					<th>Staff</th>
					<th>Events</th>
					<th>Servers</th>
					<th>Applications</th>
					<th>Databases</th>
					<th>Storage</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${projects}" var="project" status="i">
					<tr class="${i%2==0 ? 'even' : 'odd' }">
						<td>
							${project.client.name}
						</td>
						<td>${project.patner?.partyIdTo}</td>
						<td>
							${project.projectCode}
						</td>
						<td>
							<tds:convertDate timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" date="${project.startDate}" />
						</td>
						<td>
							<tds:convertDate timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" date="${project.completionDate}" />
						</td>
						<td>${project.projectCompanyStaff.size()?:''}</td>
						<td>${project.moveEventCount?:''}</td>
						<td>${assetEntity.getAssetEntityCountByProjectAndUseOfPlannig(project,'Server')[0] ?:'' }</td>
						<td>${assetEntity.getAssetEntityCountByProjectAndUseOfPlannig(project,'Application')[0] ?:'' }</td>
						<td>${assetEntity.getAssetEntityCountByProjectAndUseOfPlannig(project,'Database')[0] ?:'' }</td>
						<td>${assetEntity.getAssetEntityCountByProjectAndUseOfPlannig(project,'Files')[0] ?:'' }</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</body>
</html>

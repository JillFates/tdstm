<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Application Event Results</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<g:javascript src="model.manufacturer.js"/>
<g:render template="/layouts/responsiveAngularResources" />
<g:javascript src="asset.comment.js" />
<jqgrid:resources />
<g:javascript src="jqgrid-support.js" />
<asset:stylesheet href="css/jquery.autocomplete.css" />
<asset:stylesheet href="css/ui.accordion.css" />
<asset:stylesheet href="css/ui.resizable.css" />
<asset:stylesheet href="css/ui.slider.css" />
<asset:stylesheet href="css/ui.tabs.css" />
<asset:stylesheet href="css/ui.datepicker.css" />
<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
<script>
	$(document).ready(function() {
		$("#showEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		currentMenuId = "#reportsMenu";
		$('.menu-reports-application-migration').addClass('active');
		$('.menu-parent-reports').addClass('active');
	});
</script>
</head>
<body>
	<tds:subHeader title="Application Event Results" crumbs="['Reports', 'Application Event Results']"/>
	<div class="body" style="width:1000px;" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>${project.name} : ${moveBundle}</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${tds.currentPersonName()}.
		</div>
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}
		<br/>
		<table class="event-day-table" style="margin-left:5%;" ng-controller="tds.comments.controller.MainController as comments">
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
						<a href="javascript:EntityCrud.showAssetDetailView('${application.app.assetClass}',${application.app.id})" class="inlineLink">${application.app.assetName}</a>
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
		<g:render template="/assetEntity/modelDialog"/>
		<g:render template="/assetEntity/entityCrudDivs" />
		<g:render template="/assetEntity/dependentAdd" />
        <g:render template="/assetEntity/initAssetEntityData"/>
	</div>
</body>
</html>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Application Migration Results</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<g:javascript src="model.manufacturer.js"/>
<g:javascript src="angular/angular.min.js" />
<g:javascript src="angular/plugins/angular-ui.js"/>
<g:javascript src="angular/plugins/angular-resource.js" />
<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
<g:javascript src="asset.comment.js" />
<g:javascript src="cabling.js"/>
<jqgrid:resources />
<g:javascript src="jqgrid-support.js" />
<g:javascript src="bootstrap.js" />
<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<link href="/tdstm/css/jqgrid/ui.jqgrid.css" rel="stylesheet" type="text/css" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
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
	<div class="body" style="width:1000px;" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Application Migration Results - ${project.name} : ${moveBundle}</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div> 
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}
		<br/>
		<table style="margin-left:5%;" ng-controller="tds.comments.controller.MainController as comments">
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
		<g:render template="../assetEntity/modelDialog"/>
		<g:render template="../assetEntity/entityCrudDivs" />
		<g:render template="../assetEntity/dependentAdd" />
        <g:render template="../assetEntity/initAssetEntityData"/>
	</div>
</body>
</html>

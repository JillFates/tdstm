<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Application Profiles</title>
	<g:javascript src="bootstrap.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="projectStaff.js" />
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="angular/plugins/angular-ui.js"/>	
	<g:javascript src="angular/plugins/angular-resource.js" />
	<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
	<g:javascript src="asset.comment.js" />
	<g:javascript src="cabling.js"/>	
	<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />	
	<style type="text/css" media="print">
	<%--Had given these css property in css file but was not reflecting. so defined in page itself--%>
	@page {
		size: auto; /* auto is the current printer page size */
		margin: 0mm; /* this affects the margin in the printer settings */
	}

	body {
		position: relative;
	}

	table.tablePerPage {
		page-break-inside: avoid;
		-webkit-region-break-inside: avoid;
		position: relative;
	}

	div.onepage {
		page-break-after: always;
	}
	</style>
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
	<div class="body" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets" style="width:1000px;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Application Profiles - ${project.name} : ${moveBundle}, SME : ${sme} and App Owner : ${appOwner}</b><br/>
			This report generated on <tds:convertDateTime date="${new Date()}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div>

		<g:each var="appList" in="${applicationList}" var="application" status="i">
			<div class='onepage'>
				<table style="margin-left:80px;" class="tablePerPage">
					<tbody>
						<tr>
							<th>
								<a href="javascript:EntityCrud.showAssetDetailView('${application.app.assetClass}',${application.app.id})" class="inlineLink">${application.app.assetName}</a>
									<g:if test="${application.app.moveBundle?.useForPlanning}"> (${application.app.moveBundle})</g:if>
									- Supports ${application.supportAssets.size()} , Depends on ${application.dependentAssets.size()}
							</th>
							<td></td>
						</tr>
						<tr>
							<td colspan="2">
								<g:render template="../application/show" model="[applicationInstance:application.app, config:application.config, customs:application.customs, project:project, highlightMap:application.highlightMap]" ></g:render>			
							</td>
						</tr>
						<tr id="deps">
							<g:render template="../assetEntity/dependentShow" model="[assetEntity:application.app,supportAssets:application.supportAssets,dependentAssets:application.dependentAssets]" ></g:render>
						</tr>
					</tbody>
				</table>
			</div>
		</g:each>
		<div ng-controller="tds.comments.controller.MainController as comments">
			<jqgrid:wrapper id="applicationId" />
		</div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>

	</div>
	<g:render template="../assetEntity/initAssetEntityData"/>
</body>
</html>
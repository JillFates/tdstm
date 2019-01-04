<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Application Profiles</title>
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="projectStaff.js" />
	<g:render template="/layouts/responsiveAngularResources" />
	<g:javascript src="asset.comment.js" />
	<style type="text/css" media="print">
	<%--Had given these css property in css file but was not reflecting. so defined in page itself--%>
	@page {
		size: landscape; /* auto is the current printer page size */
		margin: 4mm; /* this affects the margin in the printer settings */
	}

	body {
		position: relative;
	}

	table.tablePerPage {
		page-break-inside: avoid;
		-webkit-region-break-inside: avoid;
		position: relative;
		margin-left:0px !important;
	}

	div.onepage {
		page-break-after: always;
	}
	</style>
</head>
<body>
	<div class="body content-generate-application-profiles" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets" style="width:1000px;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Application Profiles - ${project.name} : ${moveBundle}, SME : ${sme} and App Owner : ${appOwner}</b><br/>
			This report generated on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${tds.currentPersonName()}.
		</div>

		<g:each var="appList" in="${applicationList}" var="application" status="i">
			<div class='onepage'>
				<table style="margin-left:80px;" class="tablePerPage planning-application-table">
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
								<g:render template="/application/show"
										  model="[applicationInstance:application.app, customs: customs,
												  project:project, standardFieldSpecs: standardFieldSpecs,
												  shutdownBy: application.shutdownBy, shutdownById: application.shutdownById,
												  startupBy: application.startupBy, startupById: application.startupById,
												  testingBy: application.testingBy, testingById: application.testingById]" >
								</g:render>
							</td>
						</tr>
						<tr id="deps">
							<g:render template="/assetEntity/dependentShow" model="[assetEntity:application.app,supportAssets:application.supportAssets,dependentAssets:application.dependentAssets]" ></g:render>
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
	<g:render template="/assetEntity/initAssetEntityData"/>

	<script type="application/javascript">
		$(document).ready(function() {
			$("#showEntityView").dialog({ autoOpen: false });
			$("#editEntityView").dialog({ autoOpen: false });
			currentMenuId = "#reportsMenu";
			$('.menu-reports-application-profiles').addClass('active');
			$('.menu-parent-reports').addClass('active');
			$('[data-toggle="popover"]').popover();
		});
	</script>

</body>
</html>

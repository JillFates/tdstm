<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Application Conflicts</title>
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:render template="/layouts/responsiveAngularResources" />
	<g:javascript src="asset.comment.js" />
	<script>
		$(document).ready(function() {
			$("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
			currentMenuId = "#reportsMenu";
			$('.menu-reports-application-conflicts').addClass('active');
			$('.menu-parent-reports').addClass('active');
		});
	</script>
</head>
<body>
	<div class="body" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets" style="width:1000px;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Application Conflicts - ${project.name} : ${moveBundle} and App Owner:${currAppOwner}</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${tds.currentPersonName()}.
		</div>
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}

		<table class="planning-application-table">
			<tbody>
				<tr>
					<td></td>
					<td>
						<g:each var="appList" in="${appList}" var="application" status="i">
							<table class="conflictApp planning-application-table">
								<thead>
									<tr>
										<th colspan="${columns}">
											<a href="javascript:EntityCrud.showAssetDetailView('${application.app.assetClass}',${application.app.id})" class="inlineLink">${application.app.assetName}</a>
											<g:if test="${application.app.moveBundle.useForPlanning}"> (${application.app.moveBundle})</g:if> - Supports ${application.supportsList.size()} , Depends on ${application.dependsOnList.size()}
										</th>
									</tr>
								</thead>
								<tbody class="conflictAppBody">

									<g:if test="${application.supportsList.size() > 0}">
										<tr>
											<td class="leftCells"></td>
											<td colspan="${columns-1}">Supports (${application.supportsIssueCount} Issues)</td>
										</tr>
										<tr class="headRow">
											<td class="leftCells"></td>
											<td class="leftCells"></td>
											<td>Type</td>
											<td>Class</td>
											<td>Name</td>
											<td>Frequency</td>
											<td>Bundle</td>
											<td>Status</td>
										</tr>
										<g:each in="${application.supportsList}" var="supports" status="j">
											<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
												<td class="leftCells"></td>
												<td class="leftCells"></td>
												<td>
													${supports.type}
												</td>
												<td>
													${supports.asset.assetType}
												</td>
												<td>
													${supports.asset.assetName}
												</td>
												<td>
													${supports.dataFlowFreq}
												</td>
												<td>
													<g:if test="${supports.asset.moveBundle != application.app.moveBundle}"><b style="color:red;">${supports.asset.moveBundle}</b></g:if>
													<g:else>${supports.asset.moveBundle}</g:else>
												</td>
												<td>
													<g:if test="${supports.status in ['Questioned','Unknown']}"><b style="color:red;">${supports.status}</b></g:if>
													<g:else>${supports.status}</g:else>
												</td>
											</tr>
										</g:each>
									</g:if>

									<g:if test="${application.dependsOnList.size() > 0}">
										<tr>
											<td class="leftCells"></td>
											<td colspan="${columns-1}">Dependencies (${application.dependsOnIssueCount} Issues)</td>
										</tr>
										<tr class="headRow">
											<td class="leftCells"></td>
											<td class="leftCells"></td>
											<td>Type</td>
											<td>Class</td>
											<td>Name</td>
											<td>Frequency</td>
											<td>Bundle</td>
											<td>Status</td>
										</tr>
										<g:each in="${application.dependsOnList}" var="depOn" status="j">
											<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
												<td class="leftCells"></td>
												<td class="leftCells"></td>
												<td>
													${depOn.type}
												</td>
												<td>
													${depOn.dependent.assetType}
												</td>
												<td>
													${depOn.dependent.assetName}
												</td>
												<td>
													${depOn.dataFlowFreq}
												</td>
												<td>
													<g:if test="${depOn.dependent.moveBundle != application.app.moveBundle}"><b style="color:red;">${depOn.dependent.moveBundle}</b></g:if>
													<g:else>${depOn.dependent.moveBundle}</g:else>
												</td>
												<td>
													<g:if test="${depOn.status in ['Questioned','Unknown']}"><b style="color:red;">${depOn.status}</b></g:if>
													<g:else>${depOn.status}</g:else>
												</td>
											</tr>
										</g:each>
									</g:if>
								</tbody>
							</table>
						</g:each>
					</td>
				</tr>
			</tbody>
		</table>
		<div ng-controller="tds.comments.controller.MainController as comments">
			<jqgrid:wrapper id="applicationId" />
		</div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>

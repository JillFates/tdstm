<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Database Conflicts</title>
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
	<div class="body" style="width:1000px;margin-left: 10%;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Database Conflicts - ${project.name} : ${moveBundle} - Includes databases matching: ${title?:'' }</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div> 
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}
		<table>
			<tbody id="serverConflictTbody">
				<tr>
					<td >
					</td>
					<td>
						<g:each var="appList" in="${appList}" var="assetEntity" status="i">
							<table class="conflictApp">
								<thead>
									<tr>
										<th colspan="${columns}">
											<a href="javascript:getEntityDetails('database','Database',${assetEntity.app.id})" class="inlineLink">${assetEntity.app.assetName}</a>
											<g:if test="${assetEntity.app.moveBundle.useForPlanning}"> (${assetEntity.app.moveBundle})</g:if> 
												- Supports ${assetEntity.supportsList.size()} , Depends on ${assetEntity.dependsOnList.size()} 
												<span style="color: red;">${assetEntity.header?' - '+assetEntity.header:''}</span>
										</th>
									</tr>
								</thead>
								<tbody class="conflictAppBody">
								
									<g:if test="${assetEntity.supportsList.size() > 0}">
										<tr>
											<td class="leftCells"></td>
											<td colspan="${columns-1}">Supports (${assetEntity.supportsIssueCount} Issues)</td>
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
										<g:each in="${assetEntity.supportsList}" var="supports" status="j">
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
													<g:if test="${supports.asset.moveBundle != assetEntity.app.moveBundle}"><b style="color:red;">${supports.asset.moveBundle}</b></g:if>
													<g:else>${supports.asset.moveBundle}</g:else>
												</td>
												<td>
													<g:if test="${supports.status in ['Questioned','Unknown']}"><b style="color:red;">${supports.status}</b></g:if>
													<g:else>${supports.status}</g:else>
												</td>
											</tr>
										</g:each>
									</g:if>
									
									<g:if test="${assetEntity.dependsOnList.size() > 0}">
										<tr>
											<td class="leftCells"></td>
											<td colspan="${columns-1}">Dependencies (${assetEntity.dependsOnIssueCount} Issues)</td>
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
										<g:each in="${assetEntity.dependsOnList}" var="depOn" status="j">
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
													<g:if test="${depOn.dependent.moveBundle != assetEntity.app.moveBundle}"><b style="color:red;">${depOn.dependent.moveBundle}</b></g:if>
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
		
	<div id="showEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>

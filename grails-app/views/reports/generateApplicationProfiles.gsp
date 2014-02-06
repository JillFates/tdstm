<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Application Profiles</title>
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
			<b>Application Profiles - ${project.name} : ${moveBundle} and SME : ${sme}</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div>

<g:each var="appList" in="${applicationList}" var="application" status="i">
		<table style="margin-left:80px;">
			<tbody>
				<tr>
				<th colspan="5"><a href="javascript:getEntityDetails('Application','Application',${application.app.id})" class="inlineLink">${application.app.assetName}</a>
						<g:if test="${application.app.moveBundle.useOfPlanning}"> (${application.app.moveBundle})</g:if> - Supports ${application.supportAssets.size()} , Depends on ${application.dependentAssets.size()}</th>
				</tr>
				<tr>
				<td colspan="2">
				<table class="conflictApp">
					<tbody class="conflictAppBody">
						<tr>
							<td class="label nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;" >${application.app.assetName}</td>
							<td class="label" nowrap="nowrap">Description</td>
							<td colspan="5">${application.app.description}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td class="valueNW">${application.app.assetType}</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW">${application.app.supportType}</td>
							<td class="label" nowrap="nowrap"><label for="appFunction">Function</label></td>
							<td class="valueNW">${application.app.appFunction}</td>
							<td class="label" nowrap="nowrap"><label for="userCount">Users</label></td>
							<td class="valueNW">${application.app.userCount}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
							<td class="valueNW">${application.app.appVendor}</td>
							<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
							<td class="valueNW">${application.app.sme?.lastNameFirst}</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW">${application.app.environment}</td>
							<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label></td>
							<td class="valueNW">${application.app.userLocations}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVersion">Version</label></td>
							<td class="valueNW">${application.app.appVersion}</td>
							<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
							<td class="valueNW">${application.app.sme2?.lastNameFirst}</td>
							<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label></td>
							<td class="valueNW">${application.app.criticality}</td>
							<td class="label" nowrap="nowrap"><label for="useFrequency">Use Frequency</label></td>
							<td class="valueNW">${application.app.useFrequency}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label></td>
							<td class="valueNW">${application.app.appTech}</td>
							<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
							<td class="valueNW">${application.app.appOwner?.lastNameFirst}</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle / Dep. Group</label></td>
							<td class="valueNW">${application.app.moveBundle} / ${dependencyBundleNumber}</td>
							<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label></td>
							<td class="valueNW">${application.app.drRpoDesc}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appSource">Source</label></td>
							<td class="valueNW">${application.app.appSource}</td>
							<td class="label" nowrap="nowrap"><label for="businessUnit">Bus Unit</label></td>
							<td class="valueNW">${application.app.businessUnit}</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW">${application.app.planStatus}</td>
							<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label></td>
							<td class="valueNW">${application.app.drRtoDesc}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">License</label></td>
							<td class="valueNW">${application.app.license}</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td><tds:convertDate
									date="${application.app?.retireDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap">Validation</td>
							<td >${application.app.validation}</td>
							<td class="label" nowrap="nowrap"><label for="testProc">Test Proc OK</label></td>
							<td>${application.app.testProc ? application.app.testProc : '?'}</td>
						</tr>
						<tr>
							<td></td>
							<td></td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td class="valueNW"><tds:convertDate
									date="${application.app?.maintExpDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td class="label" nowrap="nowrap"><label for="latency">Latency OK</label></td>
							<td class="valueNW">${application.app.latency ? application.app.latency : '?'}</td>
							<td class="label" nowrap="nowrap"><label for="startupProc">Startup Proc OK</label></td>
							<td class="valueNW">${application.app.startupProc ? application.app.startupProc : '?'}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">URL</label></td>
							<td class="valueNW" ><a href="${application.app.url}" style="color:#00E">${application.app.url}</a></td>

							<td class="label" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
							<td>${application.app.externalRefId}</td>
							<td class="label" nowrap="nowrap"><label for="shutdownBy">Shutdown By</label></td>
							<td class="valueNW" nowrap="nowrap">${shutdownBy}
							<g:if test="${application.app.shutdownFixed ==1 }">
								<input type="checkbox" id="shutdownFixedShowId" disabled="disabled" name="shutdownFixed" checked="checked"/>
									<label for="shutdownFixedId" >Fixed</label>
							</g:if>
							</td>
							<td class="label" nowrap="nowrap"><label for="shutdownDuration">Shutdown Duration</label></td>
							<td class="valueNW" nowrap="nowrap">${application.app.shutdownDuration ? application.app.shutdownDuration+'m' : ''}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="startupBy">Startup By</label></td>
							<td class="valueNW" nowrap="nowrap">${startupBy}
							<g:if test="${application.app.startupFixed ==1 }">
								<input type="checkbox" id="startupFixedShowId" disabled="disabled" name="startupFixed" value="${application.app.startupFixed}" 
										checked="checked"/>
									<label for="startupFixedId" >Fixed</label>
							</g:if>
							</td>
							<td class="label" nowrap="nowrap"><label for="shutdownDuration">Startup Duration</label></td>
							<td class="valueNW" nowrap="nowrap">${application.app.startupDuration ? application.app.startupDuration+'m' :''} </td>
							<td class="label" nowrap="nowrap"><label for="testingBy">Testing By</label></td>
							<td class="valueNW" nowrap="nowrap">${testingBy}
							  <g:if test="${application.app.testingFixed ==1 }">
								<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
									<label for="testingFixedId" >Fixed</label>
							  </g:if>
							</td>
							<td class="label" nowrap="nowrap"><label for="testingDuration">Testing Duration</label></td>
							<td class="valueNW " nowrap="nowrap">${application.app.testingDuration ? application.app.testingDuration+'m' :''}</td>
						</tr>
						<g:render template="../assetEntity/customShow" model="['assetEntity':application.app]"></g:render>
                        <tr>
						   	<td class="label" nowrap="nowrap" ><label for="events">Event</label></td>
						   	<td colspan="7">
							    <g:each in="${moveEventList}" var="moveEventList">
								  <div  class="label" style="float: left;width: auto;padding: 5px;" nowrap="nowrap" ><label for="moveEvent"><b>${application.moveEventList.name} :</b> </label>
								  <g:if test="${AppMoveEvent.findByMoveEventAndApplication(application.moveEventList,application.app)?.value=='Y'}">Y</g:if>
								  <g:elseif test="${AppMoveEvent.findByMoveEventAndApplication(application.moveEventList,application.app)?.value=='N'}">N</g:elseif>
								  <g:else>?</g:else>
								  </div>
							  </g:each>
							</td>
					    </tr>
					  </tbody>
					</table>			
				</td>
				</tr>
				<tr id="deps">
					<g:render template="../assetEntity/dependentShow" model="[assetEntity:application,supportAssets:application.supportAssets,dependentAssets:application.dependentAssets]" ></g:render>
				</tr>
			</tbody>
		</table>
	</g:each>
	<div id="showEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>

<table>
	<tbody>
		<tr>
			<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
			<td style="font-weight:bold;" class="${config.assetName}">${applicationInstance.assetName}</td>
			<td class="label ${config.description}" nowrap="nowrap"><label for="description">Description</label></td>
			<td colspan="5" class="${config.description}">${applicationInstance.description}</td>
		</tr>
		<tr>
			<td class="label" nowrap="nowrap"><label for="appAccess">Type</label></td>
			<td class="valueNW">${applicationInstance.assetType}</td>
			<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support</label></td>
			<td class="valueNW ${config.supportType}">${applicationInstance.supportType}</td>
			<td class="label ${config.appFunction}" nowrap="nowrap"><label for="appFunction">Function</label></td>
			<td class="valueNW ${config.appFunction}">${applicationInstance.appFunction}</td>
			<td class="label ${config.userCount}" nowrap="nowrap"><label for="userCount">Users</label></td>
			<td class="valueNW ${config.userCount}">${applicationInstance.userCount}</td>
		</tr>
		<tr>
			<td class="label ${config.appVendor}" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
			<td class="valueNW ${config.appVendor}">${applicationInstance.appVendor}</td>
			<td class="label ${config.sme}" nowrap="nowrap"><label for="sme">SME1</label></td>
			<td class="valueNW ${config.sme}">${applicationInstance.sme?.lastNameFirst}</td>
			<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
			<td class="valueNW ${config.environment}">${applicationInstance.environment}</td>
			<td class="label ${config.userLocations}" nowrap="nowrap"><label for="userLocations">User Location</label></td>
			<td class="valueNW ${config.userLocations}">${applicationInstance.userLocations}</td>
		</tr>
		<tr>
			<td class="label ${config.appVersion}" nowrap="nowrap"><label for="appVersion">Version</label></td>
			<td class="valueNW ${config.appVersion}">${applicationInstance.appVersion}</td>
			<td class="label ${config.sme2}" nowrap="nowrap"><label for="sme2">SME2</label></td>
			<td class="valueNW ${config.sme2}">${applicationInstance.sme2?.lastNameFirst}</td>
			<td class="label ${config.criticality}" nowrap="nowrap"><label for="criticality">Criticality</label></td>
			<td class="valueNW ${config.criticality}">${applicationInstance.criticality}</td>
			<td class="label ${config.useFrequency}" nowrap="nowrap"><label for="useFrequency">Use Frequency</label></td>
			<td class="valueNW ${config.useFrequency}">${applicationInstance.useFrequency}</td>
		</tr>
		<tr>
			<td class="label ${config.appTech}" nowrap="nowrap"><label for="appTech">Tech.</label></td>
			<td class="valueNW ${config.appTech}">${applicationInstance.appTech}</td>
			<td class="label ${config.owner}" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
			<td class="valueNW ${config.owner}">${applicationInstance.appOwner?.lastNameFirst}</td>
			<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle / Dep. Group</label></td>
			<td class="valueNW ${config.moveBundle}">${applicationInstance.moveBundle} / ${dependencyBundleNumber}</td>
			<td class="label ${config.drRpoDesc}" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label></td>
			<td class="valueNW ${config.drRpoDesc}">${applicationInstance.drRpoDesc}</td>
		</tr>
		<tr>
			<td class="label ${config.appSource}" nowrap="nowrap"><label for="appSource">Source</label></td>
			<td class="valueNW ${config.appSource}">${applicationInstance.appSource}</td>
			<td class="label ${config.businessUnit}" nowrap="nowrap"><label for="businessUnit">Bus Unit</label></td>
			<td class="valueNW ${config.businessUnit}">${applicationInstance.businessUnit}</td>
			<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
			<td class="valueNW ${config.planStatus}">${applicationInstance.planStatus}</td>
			<td class="label ${config.drRtoDesc}" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label></td>
			<td class="valueNW ${config.drRtoDesc}">${applicationInstance.drRtoDesc}</td>
		</tr>
		<tr>
			<td class="label ${config.license}" nowrap="nowrap"><label for="license">License</label></td>
			<td class="valueNW ${config.license}">${applicationInstance.license}</td>
			<td class="label ${config.retireDate}" nowrap="nowrap"><label for="retireDate">Retire</label></td>
			<td class="${config.retireDate}"><tds:convertDate
					date="${applicationInstance?.retireDate}"
					timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
			<td class="label ${config.validation}" nowrap="nowrap"><label for="validation">Validation</label></td>
			<td class="${config.validation}">${applicationInstance.validation}</td>
			<td class="label ${config.testProc}" nowrap="nowrap"><label for="testProc">Test Proc OK</label></td>
			<td class="${config.testProc}">${applicationInstance.testProc ? applicationInstance.testProc : '?'}</td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td class="label ${config.maintExpDate}" nowrap="nowrap"><label for="maintExpDate">Maint Exp.</label></td>
			<td class="valueNW ${config.maintExpDate}"><tds:convertDate
					date="${applicationInstance?.maintExpDate}" formate="12hrs"
					timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
			</td>
			<td class="label ${config.latency}" nowrap="nowrap"><label for="latency">Latency OK</label></td>
			<td class="valueNW ${config.latency}">${applicationInstance.latency ? applicationInstance.latency : '?'}</td>
			<td class="label ${config.startupProc}" nowrap="nowrap"><label for="startupProc">Startup Proc OK</label></td>
			<td class="valueNW ${config.startupProc}">${applicationInstance.startupProc ? applicationInstance.startupProc : '?'}</td>
		</tr>
		<tr>
			<td class="label ${config.url}" nowrap="nowrap"><label for="url">URL</label></td>
			<td class="valueNW ${config.url}" ><a href="${applicationInstance.url}" style="color:#00E">${applicationInstance.url}</a></td>

			<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
			<td class="${config.externalRefId}">${applicationInstance.externalRefId}</td>
			<td class="label ${config.shutdownBy}" nowrap="nowrap"><label for="shutdownBy">Shutdown By</label></td>
			<td class="valueNW ${config.shutdownBy}" nowrap="nowrap">${shutdownBy}
			<g:if test="${applicationInstance.shutdownFixed ==1 }">
				<input type="checkbox" id="shutdownFixedShowId" disabled="disabled" name="shutdownFixed" checked="checked"/>
					<label for="shutdownFixedId" >Fixed</label>
			</g:if>
			</td>
			<td class="label ${config.shutdownDuration}" nowrap="nowrap"><label for="shutdownDuration">Shutdown Duration</label></td>
			<td class="valueNW ${config.shutdownDuration}" nowrap="nowrap">${applicationInstance.shutdownDuration ? applicationInstance.shutdownDuration+'m' : ''}</td>
		</tr>
		<tr>
			<td class="label ${config.startupBy}" nowrap="nowrap"><label for="startupBy">Startup By</label></td>
			<td class="valueNW ${config.startupBy}" nowrap="nowrap">${startupBy}
			<g:if test="${applicationInstance.startupFixed ==1 }">
				<input type="checkbox" id="startupFixedShowId" disabled="disabled" name="startupFixed" value="${applicationInstance.startupFixed}" 
						checked="checked"/>
					<label for="startupFixedId" >Fixed</label>
			</g:if>
			</td>
			<td class="label ${config.shutdownDuration}" nowrap="nowrap"><label for="shutdownDuration">Startup Duration</label></td>
			<td class="valueNW ${config.shutdownDuration}" nowrap="nowrap">${applicationInstance.startupDuration ? applicationInstance.startupDuration+'m' :''} </td>
			<td class="label ${config.testingBy}" nowrap="nowrap"><label for="testingBy">Testing By</label></td>
			<td class="valueNW ${config.testingBy}" nowrap="nowrap">${testingBy}
			  <g:if test="${applicationInstance.testingFixed ==1 }">
				<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
					<label for="testingFixedId" >Fixed</label>
			  </g:if>
			</td>
			<td class="label ${config.testingDuration}" nowrap="nowrap"><label for="testingDuration">Testing Duration</label></td>
			<td class="valueNW ${config.testingDuration}" nowrap="nowrap">${applicationInstance.testingDuration ? applicationInstance.testingDuration+'m' :''}</td>
		</tr>
			<g:render template="../assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
		<tr>
			<td class="label" nowrap="nowrap" ><label for="events">Event</label></td>
			<td colspan="7">
				<g:each in="${moveEventList}" var="moveEventList">
				  <div  class="label" style="float: left;width: auto;padding: 5px;" nowrap="nowrap" ><label for="moveEvent"><b>${moveEventList.name} :</b> </label>
				  <g:if test="${AppMoveEvent.findByMoveEventAndApplication(moveEventList,applicationInstance)?.value=='Y'}">Y</g:if>
				  <g:elseif test="${AppMoveEvent.findByMoveEventAndApplication(moveEventList,applicationInstance)?.value=='N'}">N</g:elseif>
				  <g:else>?</g:else>
				  </div>
			  </g:each>
			</td>
		</tr>						
	</tbody>
</table>
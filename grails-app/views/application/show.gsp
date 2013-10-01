<table style="border: 0">
	<tr>
	
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}', 'Application', ${applicationInstance?.id})"</tds:hasPermission>>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<table>
					<tbody>
						<tr>
							<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;" class="${config.assetName}">${applicationInstance.assetName}</td>
							<td class="label ${config.description}" nowrap="nowrap">Description</td>
							<td colspan="5" class="${config.description}">${applicationInstance.description}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
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
							<td class="label ${config.retireDate}" nowrap="nowrap">Retire</td>
							<td class="${config.retireDate}"><tds:convertDate
									date="${applicationInstance?.retireDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label ${config.validation}" nowrap="nowrap">Validation</td>
							<td class="${config.validation}">${applicationInstance.validation}</td>
							<td class="label ${config.testProc}" nowrap="nowrap"><label for="testProc">Test Proc OK</label></td>
							<td class="${config.testProc}">${applicationInstance.testProc ? applicationInstance.testProc : '?'}</td>
						</tr>
						<tr>
							<td></td>
							<td></td>
							<td class="label ${config.maintExpDate}" nowrap="nowrap">Maint Exp.</td>
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
							<td class="label ${config.url}" nowrap="nowrap"><label for="license">URL</label></td>
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
							<td class="label ${config.shutdownDuration}" nowrap="nowrap"><label for="shutdownDuration">Shutdown Dur.</label></td>
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
							<td class="label ${config.shutdownDuration}" nowrap="nowrap"><label for="shutdownDuration">Startup Dur.</label></td>
							<td class="valueNW ${config.shutdownDuration}" nowrap="nowrap">${applicationInstance.startupDuration ? applicationInstance.startupDuration+'m' :''} </td>
							<td class="label ${config.testingBy}" nowrap="nowrap"><label for="testingBy">testing By</label></td>
							<td class="valueNW ${config.testingBy}" nowrap="nowrap">${testingBy}
							  <g:if test="${applicationInstance.testingFixed ==1 }">
								<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
									<label for="testingFixedId" >Fixed</label>
							  </g:if>
							</td>
							<td class="label ${config.testingDuration}" nowrap="nowrap"><label for="testingDuration">Testing Dur.</label></td>
							<td class="valueNW ${config.testingDuration}" nowrap="nowrap">${applicationInstance.testingDuration ? applicationInstance.testingDuration+'m' :''}</td>
						</tr>
						<g:render template="../assetEntity/customShow" model="['assetEntity':applicationInstance]"></g:render>
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
			</div>
		</td>
	</tr>
	<tr id="deps">
		<g:render template="../assetEntity/dependentShow" model="[assetEntity:applicationInstance]" ></g:render>
	</tr>
	<tr id="commentListId">
		<g:render template="../assetEntity/commentList" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id="applicationId" value="${applicationInstance?.id}" />
					<tds:hasPermission permission='EditAndDelete'>
					<span class="button"><input type="button" class="edit" value="Edit" onclick="editEntity('${redirectTo}','Application',${applicationInstance?.id})" /> </span>
					<g:if test="${redirectTo!='dependencyConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId" name="deleteId" class="delete" value="Delete" onclick=" deleteAsset($('#applicationId').val(),'app')" /> </span>
					</g:else>
					</tds:hasPermission>
					<tds:hasPermission permission="CommentCrudView">	
						<a href="javascript:createIssue('${applicationInstance.assetName}','comment', ${applicationInstance.id}, 'update');">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
						</a>
						<a href="javascript:createIssue('${applicationInstance.assetName}','', ${applicationInstance.id}, 'update');">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Task
						</a>
					</tds:hasPermission>
				</g:form>
			</div></td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	$(document).ready(function() { 
		var prefVal = '${prefValue}'
		if(prefVal == 'FALSE')
			$(".resolved").hide()
		else
			$(".resolved").show()
			
		$(".showAllChecked").click(function(){
			 var selected = $(this).is(":checked") ? '1' : '0'
			 showTask(selected)
		})
		changeDocTitle('${applicationInstance.assetName}');
	})
</script>

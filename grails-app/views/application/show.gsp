<table style="border: 0">
	<tr>
	
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}', 'Application', ${applicationInstance?.id})"</tds:hasPermission>>
				<table>
					<tbody>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;" class="${config.assetName}">${applicationInstance.assetName}</td>
							<td class="label" nowrap="nowrap">Description</td>
							<td colspan="5" class="${config.description}">${applicationInstance.description}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td class="valueNW">${applicationInstance.assetType}</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW ${config.supportType}">${applicationInstance.supportType}</td>
							<td class="label" nowrap="nowrap"><label for="appFunction">Function</label></td>
							<td class="valueNW ${config.appFunction}">${applicationInstance.appFunction}</td>
							<td class="label" nowrap="nowrap"><label for="userCount">Users</label></td>
							<td class="valueNW ${config.userCount}">${applicationInstance.userCount}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
							<td class="valueNW ${config.appVendor}">${applicationInstance.appVendor}</td>
							<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
							<td class="valueNW ${config.sme}">${applicationInstance.sme}</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW ${config.environment}">${applicationInstance.environment}</td>
							<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label></td>
							<td class="valueNW ${config.userLocations}">${applicationInstance.userLocations}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVersion">Version</label></td>
							<td class="valueNW ${config.appVersion}">${applicationInstance.appVersion}</td>
							<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
							<td class="valueNW ${config.sme2}">${applicationInstance.sme2}</td>
							<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label></td>
							<td class="valueNW ${config.criticality}">${applicationInstance.criticality}</td>
							<td class="label" nowrap="nowrap"><label for="useFrequency">Use Frequency</label></td>
							<td class="valueNW ${config.useFrequency}">${applicationInstance.useFrequency}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label></td>
							<td class="valueNW ${config.appTech}">${applicationInstance.appTech}</td>
							<td class="label" nowrap="nowrap"><label for="businessUnit">Bus Unit</label></td>
							<td class="valueNW ${config.businessUnit}">${applicationInstance.businessUnit}</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
							<td class="valueNW ${config.moveBundle}">${applicationInstance.moveBundle} / ${dependencyBundleNumber}</td>
							<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label></td>
							<td class="valueNW ${config.drRpoDesc}">${applicationInstance.drRpoDesc}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appSource">Source</label></td>
							<td class="valueNW ${config.appSource}">${applicationInstance.appSource}</td>
							<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
							<td class="valueNW ${config.owner}">${applicationInstance.appOwner}</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW ${config.planStatus}">${applicationInstance.planStatus}</td>
							<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label></td>
							<td class="valueNW ${config.drRtoDesc}">${applicationInstance.drRtoDesc}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">License</label></td>
							<td class="valueNW ${config.license}">${applicationInstance.license}</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td class="${config.retireDate}"><tds:convertDate
									date="${applicationInstance?.retireDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap">Validation</td>
							<td class="${config.validation}">${applicationInstance.validation}</td>
							<td class="label" nowrap="nowrap"><label for="testProc">Test Proc OK</label></td>
							<td class="${config.testProc}">${applicationInstance.testProc ? applicationInstance.testProc : '?'}</td>
						</tr>
						<tr>
							<td></td>
							<td></td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td class="valueNW ${config.maintExpDate}"><tds:convertDate
									date="${applicationInstance?.maintExpDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td class="label" nowrap="nowrap"><label for="latency">Latency OK</label></td>
							<td class="valueNW ${config.latency}">${applicationInstance.latency ? applicationInstance.latency : '?'}</td>
							<td class="label" nowrap="nowrap"><label for="startupProc">Startup Proc OK</label></td>
							<td class="valueNW ${config.startupProc}">${applicationInstance.startupProc ? applicationInstance.startupProc : '?'}</td>
						</tr>
						<g:render template="../assetEntity/customShow" model="['assetEntity':applicationInstance]"></g:render>
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">URL</label></td>
							<td class="valueNW ${config.url}" colspan="7"><a href="${applicationInstance.url}" style="color:#00E">${applicationInstance.url}</a></td>
						</tr>
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
	<tr>
		<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Type</th>
							<th>Name</th>
							<th>Bundle</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${supportAssets}" var="support" status="i">
							<tr onclick="getEntityDetails('${redirectTo}','${support?.asset?.assetType}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.dataFlowFreq}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetType == 'Files' ? 'Storage' : support?.asset?.assetType}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetName}
								</td>
								<g:if test="${support?.asset?.moveBundle!=applicationInstance.moveBundle && support.status == 'Validated' }" >
									<td style="background-color: red">
										${support?.asset?.moveBundle}
									</td>
								 </g:if>
								 <g:elseif test="${support?.asset?.moveBundle!=applicationInstance.moveBundle }" >
									<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
										<b>${support?.asset?.moveBundle}</b>
									</td>
								 </g:elseif>
								 <g:else>
								    <td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
										${support?.asset?.moveBundle}
								    </td>
								 </g:else>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support.type}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
		<td valign="top">
			<div>
				<h1>Is dependent on:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Type</th>
							<th>Name</th>
							<th>Bundle</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr onclick="getEntityDetails('${redirectTo}','${dependent.dependent?.assetType}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dataFlowFreq}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dependent?.assetType == 'Files' ? 'Storage' : dependent.dependent?.assetType}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dependent?.assetName}
								</td>
								<g:if test="${dependent.dependent?.moveBundle!=applicationInstance.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: red">
										     ${dependent.dependent?.moveBundle}
									</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=applicationInstance.moveBundle }" >
									<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
										    <b>${dependent.dependent?.moveBundle}</b>
									</td>
								</g:elseif>
								<g:else>
								    <td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
										   ${dependent.dependent?.moveBundle}
									</td>
								</g:else>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.type}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<g:if test="${assetCommentList.size() > 0 }">
		<tr>
			<td colspan="2">
				<div class="list">
				<table id="listCommentsTables">
				<thead>
				<tr>
					<th nowrap class="headerwidth3"></th>
					<th nowrap class="headerwidth3">#</th>
					<th nowrap>Task/comment</th>
					<th nowrap class="headerwidth12">Status&nbsp;(&nbsp;
					<input type="checkbox" name="showAll" id="showAll" ${prefValue && prefValue == 'TRUE'?'checked="checked"':''} 
					class="showAllChecked"/>
					&nbsp;<label for="showAll">All )</label></th>
					<th nowrap class="headerwidth6">Category</th>  
					<th nowrap class="headerwidth20">Assigned To</th>
				</tr>
				</thead>
				<tbody id="listCommentsTbodyIds">
				<g:each status="i" in="${assetCommentList}"  var="commentList">
				<tr style="cursor: pointer;" class="${commentList.status == 'Completed' || commentList.status=='Pending' ? 'resolved' : 'ready' }">
					<td><a href ="javascript:showComment(${commentList.id},'edit')" ><img src="${resource(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" style="text-align: center;">${commentList.taskNumber ?:'c'}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.comment}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.status}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.category}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.assignedTo}/${commentList.role}</td>
				</tr>
				</g:each>
				</tbody>
				</table>
			</div>
			</td>
	</tr>
	</g:if>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id="applicationId" value="${applicationInstance?.id}" />
					<tds:hasPermission permission='EditAndDelete'>
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="editEntity('${redirectTo}','Application',${applicationInstance?.id})" /> </span>
					<g:if test="${redirectTo!='planningConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#applicationId').val(),'app')" /> </span>
					</g:else>
					</tds:hasPermission>
					<tds:hasPermission permission="CommentCrudView">	
						<a href="javascript:createIssue('${applicationInstance.assetName}','comment', ${applicationInstance.id});">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
						</a>
						<a href="javascript:createIssue('${applicationInstance.assetName}','', ${applicationInstance.id});">
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
	})
</script>

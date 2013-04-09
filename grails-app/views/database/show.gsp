	<table style="border: 0">
	<tr>
		<td colspan="2">
		
			<div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}','Database',${databaseInstance?.id})" </tds:hasPermission>>
		
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td class="valueNW" style="font-weight:bold;">${databaseInstance?.assetName}</td>
							<td class="label" nowrap="nowrap">Description</td>
							<td class="valueNW" colspan="5">${databaseInstance.description}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td class="valueNW">${databaseInstance?.assetType}</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW">${databaseInstance?.supportType}</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW" colspan="3">${databaseInstance?.environment}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="dbFormat">Format</label></td>
							<td class="valueNW">${databaseInstance?.dbFormat}</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td class="valueNW"><tds:convertDate date="${databaseInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
							<td class="valueNW" colspan="3">${databaseInstance?.moveBundle} / ${dependencyBundleNumber}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="dbSize">Size</label></td>
							<td class="valueNW">${databaseInstance?.dbSize}</td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td class="valueNW"><tds:convertDate date="${databaseInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW" colspan="3">${databaseInstance?.planStatus}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label>Version</label></td>
							<td></td>
							<td class="label" nowrap="nowrap"><label>SME1</label></td>
							<td></td>
							<td class="label">Validation</td>
							<td class="valueNW" colspan="3">${databaseInstance.validation}</td>
						</tr>
						<g:render template="../assetEntity/customShow" model="[assetEntity:databaseInstance, 'project':project]"></g:render>
					</tbody>
				</table>
			</div></td>
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
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support?.dataFlowFreq}</td>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
							${support?.asset?.assetType == 'Files' ? 'Storage' : support?.asset?.assetType}</td>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support?.asset?.assetName}</td>
							<g:if test="${support?.asset?.moveBundle!=databaseInstance.moveBundle && support.status == 'Validated' }" >
							<td style="background-color: red"> ${support?.asset?.moveBundle}</td></g:if>
						    <g:elseif test="${support?.asset?.moveBundle!=databaseInstance.moveBundle }" >
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
								<b>${support?.asset?.moveBundle}</b>
							</td>
							</g:elseif>
							<g:else>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
							${support?.asset?.moveBundle}
							</td>
							</g:else>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support.type}</td>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support.status}</td>
						
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	<td valign="top">
		<div>
			<h1>Depends on:</h1>
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
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">${dependent.dataFlowFreq}</td>
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
								${dependent.dependent?.assetType == 'Files' ? 'Storage' : dependent.dependent?.assetType}</td>
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">${dependent.dependent?.assetName}</td>
							<g:if test="${dependent.dependent?.moveBundle!=databaseInstance.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: red">
										     ${dependent.dependent?.moveBundle}
									</td>
							</g:if>
							<g:elseif test="${dependent.dependent?.moveBundle!=databaseInstance.moveBundle }" >
									<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
										    <b>${dependent.dependent?.moveBundle}</b>
									</td>
							</g:elseif>
							<g:else>
								    <td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
										   ${dependent.dependent?.moveBundle}
									</td>
							</g:else>
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">${dependent.type}</td>
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">${dependent.status}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	</tr>
	<g:if test="${assetCommentList.size() > 0 }">
	<tr>
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
					<input type="checkbox" name="showAll" id="showAll" ${prefValue && prefValue == 'TRUE' ?  'value="1" checked="checked"'  : 'value="0"'} 
					onchange="${remoteFunction(controller:'assetEntity', action:'setShowAllPreference',params:'\'selected=\'+ this.value')}" 
					onclick="if(this.checked){this.value = 1; $('.resolved').show();$('#showEntityView').dialog('option', 'height', 'auto')} else {this.value = 0 ; $('.resolved').hide();$('#showEntityView').dialog('option', 'height', 'auto')}"/>
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
					<input type="hidden" name="id" id ="databaseId" value="${databaseInstance?.id}" />
					<tds:hasPermission permission='EditAndDelete'>
					<span class="button"><input type="button" class="edit" value="Edit" onclick="editEntity('${redirectTo}','Database',${databaseInstance?.id})" /> </span>
					<g:if test="${redirectTo!='planningConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId" name="deleteId" class="save" value="Delete" onclick=" deleteAsset($('#databaseId').val(),'database')" /> </span>
					</g:else>
					</tds:hasPermission>
					<tds:hasPermission permission="CommentCrudView">	
						<a href="javascript:createIssue('${databaseInstance.assetName}','comment', ${databaseInstance.id});">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
						</a>
						<a href="javascript:createIssue('${databaseInstance.assetName}','', ${databaseInstance.id});">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Task
						</a>
					</tds:hasPermission>
				</g:form>
			</div>
		</td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	var prefVal = '${prefValue}'

	if(prefVal == 'FALSE'){
		$(".resolved").hide()
	} else{
		$(".resolved").show()
	}
</script>

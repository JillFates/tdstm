<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}','Storage', ${filesInstance?.id})"</tds:hasPermission>>
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;" class="${config.assetName}">${filesInstance.assetName}</td>
							<td class="label" nowrap="nowrap"><label for="description">Description</label></td>
							<td class="value ${config.description}" colspan="2">${filesInstance.description}</td>
							<td></td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
							<td class="valueNW ${config.moveBundle}">${filesInstance?.moveBundle} / ${dependencyBundleNumber}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">App Type</label></td>
							<td class="valueNW">${filesInstance.assetType == 'Files' ? 'Storage' : filesInstance.assetType}</td>
							<td class="label" nowrap="nowrap"><label for="lun">LUN</label></td>
							<td class="valueNW ${config.LUN}">${filesInstance.LUN}</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW ${config.supportType}">${filesInstance.supportType}</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW ${config.planStatus}">${filesInstance.planStatus}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="fileFormat">Format</label></td>
							<td class="valueNW ${config.fileFormat}">${filesInstance.fileFormat}</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW ${config.environment}">${filesInstance.environment}</td>
							<td class="label" nowrap="nowrap"><label for="fileSize">Size</label></td>
							<td class="valueNW ${config.fileSize}">${filesInstance.fileSize}&nbsp;${filesInstance.sizeUnit}</td>
							<td class="label">Validation</td>
							<td class="valueNW ${config.validation}">${filesInstance.validation}</td>
						</tr>
						<tr>
						</tr>
						<g:render template="../assetEntity/customShow" model="[assetEntity:filesInstance]"></g:render>
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
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support?.dataFlowFreq}</td>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
								${support?.asset?.assetType == 'Files' ? 'Storage' : support?.asset?.assetType}</td>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support?.asset?.assetName}</td>
							<g:if test="${support?.asset?.moveBundle!=filesInstance.moveBundle && support.status == 'Validated' }" >
							<td style="background-color: red"> ${support?.asset?.moveBundle}</td></g:if>
							<g:elseif test="${support?.asset?.moveBundle!=filesInstance.moveBundle }" >
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
								<b>${support?.asset?.moveBundle}</b>
							</td>
							</g:elseif>
							<g:else>
							<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">${support?.asset?.moveBundle}</td>
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
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">${dependent.dataFlowFreq}</td>
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
								${dependent.dependent?.assetType  == 'Files' ? 'Storage' : dependent.dependent?.assetType}</td>
							<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">${dependent.dependent?.assetName}</td>
							<g:if test="${dependent.dependent?.moveBundle!=filesInstance.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: red">${dependent.dependent?.moveBundle}</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=filesInstance.moveBundle }" >
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
					<td onclick="javascript:showComment(${commentList.id},'show')"  style="text-align: center;">${commentList.taskNumber ?:'c'}</td>
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
					<input type="hidden" name="id" id="filedeleteId" value="${filesInstance?.id}" />
					<tds:hasPermission permission='EditAndDelete'>
						<span class="button"><input type="button" class="edit"
							value="Edit" onclick="editEntity('${redirectTo}','Files', ${filesInstance?.id})" /> </span>
					<g:if test="${redirectTo!='planningConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#filedeleteId').val(),'files')" /> </span>
					</g:else>
					</tds:hasPermission>
					<tds:hasPermission permission="CommentCrudView">	
						<a href="javascript:createIssue('${filesInstance.assetName}','comment', ${filesInstance.id});">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
						</a>
						<a href="javascript:createIssue('${filesInstance.assetName}','', ${filesInstance.id});">
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
	
	$(document).ready(function() { 
		var prefVal = '${prefValue}'
		if(prefVal == 'FALSE'){
			$(".resolved").hide()
		} else{
			$(".resolved").show()
		}
				
		$(".showAllChecked").click(function(){
			 var selected = $(this).is(":checked") ? '1' : '0'
			 showTask(selected)
		})
	})
</script>

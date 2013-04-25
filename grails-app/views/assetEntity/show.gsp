<g:if test="${flash.message}">
<script type="text/javascript">
alert("${flash.message}")
</script>
<% flash.message = null %>
</g:if>
<script type="text/javascript">
$(document).ready(function() { 
	var assetType = "${assetEntity.assetType}"
	if(assetType=='Blade'){
		$(".bladeLabel").show()
		$(".rackLabel").hide()
		$(".vmLabel").hide()
	} else if(assetType=='VM') {
		$(".bladeLabel").hide()
		$(".rackLabel").hide()
		$(".vmLabel").show()
	} else {
		$(".bladeLabel").hide()
		$(".rackLabel").show()
		$(".vmLabel").hide()
	}
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
 	<g:form method="post">
 	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
			<tds:hasPermission permission='EditAndDelete'>
				<div class="dialog" ondblclick="editEntity('${redirectTo}','Server', ${assetEntity?.id})">
			</tds:hasPermission>
					<table>
						<tbody>
							<tr  class="prop">
								<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td colspan="3" style="font-weight:bold">${assetEntity.assetName}</td>
								<td>&nbsp</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td class="valueNW">${assetEntity.assetType}</td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label></td>
								<td class="valueNW">${assetEntity.priority}</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocation">Location</label></td>
								<td class="valueNW">${assetEntity.sourceLocation}</td>
								<td class="valueNW">${assetEntity.targetLocation}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="manufacturer">Manufacturer</label></td>
								<td class="valueNW"><a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a></td>
								<td class="label" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td class="valueNW">${assetEntity.ipAddress}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRoom">Room</label></td>
								<td class="valueNW">${assetEntity.roomSource?.roomName}</td>
								<td class="valueNW">${assetEntity.roomTarget?.roomName}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="model">Model</label></td>
								<td class="valueNW"><a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
								  <g:if test="${assetEntity.model?.modelStatus=='new' && assetEntity.model?.usize==1}"> <span style="color: red;"><b>?</b></span></g:if>
								</td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td class="valueNW">${assetEntity.os}</td>

								<td class="label rackLabel"  nowrap="nowrap" id="rackId"><label for="sourceRackId">Rack/Cab</label></td>
								<td class="label bladeLabel" nowrap="nowrap" id="bladeId" style="display: none"><label for="sourceBladeChassisId">Blade</label></td>
								<td class="label vmLabel" style="display: none" class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label></td>

								<td class=" rackLabel">${assetEntity.rackSource?.tag}</td>
								<td class=" rackLabel">${assetEntity.rackTarget?.tag}</td>

								<td class=" bladeLabel" style="display: none">${assetEntity.sourceBladeChassis}</td>
								<td class=" bladeLabel" style="display: none" >${assetEntity.targetBladeChassis}</td>

								<td class="vmLabel" style="display: none">${assetEntity.virtualHost}</td>
								<td class="vmLabel" style="display: none"></td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td class="valueNW">${assetEntity.shortName}</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td class="valueNW">${assetEntity.supportType}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Position</label></td>
								<td class="valueNW">${assetEntity.sourceRackPosition}</td>
								<td class="valueNW">${assetEntity.targetRackPosition}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="serialNumber">S/N</label></td>
								<td class="valueNW">${assetEntity.serialNumber}</td>
								<td class="label"><label for="retireDate">Retire Date:</label></td>
								<td class="valueNW"><tds:convertDate date="${assetEntity?.retireDate}"
							  		timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td class="valueNW" colspan="2">${assetEntity.moveBundle}${dependencyBundleNumber?' / ' : ''}${dependencyBundleNumber}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td class="valueNW">${assetEntity.assetTag}</td>
								<td class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td class="valueNW"><tds:convertDate date="${assetEntity?.maintExpDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							        </td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td class="valueNW" colspan="2">${assetEntity.planStatus}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								<td class="valueNW">${assetEntity.railType}</td>
								<td class="label" nowrap="nowrap"><label for="truck">Truck/Cart/Shelf</label></td>
								<td class="valueNW">${assetEntity.truck}/${assetEntity.cart}${assetEntity.shelf? ' / '+assetEntity.shelf : ''}</td>
								<td class="label">Validation</td>
								<td class="valueNW">${assetEntity.validation}</td>
								<td>&nbsp;</td>
							</tr>
							<g:render template="customShow" ></g:render>
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
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.dataFlowFreq}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetType == 'Files' ? 'Storage' : support?.asset?.assetType}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetName}
								</td>
								 <g:if test="${support?.asset?.moveBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
									<td style="background-color: red">
										${support?.asset?.moveBundle}
									</td>
								 </g:if>
								 <g:elseif test="${support?.asset?.moveBundle!=assetEntity.moveBundle }" >
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
								 <g:if test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: red">
									${dependent.dependent?.moveBundle}
									</td>
								 </g:if>
								 <g:elseif test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle }" >
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
					<input type="checkbox" name="showAll" id="showAll" class="showAllChecked" ${prefValue && prefValue == 'TRUE' ?  'checked="checked"'  : ''} />
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
			<tds:hasPermission permission='EditAndDelete'>
				<div class="buttons">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<input type="hidden" name="id" id="assetsId" value="${assetEntity?.id}" />
					<span class="button">
					<input type="button" class="edit" value="Edit" onclick="editEntity('${redirectTo}','Server', ${assetEntity?.id})" /> </span>
					<input type ="hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<g:if test="${redirectTo!='planningConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId" name="deleteId" class="save" value="Delete" onclick=" deleteAsset($('#assetsId').val(),'server')" /> </span>
					</g:else>
					<tds:hasPermission permission="CommentCrudView">	
						<a href="javascript:createIssue('${assetEntity.assetName}','comment', ${assetEntity.id});">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
						</a>
						<a href="javascript:createIssue('${assetEntity.assetName}','',${assetEntity.id});">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Task
						</a>
					</tds:hasPermission>
				</div>
			</tds:hasPermission>
			</td>
		</tr>
	</table>
</g:form>

<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}','Storage', ${filesInstance?.id})"</tds:hasPermission>>
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;">${filesInstance.assetName}</td>

							<td class="label" nowrap="nowrap"><label for="description">Description</label></td>
							<td colspan="3">${filesInstance.description}</td>

						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">App
									Type</label></td>
							<td>${filesInstance.assetType == 'Files' ? 'Storage' : filesInstance.assetType}</td>
							
							<td class="label" nowrap="nowrap"><label for="lun">LUN
							</label></td>
							<td>${filesInstance.LUN}</td>
							

							<td class="label" nowrap="nowrap"><label for="supportType">Support
							</label></td>
							<td>${filesInstance.supportType}</td>
						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="fileFormat">
									Format </label></td>
							<td>${filesInstance.fileFormat}</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment
							</label></td>
							<td>${filesInstance.environment}</td>

							<td class="label" nowrap="nowrap"><label for="fileSize">Size
							</label></td>
							<td>${filesInstance.fileSize}&nbsp;${filesInstance.sizeUnit}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
							</td>
							<td>${filesInstance.moveBundle} / ${dependencyBundleNumber}</td>
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom1)?.length()>= 4 && (filesInstance.custom1)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom1}','help');" style="color:#00E">${filesInstance.project.custom1 !=null ? filesInstance.project.custom1 :'Custom1'}</a>
							</g:if>
							<g:else>
								<label for="custom1">${filesInstance.project.custom1 ?:'Custom1'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom1 }</td>
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance?.custom2)?.length()>= 4 &&(filesInstance.custom2)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom2}','help');" style="color:#00E">${filesInstance.project.custom2 !=null ? filesInstance.project.custom2 :'Custom2'}</a>
							</g:if>
							<g:else>
								<label for="custom2">${filesInstance.project.custom2 ?:'Custom2'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom2}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom3)?.length()>= 4 &&(filesInstance.custom3)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom3}','help');" style="color:#00E">${filesInstance.project.custom3 !=null ? filesInstance.project.custom3 :'Custom3'}</a>
							</g:if>
							<g:else>
								<label for="custom3">${filesInstance.project.custom3 ?:'Custom3'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom3}</td>
							
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom4)?.length()>= 4 &&(filesInstance.custom4)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom4}','help');" style="color:#00E">${filesInstance.project.custom4 !=null ? filesInstance.project.custom4 :'Custom4'}</a>
							</g:if>
							<g:else>
								<label for="custom4">${filesInstance.project.custom4 ?:'Custom4'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom4}</td>
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom5)?.length()>= 4 &&(filesInstance.custom5)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom5}','help');" style="color:#00E">${filesInstance.project.custom5 !=null ? filesInstance.project.custom5 :'Custom5'}</a>
							</g:if>
							<g:else>
								<label for="custom5">${filesInstance.project.custom5 ?:'Custom5'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom5}</td>
						</tr>
					    <tr class="prop">
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom6)?.length()>= 4 &&(filesInstance.custom6)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom6}','help');" style="color:#00E">${filesInstance.project.custom6 !=null ? filesInstance.project.custom6 :'Custom6'}</a>
							</g:if>
							<g:else>
								 <label for="custom6">${filesInstance.project.custom6 ?:'Custom6'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom6}</td>
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom7)?.length()>= 4 &&(filesInstance.custom7)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom7}','help');" style="color:#00E">${filesInstance.project.custom7 !=null ? filesInstance.project.custom7 :'Custom7'}</a>
							</g:if>
							<g:else>
								<label for="custom7">${filesInstance.project.custom7 ?:'Custom7'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom7}</td>
							<td class="label" nowrap="nowrap">
							<g:if test="${(filesInstance.custom8)?.length()>= 4 &&(filesInstance.custom8)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${filesInstance.custom8}','help');" style="color:#00E">${filesInstance.project.custom8 !=null ? filesInstance.project.custom8 :'Custom8'}</a>
							</g:if>
							<g:else>
								 <label for="custom7">${filesInstance.project.custom8 ?:'Custom8'}</label>
							</g:else>
							</td>
							<td>${filesInstance?.custom8}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label>
							</td>
							<td>${filesInstance.planStatus}</td>
						</tr>
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
							<g:if test="${support?.asset?.moveBundle!=filesInstance.moveBundle && support.status == 'Validated' }" >
							<td style="background-color: red"> ${support?.asset?.moveBundle}</td></g:if>
						    <g:elseif test="${support?.asset?.moveBundle!=filesInstance.moveBundle }" >
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
									<td style="background-color: red">
										     ${dependent.dependent?.moveBundle}
									</td>
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
					<th nowrap>Action</th>
					<th nowrap>Comment</th>
					<th nowrap>Comment Type</th>
					<th nowrap>Resolved</th>
					<th nowrap>Category</th>  
				</tr>
				</thead>
				<tbody id="listCommentsTbodyIds">
				<g:each status="i" in="${assetCommentList}"  var="commentList">
				<tr style="cursor: pointer;">
					<td ><a href ="javascript:showComment(${commentList.id},'edit')" ><img src="${resource(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.comment}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.commentType}</td>
					<td ><g:if test ="${commentList.commentType =='issue' && commentList.isResolved == 1}"><g:checkBox name="myCheckbox" value="${true}" disabled="true"/></g:if><g:else>&nbsp</g:else></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.category}</td>
				</tr>
				</g:each>
				</tbody>
				</table>
			</td>
			</div>
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
					   <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#filedeleteId').val(),'files')" value="Delete" /> </span>
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
</script>

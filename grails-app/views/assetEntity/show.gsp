<g:if test="${flash.message}">
<script type="text/javascript">
alert("${flash.message}")
</script>
<% flash.message = null %>
</g:if>
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
								<td style="font-weight:bold">${assetEntity.assetName}</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
								<td class="label" nowrap="nowrap">
								   <g:if test="${(assetEntity.custom1)?.length()>= 4 && (assetEntity.custom1)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom1}','help');" style="color:#00E">${assetEntity.project.custom1!=null ? assetEntity.project.custom1 : 'Custom1'}</a>
								    </g:if>
								    <g:else>
								         ${assetEntity.project.custom1 ?:'Custom1'}
							        </g:else>
							    </td>
							    <td width="60">${assetEntity.custom1 }</td>
							    
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td >${assetEntity.assetType}</td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label></td>
								<td >${assetEntity.priority}</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocation">Location</label></td>
								<td>${assetEntity.sourceLocation}</td>
								<td>${assetEntity.targetLocation}</td>
								<td class="label" nowrap="nowrap">
								<g:if test="${(assetEntity.custom2)?.length()>= 4 && (assetEntity.custom2)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom2}','help');" style="color:#00E">${assetEntity.project.custom2!=null ? assetEntity.project.custom2 : 'Custom2'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom2 ?:'Custom2'}
							    </g:else>
							    </td>
							    <td>${assetEntity.custom2}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="manufacturer">Manufacturer</label></td>
								<td ><a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a></td>
								<td class="label" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td >${assetEntity.ipAddress}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRoom">Room</label></td>
								<td>${assetEntity.roomSource?.roomName}</td>
								<td>${assetEntity.roomTarget?.roomName}</td>
								<td class="label" nowrap="nowrap">
								<g:if test="${(assetEntity.custom3)?.length()>= 4 && (assetEntity.custom3)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom3}','help');" style="color:#00E">${assetEntity.project.custom3!=null ? assetEntity.project.custom3 : 'Custom3'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom3 ?:'Custom3'}
							    </g:else>
							    </td>
							    <td>${assetEntity.custom3}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="model">Model</label></td>
								<td><a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
								  <g:if test="${assetEntity.model?.modelStatus=='new' && assetEntity.model?.usize==1}"> <span style="color: red;"><b>?</b></span></g:if>
								</td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td >${assetEntity.os}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Rack/Cab</label></td>
								<td>${assetEntity.rackSource?.tag}</td>
								<td>${assetEntity.rackTarget?.tag}</td>
								<td class="label" nowrap="nowrap">
								<g:if test="${(assetEntity.custom4)?.length()>= 4 && (assetEntity.custom4)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom4}','help');" style="color:#00E">${assetEntity.project.custom4!=null ? assetEntity.project.custom4 : 'Custom4'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom4 ?:'Custom4'}
							    </g:else>
							    </td>
							    <td>${assetEntity.custom4}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td >${assetEntity.shortName}</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td >${assetEntity.supportType}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Position</label></td>
								<td>${assetEntity.sourceRackPosition}</td>
								<td>${assetEntity.targetRackPosition}</td>
								<td class="label" nowrap="nowrap">
								<g:if test="${(assetEntity.custom5)?.length()>= 4 && (assetEntity.custom5)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom5}','help');" style="color:#00E">${assetEntity.project.custom5!=null ? assetEntity.project.custom5 : 'Custom5'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom5 ?:'Custom5'}
							    </g:else>
							    </td>
							    <td>${assetEntity.custom5}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="serialNumber">S/N</label></td>
								<td >${assetEntity.serialNumber}</td>
								<td class="label"><label for="retireDate">Retire Date:</label></td>
								<td><tds:convertDate date="${assetEntity?.retireDate}"
							  		timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceBladeChassis">Blade</label></td>
								<td>${assetEntity.sourceBladeChassis}</td>
								<td>${assetEntity.targetBladeChassis}</td>
								<td class="label" nowrap="nowrap">
								<g:if test="${(assetEntity.custom6)?.length()>= 4 && (assetEntity.custom6)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom6}','help');" style="color:#00E">${assetEntity.project.custom6!=null ? assetEntity.project.custom6 : 'Custom6'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom6 ?:'Custom6'}
							    </g:else>
							    </td>
							    <td>${assetEntity.custom6}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td >${assetEntity.assetTag}</td>
								<td class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td><tds:convertDate date="${assetEntity?.maintExpDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							        </td>
								<td class="label" nowrap="nowrap"><label for="sourceBladePosition">Blade Position</label></td>
								<td>${assetEntity.sourceBladePosition}</td>
								<td>${assetEntity.targetBladePosition}</td>
								<td class="label" nowrap="nowrap">
									<g:if test="${(assetEntity.custom7)?.length()>= 4 && (assetEntity.custom7)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom7}','help');" style="color:#00E">${assetEntity.project.custom7!=null ? assetEntity.project.custom7 : 'Custom7'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom7 ?:'Custom7'}
							    </g:else>
								</td>
								<td>${assetEntity.custom7}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								<td >${assetEntity.railType}</td>
								<td class="label">Validation</td>
								<td>${assetEntity.validation}</td>
								<td class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label></td>
								<td>${assetEntity.virtualHost}</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap">
									<g:if test="${(assetEntity.custom8)?.length()>= 4 && (assetEntity.custom8)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntity.custom8}','help');" style="color:#00E">${assetEntity.project.custom8!=null ? assetEntity.project.custom8 : 'Custom8'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntity.project.custom8 ?:'Custom8'}
							    </g:else>
								</td>
								<td>${assetEntity.custom8}</td>
							</tr>
							<tr class="prop">
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td >${assetEntity.moveBundle}${dependencyBundleNumber?' / ' : ''}${dependencyBundleNumber}</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="truck">Truck</label></td>
								<td >${assetEntity.truck}</td>
							</tr>
							<tr class="prop">
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td >${assetEntity.planStatus}</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="cart">Cart/Shelf</label></td>
								<td >${assetEntity.cart}${assetEntity.shelf? ' / ' : ''}${assetEntity.shelf}</td>							
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
					<td><a href ="javascript:showComment(${commentList.id},'edit')" ><img src="${resource(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.comment}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.commentType}</td>
					<td ><g:if test ="${commentList.commentType =='issue' && commentList.isResolved == 1}"><g:checkBox name="myCheckbox" value="${true}" disabled="true"/></g:if><g:else>&nbsp</g:else></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.category}</td>
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
					   <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#assetsId').val(),'server')" value="Delete" /> </span>
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

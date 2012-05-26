 	<g:form method="post">
 	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
			<tds:hasPermission permission='EditAndDelete'>
				<div class="dialog" ondblclick="editEntity('${redirectTo}','Server', ${assetEntityInstance?.id})">
			</tds:hasPermission>
					<table>
						<tbody>
							
							<tr  class="prop">
								<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td style="font-weight:bold">${assetEntityInstance.assetName}</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
								<td colspan="2" class="label" nowrap="nowrap">
								   <g:if test="${(assetEntityInstance.custom1)?.length()>= 4 && (assetEntityInstance.custom1)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom1}','help');" style="color:#00E">${assetEntityInstance.project.custom1!=null ? assetEntityInstance.project.custom1 : 'Custom1'}</a>
								    </g:if>
								    <g:else>
								         ${assetEntityInstance.project.custom1 ?:'Custom1'}
							        </g:else>
							    </td>
							    <td>${assetEntityInstance.custom1 }</td>
							    
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td >${assetEntityInstance.assetType}</td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label></td>
								<td >${assetEntityInstance.priority}</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocation">Location</label></td>
								<td>${assetEntityInstance.sourceLocation}</td>
								<td>${assetEntityInstance.targetLocation}</td>
								<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(assetEntityInstance.custom2)?.length()>= 4 && (assetEntityInstance.custom2)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom2}','help');" style="color:#00E">${assetEntityInstance.project.custom2!=null ? assetEntityInstance.project.custom2 : 'Custom2'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom2 ?:'Custom2'}
							    </g:else>
							    </td>
							    <td>${assetEntityInstance.custom2}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="manufacturer">Manufacturer</label></td>
								<td ><a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>${assetEntityInstance.manufacturer}</a></td>
								<td class="label" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td >${assetEntityInstance.ipAddress}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRoom">Room</label></td>
								<td>${assetEntityInstance.roomSource?.roomName}</td>
								<td>${assetEntityInstance.roomTarget?.roomName}</td>
								<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(assetEntityInstance.custom3)?.length()>= 4 && (assetEntityInstance.custom3)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom3}','help');" style="color:#00E">${assetEntityInstance.project.custom3!=null ? assetEntityInstance.project.custom3 : 'Custom3'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom3 ?:'Custom3'}
							    </g:else>
							    </td>
							    <td>${assetEntityInstance.custom3}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="model">Model</label></td>
								<td><a href='javascript:showModel(${assetEntityInstance.model?.id})' style='color:#00E'>${assetEntityInstance.model}</a></td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td >${assetEntityInstance.os}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Rack/Cab</label></td>
								<td>${assetEntityInstance.rackSource?.tag}</td>
								<td>${assetEntityInstance.rackTarget?.tag}</td>
								<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(assetEntityInstance.custom4)?.length()>= 4 && (assetEntityInstance.custom4)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom4}','help');" style="color:#00E">${assetEntityInstance.project.custom4!=null ? assetEntityInstance.project.custom4 : 'Custom4'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom4 ?:'Custom4'}
							    </g:else>
							    </td>
							    <td>${assetEntityInstance.custom4}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td >${assetEntityInstance.shortName}</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td >${assetEntityInstance.supportType}</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Position</label></td>
								<td>${assetEntityInstance.sourceRackPosition}</td>
								<td>${assetEntityInstance.targetRackPosition}</td>
								<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(assetEntityInstance.custom5)?.length()>= 4 && (assetEntityInstance.custom5)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom5}','help');" style="color:#00E">${assetEntityInstance.project.custom5!=null ? assetEntityInstance.project.custom5 : 'Custom5'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom5 ?:'Custom5'}
							    </g:else>
							    </td>
							    <td>${assetEntityInstance.custom5}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="serialNumber">S/N</label></td>
								<td >${assetEntityInstance.serialNumber}</td>
								<td class="label"><label for="retireDate">Retire Date:</label></td>
								<td><tds:convertDate date="${assetEntityInstance?.retireDate}"
							  		timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceBladeChassis">Blade</label></td>
								<td>${assetEntityInstance.sourceBladeChassis}</td>
								<td>${assetEntityInstance.targetBladeChassis}</td>
								<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(assetEntityInstance.custom6)?.length()>= 4 && (assetEntityInstance.custom6)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom6}','help');" style="color:#00E">${assetEntityInstance.project.custom6!=null ? assetEntityInstance.project.custom6 : 'Custom6'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom6 ?:'Custom6'}
							    </g:else>
							    </td>
							    <td>${assetEntityInstance.custom6}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td >${assetEntityInstance.assetTag}</td>
								<td class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td><tds:convertDate date="${assetEntityInstance?.maintExpDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							        </td>
								<td class="label" nowrap="nowrap"><label for="sourceBladePosition">Blade Position</label></td>
								<td>${assetEntityInstance.sourceBladePosition}</td>
								<td>${assetEntityInstance.targetBladePosition}</td>
								<td colspan="2" class="label" nowrap="nowrap">
									<g:if test="${(assetEntityInstance.custom7)?.length()>= 4 && (assetEntityInstance.custom7)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom7}','help');" style="color:#00E">${assetEntityInstance.project.custom7!=null ? assetEntityInstance.project.custom7 : 'Custom7'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom7 ?:'Custom7'}
							    </g:else>
								</td>
								<td>${assetEntityInstance.custom7}</td>
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								<td >${assetEntityInstance.railType}</td>
								<td></td>
								<td></td>
								<td class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label></td>
								<td>${assetEntityInstance.virtualHost}</td>
								<td>&nbsp</td>
								<td colspan="2" class="label" nowrap="nowrap">
									<g:if test="${(assetEntityInstance.custom8)?.length()>= 4 && (assetEntityInstance.custom8)?.substring(0, 4)=='http'}">
										<a href="javascript:window.open('${assetEntityInstance.custom8}','help');" style="color:#00E">${assetEntityInstance.project.custom8!=null ? assetEntityInstance.project.custom8 : 'Custom8'}</a>
							    </g:if>
							    <g:else>
								   ${assetEntityInstance.project.custom8 ?:'Custom8'}
							    </g:else>
								</td>
								<td>${assetEntityInstance.custom8}</td>
							</tr>
							<tr class="prop">
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td >${assetEntityInstance.moveBundle} / ${dependencyBundlenumber}</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="truck">Truck</label></td>
								<td >${assetEntityInstance.truck}</td>
							</tr>
							<tr class="prop">
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td >${assetEntityInstance.planStatus}</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="cart">Cart/Shelf</label></td>
								<td >${assetEntityInstance.cart}/${assetEntityInstance.shelf}</td>							
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
							<th>Entity Type</th>
							<th>Name</th>
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
									${support?.asset?.assetType}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetName}
								</td>
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
							<th>Entity Type</th>
							<th>Name</th>
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
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dependent?.assetName}
								</td>
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
					<th nowrap>Must Verify</th>
					<th nowrap>Category</th>  
					<th nowrap>Comment Code</th>                     
				</tr>
				</thead>
				<tbody id="listCommentsTbodyIds">
				<g:each status="i" in="${assetCommentList}"  var="commentList">
				<tr style="cursor: pointer;">
					<td><a href ="javascript:showComment(${commentList.id},'edit')" ><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.comment}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.commentType}</td>
					<td ><g:if test ="${commentList.commentType =='issue' && commentList.isResolved == 1}"><g:checkBox name="myCheckbox" value="${true}" disabled="true"/></g:if><g:else>&nbsp</g:else></td>
					<td ><g:if test ="${commentList.mustVerify == 1}"></g:if><g:else><g:checkBox name="myVerifyBox" value="${true}" disabled="true"/></g:else></td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.category}</td>
					<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.commentCode}</td>
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
					<input type="hidden" name="id" id="assetsId" value="${assetEntityInstance?.id}" />
					<span class="button">
					<input type="button" class="edit" value="Edit" onclick="editEntity('${redirectTo}','Server', ${assetEntityInstance?.id})" /> </span>
					<input type ="hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<g:if test="${redirectTo!='planningConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#assetsId').val(),'server')" value="Delete" /> </span>
					</g:else>
					
				<g:if test="${assetComment == 'issue'}">
						<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
							<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="=0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</g:remoteLink>
				</g:if>
				<g:elseif test="${assetComment == 'comment'}">
					<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
						<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
					</g:remoteLink>
				</g:elseif>
			    <g:else>
					<a href="javascript:createNewAssetComment(${assetEntityInstance.id});">
						<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
					</a>
				</g:else>
				</div>
			</tds:hasPermission>
			</td>
		</tr>
	</table>
</g:form>

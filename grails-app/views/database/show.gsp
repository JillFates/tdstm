	<table style="border: 0">
	<tr>
		<td colspan="2">
		
			<div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}','Database',${databaseInstance?.id})" </tds:hasPermission>>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
			    </g:if>
				<table>
					<tbody>
						<tr class="prop">
							<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td class="valueNW ${config.assetName}" style="font-weight:bold;">${databaseInstance?.assetName}</td>
							<td class="label ${config.description}" nowrap="nowrap">Description</td>
							<td class="valueNW ${config.description}" colspan="5">${databaseInstance.description}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td class="valueNW">${databaseInstance?.assetType}</td>
							<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW ${config.supportType}">${databaseInstance?.supportType}</td>
							<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW ${config.environment}" colspan="3">${databaseInstance?.environment}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.dbFormat}" nowrap="nowrap"><label for="dbFormat">Format</label></td>
							<td class="valueNW ${config.dbFormat}">${databaseInstance?.dbFormat}</td>
							<td class="label ${config.retireDate}" nowrap="nowrap">Retire</td>
							<td class="valueNW ${config.retireDate}"><tds:convertDate date="${databaseInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle / Dep. Group</label></td>
							<td class="valueNW ${config.moveBundle}" colspan="3">${databaseInstance?.moveBundle} / ${dependencyBundleNumber}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.dbSize}" nowrap="nowrap"><label for="dbSize">Size</label></td>
							<td class="valueNW ${config.dbSize}">${databaseInstance?.dbSize}</td>
							<td class="label ${config.maintExpDate}" nowrap="nowrap">Maint Exp.</td>
							<td class="valueNW ${config.maintExpDate}"><tds:convertDate date="${databaseInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW ${config.planStatus}" colspan="3">${databaseInstance?.planStatus}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label>Version</label></td>
							<td></td>
							<td class="label" nowrap="nowrap"><label>SME1</label></td>
							<td></td>
							<td class="label ${config.validation}">Validation</td>
							<td class="valueNW ${config.validation}" colspan="3">${databaseInstance.validation}</td>
						</tr>
						<tr>
							<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
							<td class="${config.externalRefId}">${databaseInstance.externalRefId}</td>
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
						<th>Class</th>
						<th>Name</th>
						<th>Bundle</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${supportAssets}" var="support" status="i">
						<tr onclick="getEntityDetails('${redirectTo}','${support?.asset?.assetType}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
							<td class="dep-${support.status}">
								${support?.dataFlowFreq}
							</td>
							<td class="dep-${support.status}">
								${support?.asset?.assetType == 'Files' ? 'Storage' : support?.asset?.assetType}
							</td>
							<td class="dep-${support.status}">
								${support?.asset?.assetName}
							</td>
							<g:if test="${support?.asset?.moveBundle!=databaseInstance.moveBundle && support.status == 'Validated' }" >
								<td style="background-color: lightpink">
									${support?.asset?.moveBundle} !
								</td>
							</g:if>
						    <g:elseif test="${support?.asset?.moveBundle!=databaseInstance.moveBundle }" >
								<td class="dep-${support.status}">
									<b>${support?.asset?.moveBundle} !</b>
								</td>
							</g:elseif>
							<g:else>
								<td class="dep-${support.status}">
									${support?.asset?.moveBundle}
								</td>
							</g:else>
							<td class="dep-${support.status}">
								${support.type}
							</td>
							<td class="dep-${support.status}">
								${support.status}
							</td>
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
						<th>Class</th>
						<th>Name</th>
						<th>Bundle</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${dependentAssets}" var="dependent" status="i">
						<tr onclick="getEntityDetails('${redirectTo}','${dependent.dependent?.assetType}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
							<td class="dep-${dependent.status}">
								${dependent.dataFlowFreq}</td>
							<td class="dep-${dependent.status}">
								${dependent.dependent?.assetType == 'Files' ? 'Storage' : dependent.dependent?.assetType}
							</td>
							<td class="dep-${dependent.status}">
								${dependent.dependent?.assetName}
							</td>
							<g:if test="${dependent.dependent?.moveBundle!=databaseInstance.moveBundle && dependent.status == 'Validated' }" >
								<td style="background-color: lightpink">
									${dependent.dependent?.moveBundle} !
								</td>
							</g:if>
							<g:elseif test="${dependent.dependent?.moveBundle!=databaseInstance.moveBundle }" >
								<td class="dep-${dependent.status}">
									<b>${dependent.dependent?.moveBundle} !</b>
								</td>
							</g:elseif>
							<g:else>
								<td class="dep-${dependent.status}">
									${dependent.dependent?.moveBundle}
								</td>
							</g:else>
								<td class="dep-${dependent.status}">
								${dependent.type}
							</td>
							<td class="dep-${dependent.status}">
								${dependent.status}
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	</tr>
	<tr id="commentListId">
		<g:render template="../assetEntity/commentList" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id ="databaseId" value="${databaseInstance?.id}" />
					<tds:hasPermission permission='EditAndDelete'>
					<span class="button"><input type="button" class="edit" value="Edit" onclick="editEntity('${redirectTo}','Database',${databaseInstance?.id})" /> </span>
					<g:if test="${redirectTo!='dependencyConsole'}">
					   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					   <span class="button"><input id="deleteId" name="deleteId" class="delete" value="Delete" onclick=" deleteAsset($('#databaseId').val(),'database')" /> </span>
					</g:else>
					</tds:hasPermission>
					<tds:hasPermission permission="CommentCrudView">	
						<a href="javascript:createIssue('${databaseInstance.assetName}','comment', ${databaseInstance.id},'update');">
							<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
						</a>
						<a href="javascript:createIssue('${databaseInstance.assetName}','', ${databaseInstance.id}, 'update');">
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
		changeDocTitle('${databaseInstance.assetName}');
	})
</script>

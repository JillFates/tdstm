	<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
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
							<td class="label ${config.size}" nowrap="nowrap"><label for="size">Size/Scale</label></td>
							<td class="valueNW ${config.size}">${databaseInstance?.size} &nbsp;&nbsp; ${databaseInstance.scale?.value()}</td>
							<td class="label ${config.maintExpDate}" nowrap="nowrap">Maint Exp.</td>
							<td class="valueNW ${config.maintExpDate}"><tds:convertDate date="${databaseInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW ${config.planStatus}" colspan="3">${databaseInstance?.planStatus}</td>
						</tr>
						<tr>
							<td class="label ${config.rateOfChange}" nowrap="nowrap"><label for="rateOfChange">Rate of Change (%)</label></td>
							<td class="valueNW ${config?.rateOfChange}">${databaseInstance?.rateOfChange}</td>
							<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
							<td class="${config.externalRefId}">${databaseInstance.externalRefId}</td>
							<td class="label ${config.validation}">Validation</td>
							<td class="valueNW ${config.validation}" colspan="3">${databaseInstance.validation}</td>
						</tr>
						<g:render template="../assetEntity/customShow" model="[assetEntity:databaseInstance, 'project':project]"></g:render>
					</tbody>
				</table>
			</div></td>
	</tr>
	<tr id="deps">
		<g:render template="../assetEntity/dependentShow" model="[assetEntity:databaseInstance]" ></g:render>
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

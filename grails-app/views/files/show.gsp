<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='AssetEdit'> ondblclick="editEntity('${redirectTo}','Storage', ${filesInstance?.id})"</tds:hasPermission>>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<table>
					<tbody>
						<tr class="prop">
							<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;" class="${config.assetName}">${filesInstance.assetName}</td>
							<td class="label ${config.description}" nowrap="nowrap"><label for="description">Description</label></td>
							<td class="value ${config.description}">${filesInstance.description}</td>
							<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle / Dep. Group</label></td>
							<td class="valueNW ${config.moveBundle}">${filesInstance?.moveBundle} / ${dependencyBundleNumber}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">App Type</label></td>
							<td class="valueNW">${filesInstance.assetType == 'Files' ? 'Storage' : filesInstance.assetType}</td>
							<td class="label" nowrap="nowrap"><label for="lun">LUN</label></td>
							<td class="valueNW">${filesInstance.LUN}</td>
							<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW ${config.supportType}">${filesInstance.supportType}</td>
							<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW ${config.planStatus}">${filesInstance.planStatus}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.fileFormat}" nowrap="nowrap"><label for="fileFormat">Format</label></td>
							<td class="valueNW ${config.fileFormat}">${filesInstance.fileFormat}</td>
							<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW ${config.environment}">${filesInstance.environment}</td>
							<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
							<td class="${config.externalRefId}">${filesInstance.externalRefId}</td>
							<td class="label ${config.validation}"><label for="validation">Validation</label></td>
							<td class="valueNW ${config.validation}">${filesInstance.validation}</td>
						</tr>
						<tr>
							<td class="label ${config.size}" nowrap="nowrap"><label for="size">Size/Scale</label></td>
							<td class="valueNW ${config.size}">${filesInstance.size}&nbsp;&nbsp;${filesInstance.scale?.value()}</td>
							<td class="label ${config.rateOfChange}" nowrap="nowrap"><label for="rateOfChange">Rate of Change (%)</label></td>
							<td class="valueNW ${config?.rateOfChange}">${filesInstance?.rateOfChange}</td>
						</tr>
						<g:render template="../assetEntity/customShow" model="[assetEntity:filesInstance]"></g:render>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr id="deps">
		<g:render template="../assetEntity/dependentShow" model="[assetEntity:filesInstance]" ></g:render>
	</tr>
		<tr id="commentListId">
			<g:render template="../assetEntity/commentList" ></g:render>
		</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id="filedeleteId" value="${filesInstance?.id}" />
					<g:render template="../assetEntity/showButtons" 
						model="[assetEntity:filesInstance, redirectTo:redirectTo,type:'Files', forWhom:'files']" />
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
		changeDocTitle('${filesInstance.assetName}');
	})
</script>

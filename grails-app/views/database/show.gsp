<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %> 

<table style="border: 0">
	<tr>
		<td colspan="2">
		
			<div class="dialog" <tds:hasPermission permission="${Permission.AssetEdit}"> ondblclick="EntityCrud.showAssetEditView('${databaseInstance.assetClass}',${databaseInstance?.id})" </tds:hasPermission>>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
			    </g:if>
				<table>
					<tbody>
						<tr class="prop">
							<td class="label ${config.assetName} ${highlightMap.assetName?:''}" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td colspan="2" class="valueNW ${config.assetName}" style="max-width: 400px; font-weight:bold;">${databaseInstance?.assetName}</td>
							<td class="label ${config.description} ${highlightMap.description?:''}" nowrap="nowrap"><label for="description">Description</label></td>
							<td colspan="2" style="max-width: 400px;" class="valueNW ${config.description}" >${databaseInstance.description}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td class="valueNW">${databaseInstance?.assetType}</td>
							<td class="label ${config.supportType} ${highlightMap.supportType?:''}" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td class="valueNW ${config.supportType}">${databaseInstance?.supportType}</td>
							<td class="label ${config.environment} ${highlightMap.environment?:''}" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td class="valueNW ${config.environment}" colspan="3">${databaseInstance?.environment}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.dbFormat} ${highlightMap.dbFormat?:''}" nowrap="nowrap"><label for="dbFormat">Format</label></td>
							<td class="valueNW ${config.dbFormat}">${databaseInstance?.dbFormat}</td>
							<td class="label ${config.retireDate} ${highlightMap.retireDate?:''}" nowrap="nowrap"><label for="retireDate">Retire</label></td>
							<td class="valueNW ${config.retireDate}"><tds:convertDate date="${databaseInstance?.retireDate}"  /></td>
							<td class="label ${config.moveBundle} ${highlightMap.moveBundle?:''}" nowrap="nowrap"><label for="moveBundle">Bundle : Dep. Group</label></td>
							<td class="valueNW ${config.moveBundle}" colspan="3">${databaseInstance?.moveBundle} : ${dependencyBundleNumber}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.size} ${highlightMap.size?:''}" nowrap="nowrap"><label for="size">Size/Scale</label></td>
							<td class="valueNW ${config.size}">${databaseInstance?.size} &nbsp;&nbsp; ${databaseInstance.scale?.value()}</td>
							<td class="label ${config.maintExpDate} ${highlightMap.maintExpDate?:''}" nowrap="nowrap"><label for="maintExpDate">Maint Exp.</label></td>
							<td class="valueNW ${config.maintExpDate}"><tds:convertDate date="${databaseInstance?.maintExpDate}"  /></td>
							<td class="label ${config.planStatus} ${highlightMap.planStatus?:''}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td class="valueNW ${config.planStatus}" colspan="3">${databaseInstance?.planStatus}</td>
						</tr>
						<tr>
							<td class="label ${config.rateOfChange} ${highlightMap.rateOfChange?:''}" nowrap="nowrap"><label for="rateOfChange">Rate of Change (%)</label></td>
							<td class="valueNW ${config?.rateOfChange}">${databaseInstance?.rateOfChange}</td>
							<td class="label ${config.externalRefId} ${highlightMap.externalRefId?:''}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
							<td class="${config.externalRefId}">${databaseInstance.externalRefId}</td>
							<td class="label ${config.validation} ${highlightMap.validation?:''}"><label for="validation">Validation</label></td>
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
		<g:render template="../assetEntity/commentList" model="['asset':databaseInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id ="databaseId" value="${databaseInstance?.id}" />
					<g:render template="../assetEntity/showButtons" model="[assetEntity:databaseInstance]"/>
				</g:form>
			</div>
		</td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	$(document).ready(function() { 
		changeDocTitle('${escapedName}');
	})
</script>

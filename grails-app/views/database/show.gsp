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
							<td class="label ${config.assetName} ${standardFieldSpecs.assetName.imp?:''}" nowrap="nowrap"><label for="assetName" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetName.tip}">${standardFieldSpecs.assetName.label}</label></td>
							<td colspan="2" class="valueNW ${config.assetName}" style="max-width: 400px; font-weight:bold;">${databaseInstance?.assetName}</td>
							<td class="label ${config.description} ${standardFieldSpecs.description.imp?:''}" nowrap="nowrap"><label for="description" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.description.tip}">${standardFieldSpecs.description.label}</label></td>
							<td colspan="2" style="max-width: 400px;" class="valueNW ${config.description}" >${databaseInstance.description}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip}">${standardFieldSpecs.assetType.label}</label></td>
							<td class="valueNW">${databaseInstance?.assetType}</td>
							<td class="label ${config.supportType} ${standardFieldSpecs.supportType.imp?:''}" nowrap="nowrap"><label for="supportType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.supportType.tip}">${standardFieldSpecs.supportType.label}</label></td>
							<td class="valueNW ${config.supportType}">${databaseInstance?.supportType}</td>
							<td class="label ${config.environment} ${standardFieldSpecs.environment.imp?:''}" nowrap="nowrap"><label for="environment" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">${standardFieldSpecs.environment.label}</label></td>
							<td class="valueNW ${config.environment}" colspan="3">${databaseInstance?.environment}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.dbFormat} ${standardFieldSpecs.dbFormat.imp?:''}" nowrap="nowrap"><label for="dbFormat" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.dbFormat.tip}">${standardFieldSpecs.dbFormat.label}</label></td>
							<td class="valueNW ${config.dbFormat}">${databaseInstance?.dbFormat}</td>
							<td class="label ${config.retireDate} ${standardFieldSpecs.retireDate.imp?:''}" nowrap="nowrap"><label for="retireDate" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip}">${standardFieldSpecs.retireDate.label}</label></td>
							<td class="valueNW ${config.retireDate}"><tds:convertDate date="${databaseInstance?.retireDate}"  /></td>
							<td class="label ${config.moveBundle} ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}">${standardFieldSpecs.moveBundle.label} : Dep. Group</label></td>
							<td class="valueNW ${config.moveBundle}" colspan="3">${databaseInstance?.moveBundle} : ${dependencyBundleNumber}</td>
						</tr>
						<tr class="prop">
							<td class="label ${config.size} ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</label></td>
							<td class="valueNW ${config.size}">${databaseInstance?.size} &nbsp;&nbsp; ${databaseInstance.scale?.value()}</td>
							<td class="label ${config.maintExpDate} ${standardFieldSpecs.maintExpDate.imp?:''}" nowrap="nowrap"><label for="maintExpDate" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip}">${standardFieldSpecs.maintExpDate.label}</label></td>
							<td class="valueNW ${config.maintExpDate}"><tds:convertDate date="${databaseInstance?.maintExpDate}"  /></td>
							<td class="label ${config.planStatus} ${standardFieldSpecs.planStatus.imp?:''}" nowrap="nowrap"><label for="planStatus" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.planStatus.tip}">${standardFieldSpecs.planStatus.label}</label></td>
							<td class="valueNW ${config.planStatus}" colspan="3">${databaseInstance?.planStatus}</td>
						</tr>
						<tr>
							<td class="label ${config.rateOfChange} ${standardFieldSpecs.rateOfChange.imp?:''}" nowrap="nowrap"><label for="rateOfChange" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.rateOfChange.tip}">${standardFieldSpecs.rateOfChange.label}</label></td>
							<td class="valueNW ${config?.rateOfChange}">${databaseInstance?.rateOfChange}</td>
							<td class="label ${config.externalRefId} ${standardFieldSpecs.externalRefId.imp?:''}" nowrap="nowrap"><label for="externalRefId" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.externalRefId.tip}">${standardFieldSpecs.externalRefId.label}</label></td>
							<td class="${config.externalRefId}">${databaseInstance.externalRefId}</td>
							<td class="label ${config.validation} ${standardFieldSpecs.validation.imp?:''}"><label for="validation" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.validation.tip}">${standardFieldSpecs.validation.label}</label></td>
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

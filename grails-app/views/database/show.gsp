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
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${databaseInstance?.assetName}"/>
							<td colspan="2" class="valueNW" style="max-width: 400px; font-weight:bold;"><span data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetName.tip}">${databaseInstance?.assetName}</span></td>
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${databaseInstance?.description}"/>
							<td colspan="2" style="max-width: 400px;" class="valueNW" ><span data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.description.tip}">${databaseInstance.description}</span></td>
						</tr>
						<tr class="prop">
							<tds:showLabelAndField field="${standardFieldSpecs.dbFormat}" value="${databaseInstance.dbFormat}"/>
							<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${databaseInstance.supportType}"/>
							<tds:showLabelAndField field="${standardFieldSpecs.environment}" value="${databaseInstance.environment}"/>
						</tr>
						<tr class="prop">
							<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</label></td>
							<td class="valueNW"><span data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${databaseInstance?.size} &nbsp;&nbsp; ${databaseInstance.scale?.value()}</span></td>
							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${databaseInstance?.retireDate}"/>
							<td class="valueNW"><span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.retireDate.tip)}"><tds:convertDate date="${databaseInstance?.retireDate}"/></span></td>
							<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}">${standardFieldSpecs.moveBundle.label} : Dep. Group</label></td>
							<td class="valueNW" colspan="3"><span data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}">${databaseInstance?.moveBundle} : ${dependencyBundleNumber}</span></td>
						</tr>
						<tr class="prop">
							<tds:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${databaseInstance.rateOfChange}"/>
							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${databaseInstance?.maintExpDate}"/>
							<td class="valueNW"><span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.maintExpDate.tip)}"><tds:convertDate date="${databaseInstance?.maintExpDate}"/></span></td>
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${databaseInstance?.planStatus}"/>
							<td class="valueNW" colspan="3"><span data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.planStatus.tip}">${databaseInstance?.planStatus}</span></td>
						</tr>
						<tr>
							<td></td>
							<td></td>

							<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${databaseInstance.externalRefId}"/>
							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${databaseInstance?.validation}"/>
							<td class="valueNW" colspan="3"><span data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.validation.tip}">${databaseInstance.validation}</td>
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

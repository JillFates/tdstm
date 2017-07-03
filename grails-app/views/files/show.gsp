<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<table style="border: 0">
	<tr>
		<td colspan="2">
			<div class="dialog" <tds:hasPermission permission="${Permission.AssetEdit}"> ondblclick="EntityCrud.showAssetEditView('${filesInstance?.assetClass}',${filesInstance?.id})"</tds:hasPermission>>
			<g:if test="${errors}">
				<div id="messageDivId" class="message">${errors}</div>
			</g:if>
			<table>
				<tbody>
				<tr class="prop">
					<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${filesInstance?.assetName}"/>
					<td colspan="3" style="max-width: 400px; font-weight:bold;">${filesInstance.assetName}</td>
					<tds:inputLabel field="${standardFieldSpecs.description}" value="${filesInstance?.description}"/>
					<td colspan="3" style="max-width: 400px;">${filesInstance.description}</td>
				</tr>
				<tr class="prop">
					<tds:showLabelAndField field="${standardFieldSpecs.fileFormat}" value="${filesInstance.fileFormat}"/>
					<tds:showLabelAndField field="${standardFieldSpecs.LUN}" value="${filesInstance.LUN}"/>

					<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${filesInstance.supportType}"/>

					<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}">${standardFieldSpecs.moveBundle.label} : Dep. Group</label></td>
					<td class="valueNW">${filesInstance?.moveBundle} : ${dependencyBundleNumber}</td>
				</tr>
				<tr class="prop">
					<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</label></td>
					<td class="valueNW">${filesInstance.size}&nbsp;&nbsp;${filesInstance.scale?.value()}</td>

					<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${filesInstance.externalRefId}"/>

					<tds:showLabelAndField field="${standardFieldSpecs.environment}" value="${filesInstance.environment}"/>
					<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${filesInstance.planStatus}"/>
				</tr>
				<tr>
					<tds:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${filesInstance.rateOfChange}"/>

					<td></td>
					<td></td>

					<td></td>
					<td></td>

					<tds:showLabelAndField field="${standardFieldSpecs.validation}" value="${filesInstance.validation}"/>
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
		<g:render template="../assetEntity/commentList" model="['asset':filesInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]"></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id="filedeleteId" value="${filesInstance?.id}" />
					<g:render template="../assetEntity/showButtons" model="[assetEntity:filesInstance]" />
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


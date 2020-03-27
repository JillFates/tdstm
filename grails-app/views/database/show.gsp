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
							<td colspan="2" class="valueNW ${standardFieldSpecs.assetName.imp?:''}" style="max-width: 400px; font-weight:bold;">
								<tds:tooltipSpan field="${standardFieldSpecs.assetName}">
									${databaseInstance?.assetName}
								</tds:tooltipSpan>
							</td>
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${databaseInstance?.description}"/>
							<td colspan="2" style="max-width: 400px;" class="valueNW ${standardFieldSpecs.description.imp?:''}" >
								<tds:tooltipSpan field="${standardFieldSpecs.description}">
									${databaseInstance.description}
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr class="prop">
							<tds:showLabelAndField field="${standardFieldSpecs.dbFormat}" value="${databaseInstance.dbFormat}"/>
							<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${databaseInstance.supportType}"/>
							<tds:showLabelAndField field="${standardFieldSpecs.environment}" value="${databaseInstance.environment}"/>
						</tr>
						<tr class="prop">
							<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
								<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
									${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
								</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.size.imp?:''}">
								<tds:tooltipSpan field="${standardFieldSpecs.size}">
									${databaseInstance?.size}&nbsp;${databaseInstance.scale?.value()}
								</tds:tooltipSpan>
							</td>
							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${databaseInstance?.retireDate}"/>
							<td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}">
								<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
									<tds:convertDate date="${databaseInstance?.retireDate}"/>
								</tds:tooltipSpan>
							</td>
							<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
								<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
									${standardFieldSpecs.moveBundle.label} : Dep. Group
								</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}" colspan="3">
								<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}">
									${databaseInstance?.moveBundle}
								</tds:tooltipSpan>
								<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${databaseInstance.assetName}"/>
							</td>
						</tr>
						<tr class="prop">
							<tds:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${databaseInstance.rateOfChange}"/>
							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${databaseInstance?.maintExpDate}"/>
							<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
								<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
									<tds:convertDate date="${databaseInstance?.maintExpDate}"/>
								</tds:tooltipSpan>
							</td>
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${databaseInstance?.planStatus}"/>
							<td class="valueNW ${standardFieldSpecs.planStatus.imp?:''}" colspan="3">
								<tds:tooltipSpan field="${standardFieldSpecs.planStatus}">
									${databaseInstance.planStatus}
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<td></td>
							<td></td>

							<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${databaseInstance.externalRefId}"/>
							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${databaseInstance?.validation}"/>
							<td class="valueNW ${standardFieldSpecs.validation.imp?:''}" colspan="3">
								<tds:tooltipSpan field="${standardFieldSpecs.validation}">
									${databaseInstance.validation}
								</tds:tooltipSpan>
							</td>
						</tr>
						<g:render template="/assetEntity/customShow" model="[assetEntity:databaseInstance, 'project':project]"></g:render>
						<g:render template="/comment/assetTagsShow"></g:render>
					</tbody>
				</table>
			</div></td>
	</tr>
	<tr>
		<td colspan="2" class="dates-info-container">
			<table class="dates-info">
				<tr>
					<td class="date-created date-info">Date created: ${dateCreated}</td>
					<td class="last-updated date-info">Last updated: ${lastUpdated}</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr id="deps">
		<g:render template="/assetEntity/dependentShow" model="[assetEntity:databaseInstance]" ></g:render>
	</tr>
	<tr id="commentListId">
		<g:render template="/assetEntity/commentList" model="['asset':databaseInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id ="databaseId" value="${databaseInstance?.id}" />
					<g:render template="/assetEntity/showButtons" model="[assetEntity:databaseInstance]"/>
				</g:form>
			</div>
		</td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')

	$(document).ready(function() {
		EntityCrud.loadAssetTags(${databaseInstance?.id});
		changeDocTitle('${raw(escapedName)}');
	})
</script>

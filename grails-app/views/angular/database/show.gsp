<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<table style="border: 0">
	<tr>
		<td colspan="2">

			<div class="dialog" 
				<tds:hasPermission permission="${Permission.AssetEdit}"> 
					ondblclick="EntityCrud.showAssetEditView('${asset.assetClass}',${asset?.id})" 
				</tds:hasPermission>
			>
			<h1>In the page</h1>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
			    </g:if>
				<table>
					<tbody>
						<tr class="prop">
							<tds-angular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
							<td colspan="2" class="valueNW ${standardFieldSpecs.assetName.imp?:''}" style="max-width: 400px; font-weight:bold;">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.assetName}">
									${asset?.assetName}
								</tds-angular:tooltipSpan>
							</td>
							<tds-angular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
							<td colspan="2" style="max-width: 400px;" class="valueNW ${standardFieldSpecs.description.imp?:''}" >
								<tds-angular:tooltipSpan field="${standardFieldSpecs.description}">
									${asset.description}
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tr class="prop">
							<tds-angular:showLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}"/>
							<tds-angular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"/>
							<tds-angular:showLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
						</tr>
						<tr class="prop">
							<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
								<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
									${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
								</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.size.imp?:''}">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.size}">
									${asset?.size}&nbsp;${asset.scale?.value()}
								</tds-angular:tooltipSpan>
							</td>
							<tds-angular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
							<td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.retireDate}">
									<tds:convertDate date="${asset?.retireDate}"/>
								</tds-angular:tooltipSpan>
							</td>
							<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
								<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
									${standardFieldSpecs.moveBundle.label} : Dep. Group
								</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}" colspan="3">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
									${asset?.moveBundle} : ${dependencyBundleNumber}
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tr class="prop">
							<tds-angular:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
							<tds-angular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
							<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
									<tds:convertDate date="${asset?.maintExpDate}"/>
								</tds-angular:tooltipSpan>
							</td>
							<tds-angular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
							<td class="valueNW ${standardFieldSpecs.planStatus.imp?:''}" colspan="3">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.planStatus}">
									${asset.planStatus}
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tr>
							<td></td>
							<td></td>

							<tds-angular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}"/>
							<tds-angular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
							<td class="valueNW ${standardFieldSpecs.validation.imp?:''}" colspan="3">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.validation}">
									${asset.validation}
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<g:render template="/angular/common/customShow" model="[asset:asset, project:project]"></g:render>
					</tbody>
				</table>
			</div></td>
	</tr>
	<tr id="deps">
		<g:render template="/angular/common/dependentShow" model="[asset:asset]" ></g:render>
	</tr>
	<tr id="commentListId">
		<g:render template="/angular/common/commentList" model="[asset:asset, prefValue:prefValue, viewUnpublishedValue:viewUnpublishedValue]" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<form>
					<input type="hidden" name="id" id ="databaseId" value="${asset?.id}" />
					<g:render template="/angular/common/assetShowButtons" model="[asset:asset]" />
				<form>
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
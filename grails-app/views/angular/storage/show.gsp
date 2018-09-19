<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Logical Storage Detail</h4>
	</div>
	<div class="modal-body">
		<div>
			<table>
				<tr>
					<td colspan="2" class="dialog-container">
						<div class="dialog">
						<g:if test="${errors}">
							<div id="messageDivId" class="message">${errors}</div>
						</g:if>
						<table>
							<tbody>
							<tr class="prop">
								<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${filesInstance?.assetName}"/>
								<td class="valueNW ${standardFieldSpecs.assetName.imp?:''}" colspan="3" style="	max-width: 400px; font-weight:bold;">${filesInstance.assetName}</td>
								<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${filesInstance?.description}"/>
								<td class="valueNW ${standardFieldSpecs.description.imp?:''}" colspan="3" style="max-width: 400px;">${filesInstance.description}</td>
							</tr>
							<tr class="prop">
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.fileFormat}" value="${filesInstance.fileFormat}"/>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.LUN}" value="${filesInstance.LUN}"/>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${filesInstance.supportType}"/>
								<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
									<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
										${standardFieldSpecs.moveBundle.label} : Dep. Group
									</label>
								</td>
								<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
									${filesInstance?.moveBundle}
									<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${filesInstance.assetName}"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</td>
								<td class="valueNW ${standardFieldSpecs.scale.imp?:''}">${filesInstance.size}&nbsp;&nbsp;${filesInstance.scale?.value()}</td>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${filesInstance.externalRefId}"/>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${filesInstance.environment}"/>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${filesInstance.planStatus}" tooltipDataPlacement="bottom"/>
							</tr>
							<tr>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${filesInstance.rateOfChange}"/>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.validation}" value="${filesInstance.validation}" tooltipDataPlacement="bottom"/>
							</tr>
							<g:render template="/angular/common/customShow" model="[assetEntity:filesInstance]"></g:render>
                            <g:render template="/angular/common/assetTags"></g:render>
						</table>
						</div>
					</td>
				</tr>

				<tr>
					<td colspan="2" class="dates-info-container">
						<table class="dates-info" >
							<tr>
								<td class="date-created">Date created: ${dateCreated}</td>
								<td class="last-updated">Last updated: ${lastUpdated}</td>
							</tr>
						</table>
					</td>
				</tr>

				<tr id="deps">
					<g:render template="/angular/common/dependentShow" model="[assetEntity:filesInstance]" ></g:render>
				</tr>
				<tr id="commentListId">
					<g:render template="/angular/common/commentList" model="['asset':filesInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]"></g:render>
				</tr>
			</table>
		</div>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Close</button>
		<tds:hasPermission permission="${Permission.AssetDelete}">
			<button class="btn btn-danger" (click)="onDeleteAsset()" type="button"><span  class="glyphicon glyphicon-trash"></span> Delete</button>
		</tds:hasPermission>
	    <button class="btn btn-primary pull-left" (click)="showAssetEditView()" type="button"><span  class="glyphicon glyphicon-pencil"></span> Edit</button>
	</div>
</div>
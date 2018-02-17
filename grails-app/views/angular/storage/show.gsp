<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Logical Storage Detail</h4>
	</div>
	<div class="modal-body">
		<div>
			<table style="border: 0">
				<tr>
					<td colspan="2">
						<div class="dialog">
						<g:if test="${errors}">
							<div id="messageDivId" class="message">${errors}</div>
						</g:if>
						<table>
							<tbody>
							<tr class="prop">
								<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${filesInstance?.assetName}"/>
								<td class="valueNW ${standardFieldSpecs.assetName.imp?:''}" colspan="3" style="	max-width: 400px; font-weight:bold;">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.assetName}">
										${filesInstance.assetName}
									</tdsAngular:tooltipSpan>
								</td>
								<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${filesInstance?.description}"/>
								<td class="valueNW ${standardFieldSpecs.description.imp?:''}" colspan="3" style="max-width: 400px;">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.description}">
										${filesInstance.description}
									</tdsAngular:tooltipSpan>
								</td>
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
									<g:if test="${dependencyBundleNumber}">
										${filesInstance?.moveBundle} :
										<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${filesInstance.assetName}"/>
									</g:if>
									<g:else>
										<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}" tooltipDataPlacement="bottom">
											${filesInstance?.moveBundle}
										</tds:tooltipSpan>
									</g:else>
								</td>
							</tr>
							<tr class="prop">
								<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
									<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
										${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
									</label>
								</td>
								<td class="valueNW ${standardFieldSpecs.scale.imp?:''}">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.size}">
										${filesInstance.size}&nbsp;&nbsp;${filesInstance.scale?.value()}
									</tdsAngular:tooltipSpan>
								</td>

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
							</tbody>
						</table>
						</div>
					</td>
				</tr>

				<tr>
					<td colspan="2">
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
		<button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>
<%@page import="com.tds.asset.Database"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="Database" />

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="onCancelEdit()" class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Database Create</h4>
	</div>
	<div class="modal-body">
		<div>
			<form name="databaseEditForm">
				<table style="border: 0">
					<tr>
						<td colspan="2" class="dialog-container">
							<div class="dialog">
								<table class="asset-edit-view">
									<tbody>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance?.assetName}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${assetInstance.assetName}" ngmodel="model.asset.assetName"/>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetInstance?.description}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="12" value="${assetInstance.description}" ngmodel="model.asset.description" />
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${assetInstance.dbFormat}" tabindex="13" ngmodel="model.asset.dbFormat"/>

											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetInstance?.supportType}" ngmodel="model.asset.supportType"/>

											<tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${assetInstance?.environment}"/>
											<td class="${standardFieldSpecs.environment.imp ?: ''}" data-for="environment">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetEnvironment"
														[(ngModel)]="model.asset.environment"
														[defaultItem]="'Please Select'"
														[data]="model.environmentOptions">
												</kendo-dropdownlist>
											</td>
										</tr>
										<tr>
											<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
												<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
													${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
												</label>
											</td>
											<td  data-for="sizeScale" nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp ?: ''}">
												<tdsAngular:inputControl field="${standardFieldSpecs.size}" size="4" tabindex="14" value="${assetInstance.size}" ngmodel="model.asset.size"/>
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetScaleName"
														[data]="${SizeScale.getAsJsonList() as JSON}"
														[(ngModel)]="model.asset.scale.name"
														[defaultItem]="''"
														[textField]="'text'"
														[valueField]="'value'"
														style="width: 100px;">
												</kendo-dropdownlist>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetInstance?.retireDate}"/>
											<td data-for="retireDate"  valign="top" class="value ${hasErrors(bean:asset,field:'retireDate','errors')} ${standardFieldSpecs.retireDate.imp ?: ''}">
												<kendo-datepicker
														class="tm-input-control"
														name="modelAssetRetireDate"
														[format]="dateFormat"
														[(value)]="model.asset.retireDate">
												</kendo-datepicker>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance?.moveBundle}"/>
											<td class="${standardFieldSpecs.moveBundle.imp ?: ''}" data-for="moveBundle">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetMoveBundle"
														[data]="model.moveBundleList"
														[(ngModel)]="model.asset.moveBundle"
														[textField]="'name'"
														[valueField]="'id'">
												</kendo-dropdownlist>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${assetInstance?.rateOfChange}"/>
											<td>
												<tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" tabindex="17" value="${assetInstance.rateOfChange}" ngmodel="model.asset.rateOfChange"/>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetInstance?.maintExpDate}"/>
											<td data-for="maintExpDate" valign="top" class="value ${hasErrors(bean:asset,field:'maintExpDate','errors')} ${standardFieldSpecs.maintExpDate.imp ?: ''}">
												<kendo-datepicker
														class="tm-input-control"
														name="modelAssetMainExpDate"
														[format]="dateFormat"
														[(value)]="model.asset.maintExpDate">
												</kendo-datepicker>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance?.planStatus}"/>
											<td class="${standardFieldSpecs.planStatus.imp ?: ''}" data-for="planStatus">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetPlanStatus"
														[data]="model.planStatusOptions"
														[(ngModel)]="model.asset.planStatus">
												</kendo-dropdownlist>
											</td>
										</tr>
										<tr>
											<td></td>
											<td></td>

											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="22"/>

											<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${assetInstance?.validation}"/>
											<td class="${standardFieldSpecs.validation.imp ?: ''}" data-for="validation">
												<kendo-dropdownlist
														[defaultItem]="${assetInstance.constraints.validation.inList as JSON}[0]"
														class="tm-input-control"
														name="modelAssetValidation"
														[data]="${assetInstance.constraints.validation.inList as JSON}"
														[(ngModel)]="model.asset.validation">
												</kendo-dropdownlist>
											</td>
										</tr>
										<g:render template="/angular/common/customEdit" model="[assetEntityInstance:assetInstance]"></g:render>
                                        <g:render template="/angular/common/assetTagsEdit"></g:render>
									</tbody>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="2">&nbsp;</td>
					</tr>
					<!-- Dependencies -->
					<tr id="deps">
						<td valign="top" colspan="2">
							<tds-supports-depends (initDone)="onInitDependenciesDone($event)" [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-primary pull-left component-action-update" type="button" (click)="onCreate()" [disabled]="!isDependenciesValidForm"><span class="fa fa-fw fa-floppy-o"></span> Save</button>

		<button class="btn btn-default pull-right component-action-cancel" (click)="onCancelEdit()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>
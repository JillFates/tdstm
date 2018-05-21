<%@page import="com.tds.asset.Database"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="Database" />

<div class="modal-content tds-angular-component-content" tabindex="0">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Database Edit</h4>
	</div>
	<div class="modal-body">
		<div>
			<form name="databaseEditForm">
				<table style="border: 0">
					<tr>
						<td colspan="2">
							<div class="dialog">
								<table>
									<tbody>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${asset.assetName}" ngmodel="model.asset.assetName"/>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="12" value="${asset.description}" ngmodel="model.asset.description" tooltipDataPlacement="bottom"/>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}" tabindex="13" ngmodel="model.asset.dbFormat"/>

											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset?.supportType}" ngmodel="model.asset.supportType"/>

											<tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset?.environment}"/>
											<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">
												<tdsAngular:tooltipSpan field="${standardFieldSpecs.environment}">
													<kendo-dropdownlist
															class="select"
															name="modelAssetEnvironment"
															[(ngModel)]="model.asset.environment"
															[defaultItem]="'Please Select'"
															[data]="model.environmentOptions">
													</kendo-dropdownlist>
												</tdsAngular:tooltipSpan>
											</td>
										</tr>
										<tr>
											<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
												<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
													${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
												</label>
											</td>
											<td nowrap="nowrap" class="sizeScale">
												<tdsAngular:inputControl field="${standardFieldSpecs.size}" size="4" tabindex="14" value="${asset.size}" ngmodel="model.asset.size"/>
												<tdsAngular:tooltipSpan field="${standardFieldSpecs.scale}">
													<kendo-dropdownlist
															class="select"
															name="modelAssetScaleName"
															[data]="${SizeScale.getAsJsonList() as JSON}"
															[(ngModel)]="model.asset.scale.name"
															[defaultItem]="''"
															[textField]="'text'"
															[valueField]="'value'">
													</kendo-dropdownlist>
												</tdsAngular:tooltipSpan>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
											<td valign="top" class="value ${hasErrors(bean:asset,field:'retireDate','errors')}">
												<kendo-datepicker
														name="modelAssetRetireDate"
														[format]="dateFormat"
														[(value)]="model.asset.retireDate">
												</kendo-datepicker>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset?.moveBundle}"/>
											<td>
												<tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
													<kendo-dropdownlist
															class="select"
															name="modelAssetMoveBundle"
															[data]="model.moveBundleList"
															[(ngModel)]="model.asset.moveBundle"
															[textField]="'name'"
															[valueField]="'id'">
													</kendo-dropdownlist>
												</tdsAngular:tooltipSpan>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset?.rateOfChange}"/>
											<td>
												<tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" tabindex="17" value="${asset.rateOfChange}" ngmodel="model.asset.rateOfChange"/>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
											<td valign="top" class="value ${hasErrors(bean:asset,field:'maintExpDate','errors')}">
												<kendo-datepicker
														name="modelAssetMainExpDate"
														[format]="dateFormat"
														[(value)]="model.asset.maintExpDate">
												</kendo-datepicker> <!--  -->
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
											<td>
												<tdsAngular:tooltipSpan field="${standardFieldSpecs.planStatus}">
													<kendo-dropdownlist
															class="select"
															name="modelAssetPlanStatus"
															[data]="model.planStatusOptions"
															[(ngModel)]="model.asset.planStatus">
													</kendo-dropdownlist>
												</tdsAngular:tooltipSpan>
											</td>
										</tr>
										<tr>
											<td></td>
											<td></td>

											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="22"/>

											<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
											<td>
												<tdsAngular:tooltipSpan field="${standardFieldSpecs.validation}">
													<kendo-dropdownlist
															class="select"
															name="modelAssetValidation"
															[data]="${asset.constraints.validation.inList as JSON}"
															[(ngModel)]="model.asset.validation">
													</kendo-dropdownlist>
												</tdsAngular:tooltipSpan>
											</td>
										</tr>
										<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>
									</tbody>
								</table>
							</div>
						</td>
					</tr>
					<!-- Dependencies -->
					<tr id="deps">
						<td valign="top" colspan="2">
							<tds-supports-depends [(model)]="model"></tds-supports-depends>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-primary pull-left" type="button" (click)="onUpdate()"><span class="fa fa-fw fa-floppy-o"></span> Update</button>
		<tds:hasPermission permission="${Permission.AssetDelete}">
			<button class="btn btn-danger pull-left mar-left-50" (click)="onDelete()" type="button"><span class="glyphicon glyphicon-trash"></span> Delete</button>
		</tds:hasPermission>
		<button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>
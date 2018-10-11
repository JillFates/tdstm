<%@page import="com.tds.asset.Application"%>
<%@page import="com.tds.asset.AssetType"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<g:set var="assetClass" value="Application" />
<%@page import="grails.converters.JSON"%>

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="onCancelEdit()"
	 class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span
				aria-hidden="true">×</span></button>
		<h4 class="modal-title">Application Edit</h4>
	</div>
	<div class="modal-body">
			<form name="applicationEditForm" role="form" #applicationEditForm="ngForm">
				<table>
					<tr>
						<td class="dialog-container">
							<div class="dialog">
								<table class="asset-edit-view">
									<tbody>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset.assetName}" />
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${asset.assetName}"  ngmodel="model.asset.assetName"  />
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset.description}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="11" value="${asset.description}"  ngmodel="model.asset.description"/>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}" ngmodel="model.asset.appVendor" tabindex="13"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}" ngmodel="model.asset.supportType" tabindex="22"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}" ngmodel="model.asset.appFunction" tabindex="32"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${asset.userCount}"  ngmodel="model.asset.userCount" tabindex="42" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}" ngmodel="model.asset.appVersion" tabindex="14"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
											<td data-for="sme" class="${standardFieldSpecs.sme.imp?:''}">
												<kendo-dropdownlist #controlSme
														class="tm-input-control person-list"
														name="modelAssetSme"
														[(ngModel)]="persons.sme"
														(selectionChange)="onAddPerson($event,'application', 'sme',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON})"
														[defaultItem]="defaultItem"
														[textField]="'fullName'"
														[valueField]="'personId'"
														[data]="getPersonList(${personList as JSON})">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
											<td data-for="environment" class="${standardFieldSpecs.environment.imp?:''}">
												<kendo-dropdownlist
														class="tm-input-control person-list"
														name="modelAssetEnvironment"
														[(ngModel)]="model.asset.environment"
														[defaultItem]="'Please Select'"
														[data]="model.environmentOptions">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" ngmodel="model.asset.userLocations" tabindex="43" tooltipDataPlacement="bottom"/>
										</tr>

										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${asset.appTech}" ngmodel="model.asset.appTech" tabindex="15"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
											<td data-for="sme2" class="${standardFieldSpecs.sme2.imp?:''} suffleTd" >
												<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap Contacts"></div>
												<kendo-dropdownlist  #controlSme2
													class="tm-input-control person-list"
													name="modelAssetSme2"
													[(ngModel)]="persons.sme2"
													(selectionChange)="onAddPerson($event,'application', 'sme2',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON})"
													[defaultItem]="defaultItem"
													[textField]="'fullName'"
													[valueField]="'personId'"
													[data]="getPersonList(${personList as JSON})">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.criticality}" value="${asset.criticality}"/>
											<td data-for="criticality" class="${standardFieldSpecs.criticality.imp?:''}">
												<kendo-dropdownlist
													class="tm-input-control"
													name="modelAssetCriticality"
													[(ngModel)]="model.asset.criticality"
													[defaultItem]="'Please Select'"
													[data]="${asset.constraints.criticality.inList as JSON}">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"  value="${asset.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="44" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${asset.appSource}" ngmodel="model.asset.appSource" tabindex="16"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
											<td class="suffleTd ${standardFieldSpecs.appOwner.imp?:''}" data-for="appOwner">
												<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')" title="Swap Contacts"></div>
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetappOwner"
														[(ngModel)]="persons.appOwner"
														(selectionChange)="onAddPerson($event,'application', 'appOwner',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON})"
														[defaultItem]="defaultItem"
														[textField]="'fullName'"
														[valueField]="'personId'"
														[data]="getPersonList(${personList as JSON})">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
											<td data-for="moveBundle" class="${standardFieldSpecs.moveBundle.imp?:''}">
												<kendo-dropdownlist
													class="tm-input-control"
													name="modelAssetMoveBundle"
													[data]="model.moveBundleList"
													[(ngModel)]="model.asset.moveBundle"
													[textField]="'name'"
													[valueField]="'id'">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${asset.drRpoDesc}"  ngmodel="model.asset.drRpoDesc" tabindex="45" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.license}" value="${asset.license}" ngmodel="model.asset.license" tabindex="17"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${asset.businessUnit}" ngmodel="model.asset.businessUnit" tabindex="26"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
											<td data-for="planStatus" class="${standardFieldSpecs.planStatus.imp?:''}">
												<kendo-dropdownlist
													class="tm-input-control"
													name="modelAssetPlanStatus"
													[(ngModel)]="model.asset.planStatus"
													[data]="model.planStatusOptions">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="46" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
										<tr>
											<td></td>
											<td></td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
											<td data-for="retireDate" valign="top" class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}  ${standardFieldSpecs.retireDate.imp?:''}">
												<kendo-datepicker
													class="tm-input-control"
													name="modelAssetRetireDate"
													[format]="dateFormat"
													[(value)]="model.asset.retireDate">
												</kendo-datepicker>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
											<td data-for="validation" class="${standardFieldSpecs.validation.imp?:''}">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetValidation"
														[data]="${asset.constraints.validation.inList as JSON}"
														[(ngModel)]="model.asset.validation">
												</kendo-dropdownlist>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.testProc}" value="${asset.testProc}"/>
											<td data-for="testProc" class="${standardFieldSpecs.testProc.imp?:''}">
											<tdsAngular:tooltipSpan field="${standardFieldSpecs.testProc}">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetTestProc"
														[(ngModel)]="model.asset.testProc"
														[defaultItem]="'?'"
														[data]="yesNoList">
												</kendo-dropdownlist>
											</tdsAngular:tooltipSpan>
											</td>
										</tr>
										<tr>
											<td></td>
											<td></td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
											<td data-for="maintExpDate" valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}  ${standardFieldSpecs.maintExpDate.imp?:''} ">
												<kendo-datepicker
														class="tm-input-control"
														name="modelAssetMaintExpDate"
														[format]="dateFormat"
														[(value)]="model.asset.maintExpDate">
												</kendo-datepicker>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.latency}" value="${asset.latency}"/>
											<td data-for="latency" class="${standardFieldSpecs.latency.imp?:''}">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetLatency"
														[(ngModel)]="model.asset.latency"
														[defaultItem]="'?'"
														[data]="yesNoList">
												</kendo-dropdownlist>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}"/>
											<td data-for="startupProc" class="${standardFieldSpecs.startupProc.imp?:''}">
												<kendo-dropdownlist
														class="tm-input-control"
														name="modelAssetStartupProc"
														[(ngModel)]="model.asset.startupProc"
														[defaultItem]="'?'"
														[data]="yesNoList">
												</kendo-dropdownlist>
											</td>

										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}" value="${asset.url}" ngmodel="model.asset.url" tabindex="18"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="28"/>
											<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}" nowrap="nowrap">
												<label for="shutdownBy">
													<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
														${standardFieldSpecs.shutdownBy.label}
													</tdsAngular:tooltipSpan>
												</label>
											</td>
											<td class="tm-input-control ${standardFieldSpecs.shutdownBy.imp?:''}" data-for="shutdownBy" nowrap="nowrap">
												<tds-combobox-group
														[model]="model.asset.shutdownBy"
														(modelChange)="model.asset.shutdownBy = $event"
														(isFixedChange)="model.asset.shutdownFixed = $event"
														[isFixed]="${asset.shutdownFixed}"
														[namedStaff]="${personList as JSON}"
														[team]="${availableRoles as JSON}">
												</tds-combobox-group>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}"/>
											<td class="tm-input-control duration-container">
												<input type="text" id="shutdownDuration" name="shutdownDuration"
													   class="${standardFieldSpecs.shutdownDuration.imp?:''} duration"
													   [(ngModel)]="model.asset.shutdownDuration" tabindex="48" size="7"/>
												<label>m</label>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
											<td colspan="1" nowrap="nowrap" data-for="startupBy" class="tm-input-control ${standardFieldSpecs.startupBy.imp?:''}">
												<tds-combobox-group
														[model]="model.asset.startupBy"
														(modelChange)="model.asset.startupBy = $event"
														(isFixedChange)="model.asset.startupFixed = $event"
														[isFixed]="${asset.startupFixed}"
														[namedStaff]="${personList as JSON}"
														[team]="${availableRoles as JSON}"></tds-combobox-group>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}" ngmodel="model.asset.startupDuration" tabindex="29"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
											<td colspan="1" nowrap="nowrap" data-for="testingBy" class="tm-input-control ${standardFieldSpecs.testingBy.imp?:''}">
												<tds-combobox-group
														[model]="model.asset.testingBy"
														(modelChange)="model.asset.testingBy = $event"
														(isFixedChange)="model.asset.testingFixed = $event"
														[isFixed]="${asset.testingFixed}"
														[namedStaff]="${personList as JSON}"
														[team]="${availableRoles as JSON}">
                                                </tds-combobox-group>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}"/>
											<td class="tm-input-control duration-container">
												<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}
												duration" name="testingDuration" [(ngModel)]="model.asset.testingDuration" tabindex="49"  size="7"/>
												<label>m</label>
											</td>
										</tr>

										<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>
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
						<tds-supports-depends (initDone)="onInitDependenciesDone($event)"  [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
					</tr>
				</table>
			</form>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-primary pull-left component-action-update" type="button" (click)="onUpdate()" [disabled]="!applicationEditForm.form.valid || !isDependenciesValidForm"><span class="fa fa-fw fa-floppy-o"></span> Update</button>
		<tds:hasPermission permission="${Permission.AssetDelete}">
			<button class="btn btn-danger component-action-delete" (click)="onDeleteAsset()" type="button"><span class="glyphicon glyphicon-trash"></span> Delete</button>
		</tds:hasPermission>
		<button class="btn btn-default pull-right component-action-cancel" (click)="onCancelEdit()" type="button"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>
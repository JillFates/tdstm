<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.asset.AssetType"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<g:set var="assetClass" value="Application" />
<%@page import="grails.converters.JSON"%>

<div tds-autocenter tds-handle-escape (escPressed)="onCancelEdit()"
	 class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span
				aria-hidden="true">×</span></button>
		<h4 class="modal-title">Application Edit</h4>
	</div>
	<div class="modal-body">
		<form name="form" (ngSubmit)="form.form.valid && onUpdate()"
			  class="asset-entry-form"
			  [ngClass]="{'form-submitted': form && form.submitted}"
			  role="form" #form="ngForm" novalidate>
				<table>
					<tr>
						<td class="dialog-container">
							<div class="dialog">
								<table class="asset-edit-view">
									<tbody>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset.assetName}" />
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${asset.assetName}"  ngmodel="model.asset.assetName"  />
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset.description}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="2" value="${asset.description}"  ngmodel="model.asset.description"/>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}" ngmodel="model.asset.appVendor" tabindex="3"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}" ngmodel="model.asset.supportType" tabindex="10"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}" ngmodel="model.asset.appFunction" tabindex="18"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${asset.userCount}"  ngmodel="model.asset.userCount" tabindex="27" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}" ngmodel="model.asset.appVersion" tabindex="4"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
											<td data-for="sme" class="${standardFieldSpecs.sme.imp?:''}">
												<kendo-dropdownlist #controlSME1
														[filterable]="true"
														(filterChange)="filterSME1Change($event)"
														(focus)="focusSME1()"
														(close)="onClose($event, controlSME1)"
														[tabIndex]="11"
														class="tm-input-control person-list controlSME1"
														name="modelAssetSme"
														[(ngModel)]="persons.sme"
														(valueChange)="onAddPerson($event,'application', 'sme',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON}, 'sme1PersonList', controlSME1)"
														[defaultItem]="defaultItem"
														[textField]="'fullName'"
														[valueField]="'personId'"
														[data]="model.sme1PersonList">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="19" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" ngmodel="model.asset.userLocations" tabindex="28" tooltipDataPlacement="bottom"/>
										</tr>

										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${asset.appTech}" ngmodel="model.asset.appTech" tabindex="5"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
											<td data-for="sme2" class="${standardFieldSpecs.sme2.imp?:''} suffleTd" >
												<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap Contacts"></div>
												<kendo-dropdownlist  #controlSME2
												    [filterable]="true"
												    (filterChange)="filterSME2Change($event)"
													(focus)="focusSME2()"
													(close)="onClose($event, controlSME2)"
													[tabIndex]="12"
													class="tm-input-control person-list controlSME2"
													name="modelAssetSme2"
													[(ngModel)]="persons.sme2"
													(valueChange)="onAddPerson($event,'application', 'sme2',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON}, 'sme2PersonList', controlSME2)"
													[defaultItem]="defaultItem"
													[textField]="'fullName'"
													[valueField]="'personId'"
													[data]="model.sme2PersonList">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.criticality}" value="${asset.criticality}" tabindex="20"  ngmodel="model.asset.criticality"  blankOptionListText="Please Select..."/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"  value="${asset.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="29" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${asset.appSource}" ngmodel="model.asset.appSource" tabindex="6"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
											<td class="suffleTd ${standardFieldSpecs.appOwner.imp?:''}" data-for="appOwner">
												<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')" title="Swap Contacts"></div>
												<kendo-dropdownlist #controlAppOwner
														[filterable]="true"
														(filterChange)="filterAppOwnerChange($event)"
														(focus)="focusAppOwner()"
														(close)="onClose($event, controlAppOwner)"
														[tabIndex]="13"
														class="tm-input-control person-list controlAppOwner"
														name="modelAssetappOwner"
														[(ngModel)]="persons.appOwner"
														(valueChange)="onAddPerson($event,'application', 'appOwner',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON}, 'appOwnerPersonList', controlAppOwner)"
														[defaultItem]="defaultItem"
														[textField]="'fullName'"
														[valueField]="'personId'"
														[data]="model.appOwnerPersonList">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
											<td data-for="moveBundle" class="${standardFieldSpecs.moveBundle.imp?:''}">
												<kendo-dropdownlist
													[tabIndex]="21"
													class="tm-input-control"
													name="modelAssetMoveBundle"
													[data]="model.moveBundleList"
													[(ngModel)]="model.asset.moveBundle"
													[textField]="'name'"
													[valueField]="'id'">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${asset.drRpoDesc}"  ngmodel="model.asset.drRpoDesc" tabindex="30" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.license}" value="${asset.license}" ngmodel="model.asset.license" tabindex="7"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${asset.businessUnit}" ngmodel="model.asset.businessUnit" tabindex="14"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
											<td data-for="planStatus" class="${standardFieldSpecs.planStatus.imp?:''}">
												<kendo-dropdownlist
													[tabIndex]="22"
													class="tm-input-control"
													name="modelAssetPlanStatus"
													[(ngModel)]="model.asset.planStatus"
													[data]="model.planStatusOptions">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="31" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<td></td>
											<td></td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
											<td data-for="retireDate" valign="top" class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}  ${standardFieldSpecs.retireDate.imp?:''}">
                                                <tds-date-control
													class="tm-input-control"
													[(ngModel)]="model.asset.retireDate"
													name="modelAssetRetireDate"
													[tabindex]="15"
                                                    [value]="model.asset.retireDate">
                                                </tds-date-control>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
											<td data-for="validation" class="${standardFieldSpecs.validation.imp?:''}">
												<kendo-dropdownlist
														[tabIndex]="23"
														class="tm-input-control"
														name="modelAssetValidation"
														[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
														[(ngModel)]="model.asset.validation">
												</kendo-dropdownlist>
											</td>

											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.testProc}" value="${asset.testProc}" tabindex="32"  ngmodel="model.asset.testProc" blankOptionListText="?" />
										</tr>
										<tr>
											<td></td>
											<td></td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
											<td data-for="maintExpDate" valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}  ${standardFieldSpecs.maintExpDate.imp?:''} ">
                                                <tds-date-control
													class="tm-input-control"
													[(ngModel)]="model.asset.maintExpDate"
													name="modelAssetMaintExpDate"
													[tabindex]="16"
                                                    [value]="model.asset.maintExpDate">
                                                </tds-date-control>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.latency}" value="${asset.latency}" tabindex="24"  ngmodel="model.asset.latency" blankOptionListText="?" />
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}" tabindex="33" ngmodel="model.asset.startupProc" blankOptionListText="?" />
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}" value="${asset.url}" ngmodel="model.asset.url" tabindex="8"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="17"/>
											<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}"
												[ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${asset}" fieldName="shutdownBy" /> }"
												nowrap="nowrap">
												<label for="shutdownBy">
													<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
														${standardFieldSpecs.shutdownBy.label}
													</tdsAngular:tooltipSpan>
												</label>
											</td>
											<td class="tm-input-control ${standardFieldSpecs.shutdownBy.imp?:''}" data-for="shutdownBy" nowrap="nowrap">
												<tds-combobox-group
														[model]="model.asset.shutdownBy"
														(modelChange)="model.asset.shutdownBy.id = $event"
														(isFixedChange)="model.asset.shutdownFixed = $event"
														[isFixed]="${asset.shutdownFixed}"
														[namedStaff]="${personList as JSON}"
														[tabIndex]="25"
														[team]="${availableRoles as JSON}">
												</tds-combobox-group>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}"/>
											<td class="tm-input-control duration-container">
												<input type="text" id="shutdownDuration" name="shutdownDuration" tabindex="34"
													   class="${standardFieldSpecs.shutdownDuration.imp?:''} duration"
													   [(ngModel)]="model.asset.shutdownDuration" size="7"/>
												<label>m</label>
											</td>
										</tr>


										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
											<td colspan="1" nowrap="nowrap" data-for="startupBy"
												class="tm-input-control ${standardFieldSpecs.startupBy.imp?:''}">
												<tds-combobox-group
														[model]="model.asset.startupBy"
														(modelChange)="model.asset.startupBy.id = $event"
														(isFixedChange)="model.asset.startupFixed = $event"
														[isFixed]="${asset.startupFixed}"
														[namedStaff]="${personList as JSON}"
														[tabIndex]="9"
														[team]="${availableRoles as JSON}"></tds-combobox-group>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}" ngmodel="model.asset.startupDuration" tabindex="17"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>

											<td colspan="1" nowrap="nowrap" data-for="testingBy" class="tm-input-control ${standardFieldSpecs.testingBy.imp?:''}">
												<tds-combobox-group
														[model]="model.asset.testingBy"
														(modelChange)="model.asset.testingBy.id = $event"
														(isFixedChange)="model.asset.testingFixed = $event"
														[isFixed]="${asset.testingFixed}"
														[namedStaff]="${personList as JSON}"
														[tabIndex]="26"
														[team]="${availableRoles as JSON}">
                                                </tds-combobox-group>
											</td>

											<tdsAngular:inputLabel field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}"/>
											<td class="tm-input-control duration-container">
												<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}
												duration" name="testingDuration" [(ngModel)]="model.asset.testingDuration" tabindex="36"  size="7"/>
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
		<tds-button-save
				class="btn-primary pull-left component-action-update" tabindex="501"
				tooltip="Update Asset"
				[disabled]="!isDependenciesValidForm"
				[permissions]="['${Permission.AssetEdit}']"
				(click)="submitForm($event)">
		</tds-button-save>

		<tds:hasPermission permission="${Permission.AssetDelete}">
			<tds-button-delete
					tooltip="Delete Asset"
					class="btn-danger component-action-delete" tabindex="502"
					[permissions]="['${Permission.AssetDelete}']"
					(click)="onDeleteAsset()">
			</tds-button-delete>
		</tds:hasPermission>

		<tds-button-cancel
				tooltip="Cancel Edit"
				class="pull-right component-action-cancel"
				tabindex="503"
				(click)="onCancelEdit()">
		</tds-button-cancel>
	</div>
</div>

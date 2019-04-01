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
		<h4 class="modal-title">Application Create</h4>
	</div>
	<div class="modal-body">
			<form name="form" (ngSubmit)="form.form.valid && onCreate()"
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
											<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance.assetName}" />
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="10" value="${assetInstance.assetName}"  ngmodel="model.asset.assetName"  />
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetInstance.description}"/>
											<td colspan="3">
												<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="11" value="${assetInstance.description}"  ngmodel="model.asset.description"/>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${assetInstance.appVendor}" ngmodel="model.asset.appVendor" tabindex="12"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetInstance.supportType}" ngmodel="model.asset.supportType" tabindex="19"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${assetInstance.appFunction}" ngmodel="model.asset.appFunction" tabindex="28"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${assetInstance.userCount}"  ngmodel="model.asset.userCount" tabindex="37" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${assetInstance.appVersion}" ngmodel="model.asset.appVersion" tabindex="13"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${assetInstance.sme}" />
											<td data-for="sme" class="${standardFieldSpecs.sme.imp?:''}">
												<kendo-dropdownlist #controlSme
													[tabIndex]="20"
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
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${assetInstance.environment}" tabindex="29" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${assetInstance.userLocations}" ngmodel="model.asset.userLocations" tabindex="38" tooltipDataPlacement="bottom"/>
										</tr>

										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${assetInstance.appTech}" ngmodel="model.asset.appTech" tabindex="14"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${assetInstance.sme2}"/>
											<td data-for="sme2" class="${standardFieldSpecs.sme2.imp?:''} suffleTd"  >
												<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap Contacts"></div>
													<kendo-dropdownlist  #controlSme2
																		 [tabIndex]="21"
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
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.criticality}" value="${assetInstance.criticality}" tabindex="30"  ngmodel="model.asset.criticality"  blankOptionListText="Please Select..."/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"  value="${assetInstance.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="39" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${assetInstance.appSource}" ngmodel="model.asset.appSource" tabindex="15"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${assetInstance.appOwner}"/>
											<td class="suffleTd ${standardFieldSpecs.appOwner.imp?:''}" data-for="appOwner">
												<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')"
													 title="Swap Contacts"></div>
												<kendo-dropdownlist
														[tabIndex]="22"
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

											<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance.dependencyBundle?.value}"/>
											<td data-for="moveBundle" class="${standardFieldSpecs.moveBundle.imp?:''}">
												<kendo-dropdownlist
													[tabIndex]="31"
													class="tm-input-control"
													name="modelAssetMoveBundle"
													[data]="moveBundleList"
													[(ngModel)]="model.asset.moveBundle"
													[textField]="'name'"
													[valueField]="'id'">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${assetInstance.drRpoDesc}"  ngmodel="model.asset.drRpoDesc" tabindex="40" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.license}" value="${assetInstance.license}" ngmodel="model.asset.license" tabindex="16"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${assetInstance.businessUnit}" ngmodel="model.asset.businessUnit" tabindex="23"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance.planStatus}" />
											<td data-for="planStatus" class="${standardFieldSpecs.planStatus.imp?:''}">
												<kendo-dropdownlist
													[tabIndex]="32"
													class="tm-input-control"
													name="modelAssetPlanStatus"
													[(ngModel)]="model.asset.planStatus"
													[data]="model.planStatusOptions">
												</kendo-dropdownlist>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${assetInstance.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="41" tooltipDataPlacement="bottom"/>
										</tr>
										<tr>
										<tr>
											<td></td>
											<td></td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetInstance.retireDate}"/>
											<td data-for="retireDate"  valign="top" class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}  ${standardFieldSpecs.retireDate.imp?:''}">
                                                <tds-date-control
													[tabindex]="24"
													[(ngModel)]="model.asset.retireDate"
                                                    class="tm-input-control"
                                                    name="modelAssetRetireDate"
                                                    [value]="model.asset.retireDate">
                                                </tds-date-control>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${assetInstance.validation}"  />
											<td data-for="validation" class="${standardFieldSpecs.validation.imp?:''}">
												<kendo-dropdownlist
													[tabIndex]="33"
													class="tm-input-control"
													name="modelAssetValidation"
													[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}"
													[(ngModel)]="model.asset.validation">
												</kendo-dropdownlist>
											</td>
										<tdsAngular:inputLabelAndField field="${standardFieldSpecs.testProc}" value="${assetInstance.testProc}" tabindex="42"  ngmodel="model.asset.testProc" blankOptionListText="?" />
										</tr>
										<tr>
											<td></td>
											<td></td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetInstance.maintExpDate}"/>
											<td data-for="maintExpDate" valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}  ${standardFieldSpecs.maintExpDate.imp?:''} ">
                                                <tds-date-control
                                                    class="tm-input-control"
													[(ngModel)]="model.asset.maintExpDate"
                                                    name="modelAssetMaintExpDate"
                                                    [tabindex]="25"
                                                    [value]="model.asset.maintExpDate">
                                                </tds-date-control>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.latency}" value="${assetInstance.latency}" tabindex="34"  ngmodel="model.asset.latency" blankOptionListText="?" />
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupProc}" value="${assetInstance.startupProc}" tabindex="43" ngmodel="model.asset.startupProc" blankOptionListText="?" />
										</tr>
										<tr>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}" value="${assetInstance.url}" ngmodel="model.asset.url" tabindex="17"/>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="26"/>
											<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}"
												[ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetInstance}" fieldName="shutdownBy" /> }" nowrap="nowrap">
												<label for="shutdownBy">
													<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
														${standardFieldSpecs.shutdownBy.label}
													</tdsAngular:tooltipSpan>
												</label>
											</td>
											<td class="tm-input-control ${standardFieldSpecs.shutdownBy.imp?:''}" data-for="shutdownBy" nowrap="nowrap" tabindex="35">
												<tds-combobox-group
														[model]="model.asset.shutdownBy"
														(modelChange)="model.asset.shutdownBy.id = $event"
														(isFixedChange)="model.asset.shutdownFixed = $event"
														[isFixed]="${assetInstance.shutdownFixed}"
														[namedStaff]="${personList as JSON}"
														[team]="${availableRoles as JSON}">
												</tds-combobox-group>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${assetInstance.shutdownDuration}"/>
											<td class="tm-input-control duration-container">
												<input type="text" id="shutdownDuration" name="shutdownDuration" tabindex="44"
													   class="${standardFieldSpecs.shutdownDuration.imp?:''} duration"
													   [(ngModel)]="model.asset.shutdownDuration" size="7"/>
												<label>m</label>
											</td>
										</tr>
										<tr>
											<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${assetInstance.startupBy}"  />
											<td colspan="1" nowrap="nowrap" data-for="startupBy"
												tabindex="18"
												class="tm-input-control ${standardFieldSpecs.startupBy.imp?:''}">
												<tds-combobox-group
														[model]="model.asset.startupBy"
														(modelChange)="model.asset.startupBy.id = $event"
														(isFixedChange)="model.asset.startupFixed = $event"
														[isFixed]="${assetInstance.startupFixed}"
														[namedStaff]="${personList as JSON}"
														[team]="${availableRoles as JSON}"></tds-combobox-group>
											</td>
											<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${assetInstance.startupDuration}" ngmodel="model.asset.startupDuration" tabindex="27"/>
											<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${assetInstance.testingBy}"/>
											<td colspan="1" nowrap="nowrap" data-for="testingBy" class="tm-input-control ${standardFieldSpecs.testingBy.imp?:''}" tabindex="36">
												<tds-combobox-group
														[model]="model.asset.testingBy"
														(modelChange)="model.asset.testingBy.id = $event"
														(isFixedChange)="model.asset.testingFixed = $event"
														[isFixed]="${assetInstance.testingFixed}"
														[namedStaff]="${personList as JSON}"
														[team]="${availableRoles as JSON}">
                                                </tds-combobox-group>
											</td>
											<tdsAngular:inputLabel field="${standardFieldSpecs.testingDuration}" value="${assetInstance.testingDuration}"/>
											<td class="tm-input-control duration-container">
												<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}
												duration" name="testingDuration" [(ngModel)]="model.asset.testingDuration" tabindex="45"  size="7"/>
												<label>m</label>
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
						<tds-supports-depends (initDone)="onInitDependenciesDone($event)"  [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
					</tr>
				</table>
			</form>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-primary pull-left component-action-update" tabindex="501"  type="button" (click)="submitForm($event)"  [disabled]="!isDependenciesValidForm"><span class="fa fa-fw fa-floppy-o"></span> Create</button>

		<button class="btn btn-default pull-right component-action-cancel" tabindex="502" (click)="onCancelEdit()" type="button"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>
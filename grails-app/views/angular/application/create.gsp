<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.asset.AssetType"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<g:set var="assetClass" value="Application" />
<%@page import="grails.converters.JSON"%>

<div tds-autocenter tds-handle-escape (escPressed)="onCloseEdit()"
	 class="tds-modal-content has-side-nav tds-angular-component-content">
	<div class="modal-header">
		<tds-button-close aria-label="Close" class="close" icon="close" [flat]="true" (click)="onCloseEdit()"></tds-button-close>
		<h4 class="modal-title">Application Create</h4>
		<tds-tab-scroller>
			<tds-scroller-item>
				<button tdsScrollerLink>Details</button>
			</tds-scroller-item>
			<tds-scroller-item>
				<button tdsScrollerLink>Supports/Depends</button>
			</tds-scroller-item>
		</tds-tab-scroller>
	</div>
	<div class="modal-body asset-crud no-description" tdsScrollContainer style="position: relative">
		<form
			name="form"
			(ngSubmit)="form.form.valid && onCreate()"
			class="asset-entry-form"
			[ngClass]="{'form-submitted': form && form.submitted}"
			role="form"
			#form="ngForm"
			novalidate>
			<div tdsScrollSection class="grid-form three-column">
				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance.assetName}"/>
					<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${assetInstance.assetName}" ngmodel="model.asset.assetName"/>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${assetInstance.sme}"/>
                    <kendo-dropdownlist #controlSME1
                                        [filterable]="true"
                                        (filterChange)="filterSME1Change($event)"
                                        (focus)="focusSME1()"
                                        (close)="onClose($event, controlSME1)"
                                        [tabIndex]="20"
                                        class="tm-input-control person-list"
                                        name="modelAssetSme"
                                        [(ngModel)]="persons.sme"
                                        (valueChange)="onAddPerson($event,'application', 'sme',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON}, 'sme1PersonList', controlSME1)"
                                        [defaultItem]="defaultItem"
                                        [textField]="'fullName'"
                                        [valueField]="'personId'"
                                        [data]="model.sme1PersonList">
                    </kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${assetInstance.environment}" tabindex="9" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${assetInstance.userLocations}" ngmodel="model.asset.userLocations" tabindex="10" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${assetInstance.appTech}" ngmodel="model.asset.appTech" tabindex="11"/>

				<div class="clr-form-control">
					<div style="display: flex">
						<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${assetInstance.sme2}"/>
						<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap With SME2"></div>
					</div>
                <kendo-dropdownlist  #controlSME2
                                     [filterable]="true"
                                     (filterChange)="filterSME2Change($event)"
                                     (focus)="focusSME2()"
                                     (close)="onClose($event, controlSME2)"
                                     [tabIndex]="21"
                                     class="tm-input-control person-list"
                                     name="modelAssetSme2"
                                     [(ngModel)]="persons.sme2"
                                     (valueChange)="onAddPerson($event,'application', 'sme2',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON}, 'sme2PersonList', controlSME2)"
                                     [defaultItem]="defaultItem"
                                     [textField]="'fullName'"
                                     [valueField]="'personId'"
                                     [data]="model.sme2PersonList">
                </kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.criticality}" value="${assetInstance.criticality}" tabindex="13"  ngmodel="model.asset.criticality"  blankOptionListText="Please Select..."/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"  value="${assetInstance.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="14" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${assetInstance.appSource}" ngmodel="model.asset.appSource" tabindex="15"/>

				<div class="clr-form-control">
				<div style="display: flex">
					<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${assetInstance.appOwner}"/>
					<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')" title="Swap With App Owner"></div>
				</div>
                <kendo-dropdownlist #controlAppOwner
                                    [filterable]="true"
                                    (filterChange)="filterAppOwnerChange($event)"
                                    (focus)="focusAppOwner()"
                                    (close)="onClose($event, controlAppOwner)"
                                    [tabIndex]="22"
                                    class="tm-input-control"
                                    name="modelAssetappOwner"
                                    [(ngModel)]="persons.appOwner"
                                    (valueChange)="onAddPerson($event,'application', 'appOwner',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON}, 'appOwnerPersonList', controlAppOwner)"
                                    [defaultItem]="defaultItem"
                                    [textField]="'fullName'"
                                    [valueField]="'personId'"
                                    [data]="model.appOwnerPersonList">
                </kendo-dropdownlist>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance.moveBundle?.id}"/>
					<kendo-dropdownlist
						[tabIndex]="17"
						name="modelAssetMoveBundle"
						[data]="moveBundleList"
						[(ngModel)]="model.asset.moveBundle"
						[textField]="'name'"
						[valueField]="'id'">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${assetInstance.drRpoDesc}"  ngmodel="model.asset.drRpoDesc" tabindex="18" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.license}" value="${assetInstance.license}" ngmodel="model.asset.license" tabindex="19"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${assetInstance.businessUnit}" ngmodel="model.asset.businessUnit" tabindex="20"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance.planStatus}"/>
					<kendo-dropdownlist
						[tabIndex]="21"
						name="modelAssetPlanStatus"
						[(ngModel)]="model.asset.planStatus"
						[data]="model.planStatusOptions">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${assetInstance.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="22" tooltipDataPlacement="bottom"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetInstance.retireDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.retireDate"
						name="modelAssetRetireDate"
						[tabindex]="15"
						[value]="model.asset.retireDate">
					</tds-date-control>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${assetInstance.validation}"/>
					<kendo-dropdownlist
							[tabIndex]="24"
							name="modelAssetValidation"
							[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}"
							[(ngModel)]="model.asset.validation">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.testProc}" value="${assetInstance.testProc}" tabindex="25"  ngmodel="model.asset.testProc" blankOptionListText="?" />

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetInstance.maintExpDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.maintExpDate"
						name="modelAssetMaintExpDate"
						[tabindex]="26"
						[value]="model.asset.maintExpDate">
					</tds-date-control>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.latency}" value="${assetInstance.latency}" tabindex="27"  ngmodel="model.asset.latency" blankOptionListText="?" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupProc}" value="${assetInstance.startupProc}" tabindex="28" ngmodel="model.asset.startupProc" blankOptionListText="?" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}" value="${assetInstance.url}" ngmodel="model.asset.url" tabindex="29"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="30"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${assetInstance.shutdownBy}"/>
					<tds-combobox-group
							[model]="model.asset.shutdownBy"
							(modelChange)="model.asset.shutdownBy.id = $event"
							(isFixedChange)="model.asset.shutdownFixed = $event"
							[isFixed]="${assetInstance.shutdownFixed}"
							[namedStaff]="${personList as JSON}"
							tabindex="31"
							[team]="${availableRoles as JSON}">
					</tds-combobox-group>
				</div>

				<tdsAngular:inputLabelAndField
						field="${standardFieldSpecs.shutdownDuration}"
						value="${assetInstance.shutdownDuration}"
						ngmodel="model.asset.shutdownDuration"
						tabindex="32"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${assetInstance.startupBy}"/>
					<tds-combobox-group
						[model]="model.asset.startupBy"
						(modelChange)="model.asset.startupBy.id = $event"
						(isFixedChange)="model.asset.startupFixed = $event"
						[isFixed]="${assetInstance.startupFixed}"
						[namedStaff]="${personList as JSON}"
						[team]="${availableRoles as JSON}"
						tabindex="33">
					</tds-combobox-group>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${assetInstance.startupDuration}" ngmodel="model.asset.startupDuration" tabindex="34"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${assetInstance.testingBy}"/>
					<tds-combobox-group
							[model]="model.asset.testingBy"
							(modelChange)="model.asset.testingBy.id = $event"
							(isFixedChange)="model.asset.testingFixed = $event"
							[isFixed]="${assetInstance.testingFixed}"
							[namedStaff]="${personList as JSON}"
							[team]="${availableRoles as JSON}"
							tabindex="35">
					</tds-combobox-group>
				</div>

				<tdsAngular:inputLabelAndField
						field="${standardFieldSpecs.testingDuration}"
						value="${assetInstance.testingDuration}"
						ngmodel="model.asset.testingDuration"
						tabindex="36"/>

				<g:render template="/angular/common/customEdit" model="[assetEntityInstance:assetInstance]"></g:render>
			</div>
			<g:render template="/angular/common/assetTagsEdit"></g:render>

			<tds-supports-depends tdsScrollSection (initDone)="onInitDependenciesDone($event)"  [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
		</form>
	</div>
	<div class="modal-sidenav form-group-center">
		<nav class="modal-sidenav btn-link">
			<tds-button-edit
			(click)="submitForm($event)"
			tooltip="Create"
			icon="floppy"
			tabindex="501"
			[disabled]="!isDependenciesValidForm"></tds-button-edit>
			<tds-button-custom
			(click)="onCancelEdit()"
			tooltip="Cancel"
			tabindex="502"
			icon="ban"></tds-button-custom>
		</nav>
	</div>
</div>

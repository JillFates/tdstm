<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.asset.AssetType"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<g:set var="assetClass" value="Application" />
<%@page import="grails.converters.JSON"%>

<div>
	<div>
		<div class="clr-row tab-scroll-container" [ngClass]="{'has-description': ${!!asset.description?.trim()}}">
			<div class="clr-col-11">
				<tds-tab-scroller>
					<tds-scroller-item>
						<button tdsScrollerLink>Details</button>
					</tds-scroller-item>
					<tds-scroller-item>
						<button tdsScrollerLink>Supports/Depends</button>
					</tds-scroller-item>
				</tds-tab-scroller>
			</div>
		</div>
	</div>
	<div class="asset-crud" [ngClass]="{'has-description': ${!!asset.description?.trim()}}" tdsScrollContainer style="position: relative">
		<form
			name="form"
			(ngSubmit)="form.form.valid && onUpdate()"
			class="asset-entry-form"
			[ngClass]="{'form-submitted': form && form.submitted}"
			role="form"
			#form="ngForm"
			novalidate>
			<div tdsScrollSection class="grid-form three-column">
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.assetName}" value="${asset.assetName}" ngmodel="model.asset.assetName" tabindex="1"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.description}" value="${asset.description}" ngmodel="model.asset.description" tabindex="2"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}" ngmodel="model.asset.appVendor" tabindex="3"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}" ngmodel="model.asset.supportType" tabindex="4"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}" ngmodel="model.asset.appFunction" tabindex="5"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${asset.userCount}"  ngmodel="model.asset.userCount" tabindex="6" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}" ngmodel="model.asset.appVersion" tabindex="7"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
                <kendo-dropdownlist #controlSME1
                                    [filterable]="true"
                                    (filterChange)="filterSME1Change($event)"
                                    (focus)="focusSME1()"
                                    (close)="onClose($event, controlSME1)"
                                    [tabIndex]="11"
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

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="9" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" ngmodel="model.asset.userLocations" tabindex="10" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${asset.appTech}" ngmodel="model.asset.appTech" tabindex="11"/>

				<div class="clr-form-control">
					<div style="display: flex">
						<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
						<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap With SME2"></div>
					</div>
                <kendo-dropdownlist  #controlSME2
                                     [filterable]="true"
                                     (filterChange)="filterSME2Change($event)"
                                     (focus)="focusSME2()"
                                     (close)="onClose($event, controlSME2)"
                                     [tabIndex]="12"
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

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.criticality}" value="${asset.criticality}" tabindex="13"  ngmodel="model.asset.criticality"  blankOptionListText="Please Select..."/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"  value="${asset.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="14" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${asset.appSource}" ngmodel="model.asset.appSource" tabindex="15"/>

				<div class="clr-form-control">
					<div style="display: flex">
						<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
						<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')" title="Swap With App Owner"></div>
					</div>
                <kendo-dropdownlist #controlAppOwner
                                    [filterable]="true"
                                    (filterChange)="filterAppOwnerChange($event)"
                                    (focus)="focusAppOwner()"
                                    (close)="onClose($event, controlAppOwner)"
                                    [tabIndex]="13"
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
					<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
					<kendo-dropdownlist
						[tabIndex]="17"
						name="modelAssetMoveBundle"
						[data]="model.moveBundleList"
						[(ngModel)]="model.asset.moveBundle"
						[textField]="'name'"
						[valueField]="'id'">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${asset.drRpoDesc}"  ngmodel="model.asset.drRpoDesc" tabindex="18" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.license}" value="${asset.license}" ngmodel="model.asset.license" tabindex="19"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${asset.businessUnit}" ngmodel="model.asset.businessUnit" tabindex="20"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
					<kendo-dropdownlist
						[tabIndex]="21"
						name="modelAssetPlanStatus"
						[(ngModel)]="model.asset.planStatus"
						[data]="model.planStatusOptions">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="22" tooltipDataPlacement="bottom"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.retireDate"
						name="modelAssetRetireDate"
						[tabindex]="15"
						[value]="model.asset.retireDate">
					</tds-date-control>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
					<kendo-dropdownlist
							[tabIndex]="24"
							name="modelAssetValidation"
							[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
							[(ngModel)]="model.asset.validation">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.testProc}" value="${asset.testProc}" tabindex="25"  ngmodel="model.asset.testProc" blankOptionListText="?" />

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.maintExpDate"
						name="modelAssetMaintExpDate"
						[tabindex]="26"
						[value]="model.asset.maintExpDate">
					</tds-date-control>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.latency}" value="${asset.latency}" tabindex="27"  ngmodel="model.asset.latency" blankOptionListText="?" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}" tabindex="28" ngmodel="model.asset.startupProc" blankOptionListText="?" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}" value="${asset.url}" ngmodel="model.asset.url" tabindex="29"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="30"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${asset.shutdownBy}"/>
					<tds-combobox-group
							[model]="model.asset.shutdownBy"
							(modelChange)="model.asset.shutdownBy.id = $event"
							(isFixedChange)="model.asset.shutdownFixed = $event"
							[isFixed]="${asset.shutdownFixed}"
							[namedStaff]="${personList as JSON}"
							tabindex="31"
							[team]="${availableRoles as JSON}">
					</tds-combobox-group>
				</div>

				<tdsAngular:inputLabelAndField
						field="${standardFieldSpecs.shutdownDuration}"
						value="${asset.shutdownDuration}"
						ngmodel="model.asset.shutdownDuration" tabindex="32"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
					<tds-combobox-group
						[model]="model.asset.startupBy"
						(modelChange)="model.asset.startupBy.id = $event"
						(isFixedChange)="model.asset.startupFixed = $event"
						[isFixed]="${asset.startupFixed}"
						[namedStaff]="${personList as JSON}"
						[team]="${availableRoles as JSON}"
						tabindex="33">
					</tds-combobox-group>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}" ngmodel="model.asset.startupDuration" tabindex="34"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
					<tds-combobox-group
							[model]="model.asset.testingBy"
							(modelChange)="model.asset.testingBy.id = $event"
							(isFixedChange)="model.asset.testingFixed = $event"
							[isFixed]="${asset.testingFixed}"
							[namedStaff]="${personList as JSON}"
							[team]="${availableRoles as JSON}"
							tabindex="35">
					</tds-combobox-group>
				</div>

				<tdsAngular:inputLabelAndField
						field="${standardFieldSpecs.testingDuration}"
						value="${asset.testingDuration}"
						ngmodel="model.asset.testingDuration" tabindex="36"/>

				<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>
			</div>
			<g:render template="/angular/common/assetTagsEdit"></g:render>

			<tds-supports-depends tdsScrollSection (initDone)="onInitDependenciesDone($event)"  [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
		</form>
	</div>
</div>

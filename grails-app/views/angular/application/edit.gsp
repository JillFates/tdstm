<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.asset.AssetType"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<g:set var="assetClass" value="Application" />
<%@page import="grails.converters.JSON"%>

<div tds-autocenter tds-handle-escape (escPressed)="onCancelEdit()"
	 class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<tds-button-close aria-label="Close" class="close" icon="close" [flat]="true" (click)="onCancelEdit()"></tds-button-close>
		<h4 class="modal-title">Application Edit</h4>
	</div>
	<div class="modal-body">
		<form 
			clrForm 
			name="form" 
			(ngSubmit)="form.form.valid && onUpdate()"
			class="asset-entry-form"
			[ngClass]="{'form-submitted': form && form.submitted}"
			role="form" 
			#form="ngForm" 
			novalidate
			clrLayout="vertical">
			<div class="grid-form">
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.assetName}" value="${asset.assetName}" ngmodel="model.asset.assetName" tabindex="1"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.description}" value="${asset.description}" ngmodel="model.asset.description" size="50" tabindex="2"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}" ngmodel="model.asset.appVendor" tabindex="3"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}" ngmodel="model.asset.supportType" tabindex="4"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}" ngmodel="model.asset.appFunction" tabindex="5"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${asset.userCount}"  ngmodel="model.asset.userCount" tabindex="6" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}" ngmodel="model.asset.appVersion" tabindex="7"/>
				
				<%-- TODO: Come back and style this to look like clarity --%>
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
					<kendo-dropdownlist #controlSme
							[tabIndex]="8"
							name="modelAssetSme"
							[(ngModel)]="persons.sme"
							(selectionChange)="onAddPerson($event,'application', 'sme',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON})"
							[defaultItem]="defaultItem"
							[textField]="'fullName'"
							[valueField]="'personId'"
							[data]="getPersonList(${personList as JSON})">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="9" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" ngmodel="model.asset.userLocations" tabindex="10" tooltipDataPlacement="bottom"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${asset.appTech}" ngmodel="model.asset.appTech" tabindex="11"/>
				
				<%-- TODO: Come back and style this. --%>
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
					<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap Contacts"></div>
					<kendo-dropdownlist  #controlSme2
						[tabIndex]="12"
						name="modelAssetSme2"
						[(ngModel)]="persons.sme2"
						(selectionChange)="onAddPerson($event,'application', 'sme2',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON})"
						[defaultItem]="defaultItem"
						[textField]="'fullName'"
						[valueField]="'personId'"
						[data]="getPersonList(${personList as JSON})">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.criticality}" value="${asset.criticality}" tabindex="13"  ngmodel="model.asset.criticality"  blankOptionListText="Please Select..."/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"  value="${asset.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="14" tooltipDataPlacement="bottom"/>
				
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${asset.appSource}" ngmodel="model.asset.appSource" tabindex="15"/>
				
				<%-- TODO: Come back and style this. --%>
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
					<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')" title="Swap Contacts"></div>
					<kendo-dropdownlist
							[tabIndex]="16"
							name="modelAssetappOwner"
							[(ngModel)]="persons.appOwner"
							(selectionChange)="onAddPerson($event,'application', 'appOwner',${partyGroupList as JSON}, ${availableRoles as JSON}, ${staffTypes as JSON})"
							[defaultItem]="defaultItem"
							[textField]="'fullName'"
							[valueField]="'personId'"
							[data]="getPersonList(${personList as JSON})">
					</kendo-dropdownlist>
				</div>

				<%-- TODO: Come back and style this. --%>
				<div>
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
				
				<%-- TODO: Come back and style this --%>
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
					<kendo-dropdownlist
						[tabIndex]="21"
						name="modelAssetPlanStatus"
						[(ngModel)]="model.asset.planStatus"
						[data]="model.planStatusOptions">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="22" tooltipDataPlacement="bottom"/>

				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.retireDate"
						name="modelAssetRetireDate"
						[tabindex]="15"
						[value]="model.asset.retireDate">
					</tds-date-control>
				</div>

				<%-- TODO: Style this --%>
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
					<kendo-dropdownlist
							[tabIndex]="24"
							name="modelAssetValidation"
							[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
							[(ngModel)]="model.asset.validation">
					</kendo-dropdownlist>
				</div>

				<%-- TODO: Fix proc: see existing application for example --%>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.testProc}" value="${asset.testProc}" tabindex="25"  ngmodel="model.asset.testProc" blankOptionListText="?" />
				
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.maintExpDate"
						name="modelAssetMaintExpDate"
						[tabindex]="26"
						[value]="model.asset.maintExpDate">
					</tds-date-control>
				</div>

				<%-- TODO: Fix latency: see existing application for example: ? yes no --%>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.latency}" value="${asset.latency}" tabindex="27"  ngmodel="model.asset.latency" blankOptionListText="?" />
				
				<%-- TODO: Fix proc: see existing application for example --%>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}" tabindex="28" ngmodel="model.asset.startupProc" blankOptionListText="?" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}" value="${asset.url}" ngmodel="model.asset.url" tabindex="29"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="30"/>
				
				<%-- TODO: Style/fix this --%>
				<div>
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

				<%-- TODO: Style/fix this --%>
				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}"/>
					<input type="text" id="shutdownDuration" name="shutdownDuration" tabindex="32"
							class="${standardFieldSpecs.shutdownDuration.imp?:''} duration"
							[(ngModel)]="model.asset.shutdownDuration" size="7"/>
					<span>m</span>
				</div>

				<%-- TODO: Style/fix this --%>
				<div>
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

				<%-- TODO: Fix this --%>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}" ngmodel="model.asset.startupDuration" tabindex="34"/>

				<div>
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

				<div>
					<tdsAngular:inputLabel field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}"/>
					<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}
					duration" name="testingDuration" [(ngModel)]="model.asset.testingDuration" tabindex="36"  size="7"/>
					<span>m</span>
				</div>

				<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>		
			</div>	
			<g:render template="/angular/common/assetTagsEdit"></g:render>
			<tds-supports-depends (initDone)="onInitDependenciesDone($event)"  [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>	
		</form>
	</div>
	<div class="modal-sidenav form-group-center">
		<nav class="modal-sidenav btn-link">
			<tds-button-save 
				(click)="submitForm($event)" 
				[disabled]="!isDependenciesValidForm" 
				[permissions]="['${Permission.AssetEdit}']" 
				tooltip="Save" 
				icon="floppy"
				tabindex="501">
			</tds-button-save> 
			<tds:hasPermission permission="${Permission.AssetDelete}">
			<tds-button-delete
					tooltip="Delete Asset"
					class="btn-danger"
					[permissions]="['${Permission.AssetDelete}']"
					(click)="onDeleteAsset()"
						tabindex="502">
			</tds-button-delete>
			</tds:hasPermission>
			<tds-button-cancel
					tooltip="Cancel Edit"
					tabindex="503"
					(click)="onCancelEdit()">
			</tds-button-cancel>
		</nav>
	</div>
</div>
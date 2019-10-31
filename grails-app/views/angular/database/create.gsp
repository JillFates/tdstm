<%@page import="net.transitionmanager.asset.Database"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="Database" />

<div tds-autocenter tds-handle-escape (escPressed)="onCancelEdit()" class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Database Create</h4>
		<tds-tab-scroller>
			<tds-scroller-item>
				<button tdsScrollerLink>Details</button>
			</tds-scroller-item>
			<tds-scroller-item>
				<button tdsScrollerLink>Supports/Depends</button>
			</tds-scroller-item>
		</tds-tab-scroller>
	</div>
	<div class="modal-body" tdsScrollContainer style="position: relative">
		<form 
			clrForm
			name="form" 
			(ngSubmit)="form.form.valid && onCreate()"
			class="asset-entry-form"
			[ngClass]="{'form-submitted': form && form.submitted}"
			role="form" 
			#form="ngForm" 
			novalidate
			clrLayout="vertical">
			<div tdsScrollSection class="grid-form">
				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance?.assetName}"/>
					<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${assetInstance.assetName}" ngmodel="model.asset.assetName"/>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetInstance?.description}"/>
					<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="2" value="${assetInstance.description}" ngmodel="model.asset.description" />
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${assetInstance.dbFormat}" tabindex="3" ngmodel="model.asset.dbFormat"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetInstance?.supportType}" ngmodel="model.asset.supportType" tabIndex="4"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${assetInstance.environment}" tabindex="5" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
				
				<div class="clr-form-control">
					<label class="${standardFieldSpecs.size.imp?:''}">
						${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
					</label>
					<tdsAngular:inputControl field="${standardFieldSpecs.size}" size="4" tabindex="6" value="${assetInstance.size}" ngmodel="model.asset.size"/>
					<kendo-dropdownlist
						[tabIndex]="7"
						class="tm-input-control"
						name="modelAssetScaleName"
						[data]="${SizeScale.getAsJsonList() as JSON}"
						[(ngModel)]="model.asset.scale"
						[textField]="'text'"
						[valueField]="'value'">
					</kendo-dropdownlist>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetInstance?.retireDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.retireDate"
						class="tm-input-control"
						name="modelAssetRetireDate"
						[tabindex]="8"
						[value]="model.asset.retireDate">
					</tds-date-control>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance.dependencyBundle?.value}"/>
					<kendo-dropdownlist
						[tabIndex]="9"
						class="tm-input-control"
						name="modelAssetMoveBundle"
						[data]="model.moveBundleList"
						[(ngModel)]="model.asset.moveBundle"
						[textField]="'name'"
						[valueField]="'id'">
					</kendo-dropdownlist>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${assetInstance?.rateOfChange}"/>
					<tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" tabindex="10" value="${assetInstance.rateOfChange}" ngmodel="model.asset.rateOfChange"/>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetInstance?.maintExpDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.maintExpDate"
						class="tm-input-control"
						name="modelAssetMainExpDate"
						[tabindex]="11"
						[value]="model.asset.maintExpDate">
					</tds-date-control>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance?.planStatus}"/>
					<kendo-dropdownlist
						[tabIndex]="12"
						class="tm-input-control"
						name="modelAssetPlanStatus"
						[data]="model.planStatusOptions"
						[(ngModel)]="model.asset.planStatus">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="13"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${assetInstance?.validation}"/>
					<kendo-dropdownlist
							[tabIndex]="14"
							class="tm-input-control"
							name="modelAssetValidation"
							[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}"
							[(ngModel)]="model.asset.validation">
					</kendo-dropdownlist>
				</div>

				<g:render template="/angular/common/customEdit" model="[assetEntityInstance:assetInstance]"></g:render>                  
			</div>
			<g:render template="/angular/common/assetTagsEdit"></g:render>
					
			<tds-supports-depends tdsScrollSection (initDone)="onInitDependenciesDone($event)" [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
		</form>
	</div>
	<div class="modal-sidenav form-group-center">
		<nav class="modal-sidenav btn-link">
			<tds-button-edit 
				(click)="submitForm($event)" 
				tooltip="Create" 
				icon="floppy"
				tabindex="501"
				[disabled]="!isDependenciesValidForm">
			</tds-button-edit>
			<tds-button-custom 
				(click)="onCancelEdit()" 
				tooltip="Cancel" 
				tabindex="502"
				icon="ban">
			</tds-button-custom>
		</nav>
	</div>
</div>
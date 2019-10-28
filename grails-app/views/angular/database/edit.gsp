<%@page import="net.transitionmanager.asset.Database"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="Database" />

<div tds-autocenter tds-handle-escape (escPressed)="onCancelEdit()"
	 class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Database Edit</h4>
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
			<kendo-tabstrip [keepTabContent]="true">
				<kendo-tabstrip-tab [title]="'Details'" [selected]="true">
					<ng-template kendoTabContent>
						<div class="grid-form">
							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
								<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${asset.assetName}" ngmodel="model.asset.assetName"/>
							</div>

							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
								<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="2" value="${asset.description}" ngmodel="model.asset.description" />
							</div>

							<tdsAngular:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}" tabindex="3" ngmodel="model.asset.dbFormat"/>
							<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset?.supportType}" ngmodel="model.asset.supportType" tabindex="4" />
							<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="5" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
							
							<div class="clr-form-control">
								<label>
									${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
								</label>
								<tdsAngular:inputControl field="${standardFieldSpecs.size}" size="4" tabindex="6" value="${asset.size}" ngmodel="model.asset.size"/>
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
								<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>	
								<tds-date-control
									[(ngModel)]="model.asset.retireDate"
									class="tm-input-control"
									name="modelAssetRetireDate"
									[tabindex]="8"
									[value]="model.asset.retireDate">
								</tds-date-control>
							</div>

							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.dependencyBundle?.value}"/>
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

							<%-- Can combine? --%>
							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset?.rateOfChange}"/>
								<tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" tabindex="10" value="${asset.rateOfChange}" ngmodel="model.asset.rateOfChange"/>
							</div>

							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
								<tds-date-control
									[(ngModel)]="model.asset.maintExpDate"
									class="tm-input-control"
									name="modelAssetMainExpDate"
									[tabindex]="11"
									[value]="model.asset.maintExpDate">
								</tds-date-control>
							</div>
							
							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
								<kendo-dropdownlist
										[tabIndex]="12"
										class="tm-input-control"
										name="modelAssetPlanStatus"
										[data]="model.planStatusOptions"
										[(ngModel)]="model.asset.planStatus">
								</kendo-dropdownlist>
							</div>

							<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="13"/>

							<div class="clr-form-control">
								<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
								<kendo-dropdownlist
										[tabIndex]="14"
										class="tm-input-control"
										name="modelAssetValidation"
										[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
										[(ngModel)]="model.asset.validation">
								</kendo-dropdownlist>
							</div>

							<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>                       
						</div>

						<g:render template="/angular/common/assetTagsEdit"></g:render>
					</ng-template>
				</kendo-tabstrip-tab>

				<kendo-tabstrip-tab [title]="'Supports / Depends'">
					<ng-template kendoTabContent>
						<tds-supports-depends (initDone)="onInitDependenciesDone($event)" [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
					</ng-template>
				</kendo-tabstrip-tab>
			</kendo-tabstrip>
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
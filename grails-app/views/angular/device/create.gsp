<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>
<%@page import="net.transitionmanager.asset.AssetType"%>
<%@page import="net.transitionmanager.security.Permission"%>

<%-- Set some vars based on the action being save or update --%>
<g:set var="actionLabel" value="${action == 'save' ? 'Save' : 'Update'}" />
<g:set var="jsAction" value="${action=='save' ? 'saveToShow' : 'performAssetUpdate'}" />

<div tds-autocenter tds-handle-escape (escPressed)="onCancelEdit()"
     class="tds-modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span  aria-hidden="true">×</span></button>
        <h4 class="modal-title">Device Create</h4>
    </div>
    <div class="modal-body">
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
            <kendo-tabstrip [keepTabContent]="true">
                <kendo-tabstrip-tab [title]="'Details'" [selected]="true">
                    <ng-template kendoTabContent>
                        <div class="grid-form">
                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance.assetName}"/>
                                <tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${assetInstance.assetName}" ngmodel="model.asset.assetName"/>
                            </div>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetInstance.description}"/>
                                <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="2" value="${assetInstance.description}" ngmodel="model.asset.description"/>
                            </div>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.assetType}" value=""/>
                                <tds-combobox
                                    [(model)]="model.asset.assetTypeSelectValue"
                                    [serviceRequest]="searchAssetTypes"
                                    [searchOnScroll]="false"
                                    [reloadOnOpen]="true"
                                    (valueChange)="onAssetTypeValueChange($event)"
                                    tabindex="3">
                                </tds-combobox>
                            </div>

                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${assetInstance.environment}" tabindex="4" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
                            
                            <div class="clr-form-control">
                                <label for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip ?: standardFieldSpecs.manufacturer.label}">
                                    <a *ngIf="model.asset.manufacturer && model.asset.manufacturer.id" href='javascript:showManufacturer(${assetInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a>
                                    <label *ngIf="!model.asset.manufacturer">Manufacturer</label>
                                </label>
                                <tds-combobox
                                    [(model)]="model.asset.manufacturerSelectValue"
                                    [serviceRequest]="searchManufacturers"
                                    [searchOnScroll]="false"
                                    [reloadOnOpen]="true"
                                    (valueChange)="onManufacturerValueChange($event)"
                                    tabIndex="6">
                                </tds-combobox>
                            </div>
                            
                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.priority}" value="${assetInstance.priority}" tabindex="7" blankOptionListText="Please Select..." ngmodel="model.asset.priority" />
                            
                            <div class="clr-form-control">
                                <label>Source</label>
                                <label for="locationSourceId">Location/Room</label>
                                <kendo-dropdownlist
                                        [tabIndex]="5"
                                        class="tm-input-control"
                                        name="roomSourceId"
                                        [(ngModel)]="model.asset.roomSource"
                                        [data]="model.sourceRoomSelect"
                                        [textField]="'value'"
                                        [valueField]="'id'"
                                        (valueChange)="onRoomSourceValueChange($event)">
                                </kendo-dropdownlist>
                                <span *ngIf="model.asset.roomSource && model.asset.roomSource.id === -1" class="newRoomS">
                                    <tdsAngular:inputControl field="${standardFieldSpecs.locationSource}" size="10" tabindex="8" placeholder="Location" ngmodel="model.asset.locationSource"></tdsAngular:inputControl>
                                    <tdsAngular:inputControl field="${standardFieldSpecs.roomSource}" size="10" tabindex="9" placeholder="Room Name" ngmodel="model.asset.newRoomSource"></tdsAngular:inputControl>
                                </span>

                                <div *ngIf="showRackFields">
                                    <label>Rack/Cabinet</label>
                                    <div *ngIf="showRackSourceInput === 'select'">
                                        <kendo-dropdownlist
                                                [tabIndex]="10"
                                                class="tm-input-control useRackS"
                                                name="modelAssetRackSource"
                                                [(ngModel)]="model.asset.rackSource"
                                                [data]="rackSourceOptions"
                                                [textField]="'value'"
                                                [valueField]="'id'">
                                        </kendo-dropdownlist>
                                        <span *ngIf="model.asset.rackSource && model.asset.rackSource.id === -1">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rackSource}" size="20" tabindex="11" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackSource"></tdsAngular:inputControl>
                                        </span>
                                    </div>

                                    <div *ngIf="showRackSourceInput === 'new'">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="12" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackSource"></tdsAngular:inputControl>
                                    </div>
                                </div>

                                <div *ngIf="showBladeFields">
                                    <label>Blade Chassis</label>
                                    <div *ngIf="showBladeSourceInput === 'select'">
                                        <kendo-dropdownlist
                                            class="tm-input-control"
                                            name="modelAssetBladeSource"
                                            [(ngModel)]="model.asset.sourceChassis"
                                            [data]="bladeSourceOptions"
                                            [textField]="'value'"
                                            [valueField]="'id'">
                                        </kendo-dropdownlist>
                                    </div>
                                    <div *ngIf="model.asset.sourceChassis && model.asset.sourceChassis.id > 0" >
                                        <tdsAngular:inputControl field="${standardFieldSpecs.sourceBladePosition}" size="10" tabindex="13" value="${assetInstance.sourceBladePosition}" ngmodel="model.asset.sourceBladePosition"></tdsAngular:inputControl>
                                    </div>
                                </div>

                                <div *ngIf="showBladeFields || showRackFields">
                                    <label>Position</label>
                                    <div *ngIf="showRackFields">
                                        <div *ngIf="(showRackSourceInput === 'new' || (model.asset.rackSource && (model.asset.rackSource.id === -1 || model.asset.rackSource.id > 0)))" >
                                            <tdsAngular:inputControl field="${standardFieldSpecs.sourceRackPosition}" size="10" tabindex="14" placeholder="U Position" value="${assetInstance.sourceRackPosition}" ngmodel="model.asset.sourceRackPosition"></tdsAngular:inputControl>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="clr-form-control">
                                <label>Target</label>
                                <label for="locationSourceId">Location/Room</label>
                                <kendo-dropdownlist
                                    [tabIndex]="15"
                                    class="tm-input-control"
                                    name="roomTargetId"
                                    [(ngModel)]="model.asset.roomTarget"
                                    [data]="model.targetRoomSelect"
                                    [textField]="'value'"
                                    [valueField]="'id'"
                                    (valueChange)="onRoomTargetValueChange($event)">
                                </kendo-dropdownlist>
                                <span *ngIf="model.asset.roomTarget && model.asset.roomTarget.id === -1" class="newRoomT">
                                    <tdsAngular:inputControl field="${standardFieldSpecs.locationTarget}" size="10" tabindex="16" placeholder="Location" ngmodel="model.asset.locationTarget"></tdsAngular:inputControl>
                                    <tdsAngular:inputControl field="${standardFieldSpecs.roomTarget}" size="10" tabindex="17" placeholder="Room Name" ngmodel="model.asset.newRoomTarget"></tdsAngular:inputControl>
                                </span>

                                <div *ngIf="showRackFields">
                                    <label for="rackSourceId">Rack/Cabinet</label>
                                    <div *ngIf="showRackTargetInput === 'select'">
                                        <kendo-dropdownlist
                                                [tabIndex]="18"
                                                class="tm-input-control useRackT"
                                                name="modelAssetRackTarget"
                                                [(ngModel)]="model.asset.rackTarget"
                                                [data]="rackTargetOptions"
                                                [textField]="'value'"
                                                [valueField]="'id'">
                                        </kendo-dropdownlist>
                                        <span *ngIf="model.asset.rackTarget && model.asset.rackTarget.id === -1">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="19" placeholder="New Rack Name"value=""ngmodel="model.asset.newRackTarget"></tdsAngular:inputControl>
                                        </span>
                                    </div>

                                    <div *ngIf="showRackTargetInput === 'new'">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="20" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackTarget"></tdsAngular:inputControl>
                                    </div>
                                </div>
                            </div>

                            
                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.model}" value="${assetInstance.model}"/>
                                <tds-combobox
                                    [tabIndex]="21"
                                    [(model)]="model.asset.modelSelectValue"
                                    [serviceRequest]="searchModels"
                                    [searchOnScroll]="false"
                                    [reloadOnOpen]="true"
                                    (valueChange)="onModelValueChange($event)">
                                </tds-combobox>
                            </div>

                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetInstance.ipAddress}" tabindex="22" ngmodel="model.asset.ipAddress"/>
                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${assetInstance.shortName}" tabindex="23" ngmodel="model.asset.shortName"/>
                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.os}" value="${assetInstance.os}" tabindex="24" ngmodel="model.asset.os" />
                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetInstance.serialNumber}" tabindex="25" ngmodel="model.asset.serialNumber"/>
                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetInstance.supportType}" tabindex="26" ngmodel="model.asset.supportType"/>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance.dependencyBundle?.value}"/>
                                <kendo-dropdownlist
                                    [tabIndex]="27"
                                    class="tm-input-control"
                                    name="modelAssetMoveBundle"
                                    [data]="model.moveBundleList"
                                    [(ngModel)]="model.asset.moveBundle"
                                    [textField]="'name'"
                                    [valueField]="'id'">
                                </kendo-dropdownlist>
                            </div>

                            <div class="clr-form-control">
								<label>
									${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
								</label>
                                <kendo-numerictextbox
                                    name="deviceSize"
                                    [format]="'n'"
                                    [min]="0"
                                    [autoCorrect]=true
                                    [tabIndex]="28"
                                    [(ngModel)]="model.asset.size">
                                </kendo-numerictextbox>
                                <kendo-dropdownlist
                                    [tabIndex]="32"
                                    class="tm-input-control"
                                    name="modelAssetScaleName"
                                    [data]="${SizeScale.getAsJsonList() as JSON}"
                                    [(ngModel)]="model.asset.scale"
                                    [textField]="'text'"
                                    [valueField]="'value'">
                                </kendo-dropdownlist>
							</div>

                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetInstance.assetTag}" tabindex="29" ngmodel="model.asset.assetTag"/>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetInstance?.retireDate}" />
                                <tds-date-control
                                    [(ngModel)]="model.asset.retireDate"
                                    class="tm-input-control"
                                    name="modelAssetRetireDate"
                                    [tabindex]="30"
                                    [value]="model.asset.retireDate">
                                </tds-date-control>     
                            </div>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance.planStatus}"/>
                                <kendo-dropdownlist
                                    [tabIndex]="31"
                                    class="tm-input-control"
                                    name="modelAssetPlanStatus"
                                    [data]="model.planStatusOptions"
                                    [(ngModel)]="model.asset.planStatus">
                                </kendo-dropdownlist>
                            </div>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.railType}" value="${assetInstance.railType}"/>
                                <kendo-dropdownlist
                                    [tabIndex]="33"
                                    class="tm-input-control"
                                    name="modelAssetRailType"
                                    [data]="model.railTypeOption"
                                    [(ngModel)]="model.asset.railType">
                                </kendo-dropdownlist>
                            </div>
            
                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetInstance.maintExpDate}"/>
                                <tds-date-control
                                    [(ngModel)]="model.asset.maintExpDate"
                                    class="tm-input-control"
                                    name="modelAssetMaintExpDate"
                                    [tabindex]="34"
                                    [value]="model.asset.maintExpDate">
                                </tds-date-control>
                            </div>

                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}[0]" />
                                <kendo-dropdownlist
                                    [tabIndex]="35"
                                    class="tm-input-control"
                                    name="modelAssetValidation"
                                    [data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}"
                                    [(ngModel)]="model.asset.validation">
                                </kendo-dropdownlist>
                            </div> 

                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetInstance.rateOfChange}"  tabindex="36" ngmodel="model.asset.rateOfChange"/>
                            <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}" tabindex="37" ngmodel="model.asset.externalRefId"/>
                        
                            <div class="clr-form-control">
                                <tdsAngular:inputLabel field="${standardFieldSpecs.truck}" value="${assetInstance.truck}"/>
                                <div class="truck-input-container">
                                    <tdsAngular:inputControl field="${standardFieldSpecs.truck}" size="3" tabindex="38" value="${assetInstance.truck}" ngmodel="model.asset.truck"/>
                                    <tdsAngular:inputControl field="${standardFieldSpecs.cart}" size="3" tabindex="39" value="${assetInstance.cart}" ngmodel="model.asset.cart"/>
                                    <tdsAngular:inputControl field="${standardFieldSpecs.shelf}" size="2" tabindex="40" value="${assetInstance.shelf}" ngmodel="model.asset.shelf"/>
                                </div>
                            </div>

                            <g:render template="/angular/common/customEdit" model="[assetEntityInstance: assetInstance]"></g:render>              
                        </div>

                        <g:render template="/angular/common/assetTagsEdit"></g:render>

                    </ng-template>
				</kendo-tabstrip-tab>

				<kendo-tabstrip-tab [title]="'Supports / Depends'">
					<ng-template kendoTabContent>
                        <tds-supports-depends (initDone)="onInitDependenciesDone($event)" [(model)]="model"  (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>		
					</ng-template>
				</kendo-tabstrip-tab>
            </kendo-tabstrip>
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
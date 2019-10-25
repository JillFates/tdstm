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
        <h4 class="modal-title">Device Edit</h4>
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
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.description}" value="${asset.description}" ngmodel="model.asset.description" tabindex="2"/>
                
                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetType}" value="${asset.assetType}"/>
                    <tds-combobox
                        class="combo-group"
                        [(model)]="model.asset.assetTypeSelectValue"
                        [serviceRequest]="searchAssetTypes"
                        [searchOnScroll]="false"
                        [reloadOnOpen]="true"
                        (valueChange)="onAssetTypeValueChange($event)"
                        tabindex="3">
                    </tds-combobox>
                </div>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="4" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
                        
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
                        <tdsAngular:inputControl field="${standardFieldSpecs.locationSource}" size="10" tabindex="6" placeholder="Location" ngmodel="model.asset.locationSource"></tdsAngular:inputControl>
                        <tdsAngular:inputControl field="${standardFieldSpecs.roomSource}" size="10" tabindex="7" placeholder="Room Name" ngmodel="model.asset.newRoomSource"></tdsAngular:inputControl>
                    </span>

                    <div *ngIf="showRackFields">
                        <label>Rack/Cabinet</label>
                        <div *ngIf="showRackSourceInput === 'select'">
                            <kendo-dropdownlist
                                    [tabIndex]="8"
                                    class="tm-input-control useRackS"
                                    name="modelAssetRackSource"
                                    [(ngModel)]="model.asset.rackSource"
                                    [data]="rackSourceOptions"
                                    [textField]="'value'"
                                    [valueField]="'id'">
                            </kendo-dropdownlist>
                            <span *ngIf="model.asset.rackSource && model.asset.rackSource.id === -1">
                                <tdsAngular:inputControl field="${standardFieldSpecs.rackSource}" size="20" tabindex="9" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackSource"></tdsAngular:inputControl>
                            </span>
                        </div>

                        <div *ngIf="showRackSourceInput === 'new'">
                            <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="10" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackSource"></tdsAngular:inputControl>
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
                            <tdsAngular:inputControl field="${standardFieldSpecs.sourceBladePosition}" size="10" tabindex="11" value="${asset.sourceBladePosition}" ngmodel="model.asset.sourceBladePosition"></tdsAngular:inputControl>
                        </div>
                    </div>

                    <div *ngIf="showBladeFields || showRackFields">
                        <label>Position</label>
                        <div *ngIf="showRackFields">
                            <div *ngIf="(showRackSourceInput === 'new' || (model.asset.rackSource && (model.asset.rackSource.id === -1 || model.asset.rackSource.id > 0)))" >
                                <tdsAngular:inputControl field="${standardFieldSpecs.sourceRackPosition}" size="10" tabindex="12" placeholder="U Position" value="${asset.sourceRackPosition}" ngmodel="model.asset.sourceRackPosition"></tdsAngular:inputControl>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="clr-form-control">
                    <label>Target</label>
                    <label for="locationSourceId">Location/Room</label>
                    <kendo-dropdownlist
                        [tabIndex]="13"
                        class="tm-input-control"
                        name="roomTargetId"
                        [(ngModel)]="model.asset.roomTarget"
                        [data]="model.targetRoomSelect"
                        [textField]="'value'"
                        [valueField]="'id'"
                        (valueChange)="onRoomTargetValueChange($event)">
                    </kendo-dropdownlist>
                    <span *ngIf="model.asset.roomTarget && model.asset.roomTarget.id === -1" class="newRoomT">
                        <tdsAngular:inputControl field="${standardFieldSpecs.locationTarget}" size="10" tabindex="14" placeholder="Location" ngmodel="model.asset.locationTarget"></tdsAngular:inputControl>
                        <tdsAngular:inputControl field="${standardFieldSpecs.roomTarget}" size="10" tabindex="15" placeholder="Room Name" ngmodel="model.asset.newRoomTarget"></tdsAngular:inputControl>
                    </span>

                     <div *ngIf="showRackFields">
                        <label for="rackSourceId">Rack/Cabinet</label>
                        <div *ngIf="showRackTargetInput === 'select'">
                            <kendo-dropdownlist
                                    [tabIndex]="16"
                                    class="tm-input-control useRackT"
                                    name="modelAssetRackTarget"
                                    [(ngModel)]="model.asset.rackTarget"
                                    [data]="rackTargetOptions"
                                    [textField]="'value'"
                                    [valueField]="'id'">
                            </kendo-dropdownlist>
                            <span *ngIf="model.asset.rackTarget && model.asset.rackTarget.id === -1">
                                <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="17" placeholder="New Rack Name"value=""ngmodel="model.asset.newRackTarget"></tdsAngular:inputControl>
                            </span>
                        </div>

                        <div *ngIf="showRackTargetInput === 'new'">
                            <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="18" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackTarget"></tdsAngular:inputControl>
                        </div>
                    </div>

                    <div *ngIf="showBladeFields">
                        <label>Blade Chassis</label>
                        <div *ngIf="showBladeTargetInput === 'select'">
                            <kendo-dropdownlist
                                    class="tm-input-control"
                                    name="modelAssetBladeTarget"
                                    [(ngModel)]="model.asset.targetChassis"
                                    [data]="bladeTargetOptions"
                                    [textField]="'value'"
                                    [valueField]="'id'"
                                    [tabIndex]="19">
                            </kendo-dropdownlist>
                        </div>
                        
                        <div *ngIf="model.asset.targetChassis && model.asset.targetChassis.id > 0" >
                            <tdsAngular:inputControl field="${standardFieldSpecs.targetBladePosition}" size="10" tabindex="20" value="${asset.targetBladePosition}" ngmodel="model.asset.targetBladePosition"></tdsAngular:inputControl>
                        </div>
                    </div>

                    <div *ngIf="showBladeFields || showRackFields">
                        <label>Position</label>
                        <div *ngIf="showRackFields">
                            <div *ngIf="(showRackTargetInput === 'new' || (model.asset.rackTarget && (model.asset.rackTarget.id === -1 || model.asset.rackTarget.id > 0)))">
                                <tdsAngular:inputControl field="${standardFieldSpecs.targetRackPosition}" placeholder="U Position" size="10" tabindex="21" value="${asset.targetRackPosition}"  ngmodel="model.asset.targetRackPosition"></tdsAngular:inputControl>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.manufacturer}" value="${asset.manufacturer}"/>
                    <tds-combobox
                        class="combo-group"
                        [(model)]="model.asset.manufacturerSelectValue"
                        [serviceRequest]="searchManufacturers"
                        [searchOnScroll]="false"
                        [reloadOnOpen]="true"
                        (valueChange)="onManufacturerValueChange($event)"
                        tabindex="22">
                    </tds-combobox>
                </div>

                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.priority}" value="${asset.priority}" tabindex="23" blankOptionListText="Please Select..." ngmodel="model.asset.priority" />

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.model}" value="${asset.model}"/>
                    <tds-combobox
                        class="combo-group"
                        [(model)]="model.asset.modelSelectValue"
                        [serviceRequest]="searchModels"
                        [searchOnScroll]="false"
                        [reloadOnOpen]="true"
                        (valueChange)="onModelValueChange($event)"
                        tabindex="24">
                    </tds-combobox>
                </div>

                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.ipAddress}" value="${asset.ipAddress}" tabindex="25" ngmodel="model.asset.ipAddress"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${asset.shortName}" tabindex="26" ngmodel="model.asset.shortName"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.os}" value="${asset.os}" tabindex="27" ngmodel="model.asset.os" />
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${asset.serialNumber}" tabindex="28" ngmodel="model.asset.serialNumber"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}" tabindex="29" ngmodel="model.asset.supportType"/>
                
                <div class="clr-form-control">                    
                    <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
                    <kendo-dropdownlist
                            [tabIndex]="30"
                            class="tm-input-control"
                            name="modelAssetMoveBundle"
                            [data]="model.moveBundleList"
                            [(ngModel)]="model.asset.moveBundle"
                            [textField]="'name'"
                            [valueField]="'id'">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.size}" value="${asset.size}"/>
                    
                    <kendo-numerictextbox
                        [tabIndex]="31"
                        name="deviceSize"
                        [format]="'n'"
                        [min]="0"
                        [autoCorrect]=true
                        [(ngModel)]="model.asset.size">
                    </kendo-numerictextbox>
                </div>

                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${asset.assetTag}" tabindex="32" ngmodel="model.asset.assetTag"/>
                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
                    <tds-date-control
                        [(ngModel)]="model.asset.retireDate"
                        class="tm-input-control"
                        name="modelAssetRetireDate"
                        [tabindex]="33"
                        [value]="model.asset.retireDate">
                    </tds-date-control>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
                    <kendo-dropdownlist
                        [tabIndex]="34"
                        class="tm-input-control"
                        name="modelAssetPlanStatus"
                        [data]="model.planStatusOptions"
                        [(ngModel)]="model.asset.planStatus">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.scale}" value="${asset.scale}"/>
                    <kendo-dropdownlist
                        [tabIndex]="35"
                        class="tm-input-control"
                        name="modelAssetScaleName"
                        [data]="${SizeScale.getAsJsonList() as JSON}"
                        [(ngModel)]="model.asset.scale"
                        [textField]="'text'"
                        [valueField]="'value'">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.railType}" value="${asset.railType}"/>
                    <kendo-dropdownlist
                        [tabIndex]="36"
                        class="tm-input-control"
                        name="modelAssetRailType"
                        [data]="model.railTypeOption"
                        [(ngModel)]="model.asset.railType">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
                    <tds-date-control
                        [(ngModel)]="model.asset.maintExpDate"
                        class="tm-input-control"
                        name="modelAssetMaintExpDate"
                        [tabindex]="37"
                        [value]="model.asset.maintExpDate">
                    </tds-date-control>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
                    <kendo-dropdownlist
                        [tabIndex]="38"
                        class="tm-input-control"
                        name="modelAssetValidation"
                        [data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
                        [(ngModel)]="model.asset.validation">
                    </kendo-dropdownlist>
                </div>

                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"  tabindex="39" ngmodel="model.asset.rateOfChange"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" tabindex="40" ngmodel="model.asset.externalRefId"/>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.truck}" value="${asset.truck}"/>
                    <div class="truck-input-container">
                        <tdsAngular:inputControl field="${standardFieldSpecs.truck}" size="3" tabindex="41" value="${asset.truck}" ngmodel="model.asset.truck"/>
                        <tdsAngular:inputControl field="${standardFieldSpecs.cart}" size="3" tabindex="42" value="${asset.cart}" ngmodel="model.asset.cart"/>
                        <tdsAngular:inputControl field="${standardFieldSpecs.shelf}" size="2" tabindex="43" value="${asset.shelf}" ngmodel="model.asset.shelf"/>
                    </div>
                </div>

                <g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>
            </div>

            <g:render template="/angular/common/assetTagsEdit"></g:render>
            <tds-supports-depends (initDone)="onInitDependenciesDone($event)" [(model)]="model"  (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
        </form>
    </div>
    <div class="modal-sidenav form-group-center">
		<nav class="modal-sidenav btn-link">
			<tds-button-save 
				(click)="submitForm($event)" 
				[disabled]="!isDependenciesValidForm" 
				[permissions]="['${Permission.AssetEdit}']" 
				tooltip="Save Asset" 
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
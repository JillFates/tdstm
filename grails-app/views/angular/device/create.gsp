<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>
<%@page import="com.tds.asset.AssetType"%>
<%@page import="net.transitionmanager.security.Permission"%>

<%-- Set some vars based on the action being save or update --%>
<g:set var="actionLabel" value="${action == 'save' ? 'Save' : 'Update'}" />
<g:set var="jsAction" value="${action=='save' ? 'saveToShow' : 'performAssetUpdate'}" />

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="onCancelEdit()"
     class="modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span  aria-hidden="true">×</span></button>
        <h4 class="modal-title">Device Create</h4>
    </div>
    <div class="modal-body">
        <form name="form" (ngSubmit)="form.form.valid && onCreate()"
              class="asset-entry-form"
              [ngClass]="{'form-submitted': form && form.submitted}"
              role="form" #form="ngForm" novalidate>
            <table style="border: 0" class="ui-widget">
                <!-- Fields -->
                <tr>
                    <td colspan="2" class="dialog-container">
                        <div class="dialog">
                            <table class="asset-create-view">
                                <tbody>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance.assetName}"/>
                                    <td colspan="3" class="top-align-field">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="10" value="${assetInstance.assetName}" ngmodel="model.asset.assetName"/>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetInstance.description}"/>
                                    <td colspan="3" class="top-align-field">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${assetInstance.description}" ngmodel="model.asset.description"/>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetType}" value="${assetInstance.assetType}"/>
                                    <td class="tm-input-control-container  ${standardFieldSpecs.assetType.imp ?: ''}" data-for="model" tabindex="13">
                                        <tds-combobox
                                                [(model)]="model.asset.assetTypeSelectValue"
                                                [serviceRequest]="searchAssetTypes"
                                                [searchOnScroll]="false"
                                                [reloadOnOpen]="true"
                                                (valueChange)="onAssetTypeValueChange($event)">
                                        </tds-combobox>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${assetInstance.environment}"/>
                                    <td class="${standardFieldSpecs.environment.imp ?: ''}" data-for="environment">
                                        <kendo-dropdownlist
                                                [tabIndex]="21"
                                                class="tm-input-control"
                                                name="modelAssetEnvironment"
                                                [(ngModel)]="model.asset.environment"
                                                [defaultItem]="'Please Select'"
                                                [data]="model.environmentOptions">
                                        </kendo-dropdownlist>
                                    </td>
                                    <td colspan="1"></td>
                                    <td class="label_sm">Source</td>
                                    <td class="label_sm">Target</td>
                                </tr>
                                <tr>
                                    <td class="label ${standardFieldSpecs.manufacturer.imp ?: ''}" nowrap="nowrap">
                                        <label for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip ?: standardFieldSpecs.manufacturer.label}">
                                            <a *ngIf="model.asset.manufacturer && model.asset.manufacturer.id" href='javascript:showManufacturer(${assetInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a>
                                            <label *ngIf="!model.asset.manufacturer">Manufacturer</label>
                                        </label>
                                    </td>
                                    <td class="tm-input-control-container  ${standardFieldSpecs.manufacturer.imp ?: ''}" data-for="manufacturer" tabindex="14">
                                        <tds-combobox
                                                [(model)]="model.asset.manufacturerSelectValue"
                                                [serviceRequest]="searchManufacturers"
                                                [searchOnScroll]="false"
                                                [reloadOnOpen]="true"
                                                (valueChange)="onManufacturerValueChange($event)" >
                                        </tds-combobox>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.priority}" value="${assetInstance.priority}"/>
                                    <td class="${standardFieldSpecs.priority.imp ?: ''}" data-for="priority">
                                        <kendo-dropdownlist
											    [tabIndex]="22"
                                                class="tm-input-control"
                                                name="priorityAssetEnvironment"
                                                [(ngModel)]="model.asset.priority"
                                                [defaultItem]="'Please Select'"
                                                [data]="model.priorityOption">
                                        </kendo-dropdownlist>
                                    </td>
                                    <td class="label ${standardFieldSpecs.roomSource.imp?:''}" nowrap="nowrap">
                                        <label for="locationSourceId">Location/Room</label>
                                    </td>
                                    <td class="${standardFieldSpecs.roomSource.imp ?: ''}" data-for="roomSource"  style="vertical-align: text-top;">
                                        <kendo-dropdownlist
                                                [tabIndex]="31"
                                                class="tm-input-control"
                                                name="roomSourceId"
                                                [(ngModel)]="model.asset.roomSource"
                                                [defaultItem]="{id: -2, value: 'Please Select'}"
                                                [data]="model.sourceRoomSelect"
                                                [textField]="'value'"
                                                [valueField]="'id'"
                                                (valueChange)="onRoomSourceValueChange($event)">
                                        </kendo-dropdownlist>
                                        <%-- Theses fields are used to allow user to create a source room on the fly --%>
                                        <span *ngIf="model.asset.roomSource && model.asset.roomSource.id === -1" class="newRoomS">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.locationSource}" size="10" tabindex="32" placeholder="Location" ngmodel="model.asset.locationSource"> </tdsAngular:inputControl>
                                            <tdsAngular:inputControl field="${standardFieldSpecs.roomSource}" size="10" tabindex="33" placeholder="Room Name" ngmodel="model.asset.newRoomSource"> </tdsAngular:inputControl>
                                        </span>
                                    </td>
                                    <td class="${standardFieldSpecs.roomTarget.imp ?: ''}" data-for="roomTarget" style="vertical-align: text-top;">
                                        <kendo-dropdownlist
                                                [tabIndex]="41"
                                                class="tm-input-control"
                                                name="roomTargetId"
                                                [(ngModel)]="model.asset.roomTarget"
                                                [defaultItem]="{id: -2, value: 'Please Select'}"
                                                [data]="model.targetRoomSelect"
                                                [textField]="'value'"
                                                [valueField]="'id'"
                                                (valueChange)="onRoomTargetValueChange($event)">
                                        </kendo-dropdownlist>
                                        <%-- Theses fields are used to allow user to create a source room on the fly --%>
                                        <span *ngIf="model.asset.roomTarget && model.asset.roomTarget.id === -1" class="newRoomT">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.locationTarget}" size="10" tabindex="42" placeholder="Location" ngmodel="model.asset.locationTarget"></tdsAngular:inputControl>
                                            <tdsAngular:inputControl field="${standardFieldSpecs.roomTarget}" size="10" tabindex="43" placeholder="Room Name" ngmodel="model.asset.newRoomTarget"></tdsAngular:inputControl>
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.model}" value="${assetInstance.model}"/>
                                    <td class="tm-input-control-container ${standardFieldSpecs.model.imp ?: ''}" data-for="assetType" tabindex="15">
                                        <tds-combobox
                                                [(model)]="model.asset.modelSelectValue"
                                                [serviceRequest]="searchModels"
                                                [searchOnScroll]="false"
                                                [reloadOnOpen]="true"
                                                (valueChange)="onModelValueChange($event)">
                                        </tds-combobox>
                                    </td>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetInstance.ipAddress}" tabindex="23" ngmodel="model.asset.ipAddress"/>

                                    <!-- Rack Source/Target Select & New Fields -->
                                    <td *ngIf="showRackFields" class="label rackLabel ${standardFieldSpecs.rackSource.imp ?: ''}" nowrap="nowrap" id="rackId">
                                        <label for="rackSourceId" data-toggle="popover" data-trigger="hover"  data-content="Rack/Cabinet">Rack/Cabinet</label>
                                    </td>
                                    <td  *ngIf="showRackFields"  data-for="rackSource"  class="rackLabel ${standardFieldSpecs.rackSource.imp ?: ''}">
                                        <div *ngIf="showRackSourceInput === 'select'">
                                            <kendo-dropdownlist
												    [tabIndex]="34"
                                                    class="tm-input-control useRackS"
                                                    name="modelAssetRackSource"
                                                    [(ngModel)]="model.asset.rackSource"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="rackSourceOptions"
                                                    [textField]="'value'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                            <span *ngIf="model.asset.rackSource && model.asset.rackSource.id === -1">
                                                <tdsAngular:inputControl field="${standardFieldSpecs.rackSource}" size="20" tabindex="35" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackSource"></tdsAngular:inputControl>
                                            </span>
                                        </div>
                                        <div *ngIf="showRackSourceInput === 'new'">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="36" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackSource"></tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <td *ngIf="showRackFields" data-for="rackTarget" class="rackLabel ${standardFieldSpecs.rackTarget.imp ?: ''}">
                                        <div *ngIf="showRackTargetInput === 'select'">
                                            <kendo-dropdownlist
                                                    [tabIndex]="44"
                                                    class="tm-input-control useRackT"
                                                    name="modelAssetRackTarget"
                                                    [(ngModel)]="model.asset.rackTarget"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="rackTargetOptions"
                                                    [textField]="'value'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                            <span *ngIf="model.asset.rackTarget && model.asset.rackTarget.id === -1">
                                                <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}"
                                                                         size="20" tabindex="45" placeholder="New Rack Name"
                                                                         value=""
                                                                         ngmodel="model.asset.newRackTarget">
                                                </tdsAngular:inputControl>
                                            </span>
                                        </div>
                                        <div *ngIf="showRackTargetInput === 'new'">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}" size="20" tabindex="46" placeholder="New Rack Name" value="" ngmodel="model.asset.newRackTarget"></tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <!-- Blade Source/Target Select Fields -->
                                    <td *ngIf="showBladeFields" class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp ?: ''}" nowrap="nowrap" id="bladeId">
                                        <label for="sourceChassisId" data-toggle="popover" data-trigger="hover" data-content="Blade Chassis">Blade Chassis</label>
                                    </td>
                                    <td *ngIf="showBladeFields" data-for="sourceChassis" class="bladeLabel ${standardFieldSpecs.sourceChassis.imp ?: ''}">
                                        <div *ngIf="showBladeSourceInput === 'select'">
                                            <kendo-dropdownlist
                                                    class="tm-input-control"
                                                    name="modelAssetBladeSource"
                                                    [(ngModel)]="model.asset.sourceChassis"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="bladeSourceOptions"
                                                    [textField]="'value'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </div>
                                    </td>
                                    <td *ngIf="showBladeFields" data-for="targetChassis" class="bladeLabel ${standardFieldSpecs.targetChassis.imp ?: ''}">
                                        <div *ngIf="showBladeTargetInput === 'select'">
                                            <kendo-dropdownlist
                                                    class="tm-input-control"
                                                    name="modelAssetBladeTarget"
                                                    [(ngModel)]="model.asset.targetChassis"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="bladeTargetOptions"
                                                    [textField]="'value'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${assetInstance.shortName}" tabindex="16" ngmodel="model.asset.shortName"/>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.os}" value="${assetInstance.os}" tabindex="24" ngmodel="model.asset.os" />
                                    <td *ngIf="showBladeFields || showRackFields" class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp ?: ''}" nowrap="nowrap">
                                        <label for="sourceRackPositionId" data-toggle="popover" data-trigger="hover" data-content="Position">Position</label>
                                    </td>
                                    <%-- Rack Source/Target Position Fields --%>
                                    <td *ngIf="showRackFields" class="rackLabel">
                                        <div *ngIf="(showRackSourceInput === 'new' || (model.asset.rackSource && (model.asset.rackSource.id === -1 || model.asset.rackSource.id > 0)))" >
                                            <tdsAngular:inputControl field="${standardFieldSpecs.sourceRackPosition}" size="10" tabindex="37" placeholder="U Position" value="${assetInstance.sourceRackPosition}" ngmodel="model.asset.sourceRackPosition"></tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <td *ngIf="showRackFields" class="rackLabel">
                                        <div *ngIf="(showRackTargetInput === 'new' || (model.asset.rackTarget && (model.asset.rackTarget.id === -1 || model.asset.rackTarget.id > 0)))">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.targetRackPosition}" placeholder="U Position" size="10" tabindex="47" value="${assetInstance.targetRackPosition}"  ngmodel="model.asset.targetRackPosition"></tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <%-- Blade Source/Target Position Fields --%>
                                    <td *ngIf="showBladeFields" class="bladeLabel">
                                        <div *ngIf="model.asset.sourceChassis && model.asset.sourceChassis.id > 0" >
                                            <tdsAngular:inputControl field="${standardFieldSpecs.sourceBladePosition}" size="10" tabindex="48" value="${assetInstance.sourceBladePosition}" ngmodel="model.asset.sourceBladePosition"></tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <td *ngIf="showBladeFields" class="bladeLabel">
                                        <div *ngIf="model.asset.targetChassis && model.asset.targetChassis.id > 0" >
                                            <tdsAngular:inputControl field="${standardFieldSpecs.targetBladePosition}" size="10" tabindex="52" value="${assetInstance.targetBladePosition}" ngmodel="model.asset.targetBladePosition"></tdsAngular:inputControl>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetInstance.serialNumber}" tabindex="17" ngmodel="model.asset.serialNumber"/>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetInstance.supportType}" tabindex="25" ngmodel="model.asset.supportType"/>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance.moveBundle?.id}"/>
                                    <td class="${standardFieldSpecs.moveBundle.imp ?: ''}" data-for="moveBundle">
                                        <kendo-dropdownlist
                                                [tabIndex]="38"
                                                class="tm-input-control"
                                                name="modelAssetMoveBundle"
                                                [data]="model.moveBundleList"
                                                [(ngModel)]="model.asset.moveBundle"
                                                [textField]="'name'"
                                                [valueField]="'id'">
                                        </kendo-dropdownlist>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.size}" value="${assetInstance.size}"/>
                                    <td data-for="sizeScale" nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp ?: ''}">
                                        <kendo-numerictextbox
                                                name="deviceSize"
                                                [format]="'n'"
                                                [format]="'n'"
                                                [min]="0"
                                                [autoCorrect]=true
                                                [tabIndex]="49"
                                                [(ngModel)]="model.asset.size">
                                        </kendo-numerictextbox>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetInstance.assetTag}" tabindex="18" ngmodel="model.asset.assetTag"/>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}" />
                                    <td data-for="retireDate" valign="top" class="value ${hasErrors(bean:asset,field:'retireDate','errors')} ${standardFieldSpecs.retireDate.imp ?: ''}">
                                        <kendo-datepicker
											    [tabIndex]="26"
                                                class="tm-input-control"
                                                name="modelAssetRetireDate"
                                                [format]="dateFormat"
                                                [(value)]="model.asset.retireDate">
                                        </kendo-datepicker>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance.planStatus}"/>
                                    <td class="${standardFieldSpecs.planStatus.imp ?: ''}" data-for="planStatus">
                                        <kendo-dropdownlist
                                                [tabIndex]="39"
                                                class="tm-input-control"
                                                name="modelAssetPlanStatus"
                                                [data]="model.planStatusOptions"
                                                [(ngModel)]="model.asset.planStatus">
                                        </kendo-dropdownlist>
                                    </td>
                                    <td class="label ${standardFieldSpecs.size.imp ?: ''}">Size units</td>
                                    <td data-for="sizeScale" class="${standardFieldSpecs.size.imp ?: ''}">
                                        <kendo-dropdownlist
                                                [tabIndex]="50"
                                                class="tm-input-control"
                                                name="modelAssetScaleName"
                                                [data]="${SizeScale.getAsJsonList() as JSON}"
                                                [(ngModel)]="model.asset.scale"
                                                [defaultItem]="''"
                                                [textField]="'text'"
                                                [valueField]="'value'">
                                        </kendo-dropdownlist>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.railType}" value="${assetInstance.railType}"/>
                                    <td class="${standardFieldSpecs.railType.imp ?: ''}" data-for="railType">
                                        <kendo-dropdownlist
                                                [tabIndex]="19"
                                                class="tm-input-control"
                                                name="modelAssetRailType"
                                                [data]="model.railTypeOption"
                                                [(ngModel)]="model.asset.railType">
                                        </kendo-dropdownlist>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetInstance.maintExpDate}"/>
                                    <td data-for="maintExpDate" valign="top" class="value ${hasErrors(bean:asset,field:'maintExpDate','errors')} ${standardFieldSpecs.maintExpDate.imp ?: ''}">
                                        <kendo-datepicker
                                                [tabIndex]="27"
                                                class="tm-input-control"
                                                name="modelAssetMaintExpDate"
                                                [format]="dateFormat"
                                                [(value)]="model.asset.maintExpDate">
                                        </kendo-datepicker>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${assetInstance.validation}"/>
                                    <td colspan="1" class="${standardFieldSpecs.validation.imp ?: ''}" data-for="validation">
                                        <kendo-dropdownlist
											    [tabIndex]="40"
                                                class="tm-input-control"
                                                name="modelAssetValidation"
                                                [data]="${assetInstance.constraints.validation.inList as JSON}"
                                                [(ngModel)]="model.asset.validation">
                                        </kendo-dropdownlist>
                                    </td>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetInstance.rateOfChange}"  tabindex="51" ngmodel="model.asset.rateOfChange"/>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}"
                                                                   tabindex="20" ngmodel="model.asset.externalRefId"/>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.truck}" value="${assetInstance.truck}"/>
                                    <td class="truck-input-group">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.truck}" size="3" tabindex="28" value="${assetInstance.truck}" ngmodel="model.asset.truck"/>
                                        <tdsAngular:inputControl field="${standardFieldSpecs.cart}" size="3" tabindex="29" value="${assetInstance.cart}" ngmodel="model.asset.cart"/>
                                        <tdsAngular:inputControl field="${standardFieldSpecs.shelf}" size="2" tabindex="30" value="${assetInstance.shelf}" ngmodel="model.asset.shelf"/>
                                    </td>
                                </tr>
                                <g:render template="/angular/common/customEdit" model="[assetEntityInstance: assetInstance]"></g:render>
                                <g:render template="/angular/common/assetTagsEdit"></g:render>
                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>
                <tr><td colspan="2">&nbsp;</td></tr>
                <!-- Dependencies -->
                <tr id="deps">
                    <td valign="top" colspan="2">
                        <tds-supports-depends (initDone)="onInitDependenciesDone($event)" [(model)]="model"  (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
                    </td>
                </tr>
            </table>
        </form>
    </div>
    <div class="modal-footer form-group-center">
        <button class="btn btn-primary pull-left component-action-update" type="button" (click)="submitForm($event)" [disabled]="!isDependenciesValidForm"><span class="fa fa-fw fa-floppy-o"></span> Create</button>

        <button class="btn btn-default pull-right component-action-delete" (click)="onCancelEdit()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
    </div>
</div>
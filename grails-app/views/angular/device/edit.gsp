<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>
<%@page import="com.tds.asset.AssetType"%>
<%@page import="net.transitionmanager.security.Permission"%>

<%-- Set some vars based on the action being save or update --%>
<g:set var="actionLabel" value="${action == 'save' ? 'Save' : 'Update'}" />
<g:set var="jsAction" value="${action=='save' ? 'saveToShow' : 'performAssetUpdate'}" />

<div class="modal-content tds-angular-component-content" style="width: 105%">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
        <h4 class="modal-title">Device Edit</h4>
    </div>
    <div class="modal-body">
        <table style="border: 0" class="ui-widget">
            <!-- Fields -->
            <tr>
                <td colspan="2">
                    <div class="dialog">
                        <table>
                            <tbody>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset.assetName}"/>
                                    <td colspan="3">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="100"
                                                                 value="${asset.assetName}" ngmodel="model.asset.assetName"/>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset.description}"/>
                                    <td colspan="3">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="101"
                                                                 value="${asset.description}" tooltipDataPlacement="bottom"
                                                                 ngmodel="model.asset.description"/>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetType}" value="${asset.assetType}"/>
                                    <td class="${standardFieldSpecs.assetType.imp ?: ''}" data-for="model"
                                        style="border-top: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
                                        <tds-combobox
                                                [(model)]="model.asset.assetTypeSelectValue"
                                                [serviceRequest]="searchAssetTypes"
                                                [searchOnScroll]="false"
                                                [reloadOnOpen]="true"
                                                (valueChange)="onAssetTypeValueChange($event)">
                                        </tds-combobox>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
                                    <td>
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.environment}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetEnvironment"
                                                    [(ngModel)]="model.asset.environment"
                                                    [defaultItem]="'Please Select'"
                                                    [data]="model.environmentOptions">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                    <td colspan="1"></td>
                                    <td class="label_sm">Source</td>
                                    <td class="label_sm">Target</td>
                                </tr>
                                <tr>
                                    <td class="label ${standardFieldSpecs.manufacturer.imp ?: ''}" nowrap="nowrap">
                                        <label for="manufacturer" data-toggle="popover" data-trigger="hover"
                                               data-content="${standardFieldSpecs.manufacturer.tip ?: standardFieldSpecs.manufacturer.label}">
                                            <a *ngIf="model.asset.manufacturer && model.asset.manufacturer.id" href='javascript:showManufacturer(${asset.manufacturer?.id})'
                                               style='color:#00E'>Manufacturer</a>
                                            <label *ngIf="!model.asset.manufacturer">Manufacturer</label>
                                        </label>
                                    </td>
                                    <td class="${standardFieldSpecs.manufacturer.imp ?: ''}" data-for="manufacturer"
                                        style="border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
                                            <tds-combobox
                                                    [(model)]="model.asset.manufacturerSelectValue"
                                                    [serviceRequest]="searchManufacturers"
                                                    [searchOnScroll]="false"
                                                    [reloadOnOpen]="true"
                                                    (valueChange)="onManufacturerValueChange($event)" >
                                            </tds-combobox>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.priority}" value="${asset.priority}"/>
                                    <td>
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.priority}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="priorityAssetEnvironment"
                                                    [(ngModel)]="model.asset.priority"
                                                    [defaultItem]="'Please Select'"
                                                    [data]="model.priorityOption">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                    <td class="label ${standardFieldSpecs.roomSource.imp?:''}" nowrap="nowrap">
                                        <label for="locationSourceId">Location/Room</label>
                                    </td>
                                    <td style="vertical-align: text-top;">
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.roomSource}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="roomSourceId"
                                                    [(ngModel)]="model.asset.roomSource"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="model.sourceRoomSelect"
                                                    [textField]="'value'"
                                                    [valueField]="'id'"
                                                    (valueChange)="onRoomSourceValueChange($event)">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                        <%-- Theses fields are used to allow user to create a source room on the fly --%>
                                        <span *ngIf="model.asset.roomSource && model.asset.roomSource.id === -1" class="newRoomS" data-toggle="popover"
                                              data-trigger="hover" data-content="${standardFieldSpecs.locationSource.tip ?: standardFieldSpecs.locationSource.label}">
                                                <tdsAngular:inputControl field="${standardFieldSpecs.locationSource}"
                                                                     size="10" tabindex="301" placeholder="Location"
                                                                     ngmodel="model.asset.locationSource">
                                                </tdsAngular:inputControl>
                                                <tdsAngular:inputControl field="${standardFieldSpecs.roomSource}"
                                                                         size="10" tabindex="302" placeholder="Room Name"
                                                                         ngmodel="model.asset.newRoomSource">
                                                </tdsAngular:inputControl>
                                        </span>
                                    </td>
                                    <td style="vertical-align: text-top;">
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.roomTarget}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="roomTargetId"
                                                    [(ngModel)]="model.asset.roomTarget"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="model.targetRoomSelect"
                                                    [textField]="'value'"
                                                    [valueField]="'id'"
                                                    (valueChange)="onRoomTargetValueChange($event)">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    <%-- Theses fields are used to allow user to create a source room on the fly --%>
                                        <span *ngIf="model.asset.roomTarget && model.asset.roomTarget.id === -1" class="newRoomT" data-toggle="popover"
                                              data-trigger="hover" data-content="${standardFieldSpecs.locationTarget.tip ?: standardFieldSpecs.locationTarget.label}">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.locationTarget}"
                                                                     size="10" tabindex="331" placeholder="Location"
                                                                     ngmodel="model.asset.locationTarget">
                                            </tdsAngular:inputControl>
                                            <tdsAngular:inputControl field="${standardFieldSpecs.roomTarget}"
                                                                     size="10" tabindex="332" placeholder="Room Name"
                                                                     ngmodel="model.asset.newRoomTarget">
                                            </tdsAngular:inputControl>
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.model}" value="${asset.model}"/>
                                    <td class="${standardFieldSpecs.model.imp ?: ''}" data-for="assetType" style="border-bottom: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
                                        <tds-combobox
                                                [(model)]="model.asset.modelSelectValue"
                                                [serviceRequest]="searchModels"
                                                [searchOnScroll]="false"
                                                [reloadOnOpen]="true"
                                                (valueChange)="onModelValueChange($event)">
                                        </tds-combobox>
                                    </td>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.ipAddress}" value="${asset.ipAddress}"
                                                                   tabindex="105" ngmodel="model.asset.ipAddress"/>

                                    <!-- Rack Source/Target Select & New Fields -->
                                    <td *ngIf="showRackFields" class="label rackLabel ${standardFieldSpecs.rackSource.imp ?: ''}"
                                        nowrap="nowrap" id="rackId">
                                        <label for="rackSourceId" data-toggle="popover" data-trigger="hover"
                                               data-content="Rack/Cabinet">Rack/Cabinet</label>
                                    </td>
                                    <td *ngIf="showRackFields" class="rackLabel">
                                        <div *ngIf="showRackSourceInput === 'select'">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.rackSource}">
                                                <kendo-dropdownlist
                                                        class="select useRackS"
                                                        name="modelAssetRackSource"
                                                        [(ngModel)]="model.asset.rackSource"
                                                        [defaultItem]="{id: -2, value: 'Please Select'}"
                                                        [data]="rackSourceOptions"
                                                        [textField]="'value'"
                                                        [valueField]="'id'">
                                                </kendo-dropdownlist>
                                                <span *ngIf="model.asset.rackSource && model.asset.rackSource.id === -1">
                                                    <tdsAngular:inputControl field="${standardFieldSpecs.rackSource}"
                                                                             size="20" tabindex="311" placeholder="New Rack Name"
                                                                             value=""
                                                                             ngmodel="model.asset.newRackSource">
                                                    </tdsAngular:inputControl>
                                                </span>
                                            </tdsAngular:tooltipSpan>
                                        </div>
                                        <div *ngIf="showRackSourceInput === 'new'">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rackSource}"
                                                                     size="20" tabindex="311" placeholder="New Rack Name"
                                                                     value=""
                                                                     ngmodel="model.asset.newRackSource">
                                            </tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <td *ngIf="showRackFields" class="rackLabel">
                                        <div *ngIf="showRackTargetInput === 'select'">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.rackTarget}">
                                                <kendo-dropdownlist
                                                        class="select useRackT"
                                                        name="modelAssetRackTarget"
                                                        [(ngModel)]="model.asset.rackTarget"
                                                        [defaultItem]="{id: -2, value: 'Please Select'}"
                                                        [data]="rackTargetOptions"
                                                        [textField]="'value'"
                                                        [valueField]="'id'">
                                                </kendo-dropdownlist>
                                                <span *ngIf="model.asset.rackTarget && model.asset.rackTarget.id === -1">
                                                    <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}"
                                                                             size="20" tabindex="311" placeholder="New Rack"
                                                                             value=""
                                                                             ngmodel="model.asset.newRackTarget">
                                                    </tdsAngular:inputControl>
                                                </span>
                                            </tdsAngular:tooltipSpan>
                                        </div>
                                        <div *ngIf="showRackTargetInput === 'new'">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rackTarget}"
                                                                     size="20" tabindex="311" placeholder="New Rack Name"
                                                                     value=""
                                                                     ngmodel="model.asset.newRackTarget">
                                            </tdsAngular:inputControl>
                                        </div>
                                    </td>

                                    <!-- Blade Source/Target Select Fields -->
                                    <td *ngIf="showBladeFields" class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp ?: ''}"
                                        nowrap="nowrap" id="bladeId">
                                        <label for="sourceChassisId" data-toggle="popover" data-trigger="hover"
                                               data-content="Blade Chassis">Blade Chassis</label>
                                    </td>
                                    <td *ngIf="showBladeFields" class="bladeLabel">
                                        <div *ngIf="showBladeSourceInput === 'select'">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.sourceChassis}">
                                                <kendo-dropdownlist
                                                        class="select"
                                                        name="modelAssetBladeSource"
                                                        [(ngModel)]="model.asset.sourceChassis"
                                                        [defaultItem]="{id: -2, value: 'Please Select'}"
                                                        [data]="bladeSourceOptions"
                                                        [textField]="'value'"
                                                        [valueField]="'id'">
                                                </kendo-dropdownlist>
                                            </tdsAngular:tooltipSpan>
                                        </div>
                                    </td>
                                    <td *ngIf="showBladeFields" class="bladeLabel">
                                        <div *ngIf="showBladeTargetInput === 'select'">
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.targetChassis}">
                                                <kendo-dropdownlist
                                                        class="select"
                                                        name="modelAssetBladeTarget"
                                                        [(ngModel)]="model.asset.targetChassis"
                                                        [defaultItem]="{id: -2, value: 'Please Select'}"
                                                        [data]="bladeTargetOptions"
                                                        [textField]="'value'"
                                                        [valueField]="'id'">
                                                </kendo-dropdownlist>
                                            </tdsAngular:tooltipSpan>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${asset.shortName}"
                                                                   tabindex="105" ngmodel="model.asset.shortName"/>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.os}" value="${asset.os}"
                                                                   tabindex="220" ngmodel="model.asset.os" />

                                    <td *ngIf="showBladeFields || showRackFields" class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp ?: ''}" nowrap="nowrap">
                                        <label for="sourceRackPositionId" data-toggle="popover" data-trigger="hover"
                                               data-content="Position">Position</label>
                                    </td>

                                    <%-- Rack Source/Target Position Fields --%>
                                    <td *ngIf="showRackFields" class="rackLabel">
                                        <div *ngIf="(showRackSourceInput === 'new' || (model.asset.rackSource && (model.asset.rackSource.id === -1 || model.asset.rackSource.id > 0)))" >
                                            <tdsAngular:tooltipSpan class="sourceRackPositionT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRackPosition}">
                                                <tdsAngular:inputControl field="${standardFieldSpecs.sourceRackPosition}"
                                                                         size="10" tabindex="320" placeholder="U Position"
                                                                         value="${asset.sourceRackPosition}"
                                                                         ngmodel="model.asset.sourceRackPosition">
                                                </tdsAngular:inputControl>
                                            </tdsAngular:tooltipSpan>
                                        </div>
                                    </td>
                                    <td *ngIf="showRackFields" class="rackLabel">
                                        <div *ngIf="(showRackTargetInput === 'new' || (model.asset.rackTarget && (model.asset.rackTarget.id === -1 || model.asset.rackTarget.id > 0)))">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.targetRackPosition}" placeholder="U Position"
                                                                     size="10" tabindex="350" value="${asset.targetRackPosition}"
                                                                     ngmodel="model.asset.targetRackPosition">
                                            </tdsAngular:inputControl>
                                        </div>
                                    </td>

                                    <%-- Blade Source/Target Position Fields --%>
                                    <td *ngIf="showBladeFields" class="bladeLabel">
                                        <div *ngIf="model.asset.sourceChassis && model.asset.sourceChassis.id > 0" >
                                            <tdsAngular:inputControl field="${standardFieldSpecs.sourceBladePosition}"
                                                                     size="10" tabindex="320" value="${asset.sourceBladePosition}"
                                                                     ngmodel="model.asset.sourceBladePosition">
                                            </tdsAngular:inputControl>
                                        </div>
                                    </td>
                                    <td *ngIf="showBladeFields" class="bladeLabel">
                                        <div *ngIf="model.asset.targetChassis && model.asset.targetChassis.id > 0" >
                                            <tdsAngular:inputControl field="${standardFieldSpecs.targetBladePosition}"
                                                                     size="10" tabindex="350" value="${asset.targetBladePosition}"
                                                                     ngmodel="model.asset.targetBladePosition">
                                            </tdsAngular:inputControl>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${asset.serialNumber}"
                                                                   tabindex="106" ngmodel="model.asset.serialNumber"/>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"
                                                                   tabindex="225" ngmodel="model.asset.supportType"/>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
                                    <td>
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetMoveBundle"
                                                    [data]="model.moveBundleList"
                                                    [(ngModel)]="model.asset.moveBundle"
                                                    [textField]="'name'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.size}" value="${asset.size}"/>
                                    <td nowrap="nowrap" class="sizeScale">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.size}"
                                                                 size="4" tabindex="410"
                                                                 value="${asset.size}"
                                                                 ngmodel="model.asset.size"/>
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.scale}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetScaleName"
                                                    [data]="${SizeScale.getAsJsonList() as JSON}"
                                                    [(ngModel)]="model.asset.scale.name"
                                                    [defaultItem]="''"
                                                    [textField]="'text'"
                                                    [valueField]="'value'">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${asset.assetTag}"
                                                                   tabindex="107" ngmodel="model.asset.assetTag"/>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
                                    <td valign="top" class="value ${hasErrors(bean:asset,field:'retireDate','errors')}">
                                        <kendo-datepicker
                                                name="modelAssetRetireDate"
                                                [format]="dateFormat"
                                                [(value)]="model.asset.retireDate">
                                        </kendo-datepicker>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
                                    <td>
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.planStatus}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetPlanStatus"
                                                    [data]="model.planStatusOptions"
                                                    [(ngModel)]="model.asset.planStatus">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"
                                                                   tabindex="420" ngmodel="model.asset.rateOfChange"/>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.railType}" value="${asset.railType}"/>
                                    <td>
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.railType}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetRailType"
                                                    [data]="model.railTypeOption"
                                                    [(ngModel)]="model.asset.railType">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
                                    <td valign="top" class="value ${hasErrors(bean:asset,field:'maintExpDate','errors')}">
                                        <kendo-datepicker
                                                name="modelAssetMaintExpDate"
                                                [format]="dateFormat"
                                                [(value)]="model.asset.maintExpDate">
                                        </kendo-datepicker>
                                    </td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
                                    <td colspan="2">
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.validation}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetValidation"
                                                    [data]="${asset.constraints.validation.inList as JSON}"
                                                    [(ngModel)]="model.asset.validation">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}"
                                                                   tabindex="109" ngmodel="model.asset.externalRefId"/>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.truck}" value="${asset.truck}"/>
                                    <td>
                                        <tdsAngular:inputControl field="${standardFieldSpecs.truck}"
                                                                 size="3" tabindex="240"
                                                                 value="${asset.truck}"
                                                                 ngmodel="model.asset.truck"/>
                                        <tdsAngular:inputControl field="${standardFieldSpecs.cart}"
                                                                 size="3" tabindex="241"
                                                                 value="${asset.cart}"
                                                                 ngmodel="model.asset.cart"/>
                                        <tdsAngular:inputControl field="${standardFieldSpecs.shelf}"
                                                                 size="2" tabindex="242"
                                                                 value="${asset.shelf}"
                                                                 ngmodel="model.asset.shelf"/>
                                    </td>
                                </tr>
                                <g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
            <!-- Dependencies -->
            <tr id="deps">
                <td valign="top" colspan="2">
                    <tds-supports-depends [(model)]="model"></tds-supports-depends>
                </td>
            </tr>
        </table>
    </div>
    <div class="modal-footer form-group-center">
        <button class="btn btn-primary pull-left" type="button" (click)="onUpdate()"><span class="fa fa-fw fa-floppy-o"></span> Update</button>
        <tds:hasPermission permission="${Permission.AssetDelete}">
            <button class="btn btn-danger pull-left mar-left-50" (click)="onDelete()" type="button"><span class="glyphicon glyphicon-trash"></span> Delete</button>
        </tds:hasPermission>
        <button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
    </div>
</div>
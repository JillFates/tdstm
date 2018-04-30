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
                                        %{--<div id="modelEditId" data-toggle="popover" data-trigger="hover"--}%
                                        %{--data-content="${standardFieldSpecs.assetType.tip ?: standardFieldSpecs.assetType.label}">--}%
                                        %{--<div id="assetTypeSelect" tabindex="102">--}%
                                        %{--</div>--}%
                                        %{--</div>--}%
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
                                    %{--<tdsAngular:inputLabel *ngIf="!model.asset.manufacturer || !model.asset.manufacturer.id"--}%
                                                           %{--field="${standardFieldSpecs.manufacturer}" value="${asset.manufacturer}"/>--}%
                                    <td class="${standardFieldSpecs.manufacturer.imp ?: ''}" data-for="manufacturer"
                                        style="border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
                                            <tds-combobox
                                                    [(model)]="model.asset.manufacturerSelectValue"
                                                    [serviceRequest]="searchManufacturers"
                                                    [searchOnScroll]="false"
                                                    [reloadOnOpen]="true"
                                                    (valueChange)="onManufacturerValueChange($event)" >
                                            </tds-combobox>
                                        %{--<div id="manufacturerEditId" style="display:inline" data-toggle="popover"--}%
                                        %{--data-trigger="hover"--}%
                                        %{--data-content="${standardFieldSpecs.manufacturer.tip ?: standardFieldSpecs.manufacturer.label}">--}%
                                        %{--<div id="manufacturerSelect" tabindex="103">--}%
                                        %{--</div>--}%
                                        %{--</div>--}%
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
                                    <td class="label ${standardFieldSpecs.locationSource.imp?:''}" nowrap="nowrap">
                                        <label for="locationSourceId">Location/Room</label>
                                    </td>
                                    <td class="${standardFieldSpecs.locationSource.imp ?: ''}" style="vertical-align: text-top;" data-for="locationSourceId">
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.locationSource}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="roomSourceId"
                                                    [(ngModel)]="model.asset.roomSource"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="model.sourceRoomSelect"
                                                    [textField]="'value'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                        <%-- Theses fields are used to allow user to create a source room on the fly --%>
                                        <span class="newRoomS" style="display:none" data-toggle="popover"
                                              data-trigger="hover"
                                              data-content="${standardFieldSpecs.locationSource.tip ?: standardFieldSpecs.locationSource.label}">
                                            <input type="text" id="locationSourceId" name="locationSource" value=""
                                                   placeholder="Location"
                                                   class="${standardFieldSpecs.locationSource.imp ?: ''}"
                                                   size=10
                                                   tabindex="301"/>
                                            <input type="text" id="roomSourceId" name="roomSource" value=""
                                                   placeholder="Room Name"
                                                   class="${standardFieldSpecs.roomSource.imp ?: ''}"
                                                   size=10
                                                   tabindex="302"/>
                                        </span>
                                    </td>
                                    <td nowrap style="vertical-align: text-top;" class="${standardFieldSpecs.locationSource.imp ?: ''}" data-for="locationSourceId">
                                        <tdsAngular:tooltipSpan field="${standardFieldSpecs.locationSource}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="roomTargetId"
                                                    [(ngModel)]="model.asset.roomTarget"
                                                    [defaultItem]="{id: -2, value: 'Please Select'}"
                                                    [data]="model.targetRoomSelect"
                                                    [textField]="'value'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </tdsAngular:tooltipSpan>
                                    <%-- Theses fields are used to allow user to create a source room on the fly --%>
                                        <span class="newRoomT" style="display:none" data-toggle="popover"
                                              data-trigger="hover"
                                              data-content="${standardFieldSpecs.locationTarget.tip ?: standardFieldSpecs.locationTarget.label}">
                                            <br/>
                                            <input type="text" id="locationTarget" name="locationTarget" value=""
                                                   placeholder="Location"
                                                   class="${standardFieldSpecs.locationTarget.imp ?: ''}"
                                                   size=10 tabindex="331"/>
                                            <input type="text" id="roomTargetId" name="roomTarget" value=""
                                                   placeholder="Room Name"
                                                   class="${standardFieldSpecs.roomTarget.imp ?: ''}"
                                                   size=10 tabindex="332"/>
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
                                    <!-- Special fields with show/hide logic starts here (rackLabel/bladeLabel) -->
                                    <td class="label rackLabel ${standardFieldSpecs.rackSource.imp ?: ''}" style="display:none"
                                        nowrap="nowrap" id="rackId">
                                        <label for="rackSourceId" data-toggle="popover" data-trigger="hover"
                                               data-content="Rack/Cabinet">Rack/Cabinet</label>
                                    </td>
                                    <td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp ?: ''}" style="display:none"
                                        nowrap="nowrap" id="bladeId">
                                        <label for="sourceChassisId" data-toggle="popover" data-trigger="hover"
                                               data-content="Blade Chassis">Blade Chassis</label>
                                    </td>
                                    <td class="label rackLabel ${standardFieldSpecs.rackSource.imp ?: ''}" style="display:none"
                                        data-for="rackSourceId">
                                        %{--<tdsAngular:tooltipSpan class="useRackS" field="${standardFieldSpecs.sourceRack}"--}%
                                        %{--tooltipDataPlacement="bottom">--}%
                                        %{--<g:render template="deviceRackSelect"--}%
                                        %{--model="[clazz    : standardFieldSpecs.sourceRack?.imp ?: '', options: sourceRackSelect, rackId: assetEntityInstance?.rackSource?.id,--}%
                                        %{--rackDomId: 'rackSourceId', rackDomName: 'rackSourceId', sourceTarget: 'S', forWhom: 'Edit', tabindex: '310']"/>--}%
                                        %{--</tdsAngular:tooltipSpan>--}%
                                        %{--<tdsAngular:tooltipSpan class="newRackS" tooltipDataPlacement="bottom"--}%
                                        %{--field="${standardFieldSpecs.sourceRack}">--}%
                                        %{--<input type="text" id="sourceRackId" name="sourceRack" value=""--}%
                                        %{--placeholder="New rack name"--}%
                                        %{--class="${standardFieldSpecs.sourceRack?.imp ?: ''}"--}%
                                        %{--xstyle="display:none"--}%
                                        %{--size=20 tabindex="311"/>--}%
                                        %{--<input type="hidden" id="newRackSourceId" name="newRackSourceId" value="-1">--}%
                                        %{--</tdsAngular:tooltipSpan>--}%
                                    </td>
                                    <td class="label rackLabel ${standardFieldSpecs.rackSource.imp ?: ''}" style="display:none"
                                        data-for="rackSourceId">
                                        %{--<tdsAngular:tooltipSpan class="useRackT" tooltipDataPlacement="bottom"--}%
                                        %{--field="${standardFieldSpecs.rackTarget}">--}%
                                        %{--<g:render template="deviceRackSelect"--}%
                                        %{--model="[clazz    : standardFieldSpecs.rackTarget.imp ?: '', options: targetRackSelect, rackId: asset.rackTarget?.id,--}%
                                        %{--rackDomId: 'rackTargetId', rackDomName: 'rackTargetId', sourceTarget: 'T', forWhom: 'Edit', tabindex: '340']"/>--}%
                                        %{--</tdsAngular:tooltipSpan>--}%
                                        %{--<tdsAngular:tooltipSpan class="newRackT" tooltipDataPlacement="bottom"--}%
                                        %{--field="${standardFieldSpecs.rackTarget}">--}%
                                        %{--<input type="text" id="rackTargetId" name="rackTarget" value=""--}%
                                        %{--placeholder="New rack name"--}%
                                        %{--class="${standardFieldSpecs.rackTarget.imp ?: ''}"--}%
                                        %{--xstyle="display:none"--}%
                                        %{--size=20 tabindex="341"/>--}%
                                        %{--<input type="hidden" id="newRackTargetId" name="newRackTargetId" value="-1">--}%
                                        %{--</tdsAngular:tooltipSpan>--}%
                                    </td>
                                    <td class="label bladeLabel" style="display:none">
                                        %{--<tdsAngular:tooltipSpan class="useBladeS" tooltipDataPlacement="bottom"--}%
                                        %{--field="${standardFieldSpecs.sourceChassis}">--}%
                                        %{--<g:render template="deviceChassisSelect"--}%
                                        %{--model="[domId       : 'sourceChassisSelectId', domName: 'sourceChassis',--}%
                                        %{--options     : sourceChassisSelect, value: asset.sourceChassis?.id,--}%
                                        %{--domClass    : standardFieldSpecs.sourceChassis.imp ?: '',--}%
                                        %{--sourceTarget: 'S', forWhom: '$forWhom', tabindex: '312']"/>--}%
                                        %{--</tdsAngular:tooltipSpan>--}%
                                    </td>
                                    <td class="label bladeLabel" style="display:none">
                                        %{--<tdsAngular:tooltipSpan class="useBladeT" tooltipDataPlacement="bottom"--}%
                                        %{--field="${standardFieldSpecs.targetChassis}">--}%
                                        %{--<g:render template="deviceChassisSelect"--}%
                                        %{--model="[domId       : 'targetChassisSelectId', domName: 'targetChassis',--}%
                                        %{--options     : targetChassisSelect, value: asset.targetChassis?.id,--}%
                                        %{--domClass    : standardFieldSpecs.targetChassis.imp ?: '',--}%
                                        %{--sourceTarget: 'T', forWhom: '$forWhom', tabindex: '342']"/>--}%
                                        %{--</tdsAngular:tooltipSpan>--}%
                                    </td>
                                </tr>
                                <tr>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${asset.shortName}"
                                                                   tabindex="105" ngmodel="model.asset.shortName"/>
                                    <tdsAngular:inputLabelAndField field="${standardFieldSpecs.os}" value="${asset.os}"
                                                                   tabindex="220" ngmodel="model.asset.os" />
                                    <%-- Note that the next set of fields are toggled on/off based on the assetType selected --%>
                                    <td class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp ?: ''}" style="display: none;" nowrap="nowrap">
                                        <label for="sourceRackPositionId" data-toggle="popover" data-trigger="hover"
                                               data-content="Position">Position</label>
                                    </td>
                                    <td class="rackLabel" style="display: none;"
                                        data-content="${standardFieldSpecs.sourceRackPosition.tip ?: standardFieldSpecs.sourceRackPosition.label}">
                                        <tdsAngular:tooltipSpan class="sourceRackPositionT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRackPosition}">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.sourceRackPosition}"
                                                                     size="10" tabindex="320"
                                                                     value="${asset.sourceRackPosition}"
                                                                     ngmodel="model.asset.sourceRackPosition">
                                            </tdsAngular:inputControl>
                                        </tdsAngular:tooltipSpan>
                                    </td>
                                    <td class="rackLabel" style="display: none;" data-toggle="popover" data-trigger="hover" data-placement="bottom"
                                        data-content="${standardFieldSpecs.targetRackPosition.tip ?: standardFieldSpecs.targetRackPosition.label}">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.targetRackPosition}"
                                                                 size="10" tabindex="350" value="${asset.targetRackPosition}"
                                                                 ngmodel="model.asset.targetRackPosition">
                                        </tdsAngular:inputControl>
                                    </td>
                                    <td class="bladeLabel ${standardFieldSpecs.sourceRackPosition.imp ?: ''}" style="display: none;"
                                        data-toggle="popover" data-placement="bottom" data-trigger="hover"
                                        data-content="${standardFieldSpecs.sourceBladePosition.tip ?: standardFieldSpecs.sourceBladePosition.label}">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.sourceBladePosition}"
                                                                 size="10" tabindex="320" value="${asset.sourceBladePosition}"
                                                                 ngmodel="model.asset.sourceBladePosition">
                                        </tdsAngular:inputControl>
                                    </td>
                                    <td class="bladeLabel" style="display: none;" data-toggle="popover" data-trigger="hover" data-placement="bottom"
                                        data-content="${standardFieldSpecs.targetBladePosition.tip ?: standardFieldSpecs.targetBladePosition.label}">
                                        <tdsAngular:inputControl field="${standardFieldSpecs.targetBladePosition}"
                                                                 size="10" tabindex="350" value="${asset.targetBladePosition}"
                                                                 ngmodel="model.asset.targetBladePosition">
                                        </tdsAngular:inputControl>
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

%{--<g:form method="post" name="createEditAssetForm" action="${action}">--}%

	%{--<%-- TODO : JPM 10/2014 - Need to determine if the filter params are necessary--%>--}%
	%{--<%----}%
	%{--These properties are some how used by the JQGrid logic for filtering. I am unsure if they are really--}%
	%{--necessary as we use user selectable columns as well and we do not have any references for them--}%
	%{----%>--}%
	%{--<input type="hidden" id="asset_assetName" 		name="assetNameFilter" value="" />--}%
	%{--<input type="hidden" id="asset_assetTag" 		name="assetTagFilter" value="" />--}%
	%{--<input type="hidden" id="asset_assetType" 		name="assetTypeFilter" value="" />--}%
	%{--<input type="hidden" id="asset_model" 			name="modelFilter" value="" />--}%
	%{--<input type="hidden" id="asset_moveBundle" 		name="moveBundleFilter" value="" />--}%
	%{--<input type="hidden" id="asset_planStatus" 		name="planStatusFilter" value="" />--}%
	%{--<input type="hidden" id="asset_serialNumber" 	name="serialNumberFilter" value="" />--}%
	%{--<input type="hidden" id="asset_locationSource" 	name="locationSourceFilter" value="" />--}%
	%{--<input type="hidden" id="asset_rackSource" 		name="rackSourceFilter" value="" />--}%
	%{--<input type="hidden" id="asset_locationTarget" 	name="locationTargetFilter" value="" />--}%
	%{--<input type="hidden" id="asset_rackTarget" 		name="trackTargetFilter" value="" />--}%

	%{--<%-- Flow control parameters --%>--}%
	%{--<input type="hidden" id="dstPath" 				name="dstPath" value ="${redirectTo}"/>--}%
	%{--<input type="hidden" id="redirectTo" 			name="redirectTo" value="${redirectTo}">--}%

	%{--<%-- Key field and optimistic locking var --%>--}%
	%{--<input type="hidden" id="assetId" 				name="id" value="${assetEntityInstance?.id}"/>--}%
	%{--<input type="hidden" id="version" 				name="version" value="${version}"/>--}%

	%{--<input type="hidden" id="attributeSet.id"		name="attributeSet.id" 	value="1"/>--}%

	%{--<%-- Used to maintain the selected AssetType --%>--}%
	%{--<input type="hidden" id="currentAssetType" 		name="currentAssetType" value="${currentAssetType}"/>--}%

	%{--<%-- Used to track dependencies added and deleted --%>--}%
	%{--<g:render template="../assetEntity/dependentHidden" />--}%

	%{--<%-- Holds original values of the various SELECTS --%>--}%
	%{--<input type="hidden" id="hiddenModel"        name="modelId" value="${assetEntityInstance?.model?.id}">--}%
	%{--<input type="hidden" id="hiddenManufacturer" name="manufacturerId" value="${assetEntityInstance.manufacturer?.id}">--}%
	%{--<input type="hidden" id="deviceChassisIdS" value="${assetEntityInstance?.sourceChassis?.id}"/>--}%
	%{--<input type="hidden" id="deviceChassisIdT" value="${assetEntityInstance?.targetChassis?.id}"/>--}%
	%{--<input type="hidden" id="deviceRackIdS" value="${assetEntityInstance?.rackSource?.id}"/>--}%
	%{--<input type="hidden" id="deviceRackIdT" value="${assetEntityInstance?.rackTarget?.id}"/>--}%
	%{--<input type="hidden" id="deviceRoomIdS" value="${assetEntityInstance?.roomSource?.id}"/>--}%
	%{--<input type="hidden" id="deviceRoomIdT" value="${assetEntityInstance?.roomTarget?.id}"/>--}%

	%{--<%-- Not sure what these are used for (jpm 9/2014) --%>--}%
	%{--<%-- TODO : JPM 9/2014 : Note that the fields with id containing dot (.) can not be referenced by JQuery $('#project.id') therefore it begs the question if they are used/needed --%>--}%
	%{--<input type="hidden" id="project.id"			name="project.id" value="${projectId}"/>--}%
	%{--<input type="hidden" id="labelsListId" 			name="labels" value =""/>--}%
	%{--<input type="hidden" id="tabType" 				name="tabType" value =""/>--}%
	%{--<input type="hidden" id="updateView" 			name="updateView" value="">--}%
	%{--<input type="hidden" id="updateViewId" 			name="updateViewId" value=""/>--}%

	%{--<table style="border:0;width:1000px;" class="ui-widget">--}%
		%{--<tr>--}%
			%{--<td colspan="2">--}%
				%{--<div class="dialog">--}%
					%{--<table>--}%
						%{--<tbody>--}%
						%{--<tr>--}%
							%{--<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${assetEntityInstance.assetName}"/>--}%
							%{--<td colspan="3">--}%
								%{--<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="100" value="${assetEntityInstance.assetName}" />--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.description}" value="${assetEntityInstance.description}"/>--}%
							%{--<td colspan="3">--}%
								%{--<tds:inputControl field="${standardFieldSpecs.description}" tabindex="101" value="${assetEntityInstance.description}" tooltipDataPlacement="bottom"/>--}%
							%{--</td>--}%
						%{--</tr>--}%
						%{--<tr>--}%
							%{--<td class="label ${standardFieldSpecs.assetType.imp?:''}" nowrap="nowrap">--}%
								%{--<label for="model">--}%
									%{--<span id="assetTypeLabel" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip?: standardFieldSpecs.assetType.label}">--}%
										%{--${standardFieldSpecs.assetType.label}--}%
									%{--</span>--}%
								%{--</label>--}%
							%{--</td>--}%
							%{--<td class="${standardFieldSpecs.assetType.imp?:''}" data-for="model" style="border-top: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">--}%
								%{--<div id="modelEditId" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip?: standardFieldSpecs.assetType.label}">--}%
									%{--<div id="assetTypeSelect" tabindex="102">--}%
									%{--</div>--}%
								%{--</div>--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.environment}" value="${assetEntityInstance.environment}"/>--}%
							%{--<td>--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.environment}">--}%
								%{--<g:select id="environment" name="environment" class="${standardFieldSpecs.environment.imp?:''}" from="${environmentOptions}"--}%
										  %{--value="${assetEntityInstance.environment}" noSelection="${['':'Please select...']}"--}%
										  %{--tabindex="205"--}%
										%{--/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
							%{--<td colspan="1"></td>--}%
							%{--<td class="label_sm">Source</td>--}%
							%{--<td class="label_sm">Target</td>--}%
						%{--</tr>--}%
						%{--<tr>--}%
							%{--<td class="label ${standardFieldSpecs.manufacturer.imp?:''}" nowrap="nowrap">--}%
								%{--<g:if test="${assetEntityInstance.manufacturer?.id}">--}%
									%{--<label for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">--}%
										%{--<a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a>--}%
									%{--</label>--}%
								%{--</g:if>--}%
								%{--<g:else>--}%
									%{--<label for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">--}%
										%{--${standardFieldSpecs.manufacturer.label}--}%
									%{--</label>--}%
								%{--</g:else>--}%
							%{--</td>--}%
							%{--<td class="${standardFieldSpecs.manufacturer.imp?:''}" data-for="manufacturer" style="border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">--}%
								%{--<div id="manufacturerEditId" style="display:inline" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">--}%
									%{--<div id="manufacturerSelect" tabindex="103">--}%
									%{--</div>--}%
								%{--</div>--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.priority}" value="${assetEntityInstance.priority}"/>--}%
							%{--<td>--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.priority}">--}%
								%{--<g:select id="priority" name ="priority"--}%
										  %{--from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':'Please select...']}"--}%
										  %{--class="${standardFieldSpecs.priority.imp?:''}" tabindex="210"--}%
										%{--/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
							%{--<td class="label {standardFieldSpecs.locationSource.imp?:''}" nowrap="nowrap">--}%
								%{--<label for="locationSourceId">Location/Room</label>--}%
							%{--</td>--}%
							%{--<td class="${standardFieldSpecs.locationSource.imp?:''}" style="vertical-align: text-top;" data-for="locationSourceId">--}%
									%{--<tds:tooltipSpan class="useRoomS" field="${standardFieldSpecs.locationSource}">--}%
										%{--<g:select id="roomSelectS"  name="roomSourceId"--}%
												  %{--from="${sourceRoomSelect}" value="${assetEntityInstance.roomSource?.id}"--}%
												  %{--optionKey="id" optionValue="${{it.value}}"--}%
												  %{--noSelection="${[0:'Please select...']}"--}%
												  %{--class="${standardFieldSpecs.locationSource.imp?:''} assetSelect roomSelectS"--}%
												  %{--onchange="EntityCrud.updateOnRoomSelection(this, 'S', 'Edit')"--}%
												  %{--tabindex="300"--}%
												%{--/>--}%
									%{--</tds:tooltipSpan>--}%
								%{--<%-- Theses fields are used to allow user to create a source room on the fly --%>--}%
									%{--<span class="newRoomS" style="display:none" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.locationSource.tip?: standardFieldSpecs.locationSource.label}">--}%
										%{--<input type="text" id="locationSourceId" name="locationSource" value=""--}%
											   %{--placeholder="Location"--}%
											   %{--class="${standardFieldSpecs.locationSource.imp?:''}"--}%
											   %{--size=10--}%
											   %{--tabindex="301"--}%
												%{--/>--}%
										%{--<input type="text" id="roomSourceId" name="roomSource" value=""--}%
											   %{--placeholder="Room Name"--}%
											   %{--class="${standardFieldSpecs.roomSource.imp?:''}"--}%
											   %{--size=10--}%
											   %{--tabindex="302"--}%
												%{--/>--}%
									%{--</span>--}%
							%{--</td>--}%
							%{--<td nowrap style="vertical-align: text-top;" class="${standardFieldSpecs.locationSource.imp?:''}" data-for="locationSourceId">--}%
									%{--<tds:tooltipSpan class="useRoomT" field="${standardFieldSpecs.locationTarget}">--}%
										%{--<g:select id="roomSelectT" name="roomTargetId"--}%
												  %{--from="${targetRoomSelect}" value="${assetEntityInstance.roomTarget?.id}"--}%
												  %{--optionKey="id" optionValue="${{it.value}}"--}%
												  %{--noSelection="${[0:'Please select...']}"--}%
												  %{--class="${standardFieldSpecs.locationTarget.imp?:''} assetSelect roomSelectT"--}%
												  %{--onchange="EntityCrud.updateOnRoomSelection(this, 'T', 'Edit')"--}%
												  %{--tabindex="330"--}%
												%{--/>--}%
									%{--</tds:tooltipSpan>--}%
								%{--<%-- Theses fields are used to allow user to create a source room on the fly --%>--}%
									%{--<span class="newRoomT" style="display:none" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.locationTarget.tip?: standardFieldSpecs.locationTarget.label}">--}%
										%{--<br/>--}%
										%{--<input type="text" id="locationTarget" name="locationTarget" value=""--}%
											   %{--placeholder="Location"--}%
											   %{--class="${standardFieldSpecs.locationTarget.imp?:''}"--}%
											   %{--size=10 tabindex="331"--}%
												%{--/>--}%
										%{--<input type="text" id="roomTargetId" name="roomTarget" value=""--}%
											   %{--placeholder="Room Name"--}%
											   %{--class="${standardFieldSpecs.roomTarget.imp?:''}"--}%
											   %{--size=10 tabindex="332"--}%
												%{--/>--}%
									%{--</span>--}%
							%{--</td>--}%

						%{--</tr>--}%
						%{--<tr>--}%
							%{--<tds:inputLabel field="${standardFieldSpecs.model}" value="${assetEntityInstance.model}"/>--}%
							%{--<td class="${standardFieldSpecs.model.imp?:''}" data-for="assetType"  style="border-bottom: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.model}">--}%
								%{--<div id="modelSelect" tabindex="104">--}%
								%{--</div>--}%
								%{--<input type="hidden" value="${assetEntityInstance?.model?.id}" id="hiddenModel" name="model">--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<td class="label ${standardFieldSpecs.ipAddress.imp?:''}" nowrap="nowrap">--}%
								%{--<label for="ipAddress" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.ipAddress.tip ?: standardFieldSpecs.ipAddress.label}">--}%
									%{--${standardFieldSpecs.ipAddress.label}--}%
								%{--</label>--}%
							%{--</td>--}%
							%{--<td>--}%
								%{--<tds:tooltipSpan field="${standardFieldSpecs.ipAddress}">--}%
									%{--<input type="text" id="ipAddress" name="ipAddress"--}%
										   %{--value="${assetEntityInstance.ipAddress}"--}%
										   %{--class="${standardFieldSpecs.ipAddress.imp?:''}" tabindex="215"--}%
											%{--/>--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<td class="label rackLabel ${standardFieldSpecs.sourceRack.imp?:''}" nowrap="nowrap" id="rackId">--}%
								%{--<label for="sourceRackId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cabinet" >Rack/Cabinet</label>--}%
							%{--</td>--}%
							%{--<td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display:none">--}%
								%{--<label for="sourceChassisId" data-toggle="popover" data-trigger="hover" data-content="Blade Chassis">Blade Chassis</label>--}%
							%{--</td>--}%

							%{--<td class="label rackLabel ${standardFieldSpecs.sourceRack.imp?:''}" data-for="sourceRackId">--}%
								%{--<tds:tooltipSpan class="useRackS" field="${standardFieldSpecs.sourceRack}" tooltipDataPlacement="bottom">--}%
									%{--<g:render template="deviceRackSelect" model="[clazz:standardFieldSpecs.sourceRack.imp?:'', options:sourceRackSelect, rackId:assetEntityInstance?.rackSource?.id,--}%
																				  %{--rackDomId:'rackSourceId', rackDomName:'rackSourceId', sourceTarget:'S', forWhom:'Edit', tabindex:'310']" />--}%
								%{--</tds:tooltipSpan>--}%
								%{--<tds:tooltipSpan class="newRackS" tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRack}">--}%
									%{--<input type="text" id="sourceRackId" name="sourceRack" value=""--}%
										   %{--placeholder="New rack name"--}%
										   %{--class="${standardFieldSpecs.sourceRack.imp?:''}"--}%
										   %{--xstyle="display:none"--}%
										   %{--size=20 tabindex="311"--}%
									%{--/>--}%
									%{--<input type="hidden" id="newRackSourceId" name="newRackSourceId" value="-1">--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
							%{--<td class="label rackLabel ${standardFieldSpecs.sourceRack.imp?:''}" data-for="sourceRackId">--}%
								%{--<tds:tooltipSpan class="useRackT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.rackTarget}">--}%
									%{--<g:render template="deviceRackSelect"  model="[clazz:standardFieldSpecs.rackTarget.imp?:'', options:targetRackSelect, rackId: assetEntityInstance.rackTarget?.id,--}%
																				   %{--rackDomId:'rackTargetId', rackDomName:'rackTargetId', sourceTarget:'T', forWhom:'Edit', tabindex:'340']" />--}%
								%{--</tds:tooltipSpan>--}%
								%{--<tds:tooltipSpan class="newRackT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.rackTarget}">--}%
									%{--<input type="text" id="rackTargetId" name="rackTarget" value=""--}%
										   %{--placeholder="New rack name"--}%
										   %{--class="${standardFieldSpecs.rackTarget.imp?:''}"--}%
										   %{--xstyle="display:none"--}%
										   %{--size=20 tabindex="341" />--}%
									%{--<input type="hidden" id="newRackTargetId" name="newRackTargetId" value="-1">--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<td class="label bladeLabel" style="display:none">--}%
								%{--<tds:tooltipSpan class="useBladeS" tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceChassis}">--}%
									%{--<g:render template="deviceChassisSelect"--}%
											  %{--model="[ domId:'sourceChassisSelectId', domName:'sourceChassis',--}%
													   %{--options:sourceChassisSelect, value:assetEntityInstance.sourceChassis?.id,--}%
													   %{--domClass: standardFieldSpecs.sourceChassis.imp?:'',--}%
													   %{--sourceTarget:'S', forWhom:'$forWhom', tabindex:'312']"--}%
									%{--/>--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
							%{--<td class="label bladeLabel" style="display:none">--}%
								%{--<tds:tooltipSpan class="useBladeT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.targetChassis}">--}%
									%{--<g:render template="deviceChassisSelect"--}%
											  %{--model="[ domId:'targetChassisSelectId', domName:'targetChassis',--}%
													   %{--options:targetChassisSelect, value:assetEntityInstance.targetChassis?.id,--}%
													   %{--domClass: standardFieldSpecs.targetChassis.imp?:'',--}%
													   %{--sourceTarget:'T', forWhom:'$forWhom', tabindex:'342']"--}%
									%{--/>--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%


						%{--</tr>--}%
						%{--<tr>--}%
							%{--<tds:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntityInstance.shortName}" tabindex="105"/>--}%

							%{--<tds:inputLabelAndField field="${standardFieldSpecs.os}" value="${assetEntityInstance.os}" tabindex="220"/>--}%

							%{--<%-- Note that the next set of TDs are toggled on/off based on the assetType selected --%>--}%
							%{--<td class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}" nowrap="nowrap">--}%
								%{--<label for="sourceRackPositionId" data-toggle="popover" data-trigger="hover" data-content="Position">Position</label>--}%
							%{--</td>--}%
							%{--<td class="rackLabel" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.sourceRackPosition.tip?: standardFieldSpecs.sourceRackPosition.label}">--}%
								%{--<input type="text" id="sourceRackPositionId" name="sourceRackPosition"--}%
									   %{--value="${assetEntityInstance.sourceRackPosition}"--}%
									   %{--placeholder="U position"--}%
									   %{--class="${standardFieldSpecs.sourceRackPosition.imp?:''} useRackS"--}%
									   %{--size=10 tabindex="320"--}%
								%{--/>--}%
							%{--</td>--}%
							%{--<td class="rackLabel" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.targetRackPosition.tip?: standardFieldSpecs.targetRackPosition.label}">--}%
								%{--<input type="text" id="targetRackPositionId" name="targetRackPosition"--}%
									   %{--value="${assetEntityInstance.targetRackPosition}"--}%
									   %{--placeholder="U position"--}%
									   %{--class="${standardFieldSpecs.targetRackPosition.imp?:''} useRackT"--}%
									   %{--size=10 tabindex="350" />--}%
							%{--</td>--}%
							%{--<td class="bladeLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}" data-toggle="popover" data-placement="bottom" data-trigger="hover" data-content="${standardFieldSpecs.sourceBladePosition.tip?: standardFieldSpecs.sourceBladePosition.label}">--}%
								%{--<input type="text" id="sourceBladePositionId" name="sourceBladePosition"--}%
									   %{--value="${assetEntityInstance.sourceBladePosition}"--}%
									   %{--placeholder="Chassis position"--}%
									   %{--class="${standardFieldSpecs.sourceRackPosition.imp?:''} useBladeS"--}%
									   %{--size=10 tabindex="320"--}%
								%{--/>--}%
							%{--</td>--}%
							%{--<td class="bladeLabel" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.targetBladePosition.tip?: standardFieldSpecs.targetBladePosition.label}">--}%
								%{--<input type="text" id="targetRackPositionId" name="targetBladePosition"--}%
									   %{--value="${assetEntityInstance.targetBladePosition}"--}%
									   %{--placeholder="Chassis position"--}%
									   %{--class="${standardFieldSpecs.targetRackPosition.imp?:''} useBladeT"--}%
									   %{--size=10 tabindex="350"--}%
								%{--/>--}%
							%{--</td>--}%

						%{--</tr>--}%
						%{--<tr>--}%
							%{--<tds:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntityInstance.serialNumber}" tabindex="106"/>--}%

							%{--<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntityInstance.supportType}" tabindex="225"/>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetEntityInstance.moveBundle?.id}"/>--}%
							%{--<td>--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}">--}%
								%{--<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id"--}%
										  %{--value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name"--}%
										  %{--class="${standardFieldSpecs.moveBundle.imp?:''}"--}%
										  %{--tabindex="360"--}%
										%{--/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.size}" value="${assetEntityInstance.size}"/>--}%
							%{--<td nowrap="nowrap" class="sizeScale">--}%
							%{--<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.size}">--}%
								%{--<input type="text" id="size" name="size" class="${standardFieldSpecs.size.imp?:''}" value="${assetEntityInstance.size}" tabindex="410"/>--}%
								%{--<g:select id="scale" name="scale"--}%
										  %{--from="${assetEntityInstance.constraints.scale.inList}"--}%
										  %{--optionValue="value" noSelection="${['':'Please select...']}"--}%
										  %{--value="${assetEntityInstance.scale}"--}%
										  %{--class="${standardFieldSpecs.scale.imp?:''}"--}%
										  %{--tabindex="412"--}%
										%{--/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
						%{--</tr>--}%
						%{--<tr>--}%
							%{--<tds:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntityInstance.assetTag}" tabindex="107"/>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntityInstance.retireDate}"/>--}%
							%{--<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">--}%
								%{--<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">--}%
									%{--<script type="text/javascript" charset="utf-8">--}%
										%{--jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat()  }); });--}%
									%{--</script>--}%
									%{--<input type="text" id="retireDate" name="retireDate"--}%
										%{--value="<tds:convertDate date="${assetEntityInstance?.retireDate}" />"--}%
										%{--class="dateRange ${standardFieldSpecs.retireDate.imp?:''}"--}%
										%{--size="15" style="width: 138px;"--}%
										%{--tabindex="230"--}%
									%{--/>--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetEntityInstance.planStatus}"/>--}%
							%{--<td>--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.planStatus}">--}%
								%{--<g:select id="planStatus" name ="planStatus"--}%
										  %{--from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}"--}%
										  %{--noSelection="${['':'Please select']}"--}%
										  %{--class="${standardFieldSpecs.planStatus.imp?:''}"--}%
										  %{--tabindex="365"--}%
										%{--/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<tds:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntityInstance.rateOfChange}" tabindex="420" tooltipDataPlacement="bottom"/>--}%
						%{--</tr>--}%
						%{--<tr>--}%
							%{--<tds:inputLabel field="${standardFieldSpecs.railType}" value="${assetEntityInstance.railType}"/>--}%
							%{--<td>--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.railType}">--}%
								%{--<g:select id="railType" name ="railType"--}%
										  %{--from="${railTypeOption}" value= "${assetEntityInstance.railType}"--}%
										  %{--noSelection="${['':'Please select...']}"--}%
										  %{--class="${standardFieldSpecs.railType.imp?:''}"--}%
										  %{--tabindex="108"/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntityInstance.maintExpDate}"/>--}%
							%{--<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">--}%
							%{--<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">--}%
								%{--<input type="text" id="maintExpDate" name="maintExpDate"--}%
									%{--value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" />"--}%
									%{--class="dateRange ${standardFieldSpecs.maintExpDate.imp?:''}"--}%
									%{--size="15" style="width: 138px;"--}%
									%{--tabindex="235"--}%
								%{--/>--}%
							%{--</tds:tooltipSpan>--}%
							%{--</td>--}%

							%{--<tds:inputLabel field="${standardFieldSpecs.validation}" value="${assetEntityInstance.validation}"/>--}%
							%{--<td  colspan="2">--}%
								%{--<tds:tooltipSpan field="${standardFieldSpecs.validation}">--}%
									%{--<g:select id="validation" name="validation"--}%
										  %{--from="${assetEntityInstance.constraints.validation.inList}"--}%
										  %{--value="${assetEntityInstance.validation}"--}%
										  %{--class="${standardFieldSpecs.validation.imp?:''}"--}%
										  %{--tabindex="370"--}%
										%{--/>--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
						%{--</tr>--}%
						%{--<tr>--}%
							%{--<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntityInstance.externalRefId}" tabindex="109"/>--}%

							%{--<td class="label ${standardFieldSpecs.truck.imp?:''}" nowrap="nowrap">--}%
								%{--<label for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">Truck/Cart/Shelf</label>--}%
							%{--</td>--}%
							%{--<td>--}%
								%{--<tds:tooltipSpan field="${standardFieldSpecs.truck}">--}%
									%{--<input type="text" id="truck" class="${standardFieldSpecs.truck.imp?:''}" name="truck" value="${assetEntityInstance.truck}" size=3 tabindex="240" />--}%
									%{--<input type="text" id="cart" class="${standardFieldSpecs.cart.imp?:''}" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="241" />--}%
									%{--<input type="text" id="shelf" class="${standardFieldSpecs.shelf.imp?:''}" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="242" />--}%
								%{--</tds:tooltipSpan>--}%
							%{--</td>--}%
						%{--</tr>--}%
						%{--<tbody class="customTemplate">--}%
						%{--<g:render template="customEdit"></g:render>--}%
						%{--</tbody>--}%
						%{--</tbody>--}%
					%{--</table>--}%
				%{--</div></td>--}%
		%{--</tr>--}%

		%{--<g:if test="${action == 'save'}">--}%
			%{--<tr>--}%
				%{--<g:render template="dependentCreateEdit" model="[whom:'create', supportAssets:[], dependentAssets:[]]"></g:render>--}%
			%{--</tr>--}%
		%{--</g:if><g:else>--}%
		%{--<tr id="assetDependentId" class="assetDependent">--}%
			%{--<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/> </span></td>--}%
		%{--</tr>--}%
	%{--</g:else>--}%

		%{--<tr>--}%
			%{--<td colspan="2">--}%
				%{--<div class="buttons">--}%
					 %{--<span class="button">--}%
					 	%{--<input type="button" class="save updateDep" data-redirect='${redirectTo}' data-action='show'--}%
							   %{--value="${actionLabel}" onclick="EntityCrud.${jsAction}($(this), '${assetEntityInstance.assetClass}'); " />--}%
					 %{--</span>--}%
					%{--<tds:hasPermission permission="${Permission.AssetDelete}">--}%
						 %{--<span class="button"><g:actionSubmit class="delete"--}%
															  %{--onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>--}%
					%{--</tds:hasPermission>--}%
					%{--<span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#createEntityView').dialog('close'); $('#showEntityView').dialog('close'); $('#editEntityView').dialog('close');"/> </span>--}%
				%{--</div>--}%
			%{--</td>--}%
		%{--</tr>--}%
	%{--</table>--}%
%{--</g:form>--}%

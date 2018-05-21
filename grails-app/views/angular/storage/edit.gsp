<%@ page import="com.tds.asset.Files" %>
<%@ page import="net.transitionmanager.security.Permission" %>
<%@ page import="grails.converters.JSON"%>
<%@ page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%-- <g:set var="assetClass" value="${(new Files()).assetClass}" /> --%>
<g:set var="assetClass" value="Files"/>


<div class="modal-content tds-angular-component-content" tabindex="0">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span
                aria-hidden="true">×</span></button>
        <h4 class="modal-title">Storage Edit</h4>
    </div>

    <div class="modal-body">
        <div>
            <form name="storageEditForm">
                <table style="border: 0;">
                    <tr>
                        <td colspan="2">
                            <div class="dialog">
                                <table>
                                    <tbody>
                                    <tr>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
                                        <td colspan="3">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="10" value="${asset.assetName}" ngmodel="model.asset.assetName" />
                                        </td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
                                        <td colspan="3">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${asset.description}"
                                                                     ngmodel="model.asset.description" tooltipDataPlacement="bottom" />
                                        </td>

                                    </tr>
                                    <tr>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.fileFormat}"
                                                                       value="${asset.fileFormat}" ngmodel="model.asset.fileFormat" tabindex="12"/>

                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.LUN}"
                                                                       value="${asset.LUN}" ngmodel="model.asset.LUN" tabindex="22"/>

                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}"
                                                                       value="${asset.supportType}"  ngmodel="model.asset.supportType" tabindex="32"/>

                                        <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle}"/>
                                        <td>
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}" tooltipDataPlacement="bottom">
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
                                    </tr>

                                    <tr>
                                        <td class="label ${standardFieldSpecs.size.imp ?: ''}" nowrap="nowrap">
                                            <label for="size" data-toggle="popover" data-trigger="hover"
                                                   data-content="${standardFieldSpecs.size.tip ?: standardFieldSpecs.size.label}">
                                                ${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
                                            </label>
                                        </td>
                                        <td nowrap="nowrap" class="sizeScale">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.size}" tabindex="13"
                                                                     value="${asset.size}" ngmodel="model.asset.size"/>&nbsp;
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
                                            %{-- <g:select from="${filesInstance.constraints.scale.inList}" name="scale"
                                                       id="scale" tabindex="13"
                                                       value="${filesInstance.scale}" optionValue="value"
                                                       noSelection="${['': ' Please Select']}"/>
                                             --}%
                                            </tdsAngular:tooltipSpan>
                                        </td>

                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}"
                                                                       value="${asset.externalRefId}" tabindex="23" ngmodel="model.asset.externalRefId"/>

                                        <tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
                                        <td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">
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

                                        <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
                                        <td>
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.planStatus}" tooltipDataPlacement="bottom">
                                                <kendo-dropdownlist
                                                        class="select"
                                                        name="modelAssetPlanStatus"
                                                        [data]="model.planStatusOptions"
                                                        [(ngModel)]="model.asset.planStatus">
                                                </kendo-dropdownlist>
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                    </tr>
                                    <tr>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
                                        <td>
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.rateOfChange}">
                                                <tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4"
                                                                         value="${asset.rateOfChange}" tabindex="14" ngmodel="model.asset.rateOfChange"/>
                                            </tdsAngular:tooltipSpan>
                                        </td>

                                        <td colspan="2">
                                        <td colspan="2">

                                            <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
                                        <td>
                                            <tdsAngular:tooltipSpan field="${standardFieldSpecs.validation}"
                                                                    tooltipDataPlacement="bottom">
                                                <kendo-dropdownlist
                                                        class="select"
                                                        name="modelAssetValidation"
                                                        [data]="${asset.constraints.validation.inList as JSON}"
                                                        [(ngModel)]="model.asset.validation">
                                                </kendo-dropdownlist>
                                            </tdsAngular:tooltipSpan>
                                        </td>
                                    </tr>
                                    %{--<tbody class="customTemplate">--}%
                                    <g:render template="/angular/common/customEdit" model="[assetEntityInstance: filesInstance]"></g:render>
                                    %{--</tbody>--}%
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
            </form>
        </div>
    </div>

    <div class="modal-footer form-group-center">
        <button class="btn btn-primary pull-left" type="button" (click)="onUpdate()"><span
                class="fa fa-fw fa-floppy-o"></span> Update</button>
        <tds:hasPermission permission="${Permission.AssetDelete}">
            <button class="btn btn-danger pull-left mar-left-50" (click)="onDelete()" type="button"><span
                    class="glyphicon glyphicon-trash"></span> Delete</button>
        </tds:hasPermission>
        <button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span
                class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
    </div>
</div>
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
                <table>
                    <tr>
                        <td colspan="2" class="dialog-container">
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
                                            <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${asset.description}" ngmodel="model.asset.description" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${asset.fileFormat}" ngmodel="model.asset.fileFormat" tabindex="12"/>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${asset.LUN}" ngmodel="model.asset.LUN" tabindex="22"/>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"  ngmodel="model.asset.supportType" tabindex="32"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle}"/>
                                        <td class="${standardFieldSpecs.moveBundle.imp ?: ''}" data-for="moveBundle">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetMoveBundle"
                                                    [data]="model.moveBundleList"
                                                    [(ngModel)]="model.asset.moveBundle"
                                                    [textField]="'name'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="label ${standardFieldSpecs.size.imp ?: ''}" nowrap="nowrap">
                                            <label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip ?: standardFieldSpecs.size.label}">
                                                ${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
                                            </label>
                                        </td>
                                        <td data-for="sizeScale" nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp ?: ''}">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.size}" tabindex="13" value="${asset.size}" ngmodel="model.asset.size"/>&nbsp;
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetScaleName"
                                                    [data]="${SizeScale.getAsJsonList() as JSON}"
                                                    [(ngModel)]="model.asset.scale.name"
                                                    [defaultItem]="''"
                                                    [textField]="'text'"
                                                    [valueField]="'value'">
                                            </kendo-dropdownlist>
                                        </td>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" tabindex="23" ngmodel="model.asset.externalRefId"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
                                        <td class="${standardFieldSpecs.environment.imp ?: ''}" data-for="environment" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetEnvironment"
                                                    [(ngModel)]="model.asset.environment"
                                                    [defaultItem]="'Please Select'"
                                                    [data]="model.environmentOptions">
                                            </kendo-dropdownlist>
                                        </td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
                                        <td class="${standardFieldSpecs.planStatus.imp ?: ''}" data-for="planStatus">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetPlanStatus"
                                                    [data]="model.planStatusOptions"
                                                    [(ngModel)]="model.asset.planStatus">
                                            </kendo-dropdownlist>
                                        </td>
                                    </tr>
                                    <tr>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
                                        <td>
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" value="${asset.rateOfChange}" tabindex="14" ngmodel="model.asset.rateOfChange"/>
                                        </td>
                                        <td colspan="2">
                                        <td colspan="2">
                                            <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
                                        <td class="${standardFieldSpecs.validation.imp ?: ''}" data-for="validation">
                                            <kendo-dropdownlist
                                                    class="select"
                                                    name="modelAssetValidation"
                                                    [data]="${asset.constraints.validation.inList as JSON}"
                                                    [(ngModel)]="model.asset.validation">
                                            </kendo-dropdownlist>
                                        </td>
                                    </tr>
                                    %{--<tbody class="customTemplate">--}%
                                    <g:render template="/angular/common/customEdit" model="[assetEntityInstance: filesInstance]"></g:render>
                                    %{--</tbody>--}%
                                </table>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <!-- Dependencies -->
                    <tr id="deps">
                        <td valign="top" colspan="2">
                            <tds-supports-depends [(model)]="model"  (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
    <div class="modal-footer form-group-center">
        <button class="btn btn-primary pull-left" type="button" (click)="onUpdate()" [disabled]="!isDependenciesValidForm"><span class="fa fa-fw fa-floppy-o"></span> Update</button>
        <tds:hasPermission permission="${Permission.AssetDelete}">
            <button class="btn btn-danger pull-left mar-left-50" (click)="onDelete()" type="button"><span class="glyphicon glyphicon-trash"></span> Delete</button>
        </tds:hasPermission>
        <button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
    </div>
</div>
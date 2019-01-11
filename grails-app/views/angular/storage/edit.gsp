<%@ page import="com.tds.asset.Files" %>
<%@ page import="net.transitionmanager.security.Permission" %>
<%@ page import="grails.converters.JSON"%>
<%@ page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%-- <g:set var="assetClass" value="${(new Files()).assetClass}" /> --%>
<g:set var="assetClass" value="Files"/>

<div tds-autocenter tds-handle-escape (escPressed)="onCancelEdit()"
     class="modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close component-action-close" type="button" (click)="onCancelEdit()"><span
                aria-hidden="true">×</span></button>
        <h4 class="modal-title">Storage Edit</h4>
    </div>
    <div class="modal-body">
        <div>
            <form name="form" (ngSubmit)="form.form.valid && onUpdate()"
                  class="asset-entry-form"
                  [ngClass]="{'form-submitted': form && form.submitted}"
                  role="form" #form="ngForm" novalidate>
                <table>
                    <tr>
                        <td colspan="2" class="dialog-container">
                            <div class="dialog">
                                <table class="asset-edit-view">
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
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${asset.LUN}" ngmodel="model.asset.LUN" tabindex="16"/>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"  ngmodel="model.asset.supportType" tabindex="19"/>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle}"/>
                                        <td class="${standardFieldSpecs.moveBundle.imp ?: ''}" data-for="moveBundle">
                                            <kendo-dropdownlist
                                                    [tabIndex]="21"
                                                    class="tm-input-control"
                                                    name="modelAssetMoveBundle"
                                                    [data]="model.moveBundleList"
                                                    [(ngModel)]="model.asset.moveBundle"
                                                    [textField]="'name'"
                                                    [valueField]="'id'">
                                            </kendo-dropdownlist>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="label ${standardFieldSpecs.size.imp ?: ''}"
                                            [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${asset}" fieldName="size" /> }">
                                            <label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip ?: standardFieldSpecs.size.label}">
                                                ${standardFieldSpecs.size.label}
                                            </label>
                                        </td>
                                        <td data-for="sizeScale" nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp ?: ''}">
                                            <tdsAngular:inputControl field="${standardFieldSpecs.size}" tabindex="14" value="${asset.size}" ngmodel="model.asset.size"/>&nbsp;
                                        </td>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" tabindex="17" ngmodel="model.asset.externalRefId"/>
                                        <tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="19" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
                                        <td class="${standardFieldSpecs.planStatus.imp ?: ''}" data-for="planStatus">
                                            <kendo-dropdownlist
                                                    [tabIndex]="22"
                                                    class="tm-input-control"
                                                    name="modelAssetPlanStatus"
                                                    [data]="model.planStatusOptions"
                                                    [(ngModel)]="model.asset.planStatus">
                                            </kendo-dropdownlist>
                                        </td>
                                    </tr>
                                    <tr>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.scale}" value="${asset?.scale}"/>
                                        <td data-for="sizeScale" class="${standardFieldSpecs.size.imp ?: ''}">
                                            <kendo-dropdownlist
												    [tabIndex]="15"
                                                    class="tm-input-control"
                                                    name="modelAssetScaleName"
                                                    [data]="${SizeScale.getAsJsonList() as JSON}"
                                                    [(ngModel)]="model.asset.scale"
                                                    [defaultItem]="''"
                                                    [textField]="'text'"
                                                    [valueField]="'value'">
                                            </kendo-dropdownlist>
                                        </td>

                                        <tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
                                        <td>
                                            <tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" value="${asset.rateOfChange}" tabindex="18" ngmodel="model.asset.rateOfChange"/>
                                        </td>
                                        <td colspan="2"></td>
                                        <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
                                        <td class="${standardFieldSpecs.validation.imp ?: ''}" data-for="validation">
                                            <kendo-dropdownlist
                                                    [tabIndex]="23"
                                                    class="tm-input-control"
                                                    name="modelAssetValidation"
                                                    [data]="${asset.constraints.validation.inList as JSON}"
                                                    [(ngModel)]="model.asset.validation">
                                            </kendo-dropdownlist>
                                        </td>
                                    </tr>
                                    <g:render template="/angular/common/customEdit" model="[assetEntityInstance: filesInstance]"></g:render>
                                    <g:render template="/angular/common/assetTagsEdit"></g:render>
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
                            <tds-supports-depends [(model)]="model" (initDone)="onInitDependenciesDone($event)" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
    <div class="modal-footer form-group-center">
        <tds-button
                [action]="ButtonActions.Save"
                tooltip="Update Asset"
                class="btn-primary pull-left component-action-update" tabindex="501"
                [disabled]="!isDependenciesValidForm"
                [permissions]="['${Permission.AssetEdit}']"
                (click)="submitForm($event)">
        </tds-button>

        <tds:hasPermission permission="${Permission.AssetDelete}">
            <tds-button
                    [action]="ButtonActions.Delete"
                    class="btn-danger component-action-delete" tabindex="502"
                    tooltip="Delete Asset"
                    [permissions]="['${Permission.AssetDelete}']"
                    (click)="onDeleteAsset()">
            </tds-button>
        </tds:hasPermission>

        <tds-button
                [action]="ButtonActions.Cancel"
                tooltip="Cancel Edit"
                class="pull-right component-action-cancel"
                tabindex="503"
                (click)="onCancelEdit()">
        </tds-button>
    </div>
</div>
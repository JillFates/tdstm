<%@ page import="net.transitionmanager.asset.Files" %>
<%@ page import="net.transitionmanager.security.Permission" %>
<%@ page import="grails.converters.JSON"%>
<%@ page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%-- <g:set var="assetClass" value="${(new Files()).assetClass}" /> --%>
<g:set var="assetClass" value="Files"/>

<div>
    <div>
        <div class="clr-row">
            <div class="clr-col-11">
                <tds-tab-scroller>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Details</button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Supports/Depends</button>
                    </tds-scroller-item>
                </tds-tab-scroller>
            </div>
        </div>
    </div>
    <div class="asset-crud" [ngClass]="{'has-description': ${!!asset.description?.trim()}, 'no-description': ${!asset.description?.trim()}}" tdsScrollContainer style="position: relative">
        <form 
            name="form" 
            (ngSubmit)="form.form.valid && onUpdate()"
            class="asset-entry-form"
            [ngClass]="{'form-submitted': form && form.submitted}"
            role="form" 
            #form="ngForm" 
            novalidate>
            <div tdsScrollSection class="grid-form three-column">
                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
                    <tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${asset.assetName}" ngmodel="model.asset.assetName" />
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
                    <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="2" value="${asset.description}" ngmodel="model.asset.description" />
                </div>

                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${asset.fileFormat}" ngmodel="model.asset.fileFormat" tabindex="3"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${asset.LUN}" ngmodel="model.asset.LUN" tabindex="4"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"  ngmodel="model.asset.supportType" tabindex="5"/>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle}"/>
                    <kendo-dropdownlist
                            [tabIndex]="6"
                            class="tm-input-control"
                            name="modelAssetMoveBundle"
                            [data]="model.moveBundleList"
                            [(ngModel)]="model.asset.moveBundle"
                            [textField]="'name'"
                            [valueField]="'id'">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <label class="${standardFieldSpecs.size.imp?:''}">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</label>
                    <tdsAngular:inputControl field="${standardFieldSpecs.size}" tabindex="7" value="${asset.size}" ngmodel="model.asset.size"/>
                    <kendo-dropdownlist
                        [tabIndex]="8"
                        class="tm-input-control"
                        name="modelAssetScaleName"
                        [data]="${SizeScale.getAsJsonList() as JSON}"
                        [(ngModel)]="model.asset.scale"
                        [defaultItem]="''"
                        [textField]="'text'"
                        [valueField]="'value'">
                    </kendo-dropdownlist>
                </div>
                
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" tabindex="9" ngmodel="model.asset.externalRefId"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="10" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
                    <kendo-dropdownlist
                        [tabIndex]="11"
                        class="tm-input-control"
                        name="modelAssetPlanStatus"
                        [data]="model.planStatusOptions"
                        [(ngModel)]="model.asset.planStatus">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}"/>
                    <tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}" tabindex="12" ngmodel="model.asset.rateOfChange"/>
                </div>    

                <div class="clr-form-control">         
                    <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
                    <kendo-dropdownlist
                            [tabIndex]="13"
                            class="tm-input-control"
                            name="modelAssetValidation"
                            [data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
                            [(ngModel)]="model.asset.validation">
                    </kendo-dropdownlist>
                </div>

                <g:render template="/angular/common/customEdit" model="[assetEntityInstance: filesInstance]"></g:render>
            </div>

            <g:render template="/angular/common/assetTagsEdit"></g:render>

            <tds-supports-depends tdsScrollSection [(model)]="model" (initDone)="onInitDependenciesDone($event)" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
        </form>
    </div>
</div>

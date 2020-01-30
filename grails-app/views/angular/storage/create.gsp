<%@ page import="net.transitionmanager.asset.Files" %>
<%@ page import="net.transitionmanager.security.Permission" %>
<%@ page import="grails.converters.JSON"%>
<%@ page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%-- <g:set var="assetClass" value="${(new Files()).assetClass}" /> --%>
<g:set var="assetClass" value="Files"/>

<div tds-autocenter tds-handle-escape (escPressed)="onCloseEdit()"
     class="tds-modal-content has-side-nav tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close component-action-close" type="button" (click)="onCloseEdit()"><span
                aria-hidden="true">×</span></button>
        <h4 class="modal-title">Storage Create</h4>
        <tds-tab-scroller>
			<tds-scroller-item>
				<button tdsScrollerLink>Details</button>
			</tds-scroller-item>
			<tds-scroller-item>
				<button tdsScrollerLink>Supports/Depends</button>
			</tds-scroller-item>
		</tds-tab-scroller>
    </div>
    <div class="modal-body asset-crud no-description" tdsScrollContainer style="position: relative">
        <form 
            name="form" 
            (ngSubmit)="form.form.valid && onCreate()"
            class="asset-entry-form"
            [ngClass]="{'form-submitted': form && form.submitted}"
            role="form" 
            #form="ngForm" 
            novalidate>
            <div tdsScrollSection class="grid-form three-column">
                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetInstance.assetName}"/>
                    <tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${assetInstance.assetName}" ngmodel="model.asset.assetName" />
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetInstance.description}"/>
                    <tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="2" value="${assetInstance.description}" ngmodel="model.asset.description" />
                </div>
                
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${assetInstance.fileFormat}" ngmodel="model.asset.fileFormat" tabindex="3"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${assetInstance.LUN}" ngmodel="model.asset.LUN" tabindex="4"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetInstance.supportType}"  ngmodel="model.asset.supportType" tabindex="5"/>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetInstance.moveBundle?.id}"/>
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
                    <label class="${standardFieldSpecs.size.imp?:''}">
                        ${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
                    </label>
                    <tdsAngular:inputControl field="${standardFieldSpecs.size}" tabindex="7" value="${assetInstance.size}" ngmodel="model.asset.size"/>&nbsp;
                    <kendo-dropdownlist
                        [tabIndex]="11"
                        class="tm-input-control"
                        name="modelAssetScaleName"
                        [data]="${SizeScale.getAsJsonList() as JSON}"
                        [(ngModel)]="model.asset.scale"
                        [textField]="'text'"
                        [valueField]="'value'">
                    </kendo-dropdownlist>
                </div>

                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetInstance.externalRefId}" tabindex="8" ngmodel="model.asset.externalRefId"/>
                <tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${assetInstance.environment}" tabindex="9" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
                
                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetInstance.planStatus}"/>
                    <kendo-dropdownlist
                        [tabIndex]="10"
                        class="tm-input-control"
                        name="modelAssetPlanStatus"
                        [data]="model.planStatusOptions"
                        [(ngModel)]="model.asset.planStatus">
                    </kendo-dropdownlist>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${assetInstance.rateOfChange}"  />
                    <tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" value="${assetInstance.rateOfChange}" tabindex="12" ngmodel="model.asset.rateOfChange"/>
                </div>

                <div class="clr-form-control">
                    <tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}[0]"/>
                    <kendo-dropdownlist
                        [tabIndex]="13"
                        class="tm-input-control"
                        name="modelAssetValidation"
                        [data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetInstance.class).validation.inList as JSON}"
                        [(ngModel)]="model.asset.validation">
                    </kendo-dropdownlist>
                </div>
                <g:render template="/angular/common/customEdit" model="[assetEntityInstance: assetInstance]"></g:render>                                
            </div>

            <g:render template="/angular/common/assetTagsEdit"></g:render>
        
            <tds-supports-depends tdsScrollSection [(model)]="model" (initDone)="onInitDependenciesDone($event)" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>  
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

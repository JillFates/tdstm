<%@page import="net.transitionmanager.asset.Database"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="Database" />

<div>
	<div>
		<div class="clr-row tab-scroll-container" [ngClass]="{'has-description': ${!!asset.description?.trim()}}">
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
	<div class="asset-crud" [ngClass]="{'has-description': ${!!asset.description?.trim()}}" tdsScrollContainer style="position: relative">
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
					<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="1" value="${asset.assetName}" ngmodel="model.asset.assetName"/>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
					<tdsAngular:inputControl field="${standardFieldSpecs.description}" tabindex="2" value="${asset.description}" ngmodel="model.asset.description" />
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}" tabindex="3" ngmodel="model.asset.dbFormat"/>
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset?.supportType}" ngmodel="model.asset.supportType" tabindex="4" />
				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}" tabindex="5" blankOptionListText="Please Select..." ngmodel="model.asset.environment" />
				
				<div class="clr-form-control">
					<label class="${standardFieldSpecs.size.imp?:''}">
						${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
					</label>
					<tdsAngular:inputControl field="${standardFieldSpecs.size}" tabindex="6" value="${asset.size}" ngmodel="model.asset.size"/>
					<kendo-dropdownlist
						[tabIndex]="7"
						class="tm-input-control"
						name="modelAssetScaleName"
						[data]="${SizeScale.getAsJsonList() as JSON}"
						[(ngModel)]="model.asset.scale"
						[textField]="'text'"
						[valueField]="'value'">
					</kendo-dropdownlist>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>	
					<tds-date-control
						[(ngModel)]="model.asset.retireDate"
						class="tm-input-control"
						name="modelAssetRetireDate"
						[tabindex]="8"
						[value]="model.asset.retireDate">
					</tds-date-control>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
					<kendo-dropdownlist
							[tabIndex]="9"
							class="tm-input-control"
							name="modelAssetMoveBundle"
							[data]="model.moveBundleList"
							[(ngModel)]="model.asset.moveBundle"
							[textField]="'name'"
							[valueField]="'id'">
					</kendo-dropdownlist>
				</div>

				<%-- Can combine? --%>
				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset?.rateOfChange}"/>
					<tdsAngular:inputControl field="${standardFieldSpecs.rateOfChange}" tabindex="10" value="${asset.rateOfChange}" ngmodel="model.asset.rateOfChange"/>
				</div>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
					<tds-date-control
						[(ngModel)]="model.asset.maintExpDate"
						class="tm-input-control"
						name="modelAssetMainExpDate"
						[tabindex]="11"
						[value]="model.asset.maintExpDate">
					</tds-date-control>
				</div>
				
				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
					<kendo-dropdownlist
							[tabIndex]="12"
							class="tm-input-control"
							name="modelAssetPlanStatus"
							[data]="model.planStatusOptions"
							[(ngModel)]="model.asset.planStatus">
					</kendo-dropdownlist>
				</div>

				<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="13"/>

				<div class="clr-form-control">
					<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
					<kendo-dropdownlist
							[tabIndex]="14"
							class="tm-input-control"
							name="modelAssetValidation"
							[data]="${com.tdssrc.grails.GormUtil.getConstrainedProperties(asset.class).validation.inList as JSON}"
							[(ngModel)]="model.asset.validation">
					</kendo-dropdownlist>
				</div>

				<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>                       
			</div>

			<g:render template="/angular/common/assetTagsEdit"></g:render>
			<tds-supports-depends tdsScrollSection (initDone)="onInitDependenciesDone($event)" [(model)]="model" (isValidForm)="onDependenciesValidationChange($event)"></tds-supports-depends>
		</form>
	</div>
</div>

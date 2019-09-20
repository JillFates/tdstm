import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { ActionType } from '../../../../shared/model/data-list-grid.model';
import { KEYSTROKE } from '../../../../shared/model/constants';
import { APIActionParameterColumnModel, APIActionParameterModel } from '../../model/api-action.model';
import { COLUMN_MIN_WIDTH } from '../../../dataScript/model/data-script.model';
import { NgForm } from '@angular/forms';

@Component({
	selector: 'api-action-view-edit-parameters',
	template: `
		<form name="apiActionParametersForm" role="form" data-toggle="validator" #apiActionParametersForm='ngForm'
					class="form-horizontal left-alignment">
			<div class="box-body no-padding ">
				<kendo-grid
					[data]="parameterList"
					[pageSize]="25"
					[height]="430">
					<ng-template kendoGridToolbarTemplate [position]="'top'" *ngIf="modalType !== actionTypes.VIEW">
						<tds-button-add class="float-right mar-right-15"
														[id]="'btnAddParameter'"
														[title]="'DATA_INGESTION.ADD_PARAMETER' | translate "
														(click)="onAddParameter()">
						</tds-button-add>
					</ng-template>
					<kendo-grid-column *ngFor="let column of apiActionParameterColumnModel.columns; let columnIndex = index"
														 field="{{column.property}}"
														 [locked]="column.locked"
														 format="{{column.format}}"
														 title="{{column.label}}"
														 [width]="!column.width ? COLUMN_MIN_WIDTH : column.width">
						<ng-template kendoGridHeaderTemplate
												 *ngIf="column.type === 'boolean' && (column.property === 'readonly' || column.property === 'required' || column.property === 'encoded')">
							<span title="Parameter is required" *ngIf="column.property === 'required'"><i
								class="fa fa-fw fa-asterisk api-boolean-icon"></i></span>
							<span title="Parameter value read-only" *ngIf="column.property === 'readonly'"><i
								class="fa fa-fw fa-eye api-boolean-icon"></i></span>
							<span title="Parameter value is already URL Encoded" *ngIf="column.property === 'encoded'"><i
								class="fa fa-fw fa-code api-boolean-icon"></i></span>
						</ng-template>
						<ng-template kendoGridCellTemplate *ngIf="column.type === 'action' && modalType !== actionTypes.VIEW"
												 let-dataItem>
							<div class="tds-action-button-set">
								<tds-button-delete *ngIf="dataItem.required === 0  || dataItem.required === false"
																	 (click)="onDeleteParameter($event, dataItem)">
								</tds-button-delete>
							</div>
						</ng-template>
						<ng-template kendoGridCellTemplate *ngIf="modalType !== actionTypes.VIEW" let-dataItem
												 let-rowIndex="rowIndex">
							<div
								*ngIf="column.type === 'boolean' && (column.property === 'readonly' || column.property === 'required' || column.property === 'encoded')">
								<input (keypress)="getOnInputKey($event)" type="checkbox" class="param-text-input-boolean"
											 name="{{column.property + columnIndex + rowIndex}}-param" (change)="onValuesChange()"
											 [(ngModel)]="dataItem[column.property]"/>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'paramName'">
								<!-- Name -->
								<input (keypress)="getOnInputKey($event)"
											 type="text" required class="param-text-input"
											 name="{{column.property + columnIndex + rowIndex}}-param"
											 (change)="onValuesChange()"
											 [(ngModel)]="dataItem.paramName"
											 [ngClass]="{'has-error': dataItem.paramName?.length <= 0}"/>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'context.value'">
								<!-- Context -->
								<kendo-dropdownlist
									name="{{column.property + columnIndex + rowIndex}}" class="form-control" style="width: 100%;"
									[data]="assetClassesForParameters"
									[textField]="'value'"
									[valueField]="'assetClass'"
									(valueChange)="onContextValueChange(dataItem)"
									[(ngModel)]="dataItem.context"
									[valuePrimitive]="true"
									required
									[ngClass]="{'has-error': dataItem.paramName?.length <= 0 && (!dataItem.context || dataItem.context.value === '')}">
								</kendo-dropdownlist>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'value'">
								<!-- Value -->
								<input (keypress)="getOnInputKey($event)"
											 *ngIf="dataItem.context === 'USER_DEF' || dataItem.context?.assetClass === 'USER_DEF'"
											 type="text" class="param-text-input"
											 name="{{column.property + columnIndex + rowIndex}}-user-defined"
											 (change)="onValuesChange()"
											 [(ngModel)]="dataItem.value"
											 [disabled]="!dataItem.context || dataItem.context.value === '' || dataItem.readonly === 1  || dataItem.readonly === true"/>
								<!-- Value -->
								<kendo-combobox *ngIf="dataItem.context !== 'USER_DEF' && dataItem.context?.assetClass !== 'USER_DEF'"
																name="{{column.property + columnIndex + rowIndex}}" style="width: 100%;"
																[data]="dataItem.currentFieldList"
																[filterable]="true"
																(filterChange)="filterChange($event, dataItem)"
																[textField]="'label'"
																[valueField]="'field'"
																[(ngModel)]="dataItem.fieldName"
																[valuePrimitive]="true"
																(keypress)="getOnInputKey($event)"
																(valueChange)="onValuesChange()"
																[disabled]="!dataItem.context || dataItem.context.value === '' || dataItem.readonly === 1  || dataItem.readonly === true"
																required
																[ngClass]="{'has-error': (dataItem.context && dataItem.context.value !== '' && !dataItem.fieldName)}">
								</kendo-combobox>
							</div>
							<!-- Description -->
							<div *ngIf="column.type === 'text' && column.property === 'desc'">
								<input (keypress)="getOnInputKey($event)" type="text" class="param-text-input"
											 name="{{column.property + columnIndex + rowIndex}}-desc" [(ngModel)]="dataItem.description"/>
							</div>
						</ng-template>
						<ng-template kendoGridCellTemplate *ngIf="modalType == actionTypes.VIEW" let-dataItem
												 let-rowIndex="rowIndex">
							<div class="group-boolean-fields"
									 *ngIf="column.type === 'boolean' && (column.property === 'readonly' || column.property === 'required' || column.property === 'encoded')">
								<span *ngIf="column.property === 'required'"><span
									*ngIf="dataItem.required === 1  || dataItem.required === true"><i
									class="fa fa-fw fa-check"></i></span></span>
								<span *ngIf="column.property === 'readonly'"><span
									*ngIf="dataItem.readonly === 1  || dataItem.readonly === true"><i
									class="fa fa-fw fa-check"></i></span></span>
								<span *ngIf="column.property === 'encoded'"><span
									*ngIf="dataItem.encoded === 1  || dataItem.encoded === true"><i class="fa fa-fw fa-check"></i></span></span>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'paramName'">
								<span>{{dataItem.paramName}}</span>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'context.value'">
								<span>{{getAssetClassValue(dataItem.context)}}</span>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'value'">
								<span
									*ngIf="dataItem.context === 'USER_DEF' || dataItem.context?.assetClass === 'USER_DEF'">{{dataItem.value}}</span>
								<span
									*ngIf="dataItem.context !== 'USER_DEF' && dataItem.context?.assetClass !== 'USER_DEF'">
									{{getFieldLabel(dataItem)}}
								</span>
							</div>
							<div *ngIf="column.type === 'text' && column.property === 'desc'">
								<span>{{dataItem.description}}</span>
							</div>
						</ng-template>
					</kendo-grid-column>
				</kendo-grid>
			</div>
		</form>
	`,
	styles: [`
      .has-error, .has-error:focus {
          border: 1px #f00 solid;
      }
	`]
})
export class ApiActionViewEditParamatersComponent {
	@Input('parameterList') parameterList: Array<any>;
	@Input('modalType') modalType: ActionType;
	@Input('commonFieldSpecs') commonFieldSpecs: any;
	@Output('onValuesChange') onValuesChangeEmitter: EventEmitter<{ parameterList: Array<any>, isFormValid: boolean }>
		= new EventEmitter<{ parameterList: Array<any>, isFormValid: boolean }>();
	@ViewChild('apiActionParametersForm') apiActionParametersForm: NgForm;
	actionTypes = ActionType;
	apiActionParameterColumnModel = new APIActionParameterColumnModel();
	COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	assetClassesForParameters = [
		{
			assetClass: 'COMMON',
			value: 'Asset'
		}, {
			assetClass: 'APPLICATION',
			value: 'Application'
		}, {
			assetClass: 'DATABASE',
			value: 'Database'
		}, {
			assetClass: 'DEVICE',
			value: 'Device'
		}, {
			assetClass: 'STORAGE',
			value: 'Storage'
		}, {
			assetClass: 'TASK',
			value: 'Task'
		}, {
			assetClass: 'USER_DEF',
			value: 'User Defined'
		}
	];
	isFormValid = true;

	/**
	 * Add a new argument to the list of parameters and refresh the list.
	 */
	onAddParameter(): void {
		this.parameterList.push({
			paramName: '',
			desc: '',
			type: 'string',
			context: '',
			fieldName: '',
			currentFieldList: [],
			value: '',
			readonly: false,
			required: false,
			encoded: false
		});
		this.onValuesChange();
	}

	/**
	 * Workaround to stop propagation on shared events on Kendo
	 * Clicking on enter was causing other events to execute
	 * @param event
	 */
	getOnInputKey(event: any): void {
		if (event.key === KEYSTROKE.ENTER) {
			event.preventDefault();
			event.stopPropagation();
		}
		this.onValuesChange();
	}

	/**
	 * Make the Field from Context, filterable
	 * @param filter
	 */
	filterChange(filter: any, dataItem: any): void {
		dataItem.currentFieldList = dataItem.sourceFieldList.filter((s) => s.label.toLowerCase().indexOf(filter.toLowerCase()) !== -1);
	}

	/**
	 * When the Context has change, we should load the list of params associate with the Asset Class,
	 * if the value is USER_DEF, the field will become a text input field
	 */
	onContextValueChange(dataItem: APIActionParameterModel): void {
		if (dataItem && dataItem.context) {
			let context = (dataItem.context['assetClass']) ? dataItem.context['assetClass'] : dataItem.context;
			let fieldSpecs = this.commonFieldSpecs.find((spec) => {
				return spec.domain === context;
			});
			if (fieldSpecs) {
				dataItem.currentFieldList = fieldSpecs.fields;
				dataItem.sourceFieldList = fieldSpecs.fields;
				let property = dataItem.currentFieldList.find((field) => {
					const fieldName = dataItem.fieldName.toLowerCase();
					return field.field.toLowerCase() === fieldName || field.label.toLowerCase() === fieldName;
				});
				if (property) {
					dataItem.fieldName = property;
				}
			}
			this.onValuesChange();
		}
	}

	/**
	 * Delete from the paramaters the argument passed.
	 * @param dataItem
	 */
	onDeleteParameter(event: any, dataItem: APIActionParameterModel): void {
		let parameterIndex = this.parameterList.indexOf(dataItem);
		if (parameterIndex >= 0) {
			this.parameterList.splice(parameterIndex, 1);
			setTimeout(() => this.onValuesChange(), 500);
		}
	}

	/**
	 * Get the Label value to show on the UI like Application instead of APPLICATION
	 * @param context
	 * @returns {string}
	 */
	getAssetClassValue(context: any): string {
		let assetClass = this.assetClassesForParameters.find((param) => {
			return param.assetClass === context || param.assetClass === context.assetClass;
		});
		if (assetClass && assetClass.value) {
			return assetClass.value;
		}
		return context;
	};

	/**
	 * Returns field label from current field list.
	 * @param dataItem
	 */
	getFieldLabel(dataItem: APIActionParameterModel): string {
		const field = dataItem.currentFieldList.find(item => item.field === dataItem.fieldName);
		return field ? field.label : dataItem.fieldName;
	}

	/**
	 * On any form value change emit the change event with values to parent component.
	 */
	onValuesChange(): void {
		this.isFormValid = this.apiActionParametersForm && this.apiActionParametersForm.valid;
		const invalidParams = this.parameterList.filter(item => {
			return item.context !== 'USER_DEF' && !item.fieldName;
		});
		this.isFormValid = this.isFormValid && invalidParams.length === 0;
		this.onValuesChangeEmitter.emit({parameterList: this.parameterList, isFormValid: this.isFormValid});
	}
}
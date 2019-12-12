import {
	Component,
	EventEmitter,
	Input,
	OnInit,
	Output,
	ViewChild,
} from '@angular/core';
import { ActionType } from '../../../../shared/model/data-list-grid.model';
import { KEYSTROKE } from '../../../../shared/model/constants';
import {
	APIActionParameterColumnModel,
	APIActionParameterModel,
} from '../../model/api-action.model';
import { COLUMN_MIN_WIDTH } from '../../../dataScript/model/data-script.model';
import { NgForm } from '@angular/forms';

@Component({
	selector: 'api-action-view-edit-parameters',
	templateUrl: 'api-action-view-edit-parameters.component.html',
	styles: [
		`
			.has-error,
			.has-error:focus {
				border: 1px #f00 solid;
			}
		`,
	],
})
export class ApiActionViewEditParamatersComponent {
	@Input('parameterList') parameterList: Array<any>;
	@Input('modalType') modalType: ActionType;
	@Input('commonFieldSpecs') commonFieldSpecs: any;
	@Output('onValuesChange') onValuesChangeEmitter: EventEmitter<{
		parameterList: Array<any>;
		isFormValid: boolean;
	}> = new EventEmitter<{
		parameterList: Array<any>;
		isFormValid: boolean;
	}>();
	@ViewChild('apiActionParametersForm', { static: false })
	apiActionParametersForm: NgForm;
	actionTypes = ActionType;
	apiActionParameterColumnModel = new APIActionParameterColumnModel();
	COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	assetClassesForParameters = [
		{
			assetClass: 'COMMON',
			value: 'Asset',
		},
		{
			assetClass: 'APPLICATION',
			value: 'Application',
		},
		{
			assetClass: 'DATABASE',
			value: 'Database',
		},
		{
			assetClass: 'DEVICE',
			value: 'Device',
		},
		{
			assetClass: 'STORAGE',
			value: 'Storage',
		},
		{
			assetClass: 'TASK',
			value: 'Task',
		},
		{
			assetClass: 'USER_DEF',
			value: 'User Defined',
		},
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
			encoded: false,
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
		dataItem.currentFieldList = dataItem.sourceFieldList.filter(
			s => s.label.toLowerCase().indexOf(filter.toLowerCase()) !== -1
		);
	}

	/**
	 * When the Context has change, we should load the list of params associate with the Asset Class,
	 * if the value is USER_DEF, the field will become a text input field
	 */
	onContextValueChange(dataItem: APIActionParameterModel): void {
		if (dataItem && dataItem.context) {
			let context = dataItem.context['assetClass']
				? dataItem.context['assetClass']
				: dataItem.context;
			let fieldSpecs = this.commonFieldSpecs.find(spec => {
				return spec.domain === context;
			});
			if (fieldSpecs) {
				dataItem.currentFieldList = fieldSpecs.fields;
				dataItem.sourceFieldList = fieldSpecs.fields;
				let property = dataItem.currentFieldList.find(field => {
					const fieldName = dataItem.fieldName.toLowerCase();
					return (
						field.field.toLowerCase() === fieldName ||
						field.label.toLowerCase() === fieldName
					);
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
		let assetClass = this.assetClassesForParameters.find(param => {
			return (
				param.assetClass === context ||
				param.assetClass === context.assetClass
			);
		});
		if (assetClass && assetClass.value) {
			return assetClass.value;
		}
		return context;
	}

	/**
	 * Returns field label from current field list.
	 * @param dataItem
	 */
	getFieldLabel(dataItem: APIActionParameterModel): string {
		const field = dataItem.currentFieldList.find(
			item => item.field === dataItem.fieldName
		);
		return field ? field.label : dataItem.fieldName;
	}

	/**
	 * On any form value change emit the change event with values to parent component.
	 */
	onValuesChange(): void {
		this.isFormValid =
			this.apiActionParametersForm && this.apiActionParametersForm.valid;
		const invalidParams = this.parameterList.filter(item => {
			return item.context !== 'USER_DEF' && !item.fieldName;
		});
		this.isFormValid = this.isFormValid && invalidParams.length === 0;
		this.onValuesChangeEmitter.emit({
			parameterList: this.parameterList,
			isFormValid: this.isFormValid,
		});
	}
}

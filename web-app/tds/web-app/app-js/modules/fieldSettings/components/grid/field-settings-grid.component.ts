import {Component, Input, Output, EventEmitter, OnInit, ViewEncapsulation, ViewChild, ElementRef} from '@angular/core';
import {CUSTOM_FIELD_CONTROL_TYPE, FieldSettingsModel} from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';

import { UILoaderService } from '../../../../shared/services/ui-loader.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';
import { process, State } from '@progress/kendo-data-query';

import { MinMaxConfigurationPopupComponent } from '../min-max/min-max-configuration-popup.component';
import { SelectListConfigurationPopupComponent } from '../select-list/selectlist-configuration-popup.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {FIELD_COLORS} from '../../model/field-settings.model';
import {NumberConfigurationPopupComponent} from '../number/number-configuration-popup.component';
import {NumberControlHelper} from '../../../../shared/components/custom-control/number/number-control.helper';
import {NumberConfigurationConstraintsModel} from '../number/number-configuration-constraints.model';
import {AlertType} from '../../../../shared/model/alert.model';
import { FieldSettingsService } from '../../service/field-settings.service';

declare var jQuery: any;

@Component({
	selector: 'field-settings-grid',
	encapsulation: ViewEncapsulation.None,
	exportAs: 'fieldSettingsGrid',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/grid/field-settings-grid.component.html',
	styles: [`
		.k-grid { height:calc(100vh - 225px); }
		tr .text-center { text-align: center; }
		.has-error,.has-error:focus { border: 1px #f00 solid;}
	`]
})
export class FieldSettingsGridComponent implements OnInit {
	@Output('save') saveEmitter = new EventEmitter<any>();
	@Output('cancel') cancelEmitter = new EventEmitter<any>();
	@Output('add') addEmitter = new EventEmitter<any>();
	@Output('share') shareEmitter = new EventEmitter<any>();
	@Output('delete') deleteEmitter = new EventEmitter<any>();
	@Output('filter') filterEmitter = new EventEmitter<any>();

	@Input('data') data: DomainModel;
	@Input('state') gridState: any;
	@ViewChild('minMax') minMax: MinMaxConfigurationPopupComponent;
	@ViewChild('selectList') selectList: SelectListConfigurationPopupComponent;
	public domains: DomainModel[] = [];
	private fieldsSettings: FieldSettingsModel[];
	private gridData: GridDataResult;
	public colors = FIELD_COLORS;
	protected hasAtLeastOneInvalidField = false;
	protected formHasError: boolean = null;
	protected isDirty = false;
	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'order'
		}],
		filter: {
			filters: [{
				field: 'field',
				operator: 'contains',
				value: ''
			}],
			logic: 'or'
		}
	};

	private isEditing = false;
	private isFilterDisabled = false;
	private sortable: boolean | object = { mode: 'single' };
	private fieldsToDelete = [];
	protected resettingChanges = false;
	protected lastEditedControl = null;

	private readonly availableControls = [
		{ text: CUSTOM_FIELD_CONTROL_TYPE.List, value: CUSTOM_FIELD_CONTROL_TYPE.List},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.String, value: CUSTOM_FIELD_CONTROL_TYPE.String},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.YesNo, value: CUSTOM_FIELD_CONTROL_TYPE.YesNo},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.Date, value: CUSTOM_FIELD_CONTROL_TYPE.Date},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.DateTime, value: CUSTOM_FIELD_CONTROL_TYPE.DateTime},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.Number, value: CUSTOM_FIELD_CONTROL_TYPE.Number}
	];
	private availableFieldTypes = ['All', 'Custom Fields', 'Standard Fields'];

	constructor(
		private loaderService: UILoaderService,
		private prompt: UIPromptService,
		private dialogService: UIDialogService,
		private fieldSettingsService: FieldSettingsService) {
	}

	ngOnInit(): void {
		this.fieldsSettings = this.data.fields;
		this.refresh();
		this.fieldSettingsService.getFieldSettingsByDomain().subscribe(domains => {
			this.domains = domains;
		});
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.refresh();
	}

	protected onFilter(): void {
		this.filterEmitter.emit(null);
	}

	public applyFilter(): void {
		this.state.filter.filters = [];

		this.fieldsSettings = this.data.fields;
		if (this.gridState.filter.search !== '') {
			let search = new RegExp(this.gridState.filter.search, 'i');
			this.fieldsSettings = this.data.fields.filter(
				item => search.test(item.field) ||
					search.test(item.label) ||
					item['isNew']);
		}
		if (this.gridState.filter.fieldType !== 'All') {
			this.state.filter.filters.push({
				field: 'udf',
				operator: 'eq',
				value: this.gridState.filter.fieldType === 'Custom Fields' ? 1 : 0
			});
			this.state.filter.filters.push({
				field: 'isNew',
				operator: 'eq',
				value: true
			});
		}
		this.refresh();
	}

	protected onEdit(): void {
		this.loaderService.show();
		setTimeout(() => {
			this.isEditing = true;
			this.resetValidationFlags();
			this.sortable = { mode: 'single' };
			this.isFilterDisabled = false;
			this.onFilter();
			this.loaderService.hide();
		});
	}

	protected onSaveAll(): void {
		this.saveEmitter.emit(() => {
			this.reset();
		});

	}

	/**
	 * On click Cancel button send the message to the host container to cancel the changes
	 * @param {any} event containing the current event control which has the latest error
	 */
	protected onCancel(event: any): void {
		this.resettingChanges = true;
		event = event || this.lastEditedControl || null;

		this.cancelEmitter.emit({
			success: () => {
				this.reset();
				this.refresh();
			},
			failure: () => {
				if (event) {
					this.resettingChanges = false;
					event.target.focus();
				}
			}});
	}

	/**
	 * Delete button action, adds field to the pending to delete queue.
	 * @param {FieldSettingsModel} dataItem
	 */
	protected onDelete(dataItem: FieldSettingsModel): void {
		const targetField = this.data.fields.find((item) => item.field === dataItem.field);
		if (targetField) {
			targetField.errorMessage = '';
		}

		dataItem.toBeDeleted = true;
		this.setIsDirty(true);
		this.fieldsToDelete.push(dataItem.field);
		this.deleteEmitter.emit({
			domain: this.data.domain,
			fieldsToDelete: this.fieldsToDelete
		});
	}

	/**
	 * Undo Delete button action, removes field from pending to delete queue.
	 * @param {FieldSettingsModel} dataItem
	 */
	protected undoDelete(dataItem: FieldSettingsModel): void {
		dataItem.toBeDeleted = false;
		let index = this.fieldsToDelete.indexOf(dataItem.field, 0);
		this.fieldsToDelete.splice(index, 1);
		this.deleteEmitter.emit({
			domain: this.data.domain,
			fieldsToDelete: this.fieldsToDelete
		});
	}

	/**
	 * Check if a given field is on the pending to deleted queue.
	 * @param {FieldSettingsModel} dataItem
	 * @returns {boolean}
	 */
	protected toBeDeleted(dataItem: FieldSettingsModel): boolean {
		return this.fieldsToDelete.some(item => item === dataItem.field);
	}

	protected onAddCustom(): void {
		this.setIsDirty(true);

		this.addEmitter.emit((custom) => {
			this.state.sort = [
				{
					dir: 'desc',
					field: 'isNew'
				}, {
					dir: 'desc',
					field: 'count'
				}
			];
			let model = new FieldSettingsModel();
			model.field = custom;
			model.constraints = {
				required: false
			};
			model.label = '';
			model['isNew'] = true;
			model['count'] = this.data.fields.length;
			model.control = CUSTOM_FIELD_CONTROL_TYPE.String;
			model.show = true;
			let availableOrder = this.data.fields.map(f => f.order).sort((a, b) => a - b).filter(item => !isNaN(item));
			model.order = availableOrder[availableOrder.length - 1] + 1;
			this.data.fields.push(model);
			this.onFilter();

			setTimeout(function () {
				jQuery('#' + model.field).focus();
			});
		});
	}

	protected onShare(field: FieldSettingsModel) {
		this.shareEmitter.emit({
			field: field,
			domain: this.data.domain
		});
	}

	protected onRequired(field: FieldSettingsModel) {
		if (field.constraints.values &&
			(field.control === CUSTOM_FIELD_CONTROL_TYPE.List || field.control === CUSTOM_FIELD_CONTROL_TYPE.YesNo)) {
			if (field.constraints.required) {
				field.constraints.values.splice(field.constraints.values.indexOf(''), 1);
			} else if (field.constraints.values.indexOf('') === -1) {
				field.constraints.values.splice(0, 0, '');
			}
			if (field.constraints.values.indexOf(field.default) === -1) {
				field.default = null;
			}
		}
	}

	protected onClearTextFilter(): void {
		this.gridState.filter.search = '';
		this.onFilter();
	}

	protected reset(): void {
		this.isEditing = false;
		this.sortable = { mode: 'single' };
		this.isFilterDisabled = false;
		this.state.sort = [{
			dir: 'asc',
			field: 'order'
		}];
		this.resetValidationFlags();
		this.applyFilter();
	}

	/**
	 * Refresh the changes in the data grid, in addition reset the values of the resettingChanges
	 * and lastEditeControls variables
	 */
	public refresh(): void {
		this.gridData = process(this.fieldsSettings, this.state);
		this.resettingChanges = false;
		this.lastEditedControl = null;
	}

	/**
	 * TODO dontiveros: I need to remove this specific type of custom field code, create a helper for each field type and put code in there.
	 * TODO dontiveros: Just like has been done on the Number Field Type.
	 * @param dataItem
	 */
	protected onControlChange(previousControl: CUSTOM_FIELD_CONTROL_TYPE, dataItem: FieldSettingsModel): void {
		switch (dataItem.control) {
			case CUSTOM_FIELD_CONTROL_TYPE.List:
				NumberControlHelper.cleanNumberConstraints(dataItem.constraints as any);
				// Removes String constraints
				delete dataItem.constraints.maxSize
				delete dataItem.constraints.minSize
				if (dataItem.constraints.values &&
					dataItem.constraints.values.indexOf('Yes') !== -1 &&
					dataItem.constraints.values.indexOf('No') !== -1) {
					dataItem.constraints.values = [];
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.String:
				NumberControlHelper.cleanNumberConstraints(dataItem.constraints as any);
				// Remove List & YesNo constraints
				delete dataItem.constraints.values;
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.YesNo:
				NumberControlHelper.cleanNumberConstraints(dataItem.constraints as any);
				dataItem.constraints.values = ['Yes', 'No'];
				if (dataItem.constraints.values.indexOf(dataItem.default) === -1) {
					dataItem.default = null;
				}
				if (!dataItem.constraints.required) {
					dataItem.constraints.values.splice(0, 0, '');
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.Number:
				// Remove List & YesNo constraints
				delete dataItem.constraints.values;
				// Removes String constraints
				delete dataItem.constraints.maxSize
				delete dataItem.constraints.minSize
				NumberControlHelper.initConfiguration(dataItem.constraints as NumberConfigurationConstraintsModel);
				break;
			default:
				NumberControlHelper.cleanNumberConstraints(dataItem.constraints as any);
				// Remove List & YesNo constraints
				delete dataItem.constraints.values;
				// Removes String constraints
				delete dataItem.constraints.maxSize
				delete dataItem.constraints.minSize
				break;
		}
	}

	protected onControlModelChange(newValue: CUSTOM_FIELD_CONTROL_TYPE, dataItem: FieldSettingsModel) {
		this.setIsDirty(true);
		const previousControl = dataItem.control;
		if (dataItem.control === CUSTOM_FIELD_CONTROL_TYPE.List) {
			this.prompt.open(
				'Confirmation Required',
				'Changing the control will lose all List options. Click Ok to continue otherwise Cancel',
				'Ok', 'Cancel').then(result => {
					if (result) {
						dataItem.control = newValue;
						this.onControlChange(previousControl, dataItem);
					} else {
						setTimeout(() => {
							jQuery('#control' + dataItem.field).val('List');
						});
					}
				});
		} else {
			dataItem.control = newValue;
			this.onControlChange(previousControl, dataItem);
		}
	}

	/**
	 * Checks if the given label is an invalid String, and returns true or false respectively.
	 * The error conditions are:
	 * 	- The label is an empty String
	 * 	- The label String matches other label in the fields list.
	 * 	- The label String matches a field name in the fields list.
	 * 	- The label String matches other label or field name in the fields list of another domain.
	 * 	  This comparison is case-insensitive and it doesn't take into account any trailing, leading or in-between spaces.
	 * 	  e.g. label: "Last Modified or last modified or LastModified". other label: "Last Modified". This comparisons will error.
	 *
	 * 	- The label String matches any field names in the list of fields.
	 * 	  This comparison is case-insensitive and it doesn't take into account any trailing, leading or in-between spaces.
	 * 	  e.g. label: "Asset Name or asset N ame or AssetName or assetName". some field name: "assetName". This comparisons will error.
	 *
	 * @See TM-13505
	 * @param label
	 * @returns {boolean}
	 */
	protected hasError(dataItem: FieldSettingsModel) {
		const fields = this.getFieldsExcludingDeleted();

		return dataItem.label.trim() === '' ||
			fields.some((field) => field.errorMessage) ||
			this.fieldSettingsService.conflictsWithAnotherLabel(dataItem.label, fields) ||
			this.fieldSettingsService.conflictsWithAnotherFieldName(dataItem, fields) ||
			this.fieldSettingsService.conflictsWithAnotherDomain(dataItem, this.domains, this.domains[0]);
	}

	/**
	 * Returns a boolean indicating if the fields contain atleast one field with error
	 */
	private atLeastOneInvalidField(): boolean {
		const fields = this.getFieldsExcludingDeleted() || [];

		return fields.some((field) => field.errorMessage || !field.label);
	}

	/**
	 * On blur input field controls, it applies the validation rules to the label and name of the field control
	 * @param {FieldSettingsModel} dataItem Contains the model of the asset field control which launched the event
	 * @param {any} event Context event from the input that launched the change
	 */
	protected onBlur(dataItem: FieldSettingsModel, event: any) {
		console.log('@on blur');
		dataItem.errorMessage = '';
		const fields = this.getFieldsExcludingDeleted();
		const message = 'The label must be different from all other field names and labels';

		if (this.fieldSettingsService.conflictsWithAnotherLabel(dataItem.label, fields)) {
			dataItem.errorMessage = message;
		} else {
			if (this.fieldSettingsService.conflictsWithAnotherFieldName(dataItem, fields)) {
				dataItem.errorMessage = message;
			} else {
				if (this.fieldSettingsService.conflictsWithAnotherDomain(dataItem, this.domains, this.domains[0])) {
					dataItem.errorMessage = message;
				}
			}
		}

		this.formHasError =  Boolean(dataItem.errorMessage || this.atLeastOneInvalidField());

		if (dataItem.errorMessage)  {
			if (!this.resettingChanges) {
				this.lastEditedControl = this.lastEditedControl || event;
				setTimeout(() => this.lastEditedControl.target.focus(), 0.1);
			}
		} else {
			if (this.lastEditedControl) {
				if (this.lastEditedControl.target.id === event.target.id)  {
					this.lastEditedControl = null;
				}
			}
		}
	}

	/**
	 * Get the list of fields excluding fields to be deleted
	 * @returns {any[]}
	 */
	protected getFieldsExcludingDeleted(): any {
		return this.data.fields
			.filter((item) => !((this.fieldsToDelete || []).includes(item.field)));
	}

	/**
	 * Function to determine if given field is currently used as Project Plan Methodology.
	 * Note: Only APPLICATION asset types has the plan methodology feature.
	 * @param {FieldSettingsModel} field
	 * @returns {boolean} True or False
	 */
	protected isFieldUsedAsPlanMethodology(field: FieldSettingsModel): boolean {
		return this.data.planMethodology && this.data.planMethodology === field.field;
	}

	/**
	 * Open The Dialog to Edit Custom Field Setting
	 * @param {ProviderModel} providerModel
	 * @param {number} actionType
	 */
	private openFieldSettingsPopup(dataItem: FieldSettingsModel): void {
		if (dataItem.control === CUSTOM_FIELD_CONTROL_TYPE.String) {
			this.dialogService.open(MinMaxConfigurationPopupComponent, [
				{ provide: FieldSettingsModel, useValue: dataItem },
				{ provide: 'domain', useValue: this.data.domain }
			]).then(result => {
				// when popup closes ..
			}).catch(result => {
				console.log('Dismissed MinMaxConfigurationPopupComponent Dialog');
			});
		} else if (dataItem.control === CUSTOM_FIELD_CONTROL_TYPE.List) {
			this.dialogService.open(SelectListConfigurationPopupComponent, [
				{ provide: FieldSettingsModel, useValue: dataItem },
				{ provide: 'domain', useValue: this.data.domain }
			]).then(result => {
				// when popup closes ..
			}).catch(result => {
				console.log('Dismissed SelectListConfigurationPopupComponent Dialog');
			});
		} else {
			this.dialogService.open(NumberConfigurationPopupComponent, [
				{ provide: FieldSettingsModel, useValue: dataItem },
				{ provide: 'domain', useValue: this.data.domain }
			]).then(result => {
				// when popup closes ..
			}).catch(result => {
				// when popup is Cancelled.
			});
		}
	}

	/**
	 * Check if selected field control has configuration available.
	 * @param {CUSTOM_FIELD_CONTROL_TYPE} control
	 * @returns {boolean}
	 */
	protected isAllowedConfigurationForField(control: CUSTOM_FIELD_CONTROL_TYPE): boolean {
		if (control === CUSTOM_FIELD_CONTROL_TYPE.List
			|| control === CUSTOM_FIELD_CONTROL_TYPE.String
			|| control === CUSTOM_FIELD_CONTROL_TYPE.Number) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the field is allowed to have a default value.
	 * @param {CUSTOM_FIELD_CONTROL_TYPE} control
	 * @returns {boolean}
	 */
	protected isAllowedDefaultValueForField(control: CUSTOM_FIELD_CONTROL_TYPE): boolean {
		if (control && (control === CUSTOM_FIELD_CONTROL_TYPE.String
		||	control === CUSTOM_FIELD_CONTROL_TYPE.YesNo
		||	control === CUSTOM_FIELD_CONTROL_TYPE.List)) {
			return true;
		}
		return false;
	}

	/**
	 * On esc key pressed open confirmation dialog
	 */
	protected onKeyPressed(event: KeyboardEvent): void {
		console.log('@on key pressed');
		this.setIsDirty(true);
		if (event.code === 'Escape') {
			this.onCancel(event);
		}
	}

	/**
	 * Set the flag to indicate the form is dirty
	 */
	protected setIsDirty(value: boolean): void {
		console.log('Setting is dirty:', value);
		this.isDirty = value;
	}

	/**
	 * Reset the flags to keep track the validation state
	 */
	private resetValidationFlags(): void {
		this.formHasError = null;
		this.setIsDirty(false);
	}

}

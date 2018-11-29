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
	private fieldsSettings: FieldSettingsModel[];
	private gridData: GridDataResult;
	public colors = FIELD_COLORS;
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
		private dialogService: UIDialogService) {
	}

	ngOnInit(): void {
		this.fieldsSettings = this.data.fields;
		this.refresh();
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

	protected onCancel(): void {
		this.cancelEmitter.emit(() => {
			this.reset();
			this.refresh();
		});
	}

	/**
	 * Delete button action, adds field to the pending to delete queue.
	 * @param {FieldSettingsModel} dataItem
	 */
	protected onDelete(dataItem: FieldSettingsModel): void {
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
		let found = this.fieldsToDelete.filter(item => item === dataItem.field);
		return found.length > 0;
	}

	protected onAddCustom(): void {
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
			let availableOrder = this.fieldsSettings.map(f => f.order).sort((a, b) => a - b);
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
		this.applyFilter();
	}

	public refresh(): void {
		this.gridData = process(this.fieldsSettings, this.state);
	}

	/**
	 * TODO dontiveros: I need to remove this specific type of custom field code, create a helper for each field type and put code in there.
	 * TODO dontiveros: Just like has been done on the Number Field Type.
	 * @param dataItem
	 */
	protected onControlChange(dataItem: FieldSettingsModel): void {
		switch (dataItem.control) {
			case CUSTOM_FIELD_CONTROL_TYPE.List:

				if (dataItem.constraints.values &&
					dataItem.constraints.values.indexOf('Yes') !== -1 &&
					dataItem.constraints.values.indexOf('No') !== -1) {
					dataItem.constraints.values = [];
				}
				if (!dataItem.constraints.values ||
					dataItem.constraints.values.length === 0) {
					// selectList.onToggle();
				} else {
					this.selectList.show = false;
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.String:
				dataItem.constraints.values = [];
				if (!dataItem.constraints.minSize ||
					!dataItem.constraints.maxSize) {
					// minMax.onToggle();
				} else {
					this.minMax.show = false;
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.YesNo:
				dataItem.constraints.values = ['Yes', 'No'];
				if (dataItem.constraints.values.indexOf(dataItem.default) === -1) {
					dataItem.default = null;
				}
				if (!dataItem.constraints.required) {
					dataItem.constraints.values.splice(0, 0, '');
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.Number:
				NumberControlHelper.initConfiguration(dataItem.constraints as NumberConfigurationConstraintsModel);
				break;
			default:
				break;
		}
	}

	protected onControlModelChange(newValue: CUSTOM_FIELD_CONTROL_TYPE, dataItem: FieldSettingsModel) {
		if (dataItem.control === CUSTOM_FIELD_CONTROL_TYPE.List) {
			this.prompt.open(
				'Confirmation Required',
				'Changing the control will lose all List options. Click Ok to continue otherwise Cancel',
				'Ok', 'Cancel').then(result => {
					if (result) {
						dataItem.control = newValue;
						this.onControlChange(dataItem);
					} else {
						setTimeout(() => {
							jQuery('#control' + dataItem.field).val('List');
						});
					}
				});
		} else {
			dataItem.control = newValue;
			this.onControlChange(dataItem);
		}
	}

	protected hasError(label: string) {
		return label.trim() === '' || this.data.fields.filter(item => item.label === label.trim()).length > 1;
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

}

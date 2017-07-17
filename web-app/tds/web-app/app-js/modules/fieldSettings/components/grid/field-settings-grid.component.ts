import { Component, Input, Output, EventEmitter, OnInit, ViewEncapsulation } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';

import { UILoaderService } from '../../../../shared/services/ui-loader.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';
import { process, State } from '@progress/kendo-data-query';

import { MinMaxConfigurationPopupComponent } from '../min-max/min-max-configuration-popup.component';
import { SelectListConfigurationPopupComponent } from '../select-list/selectlist-configuration-popup.component';

declare var jQuery: any;

@Component({
	moduleId: module.id,
	selector: 'field-settings-grid',
	encapsulation: ViewEncapsulation.None,
	exportAs: 'fieldSettingsGrid',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/grid/field-settings-grid.component.html',
	styles: [`
		.float-right { float: right;}
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
	private fieldsSettings: FieldSettingsModel[];
	private gridData: GridDataResult;
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

	private availableControls = [
		{ text: 'List', value: 'List' },
		{ text: 'String', value: 'String' },
		{ text: 'YesNo', value: 'YesNo' }
	];
	private availableyFieldType = ['All', 'Custom Fields', 'Standard Fields'];

	static readonly ORDER_MIN_VALUE = 0;

	constructor(private loaderService: UILoaderService, private prompt: UIPromptService) { }

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
				value: this.gridState.filter.fieldType === 'Custom Fields'
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

	protected onDelete(dataItem: FieldSettingsModel): void {
		this.deleteEmitter.emit({
			field: dataItem,
			domain: this.data.domain,
			callback: () => {
				this.refresh();
			}
		});
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
			model.control = 'String';
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
			(field.control === 'List' || field.control === 'YesNo')) {
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

	protected onControlChange(
		dataItem: FieldSettingsModel,
		selectList: SelectListConfigurationPopupComponent,
		minMax: MinMaxConfigurationPopupComponent): void {
		switch (dataItem.control) {
			case 'List':

				if (dataItem.constraints.values &&
					dataItem.constraints.values.indexOf('Yes') !== -1 &&
					dataItem.constraints.values.indexOf('No') !== -1) {
					dataItem.constraints.values = [];
				}
				if (!dataItem.constraints.values ||
					dataItem.constraints.values.length === 0) {
					selectList.onToggle();
				} else {
					selectList.show = false;
				}
				break;
			case 'String':
				dataItem.constraints.values = [];
				if (!dataItem.constraints.minSize ||
					!dataItem.constraints.maxSize) {
					minMax.onToggle();
				} else {
					minMax.show = false;
				}
				break;
			case 'YesNo':
				dataItem.constraints.values = ['Yes', 'No'];
				if (dataItem.constraints.values.indexOf(dataItem.default) === -1) {
					dataItem.default = null;
				}
				if (!dataItem.constraints.required) {
					dataItem.constraints.values.splice(0, 0, '');
				}
				break;
			default:
				break;
		}
	}

	protected onControlModelChange(
		newValue: 'List' | 'String' | 'YesNo' | '',
		dataItem: FieldSettingsModel,
		selectList: SelectListConfigurationPopupComponent,
		minMax: MinMaxConfigurationPopupComponent) {
		if (dataItem.control === 'List') {
			this.prompt.open(
				'Confirmation Required',
				'Changing the control will lose all List options. Click Ok to continue otherwise Cancel',
				'Ok', 'Cancel').then(result => {
					if (result) {
						dataItem.control = newValue;
						this.onControlChange(dataItem, selectList, minMax);
					} else {
						setTimeout(() => {
							jQuery('#control' + dataItem.field).val('List');
						});
					}
				});
		} else {
			dataItem.control = newValue;
			this.onControlChange(dataItem, selectList, minMax);
		}
	}

	protected hasError(label: string) {
		return label.trim() === '' || this.data.fields.filter(item => item.label === label.trim()).length > 1;
	}

}
import { Component, Input, Output, EventEmitter, OnInit, ViewEncapsulation } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';

import { UILoaderService } from '../../../../shared/services/ui-loader.service';
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
	`]
})
export class FieldSettingsGridComponent implements OnInit {
	@Output('save') saveEmitter = new EventEmitter<any>();
	@Output('cancel') cancelEmitter = new EventEmitter<any>();
	@Output('add') addEmitter = new EventEmitter<any>();
	@Output('share') shareEmitter = new EventEmitter<any>();
	@Output('delete') deleteEmitter = new EventEmitter<any>();

	@Input('data') data: DomainModel;
	@Input('state') gridState: any;
	private fieldsSettings: FieldSettingsModel[];

	private filter = {
		search: '',
		fieldType: 'All'
	};
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
		{ text: 'List', value: 'Select List' },
		{ text: 'String', value: 'String' },
		{ text: 'YesNo', value: 'YesNo' }
	];
	private availableyFieldType = ['All', 'Custom Fields', 'Standard Fields'];

	constructor(private loaderService: UILoaderService) { }

	ngOnInit(): void {
		this.fieldsSettings = this.data.fields;
		this.refresh();
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.refresh();
	}

	protected onFilter(): void {
		this.state.filter.filters = [];
		this.fieldsSettings = this.data.fields;
		if (this.filter.search !== '') {
			this.fieldsSettings = this.data.fields.filter(
				item => item.field.indexOf(this.filter.search) !== -1 ||
					item.label.indexOf(this.filter.search) !== -1);
		}
		if (this.filter.fieldType !== 'All') {
			this.state.filter.filters.push({
				field: 'udf',
				operator: 'eq',
				value: this.filter.fieldType === 'Custom Fields'
			});
		}
		this.refresh();
	}

	protected onEdit(): void {
		this.loaderService.show();
		setTimeout(() => {
			this.isEditing = true;
			this.sortable = { mode: 'single' };

			this.filter = {
				search: '',
				fieldType: 'All'
			};
			this.isFilterDisabled = true;
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
					dir: 'asc',
					field: 'order'
				}
			];
			let model = new FieldSettingsModel();
			model.field = custom;
			model.constraints = {
				required: false
			};
			model['isNew'] = true;
			let availableOrder = this.fieldsSettings.map(f => f.order).sort((a, b) => a - b);
			model.order = availableOrder[availableOrder.length - 1] + 1;
			this.fieldsSettings.push(model);
			this.refresh();

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

	protected onClearTextFilter(): void {
		this.filter.search = '';
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
		this.fieldsSettings = this.data.fields;
	}

	public refresh(): void {
		this.gridData = process(this.fieldsSettings, this.state);
	}

	protected onControlChange(
		dataItem: FieldSettingsModel,
		selectList: SelectListConfigurationPopupComponent,
		minMax: MinMaxConfigurationPopupComponent): void {
		switch (dataItem.control) {
			case 'Select List':
				if (!dataItem.constraints.values ||
					dataItem.constraints.values.length === 0) {
					selectList.onToggle();
				} else {
					selectList.show = false;
				}
				break;
			case 'String':
				if (!dataItem.constraints.minSize ||
					!dataItem.constraints.maxSize) {
					minMax.onToggle();
				} else {
					minMax.show = false;
				}
				break;
			default:
				break;
		}
	}
}
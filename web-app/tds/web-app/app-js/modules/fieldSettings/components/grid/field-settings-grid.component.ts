import { Component, Input, OnInit, ViewEncapsulation, ViewChild } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';
import { FieldSettingsService } from '../../service/field-settings.service';
import { PermissionService } from '../../../../shared/services/permission.service';
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
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/grid/field-settings-grid.component.html',
	styles: [`
		.float-right { float: right;}
		.k-grid { height:calc(100vh - 225px); }
		tr .text-center { text-align: center; }
	`]
})
export class FieldSettingsGridComponent implements OnInit {
	@Input('data') data: DomainModel;
	private dataSignature: string;
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
	private isSubmitted = false;
	private sortable: boolean | object = { mode: 'single' };

	private availableControls = [
		{ text: 'List', value: 'Select List' },
		{ text: 'String', value: 'String' },
		{ text: 'YesNo', value: 'YesNo' }
	];
	private availableyFieldType = ['All', 'User Defined Fields', 'Standard Fields'];

	constructor(
		private fieldService: FieldSettingsService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private loaderService: UILoaderService) { }

	ngOnInit(): void {
		this.fieldsSettings = this.data.fields;
		this.dataSignature = JSON.stringify(this.data);
		this.refresh();
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.refresh();
	}

	protected onFilter(): void {
		this.state.filter.filters = [];
		if (this.filter.search !== '') {
			this.state.filter.filters.push({
				field: 'field',
				operator: 'contains',
				value: this.filter.search
			});
			this.state.filter.filters.push({
				field: 'label',
				operator: 'contains',
				value: this.filter.search
			});
		}
		if (this.filter.fieldType !== 'All') {
			this.state.filter.filters.push({
				field: 'udf',
				operator: 'eq',
				value: this.filter.fieldType === 'User Defined Fields'
			});
		}
		this.refresh();
	}

	protected onEdit(): void {
		this.loaderService.show();
		setTimeout(() => {
			this.isEditing = true;
			this.sortable = false;
			this.state.sort = [
				{
					dir: 'desc',
					field: 'isNew'
				}, {
					dir: 'asc',
					field: 'order'
				}
			];
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
		if (this.isEditAvailable() && this.isValid()) {
			this.reset();
			this.fieldsSettings.filter(x => x['isNew']).forEach(x => delete x['isNew']);
			this.fieldService.saveFieldSettings(this.data)
				.subscribe((res) => this.refresh(true));
		} else {
			this.isSubmitted = true;
			this.refresh();
		}
	}

	protected onCancel(): void {
		if (this.isDirty()) {
			this.prompt.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
					if (result) {
						this.reset();
						this.refresh(true);
					}
				});
		} else {
			this.reset();
			this.refresh();
		}
	}

	protected onDelete(dataItem: FieldSettingsModel): void {
		this.fieldsSettings.splice(this.fieldsSettings.indexOf(dataItem), 1);
		this.refresh();
	}

	protected onAddCustom(): void {
		let model = new FieldSettingsModel();
		model.field = this.availableCustomNumbers();
		model.constraints = {
			required: false
		};
		model['isNew'] = true;
		this.fieldsSettings.push(model);
		this.refresh();
		model.order = this.fieldsSettings.length + 1;
		setTimeout(function () {
			jQuery('#' + model.field).focus();
		});
	}

	protected onClearTextFilter(): void {
		this.filter.search = '';
		this.onFilter();
	}

	protected reset(): void {
		this.isEditing = false;
		this.isSubmitted = false;
		this.sortable = { mode: 'single' };
		this.isFilterDisabled = false;
		this.state.sort = [{
			dir: 'asc',
			field: 'order'
		}];
	}

	protected refresh(fetch = false): void {
		if (fetch) {
			this.fieldService.getFieldSettingsByDomain(this.data.domain).subscribe(
				(result) => {
					if (result[0]) {
						this.data = result[0];
						this.fieldsSettings = this.data.fields;
						this.refresh();
					}
				},
				(err) => console.log(err));
		} else {
			this.gridData = process(this.fieldsSettings, this.state);
		}
	}

	protected availableCustomNumbers(): string {
		let custom = this.fieldsSettings
			.filter(item => /^custom/i.test(item.field))
			.map((item) => +item.field.replace(/[a-z]/ig, ''))
			.sort((a, b) => a - b);
		let number = custom.findIndex((item, i) => item !== i + 1);
		return 'custom' + ((number === -1 ? custom.length : number) + 1);
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission('ProjectFieldSettingsEdit');
	}

	protected isValid(): boolean {
		return this.fieldsSettings.filter(item =>
			!item.label || !item.field).length === 0;
	}

	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.data);
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
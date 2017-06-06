import { Component, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { FieldSettingsService } from '../../service/field-settings.service';
import { GridDataResult, DataStateChangeEvent, GridComponent, RowClassArgs } from '@progress/kendo-angular-grid';
import { process, State } from '@progress/kendo-data-query';

@Component({
	moduleId: module.id,
	selector: 'field-settings-grid',
	encapsulation: ViewEncapsulation.None,
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/grid/field-settings-grid.component.html',
	styles: [`
		.float-right { float: right;}
	`]
})
export class FieldSettingsGridComponent implements OnInit {
	@Input('data') fieldsSettings: FieldSettingsModel[];
	@Input() domain: string;
	@ViewChild('fieldSettingGrid') fieldSettingGrid: GridComponent;

	private search = '';
	private fieldType = 'All';
	private gridData: GridDataResult;
	private state: State = {
		skip: 0,
		take: 25,
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
			logic: 'and'
		}
	};
	private isEditing = false;
	private isFilterDisabled = false;
	private isSubmitted = false;
	private sortable: boolean | object = { mode: 'single' };

	private availableTypes = ['String', 'Number', 'Boolean', 'Date', 'Array'];
	private availableControls = ['Select List', 'Checkbox', 'YesNo', 'DatePicker', 'TextArea'];
	private availableyFieldType = ['All', 'User Defined Fields', 'Standard Fields'];
	constructor(private fieldService: FieldSettingsService) { }

	ngOnInit(): void {
		this.refresh();
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.refresh();
	}

	protected onFilter(): void {
		this.state.filter.filters = [];
		if (this.search !== '') {
			this.state.filter.filters.push({
				field: 'field',
				operator: 'contains',
				value: this.search
			});
		}
		if (this.fieldType !== 'All') {
			this.state.filter.filters.push({
				field: 'udf',
				operator: 'eq',
				value: this.fieldType === 'User Defined Fields'
			});
		}
		this.refresh();
	}

	protected onEdit(): void {
		this.isEditing = true;
		this.sortable = false;
		this.state.sort = [{
			dir: 'desc',
			field: 'isNew'
		}, {
			dir: 'asc',
			field: 'order'
		}];
		this.search = '';
		this.isFilterDisabled = true;
		this.onFilter();
	}

	protected onSaveAll(): void {
		if (this.fieldsSettings.filter(item =>
			!item.label || !item.field).length === 0) {
			this.reset();
			this.fieldsSettings.filter(x => x.isNew).forEach(x => x.isNew = false);
			this.fieldService.saveFieldSettings(this.domain, this.fieldsSettings)
				.subscribe();
		} else {
			this.isSubmitted = true;
			this.state.skip = 0;
			this.refresh();
		}
	}

	protected onCancel(): void {
		this.reset();
		this.fieldService.getFieldSettingsByDomain(this.domain).subscribe(
			(result) => {
				if (result[0]) {
					this.fieldsSettings = result[0].fields;
					this.refresh();
				}
			},
			(err) => console.log(err));
	}

	protected onDelete(dataItem: FieldSettingsModel): void {
		this.fieldsSettings.splice(this.fieldsSettings.indexOf(dataItem), 1);
		this.refresh();
	}

	protected onAddCustom(): void {
		let model = new FieldSettingsModel();
		model.field = this.availableCustomNumbers();
		this.fieldsSettings.push(model);
		this.refresh();
		model.order = this.fieldsSettings.length + 1;
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

	protected refresh(): void {
		this.gridData = process(this.fieldsSettings, this.state);
	}

	protected availableCustomNumbers(): string {
		let custom = this.fieldsSettings
			.filter(item => /^custom/i.test(item.field))
			.map((item) => +item.field.replace(/[a-z]/ig, ''))
			.sort((a, b) => a - b);
		let number = custom.findIndex((item, i) => item !== i + 1);
		return 'custom' + ((number === -1 ? custom.length : number) + 1);
	}
}
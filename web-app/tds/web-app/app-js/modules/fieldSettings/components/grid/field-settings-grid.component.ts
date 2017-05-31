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

	private search: string;
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
	private availableControls = ['Select', 'Checkbox', 'YesNo', 'DatePicker', 'TextArea', 'Range'];

	constructor(private fieldService: FieldSettingsService) { }

	ngOnInit(): void {
		this.refresh();
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.refresh();
	}

	protected onFilter(): void {
		this.state.filter.filters = [{
			field: 'field',
			operator: 'contains',
			value: this.search
		}];
		this.refresh();
	}

	protected onEdit(): void {
		this.isEditing = true;
		this.sortable = false;
		this.search = '';
		this.isFilterDisabled = true;
		this.onFilter();
	}

	protected onSaveAll(): void {
		if (this.fieldsSettings.filter(item =>
			!item.label || !item.field).length === 0) {
			this.reset();
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
		this.fieldsSettings.push(model);
		this.refresh();
	}

	protected reset(): void {
		this.isEditing = false;
		this.isSubmitted = false;
		this.sortable = { mode: 'single' };
		this.isFilterDisabled = false;
	}

	protected refresh(): void {
		this.gridData = process(this.fieldsSettings, this.state);
	}
}
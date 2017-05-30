import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { FieldSettingsService } from '../../service/field-settings.service';
import { GridDataResult, DataStateChangeEvent, GridComponent } from '@progress/kendo-angular-grid';
import { process, State } from '@progress/kendo-data-query';

@Component({
	moduleId: module.id,
	selector: 'field-settings-grid',
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
	private sortable: boolean | object = { mode: 'single' };

	constructor(private fieldService: FieldSettingsService) { }

	ngOnInit(): void {
		this.gridData = process(this.fieldsSettings, this.state);
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.gridData = process(this.fieldsSettings, this.state);
	}

	protected onFilter(): void {
		this.state.filter.filters = [{
			field: 'field',
			operator: 'contains',
			value: this.search
		}];
		this.gridData = process(this.fieldsSettings, this.state);
	}

	protected onEdit(): void {
		this.isEditing = true;
		this.sortable = false;
	}

	protected onSaveAll(): void {
		this.isEditing = false;
		this.sortable = { mode: 'single' };
	}

	protected onCancel(): void {
		this.isEditing = false;
		this.fieldService.getFieldSettingsByDomain(this.domain).subscribe(
			(result) => {
				if (result[0]) {
					this.fieldsSettings = result[0].fields;
					this.gridData = process(this.fieldsSettings, this.state);
				}
			},
			(err) => console.log(err));
		this.sortable = { mode: 'single' };
	}

	protected onDelete(dataItem: FieldSettingsModel): void {
		this.fieldsSettings.splice(this.fieldsSettings.indexOf(dataItem), 1);
		this.gridData = process(this.fieldsSettings, this.state);
	}

	protected onAddCustom(): void {
		let model = new FieldSettingsModel();
		this.fieldsSettings.push(model);
		this.fieldSettingGrid.addRow(model);
		this.fieldSettingGrid.closeRow(0);
	}
}
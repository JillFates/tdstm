import { Component, Input, OnInit } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { FieldSettingsService } from '../../service/field-settings.service';
import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';
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
	@Input('asset-class') assetClass: string;

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
	}

	protected onSaveAll(): void {
		this.isEditing = false;
	}

	protected onCancel(): void {
		this.isEditing = false;
		// TODO: need a way to clone this content at init so we can revert the changes
		this.gridData = process(this.fieldsSettings, this.state);
	}
}
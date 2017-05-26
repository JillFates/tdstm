import { Component, Input, OnInit } from '@angular/core';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';
import { process, State } from '@progress/kendo-data-query';

@Component({
	moduleId: module.id,
	selector: 'field-settings-grid',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/grid/field-settings-grid.component.html'
})
export class FieldSettingsGridComponent implements OnInit {
	@Input('data') fieldsSettings: FieldSettingsModel[];
	@Input('asset-class') assetClass: string;

	private search: string;
	private gridData: GridDataResult;
	private state: State = {
		skip: 0,
		take: 10,
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
		console.log('editig');
	}
}
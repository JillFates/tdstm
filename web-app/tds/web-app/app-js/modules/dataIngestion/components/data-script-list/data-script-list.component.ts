import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { Observable } from 'rxjs/Observable';

import { DataIngestionService } from '../../service/data-ingestion.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { COLUMN_MIN_WIDTH, DataScriptColumnModel, DataScriptRowModel, Flatten} from '../../model/data-script.model';
import { filterBy, CompositeFilterDescriptor } from '@progress/kendo-data-query';
import { NotifierService } from '../../../../shared/services/notifier.service';

@Component({
	selector: 'data-script-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-list/data-script-list.component.html'
})
export class DataScriptListComponent {

	public filter: CompositeFilterDescriptor;
	public dataScriptColumnModel = new DataScriptColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public gridData: any[];
	public resultSet: DataScriptRowModel[];

	constructor(
		private stateService: StateService,
		@Inject('dataScripts') dataScripts: Observable<DataScriptRowModel[]>,
		private permissionService: PermissionService,
		private assetExpService: DataIngestionService,
		private prompt: UIPromptService,
		private notifier: NotifierService) {
		dataScripts.subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = filterBy(this.resultSet, this.filter);
			},
			(err) => console.log(err));
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.filter = filter;
		this.gridData = filterBy(this.resultSet, filter);
	}

	public onFilter(column: any): void {
		let root = this.filter || { logic: 'and', filters: []};

		let [filter] = Flatten(root).filter(x => x.field === column.property);

		if (column.type === 'text') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'contains',
					value: column.filter,
					ignoreCase: true
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}
		this.filterChange(root);
	}

	public clearValue(column: any): void {
		column.filter = '';
		this.onFilter(column);
	}
}
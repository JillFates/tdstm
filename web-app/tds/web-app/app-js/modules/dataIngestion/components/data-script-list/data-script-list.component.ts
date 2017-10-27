import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { Observable } from 'rxjs/Observable';
import { filterBy, CompositeFilterDescriptor } from '@progress/kendo-data-query';

import { DataIngestionService } from '../../service/data-ingestion.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { COLUMN_MIN_WIDTH, DataScriptColumnModel, DataScriptModel, Flatten, ModeType, ModalType} from '../../model/data-script.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { DataScriptViewEditComponent } from '../data-script-view-edit/data-script-view-edit.component';

@Component({
	selector: 'data-script-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-list/data-script-list.component.html',
	styles: [`
        #btnCreateDataScript { margin-left: 16px; }
	`]
})
export class DataScriptListComponent {

	public filter: CompositeFilterDescriptor;
	public dataScriptColumnModel = new DataScriptColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public gridData: any[];
	public resultSet: DataScriptModel[];

	constructor(
		private stateService: StateService,
		private dialogService: UIDialogService,
		@Inject('dataScripts') dataScripts: Observable<DataScriptModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
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

	public onCreateDataScript(): void {
		let dataScriptModel: DataScriptModel = {
			name: '',
			description: '',
			mode: ModeType.IMPORT,
			provider: { id: null, name: ''}
		};

		this.dialogService.open(DataScriptViewEditComponent, [
			{ provide: DataScriptModel, useValue: dataScriptModel },
			{ provide: Number, useValue: ModalType.CREATE}
		]).then(result => {
			console.log(result);
		}).catch(result => {
			console.log('error');
		});

	}

	public reloadDataScripts(): void {
		this.dataIngestionService.getDataScripts().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = filterBy(this.resultSet, this.filter);
			},
			(err) => console.log(err));
	}
}
import {Component, Inject, ViewChild} from '@angular/core';
import {StateService} from '@uirouter/angular';
import {Observable} from 'rxjs/Observable';
import {filterBy, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {CellClickEvent} from '@progress/kendo-angular-grid';

import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {COLUMN_MIN_WIDTH, DataScriptColumnModel, DataScriptModel, Flatten, ModeType, ActionType} from '../../model/data-script.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {DataScriptViewEditComponent} from '../data-script-view-edit/data-script-view-edit.component';

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
	public actionType = ActionType;
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

	protected onFilter(column: any): void {
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

	protected clearValue(column: any): void {
		column.filter = '';
		this.onFilter(column);
	}

	protected onCreateDataScript(): void {
		let dataScriptModel: DataScriptModel = {
			name: '',
			description: '',
			mode: ModeType.IMPORT,
			provider: { id: null, name: ''}
		};
		this.openDataScriptDialogViewEdit(dataScriptModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param dataItem
	 */
	protected onEditDataScript(type: ActionType, dataItem: any): void {
		let dataScriptModel: DataScriptModel = {
			name: dataItem.name,
			description: dataItem.description,
			mode: dataItem.mode,
			provider: dataItem.providerList
		};
		this.openDataScriptDialogViewEdit(dataScriptModel, ActionType.EDIT);
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteDataScript(dataItem: any): void {
		console.log(dataItem);
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		this.openDataScriptDialogViewEdit(event['dataItem'], ActionType.VIEW);
	}

	protected reloadDataScripts(): void {
		this.dataIngestionService.getDataScripts().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = filterBy(this.resultSet, this.filter);
			},
			(err) => console.log(err));
	}

	/**
	 * Open The Dialog to Create, View or Edit the DataScript
	 * @param {DataScriptModel} dataScriptModel
	 * @param {number} actionType
	 */
	private openDataScriptDialogViewEdit(dataScriptModel: DataScriptModel, actionType: number): void {
		this.dialogService.open(DataScriptViewEditComponent, [
			{ provide: DataScriptModel, useValue: dataScriptModel },
			{ provide: Number, useValue: actionType}
		]).then(result => {
			console.log(result);
		}).catch(result => {
			console.log('error');
		});
	}
}
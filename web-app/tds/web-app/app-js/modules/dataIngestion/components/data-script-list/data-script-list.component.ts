import {Component, Inject, ViewChild} from '@angular/core';
import {StateService} from '@uirouter/angular';
import {Observable} from 'rxjs/Observable';
import {filterBy, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {CellClickEvent, GridComponent, RowArgs} from '@progress/kendo-angular-grid';

import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {COLUMN_MIN_WIDTH, DataScriptColumnModel, DataScriptModel, Flatten, DataScriptMode, ActionType} from '../../model/data-script.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
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
	public dataScriptMode = DataScriptMode;
	public gridData: any[];
	public resultSet: DataScriptModel[];
	public selectedRows = [];
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;

	constructor(
		private stateService: StateService,
		private dialogService: UIDialogService,
		@Inject('dataScripts') dataScripts: Observable<DataScriptModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService,
		private notifierService: NotifierService) {
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

		if (!column.filter) {
			column.filter = '';
		}

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

		if (column.type === 'date') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'gte',
					value: column.filter,
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

	protected clearValue(column: any, value?: any): void {
		column.filter = '';
		if (this.filter && this.filter.filters.length > 0) {
			const filterIndex = this.filter.filters.findIndex((r: any) => r.field === column.property);
			this.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.filter);
		}
	}

	protected onCreateDataScript(): void {
		let dataScriptModel: DataScriptModel = {
			name: '',
			description: '',
			mode: DataScriptMode.IMPORT,
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
		this.openDataScriptDialogViewEdit(dataItem, ActionType.EDIT);
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteDataScript(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'There are Ingestion Batches that have used this Datasource. Deleting this will not delete the batches but will no longer reference a Datasource. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteDataScript(dataItem.id).subscribe(
						(result) => {
							this.reloadDataScripts();
						},
						(err) => console.log(err));
				}
			});
		console.log(dataItem);
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			this.selectRow(event['dataItem'].id);
			this.openDataScriptDialogViewEdit(event['dataItem'], ActionType.VIEW);
		}
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
			{provide: DataScriptModel, useValue: dataScriptModel},
			{provide: Number, useValue: actionType}
		]).then(result => {
			this.reloadDataScripts();
			if (actionType === ActionType.CREATE) {
				setTimeout(() => {
					this.selectRow(result.dataScript.id);
					this.openDataScriptDialogViewEdit(result.dataScript, ActionType.VIEW);
				}, 500);
			}
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
	}
}
import { Component, Inject } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { process, CompositeFilterDescriptor, SortDescriptor, State } from '@progress/kendo-data-query';
import { CellClickEvent, RowArgs, DataStateChangeEvent, GridDataResult } from '@progress/kendo-angular-grid';

import { DataIngestionService } from '../../service/data-ingestion.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { COLUMN_MIN_WIDTH, DataScriptColumnModel, DataScriptModel, DataScriptMode, Flatten, ActionType } from '../../model/data-script.model';
import { DataScriptViewEditComponent } from '../data-script-view-edit/data-script-view-edit.component';

@Component({
	selector: 'data-script-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-list/data-script-list.component.html',
	styles: [`
		#btnCreateDataScript { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class DataScriptListComponent {

	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};

	public pageSize = 25;
	public skip = 0;
	public dataScriptColumnModel = new DataScriptColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: DataScriptModel[];
	public selectedRows = [];
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;

	constructor(
		private dialogService: UIDialogService,
		@Inject('dataScripts') dataScripts: Observable<DataScriptModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		dataScripts.subscribe(
			(result) => {
				this.resultSet = result;
				this.resultSet.forEach(x => {
					x['modeFormat'] = (x.mode as any) ? 'Export' : 'Import';
				});
				this.gridData = process(this.resultSet, this.state);
			},
			(err) => console.log(err));
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	protected onFilter(column: any): void {
		let root = this.state.filter || { logic: 'and', filters: [] };

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
		if (this.state.filter && this.state.filter.filters.length > 0) {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === column.property);
			this.state.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.state.filter);
		}
	}

	protected onCreateDataScript(): void {
		let dataScriptModel: DataScriptModel = {
			name: '',
			description: '',
			mode: DataScriptMode.IMPORT,
			provider: { id: null, name: '' }
		};
		this.openDataScriptDialogViewEdit(dataScriptModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param dataItem
	 */
	protected onEditDataScript(dataItem: any): void {
		this.openDataScriptDialogViewEdit(dataItem, ActionType.EDIT);
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteDataScript(dataItem: any): void {
		this.dataIngestionService.validateDeleteScript(dataItem.id).subscribe(
			(result) => {
				if (result && result['canDelete']) {
					this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
						.then((res) => {
							if (res) {
								this.deleteDataScript(dataItem);
							}
						});
				} else {
					this.prompt.open('Confirmation Required', 'There are Ingestion Batches that have used this DataScript. Deleting this will not delete the batches but will no longer reference a DataScript. Do you want to proceed?', 'Yes', 'No')
						.then((res) => {
							if (res) {
								this.deleteDataScript(dataItem);
							}
						});
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Execute the Service to delete the DataScript
	 */
	private deleteDataScript(dataItem: any): void {
		this.dataIngestionService.deleteDataScript(dataItem.id).subscribe(
			(result) => {
				this.reloadDataScripts();
			},
			(err) => console.log(err));
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
				this.gridData = process(this.resultSet, this.state);
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
			{ provide: Number, useValue: actionType }
		]).then(result => {
			this.reloadDataScripts();
			if (actionType === ActionType.CREATE) {
				setTimeout(() => {
					this.selectRow(result.dataScript.id);
					this.openDataScriptDialogViewEdit(result.dataScript, ActionType.VIEW);
				}, 500);
			}
		}).catch(result => {
			// on dialog close, do nothing ..
		});
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.gridData = process(this.resultSet, this.state);
	}
}

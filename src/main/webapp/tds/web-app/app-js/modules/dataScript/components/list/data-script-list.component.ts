// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Services
import {UserContextService} from '../../../auth/service/user-context.service';
import {DataScriptService} from '../../service/data-script.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
// Components
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DataScriptViewEditComponent} from '../view-edit/data-script-view-edit.component';
// Models
import {
	COLUMN_MIN_WIDTH,
	DataScriptColumnModel,
	DataScriptModel,
	DataScriptMode,
	ActionType
} from '../../model/data-script.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {UserContextModel} from '../../../auth/model/user-context.model';
// Kendo
import {process, CompositeFilterDescriptor, State} from '@progress/kendo-data-query';
import {CellClickEvent, RowArgs, GridDataResult} from '@progress/kendo-angular-grid';

@Component({
	selector: 'data-script-list',
	templateUrl: 'data-script-list.component.html',
	styles: [`
		#btnCreateDataScript { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class DataScriptListComponent implements OnInit {
	protected gridColumns: any[];

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

	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public skip = 0;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public dataScriptColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: DataScriptModel[];
	public selectedRows = [];
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;
	public dateFormat = '';

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private dataIngestionService: DataScriptService,
		private prompt: UIPromptService,
		private userContext: UserContextService) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.setDataGrid(this.route.snapshot.data['dataScripts']);
	}

	/**
	 * Set the grid data, mapping the modeFormat column
	 * @param {DataScriptModel[]} result
	 */
	setDataGrid(result: DataScriptModel[]): void {
		this.resultSet = result;
		this.resultSet.forEach(item => {
			item['modeFormat'] = item.mode ? 'Export' : 'Import'
		});

		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.userContext.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(userContext.dateFormat);
				this.dataScriptColumnModel = new DataScriptColumnModel(`{0:${this.dateFormat}}`);
				this.gridColumns = this.dataScriptColumnModel.columns.filter((column) => column.type !== 'action');
			});
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
		const root = this.dataIngestionService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.dataIngestionService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
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
			(result: DataScriptModel[]) => { this.setDataGrid(result); },
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
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.gridData = process(this.resultSet, this.state);
	}
}

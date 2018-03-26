import {Component, Inject} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {process, CompositeFilterDescriptor, SortDescriptor, State} from '@progress/kendo-data-query';
import {CellClickEvent, RowArgs, DataStateChangeEvent, GridDataResult} from '@progress/kendo-angular-grid';

import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {MAX_OPTIONS, MAX_DEFAULT} from '../../../../shared/model/constants';
import {APIActionColumnModel, APIActionModel, EventReaction, EventReactionType} from '../../model/api-action.model';
import {
	COLUMN_MIN_WIDTH,
	Flatten,
	ActionType,
	BooleanFilterData,
	DefaultBooleanFilterData
} from '../../../../shared/model/data-list-grid.model';
import {APIActionViewEditComponent} from '../api-action-view-edit/api-action-view-edit.component';
import {DIALOG_SIZE, INTERVAL} from '../../../../shared/model/constants';

@Component({
	selector: 'api-action-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/api-action-list/api-action-list.component.html',
	styles: [`
		#btnCreate { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class APIActionListComponent {

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

	public skip = 0;
	public pageSize = MAX_DEFAULT;
	public defaultPageOptions = MAX_OPTIONS;
	public apiActionColumnModel = new APIActionColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: APIActionModel[];
	public selectedRows = [];
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	private interval = INTERVAL;
	private openLastItemId = 0;

	constructor(
		private dialogService: UIDialogService,
		@Inject('apiActions') apiActions: Observable<APIActionModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		apiActions
			.subscribe((result) => {
					this.resultSet = result;
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

		if (column.type === 'boolean') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'eq',
					value: (column.filter === 'True')
				});
			} else {
				if (column.filter === this.defaultBooleanFilterData) {
					this.clearValue(column);
				} else {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = (column.filter === 'True');
				}
			}
		}

		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		column.filter = '';
		if (this.state.filter && this.state.filter.filters.length > 0) {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === column.property);
			this.state.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.state.filter);
		}
	}

	/**
	 * Create a new API Action
	 */
	protected onCreate(): void {
		let apiActionModel = new APIActionModel();
		this.openAPIActionDialogViewEdit(apiActionModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param dataItem
	 */
	protected onEdit(dataItem: any): void {
		this.openAPIActionDialogViewEdit(dataItem, ActionType.EDIT);
	}

	/**
	 * Delete the selected API Action
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteAPIAction(dataItem.id).subscribe(
						(result) => {
							this.reloadData();
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			this.selectRow(event['dataItem'].id);
			this.openAPIActionDialogViewEdit(event['dataItem'], ActionType.VIEW);
		}
	}

	protected reloadData(): void {
		this.dataIngestionService.getAPIActions().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);

				if (this.openLastItemId !== 0) {
					setTimeout(() => {
						this.selectRow(this.openLastItemId);
						let lastApiActionModel = this.gridData.data.find((dataItem) => dataItem.id === this.openLastItemId);
						this.openLastItemId = 0;
						this.openAPIActionDialogViewEdit(lastApiActionModel, ActionType.VIEW);
					}, 700);
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {APIActionModel} apiActionModel
	 * @param {number} actionType
	 */
	private openAPIActionDialogViewEdit(apiActionModel: APIActionModel, actionType: number): void {
		this.dialogService.open(APIActionViewEditComponent, [
			{ provide: APIActionModel, useValue: apiActionModel },
			{ provide: Number, useValue: actionType }
		], DIALOG_SIZE.XLG, false).then(result => {
			if (actionType === ActionType.CREATE) {
				this.openLastItemId = result.id;
			}
			this.reloadData();
		}).catch(result => {
			this.reloadData();
			console.log('Dismissed Dialog');
		});
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
	}

	// Permissions
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionCreate);
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionEdit);
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionDelete);
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

import {Component, Inject, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {process, CompositeFilterDescriptor, SortDescriptor, State} from '@progress/kendo-data-query';
import {CellClickEvent, RowArgs, DataStateChangeEvent, GridDataResult} from '@progress/kendo-angular-grid';

import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
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
import {PreferenceService} from '../../../../shared/services/preference.service';

@Component({
	selector: 'api-action-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/api-action-list/api-action-list.component.html',
	styles: [`
		#btnCreate { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class APIActionListComponent implements OnInit {

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
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public apiActionColumnModel: APIActionColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: APIActionModel[];
	public selectedRows = [];
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	private interval = INTERVAL;
	private openLastItemId = 0;
	public dateFormat = '';

	constructor(
		private dialogService: UIDialogService,
		@Inject('apiActions') apiActions: Observable<APIActionModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		apiActions
			.subscribe((result) => {
					this.resultSet = result;
					this.gridData = process(this.resultSet, this.state);
				},
				(err) => console.log(err));
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.apiActionColumnModel = new APIActionColumnModel(`{0:${dateFormat}}`);
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
		let apiAction: APIActionModel = dataItem as APIActionModel;
		this.dataIngestionService.getAPIAction(apiAction.id).subscribe((response: APIActionModel) => {
			this.openAPIActionDialogViewEdit(response, ActionType.VIEW, apiAction);
		}, error => console.log(error));
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
			let apiAction: APIActionModel = event['dataItem'] as APIActionModel;
			this.selectRow(apiAction.id);
			this.dataIngestionService.getAPIAction(apiAction.id).subscribe((response: APIActionModel) => {
				this.openAPIActionDialogViewEdit(response, ActionType.VIEW, apiAction);
			}, error => console.log(error));
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
						this.openAPIActionDialogViewEdit(lastApiActionModel, ActionType.VIEW, lastApiActionModel);
					}, 700);
				}
			},
			(err) => console.log(err));
	}

	private reloadItem(originalModel: APIActionModel): void {
		this.dataIngestionService.getAPIAction(originalModel.id).subscribe((response: APIActionModel) => {
			Object.assign(originalModel, response);
		}, error => console.log(error));
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {APIActionModel} apiActionModel
	 * @param {number} actionType
	 */
	private openAPIActionDialogViewEdit(apiActionModel: APIActionModel, actionType: number, originalModel?: APIActionModel): void {
		this.dialogService.open(APIActionViewEditComponent, [
			{ provide: APIActionModel, useValue: apiActionModel },
			{ provide: Number, useValue: actionType }
		], DIALOG_SIZE.XLG, false).then(result => {
			if (result) {
				if (actionType === ActionType.CREATE) {
					this.reloadData();
				} else {
					this.reloadItem(originalModel);
				}
			}
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

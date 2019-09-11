import { ActivatedRoute } from '@angular/router';
import { Component, ViewChild } from '@angular/core';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';
import { Observable } from 'rxjs/Observable';
import {
	CellClickEvent,
	DetailCollapseEvent,
	DetailExpandEvent,
	GridComponent,
	GridDataResult, PageChangeEvent
} from '@progress/kendo-angular-grid';
import { ReportsService } from '../../../reports/service/reports.service';
import { TaskService } from '../../service/task.service';
import { DIALOG_SIZE, GRID_DEFAULT_PAGE_SIZE, ModalType } from '../../../../shared/model/constants';
import { forkJoin } from 'rxjs';
import { PREFERENCES_LIST, PreferenceService } from '../../../../shared/services/preference.service';
import { ColumnMenuService } from '@progress/kendo-angular-grid/dist/es2015/column-menu/column-menu.service';
import { SortUtils } from '../../../../shared/utils/sort.utils';
import { taskListColumnsModel } from '../../model/task-list-columns.model';
import { TaskDetailModel } from '../../model/task-detail.model';
import { TaskDetailComponent } from '../detail/task-detail.component';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { clone, hasIn } from 'ramda';
import { AssetShowComponent } from '../../../assetExplorer/components/asset/asset-show.component';
import { AssetExplorerModule } from '../../../assetExplorer/asset-explorer.module';
import { TaskCreateComponent } from '../create/task-create.component';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { UserContextService } from '../../../auth/service/user-context.service';
import {Store} from '@ngxs/store';
import {SetEvent} from '../../../event/action/event.actions';
import { TaskStatus } from '../../model/task-edit-create.model';
import { SortDescriptor } from '@progress/kendo-data-query';

@Component({
	selector: 'task-list',
	templateUrl: './task-list.component.html'
})
export class TaskListComponent {
	@ViewChild('gridComponent') gridComponent: GridComponent;
	TASK_MANAGER_REFRESH_TIMER: string = PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER;
	TaskStatus: any = TaskStatus;
	private readonly allEventsOption: any = { id: 0, name: 'All Events' };
	justRemaining: boolean;
	justMyTasks: boolean;
	viewUnpublished: boolean;
	eventList: any;
	grid: DataGridOperationsHelper;
	columnsModel: Array<GridColumnModel>;
	loading: boolean;
	hideGrid: boolean;
	selectedCustomColumn: any;
	selectedEvent: any;
	private urlParams: any;
	private pageSize: number;
	private currentCustomColumns: any;
	private allAvailableCustomColumns: Array<any>;
	private userContext: UserContextModel;
	private rowsExpanded: boolean;
	private rowsExpandedMap: any;
	private gridDefaultSort: Array<SortDescriptor>;

	constructor(
		private taskService: TaskService,
		private reportService: ReportsService,
		private userPreferenceService: PreferenceService,
		private store: Store,
		private dialogService: UIDialogService,
		private userContextService: UserContextService,
		private translate: TranslatePipe,
		private activatedRoute: ActivatedRoute) {
		this.gridDefaultSort = [{field: 'taskNumber', dir: 'asc'}];
		this.justMyTasks = false;
		this.loading = true;
		this.hideGrid = true;
		this.rowsExpanded = false;
		this.grid = new DataGridOperationsHelper([], this.gridDefaultSort, null, null, null);
		this.columnsModel = taskListColumnsModel;
		this.selectedCustomColumn = {};
		this.selectedEvent = this.allEventsOption;
		this.urlParams = {};
		this.currentCustomColumns = {};
		this.rowsExpanded = false;
		this.rowsExpandedMap = {};
		this.onLoad();
	}

	/**
	 * Verifies the presence of the filter passed in the url,
	 * if it exists returns the url filter value, otherwise returns the user preference value
	 * @param {string} paramName  Name of the parameter received in the url
	 * @param {string} preferenceKey  Name of user preference key
	*/
	private getUrlParamOrUserPreference(paramName: string, preferenceKey: string): Observable<any> {
		return  hasIn(paramName, this.urlParams) ?
			Observable.of(this.urlParams[paramName]) :
			this.userPreferenceService.getSinglePreference(preferenceKey);
	}

	/**
	 * Load all the user preferences to populate the task manager grid.
	 */
	private onLoad(): void {
		/* ge the parameters passed by the url */
		this.activatedRoute.queryParams
			.subscribe(params => {
				this.urlParams = params;
			});

		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
			this.selectedEvent = userContext.event;
		});
		this.loading = true;

		const observables = forkJoin(
			this.taskService.getCustomColumns(),
			this.getUrlParamOrUserPreference('moveEvent', PREFERENCES_LIST.CURRENT_EVENT_ID),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.TASK_MANAGER_LIST_SIZE),
			this.getUrlParamOrUserPreference('viewUnpublished', PREFERENCES_LIST.VIEW_UNPUBLISHED),
			this.getUrlParamOrUserPreference('justRemaining', PREFERENCES_LIST.JUST_REMAINING),
		);
		observables.subscribe({
				next: value => {
					const [custom, currentEventId, listSize, unpublished, justRemaining] = value;

					// Custom Columns, TaskPref
					this.buildCustomColumns(custom.customColumns, custom.assetCommentFields);
					// Task list size
					this.pageSize = listSize ? parseInt(listSize, 0) : GRID_DEFAULT_PAGE_SIZE;
					this.grid.state.take = this.pageSize;
					// Task View Unpublished
					this.viewUnpublished = unpublished ? (unpublished === 'true' || unpublished === '1') : false;
					// Just Remaining
					this.justRemaining = justRemaining ? justRemaining === '1' : false;
					// params were transferred to local properties,
					// we can remove them from the parameters object
					// and leave only the parameters which are not handled by local properties
					this.urlParams = ObjectUtils.excludeProperties(this.urlParams, ['moveEvent', 'justRemaining', 'viewUnpublished']);
					// Current event
					this.loadEventListAndSearch(currentEventId);
				},
				complete: () => {
					this.hideGrid = false;
				}
			}
		);
	}

	/**
	 * Loads the event list from api, sets the current event selected and runs search().
	 * @param currentEventId
	 */
	private loadEventListAndSearch(currentEventId: any): void {
		this.reportService.getEventList().subscribe(result => {
			this.eventList = [this.allEventsOption].concat(result.data);
			const match = this.eventList.find(item => item.id === parseInt(currentEventId, 0));
			if (match) {
				this.selectedEvent = match;
			} else {
				this.selectedEvent = this.allEventsOption;
			}
			this.search();
		});
	}

	/**
	 * Sets the custom columns headers into the column grid definition
	 * and builds the available custom columns.
	 */
	private buildCustomColumns(customColumns: any, assetCommentFields: any): void {
		if (!customColumns && !assetCommentFields) {
			this.allAvailableCustomColumns = [];
			this.currentCustomColumns = [];
			return;
		}
		Object.values(customColumns).forEach((custom: string, index: number) => {
			const column = `userSelectedCol${ index }`;
			let match = this.columnsModel.find(item => item.property === column);
			match.label = assetCommentFields[custom];
			this.currentCustomColumns[column] = custom;
		});
		this.allAvailableCustomColumns = [];
		Object.entries(assetCommentFields).forEach(entry => {
			this.allAvailableCustomColumns.push({ property: entry[0], label: entry[1] });
		});
		this.allAvailableCustomColumns = this.allAvailableCustomColumns.sort((a, b) => SortUtils.compareByProperty(a, b, 'label'));
	}

	/**
	 * Search Tasks based on UI filters.
	 */
	private search(taskId ?: number): void {
		this.loading = true;
		// Prepare sort, pagination & column filters for search.
		const pageNumber = (this.grid.state.skip / this.grid.state.take) + 1;
		let applySort = false;
		let sortColumn = this.grid.state.sort[0].field;
		let sortOrder = 'ASC';
		if (this.grid.state.sort[0].dir) {
			applySort = true;
			sortOrder = this.grid.state.sort[0].dir.toUpperCase();
			if (sortColumn.startsWith('userSelectedCol')) {
				sortColumn = this.currentCustomColumns[sortColumn];
			}
		}
		const searchRequest = {
			moveEvent: this.selectedEvent.id,
			justRemaining: this.justRemaining ? 1 : 0,
			justMyTasks: this.justMyTasks ? 1 : 0,
			viewUnpublished: this.viewUnpublished ? 1 : 0,
			sortOrder: applySort ? sortOrder : '',
			sortColumn: applySort ? sortColumn : '',
			page: pageNumber,
			rows: this.pageSize
		};

		// Append url filters, in case they were not present in the default filters
		const filters: any = Object.assign({}, searchRequest, this.urlParams);
		// moveEvent should be string for all events
		filters['moveEvent'] = filters.moveEvent === 0 ? '0' : filters.moveEvent;
		this.taskService.getTaskList(filters)
			.subscribe(result => {
				this.reloadGridData(result.rows, result.totalCount);
				this.rowsExpandedMap = {};
				this.rowsExpanded = false;
				this.loading = false;
				setTimeout(() => this.searchTaskAndExpandRow(taskId), 100);
			});
	}

	/**
	 * On Custom Column configuration change. Set the new column configuration and reload the task list.
	 * @param customColumn
	 * @param newColumn
	 * @param service
	 */
	onCustomColumnChange(customColumn: string, newColumn: any, service: ColumnMenuService): void {
		service.close();
		let index = parseInt(customColumn.charAt(customColumn.length - 1), 0) + 1;
		this.taskService.setCustomColumn(this.currentCustomColumns[customColumn], newColumn.property, index)
			.subscribe(result => {
				this.buildCustomColumns(result.customColumns, result.assetCommentFields);
				this.search();
			});
	}

	/**
	 * On Event select change.
	 */
	onEventSelect(): void {
		this.store.dispatch(new SetEvent({id: this.selectedEvent.id, name: this.selectedEvent.name}));
		this.onFiltersChange();
	}

	/**
	 * On Any other change.
	 * @param selection: Array<any>
	 */
	onFiltersChange($event ?: any): void {
		this.search();
	}

	/**
	 * Search for a task on the current grid data and expands it. The rest of the rows gets collapsed.
	 * @param taskId: number
	 */
	searchTaskAndExpandRow(taskId: number): void {
		(this.gridComponent.data as GridDataResult).data.forEach((item, index) => {
			if (item.id === taskId) {
				this.gridComponent.expandRow(index);
			} else {
				this.gridComponent.collapseRow(index);
			}
		});
	}

	/**
	 * On Create Task button handler open the Create task modal.
	 */
	onCreateTaskHandler(): void {
		let taskCreateModel: TaskDetailModel = {
			id: '',
			modal: {
				title: 'Create Task',
				type: ModalType.CREATE
			},
			detail: {
				assetClass: '',
				assetEntity: '',
				assetName: '',
				currentUserId: this.userContext.user.id
			}
		};
		this.dialogService.extra(TaskCreateComponent, [
			{ provide: TaskDetailModel, useValue: taskCreateModel }
		]).then((result) => {
			if (result) {
				this.search();
			}
		}).catch(result => {
			if (result) {
				// console.log(result);
			}
		});
	}

	/**
	 * On Taks Detail button handler oepn the task detail modal.
	 * @param taskRow: any
	 */
	onOpenTaskDetailHandler(taskRow: any): void {
		let taskDetailModel: TaskDetailModel = {
			id: taskRow.id,
			modal: {
				title: 'Task Detail'
			}
		};
		this.dialogService.extra(TaskDetailComponent, [
			{ provide: TaskDetailModel, useValue: taskDetailModel }
		]).then((result) => {
			if (result) {
				if (result.isDeleted) {
					this.taskService.deleteTaskComment(taskRow.id).subscribe(result => {
						this.search();
					});
				} else {
					this.search();
				}
			}
		}).catch(result => {
			if (result) {
				this.search();
			}
		});
	}

	/**
	 * Changes the time estimation of a task, on success immediately update the task updatedTime.
	 * @param taskRow: any
	 * @param days: string
	 */
	changeTimeEst(taskRow: any, days: string): void {
		this.taskService.changeTimeEst(taskRow.id, days)
			.subscribe(() => {
				taskRow.updatedTime = '0s';
			});
	}

	/**
	 * On View Timeline button handler.
	 */
	onViewTimelineHandler(): void {
		this.openLinkInNewTab('/tdstm/task/taskTimeline');
	}

	/**
	 * On View Neighborhood button handler.
	 */
	onViewTaskNeighborHandler(dataItem: any): void {
		this.openLinkInNewTab(`/tdstm/task/taskGraph?neighborhoodTaskId=${ dataItem.id }`)
	}

	/**
	 * On View Task Graph button handler.
	 */
	onViewTaskGraphHandler(): void {
		const url = `/tdstm/task/taskGraph?moveEventId=${ this.selectedEvent.id }&viewUnpublished=${ this.viewUnpublished }`;
		this.openLinkInNewTab(url);
	}

	/**
	 * Opens a link in a new browser tab
	 * @param url
	 */
	openLinkInNewTab(url: string): void {
		window.open(url, '_blank');
	}

	/**
	 * Show the asset popup detail.
	 * @param assetId: number
	 * @param assetClass: string
	 */
	onOpenAssetDetailHandler(taskRow: any): void {
		this.dialogService.open(AssetShowComponent,
			[UIDialogService,
				{ provide: 'ID', useValue: taskRow.assetEntityId },
				{ provide: 'ASSET', useValue: taskRow.assetEntityAssetClass },
				{ provide: 'AssetExplorerModule', useValue: AssetExplorerModule }
			], DIALOG_SIZE.LG).then(result => {
			// console.log('success: ' + result);
		}).catch(result => {
			console.error('rejected: ' + result);
		});
	}

	/**
	 * Update the task status.
	 * @param id: string
	 * @param status: string
	 */
	updateTaskStatus(id: string, status: string): void {
		this.taskService.updateStatus(id, status)
			.subscribe(() => {
				this.search(parseInt(id, 0));
			});
	}

	/**
	 * On Invoke button click.
	 * @param taskRow: any
	 */
	invokeActionHandler(taskRow: any): void {
		this.taskService.invokeAction(taskRow.id)
			.subscribe(result => {
				this.search(parseInt(taskRow.id, 0));
			});
	}

	/**
	 * Row Expand Event Handler. Gathers extra task info for action buttons logic.
	 * @param $event: any
	 */
	onRowDetailExpandHandler($event: DetailExpandEvent): void {
		this.rowsExpandedMap[$event.index] = true;
	}

	/**
	 * Row Collapse Event Handler. Gathers extra task info for action buttons logic.
	 * @param $event: any
	 */
	onRowDetailCollapseHandler($event: DetailCollapseEvent): void {
		this.rowsExpandedMap[$event.index] = false;
	}

	/**
	 * On AssignToMe button Handler, reassigns the task to current user.
	 * @param taskRow: any
	 */
	onAssignToMeHandler(taskRow: any): void {
		const request = {
			id: taskRow.id.toString(),
			status: taskRow.status
		}
		this.taskService.assignToMe(request)
			.subscribe(result => {
				taskRow.assignedTo = this.userContext.person.id;
				this.search(taskRow.id);
			});
	}

	/**
	 * On Reset button click handler.
	 * @param taskRow: any
	 */
	onResetTaskHandler(taskRow: any): void {
		this.taskService.resetTaskAction(taskRow.id).subscribe(result => {
			this.search(taskRow.id);
		});
	}

	/**
	 * On clear filters button click, clear all available filters.
	 */
	onClearFiltersHandler(): void {
		this.grid.clearAllFilters(this.columnsModel);
	}

	/**
	 * Determines if current columns has been filtered (contains value).
	 */
	areFiltersDirty(): boolean {
		return this.columnsModel
			.filter(column => column.filter).length > 0;
	}

	/**
	 * On bulk action button click, expand all rows that can be actionable (ready or started status)
	 */
	onBulkActionHandler(): void {
		const taskRows: Array<any> = (this.gridComponent.data as GridDataResult).data;
		let expandedEvent: DetailExpandEvent = new DetailExpandEvent({});
		if (!this.rowsExpanded) {
			taskRows.forEach((taskRow: any, index: number) => {
				expandedEvent.dataItem = taskRow;
				expandedEvent.index = index;
				if (taskRow.status === 'Ready' || taskRow.status === 'Started') {
					this.onRowDetailExpandHandler(expandedEvent);
					this.gridComponent.expandRow(index);
				}
			});
			this.rowsExpanded = true;
		} else {
			taskRows.forEach((taskRow, index) => {
				expandedEvent.dataItem = taskRow;
				expandedEvent.index = index;
				this.onRowDetailCollapseHandler(expandedEvent);
				this.gridComponent.collapseRow(index);
			});
			this.rowsExpanded = false;
		}
	}

	/**
	 * On cell click open the task action bar
	 * @param $event: CellClickEvent
	 */
	onCellClickHandler($event: CellClickEvent): void {
		if ($event.columnIndex !== 0
			&& this.currentCustomColumns[$event.column.field] !== 'assetName') {
			let expandedEvent: DetailExpandEvent = new DetailExpandEvent({});
			expandedEvent.index = $event.rowIndex;
			expandedEvent.dataItem = $event.dataItem;
			// collapse
			if (this.rowsExpandedMap[$event.rowIndex]) {
				this.onRowDetailCollapseHandler(expandedEvent);
				this.gridComponent.collapseRow($event.rowIndex);
				// expand
			} else {
				this.onRowDetailExpandHandler(expandedEvent);
				this.gridComponent.expandRow($event.rowIndex);
			}
		}
	}

	/**
	 * On Page Change, update grid state & handle pagination on server side.
	 */
	onPageChangeHandler({ skip, take }: PageChangeEvent): void {
		this.grid.state.skip = skip;
		this.grid.state.take = take;
		if (take !== this.pageSize) {
			this.userPreferenceService.setPreference(PREFERENCES_LIST.TASK_MANAGER_LIST_SIZE, this.pageSize.toString())
				.subscribe(() => {/* at this point preference should be saved */ });
		}
		this.pageSize = take;
		this.search();
	}

	/**
	 * On Sort Change, update grid state and search().
	 * @param sort
	 */
	onSortChangeHandler(sort: Array<SortDescriptor>): void {
		this.grid.state.sort = sort;
		this.search();
	}

	/**
	 * Reloads data for current grid.
	 * @param result
	 */
	public reloadGridData(result: any, totalCount: number): void {
		this.grid.resultSet = result;
		this.grid.gridData = {data: result, total: totalCount};
		this.grid.notifyUpdateGridHeight();
	}
}

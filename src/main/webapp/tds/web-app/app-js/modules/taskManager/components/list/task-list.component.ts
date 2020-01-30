import { ActivatedRoute } from '@angular/router';
import { Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';
import {pathOr} from 'ramda';
import {
	CellClickEvent,
	DetailCollapseEvent,
	DetailExpandEvent,
	GridComponent,
	GridDataResult, PageChangeEvent
} from '@progress/kendo-angular-grid';
import { ReportsService } from '../../../reports/service/reports.service';
import { TaskService } from '../../service/task.service';
import {
	DIALOG_SIZE,
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS, LOADER_IDLE_PERIOD,
	ModalType
} from '../../../../shared/model/constants';
import { forkJoin } from 'rxjs';
import { PREFERENCES_LIST, PreferenceService } from '../../../../shared/services/preference.service';
import { ColumnMenuService } from '@progress/kendo-angular-grid/dist/es2015/column-menu/column-menu.service';
import { SortUtils } from '../../../../shared/utils/sort.utils';
import { taskListColumnsModel } from '../../model/task-list-columns.model';
import { TaskDetailModel } from '../../model/task-detail.model';
import { TaskDetailComponent } from '../detail/task-detail.component';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { ObjectUtils } from '../../../../shared/utils/object.utils';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { clone, hasIn } from 'ramda';
import { AssetShowComponent } from '../../../assetExplorer/components/asset/asset-show.component';
import { AssetExplorerModule } from '../../../assetExplorer/asset-explorer.module';
import { TaskEditCreateComponent } from '../edit-create/task-edit-create.component';
import { HeaderActionButtonData } from 'tds-component-library';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { UserContextService } from '../../../auth/service/user-context.service';
import { Store } from '@ngxs/store';
import { SetEvent } from '../../../event/action/event.actions';
import { TaskStatus } from '../../model/task-edit-create.model';
import { FilterDescriptor, SortDescriptor } from '@progress/kendo-data-query';
import { TaskEditCreateModelHelper } from '../common/task-edit-create-model.helper';
import { DateUtils } from '../../../../shared/utils/date.utils';
import { TaskActionInfoModel } from '../../model/task-action-info.model';
import {UILoaderService} from '../../../../shared/services/ui-loader.service';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';

@Component({
	selector: 'task-list',
	templateUrl: './task-list.component.html'
})
export class TaskListComponent implements OnInit {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	@ViewChild('gridComponent', {static: false}) gridComponent: GridComponent;
	TASK_MANAGER_REFRESH_TIMER: string = PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER;
	TaskStatus: any = TaskStatus;
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
	selectedEventId: any;
	GRID_DEFAULT_PAGINATION_OPTIONS = GRID_DEFAULT_PAGINATION_OPTIONS;
	private readonly allEventsOption: any = { id: 0, name: 'All Events' };
	private readonly gridDefaultSort: Array<SortDescriptor>;
	private urlParams: any;
	private dashboardFilters = ['filter', 'status', 'role', 'category'];
	private pageSize: number;
	private currentPage: number;
	private currentCustomColumns: any;
	protected allAvailableCustomColumns: Array<any>;
	private userContext: UserContextModel;
	// Flag that indicates that one or more rows have been expanded
	private rowsExpanded: boolean;
	// Tracks the expansion of individual rows
	private rowsExpandedMap: any;
	// Contains the Action Bar Details for each row
	private taskActionInfoModels: Map<string, TaskActionInfoModel>;
	public hasViewUnpublishedPermission = false;
	public isFiltering  = false;
	public allTasksPermission: boolean;

	constructor(
		private taskService: TaskService,
		private reportService: ReportsService,
		private userPreferenceService: PreferenceService,
		private loaderService: UILoaderService,
		private store: Store,
		private permissionService: PermissionService,
		private dialogService: UIDialogService,
		private userContextService: UserContextService,
		private translate: TranslatePipe,
		private activatedRoute: ActivatedRoute) {
		this.gridDefaultSort = [{field: 'score', dir: 'desc'}];
		this.allTasksPermission = this.permissionService.hasPermission(Permission.TaskManagerAllTasks);
		this.justMyTasks = !this.allTasksPermission;
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
		this.currentPage = 1;
		this.taskActionInfoModels = new Map<string, TaskActionInfoModel>();
		this.hasViewUnpublishedPermission = this.permissionService.hasPermission(Permission.TaskViewUnpublished);
		this.onLoad();
	}

	/**
	 * On Init: remove the min-height that TDSTMLayout.min.js randomly calculates wrong.
	 */
	ngOnInit(): void {
		const contentWrapper = document.getElementsByClassName('content-wrapper')[0];
		if (contentWrapper) {
			// TODO: we can test this calculation among several pages and if it works correctly we can apply to the general app layout.
			// 45px is the header height, 31px is the footer height
			(contentWrapper as any).style.minHeight = 'calc(100vh - (45px + 31px))';
		}
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus-circle',
				iconClass: 'is-solid',
				title: this.translate.transform('TASK_MANAGER.CREATE_TASK'),
				show: true,
				onClick: this.onCreateTaskHandler.bind(this),
			},
			{
				icon: 'pencil',
				title: this.translate.transform('TASK_MANAGER.BULK_ACTION'),
				show: true,
				onClick: this.onBulkActionHandler.bind(this),
			},
		];
	}

	/**
	 * On Custom Column configuration change. Set the new column configuration and reload the task list.
	 * @param customColumn
	 * @param newColumn
	 * @param service
	 */
	onCustomColumnChange(customColumn: string, newColumn: any, service: ColumnMenuService): void {
		service.close();
		let index = parseInt(customColumn.charAt(customColumn.length - 1), 10) + 1;
		this.taskService.setCustomColumn(this.currentCustomColumns[customColumn], newColumn.property, index)
			.subscribe(result => {
				this.buildCustomColumns(result.customColumns, result.assetCommentFields);
				this.search();
			});
	}

	/**
	 * On Event select change.
	 */
	onEventSelect(selected: any): void {
		this.selectedEvent = this.eventList.find(item => item.id === parseInt(selected, 10));
		this.store.dispatch(new SetEvent({ id: this.selectedEvent.id, name: this.selectedEvent.name }));
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
		this.dialogService.extra(TaskEditCreateComponent, [
			{ provide: TaskDetailModel, useValue: taskCreateModel }
		]).then((result) => {
			if (result) {
				this.search();
			}
		}).catch(result => {
			if (result) {
				// nothing
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
				} else if (result.shouldOpenTask) {
					this.onOpenTaskDetailHandler(result.commentInstance);
				} else if (result.shouldEdit) {
					this.onOpenTaskEditHandler(result.id);
				} else {
					this.search(parseInt(taskRow.id, 10));
				}
			}
		}).catch(result => {
			if (result) {
				this.search(parseInt(taskRow.id, 10));
			}
		});
	}

	public onOpenTaskEditHandler(taskRow: any): void {
		let taskDetailModel: TaskDetailModel = new TaskDetailModel();
		this.taskService.getTaskDetails(taskRow.id)
			.subscribe((res) => {
				let modelHelper = new TaskEditCreateModelHelper(
					this.userContext.timezone,
					this.userContext.dateFormat,
					this.taskService,
					this.dialogService,
					this.translate);
				taskDetailModel.detail = res;
				taskDetailModel.modal = {
					title: 'Task Edit',
					type: ModalType.EDIT
				};
				let model = modelHelper.getModelForDetails(taskDetailModel);
				model.instructionLink = modelHelper.getInstructionsLink(taskDetailModel.detail);
				model.durationText = DateUtils.formatDuration(model.duration, model.durationScale);
				model.modal = taskDetailModel.modal;
				this.dialogService.extra(TaskEditCreateComponent, [
					{ provide: TaskDetailModel, useValue: clone(model) }
				], false, false)
					.then(result => {
						if (result) {
							if (result.isDeleted) {
								this.taskService.deleteTaskComment(taskRow.id).subscribe(result => {
									this.search();
								});
							} else {
								this.search(parseInt(taskRow.id, 10));
							}
						}
					}).catch(result => {
					// do nothing.
				});
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
			], DIALOG_SIZE.XXL).then(result => {
				// nothing
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
				this.refreshActionBar(id);
			});
	}

	/**
	 * On Invoke button click.
	 * @param taskRow: any
	 */
	invokeActionHandler(taskRow: any): void {
		this.taskService.invokeAction(taskRow.id)
			.subscribe(result => {
				this.refreshActionBar(taskRow.id);
			});
	}

	/**
	 * Responsible for fetching the latest data for the action bar
	 * @param taskId
	 */
	refreshActionBar(taskId: any): void {
		this.getInfoModelAndUpdateTaskActions([taskId])
			.subscribe(() => {
				// updated action bar row
			});
	}

	/**
	 * Row Collapse Event Handler. Gathers extra task info for action buttons logic.
	 * @param $event: any
	 */
	onRowDetailCollapseHandler($event: DetailCollapseEvent): void {
		this.getInfoModelAndUpdateTaskActions([$event.dataItem.id])
			.subscribe(() => {
				this.onCollapseRow($event.index);
			});
	}

	/**
	 * Row Expand Event Handler. Gathers extra task info for action buttons logic.
	 * @param $event: any
	 */
	onRowDetailExpandHandler($event: DetailExpandEvent): void {
		this.getInfoModelAndUpdateTaskActions([$event.dataItem.id])
			.subscribe(() => {
				this.onExpandRow($event.index);
			});
	}

	/**
	 * Set the flag to Collapse of the current row matched by the index
	 * @param index Index of the row to be setup
	 */
	onCollapseRow(index: number): void {
		this.rowsExpandedMap[index] = false;
	}

	/**
	 * Set the flag to Expand of the current row matched by the index
	 * @param index Index of the row to be setup
	 */
	onExpandRow(index: number): void {
		this.rowsExpandedMap[index] = true;
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
				this.refreshActionBar(taskRow.id);
			});
	}

	/**
	 * On Reset button click handler.
	 * @param taskRow: any
	 */
	onResetTaskHandler(taskRow: any): void {
		this.taskService.resetTaskAction(taskRow.id).subscribe(result => {
			this.refreshActionBar(taskRow.id);
		});
	}

	/**
	 * Checks if any filters from the dashboards are applied.
	 */
	hasDashboardFilters(): boolean {
		let hasFilters = false;
		this.dashboardFilters.forEach(filter => {
			if (this.urlParams.hasOwnProperty(filter)) {
				hasFilters = true;
			}
		});
		return hasFilters;
	}

	/**
	 * On clear filters button click, clear all available filters.
	 */
	onClearFiltersHandler(): void {
		if (this.hasDashboardFilters()) {
			this.dashboardFilters.forEach(filter => {
				delete this.urlParams[filter];
			});
			this.search();
		}
		this.columnsModel
			.filter(column => column.filterable)
			.forEach((column: GridColumnModel) => {
				column.filter = '';
			});
		if (this.grid.state.filter && this.grid.state.filter.filters.length) {
			this.grid.state.filter.filters = [];
			this.isFiltering = false;
			// reset pagination to be on page 1
			this.onPageChangeHandler({ skip: 0, take: this.pageSize });
		}
	}

	/**
	 * On bulk action button click, expand all rows that can be actionable (ready or started status)
	 */
	onBulkActionHandler(): void {
		const taskRows: Array<any> = (this.gridComponent.data as GridDataResult).data;
		let expandedEvent: DetailExpandEvent = new DetailExpandEvent({});
		if (!this.rowsExpanded) {
			let taskIds = taskRows.map(task => task.id);
			this.getInfoModelAndUpdateTaskActions(taskIds)
				.subscribe(() => {
					// take into account that grid could been filtered
					const rowCounter = pathOr(0, ['gridData', 'data', 'length'], this.grid);
					for (let i = 0; i < rowCounter; i++ ) {
						if ([TaskStatus.READY, TaskStatus.STARTED].includes(this.grid.gridData.data[i].status)) {
							this.gridComponent.expandRow(i);
							this.rowsExpandedMap[i] = true;
						}
					}
					this.rowsExpanded = true;
				});
		} else {
			taskRows.forEach((taskRow, index) => {
				index = this.grid.getRowPaginatedIndex(index);
				expandedEvent.dataItem = taskRow;
				expandedEvent.index = index;
				this.onCollapseRow(expandedEvent.index);
				this.gridComponent.collapseRow(index);
			});
			this.rowsExpanded = false;
		}
	}

	/**
	 * On cell click open the task action bar except for when clicking on the assetName
	 * @param $event: CellClickEvent
	 */
	onCellClickHandler($event: CellClickEvent): void {
		if ($event.columnIndex !== 0 && $event.columnIndex !== 1
			&& this.currentCustomColumns[$event.column.field] !== 'assetName') {
			let expandedEvent: DetailExpandEvent = new DetailExpandEvent({});
			expandedEvent.index = $event.rowIndex;
			expandedEvent.dataItem = $event.dataItem;
			// collapse
			if (this.rowsExpandedMap[$event.rowIndex]) {
				this.onCollapseRow(expandedEvent.index);
				this.gridComponent.collapseRow($event.rowIndex);
				// expand
			} else {
				this.onExpandRow(expandedEvent.index);
				this.gridComponent.expandRow($event.rowIndex);
			}
		}
	}

	/**
	 * Ge the task actions of the selected task and updates is model
	 * @param taskIds
	 */
	getInfoModelAndUpdateTaskActions(taskIds: number[]): Observable<void> {
		return this.taskService.getBulkTaskActionInfo(taskIds)
			.pipe(map((result: any) => {
				for (const taskId in result) {
					if (result.hasOwnProperty(taskId)) {
						const taskActionInfoModel = result[taskId];
						this.updateTaskActionInfoModel(taskId, taskActionInfoModel);
					}
				}
				return;
			}));
	}

	/**
	 * On Page Change, update grid state & handle pagination on server side.
	 */
	onPageChangeHandler({ skip, take }: PageChangeEvent): void {
		this.grid.state.skip = skip;
		this.grid.state.take = take;
		if (take !== this.pageSize) {
			this.userPreferenceService.setPreference(PREFERENCES_LIST.TASK_MANAGER_LIST_SIZE, this.pageSize.toString())
				.subscribe(() => {/* at this point preference should be saved */
				});
		}
		this.pageSize = take;
		this.currentPage = this.grid.getCurrentPage();
		this.search();
	}

	/**
	 * On Sort Change, update grid state and search().
	 * @param sort
	 */
	onSortChangeHandler(sort: Array<SortDescriptor>): void {
		// prevent sort on the non-sortable action column
		if (this.columnsModel
			.findIndex(item => item.property === sort[0].field && item.sortable === false) >= 0) {
			return;
		}
		this.grid.state.sort = sort;
		this.search();
	}

	/**
	 * On filter changes do the search().
	 * @param column
	 * @param clearFilter
	 */
	onFilterChangeHandler($event: string, column: GridColumnModel, clearFilter = false): void {
		column.filter = $event;
		if (clearFilter) {
			column.filter = '';
		}
		const filterColumn = { ...column };
		if (filterColumn.property.startsWith('userSelectedCol')) {
			// save the reference to the custom property name
			column.customPropertyName =  this.currentCustomColumns[filterColumn.property];
			// use the new property name
			filterColumn.property = this.currentCustomColumns[filterColumn.property];
		}
		const result = this.grid.getFilter(filterColumn);
		result.filters = result.filters.filter((filter: any) => filter.value);
		this.grid.state.filter = result;
		// reset pagination to be on page 1
		this.onPageChangeHandler({ skip: 0, take: this.pageSize });
	}

	/**
	 * Verifies the presence of the filter passed in the url,
	 * if it exists returns the url filter value, otherwise returns the user preference value
	 * @param {string} paramName  Name of the parameter received in the url
	 * @param {string} preferenceKey  Name of user preference key
	 */
	private getUrlParamOrUserPreference(paramName: string, preferenceKey: string): Observable<any> {
		return hasIn(paramName, this.urlParams) ?
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
			this.selectedEventId = this.selectedEvent && this.selectedEvent.id || 0;
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
					this.pageSize = listSize ? parseInt(listSize, 10) : GRID_DEFAULT_PAGE_SIZE;
					this.grid.state.take = this.pageSize;
					// Task View Unpublished
					this.viewUnpublished = this.hasViewUnpublishedPermission && unpublished ? (unpublished === 'true' || unpublished === '1') : false;
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
			const match = this.eventList.find(item => item.id === parseInt(currentEventId, 10));
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
		this.colapseAllExandedRows();
		const searchParams = {
			moveEvent: this.selectedEvent.id,
			justRemaining: this.justRemaining ? 1 : 0,
			justMyTasks: this.justMyTasks ? 1 : 0,
			viewUnpublished: this.viewUnpublished ? 1 : 0,
			sortOrder: applySort ? sortOrder : '',
			sortColumn: applySort ? sortColumn : '',
			page: this.currentPage,
			rows: this.pageSize
		};
		// Append url filters, in case they were not present in the default filters
		const request: any = Object.assign({}, searchParams, this.urlParams);
		// moveEvent should be string for all events
		request['moveEvent'] = request.moveEvent === 0 ? '0' : request.moveEvent;
		// Add field filters
		if (this.grid.state.filter && this.grid.state.filter.filters.length > 0) {
			this.grid.state.filter.filters.forEach((filter: FilterDescriptor) => {
				request[filter.field as string] = filter.value;
			});
		}
		this.taskService.getTaskList(request)
			.subscribe(result => {
				this.reloadGridData(result.rows, result.totalCount);
				this.loading = false;
				this.taskActionInfoModels = new Map<string, TaskActionInfoModel>();
				for (let i = 0; i < result.totalCount; i++) {
					let info = result.rows[i] ? result.rows[i].actionBarInfo : null;
					if (info) {
						let actionBarModel = this.taskService.convertToTaskActionInfoModel(info);
						this.taskActionInfoModels.set(result.rows[i].id.toString(), actionBarModel);
					}
				}
			});
		this.loaderService.stopProgress();

		setTimeout(() => {
			if (this.loading) {
				this.loaderService.initProgress();
				this.loaderService.toggle();
			}
		}, LOADER_IDLE_PERIOD * 10);
	}

	/**
	 * Used to colapse all of the rows that were expanded and reset the state used by the action bar
	 */
	private colapseAllExandedRows(): void {
		let expandedEvent: DetailExpandEvent = new DetailExpandEvent({});
		for (let rowIndex in this.rowsExpandedMap) {
			if (rowIndex) {
				let rowNum = parseInt(rowIndex, 10);
				expandedEvent.index = rowNum;
				this.onCollapseRow(expandedEvent.index);
				this.gridComponent.collapseRow(rowNum);
			}
		}
		this.rowsExpandedMap = {};
		// Clear out any expanded rows
		this.taskActionInfoModels.clear();
		this.rowsExpanded = false;
	}

	/**
	 * Reloads data for current grid.
	 * @param result
	 */
	private reloadGridData(result: any, totalCount: number): void {
		this.grid.resultSet = result;
		this.grid.gridData = { data: result, total: totalCount };
		this.grid.notifyUpdateGridHeight();
	}

	/**
	 * Returns the task action info model.
	 */
	private getTaskActionInfoModel(taskId: string): TaskActionInfoModel {
		if (!isNaN(taskId as any)) {
			taskId = taskId.toString();
		}
		return this.taskActionInfoModels.get(taskId);
	}

	/**
	 * Loads Task action information model into the Models Map.
	 */
	private loadTaskActionInfoModel(taskId: string, forceReload = false): Observable<TaskActionInfoModel> {
		if (!isNaN(taskId as any)) {
			taskId = taskId.toString();
		}
		return new Observable(observer => {
			this.taskService.getTaskActionInfo(parseInt(taskId, 10))
				.subscribe((result: TaskActionInfoModel) => {
					const taskActionInfoModel = result;
					this.updateTaskActionInfoModel(taskId, taskActionInfoModel);
					observer.next(result);
					observer.complete();
				});
		});
	}

	private updateTaskActionInfoModel(taskId, taskActionInfoModel): void {
		this.taskActionInfoModels.set(taskId, taskActionInfoModel);

		// Update the grid row with new information from the endpoint (status, assignTo, etc)
		for (let i = 0; i < this.grid.gridData.data.length; i++) {
			if (this.grid.gridData.data[i].id === taskId ) {
				this.grid.gridData.data[i].status = taskActionInfoModel.status;
				this.grid.gridData.data[i].taskStatus = 'task_' + taskActionInfoModel.status.toLowerCase();
				this.grid.gridData.data[i].assignedTo = taskActionInfoModel.assignedTo;
				this.populateAssignedToName(taskActionInfoModel.assignedToName, this.grid.gridData.data[i]);
				break;
			}
		}
	}

	/**
	 * Used to push an assignedTo name into the datagrid row if the assignedTo property was selected as one of the
	 * custom columns.
	 * @param personName
	 * @param row
	 */
	private populateAssignedToName(personName: string, row: any): void {
		if (personName) {
			for (let columnName in this.currentCustomColumns) {
				if (this.currentCustomColumns[columnName] === 'assignedTo') {
					row[columnName] = personName;
				}
			}
		}
	}

	/**
	 * Toggle the flag to show/hide grid filters
	 */
	public toggleFiltering(): void {
		this.isFiltering = !this.isFiltering;
	}

	/**
	 * Get the current number of filters selected
	 */
	public filterCounter(): number {
		return GridColumnModel.getFilterCounter(this.grid.state);
	}

	/**
	 * pageChangeTaskGrid
	 */
	public pageChangeTaskGrid(event: PageChangeEvent): void {
		// this.tasksPage = event;
		// this.loadTasksGrid();
	}

	/**
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.isFiltering = false;
		this.grid.clearAllFilters(this.columnsModel);
		this.onFiltersChange();
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.filterCounter() === 0 && this.hasDashboardFilters() === false;
	}
}

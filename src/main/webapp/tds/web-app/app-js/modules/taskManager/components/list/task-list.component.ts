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
	GridDataResult
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

@Component({
	selector: 'task-list',
	template: `
		<div class="content body tds-kendo-grid">
			<section>
				<div class="box-body box-with-empty-header">
					<div class="row top-filters">
						<div class="col-sm-7">
							<label class="control-label" for="fetch">Event</label>
							<kendo-dropdownlist
								style="width: 200px; padding-right: 20px"
								name="eventList"
								class="form-control event-dropdown"
								[data]="eventList"
								[textField]="'name'"
								[valueField]="'id'"
								[(ngModel)]="selectedEvent"
								(valueChange)="onEventSelect()">
							</kendo-dropdownlist>
							<label for="one">
								<input
									type="checkbox"
									name="one"
									id="one"
									[(ngModel)]="justRemaining"
									(ngModelChange)="onFiltersChange()">
								Just Remaining
							</label>
							<label for="two">
								<input
									type="checkbox"
									name="two"
									id="two"
									[(ngModel)]="justMyTasks"
									(ngModelChange)="onFiltersChange()">
								Just Mine
							</label>
							<label for="three">
								<input
									type="checkbox"
									name="three" id="three"
									[(ngModel)]="viewUnpublished"
									(ngModelChange)="onFiltersChange()">
								View Unpublished
							</label>
						</div>
						<div class="col-sm-5 text-right">
							<tds-button-custom class="btn-primary"
																 (click)="onViewTaskGraphHandler()"
																 title="View Task Graph"
																 tooltip="View Task Graph"
																 icon="sitemap">
							</tds-button-custom>
							<tds-button-custom class="btn-primary"
																 (click)="onViewTimelineHandler()"
																 title="View Timeline"
																 tooltip="View Timeline"
																 icon="table">
							</tds-button-custom>
							<div class="refresh-control">
								<tds-pie-countdown
									[refreshPreference]="TASK_MANAGER_REFRESH_TIMER"
									(timeout)="search()"
									[hideRefresh]="true"
									[customOptions]="[{seconds: 0, description: 'Manual'},
																		{seconds: 60, description: '1 Min'},
																		{seconds: 120, description: '2 Min'},
																		{seconds: 180, description: '3 Min'},
																		{seconds: 240, description: '4 Min'},
																		{seconds: 300, description: '5 Min'}]">
								</tds-pie-countdown>
							</div>
						</div>
					</div>
					<kendo-grid
						#gridComponent
						*ngIf="!hideGrid"
						class="task-grid"
						[data]="grid.gridData"
						[pageSize]="grid.state.take"
						[skip]="grid.state.skip"
						[pageable]="{pageSizes: grid.defaultPageOptions, info: true}"
						(pageChange)="grid.pageChange($event)"
						[filter]="grid.state.filter"
						[filterable]="true"
						(filterChange)="grid.filterChange($event)"
						[resizable]="true"
						[columnMenu]="true"
						[sort]="grid.state.sort"
						[sortable]="{mode:'single'}"
						(detailExpand)="onRowDetailExpandHandler($event)"
						(detailCollapse)="onRowDetailCollapseHandler($event)"
						(sortChange)="grid.sortChange($event)"
						(cellClick)="onCellClickHandler($event)">
						<!-- Column Menu -->
						<ng-template kendoGridColumnMenuTemplate let-service="service" let-column="column">
							<div class="k-column-list">
								<label *ngFor="let custom of allAvailableCustomColumns;"
											 [ngClass]="{'invisible': custom.property === currentCustomColumns[column.field]}"
											 class="k-column-list-item ng-star-inserted">
									<input type="radio"
												 value="{{custom.property}}"
												 (ngModelChange)="onCustomColumnChange(column.field, custom, service)"
												 [(ngModel)]="selectedCustomColumn[column.field]">
									<span class="k-checkbox-label"> {{custom.label}} </span>
								</label>
							</div>
						</ng-template>
						<!-- Toolbar -->
						<ng-template kendoGridToolbarTemplate [position]="'top'">
							<div class="button-toolbar">
								<tds-button-create
									(click)="onCreateTaskHandler()"
									[title]="'Create Task'">
								</tds-button-create>
								<tds-button-edit
									(click)="onBulkActionHandler()"
									[title]="'Bulk Action'">
								</tds-button-edit>
								<tds-button-custom
									icon="times"
									[disabled]="!areFiltersDirty()"
									(click)="onClearFiltersHandler()"
									[title]="'Clear Filters'">
								</tds-button-custom>
								<tds-button-custom
									icon="refresh"
									title="Refresh"
									isIconButton="true"
									class="component-action-reload pull-right"
									(click)="onFiltersChange()">
								</tds-button-custom>
							</div>
						</ng-template>
						<!-- Row Detail Template -->
						<div *kendoGridDetailTemplate="let dataItem, let rowIndex = rowIndex" class="task-action-buttons-wrapper">
							<div class="task-action-buttons">
								<tds-task-actions
									*ngIf="dataItem.id && dataItem.status"
									[task]="dataItem"
									[showDelayActions]="true"
									[showDetails]="true"
									[buttonClass]="'btn-primary'"
									(start)="updateTaskStatus(dataItem.id, TaskStatus.STARTED)"
									(done)="updateTaskStatus(dataItem.id, TaskStatus.COMPLETED)"
									(invoke)="invokeActionHandler(dataItem)"
									(reset)="onResetTaskHandler(dataItem)"
									(assignToMe)="onAssignToMeHandler(dataItem)"
									(neighborhood)="onViewTaskNeighborHandler(dataItem)"
									(delay)="changeTimeEst(dataItem, $event)"
									(details)="onOpenTaskDetailHandler(dataItem)">
								</tds-task-actions>
							</div>
							<button *ngIf="dataItem.parsedInstructions"
											class="btn btn-primary btn-xs"
											(click)="openLinkInNewTab(dataItem.parsedInstructions[1])">
								{{dataItem.parsedInstructions[0]}}
							</button>
						</div>
						<kendo-grid-column *ngFor="let column of columnsModel"
															 field="{{column.property}}"
															 [locked]="column.locked"
															 format="{{column.format}}"
															 [headerClass]="column.headerClass ? column.headerClass : ''"
															 [headerStyle]="column.headerStyle ? column.headerStyle : ''"
															 [class]="column.cellClass ? column.cellClass : ''"
															 [style]="column.cellStyle ? column.cellStyle : ''"
															 [width]="!column.width ? 100 : column.width"
															 [filterable]=""
															 [columnMenu]="column.columnMenu">
							<!-- Header -->
							<ng-template kendoGridHeaderTemplate>
								<div class="sortable-column">
									<label> {{column.label}}</label>
								</div>
							</ng-template>
							<!-- task Number -->
							<ng-template kendoGridCellTemplate *ngIf="column.property === 'taskNumber'" let-dataItem>
								<span class="is-grid-link" (click)="onOpenTaskDetailHandler(dataItem)">{{dataItem.taskNumber}}</span>
							</ng-template>
							<!-- asset name -->
							<ng-template kendoGridCellTemplate *ngIf="currentCustomColumns[column.property] === 'assetName'"
													 let-dataItem>
								<span class="is-grid-link"
											(click)="onOpenAssetDetailHandler(dataItem)">{{dataItem[column.property]}}</span>
							</ng-template>
							<!-- updated -->
							<ng-template kendoGridCellTemplate *ngIf="column.property === 'updatedTime'" let-dataItem>
								<span class="task-updated-cell {{dataItem.updatedClass}}">{{dataItem.updatedTime}}</span>
							</ng-template>
							<!-- status -->
							<ng-template kendoGridCellTemplate *ngIf="column.property === 'status'" let-dataItem>
								<span class="task-status-cell {{dataItem.taskStatus}}">{{dataItem.status}}</span>
							</ng-template>
							<ng-template kendoGridFilterCellTemplate let-filter>
								<div class="has-feedback" *ngIf="column.filterable" style="margin-bottom: 5px;">
									<div *ngIf="column.type === 'text'; then stringFilter"></div>
									<ng-template #stringFilter>
										<input [(ngModel)]="column.filter" (keyup)="grid.onFilter(column)"
													 type="text" class="form-control" name="{{column.property}}" placeholder="Filter" value="">
										<span *ngIf="column.filter" (click)="grid.clearValue(column)"
													style="cursor:pointer;color:#656565;pointer-events:all"
													class="fa fa-times form-control-feedback"></span>
									</ng-template>
								</div>
							</ng-template>
						</kendo-grid-column>
					</kendo-grid>
				</div>
			</section>
		</div>
	`
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

	constructor(
		private taskService: TaskService,
		private reportService: ReportsService,
		private userPreferenceService: PreferenceService,
		private store: Store,
		private dialogService: UIDialogService,
		private userContextService: UserContextService,
		private translate: TranslatePipe,
		private activatedRoute: ActivatedRoute) {
		this.justMyTasks = false;
		this.loading = true;
		this.hideGrid = true;
		this.rowsExpanded = false;
		this.grid = new DataGridOperationsHelper([]);
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
					// Current event
					this.loadEventListAndSearch(currentEventId);
					// Task list size
					this.pageSize = listSize ? parseInt(listSize, 0) : GRID_DEFAULT_PAGE_SIZE;
					// Task View Unpublished
					this.viewUnpublished = unpublished ? (unpublished === 'true' || unpublished === '1') : false;
					// Just Remaining
					this.justRemaining = justRemaining ? justRemaining === '1' : false;

					// params were transferred to local properties,
					// we can remove them from the parameters object
					// and leave only the parameters which are not handled by local properties
					this.urlParams = ObjectUtils.excludeProperties(this.urlParams, ['moveEvent', 'justRemaining', 'viewUnpublished']);
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

		// Set the default filter values
		const defaultFilters = {
			moveEvent: this.selectedEvent.id,
			justRemaining: this.justRemaining ? 1 : 0,
			justMyTasks: this.justMyTasks ? 1 : 0,
			viewUnpublished: this.viewUnpublished ? 1 : 0,
			sord: 'asc',
		};

		// Append url filters, in case they were not present in the default filters
		const filters: any = Object.assign({}, defaultFilters, this.urlParams);
		// moveEvent should be string for all events
		filters['moveEvent'] = filters.moveEvent === 0 ? '0' : filters.moveEvent;

		this.taskService.getTaskList(filters)
			.subscribe(result => {
				this.grid = new DataGridOperationsHelper(result.rows, null, null, null, this.pageSize);
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
}

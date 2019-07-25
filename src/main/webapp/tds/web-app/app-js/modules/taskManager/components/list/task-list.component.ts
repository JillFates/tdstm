import { Component, ViewChild } from '@angular/core';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';
import { GridComponent, GridDataResult } from '@progress/kendo-angular-grid';
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
import { TaskEditComponent } from '../edit/task-edit.component';
import { TaskEditCreateModelHelper } from '../common/task-edit-create-model.helper';
import { DateUtils } from '../../../../shared/utils/date.utils';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { clone } from 'ramda';
import { AssetShowComponent } from '../../../assetExplorer/components/asset/asset-show.component';
import { AssetExplorerModule } from '../../../assetExplorer/asset-explorer.module';
import { TaskCreateComponent } from '../create/task-create.component';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { UserContextService } from '../../../auth/service/user-context.service';

@Component({
	selector: 'task-list',
	template: `
		<div class="content body tds-kendo-grid without-selectabe-rows">
			<section>
				<div class="box-body box-with-empty-header">
					<div class="row top-filters">
						<div class="col-sm-6">
							<label class="control-label" for="fetch">Event</label>
							<kendo-dropdownlist
								style="width: 200px; padding-right: 20px"
								name="eventList"
								class="form-control"
								[data]="eventList"
								[textField]="'name'"
								[valueField]="'id'"
								[(ngModel)]="selectedEvent"
								(valueChange)="onFiltersChange()">
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
						<div class="col-sm-6 text-right">
							<tds-button-custom class="btn-primary"
																 (click)="onViewTaskGraphHandler()"
																 title="View Task Graph"
																 tooltip="View Task Graph"
																 icon="table">
							</tds-button-custom>
							<tds-button-custom class="btn-primary"
																 (click)="onGenerateReport()"
																 title="View Timeline"
																 tooltip="View Timeline"
																 icon="table">
							</tds-button-custom>
							<!--<kendo-dropdownlist-->
							<!--style="width: 100px"-->
							<!--name="timerList"-->
							<!--class="form-control"-->
							<!--[data]="timerList"-->
							<!--[(ngModel)]="timerValue">-->
							<!--</kendo-dropdownlist>-->
						</div>
					</div>
					<kendo-grid
						#gridComponent
						*ngIf="!hideGrid"
						class="task-grid"
						[data]="grid.gridData"
						[loading]="loading"
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
						(sortChange)="grid.sortChange($event)">
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
							<tds-button-create
								(click)="onCreateTaskHandler()"
								[title]="'Create Task'">
							</tds-button-create>
							<tds-button-edit
								(click)="onBulkActionHandler()"
								[title]="'Bulk Action'">
							</tds-button-edit>
							<tds-button-cancel
								[disabled]="!isFiltersDirty()"
								(click)="onClearFiltersHandler()"
								[title]="'Clear Filters'">
							</tds-button-cancel>
							<label class="reload-grid-button pull-right" title="Reload Batch List" (click)="onFiltersChange()">
								<span class="glyphicon glyphicon-repeat"></span>
							</label>
						</ng-template>
						<!-- Row Detail Template -->
						<div *kendoGridDetailTemplate="let dataItem, let rowIndex = rowIndex" class="task-action-buttons-wrapper">
							<div class="task-action-buttons">
								<button
									*ngIf="dataItem.status!='Started' && dataItem.status!='Completed'"
									class="btn btn-primary btn-xs"
									(click)="updateTaskStatus(dataItem.id,'Started')">
									Start
								</button>
								<button
									*ngIf="dataItem.status!='Completed'"
									class="btn btn-primary btn-xs"
									(click)="updateTaskStatus(dataItem.id,'Completed')">
									Done
								</button>
								<button class="btn btn-primary btn-xs"
												*ngIf="dataItem.apiActionId && dataItem.status === 'Hold'"
												(click)="onResetTaskHandler(dataItem)">
									Reset Action
								</button>
								<button
									class="btn btn-primary btn-xs"
									(click)="onOpenTaskDetailHandler(dataItem)">
									Details...
								</button>
							</div>
							<div *ngIf="dataItem.status ==='Ready'" class="task-action-buttons">
								<span style="margin-right:16px">Delay for:</span>
								<button
									class="btn btn-primary btn-xs"
									(click)="changeTimeEst(dataItem.id,1)">
									1 day
								</button>
								<button
									class="btn btn-primary btn-xs"
									(click)="changeTimeEst(dataItem.id,2)">
									2 day
								</button>
								<button
									class="btn btn-primary btn-xs"
									(click)="changeTimeEst(dataItem.id,7)">
									7 day
								</button>
							</div>
							<button *ngIf="dataItem.loadedActions && userContext.person.id !== dataItem.assignedTo
							&& (dataItem.status === 'Pending' || dataItem.status === 'Ready' || dataItem.status === 'Started')"
											(click)="onAssignToMeHandler(dataItem)"
											class="btn btn-primary btn-xs">
								Assign To Me
							</button>
							<button *ngIf="dataItem.successors > 0 || dataItem.predecessors > 0"
											(click)="onViewTaskNeighborHandler(dataItem)"
											class="btn btn-primary btn-xs">
								Neighborhood
							</button>
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
								<span class="task-status-cell {{dataItem.updatedClass}}">{{dataItem.updatedTime}}</span>
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
	private readonly allEventsOption = { id: 0, name: 'All Events' };
	selectedEvent = this.allEventsOption;
	justRemaining: boolean;
	justMyTasks = false;
	viewUnpublished: boolean;
	eventList: any;
	timerList = ['Manual', '1 Min', '2 Min', '3 Min', '4 Min', '5 Min'];
	timerValue = 'Manual';
	grid: DataGridOperationsHelper = new DataGridOperationsHelper([]);
	columnsModel: Array<GridColumnModel> = taskListColumnsModel;
	loading = true;
	hideGrid = true;
	private pageSize: number;
	private currentCustomColumns: any = {};
	private allAvailableCustomColumns: Array<any>;
	selectedCustomColumn = {};
	private userContext: UserContextModel;
	private rowsExpanded = false;

	constructor(
		private taskService: TaskService,
		private reportService: ReportsService,
		private userPreferenceService: PreferenceService,
		private dialogService: UIDialogService,
		private userContextService: UserContextService,
		private translate: TranslatePipe) {
		this.onLoad();
	}

	/**
	 * Load all the user preferences to populate the task manager grid.
	 */
	private onLoad(): void {
		this.loading = true;
		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
		const observables = forkJoin(
			this.taskService.getCustomColumns(),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.CURRENT_EVENT_ID),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.TASK_MANAGER_LIST_SIZE),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.VIEW_UNPUBLISHED),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.JUST_REMAINING)
		);
		observables.subscribe({
				next: value => {
					// Custom Columns, TaskPref value[0]
					this.builCustomColumns(value[0].customColumns, value[0].assetCommentFields);
					// Current event, value[1]
					this.loadEventListAndSearch(value[1]);
					// Task list size, value[2]
					this.pageSize = value[2] ? parseInt(value[2], 0) : GRID_DEFAULT_PAGE_SIZE;
					// Task Refresh Timer, value[3]
					// Task View Unpublished, value[4]
					this.viewUnpublished = value[4] ? value[4] === 'true' : false;
					// Just Remaining, value [5]
					this.justRemaining = value[5] ? value[5] === '1' : false;
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
	private builCustomColumns(customColumns: any, assetCommentFields: any): void {
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
		console.log(this.currentCustomColumns);
	}

	/**
	 * Search Tasks based on UI filters.
	 */
	private search(taskId ?: number): void {
		this.loading = true;
		this.taskService.getTaskList(this.selectedEvent.id, this.justRemaining, this.justMyTasks, this.viewUnpublished).subscribe(result => {
			this.grid = new DataGridOperationsHelper(result.rows, null, null, null, this.pageSize);
			console.log(this.grid.gridData);
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
				this.builCustomColumns(result.customColumns, result.assetCommentFields);
				this.search();
			});
	}

	/**
	 * On Event select change.
	 * @param selection: Array<any>
	 */
	onFiltersChange($event ?: any) {
		this.search();
	}

	/**
	 * Search for a task on the current grid data and expands it. The rest of the rows gets collapsed.
	 * @param taskId: number
	 */
	searchTaskAndExpandRow(taskId: number) {
		(this.gridComponent.data as GridDataResult).data.forEach((item, index) => {
			if (item.id === taskId) {
				this.gridComponent.expandRow(index);
			} else {
				this.gridComponent.collapseRow(index);
			}
		});
	}

	/**
	 * On Edit Task Button Handler open the task edit modal.
	 * @param taskRow: any
	 */
	onEditTaskHandler(taskRow: any): void {
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
				this.dialogService.extra(TaskEditComponent, [
					{ provide: TaskDetailModel, useValue: clone(model) }
				], false, false)
					.then(result => {
						if (result) {
							if (result.isDeleted) {
								this.search()
							} else {
								this.search(taskRow.id)
							}
						}
					}).catch(result => {
					console.log(result);
				});
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
				console.log(result);
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
			},
			detail: {
				currentUserId: this.userContext.user.id
			}
		};
		this.dialogService.extra(TaskDetailComponent, [
			{ provide: TaskDetailModel, useValue: taskDetailModel }
		]).then(() => {
			// do nothing.
		}).catch(result => {
			if (result) {
				this.search();
			}
		});
	}

	/**
	 * Changes the time estimation of a task.
	 * @param id: string
	 * @param days: string
	 */
	changeTimeEst(id: string, days: string) {
		this.taskService.changeTimeEst(id, days)
			.subscribe(() => {
				this.search(parseInt(id, 0));
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
	onViewTaskNeighborHandler(dataItem): void {
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
	openLinkInNewTab(url): void {
		window.open(url, '_blank');
	}

	/**
	 * Show the asset popup detail.
	 * @param assetId: number
	 * @param assetClass: string
	 */
	onOpenAssetDetailHandler(taskRow: any) {
		console.log(taskRow);
		this.dialogService.open(AssetShowComponent,
			[UIDialogService,
				{ provide: 'ID', useValue: taskRow.assetEntityId },
				{ provide: 'ASSET', useValue: taskRow.assetEntityAssetClass },
				{ provide: 'AssetExplorerModule', useValue: AssetExplorerModule }
			], DIALOG_SIZE.LG).then(result => {
			console.log('success: ' + result);
		}).catch(result => {
			console.log('rejected: ' + result);
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
	 * Row Expand Event Handler. Gathers extra task info for action buttons logic.
	 * @param $event: any
	 */
	onRowDetailExpandHandler($event: any): void {
		const taskRow = $event.dataItem;
		if (!taskRow.loadedActions) {
			this.taskService.getTaskActionInfo(taskRow.id).subscribe(result => {
				taskRow.loadedActions = true;
				taskRow.predecessors = result.predecessorsCount;
				taskRow.sucessors = result.successorsCount;
				taskRow.assignedTo = result.assignedTo;
				taskRow.apiActionId = result.apiActionId;
			});
		}
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
				console.log(result);
				taskRow.assignedTo = this.userContext.person.id;
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
	isFiltersDirty(): boolean {
		return this.columnsModel
			.filter(column => column.filter).length > 0;
	}

	/**
	 * On bulk action button click, expand all rows that can be actionable (ready or started status)
	 */
	onBulkActionHandler(): void {
		const taskRows: Array<any> = (this.gridComponent.data as GridDataResult).data;
		if (!this.rowsExpanded) {
			taskRows.forEach((taskRow: any, index: number) => {
				const $event = { dataItem: taskRow };
				if (taskRow.status === 'Ready' || taskRow.status === 'Started') {
					this.onRowDetailExpandHandler($event);
					this.gridComponent.expandRow(index);
				}
			});
			this.rowsExpanded = true;
		} else {
			taskRows.forEach((taskRow, index) => {
				this.gridComponent.collapseRow(index);
			});
			this.rowsExpanded = false;
		}
	}
}

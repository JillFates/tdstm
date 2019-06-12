import {Component, OnInit, ViewChild} from '@angular/core';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {GridComponent} from '@progress/kendo-angular-grid';
import {ReportsService} from '../../../reports/service/reports.service';
import {TaskService} from '../../service/task.service';
import {GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {forkJoin} from 'rxjs';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import { ColumnMenuService } from '@progress/kendo-angular-grid/dist/es2015/column-menu/column-menu.service';
import { SortUtils } from '../../../../shared/utils/sort.utils';
import { taskListColumnsModel } from '../../model/task-list-columns.model';

@Component({
	selector: 'task-list',
	template: `
		<div class="content body tds-kendo-grid">
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
																 (click)="onGenerateReport()"
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
							<kendo-dropdownlist
								style="width: 100px"
								name="timerList"
								class="form-control"
								[data]="timerList"
								[(ngModel)]="timerValue">
							</kendo-dropdownlist>
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
						(sortChange)="grid.sortChange($event)">
						<!-- Column Menu -->
						<ng-template kendoGridColumnMenuTemplate let-service="service" let-column="column">
							<div class="k-column-list">
								<label *ngFor="let custom of customColumns;"
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
								(click)="confirmDelete()"
								[title]="'Create Task'">
							</tds-button-create>
							<tds-button-edit
								(click)="confirmDelete()"
								[title]="'Bulk Edit'">
							</tds-button-edit>
							<tds-button-cancel
								(click)="confirmDelete()"
								[title]="'Clear Filters'">
							</tds-button-cancel>
							<label class="reload-grid-button pull-right" title="Reload Batch List" (click)="onFiltersChange()">
								<span class="glyphicon glyphicon-repeat"></span>
							</label>
						</ng-template>
						<!-- Action -->
						<kendo-grid-command-column [width]="50" [locked]="true" [columnMenu]="false">
							<ng-template kendoGridHeaderTemplate>
								<div class="text-center">
									<label class="action-header"> {{ 'GLOBAL.ACTION' | translate }} </label>
								</div>
							</ng-template>
							<ng-template kendoGridCellTemplate
													 let-dataItem
													 let-rowIndex="rowIndex">
								<div class="tds-action-button-set">
									<tds-button-edit (click)="onEdit(dataItem)"></tds-button-edit>
								</div>
							</ng-template>
						</kendo-grid-command-column>
						<!-- Columns -->
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
							<!-- updated -->
							<ng-template kendoGridCellTemplate *ngIf="column.property === 'updatedTime'" let-dataItem>
								<span class="task-status-cell {{dataItem.updatedClass}}">{{dataItem.updatedTime}}</span>
							</ng-template>
							<!-- status -->
							<ng-template kendoGridCellTemplate *ngIf="column.property === 'status'" let-dataItem>
								<span class="task-status-cell {{dataItem.taskStatus}}">{{dataItem.status}}</span>
							</ng-template>
							<ng-template kendoGridFilterCellTemplate let-filter>
								<div class="has-feedback" *ngIf="column.filterable" style="margin-bottom:0px;">
									<div *ngIf="column.type === 'text'; then stringFilter"></div>
									<ng-template #stringFilter>
										<input [(ngModel)]="column.filter" (keyup)="grid.onFilter(column)"
													 type="text" class="form-control" name="{{column.property}}" placeholder="Filter" value="">
										<span *ngIf="column.filter" (click)="grid.clearValue(column)" style="cursor:pointer;color:#656565;pointer-events:all" class="fa fa-times form-control-feedback"></span>
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
	private readonly allEventsOption = {id: 0, name: 'All Events'};
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
	private customColumns: Array<any>;
	selectedCustomColumn = {};

	constructor(
		private taskService: TaskService,
		private reportService: ReportsService,
		private userPreferenceService: PreferenceService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.loading = true;
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
					this.builCustomColumns(value[0].customColumns	, value[0].assetCommentFields);
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
			const column = `userSelectedCol${index}`;
			let match = this.columnsModel.find(item => item.property === column);
			match.label = assetCommentFields[custom];
			this.currentCustomColumns[column] = custom;
		});
		this.customColumns = [];
		Object.entries(assetCommentFields).forEach(entry => {
			this.customColumns.push({property: entry[0], label: entry[1]});
		});
		this.customColumns = this.customColumns.sort((a, b) => SortUtils.compareByProperty(a, b, 'label'));
	}

	/**
	 * Search Tasks based on UI filters.
	 */
	private search(): void {
		this.loading = true;
		this.taskService.getTaskList(this.selectedEvent.id, this.justRemaining, this.justMyTasks, this.viewUnpublished).subscribe( result => {
			this.grid = new DataGridOperationsHelper(result.rows, null, null, null, this.pageSize);
			console.log(this.grid.gridData);
			this.loading = false;
		});
	}F

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
}

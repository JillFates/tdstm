import {Component, OnInit, ViewChild} from '@angular/core';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {GridComponent} from '@progress/kendo-angular-grid';
import {ReportsService} from '../../../reports/service/reports.service';
import {TaskService} from '../../service/task.service';
import {GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {forkJoin} from 'rxjs';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';

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
									name="c"
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
						class="task-grid"
						[data]="grid.gridData"
						[loading]="loading"
						[pageSize]="grid.state.take"
						[skip]="grid.state.skip"
						[pageable]="{pageSizes: grid.defaultPageOptions, info: true}"
						(pageChange)="grid.pageChange($event)">
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
						<kendo-grid-command-column [width]="70" [locked]="true">
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
															 [width]="!column.width ? 100 : column.width">
							<ng-template kendoGridHeaderTemplate>
								<div class="sortable-column">
									<label> {{column.label}}</label>
								</div>
							</ng-template>
						</kendo-grid-column>
					</kendo-grid>
				</div>
			</section>
		</div>
	`
})

export class TaskListComponent implements OnInit {

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
	columnsModel: Array<GridColumnModel> = columnsModel;
	loading = true;
	private pageSize: number;

	constructor(
		private taskService: TaskService,
		private reportService: ReportsService,
		private userPreferenceService: PreferenceService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.loading = true;
		const observables = forkJoin(
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.CURRENT_EVENT_ID),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.TASK_MANAGER_LIST_SIZE),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.VIEW_UNPUBLISHED),
			this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.JUST_REMAINING)
		);
		observables.subscribe({
				next: value => {
					console.log(value);
					// Task list size, value[1]
					this.pageSize = value[1] ? parseInt(value[1], 0) : GRID_DEFAULT_PAGE_SIZE;
					// Task Refresh Timer, value[2]
					// Task View Unpublished, value[3]
					this.viewUnpublished = value[3] ? value[3] === 'true' : false;
					// Just Remaining, value [4]
					this.justRemaining = value[4] ? value[4] === '1' : false;
					// Current event, value[0]
					this.reportService.getEventList().subscribe(result => {
						this.eventList = [this.allEventsOption].concat(result.data);
						const match = this.eventList.find(item => item.id === parseInt(value[0], 0));
						if (match) {
							this.selectedEvent = match;
						} else {
							this.selectedEvent = this.allEventsOption;
						}
						this.search();
					});
				},
				complete: () => {
					// nothing.
				}
			}
		);
	}

	private search(): void {
		this.loading = true;
		this.taskService.getTaskList(this.selectedEvent.id, this.justRemaining, this.justMyTasks, this.viewUnpublished).subscribe( result => {
			this.grid = new DataGridOperationsHelper(result.rows, null, null, null, this.pageSize);
			console.log(this.grid.gridData);
			this.loading = false;
		});
	}

	ngOnInit(): void {
		// setTimeout(() => {
		// 	this.gridComponent.autoFitColumns();
		// }, 300);
	}

	/**
	 * On Event select change.
	 * @param selection: Array<any>
	 */
	onFiltersChange($event ?: any) {
		console.log(this.selectedEvent);
		console.log('justRemaining', this.justRemaining);
		console.log('justMyTasks', this.justMyTasks);
		console.log('viewUnpublished', this.viewUnpublished);
		this.search();
	}
}

const columnsModel: Array<GridColumnModel> = [
	{
		label: 'Task',
		property: 'taskNumber',
		type: 'number',
		width: 70,
		locked: false
	},
	{
		label: 'Description',
		property: 'comment',
		type: 'text',
		width: 250,
		locked: false
	},
	{
		label: 'Asset Name',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Asset Type',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Updated',
		property: 'updatedTime',
		type: 'text',
		width: 80,
		locked: false
	},
	{
		label: 'Due Date',
		property: 'dueDate',
		type: 'text',
		width: 80,
		locked: false
	},
	{
		label: 'Status',
		property: 'status',
		type: 'text',
		width: 80,
		locked: false
	},
	{
		label: 'Assigned To',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Team',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Category',
		property: 'assetName',
		type: 'text',
		width: 80,
		locked: false
	},
	{
		label: 'Suc.',
		property: 'assetName',
		type: 'text',
		width: 50,
		locked: false
	},
	{
		label: 'Score',
		property: 'score',
		type: 'number',
		width: 80,
		locked: false
	},
]

import {Component, OnInit, ViewChild} from '@angular/core';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {GridComponent} from '@progress/kendo-angular-grid';
import {ReportsService} from '../../../reports/service/reports.service';

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
								style="width: 300px; padding-right: 20px"
								name="eventList"
								class="form-control"
								[data]="eventList"
								[textField]="'name'"
								[valueField]="'id'"
								[(ngModel)]="selectedEvent">
							</kendo-dropdownlist>
							<label for="one">
								<input type="checkbox" name="one" id="one" [(ngModel)]="bundleConflict">
								Just Remaining
							</label>
							<label for="two">
								<input type="checkbox" name="c" id="two" [(ngModel)]="bundleConflict">
								Just Mine
							</label>
							<label for="three">
								<input type="checkbox" name="three" id="three" [(ngModel)]="bundleConflict">
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
						*ngIf="grid"
						#grid
						class="task-grid"
						[data]="grid.gridData">
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
															 [width]="!column.width ? COLUMN_MIN_WIDTH : column.width">
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

	@ViewChild('grid') gridComponent: GridComponent;
	private readonly allEventsOption = {id: -1, name: 'All Events'};
	selectedEvent = this.allEventsOption;
	eventList: any;
	timerList = ['Manual', '1 Min', '2 Min', '3 Min', '4 Min', '5 Min'];
	timerValue = 'Manual';
	grid: DataGridOperationsHelper;
	columnsModel: Array<GridColumnModel> = columnsModel;

	constructor(private reportService: ReportsService) {
		this.onLoad();
		this.grid = new DataGridOperationsHelper([]);
	}

	private onLoad(): void {
		this.reportService.getEventList().subscribe( result => {
			this.eventList = [this.allEventsOption].concat(result.data);
		})
	}

	ngOnInit(): void {
		// setTimeout(() => {
		// 	this.gridComponent.autoFitColumns();
		// }, 300);
	}
}

const columnsModel: Array<GridColumnModel> = [
	{
		label: 'Task',
		property: 'name',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Description',
		property: 'description',
		type: 'text',
		width: 180,
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
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Due Date',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Status',
		property: 'assetName',
		type: 'text',
		width: 130,
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
		width: 130,
		locked: false
	},
	{
		label: 'Suc.',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
	{
		label: 'Score',
		property: 'assetName',
		type: 'text',
		width: 130,
		locked: false
	},
]

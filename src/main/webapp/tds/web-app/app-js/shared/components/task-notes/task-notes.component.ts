import {Component, Input, Output, EventEmitter,  OnInit } from '@angular/core';
import {DataGridOperationsHelper} from '../../utils/data-grid-operations.helper';
import {TaskNotesColumnsModel} from './model/task-notes-columns.model';
import {Permission} from '../../model/permission.model';
import {PermissionService} from '../../services/permission.service';
import { COLUMN_MIN_WIDTH } from 'tds-component-library';

@Component({
	selector: `tds-task-notes`,
	template: `
        <div>
            <kendo-grid *ngIf="dataGridTaskNotesHelper"
                        class="successor-predecessor-table has-double-assignment"
                        [data]="dataGridTaskNotesHelper.gridData"
                        [sort]="dataGridTaskNotesHelper.state.sort"
                        [sortable]="{mode:'single'}"
                        [resizable]="false"
                        (sortChange)="dataGridTaskNotesHelper.sortChange($event)">
                <!-- Toolbar Template -->
                <ng-template kendoGridToolbarTemplate [position]="'top'">
					<div class="table-container">
                        <label class="table-title">Task Notes ({{dataGridTaskNotesHelper.gridData.data.length}})</label>
                        <tds-button-add [title]="'Create'"
										[displayLabel]="false"
										[small]="true"
										iconClass="is-solid"
                                        [tooltip]="'Create a note'"
                                        [id]="'btnAddNote'"
									[disabled]="!hasEditTaskPermission"
                                        (click)="onCreateNote()">
                        </tds-button-add>
					</div>
                </ng-template>
                <!-- Columns -->
                <kendo-grid-column *ngFor="let column of taskNotesColumnsModel.columns"
                                   field="{{column.property}}"
                                   [width]="!column.width ? COLUMN_MIN_WIDTH : column.width">
                    <!-- Header Template -->
                    <ng-template kendoGridHeaderTemplate>
                        <label>{{column.label}}</label>
                    </ng-template>

                    <ng-template kendoGridCellTemplate let-dataItem let-rowIndex="rowIndex" >
                        <div class="pad-left-7 pad-bot-6">{{dataItem[column.property]}}</div>
                    </ng-template>

                </kendo-grid-column>
                <kendo-grid-messages noRecords="There are no records to display."> </kendo-grid-messages>
            </kendo-grid>
        </div>
	`,
	styles: []
})
export class TaskNotesComponent implements OnInit {
	@Input() dataGridTaskNotesHelper: DataGridOperationsHelper;
	@Output() create: EventEmitter<void> = new EventEmitter<void>();
	protected taskNotesColumnsModel = new TaskNotesColumnsModel();
	public hasEditTaskPermission = false;
	COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;

	constructor(
		private permissionService: PermissionService
	) {
		// constructor
	}

	ngOnInit() {
		/* on init */
		this.hasEditTaskPermission = this.permissionService.hasPermission(Permission.TaskEdit);
	}

	protected onCreateNote(): void {
		this.create.emit();
	}

}

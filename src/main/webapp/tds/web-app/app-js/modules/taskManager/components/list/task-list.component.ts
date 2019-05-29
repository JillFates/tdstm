/**
 * Created by Jorge Morayta on 3/15/2017.
 */

import {Component, OnInit} from '@angular/core';

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
							<label for="v">
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
				</div>
			</section>
		</div>
		`
})

export class TaskListComponent {
	timerList = ['Manual', '1 Min', '2 Min', '3 Min', '4 Min', '5 Min'];
	timerValue = 'Manual';
}

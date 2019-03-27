import {Component, OnInit} from '@angular/core';
import {ReportsService} from '../../service/reports.service';

@Component({
	selector: 'tds-task-report',
	template: `
		<div class="content body">
			<section>
				<div>
					<form class="formly form-horizontal" role="form" novalidate >
						<div class="box box-primary">
							<div class="box-header">
							</div>
							<div class="box-body">
								<div class="form-group row">
									<label class="col-sm-1 control-label" for="fetch">Events</label>
									<div class="col-sm-11">
										<kendo-multiselect
											style="height: 100%;"
											name="event"
											class="form-control"
											[data]="eventList"
											[textField]="'name'"
											[valueField]="'id'"
											[(ngModel)]="selectedEvents">
										</kendo-multiselect>
									</div>
								</div>
								<div class="form-group row">
									<span class="col-sm-1"></span>
									<div class="col-sm-11">
										<label for="one" style="margin-right: 20px">
											<input type="checkbox" name="one" id="one" [(ngModel)]="includeComments">
											Include comments in report
										</label>
										<label for="two" style="margin-right: 20px">
											<input type="checkbox" name="two" id="two" [(ngModel)]="includeRTasks">
											Include only remaining tasks in report
										</label>
										<label for="three" style="margin-right: 20px">
											<input type="checkbox" name="three" id="three" [(ngModel)]="includeUTasks">
											Include Unpublished Tasks
										</label>
									</div>
								</div>
								<div class="form-group row">
									<div class="col-sm-12" style="text-align: right">
										<tds-button-custom style="margin-right: 10px"
											class="btn-primary"
											(click)="onGenerateWebReport()"
											title="Generate Web"
											tooltip="Generate Web"
											icon="check-square">
										</tds-button-custom>
										<tds-button-custom
											class="btn-primary"
											(click)="onGenerateXLSReport()"
											title="Generate XLS"
											tooltip="Generate XLS"
											icon="check-square">
										</tds-button-custom>
									</div>
								</div>
							</div>
						</div>
					</form>
				</div>
			</section>
		</div>
		`
	})
export class TaskReportComponent {

	eventList: any;
	selectedEvents: any;
	includeComments = true;
	includeRTasks = true;
	includeUTasks = true;
	private readonly allEventsOption = {id: -1, name: 'All Events'};

	constructor(private reportsService: ReportsService) {
		this.onLoad();
	}

	private onLoad() {
		this.reportsService.getEventList().subscribe( result => {
			this.eventList = [this.allEventsOption].concat(result.data);
		})
	}
}

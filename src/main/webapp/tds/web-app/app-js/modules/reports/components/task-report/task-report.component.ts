// Angular
import {Component, ComponentFactoryResolver, OnInit} from '@angular/core';
// Component
import {ReportComponent} from '../report.component';
// Service
import {ReportsService} from '../../service/reports.service';
import {DialogService} from 'tds-component-library';
// Other
import {Observable} from 'rxjs';

@Component({
	selector: 'tds-task-report',
	template: `
		<div class="content body">
			<tds-report-toggle-filters [hideFilters]="hideFilters"
			                            						 (reload)="onReload()"
																 (toggle)="toggleFilters($event)"
																 [disabled]="!generatedReport"></tds-report-toggle-filters>
			<section class="box-body">
				<div>
					<form class="formly form-horizontal" role="form" novalidate>
						<div class="box box-primary">
							<div class="box-header">
							</div>
							<div class="box-body">
								<div class="filters-wrapper" [hidden]="hideFilters">
									<div class="form-group row">
										<label class="col-sm-2 control-label" for="fetch">Events</label>
										<div class="col-sm-3">
											<kendo-multiselect
												name="event"
												class="form-control event-multiselect"
												[data]="eventList"
												[textField]="'name'"
												[valueField]="'id'"
												[(ngModel)]="selectedEvents"
												(valueChange)="onValueChange($event)">
											</kendo-multiselect>
										</div>
									</div>
									<div class="form-group row checkboxes">
										<div class="col-sm-5 col-sm-offset-2">
											<clr-checkbox-wrapper class="inline">
												<input
													clrCheckbox
													id="one"
													type="checkbox"
													name="one"
													class="component-action-just-planning"
													[(ngModel)]="includeComments"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="one"
												>
												Include comments in report
												</label>
                							</clr-checkbox-wrapper>

										</div>
									</div>
									<div class="form-group row checkboxes">
										<div class="col-sm-5 col-sm-offset-2">
											<clr-checkbox-wrapper class="inline">
												<input
													clrCheckbox
													id="two"
													type="checkbox"
													name="two"
													class="component-action-just-planning"
													[(ngModel)]="includeOnlyRemaining"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="two"
												>
												Include only remaining tasks in report
												</label>
                							</clr-checkbox-wrapper>
										</div>
									</div>
									<div class="form-group row">
										<div class="col-sm-5 col-sm-offset-2">
											<clr-checkbox-wrapper class="inline">
												<input
													clrCheckbox
													id="three"
													type="checkbox"
													name="three"
													class="component-action-just-planning"
													[(ngModel)]="includeUnpublished"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="three"
												>
												Include Unpublished Tasks
												</label>
                							</clr-checkbox-wrapper>


										</div>
									</div>
									<div class="form-group row ">
										<div class="col-sm-4 col-sm-offset-2 buttons">
											<tds-button-custom
													(click)="onGenerateReport()"
													title="Generate"
													tooltip="Generate"
													icon="table">
											</tds-button-custom>
											<tds-button-export
												(click)="onGenerateXLSReport()"
												title="Export to Excel"
												tooltip="Export to Excel">
											</tds-button-export>
										</div>
									</div>
									<hr/>
								</div>
								<div class="report-content" [innerHTML]="reportResult"></div>
							</div>
						</div>
					</form>
				</div>
			</section>
		</div>
	`
	})
export class TaskReportComponent extends ReportComponent {

	eventList: any;
	selectedEvents: Array<any>;
	includeComments = true;
	includeOnlyRemaining = true;
	includeUnpublished = true;
	private readonly allEventsOption = {id: -1, name: 'All Events'};

	constructor(
		componentFactoryResolver: ComponentFactoryResolver,
		reportsService: ReportsService,
		dialogService: DialogService
	) {
		super(componentFactoryResolver, reportsService, dialogService);
		this.selectedEvents = [this.allEventsOption];
		this.onLoad();
	}

	/**
	 * On load, get the event list.
	 */
	private onLoad() {
		this.reportsService.getEventList().subscribe( result => {
			this.eventList = [this.allEventsOption].concat(result.data);
		})
	}

	/**
	 * Revert the page to its initial state.
	 */
	public onReload(): void {
		this.hideFilters = false;
		this.generatedReport = false;
		this.reportResult = null;
		this.onLoad();
	}

	/**
	 * On Event multi-select change.
	 * If no selection is made then set the default All Events.
	 * If there are other events selected (not All Events) then remove default option.
	 * @param selection: Array<any>
	 */
	onValueChange(selection: Array<any>) {
		if (selection.length === 0) {
			this.selectedEvents = [this.allEventsOption];
		}
		const match = selection.find(item => item.id !== -1);
		if (match) {
			this.selectedEvents = selection.filter(item => item.id !== -1);
		}
	}

	/**
	 * On Generate Web Report type.
	 */
	onGenerateReport(): void {
		this.generateReport('Generate Web').subscribe(result => {
			this.hideFilters = true;
			this.reportResult = this.reportsService.getSafeHtml(result);
			this.generatedReport = true;
		});
	}

	/**
	 * On Generate XLS Report type.
	 */
	onGenerateXLSReport(): void {
		this.generateReport('Generate Xls').subscribe(result => {
			const element = document.createElement('a');
			element.href = URL.createObjectURL(result.blob);
			element.download = result.filename;
			document.body.appendChild(element);
			element.click();
		});
	}

	/**
	 * Generates the output report.
	 * Prepares the event list parameter based on user selection.
	 * @param reportType: web or xls
	 */
	generateReport(reportType: 'Generate Web'|'Generate Xls'): Observable<any> {
		let events: Array<string> = this.selectedEvents.map(item => item.id.toString());
		if (this.selectedEvents.length === 1 && this.selectedEvents[0].id === -1) {
			events = ['all'];
		}
		return this.reportsService.getTaskReport(
			events,
			reportType,
			this.includeComments,
			this.includeOnlyRemaining,
			this.includeUnpublished);
	}
}

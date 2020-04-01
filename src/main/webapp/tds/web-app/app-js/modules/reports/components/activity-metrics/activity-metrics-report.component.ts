// Angular
import {Component, ComponentFactoryResolver} from '@angular/core';
// Component
import {ReportComponent} from '../report.component';
// Service
import {ReportsService} from '../../service/reports.service';
import {DialogService} from 'tds-component-library';

@Component({
	selector: 'tds-activity-metrics-report',
	template: `
		<div class="content body activity-metrics-report">
			<tds-report-toggle-filters
				[hideFilters]="hideFilters"
				(reload)="onReload()"
				(toggle)="toggleFilters()"
				[disabled]="!generatedReport">
			</tds-report-toggle-filters>
			<section class="box-body">
				<div>
					<form class="formly form-horizontal" role="form" novalidate>
						<div class="box box-primary">
							<div class="box-header">
							</div>
							<div class="box-body">
								<div class="filters-wrapper" [hidden]="hideFilters">
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="projectList">Projects</label>
										<div class="col-sm-3">
											<kendo-multiselect
												style="height: 100%;"
												name="projectList"
												class="form-control"
												[data]="projectList"
												[textField]="'name'"
												[valueField]="'id'"
												[(ngModel)]="selectedProjects"
												(valueChange)="onValueChange($event)"
												[loading]="loadingLists">
											</kendo-multiselect>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="startDate">Start Date</label>
										<div class="col-sm-2">
											<kendo-datepicker
												name="startDate"
												[(ngModel)]="startDate"
												[value]="startDate"
												[format]="'MM/dd/yyyy'"
												class="form-control datepicker">
											</kendo-datepicker>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="endDate">End Date</label>
										<div class="col-sm-2">
											<kendo-datepicker
												name="endDate"
												[(ngModel)]="endDate"
												[value]="endDate"
												[format]="'MM/dd/yyyy'"
												class="form-control datepicker">
											</kendo-datepicker>
										</div>
									</div>
									<div class="form-group row checkboxes">
										<div class="col-sm-2 col-sm-offset-2">
											<label for="one">
												<input type="checkbox" name="one" id="one" [(ngModel)]="includeNonPlanning">
												Include Non-planning
											</label>
										</div>
									</div>
									<div class="form-group row ">
										<div class="col-sm-2 col-sm-offset-2 buttons">
											<tds-button-export
												[disabled]="loadingLists"
												(click)="onGenerateReport()"
												title="Export to Excel"
												tooltip="Export to Excel">
											</tds-button-export>
										</div>
									</div>
								</div>
								<div class="report-content" [innerHTML]="reportResult"></div>
							</div>
						</div>
					</form>
				</div>
			</section>
		</div>`})
export class ActivityMetricsReportComponent extends ReportComponent {

	projectList: Array<any> = [];
	selectedProjects: Array<any>;
	startDate: Date;
	endDate: Date;
	includeNonPlanning = false;

	private readonly allProjectsOption = {id: -1, name: 'All'};

	constructor(
		componentFactoryResolver: ComponentFactoryResolver,
		reportsService: ReportsService,
		dialogService: DialogService) {
		super(componentFactoryResolver, reportsService, dialogService);
		this.selectedProjects = [this.allProjectsOption];
		this.onLoad();
	}

	/**
	 * Load the data to populate report UI options.
	 */
	private onLoad(): void {
		this.loadingLists = true;
		this.reportsService.getProjectMetricsLists().subscribe(result => {
			this.projectList = [this.allProjectsOption, ...result.projects];
			this.startDate = new Date(result.startDate);
			this.endDate = new Date(result.endDate);
			this.loadingLists = false;
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
			this.selectedProjects = [this.allProjectsOption];
		}
		const match = selection.find(item => item.id !== -1);
		if (match) {
			this.selectedProjects = selection.filter(item => item.id !== -1);
		}
	}

	/**
	 * Call endpoint to Generate Report based on UI options.
	 */
	onGenerateReport(): void {
		let projectIds: Array<string> = this.selectedProjects.map(item => item.id.toString());
		this.reportsService.generateActivityMetricsReport(
			projectIds,
			this.startDate,
			this.endDate,
			this.includeNonPlanning)
			.subscribe( result => {
				const element = document.createElement('a');
				element.href = URL.createObjectURL(result.blob);
				element.download = result.filename;
				document.body.appendChild(element);
				element.click();
				this.generatedReport = true;
		});
	}
}

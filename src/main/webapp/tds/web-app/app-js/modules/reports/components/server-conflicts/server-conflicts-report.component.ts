import {Component, OnInit} from '@angular/core';
import {ReportsService} from '../../service/reports.service';

@Component({
	selector: 'tds-server-conflicts-report',
	template: `<div class="content body">
		<section>
			<div>
				<form class="formly form-horizontal" role="form" novalidate >
					<div class="box box-primary">
						<div class="box-header">
						</div>
						<div class="box-body">
							<div class="form-group row">
								<label class="col-sm-2 control-label" for="fetch">Events</label>
								<div class="col-sm-3">
									<kendo-dropdownlist
										name="bundleList"
										class="form-control"
										[data]="bundleList"
										[textField]="'name'"
										[valueField]="'id'"
										[(ngModel)]="selectedBundle">
									</kendo-dropdownlist>
								</div>
							</div>
							<div class="form-group row checkboxes">
								<span class="col-sm-2"></span>
								<div class="col-sm-10">
									<label for="one">
										<input type="checkbox" name="one" id="one" [(ngModel)]="bundleConflict">
										Bundle Conflict - Having dependency references to assets assigned to unrelated bundles
									</label>
								</div>
							</div>
							<div class="form-group row checkboxes">
								<span class="col-sm-2"></span>
								<div class="col-sm-10">
									<label for="two">
										<input type="checkbox" name="two" id="two" [(ngModel)]="unresolvedDependencies">
										Unresolved Dependencies - Having dependencies with status Unknown or Questioned
									</label>
								</div>
							</div>
							<div class="form-group row checkboxes">
								<span class="col-sm-2"></span>
								<div class="col-sm-10">
									<label for="three">
										<input type="checkbox" name="three" id="three" [(ngModel)]="noSupportDependencies">
										No Supports Dependencies - Having no Supports relationship depicting its purpose
									</label>
								</div>
							</div>
							<div class="form-group row">
								<span class="col-sm-2"></span>
								<div class="col-sm-10">
									<label for="four">
										<input type="checkbox" name="three" id="three" [(ngModel)]="noVmHost">
										No VM Host- VMs with no associated Host environment
									</label>
								</div>
							</div>
							<div class="form-group row">
								<label class="col-sm-2 control-label" for="fetch">Maximum servers to report</label>
								<div class="col-sm-1">
									<kendo-dropdownlist
										name="maxServers"
										class="form-control"
										[data]="maxServersList"
										[(ngModel)]="maxServersToReport">
									</kendo-dropdownlist>
								</div>
							</div>
							<div class="form-group row ">
								<div class="col-sm-12 buttons text-right">
									<tds-button-custom class="btn-primary"
																		 (click)="onGenerateWebReport()"
																		 title="Generate Report"
																		 tooltip="Generate Report"
																		 icon="check-square">
									</tds-button-custom>
								</div>
							</div>
							<hr/>
							<div class="report-content" [innerHTML]="reportResult"></div>
						</div>
					</div>
				</form>
			</div>
		</section>
	</div>`})
export class ServerConflictsReportComponent {

	bundleConflict = true;
	unresolvedDependencies = true;
	noSupportDependencies = true;
	noVmHost = true;
	selectedBundle = null;
	bundleList: Array<any>;
	maxServersList = [100, 250, 500];
	maxServersToReport = 100;

	constructor(private reportsService: ReportsService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.reportsService.getMoveBundles().subscribe(result => {
			this.bundleList = result;
			if (this.bundleList.length > 0) {
				this.selectedBundle = this.bundleList[0];
			}
		})
	}
}

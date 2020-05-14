// Angular
import {Component, ComponentFactoryResolver, ElementRef} from '@angular/core';
// Component
import {ReportComponent} from '../report.component';
// Service
import {ReportsService} from '../../service/reports.service';
import {DialogService} from 'tds-component-library';

@Component({
	selector: 'tds-server-conflicts-report',
	template: `
		<div class="content body">
			<tds-report-toggle-filters [hideFilters]="hideFilters"
																 (toggle)="toggleFilters($event)"
			                           							 (reload)="onReload()"
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
										<div class="col-sm-5 col-sm-offset-2">
											<clr-checkbox-wrapper class="inline">
												<input
													clrCheckbox
													id="one"
													type="checkbox"
													name="one"
													class="component-action-just-planning"
													[(ngModel)]="bundleConflict"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="one"
												>
												Bundle Conflict - Having dependency references to assets assigned to unrelated bundles
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
													[(ngModel)]="unresolvedDependencies"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="two"
												>
												Unresolved Dependencies - Having dependencies with status Unknown or Questioned
												</label>
                							</clr-checkbox-wrapper>

										</div>
									</div>
									<div class="form-group row checkboxes">
										<div class="col-sm-5 col-sm-offset-2">
											<clr-checkbox-wrapper class="inline">
												<input
													clrCheckbox
													id="three"
													type="checkbox"
													name="three"
													class="component-action-just-planning"
													[(ngModel)]="noSupportDependencies"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="three"
												>
												No Supports Dependencies - Having no Supports relationship depicting its purpose
												</label>
                							</clr-checkbox-wrapper>

										</div>
									</div>
									<div class="form-group row">
										<div class="col-sm-5 col-sm-offset-2">
											<clr-checkbox-wrapper class="inline">
												<input
													clrCheckbox
													id="four"
													type="checkbox"
													name="four"
													class="component-action-just-planning"
													[(ngModel)]="noVmHost"
												/>
												<label
													class="clr-control-label clr-control-label-sm inline"
													for="four"
												>
												No VM Host- VMs with no associated Host environment
												</label>
                							</clr-checkbox-wrapper>
										</div>
									</div>
									<div class="form-group row">
										<label class="col-sm-2 control-label" for="fetch">Maximum servers to report</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="maxServers"
												class="form-control"
												[data]="maxServersList"
												[(ngModel)]="maxAssetsToReport">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row ">
										<div class="col-sm-2 col-sm-offset-2 buttons">
											<tds-button-custom
													(click)="onGenerateReport()"
													title="Generate"
													tooltip="Generate"
													icon="table">
											</tds-button-custom>
										</div>
									</div>
									<hr>
								</div>
								<div class="report-content" [innerHTML]="reportResult"></div>
							</div>
						</div>
					</form>
				</div>
			</section>
		</div>`})
export class ServerConflictsReportComponent extends ReportComponent {

	bundleConflict = true;
	unresolvedDependencies = true;
	noSupportDependencies = true;
	noVmHost = true;
	selectedBundle = null;
	bundleList: Array<any>;
	maxServersList = [100, 250, 500];
	maxAssetsToReport = 100;

	constructor(
		componentFactoryResolver: ComponentFactoryResolver,
		reportsService: ReportsService,
		dialogService: DialogService,
		private elRef: ElementRef) {
		super(componentFactoryResolver, reportsService, dialogService);
		this.onLoad();
	}

	/**
	 * Load the data to populate report UI options.
	 */
	private onLoad(): void {
		this.reportsService.getMoveBundles().subscribe(result => {
			this.bundleList = result;
			if (this.bundleList.length > 0) {
				this.selectedBundle = this.bundleList[0];
			}
		});
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
	 * Call endpoint to Generate Report based on UI options.
	 */
	onGenerateReport(): void {
		this.reportsService.generateServerConflictsReport(
			this.selectedBundle.id,
			this.bundleConflict,
			this.unresolvedDependencies,
			this.noSupportDependencies,
			this.noVmHost,
			this.maxAssetsToReport).subscribe(result => {
			this.reportResult = this.reportsService.getSafeHtml(result);
			this.hideFilters = true;
			setTimeout(() => {
				const assetLinks = this.elRef.nativeElement.querySelectorAll('.inlineLink');
				assetLinks.forEach(item => {
					item.addEventListener('click', event => this.onAssetLinkClick(event));
				})
			}, 300);
			this.generatedReport = true;
		})
	}
}

import {ReportsService} from '../../service/reports.service';
import {Component, ElementRef} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {ReportComponent} from '../report.component';

@Component({
	selector: 'tds-application-event-results-report',
	template: `
		<div class="content body">
			<tds-report-toggle-filters [hideFilters]="hideFilters"
																 (toggle)="toggleFilters()"
																 [disabled]="!generatedReport"></tds-report-toggle-filters>
			<section class="box-body">
				<div>
					<form class="formly form-horizontal" role="form" novalidate>
						<div class="box box-primary">
							<div class="box-header">
							</div>
							<div class="box-body">
								<div class="filters-wrapper" [hidden]="hideFilters">
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="bundleList">Bundle</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="bundleList"
												class="form-control"
												[data]="bundleList"
												[textField]="'name'"
												[valueField]="'id'"
												[(ngModel)]="selectedBundle"
												(ngModelChange)="onBundleListChange($event)">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="smeList">SME</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="smeList"
												class="form-control"
												[data]="smeList"
												[textField]="'text'"
												[valueField]="'id'"
												[(ngModel)]="selectedSme"
												[loading]="loadingSmeList || loadingLists">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="starstWithList">Start of with</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="startsWithList"
												class="form-control"
												[data]="startsWithList"
												[textField]="'text'"
												[valueField]="'id'"
												[(ngModel)]="selectedStartWith"
												[loading]="loadingLists">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="endsWithList">Ends with</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="endsWithList"
												class="form-control"
												[data]="endsWithList"
												[textField]="'text'"
												[valueField]="'id'"
												[(ngModel)]="selectedEndWith"
												[loading]="loadingLists">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row">
										<label class="col-sm-2 control-label" for="outageWindowList">Outage window</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="outageWindowList"
												class="form-control"
												[data]="outageWindowList"
												[textField]="'label'"
												[valueField]="'field'"
												[(ngModel)]="selectedOutage"
												[loading]="loadingLists">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row ">
										<div class="col-sm-2 col-sm-offset-2 buttons">
											<tds-button-custom class="btn-primary"
																				 [disabled]="loadingSmeList || loadingLists"
																				 (click)="onGenerateReport()"
																				 title="Generate"
																				 tooltip="Generate"
																				 icon="table">
											</tds-button-custom>
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
		</div>`})
export class ApplicationEventResultsReportComponent extends ReportComponent {

	bundleList: Array<any>;
	selectedBundle: any = null;
	smeList: Array<any>;
	selectedSme: any = null;
	startsWithList: Array<any>;
	selectedStartWith: any = null;
	endsWithList: Array<any>;
	selectedEndWith: any = null;
	outageWindowList: Array<any>;
	selectedOutage: any = null;
	loadingSmeList = false;

	private readonly allSmeOption = {id: -1, text: 'All'};

	constructor(
		reportsService: ReportsService,
		dialogService: UIDialogService,
		private elRef: ElementRef,
		private userPreferenceService: PreferenceService) {
		super(reportsService, dialogService);
		this.onLoad();
	}

	/**
	 * Load the data to populate report UI options.
	 */
	private onLoad(): void {
		this.loadingLists = true;
		this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.CURRENT_MOVE_BUNDLE_ID).subscribe( moveBundleId => {
			moveBundleId = parseInt(moveBundleId, 0);
			// Load the move bundle list.
			this.reportsService.getMoveBundles().subscribe(moveBundles => {
				this.bundleList = moveBundles;
				if (moveBundleId && moveBundleId >= 0) {
					this.selectedBundle = this.bundleList.find(bundle => bundle.id === moveBundleId);
				} else {
					this.selectedBundle = this.bundleList[0];
					moveBundleId = this.selectedBundle.id;
				}
				// Load all lists
				this.reportsService.getApplicationEventReportLists(moveBundleId).subscribe(lists => {
					this.smeList = lists.smeList.map(item => {
						return {id: item.id, text: `${item.lastName}, ${item.firstName}`}
					});
					this.smeList = [this.allSmeOption].concat(this.smeList);
					this.selectedSme = this.allSmeOption;
					this.startsWithList = lists.categories;
					this.selectedStartWith = this.startsWithList.find(item => item.id === 'shutdown');
					this.endsWithList = lists.categories;
					this.selectedEndWith = this.endsWithList.find(item => item.id === 'startup');
					this.outageWindowList = lists.outageList;
					this.selectedOutage = this.outageWindowList.find(item => item.field === 'drRtoDesc');
					this.loadingLists = false;
				});
			});
		});
	}

	/**
	 * Reload the SME list based on bundle selection.
	 * @param $event
	 */
	onBundleListChange($event: any): void {
		if ($event && $event.id) {
			this.loadingSmeList = true;
			this.reportsService.getSmeList($event.id).subscribe(smeList => {
				this.smeList = smeList.map(item => {
					return {id: item.id, text: `${item.lastName}, ${item.firstName}`}
				});
				this.smeList = [this.allSmeOption].concat(this.smeList);
				this.selectedSme = this.allSmeOption;
				this.loadingSmeList = false;
			});
		}
	}

	/**
	 * Call endpoint to Generate Report based on UI options.
	 */
	onGenerateReport(): void {
		this.reportsService.getApplicationEventReport(
			this.selectedBundle.id,
			this.selectedSme.id,
			this.selectedStartWith.id,
			this.selectedEndWith.id,
			this.selectedOutage.field).subscribe(result => {
				this.hideFilters = true;
				this.reportResult = this.reportsService.getSafeHtml(result);
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

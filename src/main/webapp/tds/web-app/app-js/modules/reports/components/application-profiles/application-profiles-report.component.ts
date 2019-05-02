import {ReportsService} from '../../service/reports.service';
import {Component, ElementRef} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {ReportComponent} from '../report.component';
import {forkJoin} from 'rxjs';
import {AssetDependencyComponent} from '../../../assetExplorer/components/asset-dependency/asset-dependency.component';
import {DependecyService} from '../../../assetExplorer/service/dependecy.service';
declare var jQuery: any;

@Component({
	selector: 'tds-application-profiles-report',
	template: `
		<div class="content body">
			<tds-report-toggle-filters [hideFilters]="hideFilters" (toggle)="toggleFilters($event)"></tds-report-toggle-filters>
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
												[loading]="loadingLists">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="appOwnerList">App Owner</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="appOwnerList"
												class="form-control"
												[data]="appOwnerList"
												[textField]="'text'"
												[valueField]="'id'"
												[(ngModel)]="selectedAppOwner"
												[loading]="loadingLists">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row input">
										<label class="col-sm-2 control-label" for="reportMaxAssets">Max Applications to report</label>
										<div class="col-sm-3">
											<kendo-dropdownlist
												name="reportMaxAssets"
												class="form-control"
												[data]="[100, 250, 500]"
												[(ngModel)]="maxAssets">
											</kendo-dropdownlist>
										</div>
									</div>
									<div class="form-group row ">
										<div class="col-sm-2 col-sm-offset-2 buttons">
											<tds-button-custom class="btn-primary"
																				 [disabled]="loadingLists"
																				 (click)="onGenerateReport()"
																				 title="Generate Report"
																				 tooltip="Generate Report"
																				 icon="check-square">
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
export class ApplicationProfilesReportComponent extends ReportComponent {

	bundleList: Array<any>;
	selectedBundle: any = null;
	smeList: Array<any>;
	selectedSme: any = null;
	appOwnerList: Array<any>;
	selectedAppOwner: any = null;
	maxAssets = 100;

	private readonly allSmeOption = {id: -1, text: 'All'};

	constructor(
		reportsService: ReportsService,
		dialogService: UIDialogService,
		private elRef: ElementRef,
		private userPreferenceService: PreferenceService,
		private assetService: DependecyService) {
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
				// Load SME List
				this.onBundleListChange({id: moveBundleId});
			});
		});
	}

	/**
	 * Reload the SME list based on bundle selection.
	 * @param $event
	 */
	onBundleListChange($event: any): void {
		if ($event && $event.id) {
			this.loadingLists = true;
			const observables = forkJoin(
				this.reportsService.getSmeList($event.id),
				this.reportsService.getAppOwnerList($event.id)
			);
			observables.subscribe({
				next: value => {
					// SME List
					this.smeList = value[0].map(item => {
						return {id: item.id, text: `${item.lastName}, ${item.firstName}`}
					});
					this.smeList = [this.allSmeOption].concat(this.smeList);
					this.selectedSme = this.allSmeOption;
					// App Owner List
					this.appOwnerList = value[1].map(item => {
						return {id: item.id, text: `${item.lastName}, ${item.firstName}`}
					});
					this.appOwnerList = [this.allSmeOption].concat(this.appOwnerList);
					this.selectedAppOwner = this.allSmeOption;
				},
				complete: () => {
					this.loadingLists = false;
				}
			});
		}
	}

	/**
	 * Call endpoint to Generate Report based on UI options.
	 */
	onGenerateReport(): void {
		this.reportsService.generateApplicationProfilesReport(this.selectedBundle.id, this.selectedSme.id,
			this.selectedAppOwner.id, this.maxAssets).subscribe(result => {
				this.hideFilters = true;
				this.reportResult = this.reportsService.getSafeHtml(result);
				setTimeout(() => {
					jQuery('[data-toggle="popover"]').popover();
					const assetLinks = this.elRef.nativeElement.querySelectorAll('.inlineLink');
					assetLinks.forEach(item => {
						item.addEventListener('click', event => this.onAssetLinkClick(event));
					});
					const subAssetLinks = this.elRef.nativeElement.querySelectorAll('.subAssetLink');
					subAssetLinks.forEach(item => {
						item.removeAttribute('onclick');
						item.addEventListener('click', event => this.onAssetLinkClick(event));
					});
					const assetDepLinks = this.elRef.nativeElement.querySelectorAll('.assetDepLink');
					assetDepLinks.forEach(item => {
						item.removeAttribute('onclick');
						item.addEventListener('click', event => this.onAssetDepLinkClick(event));
					});
				}, 300);
		})
	}

	/**
	 * Asset name link handler
	 * @param event: any
	 */
	protected onAssetDepLinkClick(event: any): void {
		if (event.target) {
			const {assetId, depId} = event.target.dataset;
			if (assetId && depId) {
				this.assetService.getDependencies(assetId, depId)
					.subscribe((result) => {
						this.showDependencyView(result);
					});
			}
		}
	}

	/**
	 * Show the asset dependencies popup.
	 * @param assetId: number
	 * @param assetClass: string
	 */
	showDependencyView(assetDependencies: any): void {
		this.dialogService.extra(AssetDependencyComponent, [
			{ provide: 'ASSET_DEP_MODEL', useValue: assetDependencies }])
			.then(res => console.log(res))
			.catch(res => console.log(res));
	}

}

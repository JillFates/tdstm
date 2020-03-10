import {
	Component
} from '@angular/core';

import {
	Observable,
} from 'rxjs';

import {ReportsService} from '../../service/reports.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UserService} from '../../../auth/service/user.service';
import { EntityConflict } from '../../model/conflicts.model';
import {ReportComponent} from '../report.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'tds-application-conflicts',
	templateUrl: 'application-conflicts.component.html'
})
export class ApplicationConflictsComponent extends ReportComponent {
	invalidStatusList = ['Questioned', 'Unknown'];
	userContext = null;
	isDisplayingReport: boolean;
	dateFormatTime = '';
	reportDate = new Date();
	reportProject = '';
	reportBundle = '';
	reportOwner = '';
	planningBundles: any;
	userTimeZone;
	public model = {
		moveBundleList: [],
		appOwnerList: [],
		appOwner: null,
		bundleConflict: true,
		bundle: null,
		unresolvedDependencies: true,
		missingDependencies: true,
		maxApplications: {value: 100},
		maxApplicationsList: [{value: 100}, {value: 250}, {value: 500}]
	};
	applicationConflicts: Array<EntityConflict> = [];

	constructor(
		private preferenceService: PreferenceService,
		private userService: UserService,
		protected dialogService: UIDialogService,
		protected reportsService: ReportsService) {
			super(reportsService, dialogService);
			this.planningBundles =  {id: 'useForPlanning', name: 'Planning Bundles'};
			this.load();
	}

	/**
	 * Load the user context and list bundles
	 */
	load() {
		this.isDisplayingReport = null;
		const commonCalls = [
			this.userService.getUserContext(),
			this.reportsService.getBundles()
		];

		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [userContext, bundles] = results;
				this.userContext = userContext;
				this.dateFormatTime = this.preferenceService.getUserDateTimeFormat();
				this.userTimeZone = this.preferenceService.getUserTimeZone();
				if (bundles) {
					this.model.moveBundleList = bundles.moveBundles
						.map((bundle: any) => ({id: bundle.id.toString(), name: bundle.name}));
					this.model.moveBundleList.unshift(this.planningBundles);
					this.model.bundle = (bundles.moveBundleId) ? {id: bundles.moveBundleId} : this.planningBundles;
					this.updateOwnersList(this.model.bundle);
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
		this.load();
	}

	/**
	 * Get the name of the current bundle selected
	*/
	getReportBundleName(): string {
		const id = this.model.bundle && this.model.bundle.id || '';

		const bundle =  this.model.moveBundleList.find((bundle) => bundle.id === id);
		return bundle.name || '';
	}

	/**
	 * Get the name of the current owner selected
	*/
	getReportOwnerName(): string {
		const id = this.model.appOwner && this.model.appOwner.id || '';

		const owner =  this.model.appOwnerList.find((owner) => owner.id === id);
		return owner.name || '';
	}

	/**
	 * Get the conflicts to feed the report
	*/
	onGenerateReport(): void {
		if (this.model.bundle) {
			this.reportsService.getApplicationConflicts(
				this.model.bundle.id.toString(),
				(this.model.appOwner && this.model.appOwner.id) || '',
				this.model.bundleConflict,
				this.model.missingDependencies,
				this.model.unresolvedDependencies,
				this.model.maxApplications.value,
				this.model.moveBundleList
				)
				.subscribe((results: any) => {
					this.reportDate = new Date();
					this.reportProject =  this.userContext.project.name;
					this.reportBundle = this.getReportBundleName();
					this.reportOwner = this.getReportOwnerName();

					this.applicationConflicts = results;
					this.isDisplayingReport = true;
					this.hideFilters = true;
					this.generatedReport = true;
				});
		}

	}

	/**
	 * Get the owners whom belong to the current bundle
	 * @param bundle: any
	 */
	updateOwnersList(bundle: any) {
		if (bundle && bundle.id) {
			this.reportsService.getOwnersByBundle(bundle.id)
			.subscribe((results) => {
				if (results && results.owners) {
					this.model.appOwnerList = results.owners
						.map((result: any) => ({id: result.id.toString(), name: result.fullName}));
					this.model.appOwnerList.unshift({id: '', name: 'All' });
					this.model.appOwner = {id: ''};
				}
			});
		}
	}
}

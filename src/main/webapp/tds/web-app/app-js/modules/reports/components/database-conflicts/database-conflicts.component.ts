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
import {DatabaseFiltersModel} from '../../model/database-filters.model';

@Component({
	selector: 'tds-database-conflicts',
	templateUrl: 'database-conflicts.component.html'
})
export class DatabaseConflictsComponent extends ReportComponent {
	reportTitle: any;
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
	filters: DatabaseFiltersModel = {
		bundle: null,
		bundleConflicts: true,
		unresolvedDependencies: true,
		missingApplications: true,
		unsupportedDependencies: true,
		maxAssets: {value: 100}
	}
	public model = {
		moveBundleList: [],
		bundleConflict: true,
		bundle: null,
		unresolvedDependencies: true,
		missingApplications: true,
		unsupported: true,
		maxDatabases: {value: 100},
		maxDatabasesList: [{value: 100}, {value: 250}, {value: 500}]
	};
	databaseConflicts: Array<EntityConflict> = [];

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
	 * Set up the parameters selected in the filter component and open up the report
	 * @param params: any
	*/
	openReport(params: any) {
		this.reportBundle = params.bundleName;

		this.onGenerateReport();
	}
	/**
	 * Get the conflicts to feed the report
	*/
	onGenerateReport(): void {
		if (this.model.bundle) {
			this.reportsService.getDatabaseConflicts(
				this.filters.bundle.id.toString(),
				this.filters.bundleConflicts,
				this.filters.missingApplications,
				this.filters.unresolvedDependencies,
				this.filters.unsupportedDependencies,
				this.filters.maxAssets.value,
				this.model.moveBundleList
				)
				.subscribe((results: any) => {
					const titles = [];
					this.reportTitle = '';
					if (this.filters.bundleConflicts) {
						titles.push('Bundle Conflicts');
					}
					if (this.filters.unresolvedDependencies) {
						titles.push('Unresolved Dependencies');
					}
					if (this.filters.missingApplications) {
						titles.push('No Applications');
					}
					if (this.filters.unsupportedDependencies) {
						titles.push('DB With NO support');
					}
					if (titles.length) {
						this.reportTitle = titles.join(',').toString();
					}

					this.reportDate = new Date();
					this.reportProject =  this.userContext.project.name;
					// this.reportBundle = this.getReportBundleName();

					this.databaseConflicts = results;
					this.isDisplayingReport = true;
					this.generatedReport = true;
					this.hideFilters = true;
				});
		}

	}
}

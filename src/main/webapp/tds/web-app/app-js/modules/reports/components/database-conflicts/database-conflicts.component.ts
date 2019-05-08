import {
	Component
} from '@angular/core';

import {
	Observable,
} from 'rxjs';

import {ReportsService} from '../../service/reports.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UserService} from '../../../security/services/user.service';
import { ApplicationConflict, DatabaseConflict } from '../../model/application-conflicts.model';
import {ReportComponent} from '../report.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
import {DatabaseFiltersModel} from '../../model/database-filters.model';
import {
	DIALOG_SIZE,
} from '../../../../shared/model/constants';

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
	filters: DatabaseFiltersModel = {
		bundle: null,
		bundleConflict: true,
		unresolvedDependencies: true,
		missingApplications: true,
		unsupported: true,
		maxDatabases: {value: 100}
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
	databaseConflicts: Array<DatabaseConflict> = [];

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
				if (bundles) {
					this.model.moveBundleList = bundles.moveBundles
						.map((bundle: any) => ({id: bundle.id.toString(), name: bundle.name}));
					this.model.moveBundleList.unshift(this.planningBundles);
					this.model.bundle = (bundles.moveBundleId) ? {id: bundles.moveBundleId} : this.planningBundles;
				}
			});
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
				this.filters.bundleConflict,
				this.filters.missingApplications,
				this.filters.unresolvedDependencies,
				this.filters.maxDatabases.value
				)
				.subscribe((results: Array<DatabaseConflict>) => {
					const titles = [];
					this.reportTitle = '';
					if (this.filters.bundleConflict) {
						titles.push('Bundle Conflicts');
					}
					if (this.filters.unresolvedDependencies) {
						titles.push('Unresolved Dependencies');
					}
					if (this.filters.missingApplications) {
						titles.push('No Applications');
					}
					if (this.filters.unsupported) {
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
					this.hideFilters = true;
				});
		}

	}

	/**
	 * Open the asset show view of the current database selected
	 * @param database: any
	 */
	onDatabaseSelected(database: any): void {
		this.dialogService.open(AssetShowComponent, [
			{ provide: 'ID', useValue: database.id },
			{ provide: 'ASSET', useValue: database.assetClass }],
			DIALOG_SIZE.LG, false)
			.then(asset => {
				console.log('Done');
			}).catch(error => {
				console.log('Error:', error);
			});
	}
}

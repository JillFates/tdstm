import {
	Component
} from '@angular/core';

import {
	Observable,
} from 'rxjs';

import {ReportsService} from '../../service/reports.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UserService} from '../../../security/services/user.service';
import { ApplicationConflict } from '../../model/application-conflicts.model';
import {ReportComponent} from '../report.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
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
	applicationConflicts: Array<ApplicationConflict> = [];

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
	 * Get the name of the current bundle selected
	*/
	getReportBundleName(): string {
		const id = this.model.bundle && this.model.bundle.id || '';

		const bundle =  this.model.moveBundleList.find((bundle) => bundle.id === id);
		return bundle.name || '';
	}

	/**
	 * Get the conflicts to feed the report
	*/
	onGenerateReport(): void {
		if (this.model.bundle) {
			this.reportsService.getApplicationConflicts(
				this.model.bundle.id.toString(),
				'',
				this.model.bundleConflict,
				this.model.missingApplications,
				this.model.unresolvedDependencies,
				this.model.maxDatabases.value
				)
				.subscribe((results: Array<ApplicationConflict>) => {
					const titles = [];
					this.reportTitle = '';
					if (this.model.bundleConflict) {
						titles.push('Bundle Conflicts');
					}
					if (this.model.unresolvedDependencies) {
						titles.push('Unresolved Dependencies');
					}
					if (this.model.missingApplications) {
						titles.push('No Applications');
					}
					if (this.model.unsupported) {
						titles.push('DB With NO support');
					}
					if (titles.length) {
						this.reportTitle = titles.join(',').toString();
					}

					this.reportDate = new Date();
					this.reportProject =  this.userContext.project.name;
					this.reportBundle = this.getReportBundleName();

					this.applicationConflicts = results;
					this.isDisplayingReport = true;
					this.hideFilters = true;
				});
		}

	}

	/**
	 * Open the asset show view of the current application selected
	 * @param application: any
	 */
	onApplicationSelected(application: any): void {
		this.dialogService.open(AssetShowComponent, [
			{ provide: 'ID', useValue: application.id },
			{ provide: 'ASSET', useValue: application.assetClass }],
			DIALOG_SIZE.LG, false)
			.then(asset => {
				console.log('Done');
			}).catch(error => {
				console.log('Error:', error);
			});
	}
}

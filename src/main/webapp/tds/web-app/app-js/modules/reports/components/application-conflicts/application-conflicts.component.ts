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

	load() {
		this.isDisplayingReport = null;
		const commonCalls = [
			this.userService.getUserContext(),
			this.reportsService.getBundles()
		];

		// on init
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
					this.updateOwnersList(this.model.bundle);
				}
			});
	}

	getReportBundle(): string {
		const id = this.model.bundle && this.model.bundle.id || '';

		const bundle =  this.model.moveBundleList.find((bundle) => bundle.id === id);
		return bundle.name || '';
	}

	getReportOwner(): string {
		const id = this.model.appOwner && this.model.appOwner.id || '';

		const owner =  this.model.appOwnerList.find((owner) => owner.id === id);
		return owner.name || '';
	}

	onGenerateReport(): void {
		if (this.model.bundle) {
			this.reportsService.getApplicationConflicts(
				this.model.bundle.id.toString(),
				(this.model.appOwner && this.model.appOwner.id) || '',
				this.model.bundleConflict,
				this.model.missingDependencies,
				this.model.unresolvedDependencies,
				this.model.maxApplications.value
				)
				.subscribe((results: Array<ApplicationConflict>) => {
					this.reportDate = new Date();
					this.reportProject =  this.userContext.project.name;
					this.reportBundle = this.getReportBundle();
					this.reportOwner = this.getReportOwner();

					this.applicationConflicts = results;
					this.isDisplayingReport = true;
					this.hideFilters = true;
				});
		}

	}

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

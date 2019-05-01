import {
	ChangeDetectorRef,
	Component,
	OnInit
} from '@angular/core';

import {AlertType} from '../../../../shared/model/alert.model';

import {
	DomSanitizer,
	SafeHtml
} from '@angular/platform-browser';

import {
	Observable,
} from 'rxjs';

import {
	pathOr,
} from 'ramda';

import {ActivatedRoute} from '@angular/router';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ReportsService} from '../../service/reports.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UserService} from '../../../security/services/user.service';
import { ApplicationConflict } from '../../model/application-conflicts.model';
import {ReportComponent} from '../report.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

declare var jQuery: any;

@Component({
	selector: 'tds-application-conflicts',
	templateUrl: 'application-conflicts.component.html'
})
export class ApplicationConflictsComponent extends ReportComponent {
	invalidStatusList = ['Questioned', 'Unknown'];
	userContext = null;
	isDisplayingReport: boolean;
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
		private sanitizer: DomSanitizer,
		private route: ActivatedRoute,
		private changeDetectorRef: ChangeDetectorRef,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService,
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
			this.reportsService.getDefaults(),
			this.reportsService.getDefaultsApplicationConflicts(),
			this.userService.getUserContext(),
			this.reportsService.getBundles()
		];

		// on init
		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [events, defaultsApplication, userContext, bundles] = results;
				// this.model.moveBundleList = defaultsApplication.moveBundleList;
				// this.model.appOwnerList = defaultsApplication.appOwnerList;
				this.userContext = userContext;
				if (bundles) {
					this.model.moveBundleList = bundles.moveBundles
						.map((bundle: any) => ({id: bundle.id.toString(), name: bundle.name}));
					this.model.moveBundleList.unshift(this.planningBundles);
					console.log(this.model.moveBundleList);
					this.model.bundle = (bundles.moveBundleId) ? {id: bundles.moveBundleId} : this.planningBundles;
					this.updateOwnersList(this.model.bundle);
				}

				console.log(bundles);
			});
	}

	onGenerateReport(): void {
		if (this.model.bundle) {
			this.reportsService.getApplicationConflicts(
				this.model.bundle.id.toString(), 
				(this.model.appOwner && this.model.appOwner.id) || '',
				true, true, true, 100
				)
				.subscribe((results: Array<ApplicationConflict>) => {
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
				console.log('The results for the bundles:');
				console.log(results);
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

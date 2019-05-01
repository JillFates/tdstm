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

declare var jQuery: any;

@Component({
	selector: 'tds-application-conflicts',
	templateUrl: 'application-conflicts.component.html'
})
export class ApplicationConflictsComponent implements OnInit {
	invalidStatusList = ['Questioned', 'Unknown'];
	userContext = null;
	isDisplayingReport: boolean;
	public model = {
		moveBundleList: [],
		appOwnerList: [],
		defaultBundle: {id: -1, name: 'Planning Bundles'},
		defaultAppOwner: {id: -1, name: 'All'},
		bundleConflict: true,
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
		private reportsService: ReportsService) {
	}

	ngOnInit() {
		this.isDisplayingReport = null;
		const commonCalls = [
			this.reportsService.getDefaults(),
			this.reportsService.getDefaultsApplicationConflicts(),
			this.userService.getUserContext()
		];

		// on init
		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [events, defaultsApplication, userContext] = results;
				this.model.moveBundleList = defaultsApplication.moveBundleList;
				this.model.appOwnerList = defaultsApplication.appOwnerList;
				this.userContext = userContext;
			});
	}

	onGenerateReport(): void {
		this.reportsService.getApplicatioConflicts()
			.subscribe((results: Array<ApplicationConflict>) => {
				this.applicationConflicts = results;
				this.isDisplayingReport = true;
			});
	}

	// Enable disable the report view
	toggleFilters(value: any): void {
		if (this.isDisplayingReport == null) {
			return;
		}
		this.isDisplayingReport = !value;
	}

}

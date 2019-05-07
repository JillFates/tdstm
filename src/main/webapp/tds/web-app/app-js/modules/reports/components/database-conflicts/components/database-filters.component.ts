import {
	Input,
	Component
} from '@angular/core';

import {
	Observable,
} from 'rxjs';

import {ReportsService} from '../../../service/reports.service';
import {DatabaseFiltersModel} from '../../../model/database-filters.model';

@Component({
	selector: 'tds-database-filters',
	templateUrl: 'database-filters.component.html'
})
export class DatabaseFiltersComponent {
	@Input() filters: DatabaseFiltersModel;
	planningBundles: any;

	public model = {
		bundle: null,
		bundleConflict: true,
		unresolvedDependencies: true,
		missingApplications: true,
		unsupported: true,
		maxDatabases: {value: 100},

		moveBundleList: [],
		maxDatabasesList: [{value: 100}, {value: 250}, {value: 500}]
	};

	constructor(private reportsService: ReportsService) {
			this.planningBundles =  {id: 'useForPlanning', name: 'Planning Bundles'};
			this.load();
	}

	/**
	 * Load the bundle list
	 */
	load() {
		const commonCalls = [
			this.reportsService.getBundles()
		];

		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [bundles] = results;
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

}

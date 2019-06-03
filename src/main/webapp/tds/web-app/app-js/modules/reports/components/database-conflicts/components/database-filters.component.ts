import {
	Input,
	Output,
	EventEmitter,
	Component,
	OnInit,
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
export class DatabaseFiltersComponent implements OnInit {
	@Output() generateReport = new EventEmitter<any>();
	@Input() filters: DatabaseFiltersModel;
	planningBundles =  {id: 'useForPlanning', name: 'Planning Bundles'};
	moveBundleList: Array<any> = [];
	maxDatabasesList: Array<any> = [];

	constructor(private reportsService: ReportsService) {
		this.load();
	}

	ngOnInit() {
		this.maxDatabasesList = [{value: 100}, {value: 250}, {value: 500}];
	}

	/**
	 * Load the bundle list
	 */
	load() {

		this.reportsService.getBundles()
			.subscribe((bundles) => {
				if (bundles) {
					this.moveBundleList = bundles.moveBundles
						.map((bundle: any) => ({id: bundle.id.toString(), name: bundle.name}));
					this.moveBundleList.unshift(this.planningBundles);
					this.filters.bundle = (bundles.moveBundleId) ? {id: bundles.moveBundleId} : this.planningBundles;
				}
			});
	}

	/**
	 * Get the name of the current bundle selected
	*/
	getReportBundleName(): string {
		const id = this.filters.bundle && this.filters.bundle.id || '';

		const bundle =  this.moveBundleList.find((bundle) => bundle.id === id);
		return bundle.name || '';
	}

	/**
	 * Report to host component the generate report event
	*/
	onGenerateReport(): void {
		this.generateReport.emit({
			bundleName: this.getReportBundleName()
		});
	}

}

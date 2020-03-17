// Angular
import { Injectable } from '@angular/core';
import { Router, Resolve, ActivatedRouteSnapshot } from '@angular/router';
// Services
import { AssetExplorerService } from '../service/asset-explorer.service';
// Others
import { Observable } from 'rxjs';

@Injectable()
export class ReportResolveService implements Resolve<any> {
	constructor(private assetExplorerService: AssetExplorerService, private router: Router) {
	}

	/**
	 * Get the List of Reports used in Several Views
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		const reportId = route.params['id'];
		if (!reportId) {
			return this.assetExplorerService.getSaveOptions()
				.map(saveOptions => {
					return {
						saveOptions: saveOptions,
						isFavorite: false,
						isOwner: true,
						isShared: false,
						isSystem: false,
						schema: {
							columns: [],
							domains: []
						}
					};
				});
		} else {
			return this.assetExplorerService.getReport(reportId, { queryParamsObj: route.queryParams }).map(reports => {
				return reports;
			}).catch((err) => {
				console.error('ReportResolveService:', 'An Error Occurred trying to fetch Single Report ' + reportId);
				this.router.navigate(['/security/error']);
				return Observable.of(false);
			});
		}
	}

	/**
	 * Used to populate the dataview list of reports with the proper URL querystring argument
	 * when the view is an override so that the it results
	 * @param reports
	 */
	public populateReport(reports) {
		if (Array.isArray(reports)) {
			reports.map(rep => {
				if (Array.isArray(rep.items)) {
					rep.items.map((item) => {
						const _override: string = item.hasOverride ? 'false' : null;
						item.queryParams = { _override };
					})
				}
			})
		}
		return reports;
	}
}

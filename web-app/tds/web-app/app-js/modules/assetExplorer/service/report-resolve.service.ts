// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {AssetExplorerService} from './asset-explorer.service';
// Others
import {Observable} from 'rxjs';

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
		return this.assetExplorerService.getReport(reportId).map(reports => {
			return reports;
		}).catch((err) => {
			console.error('ReportResolveService:', 'An Error Occurred trying to fetch Single Report ' + reportId);
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
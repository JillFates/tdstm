// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {AssetExplorerService} from '../service/asset-explorer.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class ReportsResolveService implements Resolve<any> {
	constructor(private assetExplorerService: AssetExplorerService, private router: Router) {
	}

	/**
	 * Get the List of Reports used in Several Views
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.assetExplorerService.getReports().map(reports => {
			return reports;
		}).catch((err) => {
			console.error('ReportsResolveService:', 'An Error Occurred trying to fetch all Reports');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
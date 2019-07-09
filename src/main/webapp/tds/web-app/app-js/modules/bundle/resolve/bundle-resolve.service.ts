// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {BundleService} from '../service/bundle.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class BundleResolveService implements Resolve<any> {
	constructor(private bundleService: BundleService, private router: Router) {
	}

	/**
	 * Get the List of Bundles
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.bundleService.getBundles().map(bundles => {
			return bundles;
		}).catch((err) => {
			console.error('BundleService:', 'An Error Occurred trying to fetch Bundle List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
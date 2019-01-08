// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {LicenseAdminService} from '../service/license-admin.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class LicensesResolveService implements Resolve<any> {
	constructor(private licenseAdminService: LicenseAdminService, private router: Router) {
	}

	/**
	 * Get the List of Licenses
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.licenseAdminService.getLicenses().map((licenses: any) => {
			return licenses;
		}).catch((err: any) => {
			console.error('LicensesResolveService:', 'An Error Occurred trying to fetch License List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
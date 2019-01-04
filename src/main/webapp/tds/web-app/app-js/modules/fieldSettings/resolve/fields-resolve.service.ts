// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {FieldSettingsService} from '../service/field-settings.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class FieldsResolveService implements Resolve<any> {
	constructor(private fieldSettingsService: FieldSettingsService, private router: Router) {
	}

	/**
	 * Get the List of Common Fields and Domains Used on several views
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.fieldSettingsService.getFieldSettingsByDomain().map(domains => {
			return domains;
		}).catch((err) => {
			console.error('FieldsResolveService:', 'An Error Occurred trying to fetch Field List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {CustomDomainService} from '../../fieldSettings/service/custom-domain.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class FieldsResolveService implements Resolve<any> {
	constructor(private customDomainService: CustomDomainService, private router: Router) {
	}

	/**
	 * Get the List of Common Fields Used on several views
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.customDomainService.getCommonFieldSpecs().map(domains => {
			let commonIndex = domains.findIndex(x => x.domain.toUpperCase() === 'COMMON');
			if (commonIndex !== -1) {
				let common = domains.splice(commonIndex, 1);
				domains = common.concat(domains);
			}
			domains.forEach(d => {
				d.fields = d.fields.sort((a, b) => a.label > b.label ? 1 : b.label > a.label ? -1 : 0);
				d.fields.forEach(f => f['domain'] = d.domain.toLowerCase());
			});
			return domains;
		}).catch((err) => {
			console.error('FieldsResolveService:', 'An Error Occurred trying to fetch Field List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
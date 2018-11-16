// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {ProviderService} from '../service/provider.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class ProvidersResolveService implements Resolve<any> {
	constructor(private providerService: ProviderService, private router: Router) {
	}

	/**
	 * Get the List of Providers
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.providerService.getProviders().map(providers => {
			return providers;
		}).catch((err) => {
			console.error('ProvidersResolveService:', 'An Error Occurred trying to fetch Provider List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
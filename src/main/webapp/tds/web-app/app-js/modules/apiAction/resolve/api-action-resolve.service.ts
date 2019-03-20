// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {APIActionService} from '../service/api-action.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class APIActionResolveService implements Resolve<any> {
	constructor(private apiActionService: APIActionService, private router: Router) {
	}

	/**
	 * Get the List of Actions
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.apiActionService.getAPIActions().map(apiActions => {
			return apiActions;
		}).catch((err) => {
			console.error('APIActionResolveService:', 'An Error Occurred trying to fetch the API Action List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
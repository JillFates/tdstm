// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {DataScriptService} from '../service/data-script.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class DataScriptResolveService implements Resolve<any> {
	constructor(private dataScriptService: DataScriptService, private router: Router) {
	}

	/**
	 * Get the List of API Actions
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.dataScriptService.getDataScripts().map(dataScripts => {
			return dataScripts;
		}).catch((err) => {
			console.error('DataScriptResolveService:', 'An Error Occurred trying to fetch the DataScript List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
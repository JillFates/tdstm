// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {InsightService} from '../service/insight.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class InsightResolveService implements Resolve<any> {
	constructor(private insightService: InsightService, private router: Router) {
	}

	/**
	 * Get the List of Events
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		// return this.insightService.getInsightData().map(events => {
		// 	return events;
		// }).catch((err) => {
		// 	console.error('EventsResolveService:', 'An Error Occurred trying to fetch Event List');
		// 	this.router.navigate(['/security/error']);
		// 	return Observable.of(false);
		// });

		return null;
	}
}
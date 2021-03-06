// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {EventsService} from '../service/events.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class EventsResolveService implements Resolve<any> {
	constructor(private eventService: EventsService, private router: Router) {
	}

	/**
	 * Get the List of Events
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.eventService.getEventsForList().map(events => {
			return events;
		}).catch((err) => {
			console.error('EventsResolveService:', 'An Error Occurred trying to fetch Event List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}
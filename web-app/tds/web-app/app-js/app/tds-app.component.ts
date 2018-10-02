/**
 * Main Configuration for TDS App.
 * Do not add anything to this level, it works as the bootstrap entry point only
 */

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {NotifierService} from '../shared/services/notifier.service';

@Component({
	selector: 'tds-app',
	templateUrl: '../tds/web-app/app-js/app/tds-app.component.html',
})

export class TDSAppComponent implements OnInit {

	/**
	 * Inject the Router since it is the Route App
	 * It keep listen and inform any subscribe code about changes made to the routing
	 * @param router
	 * @param activatedRoute
	 * @param notifierService
	 */
	constructor(
		private router: Router,
		private activatedRoute: ActivatedRoute,
		private notifierService: NotifierService) {
	}

	ngOnInit(): void {
		this.router.events
			.filter((event) => event instanceof NavigationEnd)
			.map(() => this.activatedRoute)
			.map((route) => {
				while (route.firstChild) {
					route = route.firstChild;
				}
				return route;
			})
			.subscribe((event) => {
				this.notifierService.broadcast({
					name: 'notificationRouteNavigationEnd',
					route: event
				});
			});
	}
}

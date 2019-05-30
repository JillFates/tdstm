/**
 * Main Configuration for TDS App.
 * Do not add anything to this level, it works as the bootstrap entry point only
 */

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, NavigationStart, Router} from '@angular/router';
import {NotifierService} from '../shared/services/notifier.service';
import {NoticesValidatorService} from '../modules/user/service/notices-validator.service';

declare var jQuery: any;

@Component({
	selector: 'tds-app',
	template: `
        <tds-ui-loader></tds-ui-loader>
        <tds-ui-toast></tds-ui-toast>

        <tds-header></tds-header>
        <router-outlet></router-outlet>
	`,
})
export class TDSAppComponent implements OnInit {

	private readonly DISABLE_HEADER_ON_NAVIGATION_START = ['/notice'];

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
		private notifierService: NotifierService,
		private noticesValidatorService: NoticesValidatorService) {
	}

	ngOnInit(): void {
		// On Route Navigation Start
		this.router.events
			.filter(event => event instanceof NavigationStart)
			.subscribe((event: NavigationStart) => {
				// For some specific routes(cases) like notices we don't want the user to have
				// any interaction with the topNav menu, so we must do the following.
				if (this.DISABLE_HEADER_ON_NAVIGATION_START.includes(event.url)) {
					jQuery('.main-header').css('pointer-events', 'none');
				}
			});
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

		this.noticesValidatorService.setupValidation();
	}
}

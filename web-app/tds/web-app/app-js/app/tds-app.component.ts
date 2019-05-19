/**
 * Main Configuration for TDS App.
 * Do not add anything to this level, it works as the bootstrap entry point only
 */

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {NotifierService} from '../shared/services/notifier.service';
import {MandatoryNoticesValidatorService} from './services/mandatory-notices-validator.service';

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
		private mandatoryNoticesValidatorService: MandatoryNoticesValidatorService) {
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

		this.mandatoryNoticesValidatorService.setupCheck();
	}
}

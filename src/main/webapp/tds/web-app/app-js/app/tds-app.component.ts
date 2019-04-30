/**
 * Main Configuration for TDS App.
 * Do not add anything to this level, it works as the bootstrap entry point only
 */

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationStart, NavigationEnd, GuardsCheckStart, Router} from '@angular/router';
import {NotifierService} from '../shared/services/notifier.service';

@Component({
	selector: 'tds-app',
	template: `
		<tds-header></tds-header>
        <!-- Full Width Column -->
        <div class="content-wrapper">
            <div class="container">
                <tds-ui-loader></tds-ui-loader>
                <tds-ui-toast></tds-ui-toast>
                <tds-breadcrumb-navigation></tds-breadcrumb-navigation>
                <router-outlet></router-outlet>
            </div>
            <!-- /.container -->
        </div>
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
		private notifierService: NotifierService) {
	}

	ngOnInit(): void {
		this.handleTransitions();
	}

	/**
	 * Listen to the Transitions
	 */
	private handleTransitions(): void {
		// Specific filter to get the information from the current Page of the latest request event
		this.router.events
			.filter((event) => event instanceof NavigationStart || event instanceof NavigationEnd)
			.map((event) => ({route: this.activatedRoute, isNavigationStart: event instanceof NavigationStart}))
			.map((eventRoute) => {
				while (eventRoute.route.firstChild) {
					eventRoute.route = eventRoute.route.firstChild;
				}
				return eventRoute
			})
			.subscribe((eventRoute) => {
					// As soon as a transition start
				if (eventRoute.isNavigationStart) {
					this.notifierService.broadcast({
						name: 'notificationRouteChange'
					});
				} else {
					// As soon as a transition ends
					this.notifierService.broadcast({
						name: 'notificationRouteNavigationEnd',
						route: eventRoute.route
					});
					this.notifierService.broadcast( {
						name: 'httpRequestCompleted'
					});
				}
			});
	}
}

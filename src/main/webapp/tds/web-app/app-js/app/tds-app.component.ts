/**
 * Main Configuration for TDS App.
 * Do not add anything to this level, it works as the bootstrap entry point only
 */

// Angular
import {Component, OnInit} from '@angular/core';
import {
	ActivatedRoute,
	NavigationStart,
	NavigationEnd,
	Router,
	RoutesRecognized
} from '@angular/router';
// Services
import {NotifierService} from '../shared/services/notifier.service';
import {PostNoticesValidatorService} from '../modules/auth/service/post-notices-validator.service';

declare var jQuery: any;

@Component({
	selector: 'tds-app',
	template: `
        <tds-header></tds-header>
        <!-- Full Width Column -->
        <div class="content-wrapper">
            <div class="container">
                <tds-dialog></tds-dialog>
                <tds-ui-loader></tds-ui-loader>
                <tds-ui-toast></tds-ui-toast>
                <tds-breadcrumb-navigation></tds-breadcrumb-navigation>
                <router-outlet></router-outlet>
            </div>
            <!-- /.container -->
        </div>
        <tds-footer></tds-footer>
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
		private noticesValidatorService: PostNoticesValidatorService) {
	}

	ngOnInit(): void {
		this.handleTransitions();
	}

	/**
	 * Listen to the Transitions
	 */
	private handleTransitions(): void {
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

		// Specific filter to get the information from the current Page of the latest request event
		this.router.events
			.filter((event) => event instanceof NavigationStart || event instanceof NavigationEnd || event instanceof RoutesRecognized)
			.map((event) => (
				{
					route: this.activatedRoute,
					event: event,
					isNavigationStart: event instanceof NavigationStart,
					isNavigationEnd: event instanceof NavigationEnd,
					isRoutesRecognized: event instanceof RoutesRecognized
				}))
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
						name: 'notificationRouteChange',
						event: eventRoute.event
					});
				} else if (eventRoute.isNavigationEnd) {
					// As soon as a transition ends
					this.notifierService.broadcast({
						name: 'notificationRouteNavigationEnd',
						route: eventRoute.route
					});
					this.notifierService.broadcast({
						name: 'httpRequestCompleted'
					});
				}
			});
		// on the load init of the router this change was fired, now it will be from the beginning...
		// this.noticesValidatorService.setupValidation();
	}
}

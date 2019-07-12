/**
 * Services to restrict the access to Angular routes when the user has notices left to accept
 */

import {Injectable} from '@angular/core';
import {NavigationStart, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';

import {UserContextService} from './user-context.service';
import {Paths} from '../../../app/tds-routing.states';
import {PostNoticesManagerService} from './post-notices-manager.service';

@Injectable()
export class PostNoticesValidatorService {
	constructor(
		private router: Router,
		private userContext: UserContextService,
		private posNoticeManager: PostNoticesManagerService) {
	}

	/**
	 * Evaluates route changes, if the user has pending notices
	 * user is sent back to the notices
	 * The only route which doesn't have the restriction is /notice
	 */
	setupValidation(): void {
		this.router.events
			.filter((event) => event instanceof NavigationStart && event.url !== `/${Paths.notice}`)
			.pipe(
				switchMap((event) => this.userContext.getUserContext()),
				switchMap((context) => this.posNoticeManager.hasNoticesPending()),
			)
			.filter((hasPendings: boolean) => hasPendings === true)
			.subscribe(() => this.router.navigate([Paths.notice]),
				(error) => console.error(error));
	}
}

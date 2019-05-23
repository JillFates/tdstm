/**
 * Services to restrict the access to Angular routes when the user has mandatory notices pendings to accept
 */

import { Injectable } from '@angular/core';
import {NavigationStart, Router} from '@angular/router';
import { switchMap } from 'rxjs/operators';

import {UserPostNoticesContextService} from './user-post-notices-context.service';
import {Paths} from '../../../app/tds-routing.states';

@Injectable()
export class MandatoryNoticesValidatorService {
	private baseUri = '/tdstm'

	constructor(
		private router: Router,
		private userContext: UserPostNoticesContextService) {
	}

	/**
	 * Evaluates route changes, if the user has pending mandatory notices to agree
	 * user is sent back to the notices
	 * The only route which doesn't have the restriction is /notice
	 */
	setupValidation(): void {
		this.router.events
			.filter((event) => event instanceof NavigationStart && event.url !== `/${Paths.notice}`)
			.pipe(
				switchMap((event) => this.userContext.getUserContext()),
				switchMap((context) => context.postNoticesManager.hasMandatoryNoticesPending()),
			)
			.filter((hasPendings: boolean) => hasPendings === true)
			.subscribe(() => this.router.navigate([Paths.notice]),
				(error) => console.error(error));
	}
}
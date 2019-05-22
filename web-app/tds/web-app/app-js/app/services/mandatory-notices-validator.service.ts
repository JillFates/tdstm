/**
 * Services to restrict the access to Angular routes when the user has mandatory notices pendings to accept
 */

import { Injectable } from '@angular/core';
import {NavigationStart, Router} from '@angular/router';
import { switchMap } from 'rxjs/operators';

import {WindowService} from '../../shared/services/window.service';
import {UserService} from '../../modules/user/service/user.service';
import {Paths} from '../tds-routing.states';

@Injectable()
export class MandatoryNoticesValidatorService {
	private baseUri = '/tdstm'
	private signOutUri = `${this.baseUri}/auth/signOut`

	constructor(
		private router: Router,
		private userService: UserService,
		private windowService: WindowService) {
	}

	/**
	 * Evaluates route changes, if the user has pending mandatory notices to agree
	 * user is sent back to the notices
	 * The only route which doesn't have the restriction is /notice
	 */
	setupCheck(): void {
		this.router.events
			.filter((event) => event instanceof NavigationStart && event.url !== `/${Paths.notice}`)
			.pipe(
				switchMap((e) => this.userService.hasMandatoryNoticesPending())
			)
			.filter((hasPendings: boolean) => hasPendings === true)
			.subscribe(() => this.router.navigate([Paths.notice]),
				(error) => console.error(error));
	}
}
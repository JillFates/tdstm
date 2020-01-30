import { Component } from '@angular/core';
import {APP_STATE_KEY} from '../../../../shared/providers/localstorage.provider';
import {LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY} from '../../../../shared/model/constants';
import {Logout} from '../../../auth/action/login.actions';
import {Store} from '@ngxs/store';

@Component({
	selector: 'token-invalid-page',
	templateUrl: 'token-invalid-page.component.html',
})

export class TokenInvalidPageComponent {

	constructor(private store: Store) {

	}
	/**
	 * Destroy the Storage and redirect the user
	 */
	public logOut(): void {
		localStorage.removeItem(APP_STATE_KEY);
		localStorage.removeItem(LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY);
		this.store.dispatch(new Logout());
	}
}

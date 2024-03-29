/**
 * Permission Service keeps the reference of the requested permissions at the beginning of the App
 * it Also ensure the Permission are available
 * If a component request the permissions it will return the information on it or fetch if the list is empty
 */

// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// NGXS
import {Select, Store} from '@ngxs/store';
// Models
import {UserContextModel} from '../../modules/auth/model/user-context.model';
// Other
import {Observable} from 'rxjs';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class PermissionService {

	private permissionUrl = '../ws/security/permissions';
	private permissionsList = new BehaviorSubject([]);

	/**
	 * @param http
	 */
	constructor(
		private http: HttpClient,
		private store: Store) {
		// Add the user permissions
		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			if (userContext && userContext.user && userContext.permissions) {
				this.permissionsList.next(userContext.permissions);
			}
		});
	}

	/**
	 * Get the Permissions
	 * Validate if the Permissions are already set
	 */
	public getPermissions(): Observable<any> {
		if (this.permissionsList.getValue().length === 0) {
			return this.http.get(this.permissionUrl).map((response: any) => {
				this.permissionsList.next(response.data);
				return response.data;
			}).catch((error: any) => Observable.throw(error || 'Server error'));
		}
		return Observable.of(this.permissionsList);
	}

	/**
	 * Use a string to validate if the Permission exist on the list
	 * @param value
	 */
	public hasPermission(value: string): boolean {
		if (this.permissionsList.getValue()) {
			return this.permissionsList.getValue()[value] as boolean;
		} else {
			console.error('Permissions not defined');
			return false;
		}
	}
}

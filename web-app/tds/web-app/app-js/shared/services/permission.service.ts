import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

@Injectable()
export class PermissionService {

	private permissionUrl = '../ws/security/permissions';
	private permissionsList = new BehaviorSubject([]);
	private permissions = this.permissionsList.asObservable();

	/**
	 * @param http
	 */
	constructor(private http: HttpInterceptor) {}

	/**
	 * Get the Permissions
	 * Validate if the Permissions are already set
	 */
	public getPermissions(): Observable<any> {
		if (this.permissionsList.getValue().length === 0 ) {
			return this.http.get(this.permissionUrl)
				.map((res) => {
					let result = res.json();
					return this.permissionsList.next(result.data);
				})
				.catch((error: any) => Observable.throw(error.json() || 'Server error'));
		}
		return Observable.from(this.permissions);
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
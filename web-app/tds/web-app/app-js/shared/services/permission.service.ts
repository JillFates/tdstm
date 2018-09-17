import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import {catchError, map} from "rxjs/operators";

@Injectable()
export class PermissionService {

	private permissionUrl = '../ws/security/permissions';

	permissions: any;

	constructor(private http: HttpInterceptor) {
	}

	getPermissions(): Observable<any> {
		return this.http.get(this.permissionUrl)
			.pipe(map((res) => {
				let result = res.json();
				this.permissions = result.data;
				return this.permissions;
			}))
			.pipe(catchError((error: any) => Observable.throw(error.json() || 'Server error')));
	}

	hasPermission(value: string): boolean {
		if (this.permissions) {
			return this.permissions[value] as boolean;
		} else {
			console.log('ERROR: permissions is not defined');
			return false;
		}
	}
}
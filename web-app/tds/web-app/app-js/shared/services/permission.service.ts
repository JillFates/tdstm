import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class PermissionService {

	private permissionUrl = '../wsSecurity/permissions';

	permissions: any;

	constructor(private http: HttpInterceptor) {
	}

	getPermissions(): Observable<any> {
		let observable = Observable.from([{
			ProjectFieldSettingsEdit: 'ProjectFieldSettingsEdit',
			ProjectFieldSettingsView: 'ProjectFieldSettingsView'
		}]);
		observable.subscribe((res) => {
			this.permissions = res;
		});
		return observable;
	}

	hasPermission(value: string): boolean {
		if (this.permissions) {
			return this.permissions[value] as boolean;
		} else {
			console.log('ERROR: permissions isnt defined');
			return false;
		}
	}
}
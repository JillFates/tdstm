import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {PermissionService} from '../../../shared/services/permission.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class BulkChangeService {
	private bulkChangeUrl = '../ws/bulkChange';

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getFields(): Observable<any[]> {
		return this.http.get(`${this.bulkChangeUrl}/fields`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	update(dataViewId: number, assetIds: any[], edits: any[]): Observable<any> {
		const defaultParams = { userParams: { sortDomain: 'device', sortProperty: 'id', filters: {domains: ['device']}}, dataViewId: 1, assetIds: [], edits: []};
		const payload = Object.assign({}, defaultParams, {dataViewId, assetIds, edits});

		let body = JSON.stringify(payload);


		return this.http.put(this.bulkChangeUrl, body)
			.map((res: Response) => {
				console.log('============');
				console.log(res);
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => {
				console.log('============');
				console.log(error);
				return error;
				// error.json();
			});
	}
}
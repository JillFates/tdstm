import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class BulkChangeService {
	private bulkChangeUrl = '../ws/bulkChange';

	constructor(private http: HttpInterceptor) {}

	getFields(): Observable<any[]> {
		return this.http.get(`${this.bulkChangeUrl}/fields`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	update(dataViewId: number, assetIds: any[], edits: any[]): Observable<any> {
		const defaultUserParams = { sortDomain: 'device', sortProperty: 'id', filters: {domains: ['device']}};
		const defaultParams = { userParams: defaultUserParams, dataViewId: 1, assetIds: [], edits: []};
		const payload = Object.assign({}, defaultParams, {dataViewId, assetIds, edits});

		return this.http.put(this.bulkChangeUrl, JSON.stringify(payload))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => {
				return error;
			});
	}

	getActions(): Observable<any[]> {
		return this.http.get(`${this.bulkChangeUrl}/actions`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

}
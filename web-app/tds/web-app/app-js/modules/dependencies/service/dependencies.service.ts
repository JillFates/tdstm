import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Response, Headers} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {PermissionService} from '../../../shared/services/permission.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {switchMap} from 'rxjs/operators';

@Injectable()
export class DependenciesService {

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getDependencies(params: any): Observable<any> {
		const page: number = (params.skip / params.take) + 1;
		const queryString = `?_search=false&nd=1542120332047&rows=${params.take}&page=${page}&sidx=assetName&sord=asc`;
		const url = `../assetEntity/listDepJson${queryString}`

		return this.http.get(url)
			.pipe(
				switchMap((res: Response) => this.mapDependenciesOutputResult(params.take, res.json()))
			)
			/*
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
			*/
	}

	private mapDependenciesOutputResult(pageSize: number, legacyFormat: any): Observable<any> {
		return Observable.create((observer) => {
			const result = {
					assets: legacyFormat.rows.map((row) => {
						const [
							assetName, assetType, dependentBundle, type, dependentName, dependentType,
							assetBundle, multiField1, multiField2, status
						] = row.cell;

						return {
							assetName, assetType, dependentBundle, type, dependentName, dependentType,
							assetBundle, multiField1, multiField2, status
						}
					})
					.filter((item, index) => index <= pageSize - 1),
					pagination: {
						max: pageSize,
						offset: pageSize * (legacyFormat.page - 1),
						total: legacyFormat.records
					},
					status: 'success'
			};

			observer.next(result);
			observer.complete();
		});
	}
}
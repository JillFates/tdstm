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

	getDependencies(): Observable<any> {
		return this.http.get(`../assetEntity/listDepJson?_search=false&nd=1542120332047&rows=25&page=1&sidx=assetName&sord=asc`)
			.pipe(
				switchMap((res: Response) => this.mapDependenciesOutputResult(25, res.json()))
			)
			/*
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
			*/
	}

	private mapDependenciesOutputResult(pageSize: number, legacyFormat: any): Observable<any> {
		return Observable.create((observer) => {
			const result = {
				data: {
					assets: legacyFormat.rows.map((row) => {
						const [
							assetName, assetType, dependentBundle, type, dependentName, dependentType,
							assetBundle, multiField1, multiField2, status
						] = row.cell;

						return {
							assetName, assetType, dependentBundle, type, dependentName, dependentType,
							assetBundle, multiField1, multiField2, status
						}
					}),
					pagination: {
						max: pageSize,
						offset: pageSize * (legacyFormat.page - 1),
						total: legacyFormat.records
					},
					status: 'success'
				}
			};

			observer.next(result);
			observer.complete();
		});
	}
}
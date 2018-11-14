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
	}
}
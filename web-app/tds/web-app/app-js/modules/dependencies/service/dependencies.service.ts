import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import {PermissionService} from '../../../shared/services/permission.service';
import {DependencyResults} from '../model/dependencies.model';

import {map} from 'rxjs/operators';

@Injectable()
export class DependenciesService {

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getDependencies(params: any): Observable<DependencyResults> {
		const {skip, take, sort} = params;

		const page: number = (skip / take) + 1;
		const sorting = sort && sort.length ? sort[0] : {dir: 'asc', field: 'assetName'};
		let queryString = `?_search=false&rows=${take}&page=${page}&sidx=${sorting.field}&sord=${sorting.dir}`;
		const url = `../ws/asset/listDependencies${queryString}`;

		return this.http.get(url)
			.pipe(
				map((response: any) => {
					const results = response.json();
					const {dependencies = [], total = 0} = (results && results.data) || {};
					return {dependencies, total};
				})
			)
	}

	private mapDependenciesOutputResult(pageSize: number, legacyFormat: any): Observable<any> {
	}
}
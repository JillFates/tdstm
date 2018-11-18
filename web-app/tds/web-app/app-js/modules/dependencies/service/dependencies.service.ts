import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import {PermissionService} from '../../../shared/services/permission.service';
import {DependencyResults} from '../model/dependencies.model';

import {map} from 'rxjs/operators';

export interface DependenciesRequestParams {
	page: number;
	take: number;
	sorting: any;
	filters: any[];
}

@Injectable()
export class DependenciesService {

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getDependencies(params: DependenciesRequestParams): Observable<DependencyResults> {
		const {page, take, sorting, filters} = params;
		let queryString = '';

		queryString += `?rows=${take}&page=${page}&sidx=${sorting.field}&sord=${sorting.dir}`;
		queryString += filters.map(filter => `&${filter.field}=${filter.value}`).join('');
		queryString += `&_search=${filters.length ? 'true' : 'false'}`;
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
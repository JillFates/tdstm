import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import {PermissionService} from '../../../shared/services/permission.service';
import {DependencyResults} from '../model/dependencies.model';

import {map} from 'rxjs/operators';

export interface DependenciesRequestParams {
	page: number;
	take: number;
	sort: any;
	filters: any[];
}

@Injectable()
export class DependenciesService {
	private readonly BASE_URL = '/tdstm/ws/asset';
	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	/**
	 * Get the dependencies matched by the reques parameters
	 * @param {DependenciesRequestParams} params paremeters to filter and sorting
	 * @return {Observable<DependencyResults>)
	 */
	getDependencies(params: DependenciesRequestParams): Observable<DependencyResults> {
		const endpoint = `${this.BASE_URL}/listDependencies`;
		const {page, take, sort, filters} = params;

		const pagination = `rows=${take}&page=${page}`;
		const sorting = `&sidx=${sort.field}&sord=${sort.dir}`;
		const filtering  = filters.map(filter => `&${filter.field}=${filter.value}`).join('') +
			`&_search=${filters.length ? 'true' : 'false'}`;

		const url = `${endpoint}?${pagination}${sorting}${filtering}`;
		return this.http.get(`${url}`)
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